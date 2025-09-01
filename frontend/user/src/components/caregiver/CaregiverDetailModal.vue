<template>
  <div class="modal fade show" style="display: block;" @click.self="handleClose">
    <div class="modal-dialog modal-lg modal-dialog-centered modal-dialog-scrollable">
      <div class="modal-content">
        <!-- 彈窗標題 -->
        <div class="modal-header">
          <h5 class="modal-title">
            <i class="fas fa-user-nurse me-2"></i>
            照服員詳細資料
          </h5>
          <button type="button" class="btn-close" @click="handleClose"></button>
        </div>

        <!-- 彈窗內容 -->
        <div class="modal-body">
          <div v-if="caregiver" class="caregiver-detail">
            <!-- 基本資訊區塊 -->
            <div class="basic-info-section">
              <div class="row">
                <!-- 大頭貼 -->
                <div class="col-md-4 text-center">
                  <div class="avatar-wrapper">
                    <img 
                      :src="getCaregiverImage(caregiver.imagePath || caregiver.avatar)" 
                      :alt="caregiver.chineseName || caregiver.memberName || caregiver.name"
                      class="caregiver-avatar-large"
                      @error="handleImageError"
                    />
                    <div class="status-indicator" :class="{ active: caregiver.isActive }">
                      {{ caregiver.isActive ? '可預約' : '暫停服務' }}
                    </div>
                  </div>
                </div>

                <!-- 基本資料 -->
                <div class="col-md-8">
                  <h3 class="caregiver-name">{{ caregiver.chineseName || caregiver.memberName || caregiver.name || '未知姓名' }}</h3>
                  
                  <!-- 評分 -->
                  <div class="rating-section mb-3">
                    <div class="stars">
                      <template v-for="i in 5" :key="i">
                        <i 
                          class="fas fa-star"
                          :class="i <= Math.floor(caregiver.avgRating || caregiver.rating || 0) ? 'text-warning' : 'text-muted'"
                        ></i>
                      </template>
                    </div>
                    <span class="rating-text ms-2">
                      {{ caregiver.avgRating && caregiver.avgRating > 0 
                          ? `${caregiver.avgRating.toFixed(1)} ⭐ (${caregiver.totalRatings || 0}則評價)` 
                          : '尚無評分' }}
                    </span>
                  </div>

                  <!-- 詳細資訊 -->
                  <div class="info-grid">
                    <div class="info-item">
                      <i class="fas fa-map-marker-alt text-primary"></i>
                      <span class="label">服務區域：</span>
                      <span class="value">{{ caregiver.serviceArea || caregiver.serviceAreaDisplay || '桃園市' }}</span>
                    </div>
                    
                    <div class="info-item">
                      <i class="fas fa-medal text-success"></i>
                      <span class="label">工作經驗：</span>
                      <span class="value">{{ caregiver.experienceYears || 0 }}年</span>
                    </div>
                    
                    <div v-if="caregiver.phone || caregiver.formattedPhone" class="info-item">
                      <i class="fas fa-phone text-info"></i>
                      <span class="label">聯絡電話：</span>
                      <span class="value">{{ caregiver.formattedPhone || caregiver.phone }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 分隔線 -->
            <hr class="section-divider">

            <!-- 專業資訊 -->
            <div class="professional-info-section">
              <h5 class="section-title">
                <i class="fas fa-certificate me-2"></i>
                專業資訊
              </h5>
              
              <div class="row">
                <div class="col-md-6">
                  <div class="info-card">
                    <h6>專業專長</h6>
                    <p>{{ caregiver.specialty || caregiver.specialtiesDisplay || '一般照護' }}</p>
                  </div>
                </div>
                
                <div class="col-md-6">
                  <div class="info-card">
                    <h6>相關證照</h6>
                    <p>{{ caregiver.certificationsDisplay || '相關證照申請中' }}</p>
                  </div>
                </div>
              </div>
            </div>

            <!-- 自我介紹 -->
            <div class="introduction-section">
              <h5 class="section-title">
                <i class="fas fa-user-circle me-2"></i>
                自我介紹
              </h5>
              <div class="introduction-content">
                <p>{{ caregiver.introduction || caregiver.introductionFormatted || '專業照服員，提供優質服務，用心照護每一位長者。' }}</p>
              </div>
            </div>

            <!-- 用戶評價 -->
            <div class="reviews-section">
              <h5 class="section-title">
                <i class="fas fa-comments me-2"></i>
                用戶評價
                <span v-if="caregiver.totalRatings && caregiver.totalRatings > 0" class="badge badge-primary ms-2">
                  {{ caregiver.totalRatings }}則評價
                </span>
              </h5>
              
              <div v-if="caregiverRatings.length > 0" class="reviews-list">
                <div 
                  v-for="review in caregiverRatings" 
                  :key="review.id"
                  class="review-item"
                >
                  <div class="review-header">
                    <div class="reviewer-info">
                      <strong>{{ maskMemberName(review.memberName) }}</strong>
                      <div class="review-stars">
                        <template v-for="i in 5" :key="i">
                          <i 
                            class="fas fa-star"
                            :class="i <= review.rating ? 'text-warning' : 'text-muted'"
                          ></i>
                        </template>
                      </div>
                    </div>
                    <small class="review-date">{{ formatDate(review.createdAt) }}</small>
                  </div>
                  <p class="review-comment">{{ review.comment }}</p>
                </div>
              </div>
              
              <div v-else class="no-reviews text-center text-muted py-3">
                <i class="fas fa-comment-slash fa-2x mb-2"></i>
                <p>目前還沒有評價</p>
              </div>
            </div>
          </div>
        </div>

        <!-- 彈窗底部 -->
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" @click="handleClose">
            關閉
          </button>
          <button 
            type="button" 
            class="btn btn-primary"
            :disabled="!caregiver?.isActive"
            @click="handleBookClick"
          >
            <i class="fas fa-calendar-plus me-1"></i>
            {{ caregiver?.isActive ? '立即預約' : '暫停服務' }}
          </button>
        </div>
      </div>
    </div>
  </div>
  
  <!-- 背景遮罩 -->
  <div class="modal-backdrop fade show"></div>
</template>

<script>
import { watch, onMounted } from 'vue'
import { useCaregivers } from '@/composables/useCaregivers'

export default {
  name: 'CaregiverDetailModal',
  props: {
    caregiver: {
      type: Object,
      default: null
    }
  },
  emits: ['close', 'book'],
  setup(props, { emit }) {
    const { loadCaregiverRatings, caregiverRatings } = useCaregivers()

    /**
     * 取得照服員圖片（與列表頁面使用相同的處理方式）
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
      
      // 統一處理相對路徑（參考後台實作）
      const cleanPath = imagePath.startsWith('/') ? imagePath : '/' + imagePath
      const fullPath = `http://localhost:8080${cleanPath}`
      
      return fullPath
    }

    /**
     * 載入照服員評價
     */
    const loadReviews = async () => {
      if (props.caregiver?.id) {
        await loadCaregiverRatings(props.caregiver.id, 0, 5) // 載入前5則評價
      }
    }

    /**
     * 監聽照服員變化
     */
    watch(
      () => props.caregiver,
      (newCaregiver) => {
        if (newCaregiver) {
          loadReviews()
        }
      },
      { immediate: true }
    )

    /**
     * 組件掛載時載入評價
     */
    onMounted(() => {
      loadReviews()
    })

    /**
     * 處理關閉彈窗
     */
    const handleClose = () => {
      emit('close')
    }

    /**
     * 處理預約點擊
     */
    const handleBookClick = () => {
      if (props.caregiver?.isActive) {
        emit('book', props.caregiver)
      }
    }

    /**
     * 處理圖片載入錯誤
     */
    const handleImageError = (event) => {
      try {
        event.target.src = require('@/assets/img/thumb/error.png')
      } catch (err) {
        console.error('載入錯誤圖片失敗:', err)
        // 作為備用方案，使用一個簡單的 data URL 圖片
        event.target.src = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgdmlld0JveD0iMCAwIDIwMCAyMDAiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+CjxyZWN0IHdpZHRoPSIyMDAiIGhlaWdodD0iMjAwIiBmaWxsPSIjRjNGNEY2Ii8+CjxwYXRoIGQ9Ik0xMDAgNjBDMTE0LjMgNjAgMTI2IDcxLjcgMTI2IDg2QzEyNiAxMDAuMyAxMTQuMyAxMTIgMTAwIDExMkM4NS43IDExMiA3NCAxMDAuMyA3NCA4NkM3NCA3MS43IDg1LjcgNjAgMTAwIDYwWiIgZmlsbD0iIzlDQTNBRiIvPgo8cGF0aCBkPSJNNzQgMTQwSDEyNkMxMzAgMTQwIDEzNCAxNDAuOSAxMzYuOCAxNDMuMkMxMzkuMSAxNDYgMTQwIDE0OSAxNDAgMTUzVjE2NEg2MFYxNTNDNjAgMTQ5IDYwLjkgMTQ2IDYzLjIgMTQzLjJDNjYgMTQwLjkgNjkgMTQwIDc0IDE0MFoiIGZpbGw9IiM5Q0EzQUYiLz4KPC9zdmc+'
      }
    }

    /**
     * 遮罩會員姓名
     */
    const maskMemberName = (name) => {
      if (!name) return '匿名用戶'
      if (name.length <= 2) return name
      return name.charAt(0) + '*'.repeat(name.length - 2) + name.charAt(name.length - 1)
    }

    /**
     * 格式化日期
     */
    const formatDate = (dateString) => {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleDateString('zh-TW', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit'
      })
    }

    return {
      caregiverRatings,
      getCaregiverImage,
      handleClose,
      handleBookClick,
      handleImageError,
      maskMemberName,
      formatDate
    }
  }
}
</script>

<style scoped>
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

.modal-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-bottom: none;
  border-radius: 12px 12px 0 0;
  padding: 20px 24px;
}

.modal-title {
  font-weight: 600;
}

.btn-close {
  filter: invert(1);
  opacity: 0.8;
}

.btn-close:hover {
  opacity: 1;
}

.modal-body {
  padding: 24px;
  max-height: 70vh;
  overflow-y: auto;
}

.avatar-wrapper {
  position: relative;
  display: inline-block;
}

.caregiver-avatar-large {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  object-fit: cover;
  border: 4px solid #e9ecef;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.status-indicator {
  position: absolute;
  bottom: 0;
  right: 0;
  background: #6c757d;
  color: white;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 500;
}

.status-indicator.active {
  background: #28a745;
}

.caregiver-name {
  font-size: 1.8rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.rating-section {
  display: flex;
  align-items: center;
}

.stars {
  display: flex;
  gap: 2px;
}

.stars i {
  font-size: 1.1rem;
}

.rating-text {
  font-size: 0.9rem;
  color: #666;
}

.info-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.info-item i {
  width: 20px;
  font-size: 0.9rem;
}

.info-item .label {
  font-weight: 500;
  color: #555;
  min-width: 80px;
}

.info-item .value {
  color: #333;
}

.section-divider {
  margin: 24px 0;
  border-color: #e9ecef;
}

.section-title {
  font-weight: 600;
  color: #333;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 2px solid #e9ecef;
}

.info-card {
  background: #f8f9fa;
  padding: 16px;
  border-radius: 8px;
  height: 100%;
}

.info-card h6 {
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.info-card p {
  color: #666;
  margin-bottom: 0;
  line-height: 1.5;
}

.introduction-content {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  border-left: 4px solid #667eea;
}

.introduction-content p {
  color: #555;
  line-height: 1.6;
  margin-bottom: 0;
}

.reviews-list {
  max-height: 300px;
  overflow-y: auto;
}

.review-item {
  background: #f8f9fa;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 12px;
}

.review-header {
  display: flex;
  justify-content: between;
  align-items: flex-start;
  margin-bottom: 8px;
}

.reviewer-info {
  flex: 1;
}

.reviewer-info strong {
  color: #333;
  display: block;
  margin-bottom: 4px;
}

.review-stars {
  display: flex;
  gap: 1px;
}

.review-stars i {
  font-size: 0.8rem;
}

.review-date {
  color: #888;
}

.review-comment {
  color: #555;
  line-height: 1.5;
  margin-bottom: 0;
}

.no-reviews {
  background: #f8f9fa;
  padding: 40px 20px;
  border-radius: 8px;
}

.no-reviews i {
  opacity: 0.5;
}

.modal-footer {
  padding: 16px 24px;
  border-top: 1px solid #e9ecef;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 6px;
  padding: 8px 20px;
  font-weight: 500;
}

.btn-primary:hover:not(:disabled) {
  background: linear-gradient(135deg, #5a6fd8 0%, #6a4190 100%);
}

.btn-primary:disabled {
  background: #e9ecef;
  color: #6c757d;
}

.badge-primary {
  background-color: #667eea;
}

/* 響應式設計 */
@media (max-width: 768px) {
  .modal-dialog {
    margin: 10px;
  }
  
  .caregiver-avatar-large {
    width: 100px;
    height: 100px;
  }
  
  .caregiver-name {
    font-size: 1.5rem;
  }
  
  .info-grid {
    margin-top: 16px;
  }
}
</style>
