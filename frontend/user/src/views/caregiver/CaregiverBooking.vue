<template>
  <div class="caregiver-booking-page">
    <!-- 頁面標題區域 -->
    <div class="page-header bg-overlay p-14 sm:p-16 before:bg-title before:bg-opacity-70"
         :style="{ backgroundImage: 'url(' + bg + ')' }">
      <div class="container">
        <div class="page-title-section">
          <h1 class="page-title">
            <i class="fas fa-calendar-plus me-3"></i>
            預約照服員服務
          </h1>
          <p class="page-subtitle"></p>
          
          <!-- [重點] 導航按鈕 -->
          <div class="navigation-buttons">
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
    </div>

    <!-- 主要內容區域 -->
    <div class="page-content">
      <div class="container">
        
        <!-- 載入狀態 -->
        <div v-if="initialLoading" class="loading-container">
          <div class="text-center py-5">
            <div class="spinner-border text-primary" role="status">
              <span class="sr-only">載入中...</span>
            </div>
            <p class="mt-3 text-muted">載入照服員資料中...</p>
          </div>
        </div>

        <!-- 錯誤狀態 -->
        <div v-else-if="loadError" class="error-container">
          <div class="alert alert-danger text-center">
            <i class="fas fa-exclamation-triangle fa-3x mb-3"></i>
            <h4>載入失敗</h4>
            <p>{{ loadError }}</p>
            <button 
              class="btn btn-primary"
              @click="initializePage"
            >
              <i class="fas fa-redo me-1"></i>
              重新載入
            </button>
          </div>
        </div>

        <!-- 預約表單 -->
        <div v-else class="booking-content">
          <div class="row">
            
            <!-- 左側：預約表單 -->
            <div class="col-lg-8">
              <AppointmentForm
                :caregiver-id="caregiverId"
                :caregiver-info="caregiverData"
                @success="handleBookingSuccess"
                @cancel="handleBookingCancel"
              />
            </div>

            <!-- 右側：服務保證 -->
            <div class="col-lg-4">
              <div class="sidebar">

                <!-- 服務保證卡片 -->
                <div class="info-card service-guarantee-card">
                  <div class="card-header">
                    <h5 class="card-title">
                      <i class="fas fa-shield-alt me-2"></i>
                      服務保證
                    </h5>
                  </div>
                  <div class="card-body">
                    <div class="guarantee-list">
                      <div class="guarantee-item">
                        <i class="fas fa-check-circle text-success"></i>
                        <span>專業證照認證</span>
                      </div>
                      
                      <div class="guarantee-item">
                        <i class="fas fa-check-circle text-success"></i>
                        <span>保險全額保障</span>
                      </div>
                      
                      <div class="guarantee-item">
                        <i class="fas fa-check-circle text-success"></i>
                        <span>24小時客服支援</span>
                      </div>
                      
                      <div class="guarantee-item">
                        <i class="fas fa-check-circle text-success"></i>
                        <span>服務品質滿意保證</span>
                      </div>
                    </div>
                  </div>
                </div>

              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 成功提示模態框 -->
    <div 
      v-if="showSuccessModal"
      class="modal fade show"
      style="display: block;"
      @click.self="closeSuccessModal"
    >
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
          <div class="modal-header bg-success text-white">
            <h5 class="modal-title">
              <i class="fas fa-check-circle me-2"></i>
              預約成功
            </h5>
            <button 
              type="button" 
              class="btn-close btn-close-white"
              @click="closeSuccessModal"
            ></button>
          </div>
          <div class="modal-body text-center">
            <i class="fas fa-calendar-check fa-4x text-success mb-3"></i>
            <h4>預約提交成功！</h4>
            <p class="text-muted mb-3">
              您的預約申請已送出，我們將在1個工作日內聯繫您確認服務細節。
            </p>
            <div v-if="bookingResult" class="booking-info">
              <p><strong>預約編號：</strong>{{ bookingResult.appointmentId }}</p>
            </div>
          </div>
          <div class="modal-footer justify-content-center">
            <button 
              type="button" 
              class="btn btn-primary"
              @click="goToAppointments"
            >
              <i class="fas fa-list me-1"></i>
              查看我的預約
            </button>
            <button 
              type="button" 
              class="btn btn-secondary"
              @click="closeSuccessModal"
            >
              關閉
            </button>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 模態框背景遮罩 -->
    <div 
      v-if="showSuccessModal"
      class="modal-backdrop fade show"
    ></div>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAppointment } from '@/composables/useAppointments'
import AppointmentForm from '@/components/caregiver/AppointmentForm.vue'

// 背景圖片
import bg from '@/assets/img/bg/footer.jpg'

export default {
  name: 'CaregiverBooking',
  components: {
    AppointmentForm
  },
  setup() {
    const router = useRouter()
    const route = useRoute()
    
    // 取得路由參數中的照服員ID - 符合您的路由結構
    const caregiverId = computed(() => {
      return parseInt(route.params.caregiverId) || parseInt(route.params.id)
    })
    
    // 響應式狀態
    const initialLoading = ref(true)
    const loadError = ref(null)
    const caregiverData = ref(null)
    const showSuccessModal = ref(false)
    const bookingResult = ref(null)
    
    // 使用預約組合函數
    const {
      loading,
      error,
      loadCurrentMember
    } = useAppointment()

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
     * @param {Event} event - 錯誤事件
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
     * 格式化電話號碼
     * @param {string} phone - 電話號碼
     * @returns {string} 格式化後的電話號碼
     */
    const formatPhone = (phone) => {
      if (!phone) return ''
      
      // 簡單的電話號碼格式化，可根據需要調整
      const cleaned = phone.replace(/\D/g, '')
      if (cleaned.length === 10) {
        return cleaned.replace(/(\d{4})(\d{3})(\d{3})/, '$1-$2-$3')
      }
      return phone
    }

    /**
     * 載入照服員資料
     * 直接使用 API 層獲取照服員資料
     */
    const loadCaregiverData = async () => {
      try {
        // 驗證照服員ID
        if (!caregiverId.value || caregiverId.value <= 0) {
          throw new Error('無效的照服員ID')
        }
        
        // 直接使用 caregiverApi 而不是 service 層
        const { caregiverApi } = await import('@/api/caregiverApi')
        const response = await caregiverApi.getCaregiverById(caregiverId.value)
        
        // 檢查回應是否包含有效資料
        if (!response || !response.data) {
          throw new Error('照服員資料回應格式不正確')
        }
        
        // 強制清空舊資料再設定新資料
        caregiverData.value = null
        await new Promise(resolve => setTimeout(resolve, 10)) // 確保 DOM 更新
        
        // 正確提取實際的照服員資料（response.data.data）
        caregiverData.value = response.data.data
        
      } catch (err) {
        console.error('載入照服員資料失敗:', err)
        throw new Error(err.message || '無法載入照服員資料')
      }
    }

    /**
     * 初始化頁面
     */
    const initializePage = async () => {
      try {
        initialLoading.value = true
        loadError.value = null
        
        // 驗證照服員ID
        if (!caregiverId.value || caregiverId.value <= 0) {
          throw new Error('無效的照服員ID')
        }
        
        // 先載入照服員資料（不依賴會員資料）
        await loadCaregiverData()
        
        // 非同步載入會員資料（失敗不影響照服員資料顯示）
        loadCurrentMember().catch(() => {
          console.warn('會員資料載入失敗，但不影響照服員資料顯示')
        })
        
      } catch (err) {
        console.error('頁面初始化失敗:', err)
        loadError.value = err.message || '頁面載入失敗'
      } finally {
        initialLoading.value = false
      }
    }

    /**
     * 處理預約成功
     * @param {Object} result - 預約結果
     */
    const handleBookingSuccess = (result) => {
      bookingResult.value = result
      showSuccessModal.value = true
    }

    /**
     * 處理預約取消
     */
    const handleBookingCancel = () => {
      router.go(-1) // 返回上一頁
    }

    /**
     * 關閉成功提示模態框
     */
    const closeSuccessModal = () => {
      showSuccessModal.value = false
      bookingResult.value = null
    }

    /**
     * 跳轉到預約列表頁面
     */
    const goToAppointments = () => {
      closeSuccessModal()
      // 重點註解：在跳轉時添加成功參數，用於顯示預約成功通知
      router.push('/my-appointments?booking_success=1')
    }

    /**
     * [重點] 回到首頁
     */
    const goToHome = () => {
      router.push('/')
    }

    /**
     * [重點] 回到照服員列表
     */
    const goToCaregivers = () => {
      router.push('/caregiver')
    }

    // 組件掛載時初始化
    onMounted(() => {
      initializePage()
    })

    return {
      // 狀態
      caregiverId,
      initialLoading,
      loadError,
      caregiverData,
      showSuccessModal,
      bookingResult,
      bg, // 背景圖片
      
      // 來自組合函數的狀態
      loading,
      error,
      
      // 方法
      getCaregiverImage,
      handleImageError,
      formatPhone,
      initializePage,
      handleBookingSuccess,
      handleBookingCancel,
      closeSuccessModal,
      goToAppointments,
      goToHome,
      goToCaregivers
    }
  }
}
</script>

<style scoped>
.caregiver-booking-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.page-header {
  position: relative;
  color: white;
  min-height: 300px;
  display: flex;
  align-items: center;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
}

/* 背景遮罩樣式 */
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
  z-index: 1;
}

.page-header .container {
  position: relative;
  z-index: 2;
}

.breadcrumb {
  margin-bottom: 16px;
  background: none;
  padding: 0;
}

.breadcrumb-item + .breadcrumb-item::before {
  content: ">";
  color: #6c757d;
}

.page-title-section {
  text-align: center;
  margin-top: 10px;
}

.page-title {
  color: white;
  font-weight: 700;
  margin-bottom: 8px;
}

.page-subtitle {
  color: rgba(255, 255, 255, 0.9);
  font-size: 1.1rem;
  margin-bottom: 20px;
}

/* [重點] 導航按鈕樣式 */
.navigation-buttons {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  gap: 12px;
}

/* 響應式設計 */
@media (max-width: 768px) {
  .navigation-buttons {
    flex-direction: column;
    align-items: center;
    gap: 8px;
  }
  
  .navigation-buttons button {
    width: 200px;
    justify-content: center;
  }
}

.page-subtitle {
  color: #666;
  font-size: 1.1rem;
  margin-bottom: 0;
}

.page-content {
  padding: 40px 0;
}

.loading-container,
.error-container {
  min-height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.booking-content {
  margin-top: 20px;
}

.sidebar {
  position: sticky;
  top: 20px;
}

.info-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.07);
  margin-bottom: 20px;
  overflow: hidden;
}

.card-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 16px 20px;
  border-bottom: none;
}

.card-title {
  margin: 0;
  font-weight: 600;
  font-size: 1rem;
}

.card-body {
  padding: 20px;
}

.caregiver-summary-card .caregiver-avatar-large {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid #e9ecef;
}

.caregiver-name {
  font-weight: 600;
  color: #333;
  margin-bottom: 16px;
}

.info-details {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.9rem;
}

.info-item i {
  width: 16px;
  flex-shrink: 0;
}

.info-item .label {
  font-weight: 500;
  color: #666;
  white-space: nowrap;
}

.info-item .value {
  color: #333;
}

.guidelines-list,
.guarantee-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.guideline-item,
.guarantee-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  font-size: 0.9rem;
  line-height: 1.4;
}

.guideline-item i,
.guarantee-item i {
  margin-top: 2px;
  flex-shrink: 0;
}

.guideline-item span,
.guarantee-item span {
  color: #555;
}

/* 成功模態框樣式 */
.modal {
  z-index: 1055;
}

.modal-backdrop {
  z-index: 1050;
}

.modal-content {
  border: none;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
}

.modal-header.bg-success {
  background: linear-gradient(135deg, #28a745 0%, #20c997 100%) !important;
}

.btn-close-white {
  filter: invert(1) grayscale(100%) brightness(200%);
}

.booking-info {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 12px;
  margin-top: 16px;
}

/* 響應式設計 */
@media (max-width: 992px) {
  .sidebar {
    position: static;
    margin-top: 30px;
  }
  
  .page-content {
    padding: 20px 0;
  }
}

@media (max-width: 768px) {
  .page-header {
    padding: 16px 0;
  }
  
  .page-title {
    font-size: 1.8rem;
  }
  
  .page-subtitle {
    font-size: 1rem;
  }
  
  .info-card {
    margin-bottom: 16px;
  }
  
  .card-body {
    padding: 16px;
  }
  
  .caregiver-summary-card .caregiver-avatar-large {
    width: 70px;
    height: 70px;
  }
  
  .info-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
  
  .info-item .label,
  .info-item .value {
    margin-left: 24px;
  }
}

@media (max-width: 480px) {
  .page-content {
    padding: 16px 0;
  }
  
  .breadcrumb {
    font-size: 0.875rem;
  }
  
  .modal-dialog {
    margin: 10px;
  }
}

/* 無障礙設計 */
@media (prefers-reduced-motion: reduce) {
  .info-card,
  .modal-content {
    transition: none;
  }
}

/* 高對比度模式支援 */
@media (prefers-contrast: high) {
  .info-card {
    border: 2px solid #000;
  }
  
  .card-header {
    background: #000 !important;
    color: #fff;
  }
}

/* 深色模式支援 */
@media (prefers-color-scheme: dark) {
  .caregiver-booking-page {
    background: linear-gradient(135deg, #2d3748 0%, #4a5568 100%);
  }
  
  .page-header {
    background: #2d3748;
    color: white;
  }
  
  .info-card {
    background: #2d3748;
    color: white;
  }
  
  .info-item .label {
    color: #a0aec0;
  }
  
  .info-item .value {
    color: white;
  }
  
  .guideline-item span,
  .guarantee-item span {
    color: #e2e8f0;
  }
}
</style>