import { ref } from 'vue';

// [重點] 使用模組級別的變數，確保所有元件共用同一個 toasts 陣列
const toasts = ref([]);

// [重點] 追蹤添加的 toast ID，避免重複
const addedToastIds = new Set();

/**
 * [重點] 顯示通知訊息
 * @param {string|Object} messageOrOptions - 訊息內容或選項物件
 * @param {string} type - 通知類型 ('success', 'error', 'info', 'warning')
 * @param {string} title - 通知標題 (可選)
 * @param {number} duration - 顯示時間 (可選)
 */
const showToast = (messageOrOptions, type = 'info', title = '', duration = 3000) => {
  // [重點] 支援兩種呼叫方式：字串參數或物件參數
  let options;
  if (typeof messageOrOptions === 'string') {
    options = { message: messageOrOptions, type, title, duration };
  } else {
    options = { type: 'info', duration: 3000, ...messageOrOptions };
  }

  // [重點] 建立通知 ID - 結合標題和訊息以避免重複
  const toastKey = `${options.title || ''}-${options.message}`;
  
  // [重點] 檢查是否是短時間內的重複通知
  if (addedToastIds.has(toastKey)) {
    console.log('避免重複顯示相同的通知:', toastKey);
    return;
  }
  
  // [重點] 標記此通知已被添加
  addedToastIds.add(toastKey);
  
  const id = Date.now() + Math.random(); // [重點] 使用時間戳 + 隨機數作為唯一 ID
  toasts.value.push({ 
    id, 
    title: options.title, 
    message: options.message, 
    type: options.type 
  });
  
  console.log('顯示 Toast:', { id, title: options.title, message: options.message, type: options.type });
  
  // [重點] 設定移除通知的定時器
  setTimeout(() => {
    toasts.value = toasts.value.filter(toast => toast.id !== id);
    
    // [重點] 延遲一段時間後清除對此通知的追蹤
    setTimeout(() => {
      addedToastIds.delete(toastKey);
    }, 1000); // [重點] 等待 1 秒後才允許相同內容的通知再次顯示
    
  }, options.duration);
};

/**
 * [重點] 移除通知
 * @param {number} id - 通知 ID
 */
const removeToast = (id) => {
  const toast = toasts.value.find(t => t.id === id);
  if (toast) {
    const toastKey = `${toast.title || ''}-${toast.message}`;
    addedToastIds.delete(toastKey);
  }
  toasts.value = toasts.value.filter(toast => toast.id !== id);
};

/**
 * [重點] 清除所有通知
 */
const clearAllToasts = () => {
  toasts.value = [];
  addedToastIds.clear();
};

/**
 * [重點] 便利方法：顯示成功通知
 * @param {string} message - 通知內容
 * @param {string} title - 通知標題 (可選)
 */
const showSuccess = (message, title = '成功') => {
  showToast({ message, type: 'success', title });
};

/**
 * [重點] 便利方法：顯示錯誤通知
 * @param {string} message - 通知內容
 * @param {string} title - 通知標題 (可選)
 */
const showError = (message, title = '錯誤') => {
  showToast({ message, type: 'error', title });
};

/**
 * [重點] 便利方法：顯示警告通知
 * @param {string} message - 通知內容
 * @param {string} title - 通知標題 (可選)
 */
const showWarning = (message, title = '警告') => {
  showToast({ message, type: 'warning', title });
};

/**
 * [重點] 便利方法：顯示資訊通知
 * @param {string} message - 通知內容
 * @param {string} title - 通知標題 (可選)
 */
const showInfo = (message, title = '提示') => {
  showToast({ message, type: 'info', title });
};

export function useToast() {
  return {
    toasts,
    showToast,
    removeToast,
    clearAllToasts,
    // [重點] 便利方法
    showSuccess,
    showError,
    showWarning,
    showInfo
  };
}