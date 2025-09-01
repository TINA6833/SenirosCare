import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { useSafeAuthStore } from './useSafeStore';
import { validateToken, getLineLoginUrl } from '@/api/authApi';
import { memberService } from '@/service/memberService';

/**
 * [重點] 認證相關的 Composable
 * 提供登入、登出、狀態檢查等功能
 */
export function useAuth() {
  // [重點] 取得安全的 authStore 和 router
  const authStore = useSafeAuthStore();
  const router = useRouter();
  
  // [重點] 計算屬性：是否已登入
  const isAuthenticated = computed(() => {
    return authStore.$id ? authStore.isAuthenticated : false;
  });
  
  // [重點] 計算屬性：當前使用者資料
  const currentUser = computed(() => {
    return authStore.$id ? authStore.user : null;
  });

  /**
   * [重點] 檢查登入狀態的核心邏輯
   * @returns {Promise<Object>} 登入狀態和相關資訊
   */
  async function checkAuthStatus() {
    try {
      // [重點] 1. 先檢查 Pinia 中是否有 token
      if (!authStore.token) {
        return {
          isLoggedIn: false,
          needLogin: true,
          message: '未找到登入資訊'
        };
      }

      // [重點] 2. 檢查 token 是否仍然有效
      const isTokenValid = await validateToken();
      
      if (!isTokenValid) {
        // [重點] Token 已過期，清除本地資料
        if (authStore.$id && typeof authStore.logout === 'function') {
          await authStore.logout();
        }
        return {
          isLoggedIn: false,
          needLogin: true,
          message: 'Token 已過期，需要重新登入'
        };
      }

      // [重點] 3. Token 有效，但檢查是否有使用者資料
      if (!authStore.user) {
        // [重點] 重新取得使用者資料
        const userData = await memberService.getCurrentMember();
        if (authStore.$id && typeof authStore.setUser === 'function') {
          authStore.setUser(userData);
        }
      }

      return {
        isLoggedIn: true,
        needLogin: false,
        message: '已成功登入',
        user: authStore.user
      };

    } catch (error) {
      console.error('檢查認證狀態時發生錯誤:', error);
      
      // [重點] 發生錯誤時，清除可能無效的認證資料
      if (authStore.$id && typeof authStore.logout === 'function') {
        await authStore.logout();
      }
      
      return {
        isLoggedIn: false,
        needLogin: true,
        message: '認證檢查失敗，請重新登入'
      };
    }
  }

  /**
   * [重點] 執行登入流程
   */
  async function login() {
    try {
      // [重點] 先檢查當前認證狀態
      const authStatus = await checkAuthStatus();
      
      if (authStatus.isLoggedIn) {
        // [重點] 已經登入，不需要重複登入
        return {
          success: true,
          message: '您已經登入',
          user: authStatus.user
        };
      }

      // [重點] 需要登入，重定向到 Line 登入頁面
      redirectToLineLogin();
      
      return {
        success: true,
        message: '正在重定向到 Line 登入...',
        redirecting: true
      };

    } catch (error) {
      console.error('登入流程發生錯誤:', error);
      return {
        success: false,
        message: '登入失敗，請稍後再試'
      };
    }
  }

  /**
   * [重點] 重定向到 Line 登入頁面
   */
  function redirectToLineLogin() {
    if (authStore.$id && typeof authStore.redirectToLineLogin === 'function') {
      authStore.redirectToLineLogin();
    } else {
      // [重點] 備用方案
      const currentPath = window.location.pathname + window.location.search;
      if (currentPath !== '/auth/callback' && currentPath !== '/') {
        localStorage.setItem('redirectAfterLogin', currentPath);
      }
      window.location.href = getLineLoginUrl();
    }
  }

  /**
   * [重點] 登出功能 - 清除 Pinia 並跳轉回首頁
   */
  async function logout() {
    try {
      console.log('開始執行登出流程...');
      
      // [重點] 1. 執行 store 的登出邏輯
      let logoutResult = { success: true, message: '登出成功' };
      
      if (authStore.$id && typeof authStore.logout === 'function') {
        logoutResult = await authStore.logout();
      } else {
        // [重點] 如果 store 不可用，手動清除 localStorage
        localStorage.removeItem('authToken');
        localStorage.removeItem('redirectAfterLogin');
        localStorage.removeItem('userProfile');
        localStorage.removeItem('userPreferences');
        console.log('手動清除本地儲存資料');
      }
      
      // [重點] 2. 跳轉回首頁
      console.log('準備跳轉回首頁...');
      await router.push('/');
      
      return {
        success: true,
        message: logoutResult.message || '登出成功，已返回首頁'
      };
      
    } catch (error) {
      console.error('登出時發生錯誤:', error);
      
      // [重點] 即使發生錯誤也要清除本地資料並跳轉
      localStorage.clear(); // 清除所有本地儲存
      
      try {
        await router.push('/');
      } catch (routerError) {
        // [重點] 如果路由跳轉失敗，使用原生方式
        window.location.href = '/';
      }
      
      return {
        success: false,
        message: '登出過程中發生錯誤，但已清除本地資料並返回首頁'
      };
    }
  }

  return {
    // [重點] 狀態
    isAuthenticated,
    currentUser,
    
    // [重點] 方法
    checkAuthStatus,
    login,
    logout,
    redirectToLineLogin
  };
}