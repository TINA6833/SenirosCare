<template>
  <!-- 確認對話框 Modal -->
  <div 
    v-if="isVisible" 
    class="modal fade show confirm-dialog-modal" 
    tabindex="-1" 
    aria-labelledby="confirmDialogLabel" 
    aria-hidden="false"
    style="display: block;"
    @click.self="closeDialog">
    
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content shadow-lg border-0">
        
        <!-- Modal Header -->
        <div class="modal-header border-0 pb-0">
          <div class="d-flex align-items-center gap-3 w-100">
            <!-- 圖示 -->
            <div 
              class="icon-container d-flex align-items-center justify-content-center rounded-circle"
              :class="iconContainerClass">
              <iconify-icon 
                :icon="dialogData.icon" 
                class="icon-lg"
                :class="iconClass">
              </iconify-icon>
            </div>
            
            <!-- 標題 -->
            <h5 class="modal-title flex-grow-1 mb-0 fw-bold" id="confirmDialogLabel">
              {{ dialogData.title }}
            </h5>
            
            <!-- 關閉按鈕 -->
            <button 
              type="button" 
              class="btn-close opacity-50"
              aria-label="關閉"
              @click="closeDialog">
            </button>
          </div>
        </div>
        
        <!-- Modal Body -->
        <div class="modal-body pt-3">
          <p class="text-secondary mb-0 fs-6 lh-base">
            {{ dialogData.message }}
          </p>
        </div>
        
        <!-- Modal Footer -->
        <div class="modal-footer border-0 pt-1">
          <div class="d-flex gap-2 w-100">
            <!-- 取消按鈕 -->
            <button 
              type="button" 
              class="btn btn-outline-secondary flex-fill py-2"
              @click="cancelAction">
              {{ dialogData.cancelText }}
            </button>
            
            <!-- 確認按鈕 -->
            <button 
              type="button" 
              class="btn flex-fill py-2"
              :class="dialogData.confirmButtonClass"
              @click="confirmAction">
              {{ dialogData.confirmText }}
            </button>
          </div>
        </div>
        
      </div>
    </div>
  </div>
  
  <!-- Modal 背景遮罩 -->
  <div 
    v-if="isVisible" 
    class="modal-backdrop fade show">
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useConfirmDialog } from '@/composables/useConfirmDialog';

// 使用確認對話框的邏輯
const { 
  isVisible, 
  dialogData, 
  confirmAction, 
  cancelAction, 
  closeDialog 
} = useConfirmDialog();

/**
 * 根據對話框類型計算圖示容器的樣式類別
 */
const iconContainerClass = computed(() => {
  const typeClasses = {
    success: 'bg-success-subtle',
    error: 'bg-danger-subtle',
    warning: 'bg-warning-subtle',
    info: 'bg-info-subtle'
  };
  
  return typeClasses[dialogData.value.type] || typeClasses.warning;
});

/**
 * 根據對話框類型計算圖示的樣式類別
 */
const iconClass = computed(() => {
  const typeClasses = {
    success: 'text-success',
    error: 'text-danger',
    warning: 'text-warning',
    info: 'text-info'
  };
  
  return typeClasses[dialogData.value.type] || typeClasses.warning;
});
</script>

<style scoped>
/* 確認對話框專用樣式 */
.confirm-dialog-modal {
  z-index: 1060; /* 確保在其他元素之上 */
}

.modal-content {
  border-radius: 12px;
  overflow: hidden;
}

.modal-header {
  padding: 24px 24px 0 24px;
}

.modal-body {
  padding: 12px 24px;
}

.modal-footer {
  padding: 0 24px 24px 24px;
}

/* 圖示容器樣式 */
.icon-container {
  width: 48px;
  height: 48px;
  flex-shrink: 0;
}

.icon-lg {
  font-size: 24px;
}

/* 按鈕樣式調整 */
.btn {
  font-weight: 500;
  border-radius: 8px;
  transition: all 0.2s ease;
}

.btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

/* 關閉按鈕樣式 */
.btn-close {
  font-size: 0.8rem;
  padding: 8px;
}

.btn-close:hover {
  opacity: 1 !important;
  transform: scale(1.1);
}

/* 響應式調整 */
@media (max-width: 576px) {
  .modal-dialog {
    margin: 1rem;
  }
  
  .modal-header,
  .modal-body,
  .modal-footer {
    padding-left: 16px;
    padding-right: 16px;
  }
  
  .icon-container {
    width: 40px;
    height: 40px;
  }
  
  .icon-lg {
    font-size: 20px;
  }
}

/* 深色主題支援 */
[data-theme="dark"] .modal-content {
  background-color: var(--bs-dark);
  color: var(--bs-light);
}

[data-theme="dark"] .btn-close {
  filter: invert(1);
}
</style>