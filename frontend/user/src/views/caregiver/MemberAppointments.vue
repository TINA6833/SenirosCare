<template>
  <div>
    <!-- 導覽列 -->
    <main-navbar />
    
    <!-- 頂部留白，避免導覽列壓住內容 -->
    <div style="height: 140px;"></div>
    
    <!-- 頁面標題區 -->
    <div class="flex items-center gap-4 flex-wrap bg-overlay p-14 sm:p-16 before:bg-title before:bg-opacity-70"
         :style="{ backgroundImage: 'url(' + bg + ')' }">
      <div class="text-center w-full">
        <h2 class="text-white text-8 md:text-[40px] font-normal leading-none text-center">我的預約</h2>
        <ul class="flex items-center justify-center gap-[10px] text-base md:text-lg leading-none font-normal text-white mt-3 md:mt-4">
          <li><router-link to="/">首頁</router-link></li>
          <li>/</li>
          <li class="text-primary">我的預約</li>
        </ul>
        
        <!-- 導航按鈕 -->
        <div class="flex gap-3 justify-center mt-6">
          <button 
            @click="goToHome"
            class="px-6 py-3 bg-gradient-to-r from-blue-500 to-blue-600 text-white rounded-lg hover:from-blue-600 hover:to-blue-700 transition-all duration-200 flex items-center gap-2"
          >
            <i class="fas fa-home"></i>
            <span>回首頁</span>
          </button>
          
          <button 
            @click="goToCaregivers"
            class="px-6 py-3 bg-gradient-to-r from-pink-500 to-red-500 text-white rounded-lg hover:from-pink-600 hover:to-red-600 transition-all duration-200 flex items-center gap-2"
          >
            <i class="fas fa-user-nurse"></i>
            <span>回照服員</span>
          </button>
        </div>
      </div>
    </div>

    <!-- 主內容區域 -->
    <div class="container-fluid max-w-[1720px] mx-auto py-10">
      <!-- [重點] 認證檢查載入狀態 -->
      <div v-if="isCheckingAuth" class="flex justify-center items-center py-12">
        <div class="text-center">
          <div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
          <p class="mt-2 text-gray-600">正在驗證登入狀態...</p>
        </div>
      </div>

      <!-- [重點] 認證通過後才顯示頁面內容 -->
      <div v-else-if="isAuthenticated">
        <!-- 統計卡片區域 -->
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        <!-- 總預約數 -->
        <div class="bg-white rounded-lg shadow-lg p-6 hover:shadow-xl transition-shadow duration-300">
          <div class="flex items-center">
            <div class="flex-shrink-0">
              <div class="w-12 h-12 bg-blue-500 rounded-lg flex items-center justify-center">
                <i class="fas fa-calendar-alt text-white text-xl"></i>
              </div>
            </div>
            <div class="ml-4">
              <p class="text-sm font-medium text-gray-600">總預約數</p>
              <p class="text-2xl font-bold text-gray-900">{{ statistics.total }}</p>
            </div>
          </div>
        </div>

        <!-- 待審核 -->
        <div class="bg-white rounded-lg shadow-lg p-6 hover:shadow-xl transition-shadow duration-300">
          <div class="flex items-center">
            <div class="flex-shrink-0">
              <div class="w-12 h-12 bg-yellow-500 rounded-lg flex items-center justify-center">
                <i class="fas fa-clock text-white text-xl"></i>
              </div>
            </div>
            <div class="ml-4">
              <p class="text-sm font-medium text-gray-600">待審核</p>
              <p class="text-2xl font-bold text-gray-900">{{ statistics.pending }}</p>
            </div>
          </div>
        </div>

        <!-- 已確認 -->
        <div class="bg-white rounded-lg shadow-lg p-6 hover:shadow-xl transition-shadow duration-300">
          <div class="flex items-center">
            <div class="flex-shrink-0">
              <div class="w-12 h-12 bg-green-500 rounded-lg flex items-center justify-center">
                <i class="fas fa-check-circle text-white text-xl"></i>
              </div>
            </div>
            <div class="ml-4">
              <p class="text-sm font-medium text-gray-600">已確認</p>
              <p class="text-2xl font-bold text-gray-900">{{ statistics.approved }}</p>
            </div>
          </div>
        </div>

        <!-- 待評價 -->
        <div class="bg-white rounded-lg shadow-lg p-6 hover:shadow-xl transition-shadow duration-300">
          <div class="flex items-center">
            <div class="flex-shrink-0">
              <div class="w-12 h-12 bg-orange-500 rounded-lg flex items-center justify-center">
                <i class="fas fa-star text-white text-xl"></i>
              </div>
            </div>
            <div class="ml-4">
              <p class="text-sm font-medium text-gray-600">待評價</p>
              <p class="text-2xl font-bold text-gray-900">{{ statistics.pendingRatings }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 篩選區域 -->
      <div class="bg-white rounded-lg shadow-lg p-6 mb-8">
        <div class="grid grid-cols-1 md:grid-cols-4 gap-4 items-end">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">預約狀態</label>
            <select 
              class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent"
              v-model="filters.status"
              @change="updateFilters({ status: $event.target.value })"
            >
              <option value="">全部狀態</option>
              <option value="pending">待審核</option>
              <option value="approved">已確認</option>
              <option value="completed">已完成</option>
              <option value="cancelled">已取消</option>
              <option value="rejected">已拒絕</option>
            </select>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">開始日期</label>
            <input 
              type="date" 
              class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent"
              v-model="filters.dateFrom"
              @change="updateFilters({ dateFrom: $event.target.value })"
            />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">結束日期</label>
            <input 
              type="date" 
              class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent"
              v-model="filters.dateTo"
              @change="updateFilters({ dateTo: $event.target.value })"
            />
          </div>
          <div>
            <button 
              class="w-full px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors duration-200"
              @click="resetFilters"
            >
              重置篩選
            </button>
          </div>
        </div>
      </div>

      <!-- 載入狀態 -->
      <div v-if="loading" class="flex justify-center items-center py-12">
        <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"></div>
      </div>

      <!-- 錯誤狀態 -->
      <div v-else-if="error" class="bg-red-50 border border-red-200 rounded-lg p-4 mb-6">
        <div class="flex items-center">
          <i class="fas fa-exclamation-triangle text-red-500 mr-3"></i>
          <span class="text-red-700">{{ error }}</span>
          <button 
            class="ml-auto px-4 py-2 bg-red-500 text-white rounded-lg hover:bg-red-600 transition-colors duration-200"
            @click="loadMemberAppointments"
          >
            重新載入
          </button>
        </div>
      </div>

      <!-- 預約卡片網格 -->
      <div v-else class="grid grid-cols-1 lg:grid-cols-2 xl:grid-cols-3 gap-6">
        <div
          v-for="appointment in filteredAppointments"
          :key="appointment.id"
          class="bg-white rounded-lg shadow-lg overflow-hidden hover:shadow-xl transition-shadow duration-300"
        >
          <!-- 預約卡片頭部 -->
          <div class="p-6 border-b border-gray-200">
            <div class="flex items-center justify-between mb-4">
              <div class="flex items-center">
                <img 
                  :src="getCaregiverImage(appointment.caregiverAvatar || appointment.caregiverImagePath || appointment.caregiverPhoto)" 
                  :alt="appointment.caregiverName"
                  class="w-12 h-12 rounded-full object-cover border-2 border-gray-200"
                  @error="handleImageError"
                />
                <div class="ml-3">
                  <h3 class="text-lg font-semibold text-gray-800">{{ appointment.caregiverName || '未知照服員' }}</h3>
                  <p class="text-sm text-gray-600">{{ appointment.serviceTypeName || '未知服務' }}</p>
                </div>
              </div>
              
              <!-- 狀態標籤 -->
              <span 
                class="px-3 py-1 rounded-full text-sm font-medium"
                :class="getStatusClass(appointment.status)"
              >
                {{ appointment.statusDisplay }}
              </span>
            </div>
            
            <!-- 即將到來的預約提醒 -->
            <div v-if="isUpcomingAppointment(appointment)" class="bg-yellow-50 border border-yellow-200 rounded-lg p-3 mb-4">
              <div class="flex items-center">
                <i class="fas fa-exclamation-circle text-yellow-600 mr-2"></i>
                <span class="text-sm text-yellow-800">{{ getTimeUntilService(appointment) }}</span>
              </div>
            </div>
          </div>

          <!-- 預約詳情 -->
          <div class="p-6">
            <div class="space-y-3">
              <div class="flex items-center text-sm text-gray-600">
                <i class="fas fa-calendar-alt text-primary w-4 mr-3"></i>
                <span>開始：{{ appointment.formattedScheduledAt }}</span>
              </div>
              <div class="flex items-center text-sm text-gray-600">
                <i class="fas fa-clock text-secondary w-4 mr-3"></i>
                <span>結束：{{ appointment.formattedEndTime }}</span>
              </div>
              <div class="flex items-center text-sm text-gray-600">
                <i class="fas fa-hourglass-half text-info w-4 mr-3"></i>
                <span>時長：{{ appointment.serviceDurationDisplay }}</span>
              </div>
              <div class="flex items-center text-sm text-gray-600">
                <i class="fas fa-map-marker-alt text-warning w-4 mr-3"></i>
                <span class="truncate">{{ truncateText(appointment.serviceLocation, 30) }}</span>
              </div>
              <div class="flex items-center text-sm text-gray-600">
                <i class="fas fa-dollar-sign text-success w-4 mr-3"></i>
                <span class="font-semibold">{{ appointment.formattedTotalAmount }}</span>
              </div>
            </div>
          </div>

          <!-- 操作按鈕 -->
          <div class="px-6 pb-6">
            <div class="flex gap-2">
              <button 
                class="flex-1 px-3 py-2 border border-primary text-primary rounded-lg hover:bg-primary hover:text-white transition-colors duration-200 text-sm"
                @click="viewAppointmentDetail(appointment.id)"
              >
                <i class="fas fa-eye mr-1"></i>
                查看詳情
              </button>
              
              <button 
                v-if="canCancelAppointment(appointment)"
                class="flex-1 px-3 py-2 border border-red-500 text-red-500 rounded-lg hover:bg-red-500 hover:text-white transition-colors duration-200 text-sm"
                @click="confirmCancelAppointment(appointment)"
              >
                <i class="fas fa-times mr-1"></i>
                取消預約
              </button>
              
              <button 
                v-if="canRateAppointment(appointment)"
                class="flex-1 px-3 py-2 border border-yellow-500 text-yellow-500 rounded-lg hover:bg-yellow-500 hover:text-white transition-colors duration-200 text-sm"
                @click="openRatingModal(appointment)"
              >
                <i class="fas fa-star mr-1"></i>
                評價
              </button>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 空狀態 -->
      <div v-if="filteredAppointments.length === 0 && !loading" class="text-center py-12">
        <div class="text-gray-400 mb-4">
          <svg class="w-16 h-16 mx-auto" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"></path>
          </svg>
        </div>
        <h3 class="text-lg font-medium text-gray-900 mb-2">沒有找到預約記錄</h3>
        <p class="text-gray-500 mb-4">您還沒有任何預約記錄，或者沒有符合篩選條件的預約</p>
        <div class="flex gap-3 justify-center">
          <router-link 
            to="/caregiver" 
            class="px-6 py-3 bg-primary text-white rounded-lg hover:bg-primary-dark transition-colors duration-200"
          >
            <i class="fas fa-plus mr-2"></i>
            立即預約
          </router-link>
          <button 
            class="px-6 py-3 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors duration-200"
            @click="resetFilters"
          >
            清除篩選
          </button>
        </div>
      </div>
      </div>

      <!-- [重點] 未認證時顯示的內容 -->
      <div v-else class="flex justify-center items-center py-12">
        <div class="text-center">
          <i class="fas fa-lock text-4xl text-gray-400 mb-4"></i>
          <p class="text-gray-600">正在為您導向登入頁面...</p>
        </div>
      </div>
    </div>

    <!-- 取消預約確認彈窗 -->
    <CancelAppointmentDialog
      :visible="showCancelConfirm"
      :appointment="selectedAppointment"
      :submitting="submitting"
      @confirm="handleCancelConfirm"
      @cancel="showCancelConfirm = false"
      @close="showCancelConfirm = false"
    />

    <!-- 評價彈窗 -->
    <RatingModal
      :visible="showRatingModal"
      :appointment="selectedAppointment"
      :submitting="submitting"
      @submit="handleRatingSubmit"
      @close="showRatingModal = false"
    />
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthGuard } from '@/composables/useAuthGuard'
import { useAppointment } from '@/composables/useAppointments'
import CancelAppointmentDialog from '@/components/caregiver/CancelAppointmentDialog.vue'
import RatingModal from '@/components/caregiver/RatingModal.vue'
import MainNavbar from '@/components/navbar/main-navbar.vue'

// 背景圖片
import bg from '@/assets/img/bg/footer.jpg'

export default {
  name: 'MemberAppointments',
  components: {
    CancelAppointmentDialog,
    RatingModal,
    MainNavbar
  },
  setup() {
    const router = useRouter()
    
    // [重點] 使用認證守衛確保使用者已登入
    const { 
      isCheckingAuth, 
      isAuthenticated, 
      ensureAuthenticated 
    } = useAuthGuard({
      autoCheck: true,
      showToast: true,
      showLoading: true
    })
    const {
      // 狀態
      appointments,
      loading,
      submitting,
      error,
      filters,
      // 計算屬性
      filteredAppointments,
      formattedStatistics: statistics, // 重新命名為 statistics
      // 方法
      loadMemberAppointments,
      loadMemberStatistics, // 添加 loadMemberStatistics
      cancelAppointment,
      rateAppointment,
      updateFilters,
      resetFilters
    } = useAppointment()

    // 本地狀態
    const showCancelConfirm = ref(false)
    const showRatingModal = ref(false)
    const selectedAppointment = ref(null)

    /**
     * 初始化載入資料
     */
    onMounted(async () => {
      // [重點] 確保使用者已登入後再載入預約資料
      const authenticated = await ensureAuthenticated()
      if (authenticated) {
        await loadMemberAppointments()
        await loadMemberStatistics() // 載入統計資料
      }
      // 如果未登入，ensureAuthenticated 會自動處理跳轉到登入頁面
    })

    /**
     * 查看預約詳情
     */
    const viewAppointmentDetail = (appointmentId) => {
      router.push({
        name: 'AppointmentDetail',
        params: { id: appointmentId }
      })
    }

    /**
     * 確認取消預約
     */
    const confirmCancelAppointment = (appointment) => {
      selectedAppointment.value = appointment
      showCancelConfirm.value = true
    }

    /**
     * 處理取消確認
     */
    const handleCancelConfirm = async (reason = '') => {
      if (selectedAppointment.value) {
        try {
          // 傳遞取消原因給 cancelAppointment 方法
          const result = await cancelAppointment(selectedAppointment.value.id, reason)
          if (result && result.success) {
            showCancelConfirm.value = false
            selectedAppointment.value = null
            // 重新載入統計資料
            await loadMemberStatistics()
          }
        } catch (error) {
          console.error('取消預約失敗:', error)
          // 錯誤處理已經在 cancelAppointment 方法中處理
        }
      }
    }

    /**
     * 開啟評價彈窗
     */
    const openRatingModal = (appointment) => {
      selectedAppointment.value = appointment
      showRatingModal.value = true
    }

    /**
     * 處理評價提交
     */
    const handleRatingSubmit = async (ratingData) => {
      if (selectedAppointment.value) {
        try {
          const result = await rateAppointment(selectedAppointment.value.id, ratingData)
          if (result && result.success) {
            showRatingModal.value = false
            selectedAppointment.value = null
            // 重新載入統計資料
            await loadMemberStatistics()
          }
        } catch (error) {
          console.error('提交評價失敗:', error)
          // 錯誤處理已經在 rateAppointment 方法中處理
        }
      }
    }

    /**
     * 取得照服員圖片路徑（參考 CaregiverReviews 實作）
     * @param {string} imagePath - 圖片路徑
     * @returns {string} 完整的圖片URL
     */
    const getCaregiverImage = (imagePath) => {
      if (!imagePath) {
        try {
          return require('@/assets/img/thumb/error.png')
        } catch (err) {
          return '/src/assets/img/thumb/error.png'
        }
      }
      
      // 如果是完整的 URL
      if (imagePath.startsWith('http://') || imagePath.startsWith('https://')) {
        return imagePath
      }
      
      // 如果是 Base64 編碼的圖片數據
      if (imagePath.startsWith('data:image')) {
        return imagePath
      }
      
      // 統一處理相對路徑（參考後台 CaregiverTable.vue 的實作）
      // 移除可能的前導斜線，然後加上正確的基礎 URL
      const cleanPath = imagePath.startsWith('/') ? imagePath : '/' + imagePath
      const fullPath = `http://localhost:8080${cleanPath}`
      
      return fullPath
    }

    /**
     * 處理圖片載入錯誤（參考 CaregiverReviews 實作）
     */
    const handleImageError = (event) => {
      try {
        event.target.src = require('@/assets/img/thumb/error.png')
      } catch (err) {
        // 作為備用方案，使用一個簡單的 data URL 圖片
        event.target.src = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgdmlld0JveD0iMCAwIDIwMCAyMDAiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+CjxyZWN0IHdpZHRoPSIyMDAiIGhlaWdodD0iMjAwIiBmaWxsPSIjRjNGNEY2Ii8+CjxwYXRoIGQ9Ik0xMDAgNjBDMTE0LjMgNjAgMTI2IDcxLjcgMTI2IDg2QzEyNiAxMDAuMyAxMTQuMyAxMTIgMTAwIDExMkM4NS43IDExMiA3NCAxMDAuMyA3NCA4NkM3NCA3MS43IDg1LjcgNjAgMTAwIDYwWiIgZmlsbD0iIzlDQTNBRiIvPgo8cGF0aCBkPSJNNzQgMTQwSDEyNkMxMzAgMTQwIDEzNCAxNDAuOSAxMzYuOCAxNDMuMkMxMzkuMSAxNDYgMTQwIDE0OSAxNDAgMTUzVjE2NEg2MFYxNTNDNjAgMTQ5IDYwLjkgMTQ2IDYzLjIgMTQzLjJDNjYgMTQwLjkgNjkgMTQwIDc0IDE0MFoiIGZpbGw9IiM5Q0EzQUYiLz4KPC9zdmc+'
      }
    }

    /**
     * 截斷文字
     */
    const truncateText = (text, maxLength) => {
      if (!text) return ''
      if (text.length <= maxLength) return text
      return text.substring(0, maxLength) + '...'
    }

    /**
     * 取得狀態樣式類別 - 改為 Tailwind CSS 樣式
     */
    const getStatusClass = (status) => {
      const statusClasses = {
        pending: 'bg-yellow-100 text-yellow-800',
        approved: 'bg-green-100 text-green-800', 
        completed: 'bg-blue-100 text-blue-800',
        cancelled: 'bg-gray-100 text-gray-800',
        rejected: 'bg-red-100 text-red-800'
      }
      return statusClasses[status] || 'bg-gray-100 text-gray-800'
    }

    /**
     * 檢查是否為即將到來的預約
     */
    const isUpcomingAppointment = (appointment) => {
      if (!appointment.scheduledAt || appointment.status !== 'approved') return false
      
      const now = new Date()
      const scheduledTime = new Date(appointment.scheduledAt)
      const hoursUntil = (scheduledTime - now) / (1000 * 60 * 60)
      
      return hoursUntil > 0 && hoursUntil <= 24
    }

    /**
     * 取得距離服務時間的文字
     */
    const getTimeUntilService = (appointment) => {
      if (!appointment.scheduledAt) return ''
      
      const now = new Date()
      const scheduledTime = new Date(appointment.scheduledAt)
      const diff = scheduledTime - now
      
      if (diff <= 0) return '服務時間已過'
      
      const hours = Math.floor(diff / (1000 * 60 * 60))
      const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
      
      if (hours > 0) {
        return `${hours}小時${minutes}分鐘後`
      } else {
        return `${minutes}分鐘後`
      }
    }

    /**
     * 檢查是否可以取消預約
     */
    const canCancelAppointment = (appointment) => {
      if (!appointment) return false
      
      const cancellableStatuses = ['pending', 'approved']
      if (!cancellableStatuses.includes(appointment.status)) return false

      // 檢查是否在允許取消的時間範圍內（預約時間前24小時）
      if (appointment.scheduledAt) {
        const now = new Date()
        const scheduledTime = new Date(appointment.scheduledAt)
        const hoursUntilAppointment = (scheduledTime - now) / (1000 * 60 * 60)
        return hoursUntilAppointment >= 24
      }

      return true
    }

    /**
     * 檢查是否可以評分
     */
    const canRateAppointment = (appointment) => {
      return appointment && 
             appointment.status === 'completed' && 
             !appointment.isRated
    }

    /**
     * 導航到首頁
     */
    const goToHome = () => {
      router.push('/')
    }

    /**
     * 導航到照服員頁面
     */
    const goToCaregivers = async () => {
      try {
        // 檢查當前路由是否已經是照服員相關頁面
        const currentPath = router.currentRoute.value.path
        const targetPath = '/caregiver'
        
        if (currentPath === targetPath) {
          // 如果已經在目標頁面，強制重新載入
          window.location.reload()
        } else if (currentPath.includes('/caregiver') || currentPath.includes('/my-appointments')) {
          // 如果在照服員相關頁面，先跳轉再重新載入
          await router.push(targetPath)
          // 使用 nextTick 確保路由跳轉完成後再重新載入
          await router.isReady()
          window.location.reload()
        } else {
          // 正常的路由跳轉
          await router.push(targetPath)
        }
      } catch (error) {
        console.error('導航到照服員頁面失敗:', error)
        // 回退方案：直接使用 window.location
        window.location.href = '/caregiver'
      }
    }

    return {
      // 資料
      bg,
      
      // [重點] 認證狀態
      isCheckingAuth,
      isAuthenticated,
      
      // 狀態
      appointments,
      loading,
      submitting,
      error,
      filters,
      showCancelConfirm,
      showRatingModal,
      selectedAppointment,
      
      // 計算屬性
      filteredAppointments,
      statistics,
      
      // 方法
      loadMemberAppointments,
      updateFilters,
      resetFilters,
      viewAppointmentDetail,
      confirmCancelAppointment,
      handleCancelConfirm,
      openRatingModal,
      handleRatingSubmit,
      getCaregiverImage,
      handleImageError,
      truncateText,
      getStatusClass,
      isUpcomingAppointment,
      getTimeUntilService,
      canCancelAppointment,
      canRateAppointment,
      goToHome,
      goToCaregivers
    }
  }
}
</script>

<style scoped>
/* 自定義樣式 */
.bg-overlay {
  position: relative;
}

.bg-overlay::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: -1;
}

/* 響應式設計 */
@media (max-width: 768px) {
  .container-fluid {
    padding-left: 1rem;
    padding-right: 1rem;
  }
  
  .grid-cols-1 {
    grid-template-columns: 1fr;
  }
  
  .gap-6 {
    gap: 1rem;
  }
  
  .p-6 {
    padding: 1rem;
  }
}

@media (min-width: 640px) {
  .sm\:grid-cols-2 {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (min-width: 1024px) {
  .lg\:grid-cols-4 {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }
  
  .lg\:grid-cols-2 {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (min-width: 1280px) {
  .xl\:grid-cols-3 {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

/* 確保按鈕 hover 效果正常運作 */
.transition-colors {
  transition-property: color, background-color, border-color;
  transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1);
  transition-duration: 200ms;
}

.transition-shadow {
  transition-property: box-shadow;
  transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1);
  transition-duration: 300ms;
}

.transition-all {
  transition-property: all;
  transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1);
  transition-duration: 200ms;
}

/* 自定義 primary 顏色 */
.text-primary {
  color: #667eea;
}

.bg-primary {
  background-color: #667eea;
}

.border-primary {
  border-color: #667eea;
}

.bg-primary-dark {
  background-color: #5a6fd8;
}

.hover\:bg-primary:hover {
  background-color: #667eea;
}

.hover\:bg-primary-dark:hover {
  background-color: #5a6fd8;
}

.focus\:ring-primary:focus {
  --tw-ring-color: rgba(102, 126, 234, 0.5);
}

/* 自定義顏色變數 */
.text-secondary {
  color: #6c757d;
}

.text-info {
  color: #17a2b8;
}

.text-warning {
  color: #ffc107;
}

.text-success {
  color: #28a745;
}

/* 確保載入動畫正確運作 */
@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.animate-spin {
  animation: spin 1s linear infinite;
}
</style>
