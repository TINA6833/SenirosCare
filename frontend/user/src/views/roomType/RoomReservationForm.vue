<template>
  <!-- 重點註解：移除 AuthPrompt，因為使用者已在房型頁面完成登入 -->
  <div class="max-w-[900px] mx-auto py-12">
    <h2 class="text-2xl font-bold mb-6 text-center">預約表單</h2>
    <form @submit.prevent="onSubmit" class="bg-white rounded-lg shadow p-8 flex flex-col md:flex-row gap-8">
      <!-- 左側欄位 -->
      <div class="flex-1 flex flex-col gap-6">
        <div>
          <label class="block mb-2 font-medium">申請人姓名</label>
          <!-- 重點註解：顯示當前登入使用者姓名，唯讀 -->
          <input v-model="form.applicantName" type="text" class="input bg-gray-50" readonly />
        </div>
        <div>
          <label class="block mb-2 font-medium">聯絡電話</label>
          <!-- 重點註解：顯示當前登入使用者電話，唯讀 -->
          <input v-model="form.mainPhone" type="tel" class="input bg-gray-50" readonly />
        </div>
        <div>
          <label class="block mb-2 font-medium">房型</label>
          <input v-model="form.roomTypeName" type="text" class="input" :readonly="!!form.roomTypeName" required />
        </div>
        <div>
          <label class="block mb-2 font-medium">參觀人數(上限10人)</label>
          <input v-model.number="form.partySize" type="number" min="1" max="10" class="input" required
            @input="checkPartySize" />
        </div>
      </div>
      <!-- 右側欄位 -->
      <div class="flex-1 flex flex-col gap-6">
        <div>
          <label class="block mb-2 font-medium">預約日期</label>
          <input v-model="form.preferredDate" type="date" class="input" required :min="today" />
        </div>
        <div class="flex gap-4">
          <div class="flex-1">
            <label class="block mb-2 font-medium">開始時間</label>
            <select v-model="form.timeFrom" class="input" required @change="updateTimeToOptions">
              <option v-for="t in timeOptions" :key="t" :value="t">{{ t }}</option>
            </select>
          </div>
          <div class="flex-1">
            <label class="block mb-2 font-medium">結束時間</label>
            <select v-model="form.timeTo" class="input" required>
              <option v-for="t in timeToOptions" :key="t" :value="t">{{ t }}</option>
            </select>
          </div>
        </div>
        <div class="flex gap-6 items-end">
          <div class="flex-1">
            <label class="block mb-2 font-medium">備註</label>
            <!-- textarea 高度與 input 一致，底線一致 -->
            <textarea v-model="form.note" class="input h-[44px] resize-none" rows="1"></textarea>
          </div>
        </div>
        <div class="text-right mt-8">
          <button type="submit"
            class="px-8 py-3 bg-primary text-white rounded hover:bg-primary-dark transition text-lg">
            送出預約
          </button>
        </div>
      </div>
    </form>
    <!-- 重點註解：載入中狀態顯示 -->
    <div v-if="isLoading" class="mt-6 text-center text-gray-600">正在載入使用者資料...</div>
    <div v-if="successMsg" class="mt-6 text-green-600 text-center">{{ successMsg }}</div>
    <div v-if="errorMsg" class="mt-6 text-red-600 text-center">{{ errorMsg }}</div>
    <!-- 重點註解：確認對話框元件 -->
    <ConfirmDialog v-if="confirmDialog.isVisible" :title="confirmDialog.dialogData.title"
      :message="confirmDialog.dialogData.message" :type="confirmDialog.dialogData.type"
      :confirmText="confirmDialog.dialogData.confirmText" :cancelText="confirmDialog.dialogData.cancelText"
      :confirmButtonClass="confirmDialog.dialogData.confirmButtonClass" :icon="confirmDialog.dialogData.icon"
      @confirm="confirmDialog.confirmAction" @cancel="confirmDialog.cancelAction" @close="confirmDialog.closeDialog" />
    <!-- 重點註解：Toast 通知元件，請用 ToastContainer -->
    <ToastContainer />
  </div>
</template>

<script setup>
// 重點註解：引入 useConfirmDialog 與 useToast
import { ref, onMounted, defineProps, watch } from 'vue'
import { useRouter } from 'vue-router'
import { addReservation } from '@/service/reservationService'
import { useConfirmDialog } from '@/composables/useConfirmDialog'
import { useToast } from '@/composables/useToast'

// 重點註解：引入認證相關的 Composables
import { useAuth } from '@/composables/useAuth'

// 重點註解：引入 memberService 取得當前登入使用者資料
import { memberService } from '@/service/memberService'

// 重點註解：引入確認對話框與 ToastContainer 元件
import ConfirmDialog from '@/components/ConfirmDialog.vue'
import ToastContainer from '@/components/ToastContainer.vue'

// 重點註解：取得認證相關功能
const { currentUser } = useAuth()

// 取得今天日期（yyyy-MM-dd 格式）
const today = (() => {
  const d = new Date()
  const yyyy = d.getFullYear()
  const mm = String(d.getMonth() + 1).padStart(2, '0')
  const dd = String(d.getDate()).padStart(2, '0')
  return `${yyyy}-${mm}-${dd}`
})()

const props = defineProps({
  roomTypeId: Number,
  roomTypeName: String
})

const router = useRouter()

function generateTimeOptions() {
  const options = []
  for (let h = 9; h <= 16; h++) {
    for (let m = 0; m < 60; m += 30) {
      const hour = String(h).padStart(2, '0')
      const minute = String(m).padStart(2, '0')
      options.push(`${hour}:${minute}`)
    }
  }
  return options
}
const timeOptions = generateTimeOptions()
const timeToOptions = ref([])

// 重點註解：表單預設值移除假資料，將由 getCurrentMember 填入
const form = ref({
  memberId: null,
  applicantName: '',
  mainPhone: '',
  roomTypeId: props.roomTypeId || 1,
  roomTypeName: props.roomTypeName || '',
  partySize: 1,
  preferredDate: today,
  timeFrom: timeOptions[0],
  timeTo: timeOptions[1],
  note: ''
})

const successMsg = ref('')
const errorMsg = ref('')
// 重點註解：新增載入狀態
const isLoading = ref(false)

// 重點註解：取得確認對話框與 Toast 狀態
const confirmDialog = useConfirmDialog()
const toast = useToast()

function checkPartySize() {
  if (form.value.partySize > 10) {
    form.value.partySize = ''
  }
}

function updateTimeToOptions() {
  const idx = timeOptions.indexOf(form.value.timeFrom)
  timeToOptions.value = timeOptions.slice(idx + 1)
  if (!timeToOptions.value.includes(form.value.timeTo)) {
    form.value.timeTo = timeToOptions.value[0] || ''
  }
}

// 重點註解：載入當前登入使用者資料
async function loadCurrentMemberData() {
  try {
    isLoading.value = true
    const memberData = await memberService.getCurrentMember()

    // 重點註解：將使用者資料填入表單
    form.value.memberId = memberData.memberId
    form.value.applicantName = memberData.memberName
    form.value.mainPhone = memberData.mainPhone

    console.log('成功載入當前使用者資料:', {
      memberId: memberData.memberId,
      memberName: memberData.memberName,
      mainPhone: memberData.mainPhone
    })
  } catch (error) {
    console.error('載入使用者資料失敗:', error)
    errorMsg.value = '載入使用者資料失敗，請重新整理頁面'
    toast.showToast({
      title: '載入失敗',
      message: '無法取得使用者資料',
      type: 'error'
    })
  } finally {
    isLoading.value = false
  }
}

// 重點註解：監聽 currentUser 的變化，自動載入會員資料
watch(currentUser, (newUser) => {
  if (newUser) {
    loadCurrentMemberData()
  }
}, { immediate: true })

onMounted(async () => {
  if (props.roomTypeId) {
    form.value.roomTypeId = props.roomTypeId
  }
  if (props.roomTypeName) {
    form.value.roomTypeName = props.roomTypeName
  }
  updateTimeToOptions()
})

// 重點註解：送出前先彈出確認視窗
async function onSubmit() {
  errorMsg.value = ''
  successMsg.value = ''

  // 重點註解：檢查必要的使用者資料是否已載入
  if (!form.value.memberId || !form.value.applicantName || !form.value.mainPhone) {
    errorMsg.value = '使用者資料尚未載入完成，請稍後再試'
    return
  }

  try {
    const confirmed = await confirmDialog.showConfirmDialog({
      title: '送出預約',
      message: '您確定要送出預約申請嗎？',
      type: 'info',
      confirmText: '送出',
      cancelText: '取消',
      confirmButtonClass: 'btn-primary',
      icon: 'heroicons:information-circle'
    })
    if (confirmed) {
      await handleSubmit()
    }
  } catch {
    // 使用者取消，不做任何事
  }
}

// 重點註解：預約成功後用 Toast 通知
async function handleSubmit() {
  try {
    await addReservation(form.value)
    toast.showToast({
      title: '預約成功',
      message: '您的預約已送出',
      type: 'success'
    })
    setTimeout(() => {
      router.go(0)
    }, 1200)
  } catch (err) {
    errorMsg.value = '預約失敗，請稍後再試'
    toast.showToast({
      title: '預約失敗',
      message: '請稍後再試',
      type: 'error'
    })
  }
}
</script>

<style scoped>
.input {
  width: 100%;
  padding: 0.5rem 0.75rem;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 1rem;
  outline: none;
  box-sizing: border-box;
}

.input:focus {
  border-color: #1CB28E;
}

/* 重點註解：唯讀欄位樣式，灰色背景 */
.input.bg-gray-50 {
  background-color: #f9fafb;
  cursor: not-allowed;
}

form {
  min-width: 320px;
}

.max-w-\[900px\] {
  max-width: 900px;
}

/* 讓 textarea 高度與 input 一致 */
.h-\[44px\] {
  height: 44px;
}
</style>