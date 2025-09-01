<template>
  <!-- [重點] 確認對話框 Modal - 使用原生 CSS 實作 -->
  <div 
    v-if="isVisible" 
    class="modal-overlay"
    @click.self="closeDialog">
    
    <div class="modal-container">
      <div class="modal-content">
        
        <!-- [重點] Modal Header -->
        <div class="modal-header">
          <div class="header-content">
            <!-- [重點] 圖示區域 -->
            <div 
              class="icon-container"
              :class="iconContainerClass">
              <i 
                :class="getIconClass(dialogData.type)" 
                class="dialog-icon">
              </i>
            </div>
            
            <!-- [重點] 標題區域 -->
            <h3 class="modal-title">
              {{ dialogData.title }}
            </h3>
            
            <!-- [重點] 關閉按鈕 -->
            <button 
              type="button" 
              class="close-button"
              aria-label="關閉"
              @click="closeDialog">
              <i class="fas fa-times"></i>
            </button>
          </div>
        </div>
        
        <!-- [重點] Modal Body -->
        <div class="modal-body">
          <p class="modal-message">
            {{ dialogData.message }}
          </p>
        </div>
        
        <!-- [重點] Modal Footer -->
        <div class="modal-footer">
          <div class="button-group">
            <!-- [重點] 取消按鈕 - 簡化結構 -->
            <button 
              type="button" 
              class="cancel-btn flex items-center gap-2 px-4 py-2 bg-gray-500 text-white rounded-lg hover:bg-gray-600 transition-colors duration-300"
              style="text-decoration: none !important;"
              @click="cancelAction">
              <i class="fas fa-times" style="text-decoration: none !important;"></i>
              <span style="text-decoration: none !important;">{{ dialogData.cancelText }}</span>
            </button>
            
            <!-- [重點] 確認按鈕 - 簡化結構 -->
            <button 
              type="button" 
              class="confirm-btn flex items-center gap-2 px-4 py-2 bg-primary text-white rounded-lg hover:bg-primary/90 transition-colors duration-300"
              style="text-decoration: none !important;"
              @click="confirmAction">
              <i :class="getIconClass(dialogData.type)" style="text-decoration: none !important;"></i>
              <span style="text-decoration: none !important;">{{ dialogData.confirmText }}</span>
            </button>
          </div>
        </div>
        
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useConfirmDialog } from '@/composables/useConfirmDialog';

// [重點] 使用確認對話框的邏輯
const { 
  isVisible, 
  dialogData, 
  confirmAction, 
  cancelAction, 
  closeDialog 
} = useConfirmDialog();

/**
 * [重點] 根據對話框類型計算圖示容器的樣式類別
 */
const iconContainerClass = computed(() => {
  const typeClasses = {
    success: 'icon-container--success',
    error: 'icon-container--error',
    warning: 'icon-container--warning',
    info: 'icon-container--info'
  };
  
  return typeClasses[dialogData.value.type] || typeClasses.warning;
});

/**
 * [重點] 根據對話框類型獲取對應的圖示類別
 * @param {string} type - 對話框類型
 * @returns {string} Font Awesome 圖示類別
 */
const getIconClass = (type) => {
  const iconMap = {
    success: 'fas fa-check-circle',
    error: 'fas fa-exclamation-circle',
    warning: 'fas fa-exclamation-triangle',
    info: 'fas fa-info-circle'
  };
  return iconMap[type] || iconMap.warning;
};
</script>

<style scoped>
/* [重點] 強制重置所有可能的文字裝飾 */
* {
  text-decoration: none !important;
  text-decoration-line: none !important;
  text-decoration-style: none !important;
  text-decoration-color: transparent !important;
  text-decoration-thickness: 0 !important;
  -webkit-text-decoration: none !important;
  -moz-text-decoration: none !important;
}

/* [重點] Modal 覆蓋層樣式 */
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

/* [重點] Modal 容器樣式 */
.modal-container {
  width: 100%;
  max-width: 500px;
  margin: 20px;
  animation: slideUp 0.3s ease-out;
}

/* [重點] Modal 內容樣式 */
.modal-content {
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.2);
  overflow: hidden;
  font-family: inherit;
}

/* [重點] Modal Header 樣式 */
.modal-header {
  padding: 24px;
  /* [重點] 移除底線 */
  /* border-bottom: 1px solid #e5e7eb; */
}

.header-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

/* [重點] 圖示容器樣式 */
.icon-container {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.icon-container--success {
  background-color: #dcfce7;
}

.icon-container--error {
  background-color: #fee2e2;
}

.icon-container--warning {
  background-color: #fef3c7;
}

.icon-container--info {
  background-color: #dbeafe;
}

/* [重點] 對話框圖示樣式 */
.dialog-icon {
  font-size: 24px;
}

.icon-container--success .dialog-icon {
  color: #16a34a;
}

.icon-container--error .dialog-icon {
  color: #dc2626;
}

.icon-container--warning .dialog-icon {
  color: #d97706;
}

.icon-container--info .dialog-icon {
  color: #2563eb;
}

/* [重點] Modal 標題樣式 */
.modal-title {
  flex: 1;
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
  line-height: 1.3;
}

/* [重點] 關閉按鈕樣式 */
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

/* [重點] Modal Body 樣式 */
.modal-body {
  /* [重點] 調整 padding，讓內容有足夠空間 */
  padding: 16px 24px 24px 24px;
}

.modal-message {
  font-size: 16px; /* [重點] 稍微放大字體 */
  color: #4b5563; /* [重點] 加深文字顏色 */
  line-height: 1.6;
  margin: 0;
  /* [重點] 移除左邊內距並置中文字 */
  padding-left: 0;
  text-align: center;
}

/* [重點] Modal Footer 樣式 */
.modal-footer {
  padding: 0 24px 24px 24px;
}

.button-group {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

/* [重點] 按鈕基本樣式 - 移除所有可能的裝飾 */
.cancel-btn,
.confirm-btn {
  border: none;
  outline: none;
  text-decoration: none !important;
  text-decoration-line: none !important;
  text-decoration-style: none !important;
  text-decoration-color: transparent !important;
  text-decoration-thickness: 0 !important;
  -webkit-text-decoration: none !important;
  -moz-text-decoration: none !important;
  font-family: inherit;
  font-style: normal;
}

.cancel-btn *,
.confirm-btn * {
  text-decoration: none !important;
  text-decoration-line: none !important;
  text-decoration-style: none !important;
  text-decoration-color: transparent !important;
  text-decoration-thickness: 0 !important;
  -webkit-text-decoration: none !important;
  -moz-text-decoration: none !important;
  font-style: normal;
}

.cancel-btn:hover,
.cancel-btn:focus,
.cancel-btn:active,
.confirm-btn:hover,
.confirm-btn:focus,
.confirm-btn:active {
  text-decoration: none !important;
  text-decoration-line: none !important;
}

.cancel-btn:hover *,
.cancel-btn:focus *,
.cancel-btn:active *,
.confirm-btn:hover *,
.confirm-btn:focus *,
.confirm-btn:active * {
  text-decoration: none !important;
  text-decoration-line: none !important;
}

/* [重點] 動畫效果 */
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

/* [重點] 響應式設計 */
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
    padding-bottom: 20px;
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
  
  .modal-message {
    padding-left: 52px;
    font-size: 13px;
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

/* [重點] 深色模式支援 */
@media (prefers-color-scheme: dark) {
  .modal-content {
    background: #1f2937;
    color: #f9fafb;
  }
  
  .modal-header {
    border-bottom-color: #374151;
  }
  
  .modal-title {
    color: #f9fafb;
  }
  
  .modal-message {
    color: #d1d5db;
  }
  
  .close-button {
    background: #374151;
    color: #9ca3af;
  }
  
  .close-button:hover {
    background: #4b5563;
    color: #d1d5db;
  }
}

/* [重點] 無障礙設計 */
@media (prefers-reduced-motion: reduce) {
  .modal-overlay,
  .modal-container {
    animation: none;
  }
}
</style>