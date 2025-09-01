import { ref } from 'vue';

// [重點] 使用模組級別的變數，確保所有元件共用同一個對話框狀態
const isVisible = ref(false);
const dialogData = ref({
  title: '',
  message: '',
  type: 'warning', // [重點] 'success', 'error', 'warning', 'info'
  confirmText: '確認',
  cancelText: '取消',
  icon: 'fas fa-exclamation-triangle' // [重點] 改用 Font Awesome 圖示
});

// [重點] 儲存 resolve 和 reject 函數，用於 Promise 處理
let resolvePromise = null;
let rejectPromise = null;

/**
 * [重點] 顯示確認對話框
 * @param {Object} options - 對話框選項
 * @param {string} options.title - 對話框標題
 * @param {string} options.message - 對話框內容
 * @param {string} options.type - 對話框類型 ('success', 'error', 'warning', 'info')
 * @param {string} options.confirmText - 確認按鈕文字
 * @param {string} options.cancelText - 取消按鈕文字
 * @param {string} options.icon - 圖示名稱 (Font Awesome)
 * @returns {Promise<boolean>} 回傳使用者的選擇結果
 */
const showConfirmDialog = (options = {}) => {
  // [重點] 根據類型設定預設配置 - 移除 Bootstrap 類別
  const typeConfigs = {
    success: {
      icon: 'fas fa-check-circle'
    },
    error: {
      icon: 'fas fa-exclamation-circle'
    },
    warning: {
      icon: 'fas fa-exclamation-triangle'
    },
    info: {
      icon: 'fas fa-info-circle'
    }
  };

  const typeConfig = typeConfigs[options.type] || typeConfigs.warning;

  // [重點] 合併設定
  dialogData.value = {
    title: options.title || '確認操作',
    message: options.message || '您確定要執行此操作嗎？',
    type: options.type || 'warning',
    confirmText: options.confirmText || '確認',
    cancelText: options.cancelText || '取消',
    icon: options.icon || typeConfig.icon
  };

  // [重點] 顯示對話框
  isVisible.value = true;

  // [重點] 回傳 Promise 以處理使用者的選擇
  return new Promise((resolve, reject) => {
    resolvePromise = resolve;
    rejectPromise = reject;
  });
};

/**
 * [重點] 確認操作
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
 * [重點] 取消操作
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
 * [重點] 關閉對話框（等同於取消）
 */
const closeDialog = () => {
  cancelAction();
};

/**
 * [重點] 便利方法：顯示成功確認對話框
 * @param {string} message - 訊息內容
 * @param {string} title - 標題 (可選)
 * @param {Object} options - 其他選項 (可選)
 */
const showSuccessDialog = (message, title = '成功', options = {}) => {
  return showConfirmDialog({
    message,
    title,
    type: 'success',
    ...options
  });
};

/**
 * [重點] 便利方法：顯示錯誤確認對話框
 * @param {string} message - 訊息內容
 * @param {string} title - 標題 (可選)
 * @param {Object} options - 其他選項 (可選)
 */
const showErrorDialog = (message, title = '錯誤', options = {}) => {
  return showConfirmDialog({
    message,
    title,
    type: 'error',
    ...options
  });
};

/**
 * [重點] 便利方法：顯示警告確認對話框
 * @param {string} message - 訊息內容
 * @param {string} title - 標題 (可選)
 * @param {Object} options - 其他選項 (可選)
 */
const showWarningDialog = (message, title = '警告', options = {}) => {
  return showConfirmDialog({
    message,
    title,
    type: 'warning',
    ...options
  });
};

/**
 * [重點] 便利方法：顯示資訊確認對話框
 * @param {string} message - 訊息內容
 * @param {string} title - 標題 (可選)
 * @param {Object} options - 其他選項 (可選)
 */
const showInfoDialog = (message, title = '提示', options = {}) => {
  return showConfirmDialog({
    message,
    title,
    type: 'info',
    ...options
  });
};

export function useConfirmDialog() {
  return {
    // [重點] 狀態
    isVisible,
    dialogData,
    
    // [重點] 基本方法
    showConfirmDialog,
    confirmAction,
    cancelAction,
    closeDialog,
    
    // [重點] 便利方法
    showSuccessDialog,
    showErrorDialog,
    showWarningDialog,
    showInfoDialog
  };
}