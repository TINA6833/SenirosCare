<!-- filepath: d:\VScode\backend-temp\wowdash-admin\src\components\ToastContainer.vue -->
<template>
  <!-- [重點] 使用原生 CSS 實作的 Toast 容器 -->
  <div class="toast-container">
    <transition-group
      name="toast"
      tag="div"
      class="toast-wrapper"
    >
      <div
        v-for="toast in toasts"
        :key="toast.id"
        :class="['toast', `toast--${toast.type}`]"
        @click="removeToast(toast.id)"
        role="alert"
        aria-live="assertive"
        aria-atomic="true"
      >
        <!-- [重點] Toast 圖示區域 -->
        <div class="toast__icon">
          <i v-if="toast.type === 'success'" class="fas fa-check-circle"></i>
          <i v-else-if="toast.type === 'error'" class="fas fa-exclamation-circle"></i>
          <i v-else-if="toast.type === 'warning'" class="fas fa-exclamation-triangle"></i>
          <i v-else class="fas fa-info-circle"></i>
        </div>

        <!-- [重點] Toast 內容區域 -->
        <div class="toast__content">
          <div v-if="toast.title" class="toast__title">
            {{ toast.title }}
          </div>
          <div class="toast__message">
            {{ toast.message }}
          </div>
        </div>

        <!-- [重點] 關閉按鈕 -->
        <button 
          class="toast__close"
          @click.stop="removeToast(toast.id)"
          aria-label="關閉通知"
        >
          <i class="fas fa-times"></i>
        </button>
      </div>
    </transition-group>
  </div>
</template>

<script setup>
// [重點] 引入 useToast composable
import { useToast } from '@/composables/useToast';

// [重點] 取得 toast 相關功能
const { toasts, removeToast } = useToast();
</script>

<style scoped>
/* [重點] Toast 容器樣式 - 固定在右下角 */
.toast-container {
  position: fixed;
  bottom: 20px;  /* [重點] 改為 bottom，距離底部 20px */
  right: 20px;   /* [重點] 保持距離右側 20px */
  z-index: 9999;
  pointer-events: none;
}

.toast-wrapper {
  display: flex;
  flex-direction: column-reverse; /* [重點] 改為 column-reverse，讓新的 toast 出現在下方 */
  gap: 12px;
  max-width: 400px;
}

/* [重點] Toast 基本樣式 */
.toast {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px;
  background: #ffffff;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  border-left: 4px solid;
  cursor: pointer;
  pointer-events: auto;
  min-width: 300px;
  max-width: 400px;
  transition: all 0.3s ease;
  font-family: inherit;
}

.toast:hover {
  transform: translateX(-4px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);
}

/* [重點] 不同類型的 Toast 樣式 - 使用您原本的配色 */
.toast--success {
  border-left-color: #28a745;
}

.toast--error {
  border-left-color: #dc3545;
}

.toast--warning {
  border-left-color: #ffc107;
}

.toast--info {
  border-left-color: #17a2b8;
}

/* [重點] Toast 圖示樣式 */
.toast__icon {
  flex-shrink: 0;
  font-size: 20px;
  margin-top: 2px;
}

.toast--success .toast__icon {
  color: #28a745;
}

.toast--error .toast__icon {
  color: #dc3545;
}

.toast--warning .toast__icon {
  color: #ffc107;
}

.toast--info .toast__icon {
  color: #17a2b8;
}

/* [重點] Toast 內容樣式 */
.toast__content {
  flex: 1;
  min-width: 0;
}

.toast__title {
  font-weight: 600;
  font-size: 14px;
  color: #1f2937;
  margin-bottom: 4px;
  line-height: 1.3;
}

.toast__message {
  font-size: 13px;
  color: #6b7280;
  line-height: 1.4;
  word-break: break-word;
}

/* [重點] 關閉按鈕樣式 */
.toast__close {
  flex-shrink: 0;
  width: 24px;
  height: 24px;
  border: none;
  background: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #9ca3af;
  border-radius: 4px;
  transition: all 0.2s ease;
  font-size: 12px;
}

.toast__close:hover {
  background: #f3f4f6;
  color: #6b7280;
}

.toast__close:focus {
  outline: 2px solid #3b82f6;
  outline-offset: 1px;
}

/* [重點] Toast 進入/離開動畫 - 針對右下角位置調整 */
.toast-enter-active,
.toast-leave-active {
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
}

.toast-enter-from {
  opacity: 0;
  transform: translateX(100%) scale(0.95); /* [重點] 從右側滑入 */
}

.toast-leave-to {
  opacity: 0;
  transform: translateX(100%) scale(0.95); /* [重點] 向右側滑出 */
}

.toast-move {
  transition: transform 0.3s ease;
}

/* [重點] 響應式設計 - 針對手機版調整 */
@media (max-width: 640px) {
  .toast-container {
    bottom: 10px;  /* [重點] 手機版距離底部 10px */
    right: 10px;
    left: 10px;
    max-width: none;
  }
  
  .toast-wrapper {
    max-width: none;
  }
  
  .toast {
    min-width: auto;
    max-width: none;
  }
}

/* [重點] 深色模式支援（如果需要） */
@media (prefers-color-scheme: dark) {
  .toast {
    background: #1f2937;
    color: #f9fafb;
  }
  
  .toast__title {
    color: #f9fafb;
  }
  
  .toast__message {
    color: #d1d5db;
  }
  
  .toast__close:hover {
    background: #374151;
    color: #d1d5db;
  }
}

/* [重點] 無障礙設計 */
@media (prefers-reduced-motion: reduce) {
  .toast-enter-active,
  .toast-leave-active,
  .toast,
  .toast__close {
    transition: none;
  }
}

/* [重點] 確保 Toast 不會被底部導航或其他固定元素遮擋 */
@media (max-width: 768px) {
  .toast-container {
    bottom: 80px; /* [重點] 如果有底部導航，可以調整這個數值 */
  }
}
</style>