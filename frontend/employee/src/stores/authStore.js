import { defineStore } from 'pinia';
import { ref, computed, readonly } from 'vue';
import { employeeService } from '@/services/employeeService';
import router from '@/router';

/**
 * 認證狀態管理 Store
 * 負責管理登入狀態、JWT Token 和員工資料
 */
export const useAuthStore = defineStore('auth', () => {
  // ===== 狀態定義 =====
  
  /**
   * JWT Token - 從 sessionStorage 讀取初始狀態
   */
  const token = ref(sessionStorage.getItem('authToken') || null);
  
  /**
   * 員工資料 - 從 sessionStorage 讀取初始狀態
   */
  const employee = ref(() => {
    const stored = sessionStorage.getItem('currentEmployee');
    return stored ? JSON.parse(stored) : null;
  });

  // ===== 計算屬性 =====
  
  /**
   * 判斷使用者是否已登入
   * @returns {boolean} 登入狀態
   */
  const isAuthenticated = computed(() => {
    return !!(token.value && employee.value && employee.value.isActive);
  });

  /**
   * 員工姓名 - 對應後端的 empName
   * @returns {string} 員工姓名
   */
  const employeeName = computed(() => {
    return employee.value?.empName || '';
  });

  /**
   * 員工信箱 - 對應後端的 email
   * @returns {string} 員工信箱
   */
  const employeeEmail = computed(() => {
    return employee.value?.email || '';
  });

  /**
   * 員工角色列表 - 對應後端的 roles 陣列
   * @returns {Array} 角色列表
   */
  const employeeRoles = computed(() => {
    return employee.value?.roles || [];
  });

  /**
   * 員工頭像路徑 - 對應後端的 imagePath
   * @returns {string} 頭像完整路徑
   */
  const employeeAvatar = computed(() => {
    const imagePath = employee.value?.imagePath;
    if (!imagePath) return '';
    
    // 如果是相對路徑，加上後端伺服器 URL
    if (imagePath.startsWith('/')) {
      return `http://localhost:8080${imagePath}`;
    }
    
    // 如果已經是完整 URL，直接回傳
    return imagePath;
  });

  /**
   * 員工 ID - 對應後端的 empId
   * @returns {number|null} 員工 ID
   */
  const employeeId = computed(() => {
    return employee.value?.empId || null;
  });

  /**
   * 取得主要角色顯示名稱
   * @returns {string} 格式化的角色名稱
   */
  const primaryRoleName = computed(() => {
    const roles = employeeRoles.value;
    if (!roles || roles.length === 0) return '一般員工';
    
    // 角色優先級：ADMIN > MANAGER > USER
    const roleHierarchy = {
      'ROLE_ADMIN': '系統管理員',
      'ROLE_MANAGER': '主管',
      'ROLE_USER': '一般員工',
      'ROLE_EMPLOYEE': '員工'
    };
    
    // 尋找最高權限的角色
    for (const priority of ['ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_USER', 'ROLE_EMPLOYEE']) {
      const foundRole = roles.find(role => role.role_name === priority);
      if (foundRole) {
        return roleHierarchy[priority] || priority.replace('ROLE_', '');
      }
    }
    
    // 如果沒有找到預定義角色，回傳第一個角色
    return roles[0].role_name.replace('ROLE_', '');
  });

  /**
   * 檢查是否有特定角色的函數
   * @param {string} roleName - 角色名稱（例如：'ROLE_ADMIN'）
   * @returns {boolean} 是否擁有該角色
   */
  const hasRole = computed(() => {
    return (roleName) => {
      const roles = employeeRoles.value;
      return roles.some(role => role.role_name === roleName);
    };
  });

  // ===== 動作方法 =====

  /**
   * 登入動作
   * @param {string} email - 使用者信箱
   * @param {string} password - 使用者密碼
   * @returns {Promise<Object>} 登入結果
   */
  async function login(email, password) {
    try {
      console.log('Auth Store: 開始登入流程，帳號:', email);
      
      // 呼叫 service 層的登入方法
      const authResponse = await employeeService.login(email, password);
      
      console.log('Auth Store: 從 Service 層收到的資料', authResponse);
      
      // **關鍵修正**: 驗證 authResponse.employeeProfileDto 是否存在
      if (!authResponse || !authResponse.token || !authResponse.employeeProfileDto) {
        throw new Error('登入回應格式不正確：缺少 token 或 employeeProfileDto');
      }
      
      // 從 authResponse 解構出 token 和 employeeProfileDto
      const { token: authToken, employeeProfileDto } = authResponse;
      
      // 驗證員工資料的必要欄位
      if (!employeeProfileDto.empId || !employeeProfileDto.empName || !employeeProfileDto.email) {
        throw new Error('員工資料不完整：缺少必要欄位');
      }
      
      // 檢查員工是否為啟用狀態
      if (!employeeProfileDto.isActive) {
        throw new Error('此帳號已被停用，請聯繫系統管理員');
      }
      
      // 設定 Token 和員工資料
      token.value = authToken;
      employee.value = employeeProfileDto;
      
      // 持久化到 sessionStorage
      sessionStorage.setItem('authToken', authToken);
      sessionStorage.setItem('currentEmployee', JSON.stringify(employeeProfileDto));
      sessionStorage.setItem('isLoggedIn', 'true');
      
      console.log('Auth Store: 登入成功', {
        empId: employeeProfileDto.empId,
        empName: employeeProfileDto.empName
      });
      
      // 登入成功後導向首頁
      router.push('/');
      
      return authResponse;
      
    } catch (error) {
      console.error('Auth Store: 登入失敗', error);
      clearAuthState();
      throw error;
    }
  }

  /**
   * 登出動作
   */
  function logout() {
    console.log('Auth Store: 執行登出');
    
    // 清除狀態
    clearAuthState();
    
    // 導向登入頁面
    router.push('/sign-in');
  }

  /**
   * 清除認證狀態
   */
  function clearAuthState() {
    // 清除 Pinia 狀態
    token.value = null;
    employee.value = null;
    
    // 清除 sessionStorage 中的所有認證相關資料
    sessionStorage.removeItem('authToken');
    sessionStorage.removeItem('currentEmployee');
    sessionStorage.removeItem('isLoggedIn');
    
    console.log('Auth Store: 認證狀態已清除');
  }

  /**
   * 更新員工資料 - 修正：不使用 this，直接使用變數
   * @param {Object} updatedEmployee - 更新的員工資料
   */
  function updateEmployee(updatedEmployee) {
    console.log('Auth Store: 準備更新員工資料', updatedEmployee);
    
    // 驗證更新的員工資料結構
    if (!updatedEmployee || !updatedEmployee.empId) {
      console.error('Auth Store: 更新員工資料失敗 - 資料格式不正確', updatedEmployee);
      return;
    }
    
    // 更新 Pinia 狀態中的員工資料
    employee.value = updatedEmployee;
    
    // 同時更新 sessionStorage 中的資料
    if (token.value) {
      sessionStorage.setItem('currentEmployee', JSON.stringify(updatedEmployee));
      console.log('Auth Store: 員工資料已更新並同步到 sessionStorage', {
        empId: updatedEmployee.empId,
        empName: updatedEmployee.empName,
        email: updatedEmployee.email
      });
    }
  }

  /**
   * 初始化認證狀態
   */
  function initializeAuth() {
    const storedToken = sessionStorage.getItem('authToken');
    const storedEmployee = sessionStorage.getItem('currentEmployee');
    
    if (storedToken && storedEmployee) {
      try {
        const parsedEmployee = JSON.parse(storedEmployee);
        
        // 驗證儲存的員工資料結構
        if (parsedEmployee.empId && parsedEmployee.empName && parsedEmployee.email) {
          token.value = storedToken;
          employee.value = parsedEmployee;
          console.log('Auth Store: 從 sessionStorage 恢復認證狀態', {
            empName: parsedEmployee.empName,
            email: parsedEmployee.email
          });
        } else {
          throw new Error('儲存的員工資料結構不正確');
        }
      } catch (error) {
        console.error('Auth Store: 初始化認證狀態失敗', error);
        clearAuthState();
      }
    } else {
      console.log('Auth Store: 無儲存的認證資料');
    }
  }

  // 暴露所有需要的狀態和方法
  return {
    // 狀態
    token: readonly(token),
    employee: readonly(employee),
    
    // 計算屬性
    isAuthenticated,
    employeeName,
    employeeEmail,
    employeeRoles,
    employeeAvatar,
    employeeId,
    primaryRoleName,
    hasRole,
    
    // 方法
    login,
    logout,
    updateEmployee,
    initializeAuth
  };
});