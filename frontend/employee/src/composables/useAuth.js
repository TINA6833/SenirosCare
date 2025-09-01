import { computed } from 'vue';
import { useAuthStore } from '@/stores/authStore';
import { useRouter } from 'vue-router';

/**
 * 認證狀態管理的可組合函數
 * 作為 Auth Store 的包裝層，提供元件友好的介面
 * @returns {Object} 認證相關的狀態和方法
 */
export function useAuth() {
  // 取得 Auth Store 實例
  const authStore = useAuthStore();
  const router = useRouter();

  // 計算屬性 - 直接從 Auth Store 取得
  const isAuthenticated = computed(() => authStore.isAuthenticated);
  const currentEmployee = computed(() => authStore.employee);
  const employeeName = computed(() => authStore.employeeName);
  const employeeEmail = computed(() => authStore.employeeEmail);
  const employeeRoles = computed(() => authStore.employeeRoles);

  /**
   * 員工登入 - 委託給 Auth Store
   * @param {string} email - 員工電子郵件  
   * @param {string} password - 密碼
   */
  const login = async (email, password) => {
    try {
      await authStore.login(email, password);
    } catch (err) {
      console.error('useAuth 登入失敗:', err);
      throw err;
    }
  };

  /**
   * 員工登出 - 委託給 Auth Store
   */
  const logout = () => {
    authStore.logout();
  };

  /**
   * 檢查認證狀態
   * @returns {boolean} 是否已通過認證
   */
  const checkAuthStatus = () => {
    return authStore.isAuthenticated;
  };

  /**
   * 需要認證的路由守衛
   * @returns {boolean} 是否允許訪問
   */
  const requireAuth = () => {
    const isLoggedIn = checkAuthStatus();
    
    if (!isLoggedIn) {
      console.log('useAuth: 未登入，導向登入頁面');
      router.push('/authentication/sign-in');
      return false;
    }
    
    return true;
  };

  return {
    // 狀態
    isAuthenticated,
    currentEmployee,
    
    // 計算屬性
    employeeName,
    employeeEmail,
    employeeRoles,
    
    // 方法
    login,
    logout,
    checkAuthStatus,
    requireAuth
  };
}