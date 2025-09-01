<template>
  <div class="caregiver-card">
    <div class="card h-100" @click="handleCardClick">
      <div class="card-body">
        <!-- 照服員頭像 -->
        <div class="caregiver-avatar-section text-center mb-3">
          <img 
            :src="getCaregiverImage(caregiver.imagePath)" 
            :alt="caregiver.chineseName"
            class="caregiver-avatar"
            @error="handleImageError"
          />
        </div>

        <!-- 照服員基本資訊 -->
        <div class="caregiver-info text-center mb-3">
          <h6 class="caregiver-name">{{ caregiver.chineseName }}</h6>
          
          <!-- 評分資訊 -->
          <div class="rating-info mb-2">
            <div class="stars mb-1">
              <i 
                v-for="star in 5" 
                :key="star"
                class="fas fa-star"
                :class="star <= Math.floor(caregiver.avgRating) ? 'text-warning' : 'text-muted'"
              ></i>
            </div>
            <small class="rating-text text-muted">
              {{ caregiver.avgRating }}分 ({{ caregiver.totalRatings }}則評價)
            </small>
          </div>

          <!-- 服務區域 -->
          <div class="service-area mb-2">
            <i class="fas fa-map-marker-alt text-primary me-1"></i>
            <span class="text-muted">{{ caregiver.serviceArea }}</span>
          </div>

          <!-- 經驗年資 -->
          <div class="experience mb-2">
            <i class="fas fa-calendar-alt text-success me-1"></i>
            <span class="text-muted">{{ caregiver.experienceYears }}年經驗</span>
          </div>

          <!-- 服務價格 -->
          <div class="hourly-rate mb-3">
            <span class="price-tag">
              <i class="fas fa-dollar-sign me-1"></i>
              NT$ {{ caregiver.hourlyRate }}/小時
            </span>
          </div>
        </div>

        <!-- 自我介紹 -->
        <div class="introduction mb-3">
          <p class="intro-text">
            {{ truncateText(caregiver.introduction || '專業照服員，提供優質服務', 60) }}
          </p>
        </div>

        <!-- 服務狀態標籤 -->
        <div class="status-section mb-3">
          <div class="d-flex justify-content-center">
            <span 
              class="badge"
              :class="caregiver.isActive ? 'badge-success' : 'badge-secondary'"
            >
              {{ caregiver.isActive ? '可預約' : '暫停服務' }}
            </span>
          </div>
        </div>

        <!-- 預約按鈕 -->
        <div class="booking-button">
          <button 
            class="btn btn-primary w-100"
            :disabled="!caregiver.isActive"
            @click.stop="handleBookClick"
          >
            <i class="fas fa-calendar-plus me-2"></i>
            {{ caregiver.isActive ? '立即預約' : '暫停服務' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { useRouter } from 'vue-router'
import { useAuth } from '@/composables/useAuth'
import { useToast } from '@/composables/useToast'

export default {
  name: 'CaregiverCard',
  props: {
    /**
     * 照服員資料物件
     */
    caregiver: {
      type: Object,
      required: true,
      validator(value) {
        // 基本必要欄位驗證
        return value && 
               (value.caregiverId || value.id) && 
               (value.chineseName || value.memberName || value.name)
      }
    }
  },
  emits: ['click', 'book'],
  setup(props, { emit }) {
    // 注入依賴
    const router = useRouter()
    const { isAuthenticated } = useAuth()
    const { showWarning } = useToast()

    /**
     * 處理卡片點擊事件
     * 點擊卡片時跳轉到照服員詳細頁面
     */
    const handleCardClick = () => {
      // 發出點擊事件
      emit('click', props.caregiver)
      
      // 跳轉到照服員詳細頁面
      const caregiverId = props.caregiver.caregiverId || props.caregiver.id
      if (caregiverId) {
        router.push({
          name: 'CaregiverDetail',
          params: { id: caregiverId }
        })
      }
    }

   /**
     * 處理預約按鈕點擊事件
     * 檢查登入狀態並跳轉到預約頁面
     */
    const handleBookClick = () => {
      // 檢查照服員是否可預約
      if (!props.caregiver.isActive) {
        showWarning('此照服員目前暫停服務')
        return
      }

      // 檢查登入狀態
      if (!isAuthenticated.value) {
        showWarning('請先登入才能預約服務')
        router.push('/login')
        return
      }

      // 發出預約事件
      emit('book', props.caregiver)

      // 跳轉到預約頁面 - 修改為符合您的路由結構
      const caregiverId = props.caregiver.caregiverId || props.caregiver.caregiverMemberId || props.caregiver.id
      if (caregiverId) {
        router.push({
          name: 'CaregiverBooking',
          params: { caregiverId: caregiverId.toString() } // 使用 caregiverId 作為參數名
        })
      }
    }

    /**
     * 處理圖片載入錯誤
     * 設置預設頭像
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
     * @param {string} text - 原始文字
     * @param {number} maxLength - 最大長度
     * @returns {string} 截斷後的文字
     */
    const truncateText = (text, maxLength) => {
      if (!text) return ''
      if (text.length <= maxLength) return text
      return text.substring(0, maxLength) + '...'
    }

    return {
      // 方法
      handleCardClick,
      handleBookClick,
      getCaregiverImage,
      handleImageError,
      truncateText
    }
  }
}
</script>

<style scoped>
.caregiver-card {
  cursor: pointer;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.caregiver-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.caregiver-card .card {
  border: none;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.caregiver-card:hover .card {
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.15);
}

.caregiver-avatar-section {
  position: relative;
}

.caregiver-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid #e9ecef;
  transition: border-color 0.3s ease;
}

.caregiver-card:hover .caregiver-avatar {
  border-color: #667eea;
}

.caregiver-info {
  margin-bottom: 16px;
}

.caregiver-name {
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
  font-size: 1.1rem;
}

.rating-info {
  margin-bottom: 12px;
}

.stars {
  display: flex;
  justify-content: center;
  gap: 2px;
  margin-bottom: 4px;
}

.stars i {
  font-size: 0.85rem;
}

.rating-text {
  font-size: 0.8rem;
}

.service-area,
.experience {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.85rem;
  color: #666;
  margin-bottom: 8px;
}

.service-area i,
.experience i {
  font-size: 0.8rem;
}

.hourly-rate {
  text-align: center;
}

.price-tag {
  display: inline-block;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 0.9rem;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
}

.introduction {
  padding: 0 10px;
}

.intro-text {
  font-size: 0.85rem;
  color: #666;
  line-height: 1.4;
  margin: 0;
  text-align: center;
}

.status-section {
  margin-bottom: 16px;
}

.badge {
  padding: 6px 12px;
  font-size: 0.8rem;
  font-weight: 500;
  border-radius: 20px;
}

.badge-success {
  background-color: #28a745;
  color: white;
}

.badge-secondary {
  background-color: #6c757d;
  color: white;
}

.booking-button {
  padding: 0 10px;
}

.booking-button .btn {
  font-weight: 600;
  border-radius: 8px;
  padding: 10px 16px;
  transition: all 0.3s ease;
  border: none;
}

.booking-button .btn-primary {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

.booking-button .btn-primary:hover:not(:disabled) {
  background: linear-gradient(135deg, #5a6fd8, #6a42a0);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.6);
}

.booking-button .btn-primary:disabled {
  background: #6c757d;
  color: #fff;
  opacity: 0.6;
  cursor: not-allowed;
  box-shadow: none;
}

.booking-button .btn-primary:disabled:hover {
  transform: none;
}

/* 響應式設計 */
@media (max-width: 576px) {
  .caregiver-avatar {
    width: 70px;
    height: 70px;
  }
  
  .caregiver-name {
    font-size: 1rem;
  }
  
  .price-tag {
    font-size: 0.8rem;
    padding: 5px 10px;
  }
  
  .intro-text {
    font-size: 0.8rem;
  }
  
  .service-area,
  .experience {
    font-size: 0.8rem;
  }
  
  .booking-button .btn {
    padding: 8px 12px;
    font-size: 0.9rem;
  }
}

/* 動畫效果 */
@keyframes pulse {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
  100% {
    transform: scale(1);
  }
}

.caregiver-card:hover .caregiver-avatar {
  animation: pulse 1s ease-in-out;
}

/* 無障礙設計 */
@media (prefers-reduced-motion: reduce) {
  .caregiver-card {
    transition: none;
  }
  
  .caregiver-card:hover {
    transform: none;
  }
  
  .caregiver-card:hover .caregiver-avatar {
    animation: none;
  }
}

/* 高對比度模式支援 */
@media (prefers-contrast: high) {
  .caregiver-card .card {
    border: 2px solid #000;
  }
  
  .price-tag {
    background: #000;
    color: #fff;
  }
}
</style>