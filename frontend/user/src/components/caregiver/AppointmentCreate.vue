<template>
  <div class="mx-auto max-w-3xl p-4 space-y-6">
    <h1 class="text-2xl font-semibold">預約照服員</h1>

    <!-- 照服員資訊（可換成你現有的 Card/Detail 元件） -->
    <section class="rounded-xl border p-4">
      <div class="font-medium">照服員：{{ caregiverName || '—' }}</div>
      <div class="text-sm text-gray-500">ID：{{ caregiverId }}</div>
    </section>

    <!-- 服務與時間 -->
    <section class="rounded-xl border p-4 space-y-4">
      <h2 class="font-medium">服務與時間</h2>

      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <label class="space-y-1">
          <span class="text-sm">服務類型</span>
          <select v-model="form.serviceTypeId" class="w-full rounded border p-2">
            <option value="" disabled>請選擇</option>
            <option v-for="t in serviceTypes" :key="t.id" :value="t.id">
              {{ t.name }}（{{ t.duration }} 小時）
            </option>
          </select>
          <p v-if="v$.serviceTypeId.$error" class="text-red-600 text-sm">請選擇服務類型</p>
        </label>

        <label class="space-y-1">
          <span class="text-sm">服務時數</span>
          <input v-model.number="form.hours" type="number" min="1" class="w-full rounded border p-2" />
          <p v-if="v$.hours.$error" class="text-red-600 text-sm">請輸入正整數時數</p>
        </label>
      </div>

      <!-- 時段挑選：改成你現有的 CaregiverSchedulePicker -->
      <div class="space-y-2">
        <span class="text-sm">選擇日期與時段</span>
        <!-- 假設你的元件支援 v-model:selectedSlot，回傳 { date: 'YYYY-MM-DD', start: 'HH:mm', end: 'HH:mm' } -->
        <CaregiverSchedulePicker
          :caregiver-id="caregiverId"
          v-model:selectedSlot="form.slot"
        />
        <p v-if="v$.slot.$error" class="text-red-600 text-sm">請選擇時段</p>
      </div>
    </section>

    <!-- 服務地點與照護對象資訊 -->
    <section class="rounded-xl border p-4 space-y-4">
      <h2 class="font-medium">服務地點與照護對象</h2>

      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <label class="space-y-1">
          <span class="text-sm">聯絡人姓名</span>
          <input v-model.trim="form.contactName" type="text" class="w-full rounded border p-2" />
          <p v-if="v$.contactName.$error" class="text-red-600 text-sm">請填寫聯絡人姓名</p>
        </label>

        <label class="space-y-1">
          <span class="text-sm">聯絡電話</span>
          <input v-model.trim="form.contactPhone" type="tel" class="w-full rounded border p-2" />
          <p v-if="v$.contactPhone.$error" class="text-red-600 text-sm">請填寫合法電話</p>
        </label>
      </div>

      <label class="space-y-1 block">
        <span class="text-sm">服務地址</span>
        <input v-model.trim="form.address" type="text" class="w-full rounded border p-2" placeholder="例：桃園市中壢區中大路300號" />
        <p v-if="v$.address.$error" class="text-red-600 text-sm">請填寫服務地址</p>
      </label>

      <label class="space-y-1 block">
        <span class="text-sm">備註（選填）</span>
        <textarea v-model.trim="form.note" class="w-full rounded border p-2" rows="3" placeholder="過敏、行動不便、看護需求等"></textarea>
      </label>
    </section>

    <!-- 送出 -->
    <div class="flex items-center gap-3">
      <button
        class="rounded-xl px-4 py-2 border bg-black text-white disabled:opacity-50"
        :disabled="submitting || v$.$invalid"
        @click="onSubmit"
      >
        {{ submitting ? '送出中…' : '送出預約' }}
      </button>
      <p v-if="error" class="text-red-600">{{ error }}</p>
      <p v-if="successMessage" class="text-green-700">{{ successMessage }}</p>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, computed } from 'vue'
import { useAppointment } from '@/composables/useAppointments'
import { serviceTypeApi } from '@/api/serviceTypeApi'
// 換成你專案實際路徑
import CaregiverSchedulePicker from '@/components/CaregiverSchedulePicker.vue'

// ---- 接收路由或父層傳入的照服員資訊 ----
const props = defineProps({
  caregiverId: { type: Number, required: true },
  caregiverName: { type: String, default: '' },
})

// ---- 表單資料 ----
const form = reactive({
  caregiverId: props.caregiverId,
  serviceTypeId: '',
  hours: 2,
  slot: null, // { date: 'YYYY-MM-DD', start: 'HH:mm', end: 'HH:mm' }
  contactName: '',
  contactPhone: '',
  address: '',
  note: ''
})

// ---- 服務類型選單 ----
const serviceTypes = ref([])
onMounted(async () => {
  try {
    const { data } = await serviceTypeApi.list()
    serviceTypes.value = data
  } catch (e) {
    // 若沒有服務類型 API，可先用硬编码塞幾筆測試資料
    serviceTypes.value = [
      { id: 1, name: '居家照護', duration: 2 },
      { id: 2, name: '陪同就醫', duration: 3 },
    ]
  }
})

// ---- 驗證（用最簡單的手寫驗證；若專案有 Vuelidate/Yup 可換成你們慣用方案） ----
const v$ = computed(() => ({
  serviceTypeId: { $error: !form.serviceTypeId },
  hours: { $error: !(Number.isInteger(form.hours) && form.hours > 0) },
  slot: { $error: !(form.slot && form.slot.date && form.slot.start && form.slot.end) },
  contactName: { $error: !form.contactName },
  contactPhone: { $error: !/^[0-9\-+()\s]{6,}$/.test(form.contactPhone) },
  address: { $error: !form.address },
  $invalid: false
}))
// 計算 $invalid
v$.value.$invalid =
  v$.value.serviceTypeId.$error ||
  v$.value.hours.$error ||
  v$.value.slot.$error ||
  v$.value.contactName.$error ||
  v$.value.contactPhone.$error ||
  v$.value.address.$error

// ---- 送出 ----
const { submitting, error, createAppointment } = useAppointment()
const successMessage = ref('')

const onSubmit = async () => {
  // 重新算一次 invalid
  const invalid =
    !form.serviceTypeId ||
    !(Number.isInteger(form.hours) && form.hours > 0) ||
    !(form.slot && form.slot.date && form.slot.start && form.slot.end) ||
    !form.contactName ||
    !/^[0-9\-+()\s]{6,}$/.test(form.contactPhone) ||
    !form.address

  if (invalid) return

  // 依照後端格式組 payload
  const payload = {
    caregiverId: form.caregiverId,
    serviceTypeId: form.serviceTypeId,
    hours: form.hours,
    date: form.slot.date,        // '2025-08-26'
    startTime: form.slot.start,  // '09:00'
    endTime: form.slot.end,      // '11:00'
    contactName: form.contactName,
    contactPhone: form.contactPhone,
    address: form.address,
    note: form.note
  }

  try {
    const data = await createAppointment(payload)
    successMessage.value = `預約成功！訂單編號：${data?.id ?? '—'}`
    // 這裡可導向訂單詳情頁：router.push({ name: 'AppointmentDetail', params: { id: data.id } })
  } catch (e) {
    // 錯誤已在 composable 設定到 error.value
  }
}
</script>

<style scoped>
/* 可換成 Tailwind；此處預留簡單樣式 */
</style>
