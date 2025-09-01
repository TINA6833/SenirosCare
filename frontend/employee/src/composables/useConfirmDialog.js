import { ref } from 'vue';

// 使用模組級別的變數，確保所有元件共用同一個對話框狀態
const isVisible = ref(false);
const dialogData = ref({
  title: '',
  message: '',
  type: 'warning', // 'success', 'error', 'warning', 'info'
  confirmText: '確認',
  cancelText: '取消',
  confirmButtonClass: 'btn-primary',
  icon: 'heroicons:exclamation-triangle'
});

// 儲存 resolve 和 reject 函數，用於 Promise 處理
let resolvePromise = null;
let rejectPromise = null;

/**
 * 顯示確認對話框
 * @param {Object} options - 對話框選項
 * @param {string} options.title - 對話框標題
 * @param {string} options.message - 對話框內容
 * @param {string} options.type - 對話框類型 ('success', 'error', 'warning', 'info')
 * @param {string} options.confirmText - 確認按鈕文字
 * @param {string} options.cancelText - 取消按鈕文字
 * @param {string} options.confirmButtonClass - 確認按鈕樣式類別
 * @param {string} options.icon - 圖示名稱
 * @returns {Promise<boolean>} 回傳使用者的選擇結果
 */
const showConfirmDialog = (options = {}) => {
  // 根據類型設定預設配置
  const typeConfigs = {
    success: {
      confirmButtonClass: 'btn-success',
      icon: 'heroicons:check-circle'
    },
    error: {
      confirmButtonClass: 'btn-danger',
      icon: 'heroicons:x-circle'
    },
    warning: {
      confirmButtonClass: 'btn-warning',
      icon: 'heroicons:exclamation-triangle'
    },
    info: {
      confirmButtonClass: 'btn-info',
      icon: 'heroicons:information-circle'
    }
  };

  const typeConfig = typeConfigs[options.type] || typeConfigs.warning;

  // 合併設定
  dialogData.value = {
    title: options.title || '確認操作',
    message: options.message || '您確定要執行此操作嗎？',
    type: options.type || 'warning',
    confirmText: options.confirmText || '確認',
    cancelText: options.cancelText || '取消',
    confirmButtonClass: options.confirmButtonClass || typeConfig.confirmButtonClass,
    icon: options.icon || typeConfig.icon
  };

  // 顯示對話框
  isVisible.value = true;

  // 回傳 Promise 以處理使用者的選擇
  return new Promise((resolve, reject) => {
    resolvePromise = resolve;
    rejectPromise = reject;
  });
};

/**
 * 確認操作
 */
const confirmAction = () => {
  isVisible.value = false;
  if (resolvePromise) {
    resolvePromise(true);
    resolvePromise = null;
    rejectPromise = null;
  }
};

/**
 * 取消操作
 */
const cancelAction = () => {
  isVisible.value = false;
  if (rejectPromise) {
    rejectPromise(false);
    resolvePromise = null;
    rejectPromise = null;
  }
};

/**
 * 關閉對話框（等同於取消）
 */
const closeDialog = () => {
  cancelAction();
};

export function useConfirmDialog() {
  return {
    // 狀態
    isVisible,
    dialogData,
    
    // 方法
    showConfirmDialog,
    confirmAction,
    cancelAction,
    closeDialog
  };
}