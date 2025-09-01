import axios from 'axios';
import router from '@/router';
import { useAuthStore } from '@/stores/authStore';
import { useToast } from '@/composables/useToast';
import { useConfirmDialog } from '@/composables/useConfirmDialog';

// 建立一個 Axios 實例
const axiosInstance = axios.create({
  // 設定 API 的基礎路徑
  baseURL: 'http://localhost:8080/api', 
});

/**
 * 請求攔截器 (Request Interceptor)
 * 功能：在每個請求發送前，自動加上 Authorization 標頭
 */
axiosInstance.interceptors.request.use(
  (config) => {
    // 從 Pinia Store 中取得 auth store
    const authStore = useAuthStore();
    const token = authStore.token;

    // 如果 token 存在，則將其加入到請求的 Authorization 標頭中
    if (token) {
      // 格式為 'Bearer ' + token
      config.headers.Authorization = `Bearer ${token}`;
    }

    // 回傳設定好的 config
    return config;
  },
  (error) => {
    // 如果請求設定出錯，直接回絕
    return Promise.reject(error);
  }
);

/**
 * **重點修正：回應攔截器 - 使用 useConfirmDialog 處理登入錯誤**
 */
axiosInstance.interceptors.response.use(
  (response) => {
    // 正常回應直接返回
    return response;
  },
  (error) => {
    const { response, config } = error;
    
    // **重點：檢查是否為登入相關的 API 請求**
    const isLoginRequest = config?.url?.includes('/auth/login') || config?.url?.includes('/login');
    
    console.log('[axiosInstance] 錯誤攔截器觸發:', {
      url: config?.url,
      status: response?.status,
      isLoginRequest: isLoginRequest
    });

    // **重點：處理登入請求的特定錯誤 - 使用 useConfirmDialog**
    if (isLoginRequest && response?.status) {
      const { status, data } = response;
      const { showConfirmDialog } = useConfirmDialog();
      
      console.log('[axiosInstance] 處理登入請求錯誤，狀態碼:', status);
      
      switch (status) {
        case 401:
          // **重點：登入時的 401 - 帳號密碼錯誤**
          console.log('[axiosInstance] 顯示帳號密碼錯誤確認對話框');
          showConfirmDialog({
            title: '登入失敗',
            message: '電子郵件或密碼錯誤，請檢查後重新輸入',
            type: 'error',
            confirmText: '確定',
            cancelText: null, // **重點：不顯示取消按鈕，只有確定按鈕**
            icon: 'heroicons:x-circle'
          }).then(() => {
            // **重點：確認後清空密碼欄位並聚焦**
            const passwordInput = document.getElementById('your-password');
            if (passwordInput) {
              passwordInput.value = '';
              setTimeout(() => passwordInput.focus(), 100);
            }
          }).catch(() => {
            // **重點：處理取消情況（雖然沒有取消按鈕，但保留處理邏輯）**
            console.log('[axiosInstance] 使用者關閉登入錯誤對話框');
          });
          break;
          
        case 403:
          // **重點：登入時的 403 - 帳號被停權**
          console.log('[axiosInstance] 顯示帳號停權確認對話框');
          showConfirmDialog({
            title: '帳號已停權',
            message: '您的帳號已被管理員停權，如有疑問請聯繫系統管理員',
            type: 'warning',
            confirmText: '確定',
            cancelText: null, // **重點：不顯示取消按鈕**
            icon: 'heroicons:exclamation-triangle'
          }).then(() => {
            // **重點：確認後清空表單並聚焦到電子郵件輸入框**
            const emailInput = document.querySelector('input[type="email"]');
            const passwordInput = document.getElementById('your-password');
            
            if (emailInput) emailInput.value = '';
            if (passwordInput) passwordInput.value = '';
            
            setTimeout(() => {
              if (emailInput) emailInput.focus();
            }, 100);
          }).catch(() => {
            console.log('[axiosInstance] 使用者關閉停權對話框');
          });
          break;
          
        case 500:
          // **重點：登入時的伺服器錯誤**
          console.log('[axiosInstance] 顯示登入伺服器錯誤對話框');
          showConfirmDialog({
            title: '伺服器錯誤',
            message: '登入服務暫時無法使用，請稍後再試',
            type: 'error',
            confirmText: '確定',
            cancelText: null,
            icon: 'heroicons:server'
          });
          break;
          
        default:
          // **重點：登入時的其他錯誤**
          console.log('[axiosInstance] 顯示登入其他錯誤對話框');
          showConfirmDialog({
            title: '登入失敗',
            message: data?.message || `登入時發生錯誤 (狀態碼: ${status})，請稍後再試`,
            type: 'error',
            confirmText: '確定',
            cancelText: null,
            icon: 'heroicons:x-circle'
          });
          break;
      }
      
      // **重點：登入錯誤已處理，直接拋出錯誤但不再次處理**
      return Promise.reject(error);
    }

    // **重點：處理登入時的網路錯誤**
    if (isLoginRequest && !response) {
      console.log('[axiosInstance] 顯示登入網路錯誤對話框');
      const { showConfirmDialog } = useConfirmDialog();
      
      showConfirmDialog({
        title: '連線失敗',
        message: '無法連接到伺服器，請檢查網路連線後重試',
        type: 'error',
        confirmText: '確定',
        cancelText: null,
        icon: 'heroicons:wifi'
      });
      
      return Promise.reject(error);
    }
    
    // **重點：處理非登入請求的錯誤**
    if (response && !isLoginRequest) {
      const { status, data } = response;
      const { showToast } = useToast();
      
      // **重點：檢查使用者是否已登入（從 Store 檢查）**
      const authStore = useAuthStore();
      const isAuthenticated = authStore.isAuthenticated;
      
      console.log('[axiosInstance] 處理非登入請求錯誤:', {
        status,
        isAuthenticated,
        url: config?.url
      });

      // **重點：根據狀態碼和登入狀態處理**
      switch (status) {
        case 401:
          // **重點：已登入但 Token 過期 - 清除狀態並跳轉**
          if (isAuthenticated) {
            console.log('[axiosInstance] Token 已過期，清除登入狀態');
            authStore.logout();
            showToast({
              title: '登入已過期',
              message: '請重新登入',
              type: 'warning'
            });
            router.push('/sign-in');
          }
          break;
          
        case 403:
          // **重點：已登入但權限不足 - 跳轉到 403 頁面**
          if (isAuthenticated) {
            showToast({
              title: '存取被拒',
              message: '您沒有權限存取此資源',
              type: 'error'
            });
            router.push('/forbidden');
          }
          break;
          
        case 404:
          // **重點：資源不存在 - 只有在已登入時才跳轉**
          if (isAuthenticated) {
            showToast({
              title: '頁面不存在',
              message: '找不到請求的資源',
              type: 'error'
            });
            router.push('/error');
          }
          break;
          
        case 500:
          // **重點：伺服器錯誤 - 總是顯示錯誤訊息，但只有已登入時才跳轉**
          showToast({
            title: '伺服器錯誤',
            message: '伺服器發生內部錯誤，請稍後再試',
            type: 'error'
          });
          if (isAuthenticated) {
            router.push('/internal-server');
          }
          break;
          
        case 503:
          // **重點：服務無法使用 - 總是顯示錯誤訊息，但只有已登入時才跳轉**
          showToast({
            title: '服務暫時無法使用',
            message: '系統維護中，請稍後再試',
            type: 'warning'
          });
          if (isAuthenticated) {
            router.push('/service-unavailable');
          }
          break;
          
        default:
          // **重點：其他錯誤 - 顯示通用錯誤訊息，不跳轉**
          showToast({
            title: '操作失敗',
            message: data?.message || '發生未知錯誤',
            type: 'error'
          });
          break;
      }
    } else if (!response && !isLoginRequest) {
      // **重點：非登入請求的網路錯誤 - 使用 Toast**
      const { showToast } = useToast();
      showToast({
        title: '網路錯誤',
        message: '無法連接到伺服器，請檢查網路連線',
        type: 'error'
      });
    }
    
    // **重點：繼續拋出錯誤，讓呼叫端也能處理**
    return Promise.reject(error);
  }
);

export default axiosInstance;