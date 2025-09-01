<template>
    <div>
        <MainNavbar />
        <div style="height: 140px;"></div>

        <!-- 麵包屑 -->
        <div class="bg-[#F8F5F0] dark:bg-dark-secondary py-5 md:py-[30px]">
            <div class="container-fluid">
                <ul class="flex items-center gap-[10px] text-base md:text-lg leading-none font-normal text-title dark:text-white max-w-[1720px] mx-auto flex-wrap">
                    <li><router-link to="/">Home</router-link></li>
                    <li>/</li>
                    <li><router-link to="/activity">活動</router-link></li>
                    <li>/</li>
                    <li class="text-primary">我的預約</li>
                </ul>
            </div>
        </div>

        <div class="s-py-50" data-aos="fade-up">
            <div class="container-fluid">
                <div class="max-w-[1100px] mx-auto">
                    <h1 class="text-3xl md:text-4xl font-bold mb-8 dark:text-white text-center">我的預約紀錄</h1>

                    <!-- [重點] 載入狀態 -->
                    <div v-if="loading" class="text-center py-16">
                        <div class="text-gray-600 dark:text-gray-300 text-lg">載入中...</div>
                    </div>

                    <!-- [重點] 錯誤狀態 -->
                    <div v-else-if="error" class="text-center py-16">
                        <div class="text-red-500 text-lg mb-4">{{ error }}</div>
                        <button @click="loadReservations" class="px-6 py-3 rounded-md transition-all bg-primary text-white font-medium hover:bg-primary-dark">
                            重新載入
                        </button>
                    </div>

                    <!-- [重點] 空狀態 -->
                    <div v-else-if="!reservations || reservations.length === 0" class="text-center py-16">
                        <div class="text-gray-600 dark:text-gray-300 text-lg mb-4">尚無預約紀錄</div>
                        <router-link to="/activity" class="px-6 py-3 rounded-md transition-all bg-primary text-white font-medium hover:bg-primary-dark">
                            立即預約活動
                        </router-link>
                    </div>

                    <!-- [重點] 預約清單 -->
                    <div v-else class="space-y-6">
                        <div v-for="reservation in reservations" :key="reservation.id" 
                            class="border border-bdr-clr dark:border-bdr-clr-drk rounded-2xl p-6 bg-white dark:bg-dark-secondary transition-all duration-300 hover:shadow-lg">
                            
                            <div class="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-4">
                                <!-- [重點] 預約基本資訊 -->
                                <div class="flex-1">
                                    <div class="flex items-start gap-4">
                                        <!-- 活動圖片縮圖 -->
                                        <div class="w-20 h-20 flex-shrink-0 rounded-lg overflow-hidden bg-gray-100">
                                            <img :src="reservation.activity?.imageUrl || fallbackImg" 
                                                :alt="reservation.activity?.name || '活動圖片'"
                                                class="w-full h-full object-cover"
                                                @error="onImageError" />
                                        </div>

                                        <!-- 活動詳情 -->
                                        <div class="flex-1 min-w-0">
                                            <h3 class="text-xl font-semibold text-title dark:text-white mb-2 line-clamp-2">
                                                {{ reservation.activity?.name || `活動 #${reservation.activityId}` }}
                                            </h3>

                                            <div class="space-y-1 text-sm text-gray-600 dark:text-gray-300">
                                                <p>
                                                    <span class="font-medium">預約人數：</span>{{ reservation.num || 1 }} 人
                                                </p>
                                                <p>
                                                    <span class="font-medium">預約時間：</span>{{ reservation.formattedScheduledAt }}
                                                </p>
                                                <p>
                                                    <span class="font-medium">活動時間：</span>{{ reservation.activity?.displayDate || '時間未定' }}
                                                </p>
                                                <p v-if="reservation.activity?.location">
                                                    <span class="font-medium">活動地點：</span>{{ reservation.activity.location }}
                                                </p>
                                            </div>
                                        </div>
                                    </div>
                                </div>

                                <!-- [重點] 狀態與操作 -->
                                <div class="flex flex-col sm:flex-row items-start sm:items-center gap-3 lg:flex-col lg:items-end">
                                    <!-- 預約狀態標籤 -->
                                    <div class="flex items-center gap-2">
                                        <span class="inline-block px-3 py-1 rounded-full text-sm font-medium"
                                            :class="getStatusClass(reservation.status)">
                                            {{ reservation.statusText }}
                                        </span>
                                    </div>

                                    <!-- [重點] 操作按鈕區 -->
                                    <div class="flex items-center gap-2">
                                        <!-- [重點] 取消報名按鈕 -->
                                        <button 
                                            v-if="reservation.status === '預約審核中'"
                                            @click="handleCancelReservation(reservation.id)"
                                            class="px-6 py-3 rounded-md transition-all bg-primary text-white font-medium hover:bg-primary-dark">
                                            取消報名
                                        </button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- [重點] 返回按鈕 -->
                    <div class="mt-12 text-center">
                        <router-link to="/activity" class="px-6 py-3 rounded-md transition-all bg-primary text-white font-medium hover:bg-primary-dark">
                            返回活動列表
                        </router-link>
                    </div>
                </div>
            </div>
        </div>

        <FooterThree />
        <ScrollToTop />
        
        <!-- [重點] 引入確認對話框元件 -->
        <ConfirmDialog />
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import Aos from 'aos'
import 'aos/dist/aos.css'

import MainNavbar from '@/components/navbar/main-navbar.vue'
import FooterThree from '@/components/footer/footer-three.vue'
import ScrollToTop from '@/components/scroll-to-top.vue'
// [重點] 引入確認對話框元件
import ConfirmDialog from '@/components/ConfirmDialog.vue'

// [重點] 使用 activityService
import { activityService } from '@/service/activityService'
// [重點] 使用確認對話框 composable
import { useConfirmDialog } from '@/composables/useConfirmDialog'

// [重點] 響應式資料
const loading = ref(true)
const error = ref('')
const reservations = ref([])

// [重點] 使用確認對話框的方法
const { showWarningDialog, showSuccessDialog, showErrorDialog } = useConfirmDialog()

// [重點] 圖片錯誤處理
const fallbackImg = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='80' height='80'%3E%3Crect fill='%23f3f4f6' width='100%25' height='100%25'/%3E%3Ctext x='50%25' y='50%25' dominant-baseline='middle' text-anchor='middle' fill='%236b7280' font-size='12'%3E活動%3C/text%3E%3C/svg%3E"

function onImageError(e) {
    e.target.src = fallbackImg
}

/**
 * [重點] 載入我的預約清單
 */
async function loadReservations() {
    loading.value = true
    error.value = ''
    
    try {
        // [重點] 使用 activityService 的 getMyReservations 方法
        const data = await activityService.getMyReservations()
        
        // [重點] 載入活動詳情
        reservations.value = await Promise.all(
            data.map(async (reservation) => {
                try {
                    // [重點] 載入對應的活動詳情
                    const activity = await activityService.getActivityById(reservation.activityId)
                    
                    return {
                        ...reservation,
                        activity: activity
                    }
                } catch (err) {
                    console.warn(`無法載入活動 ${reservation.activityId} 的詳情:`, err)
                    return {
                        ...reservation,
                        activity: null
                    }
                }
            })
        )
        
    } catch (e) {
        console.error('載入預約清單失敗:', e)
        error.value = e?.message || '載入預約清單失敗，請稍後再試'
    } finally {
        loading.value = false
    }
}

/**
 * [重點] 取消預約 - 傳入活動ID而非預約ID
 * @param {number} reservationId 預約紀錄的 ID (用於在前端找到對應的預約資料)
 */
async function handleCancelReservation(reservationId) {
    try {
        // [重點] 先找到對應的預約資料，以便取得活動 ID
        const reservation = reservations.value.find(r => r.id === reservationId)
        
        if (!reservation) {
            throw new Error('找不到對應的預約紀錄')
        }

        // [重點] 確保有活動ID
        if (!reservation.activityId) {
            throw new Error('預約記錄中缺少活動ID')
        }

        // [重點] 顯示詳細的預約資訊用於調試
        console.log('準備取消的預約詳情:', {
            reservationId: reservationId,           // 前端預約記錄ID
            activityId: reservation.activityId,     // 活動ID (要傳給後端的)
            activityName: reservation.activity?.name,
            status: reservation.status,
            num: reservation.num
        })

        // [重點] 使用 useConfirmDialog 顯示確認對話框
        const confirmed = await showWarningDialog(
            `確定要取消「${reservation.activity?.name || '此活動'}」的預約嗎？此操作無法復原。`,
            '確認取消預約',
            {
                confirmText: '確認取消',
                cancelText: '保留預約'
            }
        )
        
        // [重點] 如果使用者確認取消，則執行取消動作
        if (confirmed) {
            console.log('使用者確認取消，準備呼叫 API，活動ID:', reservation.activityId)
            
            // [重點] 呼叫 activityService 的 cancelReservation 方法，傳入活動ID
            const success = await activityService.cancelReservation(reservation.activityId)
            
            if (success) {
                // [重點] 使用成功對話框顯示結果
                await showSuccessDialog(
                    `「${reservation.activity?.name || '活動'}」的預約已成功取消。`,
                    '取消成功',
                    {
                        confirmText: '確定'
                    }
                )
                
                // [重點] 重新載入預約清單以更新狀態
                await loadReservations()
            } else {
                throw new Error('取消預約失敗，API 回傳失敗狀態')
            }
        }
    } catch (e) {
        console.error('取消預約失敗 - 詳細錯誤:', {
            error: e,
            message: e?.message,
            response: e?.response,
            reservationId: reservationId
        })
        
        // [重點] 使用錯誤對話框顯示錯誤訊息
        await showErrorDialog(
            e?.message || '取消預約失敗，請稍後再試。',
            '取消失敗',
            {
                confirmText: '確定'
            }
        )
    }
}

/**
 * [重點] 取得狀態樣式類別
 */
function getStatusClass(status) {
    switch (status) {
        case '預約審核中':
            return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200'
        case '報名成功':
            return 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200'
        case '取消預約':
        case '已取消':
            return 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200'
        default:
            return 'bg-gray-100 text-gray-800 dark:bg-gray-900 dark:text-gray-200'
    }
}

// [重點] 生命週期
onMounted(() => {
    Aos.init()
    loadReservations()
})
</script>

<style scoped>
/* [重點] 行數限制樣式 */
.line-clamp-2 {
    display: -webkit-box;
    -webkit-box-orient: vertical;
    overflow: hidden;
}

/* [重點] 小尺寸按鈕樣式 */
.btn-sm {
    padding: 0.375rem 0.75rem;
    font-size: 0.875rem;
    line-height: 1.25rem;
}

/* [重點] 危險按鈕樣式 */
.btn-outline-danger {
    border: 1px solid #ef4444;
    color: #ef4444;
    background-color: transparent;
    padding: 0.375rem 0.75rem;
    font-size: 0.875rem;
    line-height: 1.25rem;
    border-radius: 0.375rem;
    transition: all 0.3s ease;
    cursor: pointer;
}

.btn-outline-danger:hover {
    background-color: #ef4444;
    color: white;
}
</style>
