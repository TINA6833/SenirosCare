// Service 層：封裝業務邏輯和資料轉換
import { appointmentApi } from '@/api/caregiverAppointmentApi';
import { caregiverApi } from '@/api/caregiverApi';
import { caregiverService } from '@/services/caregiverService';

// 照服員快取，避免重複 API 請求
const caregiverCache = new Map();
let caregiverCacheInitialized = false;

/**
 * 初始化照服員快取 - 預先載入所有照服員資料
 */
const initializeCaregiverCache = async () => {
  if (caregiverCacheInitialized) return;
  
  try {
    const caregivers = await caregiverService.getCaregivers();
    
    caregivers.forEach(caregiver => {
      caregiverCache.set(caregiver.id, caregiver.chineseName);
    });
    
    caregiverCacheInitialized = true;
  } catch (error) {
    // 靜默處理快取初始化失敗，使用即時查詢
  }
};

/**
 * 根據照服員 ID 獲取中文名字
 * @param {number} caregiverId - 照服員 ID
 * @returns {Promise<string>} 照服員中文名字
 */
const getCaregiverChineseName = async (caregiverId) => {
  if (!caregiverId) return '未指定照服員';
  
  // 如果快取未初始化，先初始化
  if (!caregiverCacheInitialized) {
    await initializeCaregiverCache();
  }
  
  // 檢查快取
  if (caregiverCache.has(caregiverId)) {
    return caregiverCache.get(caregiverId);
  }
  
  try {
    // 先嘗試使用照服員服務
    try {
      const caregiverData = await caregiverService.getCaregiverById(caregiverId);
      if (caregiverData && caregiverData.chineseName) {
        caregiverCache.set(caregiverId, caregiverData.chineseName);
        return caregiverData.chineseName;
      }
    } catch (serviceError) {
      // 服務失敗時嘗試直接 API
    }
    
    // 如果服務失敗，直接使用 API
    const response = await caregiverApi.getCaregiverById(caregiverId);
    
    let chineseName = null;
    
    // 處理多種回應格式
    if (response.status === 200) {
      if (response.data) {
        // 格式 1: { success: true, data: { chineseName: ... } }
        if (response.data.success && response.data.data) {
          chineseName = response.data.data.chineseName;
        }
        // 格式 2: { chineseName: ... } (直接回應)
        else if (response.data.chineseName) {
          chineseName = response.data.chineseName;
        }
        // 格式 3: 嘗試其他可能的欄位名稱
        else {
          chineseName = response.data.chinese_name || 
                       response.data.name || 
                       response.data.fullName || 
                       response.data.displayName;
        }
      }
    }

    if (!chineseName) {
      chineseName = `照服員 ${caregiverId}`;
    }

    // 快取結果
    caregiverCache.set(caregiverId, chineseName);
    return chineseName;
  } catch (error) {
    const fallbackName = `照服員 ${caregiverId}`;
    caregiverCache.set(caregiverId, fallbackName);
    return fallbackName;
  }
};

/**
 * 獲取虛擬訂單類型的中文顯示
 * @param {string} blockType - 訂單類型
 * @returns {string} 中文顯示名稱
 */
const getBlockTypeDisplay = (blockType) => {
  const blockTypeMap = {
    'off-work': '下班時間',
    'break': '休息時間',
    'training': '訓練時間',
    'meeting': '會議時間',
    'maintenance': '維護時間',
    'personal': '個人時間',
    'leave': '請假'
  };
  
  return blockTypeMap[blockType] || blockType || '虛擬訂單';
};

/**
 * 預約訂單服務模組
 * 處理業務邏輯和資料格式轉換
 */
export const caregiverAppointmentService = {
  /**
   * 清除照服員快取
   */
  clearCaregiverCache() {
    caregiverCache.clear();
    caregiverCacheInitialized = false;
  },

  /**
   * 獲取預約訂單列表並轉換資料格式
   * @param {Object} filters - 篩選條件
   * @returns {Promise<Object>} 轉換後的訂單資料
   */
  async getAppointments(filters = {}) {
    try {
      // 準備 API 查詢參數
      const params = this._buildQueryParams(filters);

      // 呼叫 API
      const response = await appointmentApi.getAllAppointments(params);

      if (response.status === 200) {
        // 轉換資料格式 - 處理 appointmentWrapper 結構（並行處理）
        let appointments = await Promise.all(
          (response.data.appointments || []).map(async (appointmentWrapper) => {
            return await this._transformAppointmentData(appointmentWrapper);
          })
        );

        // 前端搜尋過濾
        if (filters.searchQuery && filters.searchQuery.trim()) {
          const searchQuery = filters.searchQuery.trim().toLowerCase();
          
          appointments = appointments.filter(appointment => {
            // 搜尋會員名稱
            const memberNameMatch = appointment.memberName && 
              appointment.memberName.toLowerCase().includes(searchQuery);
            
            // 搜尋照服員名稱
            const caregiverNameMatch = appointment.caregiverName && 
              appointment.caregiverName.toLowerCase().includes(searchQuery); 
            
            // 搜尋訂單編號
            const idMatch = appointment.id && 
              appointment.id.toString().includes(searchQuery);
            
            // 搜尋備註
            const notesMatch = appointment.notes && 
              appointment.notes.toLowerCase().includes(searchQuery);
            
            return memberNameMatch || caregiverNameMatch || idMatch || notesMatch;
          });
        }

        // 如果是搜尋模式，重新計算分頁資訊
        const originalSize = filters.size || 20;
        const currentPage = filters.page || 0;
        let paginatedAppointments = appointments;
        let total = appointments.length;
        let totalPages = Math.ceil(total / originalSize);

        // 如果有搜尋，進行前端分頁
        if (filters.searchQuery && filters.searchQuery.trim()) {
          const startIndex = currentPage * originalSize;
          const endIndex = startIndex + originalSize;
          paginatedAppointments = appointments.slice(startIndex, endIndex);
        }

        return {
          appointments: paginatedAppointments,
          total: total,
          totalPages: totalPages,
          currentPage: currentPage,
          statistics: response.data.statistics || {}
        };
      }

      throw new Error(`伺服器回應錯誤：${response.status}`);
    } catch (error) {
      throw this._handleError(error, '獲取預約訂單列表');
    }
  },

  /**
   * 根據 ID 獲取單一預約訂單
   * @param {number} id - 訂單 ID
   * @returns {Promise<Object>} 訂單詳情
   */
  async getAppointmentById(id) {
    try {
      console.log(`🔄 Service 層 - 獲取訂單詳情: ID=${id}`);
      const response = await appointmentApi.getAppointmentById(id);

      if (response.status === 200) {
        return await this._transformAppointmentData(response.data);
      }

      throw new Error(`伺服器回應錯誤：${response.status}`);
    } catch (error) {
      console.error(`❌ 獲取訂單詳情失敗 (ID: ${id}):`, error);
      throw this._handleError(error, `獲取訂單詳情 (ID: ${id})`);
    }
  },

 /**
 * 審核預約訂單 - 修正版本
 * @param {number} id - 訂單 ID
 * @param {string} action - 審核動作 ('approve' | 'reject')
 * @param {string} notes - 審核備註
 * @returns {Promise<boolean>} 是否成功
 */
async reviewAppointment(id, action, notes = '') {
  try {
    console.log(`🔄 Service 層 - 審核訂單: ID=${id}, 動作=${action}`);
    
    let response;
    if (action === 'approve') {
      // ✅ 使用正確的 API 端點：審核通過
      response = await appointmentApi.approveAppointment(id, notes);
    } else if (action === 'reject') {
      // ✅ 使用正確的 API 端點：審核拒絕  
      response = await appointmentApi.rejectAppointment(id, notes);
    } else {
      throw new Error('無效的審核動作，必須是 approve 或 reject');
    }
    
    return response.status === 200;
  } catch (error) {
    console.error(`❌ 審核訂單失敗 (ID: ${id}):`, error);
    throw this._handleError(error, `審核訂單 (ID: ${id})`);
  }
},

  /**
   * 完成預約訂單
   * @param {number} id - 訂單 ID
   * @returns {Promise<boolean>} 是否成功
   */
  async completeAppointment(id) {
    try {
      console.log(`🔄 Service 層 - 完成訂單: ID=${id}`);
      const response = await appointmentApi.completeAppointment(id);
      return response.status === 200;
    } catch (error) {
      console.error(`❌ 完成訂單失敗 (ID: ${id}):`, error);
      throw this._handleError(error, `完成訂單 (ID: ${id})`);
    }
  },

  /**
   * 取消預約訂單
   * @param {number} id - 訂單 ID
   * @param {string} reason - 取消原因
   * @returns {Promise<boolean>} 是否成功
   */
  async cancelAppointment(id, reason = '') {
    try {
      console.log(`🔄 Service 層 - 取消訂單: ID=${id}`);
      const response = await appointmentApi.cancelAppointment(id, reason);
      return response.status === 200;
    } catch (error) {
      console.error(`❌ 取消訂單失敗 (ID: ${id}):`, error);
      throw this._handleError(error, `取消訂單 (ID: ${id})`);
    }
  },

  /**
   * 更新預約訂單
   * @param {number} id - 訂單 ID
   * @param {Object} updateData - 更新資料
   * @returns {Promise<boolean>} 是否成功
   */
  async updateAppointment(id, updateData) {
    try {
      console.log(`🔄 Service 層 - 更新訂單: ID=${id}`, updateData);
      
      // 驗證必要欄位
      if (!updateData.caregiverId || !updateData.scheduledAt || !updateData.endTime) {
        throw new Error('照服員、開始時間和結束時間為必填欄位');
      }
      
      // 呼叫 API 更新
      const response = await appointmentApi.updateAppointment(id, updateData);
      
      if (response.status === 200) {
        console.log(`✅ 訂單更新成功: ID=${id}`);
        return response.data;
      }
      
      throw new Error(`伺服器回應錯誤：${response.status}`);
    } catch (error) {
      console.error(`❌ 更新訂單失敗 (ID: ${id}):`, error);
      throw this._handleError(error, `更新訂單 (ID: ${id})`);
    }
  },

  /**
   * 建構查詢參數
   * @private
   */
  _buildQueryParams(filters) {
    const params = {
      page: filters.page || 0,
      size: filters.size || 20
    };

    // 添加篩選條件
    if (filters.status) params.status = filters.status;
    if (filters.caregiverId) params.caregiverId = filters.caregiverId;
    if (filters.memberId) params.memberId = filters.memberId;
    if (filters.startDate) params.startDate = filters.startDate;
    if (filters.endDate) params.endDate = filters.endDate;
    if (filters.isBlocked !== undefined) params.isBlocked = filters.isBlocked;
    
    // 如果有搜尋關鍵字，暫時不傳送到後端，改用前端過濾
    // 因為後端可能不支援搜尋參數，我們在前端處理搜尋過濾
    if (filters.searchQuery && filters.searchQuery.trim()) {
      // 增加頁面大小以獲取更多資料進行搜尋
      params.size = 1000; // 暫時獲取大量資料用於搜尋
    }

    return params;
  },

  /**
   * 轉換後端資料格式為前端所需格式
   * @private
   */
  async _transformAppointmentData(appointmentWrapper) {
    // 處理 appointmentWrapper 結構
    if (!appointmentWrapper || !appointmentWrapper.appointment) {
      return {};
    }

    const appointment = appointmentWrapper.appointment;
    
    // 計算服務時長 (小時)
    const duration = this._calculateDuration(appointment.scheduledAt, appointment.endTime);

    // 格式化狀態顯示文字
    const statusDisplay = this._getStatusDisplay(appointment.status);

    // 格式化時間顯示
    const startTimeDisplay = this._formatDateTime(appointment.scheduledAt);
    const endTimeDisplay = this._formatDateTime(appointment.endTime);

    // 🔥 新邏輯：處理虛擬訂單的顯示
    let memberName, caregiverName;
    
    if (appointment.isBlocked) {
      // 虛擬訂單：會員名字顯示訂單類型，照服員名字顯示真實姓名
      memberName = this.getBlockTypeDisplay(appointment.blockType);
      caregiverName = await getCaregiverChineseName(appointment.caregiverId);
    } else {
      // 正常訂單：使用原有邏輯
      memberName = appointmentWrapper.creatorType === 'member' ? appointmentWrapper.creatorDisplayName : '未指定會員';
      caregiverName = appointmentWrapper.creatorType === 'employee' ? appointmentWrapper.creatorDisplayName : await getCaregiverChineseName(appointment.caregiverId);
    }

    return {
      id: appointment.id,
      memberId: appointment.memberId,
      memberName,
      caregiverId: appointment.caregiverId,
      caregiverName,
      scheduledAt: appointment.scheduledAt,
      endTime: appointment.endTime,
      startTimeDisplay,
      endTimeDisplay,
      duration,
      durationDisplay: `${duration} 小時`,
      status: appointment.status,
      statusDisplay,
      statusColor: this._getStatusColor(appointment.status),
      totalAmount: appointment.totalAmount || 0,
      totalAmountDisplay: appointment.totalAmount ? `$${appointment.totalAmount}` : '未設定',
      serviceLocation: appointment.serviceLocation || '未指定地點',
      serviceTypeId: appointment.serviceTypeId,
      notes: appointment.notes || '',
      isBlocked: appointment.isBlocked || false,
      blockType: appointment.blockType,
      createdAt: appointment.createdAt,
      updatedAt: appointment.updatedAt,
      // 額外的 wrapper 資訊
      creatorDisplayName: appointmentWrapper.creatorDisplayName || '',
      creatorType: appointmentWrapper.creatorType || 'unknown',
      isMemberAppointment: appointmentWrapper.isMemberAppointment || false,
      isEmployeeAppointment: appointmentWrapper.isEmployeeAppointment || false,
      // 可執行的操作 (根據狀態決定)
      availableActions: this._getAvailableActions(appointment.status, appointment.isBlocked)
    };
  },

  /**
   * 計算服務時長
   * @private
   */
  _calculateDuration(startTime, endTime) {
    if (!startTime || !endTime) return 0;
    
    const start = new Date(startTime);
    const end = new Date(endTime);
    const diffMs = end - start;
    const hours = Math.round(diffMs / (1000 * 60 * 60) * 10) / 10; // 四捨五入到小數點第一位
    
    return hours > 0 ? hours : 0;
  },

  /**
   * 格式化日期時間顯示
   * @private
   */
  _formatDateTime(dateTime) {
    if (!dateTime) return '未設定';
    
    return new Date(dateTime).toLocaleString('zh-TW', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    });
  },

  /**
   * 獲取狀態顯示文字
   * @private
   */
  _getStatusDisplay(status) {
    const statusMap = {
      'pending': '待審核',
      'approved': '已確認',
      'in_progress': '進行中',
      'completed': '已完成',
      'cancelled': '已取消',
      'rejected': '已拒絕'
    };
    
    return statusMap[status] || '未知狀態';
  },

  /**
   * 獲取狀態顏色
   * @private
   */
  _getStatusColor(status) {
    const colorMap = {
      'pending': 'warning',     // 待審核 - 黃色
      'approved': 'success',    // 已確認 - 綠色
      'in_progress': 'primary', // 進行中 - 藍色
      'completed': 'info',      // 已完成 - 淺藍色
      'cancelled': 'danger',    // 已取消 - 紅色
      'rejected': 'danger'      // 已拒絕 - 紅色
    };
    
    return colorMap[status] || 'secondary';
  },

  /**
   * 根據狀態獲取可執行的操作
   * @private
   */
  _getAvailableActions(status, isBlocked) {
    const actions = [];

    // 所有訂單都可以查看詳細
    actions.push('view');

    if (isBlocked) {
      // 員工建立的預約 (時間鎖定)
      actions.push('edit');
      if (status === 'approved') {
        actions.push('complete', 'cancel');
      }
    } else {
      // 會員建立的預約
      switch (status) {
        case 'pending':
          actions.push('review', 'edit', 'cancel');
          break;
        case 'approved':
          actions.push('complete', 'edit', 'cancel');
          break;
        case 'in_progress':
          actions.push('complete', 'cancel');
          break;
        case 'completed':
        case 'cancelled':
        case 'rejected':
          // 已結束的訂單只能查看
          break;
      }
    }

    return actions;
  },

  /**
   * 錯誤處理
   * @private
   */
  _handleError(error, action) {
    let errorMessage = `${action}失敗`;

    if (error.response) {
      const status = error.response.status;
      const data = error.response.data;
      
      if (status === 404) {
        errorMessage += ': 找不到指定的訂單';
      } else if (status === 403) {
        errorMessage += ': 權限不足';
      } else if (status === 400) {
        errorMessage += `: ${data?.message || '請求參數錯誤'}`;
      } else {
        errorMessage += `: HTTP ${status} - ${data?.message || '伺服器錯誤'}`;
      }
    } else if (error.request) {
      errorMessage += ': 無法連接到伺服器';
    } else {
      errorMessage += `: ${error.message || '未知錯誤'}`;
    }

    return new Error(errorMessage);
  },

  // ===== 預約表單處理工具函數 (原 appointmentHelper.js 功能) =====

  /**
   * 🔧 前端資料格式轉換函數
   * 將表單資料轉換為後端 API 預期的格式
   * @param {Object} formData - 表單資料
   * @returns {Object} 轉換後的 API 資料
   */
  formatAppointmentDataForAPI(formData) {
    return {
      caregiverId: parseInt(formData.caregiverId),              // 確保為數字類型
      scheduledAt: `${formData.startDate}T${formData.startTime}:00`,  // ISO 格式時間字串
      endTime: `${formData.endDate}T${formData.endTime}:00`,          // ISO 格式時間字串
      blockType: formData.blockType,                            // 字串
      notes: formData.notes?.trim() || `員工建立的${this.getBlockTypeDisplay(formData.blockType)}`
    };
  },

  /**
   * 🔧 鎖定類型顯示名稱對應
   * @param {string} blockType - 鎖定類型代碼
   * @returns {string} 顯示名稱
   */
  getBlockTypeDisplay(blockType) {
    const typeMap = {
      'off-work': '下班時間',
      'break': '休息時間',
      'training': '培訓時間',
      'leave': '請假',
      'unavailable': '不可用'
    };
    return typeMap[blockType] || '時間鎖定';
  },

  /**
   * 🔧 API 錯誤處理函數 (增強版)
   * @param {Error} error - 錯誤物件
   * @returns {string} 錯誤訊息
   */
  handleApiError(error) {
    console.error('API 錯誤詳情:', {
      status: error.response?.status,
      statusText: error.response?.statusText,
      data: error.response?.data,
      message: error.message,
      config: {
        url: error.config?.url,
        method: error.config?.method,
        data: error.config?.data
      }
    });
    
    // 🔍 根據不同的錯誤狀態碼回傳適當的錯誤訊息
    if (error.response) {
      switch (error.response.status) {
        case 400:
          return error.response.data?.message || '請求資料格式錯誤';
        case 401:
          return '未授權存取，請重新登入';
        case 403:
          return '權限不足，無法執行此操作';
        case 404:
          return '找不到對應的資源';
        case 409:
          return '資料衝突，請檢查輸入內容';
        case 422:
          return '資料驗證失敗，請檢查輸入格式';
        case 500:
          return '伺服器內部錯誤，請稍後再試';
        default:
          return error.response.data?.message || `伺服器錯誤 (${error.response.status})`;
      }
    } else if (error.request) {
      return '網路連線錯誤，請檢查網路狀態';
    } else {
      return error.message || '未知錯誤';
    }
  },

  /**
   * 🔧 驗證時間格式是否正確
   * @param {string} dateTime - 日期時間字串
   * @returns {boolean} 是否有效
   */
  isValidDateTime(dateTime) {
    const date = new Date(dateTime);
    return !isNaN(date.getTime());
  },

  /**
   * 🔧 計算兩個時間之間的小時數
   * @param {string} startDateTime - 開始時間
   * @param {string} endDateTime - 結束時間
   * @returns {number} 小時數
   */
  calculateHours(startDateTime, endDateTime) {
    try {
      const start = new Date(startDateTime);
      const end = new Date(endDateTime);
      
      if (isNaN(start.getTime()) || isNaN(end.getTime())) {
        return 0;
      }
      
      const diffMs = end - start;
      if (diffMs <= 0) {
        return 0;
      }
      
      return Math.round(diffMs / (1000 * 60 * 60) * 10) / 10;
    } catch (error) {
      console.error('計算時間錯誤:', error);
      return 0;
    }
  },

  /**
   * 🔧 格式化顯示時間
   * @param {string} dateTime - ISO 時間字串
   * @returns {string} 格式化後的時間
   */
  formatDisplayDateTime(dateTime) {
    try {
      const date = new Date(dateTime);
      return date.toLocaleString('zh-TW', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      });
    } catch (error) {
      return dateTime;
    }
  }
};