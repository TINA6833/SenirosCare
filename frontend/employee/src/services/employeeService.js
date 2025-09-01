import { employeeApi } from '@/api/employeeApi';

/**
 * 格式化日期顯示為年月日
 * @param {string|Date} dateString - 日期字串或日期物件
 * @returns {string} 格式化後的日期字串
 */
const formatDate = (dateString) => {
  if (!dateString) return 'N/A';
  
  try {
    const date = new Date(dateString);
    // 只顯示年月日，不顯示時間
    return new Intl.DateTimeFormat('zh-TW', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit'
    }).format(date);
  } catch (e) {
    console.error('日期格式化錯誤:', e);
    return dateString;
  }
};

/**
 * 圖片基礎網址設定
 */
const IMAGE_BASE_URL = 'http://localhost:8080';

/**
 * 處理圖片網址，將相對路徑轉換為完整網址
 * @param {string} imagePath - 圖片相對路徑（例如：/images/xxx.png）
 * @returns {string} 完整的圖片網址或預設圖片
 */
const getImageUrl = (imagePath) => {
  // 如果沒有圖片路徑或為空字串，回傳預設圖片
  if (!imagePath || imagePath.trim() === '' || imagePath === '無圖片') {
    return '/src/assets/images/default-avatar.png'; // 或其他預設圖片路徑
  }
  
  // 如果已經是完整網址（包含 http），直接回傳
  if (imagePath.startsWith('http')) {
    return imagePath;
  }
  
  // 如果是相對路徑，加上基礎網址
  return `${IMAGE_BASE_URL}${imagePath}`;
};

/**
 * 員工服務模組 - 處理業務邏輯和資料轉換
 */
export const employeeService = {
  /**
   * 轉換員工資料格式，統一處理 EmployeeProfileDto
   * @param {Object} employee - 從後端 API 回傳的 EmployeeProfileDto 資料
   * @returns {Object} 轉換後的前端使用格式
   * @private
   */
  _transformEmployeeData(employee) {
    // 統一的資料轉換邏輯，對應 EmployeeProfileDto 結構
    return {
      empId: employee.empId,
      empName: employee.empName || '未命名',
      email: employee.email || 'N/A',
      roles: employee.roles || [], // EmpRole 列表，預設空陣列
      isActive: employee.isActive ? 'active' : 'inactive', // 轉換 Boolean 為字串
      imagePath: getImageUrl(employee.imagePath), // **重點：使用 getImageUrl 處理圖片網址**
      originalImagePath: employee.imagePath, // **重點：保留原始路徑供編輯時使用**
      describe: employee.describe || '無描述',
      createdAt: employee.createdAt ? formatDate(employee.createdAt) : '未知',
      updatedAt: employee.updatedAt ? formatDate(employee.updatedAt) : '未更新',
    };
  },

  /**
   * 獲取員工清單並進行資料轉換
   * @param {Object} params - 查詢參數
   * @param {boolean} params.isActive - 是否啟用 (true: 啟用的員工, false: 停用的員工)
   * @param {string|null} params.empName - 員工姓名模糊查詢 (若為 null 則查詢全部)
   * @param {string|null} params.role - 職位篩選 (若為 null 則查詢全部)
   * @param {number|null} params.limit - 限制回傳筆數
   * @param {number|null} params.offset - 偏移筆數 (分頁用)
   * @returns {Promise<Array>} 轉換後的員工資料陣列
   */
  async getEmployees(params = { isActive: true, empName: null }) {
    try {
      // 呼叫 API 層取得資料
      const response = await employeeApi.getEmployees(params);
      
      // 檢查回應是否成功
      if (response.status === 200) {
        // 使用統一的轉換方法處理 EmployeeProfileDto 陣列
        return response.data.map(employee => this._transformEmployeeData(employee));
      }
      
      // 如果回應不是 200，拋出錯誤
      throw new Error(`伺服器回應錯誤：${response.status}`);
    } catch (error) {
      // 整合錯誤訊息
      const errorMsg = error.response ? 
        `錯誤 ${error.response.status}: ${error.response.data?.message || '未知錯誤'}` : 
        error.message || '無法連接到伺服器';
      
      console.error('獲取員工資料失敗:', errorMsg);
      throw new Error(errorMsg);
    }
  },
    
  /**
   * 獲取單一員工詳細資料
   * @param {number|string} id 員工 ID
   * @returns {Promise<Object>} 員工詳細資料物件
   */
  async getEmployeeById(id) {
    try {
      const response = await employeeApi.getEmployeeById(id);
      
      // 檢查回應是否成功
      if (response.status === 200) {
        // 使用統一的轉換方法處理單一 EmployeeProfileDto
        return this._transformEmployeeData(response.data);
      }
      
      throw new Error(`伺服器回應錯誤：${response.status}`);
    } catch (error) {
      const errorMsg = error.response ? 
        `錯誤 ${error.response.status}: ${error.response.data?.message || '未知錯誤'}` : 
        error.message || '無法連接到伺服器';
      
      console.error('獲取員工詳細資料失敗:', errorMsg);
      throw new Error(errorMsg);
    }
  },

  /**
   * 獲取所有可用的職位列表
   * @returns {Promise<Array<Object>>} 職位列表陣列，每個物件包含 { role_id, role_name }
   */
  async getAllRoles() {
    try {
      const response = await employeeApi.getAllRoles();
      
      if (response.status === 200) {
        // 註解：直接回傳完整的 EmpRole 物件陣列，而不是只有名稱。
        // 這能讓前端同時獲得 role_id (用於提交) 和 role_name (用於顯示)。
        return response.data;
      }
      
      throw new Error(`伺服器回應錯誤：${response.status}`);
    } catch (error) {
      const errorMsg = error.response ? 
        `錯誤 ${error.response.status}: ${error.response.data?.message || '未知錯誤'}` : 
        error.message || '無法連接到伺服器';
      
      console.error('獲取職位列表失敗:', errorMsg);
      throw new Error(errorMsg);
    }
  },

  /**
   * 新增員工
   * @param {Object} employeeData - 員工資料
   * @param {string} employeeData.empName - 員工姓名（必填）
   * @param {string} employeeData.email - 員工電子郵件（必填）
   * @param {string} employeeData.password - 員工密碼（必填）
   * @param {string} employeeData.imagePath - 員工圖片路徑（選填）
   * @param {string} employeeData.describe - 員工描述（選填）
   * @returns {Promise<Object>} 新增後的員工資料
   */
  async addEmployee(employeeData) {
    try {
      console.log('提交員工資料:', employeeData);
      // 驗證必填欄位
      if (!employeeData.empName || !employeeData.email || !employeeData.password) {
        throw new Error('缺少必填欄位：員工姓名、電子郵件、密碼');
      }

      // 構建符合後端 EmployeeCreateDto 的物件
      const employeeCreateDto = {
        empName: employeeData.empName,
        email: employeeData.email,
        password: employeeData.password,
        imagePath: employeeData.imagePath || '',
        describe: employeeData.describe || ''
      };

      // 調用 API 新增員工
      const response = await employeeApi.addEmployee(employeeCreateDto);
      
      // 回傳新增的員工，使用統一轉換方法
      if (response.status === 201) {
        return this._transformEmployeeData(response.data);
      }
      
      throw new Error(`新增員工失敗：${response.status}`);
    } catch (error) {
      const errorMsg = error.response ? 
        `錯誤 ${error.response.status}: ${error.response.data?.message || '未知錯誤'}` : 
        error.message || '無法連接到伺服器';
      
      console.error('新增員工失敗:', errorMsg);
      throw new Error(errorMsg);
    }
  },

  /**
   * 更新員工資料
   * @param {number} empId - 員工 ID
   * @param {Object} employeeData - 更新的員工資料
   * @param {string} employeeData.empName - 員工姓名（必填）
   * @param {string} employeeData.email - 員工電子郵件（必填）
   * @param {string} employeeData.imagePath - 員工圖片路徑（選填）
   * @param {string} employeeData.describe - 員工描述（選填）
   * @returns {Promise<Object>} 更新後的員工資料
   */
  async updateEmployee(empId, employeeData) {
    try {
      // 驗證必填欄位
      if (!employeeData.empName || !employeeData.email || !employeeData.password) {
        throw new Error('缺少必填欄位：員工姓名、電子郵件');
      }

      // 構建符合後端 EmployeeUpdateDto 的物件  
      const employeeUpdateDto = {
        empName: employeeData.empName, // 員工名稱
        email: employeeData.email, // 電子郵件
        imagePath: employeeData.imagePath || '', // 圖片路徑
        describe: employeeData.describe || '', // 描述
      };

      // 調用 API 更新員工
      const response = await employeeApi.updateEmployee(empId, employeeUpdateDto);
      
      // 回傳修改的員工，使用統一轉換方法
      if (response.status === 200) {
        return this._transformEmployeeData(response.data);
      }
      
      throw new Error(`更新員工失敗：${response.status}`);
    } catch (error) {
      const errorMsg = error.response ? 
        `錯誤 ${error.response.status}: ${error.response.data?.message || '未知錯誤'}` : 
        error.message || '無法連接到伺服器';
      throw new Error(errorMsg);
    }
  },

  /**
   * 重新賦予員工權限
   * @param {number} empId - 員工 ID
   * @param {Array<number>} newRoleIds - 新的職等 ID 列表
   * @returns {Promise<Array<EmpRole>>} 更新後的權限列表
   */
  async updateEmployeeRoles(empId, newRoleIds) {
    try {
      // 驗證參數
      if (!empId || !Array.isArray(newRoleIds)) {
        throw new Error('參數錯誤：員工 ID 或職等 ID 列表格式不正確');
      }

      // 註解：【排查點 3】在 service 層確認最終要發送到 API 的資料
      console.log(`[employeeService.js] 準備呼叫 API。員工 ID: ${empId}, 本體 (Body):`, newRoleIds);

      // 調用 API 更新員工權限
      const response = await employeeApi.updateEmployeeRoles(empId, newRoleIds);
      
      if (response.status === 200) {
        // 直接回傳 EmpRole 列表，無需轉換
        return response.data;
      }
      
      throw new Error(`更新員工權限失敗：${response.status}`);
    } catch (error) {
      const errorMsg = error.response ? 
        `錯誤 ${error.response.status}: ${error.response.data?.message || '未知錯誤'}` : 
        error.message || '無法連接到伺服器';
      
      console.error(`更新員工 ID ${empId} 權限失敗:`, errorMsg);
      throw new Error(errorMsg);
    }
  },

  /**
   * 切換員工啟用/停用狀態
   * @param {number} empId - 員工 ID
   * @returns {Promise<Object>} 更新後的員工資料
   */
  async toggleEmployeeStatus(empId) {
    try {
      // 調用 API 切換員工狀態
      const response = await employeeApi.toggleEmployeeStatus(empId);
      
      // 回傳更新後的員工，使用統一轉換方法
      if (response.status === 200) {
        return this._transformEmployeeData(response.data);
      }
      
      throw new Error(`切換員工狀態失敗：${response.status}`);
    } catch (error) {
      const errorMsg = error.response ? 
        `錯誤 ${error.response.status}: ${error.response.data?.message || '未知錯誤'}` : 
        error.message || '無法連接到伺服器';
      
      console.error(`切換員工 ID ${empId} 狀態失敗:`, errorMsg);
      throw new Error(errorMsg);
    }
  },

  /**
   * 忘記密碼申請
   * @param {string} email - 員工電子郵件
   * @returns {Promise<void>} 發送重設密碼郵件請求
   */
  async requestPasswordReset(email) {
    try {
      // 驗證參數
      if (!email) {
        throw new Error('缺少必填欄位：員工電子郵件');
      }

      // 調用 API 申請密碼重設
      const response = await employeeApi.requestReset(email);
      
      if (response.status === 202) {
        // 請求已接受，郵件已發送
        return;
      }
      
      throw new Error(`申請密碼重設失敗：${response.status}`);
    } catch (error) {
      const errorMsg = error.response ? 
        `錯誤 ${error.response.status}: ${error.response.data?.message || '未知錯誤'}` : 
        error.message || '無法連接到伺服器';

      console.error(`員工電子郵件 ${email} 申請密碼重設失敗:`, errorMsg);
      throw new Error(errorMsg);
    }
  },

  /**
   * 驗證密碼重設驗證碼並重設密碼
   * @param {string} email - 員工電子郵件
   * @param {Object} resetData - 重設資料
   * @param {string} resetData.code - 驗證碼
   * @param {string} resetData.newPassword - 新密碼
   * @returns {Promise<void>} 密碼重設完成
   */
  async validateResetCode(email, resetData) {
    try {
        // 驗證參數
        if (!email || !resetData.code || !resetData.newPassword) {
            throw new Error('缺少必填欄位：員工電子郵件、驗證碼、新密碼');
        }

        // 構建 PwdResetDto 物件 - 修正：使用後端期望的變數名稱
        const pwdResetDto = {
            code: resetData.code,
            newPwd: resetData.newPassword  // 修正：改為 newPwd 以配合後端 DTO
        };

        console.log(`[employeeService.js] 準備驗證重設密碼，信箱: ${email}，DTO:`, pwdResetDto);

        // 調用 API 驗證並重設密碼
        const response = await employeeApi.validateResetCode(email, pwdResetDto);
        
        if (response.status === 202) {
            // 密碼重設成功
            console.log(`[employeeService.js] 員工信箱 ${email} 密碼重設成功`);
            return;
        }
        
        throw new Error(`密碼重設失敗：${response.status}`);
    } catch (error) {
        const errorMsg = error.response ? 
            `錯誤 ${error.response.status}: ${error.response.data?.message || error.response.data || '未知錯誤'}` : 
            error.message || '無法連接到伺服器';
        
        console.error(`員工電子郵件 ${email} 密碼重設失敗:`, errorMsg);
        throw new Error(errorMsg);
    }
  },

  /**
   * 員工登入
   * @param {string} email - 員工電子郵件
   * @param {string} password - 密碼
   * @returns {Promise<Object>} 登入成功後回傳的認證資料（包含 token 和員工資料）
   */
  async login(email, password) {
    try {
      // 驗證必填欄位
      if (!email || !password) {
        throw new Error('缺少必填欄位：電子郵件、密碼');
      }

      // 構建登入 DTO 物件
      const loginDto = {
        email,
        password
      };

      // 調用 API 登入
      const response = await employeeApi.login(loginDto);
      
      if (response.status === 200) {
        // 解構後端回傳的 AuthResponse（參考 AuthController.java）
        const { token, employeeProfileDto } = response.data;
        
        // 轉換員工資料格式
        const transformedEmployee = this._transformEmployeeData(employeeProfileDto);
        
        // 回傳包含 token 和轉換後員工資料的物件
        return {
          token: token,
          employeeProfileDto: transformedEmployee
        };
      }
      
      throw new Error(`登入失敗：${response.status}`);
    } catch (error) {
      const errorMsg = error.response ? 
        `錯誤 ${error.response.status}: ${error.response.data?.message || '未知錯誤'}` : 
        error.message || '無法連接到伺服器';
      
      console.error('員工登入失敗:', errorMsg);
      throw new Error(errorMsg);
    }
  },

    /**
   * 直接修改密碼（不需要驗證碼）
   * @param {number} empId - 員工 ID
   * @param {string} newPassword - 新密碼
   * @returns {Promise<void>} 密碼修改完成
   */
  async resetPassword(empId, newPassword) {
    try {
      // 驗證參數
      if (!empId || !newPassword) {
        throw new Error('缺少必填欄位：員工 ID、新密碼');
      }

      // 密碼長度驗證
      if (newPassword.length < 6) {
        throw new Error('密碼長度至少需要 6 個字元');
      }

      console.log(`[employeeService.js] 準備直接修改密碼。員工 ID: ${empId}`);

      // 調用 API 直接修改密碼
      const response = await employeeApi.resetPassword(empId, newPassword);
      
      if (response.status === 202) {
        // 密碼修改成功
        console.log(`[employeeService.js] 員工 ID ${empId} 密碼修改成功`);
        return;
      }
      
      throw new Error(`密碼修改失敗：${response.status}`);
    } catch (error) {
      const errorMsg = error.response ? 
        `錯誤 ${error.response.status}: ${error.response.data?.message || error.response.data || '未知錯誤'}` : 
        error.message || '無法連接到伺服器';
      
      console.error(`員工 ID ${empId} 密碼修改失敗:`, errorMsg);
      throw new Error(errorMsg);
    }
  },
};