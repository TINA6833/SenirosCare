import { ref, onMounted } from 'vue';
import { useAuth } from '@/composables/useAuth';
import { useToast } from '@/composables/useToast';

/**
 * [重點] 登入狀態檢查守護 Composable
 * 提供統一的登入狀態檢查邏輯，可在任何需要登入的頁面使用
 * @param {Object} options - 設定選項
 * @returns {Object} 包含狀態和方法的物件
 */
export function useAuthGuard(options = {}) {
  // [重點] 預設設定選項
  const defaultOptions = {
    // [重點] 是否在頁面載入時自動檢查登入狀態
    autoCheck: true,
    // [重點] 未登入時是否顯示 Toast 提示
    showToast: true,
    // [重點] 登入成功後的重導向路徑（如果不設定則留在當前頁面）
    redirectAfterLogin: null,
    // [重點] 是否在載入時顯示載入動畫
    showLoading: true
  };

  // [重點] 合併使用者設定與預設設定
  const config = { ...defaultOptions, ...options };

  // [重點] 取得認證相關功能和 Toast 提示
  const { isAuthenticated, currentUser, login, checkAuthStatus } = useAuth();
  const { showToast } = useToast();

  // [重點] 頁面狀態管理
  const isCheckingAuth = ref(false);
  const isLoggingIn = ref(false);
  const authError = ref(null);

  /**
   * [重點] 檢查登入狀態的主要方法
   * @returns {Promise<Object>} 登入狀態檢查結果
   */
  async function checkAuth() {
    try {
      isCheckingAuth.value = true;
      authError.value = null;


      // [重點] 執行登入狀態檢查
      const authStatus = await checkAuthStatus();

      if (authStatus.isLoggedIn) {
        return {
          success: true,
          isLoggedIn: true,
          message: authStatus.message,
          user: authStatus.user
        };
      } else {
        if (config.showToast) {
          showToast('請先登入以繼續操作', 'warning');
        }
        return {
          success: false,
          isLoggedIn: false,
          message: authStatus.message,
          needLogin: true
        };
      }

    } catch (error) {
      authError.value = error.message || '登入狀態檢查失敗';
      
      if (config.showToast) {
        showToast('登入狀態檢查失敗，請重新整理頁面', 'error');
      }

      return {
        success: false,
        isLoggedIn: false,
        message: '登入狀態檢查失敗',
        error: error.message
      };
    } finally {
      isCheckingAuth.value = false;
    }
  }

  /**
   * [重點] 要求使用者登入的方法
   * @returns {Promise<Object>} 登入操作結果
   */
  async function requireLogin() {
    try {
      isLoggingIn.value = true;

      // [重點] 如果設定了登入後重導向路徑，則儲存起來
      if (config.redirectAfterLogin) {
        localStorage.setItem('redirectAfterLogin', config.redirectAfterLogin);
      }

      
      if (config.showToast) {
        showToast('正在跳轉到 Line 登入...', 'info');
      }

      // [重點] 執行登入流程
      const result = await login();

      return result;

    } catch (error) {
      
      if (config.showToast) {
        showToast('登入失敗，請稍後再試', 'error');
      }

      return {
        success: false,
        message: '登入失敗，請稍後再試'
      };
    } finally {
      isLoggingIn.value = false;
    }
  }

  /**
   * [重點] 確保使用者已登入的方法
   * 如果未登入會自動觸發登入流程
   * @returns {Promise<boolean>} 是否已登入或登入成功
   */
  async function ensureAuthenticated() {
    const authResult = await checkAuth();
    
    if (!authResult.isLoggedIn) {
      // [重點] 未登入，觸發登入流程
      await requireLogin();
      return false;
    }
    
    return true;
  }

  /**
   * [重點] 包裝需要登入的操作
   * @param {Function} operation - 需要登入才能執行的操作
   * @param {string} operationName - 操作名稱（用於錯誤提示）
   * @returns {Promise<any>} 操作執行結果
   */
  async function withAuthRequired(operation, operationName = '此操作') {
    try {
      // [重點] 先確保使用者已登入
      const isAuthenticated = await ensureAuthenticated();
      
      if (!isAuthenticated) {
        throw new Error(`${operationName}需要登入，請完成登入後再試`);
      }

      // [重點] 執行實際操作
      return await operation();

    } catch (error) {
      
      if (config.showToast) {
        showToast(error.message || `${operationName}失敗`, 'error');
      }
      
      throw error;
    }
  }

  // [重點] 如果設定為自動檢查，則在元件掛載時執行
  if (config.autoCheck) {
    onMounted(async () => {
      await checkAuth();
    });
  }

  return {
    // [重點] 狀態
    isCheckingAuth,
    isLoggingIn,
    authError,
    isAuthenticated,
    currentUser,

    // [重點] 方法
    checkAuth,
    requireLogin,
    ensureAuthenticated,
    withAuthRequired
  };
}