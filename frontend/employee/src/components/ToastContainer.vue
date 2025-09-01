<!-- filepath: d:\VScode\backend-temp\wowdash-admin\src\components\ToastContainer.vue -->
<template>
  <div class="toast-container position-fixed top-0 end-0 p-3">
    <transition-group name="toast">
      <div v-for="toast in toasts" :key="toast.id" 
          class="toast show" 
          role="alert" 
          aria-live="assertive" 
          aria-atomic="true">
        <div class="toast-header" :class="getHeaderClass(toast.type)">
          <strong class="me-auto">{{ toast.title }}</strong>
          <button type="button" class="btn-close" @click="removeToast(toast.id)"></button>
        </div>
        <div class="toast-body">
          {{ toast.message }}
        </div>
      </div>
    </transition-group>
  </div>
</template>

<script>
import { useToast } from '@/composables/useToast';

export default {
  name: 'ToastContainer',
  setup() {
    const { toasts, removeToast } = useToast();

    // 根據通知類型獲取對應的標頭樣式類別
    const getHeaderClass = (type) => {
      const classMap = {
        success: 'bg-success text-white',
        error: 'bg-danger text-white',
        warning: 'bg-warning',
        info: 'bg-info text-white'
      };
      return classMap[type] || 'bg-secondary text-white';
    };

    return {
      toasts,
      removeToast,
      getHeaderClass
    };
  }
};
</script>

<style scoped>
.toast-container {
  z-index: 1050;
}

/* 添加過渡動畫 */
.toast-enter-active,
.toast-leave-active {
  transition: all 0.3s ease;
}

.toast-enter-from {
  transform: translateX(100%);
  opacity: 0;
}

.toast-leave-to {
  transform: translateX(100%);
  opacity: 0;
}
</style>