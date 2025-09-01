import { getActivePinia } from 'pinia';

/**
 * [重點] 安全的 Store 取得函式
 * 這個函式會檢查 Pinia 是否已經準備好，如果沒有，會回傳一個預設的空 store
 * @param {Function} storeFactory - 例如 useAuthStore
 * @param {Object} defaultState - 預設狀態，當 Pinia 未準備好時使用
 * @returns {Object} store 實例或預設狀態
 */
export function useSafeStore(storeFactory, defaultState = {}) {
  try {
    // [重點] 檢查 Pinia 是否已經初始化
    const pinia = getActivePinia();

    if (!pinia) {
      console.warn('Pinia 尚未初始化，使用預設狀態');
      return {
        ...defaultState,
        $id: null,
        // [重點] 提供基本的方法防止錯誤
        setToken: () => console.warn('Pinia 未初始化，無法設置 token'),
        setUser: () => console.warn('Pinia 未初始化，無法設置使用者'),
        logout: () => console.warn('Pinia 未初始化，無法登出'),
        redirectToLineLogin: () => console.warn('Pinia 未初始化，無法重導向'),
      };
    }

    // [重點] Pinia 已初始化，正常回傳 store
    return storeFactory();
  } catch (error) {
    console.error('取得 Store 時發生錯誤:', error);
    return {
      ...defaultState,
      $id: null,
      // [重點] 錯誤時的備用方法
      setToken: () => console.warn('Store 錯誤，無法設置 token'),
      setUser: () => console.warn('Store 錯誤，無法設置使用者'),
      logout: () => console.warn('Store 錯誤，無法登出'),
      redirectToLineLogin: () => console.warn('Store 錯誤，無法重導向'),
    };
  }
}

/**
 * [重點] 專門針對 AuthStore 的安全包裝器
 * @returns {Object} authStore 或預設的 auth 狀態
 */
export function useSafeAuthStore() {
  // [重點] 延遲動態引入，確保不會在模組載入時就執行
  return useSafeStore(
    () => {
      const { useAuthStore } = require('@/stores/authStore');
      return useAuthStore();
    },
    // [重點] 預設的認證狀態
    {
      token: localStorage.getItem('authToken') || null,
      user: null,
      isAuthenticated: false,
    }
  );
}