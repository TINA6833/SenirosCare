import axiosInstance from './axiosInstance'; // **重點：引入我們建立的實例**

/**
 * 員工相關的 API 請求
 */
export const employeeApi = {
  /**
   * 員工登入
   * @param {Object} loginDto - 包含 email 和 password
   * @returns {Promise<Object>} Axios 回應
   */
  login(loginDto) {
    // **注意：這裡不需要手動加上 token，因為攔截器會處理**
    return axiosInstance.post('/auth/login', loginDto);
  },

  /**
   * 直接修改密碼（不需要驗證碼）
   * @param {number} empId - 員工 ID
   * @param {string} newPassword - 新密碼
   * @returns {Promise} 修改密碼的 Promise 物件
   */
  resetPassword(empId, newPassword) {
    return axiosInstance.put(`/employees/reset/${empId}`, newPassword, {
      headers: {
        'Content-Type': 'text/plain' // 指定內容類型為純文字
      }
    });
  },

  /**
   * 獲取所有員工資料
   * @returns {Promise<Object>} Axios 回應
   */
  getAllEmployees() {
    // **重點：使用 axiosInstance，它會自動帶上 Token**
    return axiosInstance.get('/employees');
  },

  /**
   * 取得員工列表
   * @param {Object} params - 查詢參數
   * @param {boolean} params.isActive - 是否啟用 (true: 啟用的員工, false: 停用的員工)
   * @param {string|null} params.empName - 員工姓名模糊查詢 (若為 null 則查詢全部)
   * @returns {Promise} 包含員工資料的 Promise 物件
   */
  getEmployees(params = { isActive: true, empName: null }) {
    // 直接返回 axios 請求結果，由上層處理成功/錯誤情況
    return axiosInstance.get('/employees', { params });
  },

  /**
   * 取得跟員工詳細資訊
   * @param {number} id - 員工 ID (empId)
   * @returns {Promise} 包含員工資料的 Promise 物件
   */
  getEmployeeById(id) {
    return axiosInstance.get(`/employees/${id}`);
  },

  /**
   * 取得所有職等列表
   * @returns {Promise} 包含所有職等的 Promise 物件
   */
  getAllRoles() {
    return axiosInstance.get('/employees/roles');
  },

  /**
   * 新增員工
   * @param {Object} employeeCreateDto - 員工建立資料
   * @returns {Promise} 包含新建員工資料的 Promise 物件
   */
  addEmployee(employeeCreateDto) {
    return axiosInstance.post('/employees', employeeCreateDto);
  },

  /**
   * 更新員工資料
   * @param {number} empId - 員工 ID
   * @param {Object} employeeCreateDto - 員工更新資料
   * @returns {Promise} 包含更新後員工資料的 Promise 物件
   */
  updateEmployee(empId, employeeUpdateDto) {
    return axiosInstance.put(`/employees/${empId}`, employeeUpdateDto);
  },

  /**
   * 重新賦予該員工權限
   * @param {number} empId - 員工 ID
   * @param {Array<number>} newRoleIds - 新的職等 ID 列表
   * @returns {Promise} 包含更新後權限列表的 Promise 物件
   */
  updateEmployeeRoles(empId, newRoleIds) {
    // 對 /employees/{empId}/roles 發送 PUT 請求，並在請求主體中攜帶新的職等 ID 列表
    return axiosInstance.put(`/employees/${empId}/roles`, newRoleIds);
  },
  

  /**
   * 切換員工啟用/停用狀態
   * @param {number} empId - 員工 ID
   * @returns {Promise} 包含更新後員工資料的 Promise 物件
   */
  toggleEmployeeStatus(empId) {
    return axiosInstance.put(`/employees/${empId}/status`);
  },

  /**
   * 申請密碼重設（發送驗證碼到信箱）
   * @param {string} email - 員工電子郵件
   * @returns {Promise<Object>} Axios 回應
   */
  requestReset(email) {
    // URL 編碼 email 地址以避免特殊字符問題
    const encodedEmail = encodeURIComponent(email);
    
    return axiosInstance.post(`/employees/pwdReset/${encodedEmail}`);
  },

  /**
   * 驗證重設密碼驗證碼並重設密碼
   * @param {string} email - 員工電子郵件
   * @param {Object} pwdResetDto - 重設密碼資料（包含 code 和 newPwd）
   * @param {string} pwdResetDto.code - 6位數驗證碼
   * @param {string} pwdResetDto.newPwd - 新密碼（注意：後端期望的變數名稱是 newPwd）
   * @returns {Promise<Object>} Axios 回應
   */
  validateResetCode(email, pwdResetDto) {
    // URL 編碼 email 地址以避免特殊字符問題
    const encodedEmail = encodeURIComponent(email);
    
    return axiosInstance.put(`/employees/pwdReset/${encodedEmail}`, pwdResetDto, {
      headers: {
        'Content-Type': 'application/json'
      }
    });
  }
};