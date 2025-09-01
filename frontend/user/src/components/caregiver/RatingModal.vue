<template>
  <!-- 服務評價對話框 -->
  <div 
    v-if="isVisible" 
    class="modal-overlay"
    @click.self="closeDialog">
    
    <div class="modal-container">
      <div class="modal-content">
        
        <!-- Modal Header -->
        <div class="modal-header">
          <div class="header-content">
            <!-- 評價圖示 -->
            <div class="icon-container icon-container--rating">
              <i class="fas fa-star dialog-icon"></i>
            </div>
            
            <!-- 標題 -->
            <h3 class="modal-title">服務評價</h3>
            
            <!-- 關閉按鈕 -->
            <button 
              type="button" 
              class="close-button"
              aria-label="關閉"
              @click="closeDialog">
              <i class="fas fa-times"></i>
            </button>
          </div>
        </div>
        
        <!-- Modal Body -->
        <div class="modal-body">
          <!-- 預約資訊 -->
          <div class="appointment-info">
            <div class="appointment-header">
              <img 
                :src="getCaregiverImage(appointment?.caregiverAvatar)" 
                :alt="appointment?.caregiverName"
                class="caregiver-avatar"
                @error="handleImageError"
              />
              <div class="appointment-details">
                <h6 class="caregiver-name">{{ appointment?.caregiverName || '未知照服員' }}</h6>
                <p class="appointment-detail">
                  <i class="fas fa-calendar-alt text-primary me-2"></i>
                  <span>{{ appointment?.formattedScheduledAt || '未知時間' }}</span>
                </p>
                <p class="appointment-detail">
                  <i class="fas fa-hand-holding-heart text-success me-2"></i>
                  <span>{{ appointment?.serviceTypeName || '未知服務' }}</span>
                </p>
              </div>
            </div>
          </div>

          <!-- 評分區域 -->
          <div class="rating-section">
            <label class="form-label">
              服務評分 <span class="required">*</span>
            </label>
            <div class="star-rating">
              <button
                v-for="star in 5"
                :key="star"
                type="button"
                class="star-btn"
                :class="{ 
                  active: star <= ratingForm.rating, 
                  hover: star <= hoverRating && hoverRating > 0 
                }"
                @click="setRating(star)"
                @mouseenter="hoverRating = star"
                @mouseleave="hoverRating = 0"
              >
                <i class="fas fa-star"></i>
              </button>
            </div>
            <div class="rating-text">
              <span v-if="ratingForm.rating" class="rating-description">
                {{ getRatingText(ratingForm.rating) }}
              </span>
              <span v-else class="text-muted">請點擊星星進行評分</span>
            </div>
          </div>

          <!-- 評價內容 -->
          <div class="comment-section">
            <label class="form-label">
              評價內容 <span class="text-muted">(選填)</span>
            </label>
            <textarea
              v-model="ratingForm.comment"
              class="form-control"
              rows="3"
              placeholder="請分享您對這次服務的感受和建議..."
              maxlength="500"
            ></textarea>
            <div class="char-count">{{ ratingForm.comment.length }}/500</div>
          </div>

          <!-- 服務特點標籤 -->
          <div class="tags-section">
            <label class="form-label">服務特點 <span class="text-muted">(可選)</span></label>
            <div class="tag-options">
              <button
                v-for="tag in predefinedTags"
                :key="tag"
                type="button"
                class="tag-btn"
                :class="{ selected: ratingForm.tags.includes(tag) }"
                @click="toggleTag(tag)"
              >
                {{ tag }}
              </button>
            </div>
          </div>


        </div>
        
        <!-- Modal Footer -->
        <div class="modal-footer">
          <div class="button-group">
            <!-- 取消按鈕 -->
            <button 
              type="button" 
              class="cancel-btn"
              @click="closeDialog">
              <i class="fas fa-times me-2"></i>
              <span>取消</span>
            </button>
            
            <!-- 提交評價按鈕 -->
            <button 
              type="button" 
              class="confirm-btn"
              :disabled="!canSubmit || isSubmitting"
              @click="handleSubmit">
              <i class="fas fa-spinner fa-spin me-2" v-if="isSubmitting"></i>
              <i class="fas fa-paper-plane me-2" v-else></i>
              <span>{{ isSubmitting ? '提交中...' : '提交評價' }}</span>
            </button>
          </div>
        </div>
        
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, computed, watch } from 'vue'
import { useToast } from '@/composables/useToast'

export default {
  name: 'RatingModal',
  props: {
    /** 是否顯示對話框 */
    visible: {
      type: Boolean,
      default: false
    },
    /** 預約資料 */
    appointment: {
      type: Object,
      default: () => ({})
    },
    /** 是否正在提交 */
    submitting: {
      type: Boolean,
      default: false
    }
  },
  emits: ['submit', 'close'],
  setup(props, { emit }) {
    // ========== 響應式資料 ==========
    const isVisible = ref(props.visible)
    const isSubmitting = ref(props.submitting)
    const hoverRating = ref(0)

    // 重點註解：引入 Toast 系統
    const { showToast } = useToast()

    // 評價表單資料
    const ratingForm = reactive({
      rating: 0,
      comment: '',
      tags: []
    })

    // 預定義標籤
    const predefinedTags = [
      '專業細心',
      '準時到達',
      '態度親切',
      '技術熟練',
      '耐心負責',
      '溝通良好',
      '服務貼心',
      '值得推薦'
    ]

    // ========== 計算屬性 ==========
    
    /**
     * 是否可以提交
     */
    const canSubmit = computed(() => {
      return ratingForm.rating > 0
    })

    // ========== 監聽器 ==========
    
    /**
     * 監聽 visible prop 變化
     */
    watch(() => props.visible, (newVal) => {
      isVisible.value = newVal
      // 對話框開啟時重置表單
      if (newVal) {
        resetForm()
      }
    })

    /**
     * 監聽 submitting prop 變化
     */
    watch(() => props.submitting, (newVal) => {
      isSubmitting.value = newVal
    })

    // ========== 方法 ==========
    
    /**
     * 重置表單
     */
    const resetForm = () => {
      ratingForm.rating = 0
      ratingForm.comment = ''
      ratingForm.tags = []
      hoverRating.value = 0
    }

    /**
     * 設定評分
     */
    const setRating = (rating) => {
      ratingForm.rating = rating
    }

    /**
     * 切換標籤選擇
     */
    const toggleTag = (tag) => {
      const index = ratingForm.tags.indexOf(tag)
      if (index > -1) {
        ratingForm.tags.splice(index, 1)
      } else {
        ratingForm.tags.push(tag)
      }
    }

    /**
     * 取得評分文字
     */
    const getRatingText = (rating) => {
      const texts = {
        1: '很不滿意',
        2: '不滿意',
        3: '普通',
        4: '滿意',
        5: '非常滿意'
      }
      return texts[rating] || ''
    }

    /**
     * 關閉對話框
     */
    const closeDialog = () => {
      if (!isSubmitting.value) {
        isVisible.value = false
        resetForm()
        emit('close')
      }
    }

    /**
     * 處理表單提交
     */
    const handleSubmit = () => {
      if (!canSubmit.value || isSubmitting.value) return

      // 準備提交資料
      const submitData = {
        ratingScore: ratingForm.rating,
        ratingComment: ratingForm.comment.trim(),
        tags: ratingForm.tags
      }

      // 重點註解：使用統一的 Toast 系統顯示評論完成通知（參考「歡迎回來」通知）
      showToast('評論已完成！', 'warning', '評價成功')

      emit('submit', submitData)
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

    /**
     * 處理 ESC 鍵
     */
    const handleKeydown = (event) => {
      if (event.key === 'Escape' && isVisible.value && !isSubmitting.value) {
        closeDialog()
      }
    }

    // 綁定鍵盤事件
    if (typeof window !== 'undefined') {
      document.addEventListener('keydown', handleKeydown)
    }

    return {
      // 響應式資料
      isVisible,
      isSubmitting,
      hoverRating,
      ratingForm,
      predefinedTags,
      
      // 計算屬性
      canSubmit,
      
      // 方法
      setRating,
      toggleTag,
      getRatingText,
      closeDialog,
      handleSubmit,
      getCaregiverImage,
      handleImageError
    }
  }
}
</script>

<style scoped>
/* 基本重置 */
* {
  text-decoration: none !important;
  box-sizing: border-box;
}

/* Modal 覆蓋層 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1060;
  backdrop-filter: blur(4px);
  animation: fadeIn 0.2s ease-out;
}

/* Modal 容器 */
.modal-container {
  width: 100%;
  max-width: 700px;
  margin: 20px;
  animation: slideUp 0.3s ease-out;
}

/* Modal 內容 */
.modal-content {
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.2);
  overflow: hidden;
  font-family: inherit;
  max-height: 85vh;
  display: flex;
  flex-direction: column;
}

/* Modal Header */
.modal-header {
  padding: 24px;
  flex-shrink: 0;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

/* 圖示容器 */
.icon-container {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.icon-container--rating {
  background-color: #fff3cd;
}

.dialog-icon {
  font-size: 24px;
  color: #856404;
}

/* Modal 標題 */
.modal-title {
  flex: 1;
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
  line-height: 1.3;
}

/* 關閉按鈕 */
.close-button {
  width: 32px;
  height: 32px;
  border: none;
  background: #f3f4f6;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #6b7280;
  transition: all 0.2s ease;
  flex-shrink: 0;
}

.close-button:hover {
  background: #e5e7eb;
  color: #374151;
  transform: scale(1.1);
}

/* Modal Body */
.modal-body {
  padding: 0 24px 0 24px;
  flex: 1;
  overflow-y: auto;
}

/* 預約資訊 */
.appointment-info {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
}

.appointment-header {
  display: flex;
  align-items: center;
  gap: 16px;
}

.caregiver-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #e9ecef;
  flex-shrink: 0;
}

.appointment-details {
  flex: 1;
}

.caregiver-name {
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 8px 0;
  font-size: 16px;
}

.appointment-detail {
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #4b5563;
  margin: 0 0 4px 0;
  line-height: 1.4;
}

.appointment-detail:last-child {
  margin-bottom: 0;
}

.appointment-detail i {
  width: 16px;
  flex-shrink: 0;
}

.text-primary {
  color: #3b82f6;
}

.text-success {
  color: #10b981;
}

.me-2 {
  margin-right: 0.5rem;
}

/* 評分區域 */
.rating-section {
  margin-bottom: 16px;
}

.form-label {
  display: block;
  font-weight: 500;
  color: #374151;
  margin-bottom: 8px;
  font-size: 14px;
}

.required {
  color: #dc2626;
}

.text-muted {
  color: #6b7280;
  font-weight: 400;
}

.star-rating {
  display: flex;
  gap: 4px;
  margin-bottom: 8px;
}

.star-btn {
  background: none;
  border: none;
  padding: 4px;
  cursor: pointer;
  transition: all 0.2s ease;
  color: #d1d5db;
  font-size: 1.5rem;
}

.star-btn:hover,
.star-btn.hover {
  color: #fbbf24;
  transform: scale(1.1);
}

.star-btn.active {
  color: #fbbf24;
}

.rating-text {
  margin-bottom: 4px;
}

.rating-description {
  font-size: 14px;
  font-weight: 500;
  color: #374151;
}

/* 評價內容區域 */
.comment-section {
  margin-bottom: 16px;
}

.form-control {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
  line-height: 1.5;
  color: #374151;
  background-color: #ffffff;
  transition: border-color 0.15s ease-in-out, box-shadow 0.15s ease-in-out;
  resize: vertical;
  min-height: 60px;
}

.form-control:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.form-control::placeholder {
  color: #9ca3af;
}

/* 字數統計 */
.char-count {
  text-align: right;
  font-size: 12px;
  color: #6b7280;
  margin-top: 4px;
}

/* 標籤區域 */
.tags-section {
  margin-bottom: 16px;
}

.tag-options {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-btn {
  background: #f3f4f6;
  border: 1px solid #d1d5db;
  border-radius: 20px;
  padding: 6px 12px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
  color: #374151;
}

.tag-btn:hover {
  background: #e5e7eb;
  border-color: #9ca3af;
}

.tag-btn.selected {
  background: #3b82f6;
  color: white;
  border-color: #3b82f6;
}

/* Modal Footer */
.modal-footer {
  padding: 24px;
  background-color: #f8f9fa;
  border-top: 1px solid #e9ecef;
  flex-shrink: 0;
}

.button-group {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

/* 按鈕樣式 */
.cancel-btn,
.confirm-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  text-decoration: none !important;
  min-width: 120px;
  justify-content: center;
}

.cancel-btn {
  color: #6b7280;
  background-color: #f3f4f6;
  border: 1px solid #d1d5db;
}

.cancel-btn:hover {
  background-color: #e5e7eb;
  color: #374151;
}

.confirm-btn {
  color: #ffffff;
  background-color: #3b82f6;
  border: 1px solid #3b82f6;
}

.confirm-btn:hover:not(:disabled) {
  background-color: #2563eb;
  border-color: #2563eb;
}

.confirm-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 動畫效果 */
@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* 響應式設計 */
@media (max-width: 640px) {
  .modal-container {
    margin: 16px;
    max-width: none;
  }
  
  .modal-header,
  .modal-body,
  .modal-footer {
    padding-left: 20px;
    padding-right: 20px;
  }
  
  .modal-header {
    padding-top: 20px;
    padding-bottom: 16px;
  }
  
  .modal-body {
    padding-top: 0;
    padding-bottom: 0;
  }
  
  .modal-footer {
    padding-top: 20px;
    padding-bottom: 20px;
  }
  
  .header-content {
    gap: 12px;
  }
  
  .icon-container {
    width: 40px;
    height: 40px;
  }
  
  .dialog-icon {
    font-size: 20px;
  }
  
  .modal-title {
    font-size: 16px;
  }
  
  .appointment-header {
    gap: 12px;
  }
  
  .caregiver-avatar {
    width: 50px;
    height: 50px;
  }
  
  .star-rating {
    justify-content: center;
  }
  
  .star-btn {
    font-size: 1.3rem;
  }
  
  .tag-options {
    justify-content: center;
  }
  
  .button-group {
    flex-direction: column-reverse;
    gap: 8px;
  }
  
  .button-group button {
    width: 100%;
    justify-content: center;
  }
}

/* 旋轉動畫 */
.fa-spin {
  animation: fa-spin 1s infinite linear;
}

@keyframes fa-spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}
</style>
