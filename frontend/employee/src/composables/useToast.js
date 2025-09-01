import { ref } from 'vue';

// 使用模組級別的變數，確保所有元件共用同一個 toasts 陣列
const toasts = ref([]);

// 追蹤添加的 toast ID，避免重複
const addedToastIds = new Set();

/**
 * 顯示通知訊息
 * @param {Object} options - 通知選項
 * @param {string} options.title - 通知標題
 * @param {string} options.message - 通知內容
 * @param {string} options.type - 通知類型 ('success', 'error', 'info', 'warning')
 * @param {number} options.duration - 通知顯示時間 (毫秒)
 */
const showToast = ({ title, message, type = 'info', duration = 3000 }) => {
  // 建立通知 ID - 結合標題和訊息以避免重複
  const toastKey = `${title}-${message}`;
  
  // 檢查是否是短時間內的重複通知
  if (addedToastIds.has(toastKey)) {
    console.log('避免重複顯示相同的通知:', toastKey);
    return;
  }
  
  // 標記此通知已被添加
  addedToastIds.add(toastKey);
  
  const id = Date.now(); // 使用時間戳作為唯一 ID
  toasts.value.push({ id, title, message, type });
  
  // 設定移除通知的定時器
  setTimeout(() => {
    toasts.value = toasts.value.filter(toast => toast.id !== id);
    
    // 延遲一段時間後清除對此通知的追蹤
    setTimeout(() => {
      addedToastIds.delete(toastKey);
    }, 1000); // 等待 1 秒後才允許相同內容的通知再次顯示
    
  }, duration);
};

/**
 * 移除通知
 * @param {number} id - 通知 ID
 */
const removeToast = (id) => {
  const toast = toasts.value.find(t => t.id === id);
  if (toast) {
    const toastKey = `${toast.title}-${toast.message}`;
    addedToastIds.delete(toastKey);
  }
  toasts.value = toasts.value.filter(toast => toast.id !== id);
};

// 清除所有通知
const clearAllToasts = () => {
  toasts.value = [];
  addedToastIds.clear();
};

export function useToast() {
  return {
    toasts,
    showToast,
    removeToast,
    clearAllToasts
  };
}