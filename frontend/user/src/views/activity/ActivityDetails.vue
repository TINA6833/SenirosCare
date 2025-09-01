<template>
    <div>
        <MainNavbar />
        <div style="height: 140px;"></div>
        <!-- 麵包屑 -->
        <div class="bg-[#F8F5F0] dark:bg-dark-secondary py-5 md:py-[30px]">
            <div class="container-fluid">
                <ul
                    class="flex items-center gap-[10px] text-base md:text-lg leading-none font-normal text-title dark:text-white max-w-[1720px] mx-auto flex-wrap">
                    <li><router-link to="/">Home</router-link></li>
                    <li>/</li>
                    <li><router-link to="/activity">活動</router-link></li>
                    <li>/</li>
                    <li class="text-primary">{{ activity?.name || '活動詳情' }}</li>
                </ul>
            </div>
        </div>

        <div class="s-py-50" data-aos="fade-up">
            <div class="container-fluid">
                <div class="max-w-[1720px] mx-auto flex justify-between gap-10 flex-col lg:flex-row">
                    <!-- 左：只顯示單張主圖片 -->
                    <div class="w-full lg:w-[58%]">
                        <div class="relative">
                            <img :src="activityImageUrl" :alt="activity?.name || 'activity'"
                                class="w-full h-[500px] object-cover rounded-2xl" @error="onImageError" />
                        </div>
                    </div>

                    <!-- 右：內容/預約 -->
                    <div class="lg:max-w-[635px] w-full">
                        <div class="pb-4 sm:pb-6 border-b border-bdr-clr dark:border-bdr-clr-drk">
                            <h2 class="font-semibold text-2xl md:text-3xl leading-tight">
                                {{ activity?.name || '活動名稱' }}
                            </h2>

                            <div class="mt-4 space-y-2 text-[15px]">
                                <p><strong>分類：</strong>{{ activity?.category || '—' }}</p>
                                <p>
                                    <strong>日期：</strong>{{ activity?.displayDate || '日期未定' }}
                                    <span v-if="activity?.time">（{{ activity.time }}）</span>
                                </p>
                                <p><strong>地點：</strong>{{ activity?.location || '—' }}</p>
                                <p v-if="activity?.instructor"><strong>授課老師：</strong>{{ activity.instructor }}</p>
                                <p>
                                    <strong>名額：</strong>{{ activity?.current ?? 0 }}/{{ activity?.limit ?? '—' }}
                                    <span class="inline-block ml-2 px-2 py-[2px] rounded text-xs"
                                        :class="getStatusClass()">
                                        {{ getStatusText() }}
                                    </span>
                                </p>
                                <p>
                                    <strong>報名期間：</strong>{{ formatDate(activity?.registrationStart) }} ～ {{
                                    formatDate(activity?.registrationEnd) }}
                                </p>
                            </div>

                            <p class="sm:text-base mt-5 md:mt-6 whitespace-pre-line">
                                {{ activity?.description || '尚無活動介紹內容。' }}
                            </p>
                        </div>

                        <div class="py-4 sm:py-6 border-b border-bdr-clr dark:border-bdr-clr-drk" data-aos="fade-up"
                            data-aos-delay="200">
                            <div class="flex gap-4 mt-2 sm:mt-3">
                                <!-- 立即預約按鈕 -->
                                <button class="px-6 py-3 rounded-md transition-all bg-primary text-white font-medium hover:bg-primary-dark"
                                    @click="openReserveModal" :disabled="!activity?.canBook">
                                    立即預約
                                </button>

                                <!-- 返回列表按鈕 -->
                                <button class="px-6 py-3 rounded-md transition-all bg-primary text-white font-medium hover:bg-primary-dark"
                                    @click="goBack">
                                    返回列表
                                </button>
                            </div>

                            <!-- 提示訊息 -->
                            <p v-if="!activity?.canBook" class="text-xs text-gray-500 mt-2">
                                {{ getDisabledReason() }}
                            </p>
                        </div>

                        <!-- 載入/錯誤狀態 -->
                        <div v-if="loading" class="mt-6 text-sm opacity-70">載入中…</div>
                        <div v-else-if="error" class="mt-6 text-sm text-red-600">錯誤：{{ error }}</div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 預約人數 Modal -->
        <div v-if="showReserveModal" class="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
            <div class="bg-white dark:bg-title rounded-2xl p-6 w-[90%] max-w-[460px]">
                <h3 class="text-xl font-semibold mb-4 dark:text-white">預約活動</h3>

                <!-- 活動資訊 -->
                <div class="bg-gray-50 dark:bg-dark-secondary rounded-lg p-4 mb-4">
                    <h4 class="font-medium dark:text-white">{{ activity?.name }}</h4>
                    <p class="text-sm text-gray-600 dark:text-gray-300 mt-1">
                        {{ activity?.displayDate }} {{ activity?.time || '' }}
                    </p>
                    <p class="text-sm text-gray-600 dark:text-gray-300">
                        剩餘名額：{{ activity?.remainingSlots || 0 }} 人
                    </p>
                </div>

                <!-- 人數選擇 -->
                <div class="space-y-3 mb-6">
                    <label class="text-sm font-medium text-gray-700 dark:text-gray-300">
                        預約人數 <span class="text-red-500">*</span>
                    </label>

                    <div class="flex items-center gap-3">
                        <button type="button"
                            class="px-3 py-2 rounded-lg border bg-white text-gray-800 hover:bg-gray-50 dark:bg-dark-secondary dark:text-white disabled:opacity-50"
                            @click="decreaseNum" :disabled="reserveNum <= 1">
                            −
                        </button>

                        <!-- [重點] 確保 v-model.number 正確綁定 -->
                        <input type="number"
                            class="flex-1 border rounded-lg px-3 py-2 text-center dark:bg-dark-secondary dark:text-white"
                            v-model.number="reserveNum" :min="1" :max="activity?.remainingSlots || 1" placeholder="1"
                            @input="validateReservation" />

                        <button type="button"
                            class="px-3 py-2 rounded-lg border bg-white text-gray-800 hover:bg-gray-50 dark:bg-dark-secondary dark:text-white disabled:opacity-50"
                            @click="increaseNum" :disabled="reserveNum >= (activity?.remainingSlots || 1)">
                            ＋
                        </button>
                    </div>

                    <!-- 錯誤/成功訊息 -->
                    <p v-if="reserveError" class="text-red-500 text-sm">{{ reserveError }}</p>
                    <p v-if="reserveSuccess" class="text-green-600 text-sm">{{ reserveSuccess }}</p>
                </div>

                <!-- 按鈕區 -->
                <div class="flex justify-end gap-3">
                    <button class="btn btn-outline" data-text="取消" @click="closeReserveModal" :disabled="submitting">
                        <span>取消</span>
                    </button>

                    <button class="btn btn-solid" data-text="確認預約" @click="submitReservation"
                        :disabled="submitting || !isValidReservation">
                        <span>{{ submitting ? '預約中…' : '確認預約' }}</span>
                    </button>
                </div>
            </div>
        </div>

        <FooterThree />
        <ScrollToTop />
    </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Aos from 'aos'
import 'aos/dist/aos.css'

import MainNavbar from '@/components/navbar/main-navbar.vue';
import FooterThree from '@/components/footer/footer-three.vue';
import ScrollToTop from '@/components/scroll-to-top.vue';


// 使用 Service 層而非直接呼叫 API
import { activityService } from '@/service/activityService'


/* 路由/狀態 */
const route = useRoute()
const router = useRouter()
const id = ref(route.params.id)

const activity = ref(null)
const loading = ref(false)
const error = ref('')


/* 圖片處理 - 使用 Service 層處理過的 imageUrl */
const fallbackImg = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='900' height='600'%3E%3Crect fill='%23f3f4f6' width='100%25' height='100%25'/%3E%3Ctext x='50%25' y='50%25' dominant-baseline='middle' text-anchor='middle' fill='%236b7280' font-size='32'%3ENo%20Image%3C/text%3E%3C/svg%3E"


const activityImageUrl = computed(() => {
    return activity.value?.imageUrl || fallbackImg
})

function onImageError(e) {
    e.target.src = fallbackImg
}

/* 預約 Modal 狀態 */
const showReserveModal = ref(false)
const reserveNum = ref(1)
const submitting = ref(false)
const reserveError = ref('')
const reserveSuccess = ref('')

/* 工具函數 */
function formatDate(dateStr) {
    if (!dateStr) return '—'
    return new Date(dateStr).toLocaleDateString('zh-TW')
}

function goBack() {
    router.push('/activity')
}

/* 活動狀態判斷 */
function getStatusClass() {
    if (!activity.value) return 'bg-gray-200 text-gray-700'

    // 優先檢查活動是否停用
    if (!activity.value.status) {
        return 'bg-red-100 text-red-700'
    }

    // 檢查其他狀態
    if (activity.value.canBook) {
        return 'bg-green-100 text-green-700'
    } else if (activity.value.isFull) {
        return 'bg-red-100 text-red-700'
    } else if (activity.value.isExpired) {
        return 'bg-gray-100 text-gray-700'
    } else if (!activity.value.isRegistrationOpen) {
        return 'bg-yellow-100 text-yellow-700'
    } else {
        return 'bg-gray-100 text-gray-700'
    }
}

function getStatusText() {
    if (!activity.value) return '載入中'

    // 優先檢查活動狀態
    if (!activity.value.status) {
        return '活動已停用'
    }

    // 檢查其他狀態
    if (activity.value.canBook) {
        return '可預約'
    } else if (activity.value.isFull) {
        return '名額已滿'
    } else if (activity.value.isExpired) {
        return '活動已結束'
    } else if (!activity.value.isRegistrationOpen) {
        return '非報名期間'
    } else {
        return '暫停預約'
    }
}

function getDisabledReason() {
    if (!activity.value) return ''

    // 依優先級順序檢查無法預約的原因
    if (!activity.value.status) {
        return '此活動已被管理員停用'
    } else if (activity.value.isExpired) {
        return '活動已結束'
    } else if (activity.value.isFull) {
        return '活動名額已滿'
    } else if (!activity.value.isRegistrationOpen) {
        return '目前不在報名期間內'
    }

    return '目前無法預約此活動'
}

/* 預約相關驗證 */
const isValidReservation = computed(() => {
    return reserveNum.value >= 1 &&
        reserveNum.value <= (activity.value?.remainingSlots || 0) &&
        !reserveError.value
})

/* Modal 操作 */
function openReserveModal() {
    if (!activity.value?.canBook) return

    reserveNum.value = 1
    reserveError.value = ''
    reserveSuccess.value = ''
    showReserveModal.value = true
}

function closeReserveModal() {
    showReserveModal.value = false
    reserveError.value = ''
    reserveSuccess.value = ''
}

function decreaseNum() {
    if (reserveNum.value > 1) {
        reserveNum.value--
        validateReservation()
    }
}

function increaseNum() {
    const maxNum = activity.value?.remainingSlots || 1
    if (reserveNum.value < maxNum) {
        reserveNum.value++
        validateReservation()
    }
}

function validateReservation() {
    reserveError.value = ''

    // [重點] 確保 reserveNum 是有效數字
    const numValue = Number(reserveNum.value)

    if (!numValue || numValue < 1) {
        reserveError.value = '預約人數至少為 1 人'
        return false
    }

    const maxNum = activity.value?.remainingSlots || 0
    if (numValue > maxNum) {
        reserveError.value = `預約人數不能超過剩餘名額（${maxNum} 人）`
        return false
    }

    return true
}

/* 送出預約 */
async function submitReservation() {
    if (!validateReservation()) return

    submitting.value = true
    reserveError.value = ''

    try {
        // [重點] 確保預約資料格式正確
        const reservationData = {
            activityId: Number(activity.value.id),
            num: Number(reserveNum.value) // [重點] 確保轉換為數字
        }

        // [重點] 在發送前檢查資料
        console.log('準備發送的預約資料:', reservationData)

        // 使用 activityService 的預約方法
        const success = await activityService.bookActivity(reservationData)

        if (success) {
            reserveSuccess.value = '預約成功！即將跳轉到我的預約頁面'

            // 延遲跳轉，讓使用者看到成功訊息
            setTimeout(() => {
                closeReserveModal()
                router.push('/activity/my-reservations')
            }, 1500)
        }
    } catch (error) {
        console.error('預約失敗:', error)
        reserveError.value = error.message || '預約失敗，請稍後再試'
    } finally {
        submitting.value = false
    }
}

/* 載入活動資料 */
async function loadActivityData() {
    if (!id.value) {
        error.value = '無效的活動ID'
        return
    }

    loading.value = true
    error.value = ''

    try {
        // 使用 activityService 取得處理過的活動資料
        activity.value = await activityService.getActivityById(Number(id.value))
    } catch (e) {
        console.error('載入活動詳情失敗:', e)
        error.value = e.message || '載入活動詳情失敗'
    } finally {
        loading.value = false
    }
}

/* 生命週期 */
onMounted(() => {
    Aos.init()
    loadActivityData()
})

// 監聽路由變化，重新載入資料
watch(() => route.params.id, (newId) => {
    id.value = newId
    activity.value = null
    loadActivityData()
})

// 監聽預約人數變化，即時驗證
watch(() => reserveNum.value, () => {
    if (showReserveModal.value) {
        validateReservation()
    }
})
</script>

<style scoped>
/* 按鈕禁用狀態樣式 */
.btn:disabled {
    opacity: 0.6;
    cursor: not-allowed;
}

/* Modal 遮罩動畫 */
.fixed {
    animation: fadeIn 0.2s ease-out;
}

@keyframes fadeIn {
    from {
        opacity: 0;
    }

    to {
        opacity: 1;
    }
}

/* Modal 內容動畫 */
.bg-white,
.dark\:bg-title {
    animation: slideIn 0.3s ease-out;
}

@keyframes slideIn {
    from {
        opacity: 0;
        transform: translateY(-20px);
    }

    to {
        opacity: 1;
        transform: translateY(0);
    }
}
</style>
