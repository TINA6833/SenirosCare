<template>
  <!-- 取消預約確認對話框 -->
  <div 
    v-if="isVisible" 
    class="modal-overlay"
    @click.self="closeDialog">
    
    <div class="modal-container">
      <div class="modal-content">
        
        <!-- Modal Header -->
        <div class="modal-header">
          <div class="header-content">
            <!-- 警告圖示 -->
            <div class="icon-container icon-container--warning">
              <i class="fas fa-exclamation-triangle dialog-icon"></i>
            </div>
            
            <!-- 標題 -->
            <h3 class="modal-title">確認取消預約</h3>
            
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
            <p class="appointment-detail">
              <strong>照服員：</strong>{{ appointment?.caregiverName || '未知' }}
            </p>
            <p class="appointment-detail">
              <strong>服務時間：</strong>{{ appointment?.formattedScheduledAt || '未知' }}
            </p>
            <p class="appointment-detail">
              <strong>服務類型：</strong>{{ appointment?.serviceTypeName || '未知' }}
            </p>
          </div>
          
          <!-- 警告訊息 -->
          <div class="warning-message">
            <i class="fas fa-exclamation-circle text-warning me-2"></i>
            <span>取消後將無法恢復，請確認您的決定。</span>
          </div>
          
          <!-- 取消原因輸入 -->
          <div class="reason-input-section">
            <label for="cancelReason" class="form-label">
              取消原因 <span class="text-muted">(選填)</span>
            </label>
            <textarea
              id="cancelReason"
              v-model="cancelReason"
              class="form-control"
              rows="3"
              placeholder="請說明取消預約的原因，這將有助於我們改善服務品質..."
              maxlength="500"
            ></textarea>
            <div class="char-count">{{ cancelReason.length }}/500</div>
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
              <span>保留預約</span>
            </button>
            
            <!-- 確認取消按鈕 -->
            <button 
              type="button" 
              class="confirm-btn"
              :disabled="isSubmitting"
              @click="confirmCancel">
              <i class="fas fa-exclamation-triangle me-2" v-if="!isSubmitting"></i>
              <i class="fas fa-spinner fa-spin me-2" v-if="isSubmitting"></i>
              <span>{{ isSubmitting ? '取消中...' : '確認取消' }}</span>
            </button>
          </div>
        </div>
        
      </div>
    </div>
  </div>
</template>

<script>
import { ref, watch } from 'vue'

export default {
  name: 'CancelAppointmentDialog',
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
  emits: ['confirm', 'cancel', 'close'],
  setup(props, { emit }) {
    // ========== 響應式資料 ==========
    const isVisible = ref(props.visible)
    const isSubmitting = ref(props.submitting)
    const cancelReason = ref('')

    // ========== 監聽器 ==========
    
    /**
     * 監聽 visible prop 變化
     */
    watch(() => props.visible, (newVal) => {
      isVisible.value = newVal
      // 對話框開啟時重置原因輸入
      if (newVal) {
        cancelReason.value = ''
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
     * 關閉對話框
     */
    const closeDialog = () => {
      if (!isSubmitting.value) {
        isVisible.value = false
        cancelReason.value = ''
        emit('close')
        emit('cancel')
      }
    }

    /**
     * 確認取消預約
     */
    const confirmCancel = () => {
      if (!isSubmitting.value) {
        emit('confirm', cancelReason.value.trim())
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
      cancelReason,
      
      // 方法
      closeDialog,
      confirmCancel
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
  max-width: 500px;
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
}

/* Modal Header */
.modal-header {
  padding: 24px;
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

.icon-container--warning {
  background-color: #fef3c7;
}

.dialog-icon {
  font-size: 24px;
  color: #d97706;
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
  padding: 0 24px 24px 24px;
}

/* 預約資訊 */
.appointment-info {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
}

.appointment-detail {
  margin: 0 0 8px 0;
  font-size: 14px;
  color: #4b5563;
  line-height: 1.5;
}

.appointment-detail:last-child {
  margin-bottom: 0;
}

.appointment-detail strong {
  color: #1f2937;
  font-weight: 600;
}

/* 警告訊息 */
.warning-message {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  background: #fef3c7;
  border: 1px solid #fed7aa;
  border-radius: 8px;
  margin-bottom: 16px;
  font-size: 14px;
  color: #92400e;
}

.text-warning {
  color: #d97706;
}

.me-2 {
  margin-right: 0.5rem;
}

/* 取消原因輸入區域 */
.reason-input-section {
  margin-bottom: 8px;
}

.form-label {
  display: block;
  font-weight: 500;
  color: #374151;
  margin-bottom: 8px;
  font-size: 14px;
}

.text-muted {
  color: #6b7280;
  font-weight: 400;
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
  min-height: 80px;
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

/* Modal Footer */
.modal-footer {
  padding: 0 24px 24px 24px;
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
  padding: 10px 20px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  text-decoration: none !important;
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
  background-color: #dc2626;
  border: 1px solid #dc2626;
}

.confirm-btn:hover:not(:disabled) {
  background-color: #b91c1c;
  border-color: #b91c1c;
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
    padding-bottom: 20px;
  }
  
  .modal-footer {
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
