<template>
  <div class="caregiver-detail-page">
    <!-- 頁面標題 -->
    <div class="page-header">
      <div class="container">
        <nav class="breadcrumb">
          <ol class="breadcrumb">
            <li class="breadcrumb-item">
              <router-link to="/">首頁</router-link>
            </li>
            <li class="breadcrumb-item">
              <router-link to="/caregiver">照服員</router-link>
            </li>
            <li class="breadcrumb-item active">照服員詳情</li>
          </ol>
        </nav>
        <h1 class="page-title">照服員詳情</h1>
      </div>
    </div>

    <div class="container">
      <!-- 載入狀態 -->
      <div v-if="loading" class="text-center py-5">
        <div class="spinner-border text-primary" role="status">
          <span class="sr-only">載入中...</span>
        </div>
        <p class="mt-3">載入照服員資料中...</p>
      </div>

      <!-- 錯誤狀態 -->
      <div v-else-if="error" class="alert alert-danger">
        <i class="fas fa-exclamation-triangle me-2"></i>
        {{ error }}
      </div>

      <!-- 照服員詳細資料 -->
      <div v-else-if="caregiver" class="row">
        <!-- 左側：照服員基本資訊 -->
        <div class="col-lg-8">
          <div class="caregiver-profile-card">
            <div class="card">
              <div class="card-body">
                <div class="row">
                  <div class="col-md-4 text-center">
                    <img 
                      :src="getCaregiverImage(caregiver.imagePath)" 
                      :alt="caregiver.chineseName"
                      class="caregiver-avatar mb-3"
                      @error="handleImageError"
                    />
                  </div>
                  <div class="col-md-8">
                    <h2 class="caregiver-name">{{ caregiver.chineseName }}</h2>
                    
                    <!-- 評分資訊 -->
                    <div class="rating-section mb-3">
                      <div class="stars">
                        <i 
                          v-for="star in 5" 
                          :key="star"
                          class="fas fa-star"
                          :class="star <= Math.floor(caregiver.avgRating) ? 'text-warning' : 'text-muted'"
                        ></i>
                      </div>
                      <span class="rating-text">
                        {{ caregiver.avgRating }}分 ({{ caregiver.totalRatings }}則評價)
                      </span>
                    </div>

                    <!-- 基本資訊 -->
                    <div class="info-grid">
                      <div class="info-item">
                        <i class="fas fa-map-marker-alt text-primary"></i>
                        <span><strong>服務區域：</strong>{{ caregiver.serviceArea }}</span>
                      </div>
                      <div class="info-item">
                        <i class="fas fa-calendar-alt text-success"></i>
                        <span><strong>經驗年資：</strong>{{ caregiver.experienceYears }}年</span>
                      </div>
                      <div class="info-item">
                        <i class="fas fa-dollar-sign text-warning"></i>
                        <span><strong>服務價格：</strong>NT$ {{ caregiver.hourlyRate }}/小時</span>
                      </div>
                      <div class="info-item">
                        <i class="fas fa-check-circle text-success"></i>
                        <span><strong>服務狀態：</strong>
                          <span :class="caregiver.isActive ? 'text-success' : 'text-secondary'">
                            {{ caregiver.isActive ? '可預約' : '暫停服務' }}
                          </span>
                        </span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 自我介紹 -->
          <div class="introduction-card mt-4">
            <div class="card">
              <div class="card-header">
                <h5 class="card-title mb-0">
                  <i class="fas fa-user me-2"></i>
                  自我介紹
                </h5>
              </div>
              <div class="card-body">
                <p class="intro-text">
                  {{ caregiver.introduction || '專業照服員，提供優質的照護服務，致力於維護長者的生活品質與尊嚴。' }}
                </p>
              </div>
            </div>
          </div>
        </div>

        <!-- 右側：預約卡片 -->
        <div class="col-lg-4">
          <div class="booking-card sticky-top">
            <div class="card">
              <div class="card-header text-center">
                <h5 class="card-title mb-0 text-white">
                  <i class="fas fa-calendar-plus me-2"></i>
                  預約服務
                </h5>
              </div>
              <div class="card-body text-center">
                <div class="price-display mb-3">
                  <span class="price-label">服務費用</span>
                  <div class="price-amount">
                    NT$ {{ caregiver.hourlyRate }}
                    <small>/小時</small>
                  </div>
                </div>

                <div class="service-status mb-3">
                  <span 
                    class="badge"
                    :class="caregiver.isActive ? 'badge-success' : 'badge-secondary'"
                  >
                    {{ caregiver.isActive ? '可立即預約' : '暫停服務中' }}
                  </span>
                </div>

                <button 
                  class="btn btn-primary btn-lg w-100 mb-3"
                  :disabled="!caregiver.isActive"
                  @click="goToBooking"
                >
                  <i class="fas fa-calendar-plus me-2"></i>
                  {{ caregiver.isActive ? '立即預約' : '暫停服務' }}
                </button>

                <button 
                  class="btn btn-outline-primary w-100"
                  @click="viewReviews"
                >
                  <i class="fas fa-comments me-2"></i>
                  查看評價
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useCaregivers } from '@/composables/useCaregivers'
import { useAuth } from '@/composables/useAuth'
import { useToast } from '@/composables/useToast'

export default {
  name: 'CaregiverDetail',
  props: {
    caregiverId: { // 修改 prop 名稱為 caregiverId
      type: [String, Number],
      required: true
    }
  },
  setup(props) {
    // 路由相關
    const route = useRoute()
    const router = useRouter()
    const caregiverId = computed(() => parseInt(props.caregiverId || route.params.caregiverId)) // 修改參數名稱

    // Composables
    const { getCaregiverById } = useCaregivers()
    const { isAuthenticated } = useAuth()
    const { showWarning } = useToast()

    // 響應式資料
    const caregiver = ref(null)
    const loading = ref(false)
    const error = ref(null)

    /**
     * 載入照服員資料
     */
    const loadCaregiverData = async () => {
      try {
        loading.value = true
        error.value = null

        const caregiverData = await getCaregiverById(caregiverId.value)

        if (caregiverData) {
          caregiver.value = caregiverData
        } else {
          error.value = '找不到指定的照服員'
        }
      } catch (err) {
        console.error('載入照服員資料失敗:', err)
        error.value = err.message || '載入照服員資料失敗'
      } finally {
        loading.value = false
      }
    }

    /**
     * 前往預約頁面
     */
    const goToBooking = () => {
      if (!caregiver.value.isActive) {
        showWarning('此照服員目前暫停服務')
        return
      }

      if (!isAuthenticated.value) {
        showWarning('請先登入才能預約服務')
        router.push('/login')
        return
      }

      router.push({
        name: 'CaregiverBooking',
        params: { caregiverId: caregiverId.value } // 修改參數名稱
      })
    }

    /**
     * 查看評價
     */
    const viewReviews = () => {
      router.push({
        name: 'CaregiverReviews',
        params: { caregiverId: caregiverId.value } // 修改參數名稱
      })
    }

    /**
     * 處理圖片載入錯誤
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

    // 初始化
    onMounted(() => {
      if (!caregiverId.value || caregiverId.value <= 0) {
        error.value = '無效的照服員ID'
        return
      }
      loadCaregiverData()
    })

    return {
      // 資料
      caregiver,
      loading,
      error,
      
      // 方法
      goToBooking,
      viewReviews,
      getCaregiverImage,
      handleImageError
    }
  }
}
</script>

<style scoped>
.caregiver-detail-page {
  min-height: 100vh;
  background-color: #f8f9fa;
}

.page-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 40px 0;
  margin-bottom: 40px;
}

.breadcrumb {
  background: none;
  padding: 0;
  margin-bottom: 16px;
}

.breadcrumb-item a {
  color: rgba(255, 255, 255, 0.8);
  text-decoration: none;
}

.breadcrumb-item a:hover {
  color: white;
}

.breadcrumb-item.active {
  color: rgba(255, 255, 255, 0.9);
}

.page-title {
  font-size: 2rem;
  font-weight: 600;
  margin-bottom: 0;
}

.caregiver-avatar {
  width: 150px;
  height: 150px;
  border-radius: 50%;
  object-fit: cover;
  border: 4px solid #e9ecef;
}

.caregiver-name {
  font-size: 2rem;
  font-weight: 700;
  color: #333;
  margin-bottom: 16px;
}

.rating-section {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.stars {
  display: flex;
  gap: 4px;
}

.stars i {
  font-size: 1.2rem;
}

.rating-text {
  font-size: 1rem;
  color: #666;
  font-weight: 500;
}

.info-grid {
  display: grid;
  gap: 12px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 1rem;
}

.info-item i {
  width: 20px;
  font-size: 1.1rem;
}

.introduction-card .card-header {
  background: #f8f9fa;
  border-bottom: 1px solid #e9ecef;
}

.intro-text {
  font-size: 1rem;
  line-height: 1.6;
  color: #555;
  margin: 0;
}

.booking-card {
  top: 100px;
}

.booking-card .card-header {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-bottom: none;
}

.price-display {
  text-align: center;
}

.price-label {
  display: block;
  font-size: 0.9rem;
  color: #666;
  margin-bottom: 8px;
}

.price-amount {
  font-size: 2rem;
  font-weight: 700;
  color: #667eea;
}

.price-amount small {
  font-size: 1rem;
  color: #999;
}

.service-status {
  margin: 16px 0;
}

.badge {
  padding: 8px 16px;
  font-size: 0.9rem;
  border-radius: 20px;
}

.badge-success {
  background-color: #28a745;
}

.badge-secondary {
  background-color: #6c757d;
}

.btn-lg {
  padding: 12px 24px;
  font-size: 1.1rem;
  font-weight: 600;
}

/* 響應式設計 */
@media (max-width: 768px) {
  .page-header {
    padding: 30px 0;
  }
  
  .page-title {
    font-size: 1.5rem;
  }
  
  .booking-card {
    position: static !important;
    margin-top: 30px;
  }
  
  .caregiver-avatar {
    width: 120px;
    height: 120px;
  }
  
  .caregiver-name {
    font-size: 1.5rem;
    text-align: center;
  }
  
  .info-grid {
    margin-top: 20px;
  }
  
  .rating-section {
    justify-content: center;
  }
}
</style>