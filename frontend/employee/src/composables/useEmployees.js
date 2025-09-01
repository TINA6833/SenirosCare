import { ref, computed, onMounted } from 'vue';
import { employeeService } from '@/services/employeeService';
/** Composables 的用途
1.狀態管理：管理元件間共享的響應式狀態
2.業務邏輯協調：調用不同的 Service 層方法
3.快取管理：控制資料的快取策略和更新時機
4.錯誤處理：統一處理和暴露錯誤狀態給元件
5.資料聚合：將多個 Service 的結果組合後提供給元件使用
 */


/**
 * 員工資料管理的可組合函數
 * 提供元件使用的狀態和方法，作為元件與服務層之間的控制器
 * @param {Object} options - 配置選項
 * @param {boolean} options.autoLoad - 是否在掛載時自動載入資料
 * @param {Object} options.defaultParams - 預設查詢參數
 * @returns {Object} 員工資料狀態和操作方法
 */
export function useEmployees(options = { autoLoad: true, defaultParams: { isActive: true } }) {
    // 基本狀態
    const employees = ref([]); // 員工列表
    const isLoading = ref(false); // 載入狀態
    const error = ref(null); // 錯誤訊息
    const lastFetched = ref(null); // 上次資料更新時間 (用於快取管理)
    const queryParams = ref({ ...options.defaultParams }); // 查詢參數
    const roles = ref([]); // 職位列表

    // 計算屬性 - 修正欄位名稱對應
    // 統計啟用的員工數量
    const activeEmployeeCount = computed(() => 
        employees.value.filter(emp => emp.isActive === 'active').length
    );

    // 總員工數量
    const totalEmployeeCount = computed(() => employees.value.length);

    /**
     * 從服務層獲取員工資料
     * @param {Object} params - 查詢參數，若不提供則使用儲存的查詢參數
     * @param {boolean} forceRefresh - 是否強制重新獲取 (忽略快取)
     */
    async function fetchEmployees(params = null, forceRefresh = false) {
        // 如果提供了新參數，則更新查詢參數
        if (params) {
            queryParams.value = { ...queryParams.value, ...params };
        }
        
        // 檢查是否有快取且未過期 (5分鐘內)
        const now = new Date();
        const cacheValid = lastFetched.value && 
                        (now - lastFetched.value) < 5 * 60 * 1000;
        
        // 如果資料已經載入且快取有效，且不強制重新整理，則直接返回
        if (employees.value.length > 0 && cacheValid && !forceRefresh) {
            console.log('使用快取的員工資料');
            return;
        }

        isLoading.value = true;
        error.value = null;
        
        try {
            // 獲取員工資料
            employees.value = await employeeService.getEmployees(queryParams.value);
            lastFetched.value = new Date();
        } catch (err) {
            error.value = err.message || '載入員工資料時發生錯誤';
            console.error('載入員工資料失敗:', err);
        } finally {
            isLoading.value = false;
        }
    }

    /**
     * 獲取所有可用的職位列表
     */
    async function fetchRoles() {
        isLoading.value = true;
        error.value = null;
        try {
            // 獲取職位資料
            roles.value = await employeeService.getAllRoles();
        } catch (err) {
            error.value = err.message || '載入職位資料時發生錯誤';
            console.error('載入職位列表失敗:', err);
        } finally {
            isLoading.value = false;
        }
    }

    /**
     * 透過員工姓名搜尋員工
     * @param {string} empName - 員工姓名（支援模糊搜尋）
     */
    async function searchEmployeesByName(empName) {
        // 更新查詢參數並重新獲取資料
        await fetchEmployees({ empName }, true);
    }

    /**
     * 切換篩選已啟用/已停用的員工
     * @param {boolean} isActive 是否篩選啟用的員工
     */
    async function filterByStatus(isActive) {
      console.log('篩選狀態更新為：', isActive ? '啟用' : '停用'); // 調試日誌
      
      // 更新查詢參數
      queryParams.value = { ...queryParams.value, isActive };
      
      // 強制重新載入資料
      await fetchEmployees(null, true);
    }

    /**
     * 根據職位篩選員工
     * @param {string} role - 職位名稱
     */
    async function filterByRole(role) {
        // 更新查詢參數並重新獲取資料
        await fetchEmployees({ role }, true);
    }

    /**
     * 重設所有篩選條件並重新載入資料
     */
    async function resetFilters() {
        await fetchEmployees({ isActive: true, empName: null, role: null }, true);
    }

    /**
     * 獲取特定員工的詳細資料
     * @param {number} id - 員工 ID
     * @returns {Promise<Object>} 員工詳細資料
     */
    async function getEmployeeDetail(id) {
        isLoading.value = true;
        error.value = null;
        
        try {
            const employee = await employeeService.getEmployeeById(id);
            return employee;
        } catch (err) {
            error.value = err.message || `獲取員工 ID ${id} 資料時發生錯誤`;
            console.error('獲取員工詳情失敗:', err);
            throw err;
        } finally {
            isLoading.value = false;
        }
    }

    /**
     * 新增員工
     * @param {Object} employeeData - 員工資料
     * @returns {Promise<Object>} 新增的員工資料
     */
    async function addEmployee(employeeData) {
        isLoading.value = true;
        error.value = null;
        
        try {
            // 呼叫服務層新增員工
            const newEmployee = await employeeService.addEmployee(employeeData);
            
            // 重新載入員工列表以反映變更
            await fetchEmployees(null, true);
            
            return newEmployee;
        } catch (err) {
            error.value = err.message || '新增員工時發生錯誤';
            console.error('新增員工失敗:', err);
            throw err;
        } finally {
            isLoading.value = false;
        }
    }

    /**
     * 更新員工資料
     * @param {number} empId - 員工 ID
     * @param {Object} employeeData - 更新的員工資料
     * @returns {Promise<Object>} 更新後的員工資料
     */
    async function updateEmployee(empId, employeeData) {
        isLoading.value = true;
        error.value = null;
        
        try {
            // 呼叫服務層更新員工
            const updatedEmployee = await employeeService.updateEmployee(empId, employeeData);
            
            // 更新本地快取中的員工資料 - 修正欄位名稱
            const index = employees.value.findIndex(emp => emp.empId === empId);
            if (index !== -1) {
                employees.value[index] = updatedEmployee;
            }
            
            return updatedEmployee;
        } catch (err) {
            error.value = err.message || `更新員工 ID ${empId} 時發生錯誤`;
            console.error('更新員工失敗:', err);
            throw err;
        } finally {
            isLoading.value = false;
        }
    }

    /**
     * 重新賦予員工權限
     * @param {number} empId - 員工 ID
     * @param {Array<number>} newRoleIds - 新的職等 ID 列表
     * @returns {Promise<Array>} 更新後的權限列表
     */
    async function updateEmployeeRoles(empId, newRoleIds) {
        console.log(`[useEmployees.js] 呼叫 updateEmployeeRoles。員工 ID: ${empId}, 職位 IDs:`, newRoleIds);
        isLoading.value = true;
        error.value = null;
        
        try {
            // 呼叫服務層更新員工權限
            const updatedRoles = await employeeService.updateEmployeeRoles(empId, newRoleIds);
            
            // 更新本地快取中的員工權限資料
            const index = employees.value.findIndex(emp => emp.empId === empId);
            if (index !== -1) {
                employees.value[index].roles = updatedRoles;
            }
            
            return updatedRoles;
        } catch (err) {
            error.value = err.message || `更新員工 ID ${empId} 權限時發生錯誤`;
            console.error('更新員工權限失敗:', err);
            throw err;
        } finally {
            isLoading.value = false;
        }
    }

    /**
     * 切換員工狀態 (啟用/停用) - 修正方法
     * @param {number} empId 員工 ID
     * @returns {Promise<void>}
     */
    const toggleEmployeeStatus = async (empId) => {
        isLoading.value = true;
        error.value = null;
        
        try {
            // 找出當前員工
            const employee = employees.value.find(emp => emp.empId === empId);
            if (!employee) {
                throw new Error(`找不到 ID 為 ${empId} 的員工`);
            }
            
            // 呼叫 API 進行狀態切換 - 移除不必要的參數
            const updatedEmployee = await employeeService.toggleEmployeeStatus(empId);
            
            // 更新本地資料
            const index = employees.value.findIndex(emp => emp.empId === empId);
            if (index !== -1) {
                employees.value[index] = updatedEmployee;
            }
            
        } catch (error) {
            error.value = error.message || '切換員工狀態時發生錯誤';
            console.error('切換員工狀態失敗:', error);
            throw error;
        } finally {
            isLoading.value = false;
        }
    };

    /**
     * 忘記密碼申請 - 新增方法
     * @param {number} empId - 員工 ID
     * @returns {Promise<void>} 發送重設密碼郵件請求
     */
    async function requestPasswordReset(email) {
        isLoading.value = true;
        error.value = null;
        
        try {
            await employeeService.requestPasswordReset(email);
        } catch (err) {
            error.value = err.message || `申請密碼重設時發生錯誤`;
            console.error('申請密碼重設失敗:', err);
            throw err;
        } finally {
            isLoading.value = false;
        }
    }

    /**
     * 驗證密碼重設驗證碼並重設密碼 - 新增方法
     * @param {string} email - 員工電子郵件
     * @param {Object} resetData - 重設資料
     * @param {string} resetData.code - 驗證碼
     * @param {string} resetData.newPassword - 新密碼
     * @returns {Promise<void>} 密碼重設完成
     */
    async function validateResetCode(email, resetData) {
        isLoading.value = true;
        error.value = null;
        
        try {
            await employeeService.validateResetCode(email, resetData);
        } catch (err) {
            error.value = err.message || `密碼重設時發生錯誤`;
            console.error('密碼重設失敗:', err);
            throw err;
        } finally {
            isLoading.value = false;
        }
    }

    /**
     * 直接修改密碼（不需要驗證碼）- 新增方法
     * @param {number} empId - 員工 ID
     * @param {string} newPassword - 新密碼
     * @returns {Promise<void>} 密碼修改完成
     */
    async function resetPassword(empId, newPassword) {
        isLoading.value = true;
        error.value = null;
        
        try {
            await employeeService.resetPassword(empId, newPassword);
        } catch (err) {
            error.value = err.message || `密碼修改時發生錯誤`;
            console.error('密碼修改失敗:', err);
            throw err;
        } finally {
            isLoading.value = false;
        }
    }

    /**
     * 分配角色給員工 - 已棄用，建議使用 updateEmployeeRoles
     * @deprecated 請使用 updateEmployeeRoles 方法
     */
    async function assignRole(empId, role) {
        console.warn('assignRole 方法已棄用，請使用 updateEmployeeRoles 方法');
        // 這裡可以保留向下相容性，或者直接移除
        throw new Error('assignRole 方法已棄用，請使用 updateEmployeeRoles 方法');
    }

    /**
     * 員工登入 - 修正參數，現在使用 Auth Store
     * @param {string} email - 員工電子郵件
     * @param {string} password - 密碼
     * @returns {Promise<Object>} 登入的員工資料
     */
    async function login(email, password) {
        isLoading.value = true;
        error.value = null;
        
        try {
            // 直接委託給 Auth Store 處理登入
            const { useAuthStore } = await import('@/stores/authStore');
            const authStore = useAuthStore();
            
            await authStore.login(email, password);
            
            // 登入成功後回傳當前員工資料
            return authStore.employee;
        } catch (err) {
            error.value = err.message || '登入時發生錯誤';
            console.error('員工登入失敗:', err);
            throw err;
        } finally {
            isLoading.value = false;
        }
    }

    // 如果設定了自動載入，則在元件掛載時獲取資料
    if (options.autoLoad) {
        onMounted(() => {
            fetchEmployees();
            fetchRoles(); // 同時獲取職位列表
        });
    }

    // 返回狀態和方法供元件使用
    return {
        // 狀態
        employees,
        roles,
        isLoading,
        error,
        queryParams,
        activeEmployeeCount,
        totalEmployeeCount,
        
        // 方法
        fetchEmployees,
        fetchRoles,
        searchEmployeesByName,
        filterByStatus,
        filterByRole,
        resetFilters,
        getEmployeeDetail,
        addEmployee,
        updateEmployee,
        updateEmployeeRoles, 
        toggleEmployeeStatus,
        requestPasswordReset,
        validateResetCode, 
        resetPassword, 
        login,
    };
}