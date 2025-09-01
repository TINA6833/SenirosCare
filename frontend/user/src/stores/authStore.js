import { defineStore } from 'pinia';
import { ref, computed } from 'vue';

export const useAuthStore = defineStore('auth', () => {
  // [重點] 從 localStorage 初始化 token，這樣重新整理頁面後登入狀態不會消失
  const token = ref(localStorage.getItem('authToken') || null);
  // [重點] 儲存使用者資訊（包含 Line 頭像 URL）
  const user = ref(null);

  /**
   * [重點] 計算屬性，判斷使用者是否已登入
   * @returns {boolean}
   */
  const isAuthenticated = computed(() => !!token.value);

  /**
   * [重點] 計算屬性，取得使用者頭像 URL
   * @returns {string|null}
   */
  const userAvatar = computed(() => {
    return user.value?.imagePath || null;
  });

  /**
   * [重點] 設置使用者資料，由外部傳入
   * @param {Object} newUser - 從 service 層取得並轉換後的使用者資料
   */
  function setUser(newUser) {
    try {
      user.value = newUser;
      
      // [重點] 如果有頭像 URL，記錄到 console
      if (newUser?.imagePath) {
        console.log('Pinia 已儲存使用者頭像 URL:', newUser.imagePath);
      }
      
      console.log('使用者資料已設置:', newUser);
    } catch (error) {
      console.error('設置使用者資料時發生錯誤:', error);
    }
  }

  /**
   * [重點] 設置 token 並存入 localStorage
   * @param {string} newToken - 從後端取得的 JWT
   */
  function setToken(newToken) {
    try {
      token.value = newToken;
      localStorage.setItem('authToken', newToken);
      console.log('Token 已設置並存入 localStorage');
    } catch (error) {
      console.error('設置 token 時發生錯誤:', error);
    }
  }

  /**
   * [重點] 清除 token 和使用者資訊
   */
  function clearAuthData() {
    try {
      token.value = null;
      user.value = null;
      localStorage.removeItem('authToken');
      localStorage.removeItem('userProfile');
      console.log('認證資料已清除');
    } catch (error) {
      console.error('清除認證資料時發生錯誤:', error);
    }
  }

  /**
   * [重點] 登出功能 - 完整清除所有認證相關資料
   */
  async function logout() {
    try {
      console.log('開始執行 Pinia store 登出流程...');
      
      // [重點] 清除所有認證相關資料
      clearAuthData();
      
      return {
        success: true,
        message: '您已登出'
      };
    } catch (error) {
      console.error('登出時發生錯誤:', error);
      
      // [重點] 即使發生錯誤也要強制清除資料
      token.value = null;
      user.value = null;
      localStorage.clear();
      
      return {
        success: false,
        message: '登出時發生錯誤，但已強制清除資料'
      };
    }
  }

  /**
   * [重點] 重定向到 Line 登入頁面
   */
  function redirectToLineLogin() {
    try {
      const lineLoginUrl = 'http://localhost:8080/oauth2/authorization/line';
      console.log('準備重定向到 Line 登入:', lineLoginUrl);
      window.location.href = lineLoginUrl;
    } catch (error) {
      console.error('重定向到 Line 登入時發生錯誤:', error);
    }
  }

  return {
    // [重點] 狀態
    token,
    user,
    isAuthenticated,
    userAvatar, // [重點] 新增頭像計算屬性
    
    // [重點] 方法
    setUser,
    setToken,
    clearAuthData,
    logout,
    redirectToLineLogin
  };
});