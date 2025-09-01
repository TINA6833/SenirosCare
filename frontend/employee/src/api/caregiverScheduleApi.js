// API 層：照服員班表相關的 HTTP 請求 
import axiosInstance from './axiosInstance';

/**
 * 照服員班表 API 模組 - 修正版本
 * 對應後端的 CaregiverAppointmentController 照服員專屬功能
 */
export const scheduleApi = {
  /**
   * 獲取照服員行程表 (照服員專屬功能)
   * 對應：GET /api/caregiver/{caregiverId}/schedule
   */
  getCaregiverSchedule(caregiverId, startDate, endDate) {
    console.log(`📡 API 呼叫 - 取得照服員班表: ID=${caregiverId}, ${startDate} ~ ${endDate}`);
    
    // 修正：將日期字串轉換為 LocalDateTime 格式
    const formatToLocalDateTime = (dateStr, isEndDate = false) => {
      const dateOnly = dateStr.split('T')[0];
      // 如果是結束日期，設定為當天的最後一秒
      return isEndDate ? `${dateOnly}T23:59:59` : `${dateOnly}T00:00:00`;
    };
    
    const params = {
      startDate: formatToLocalDateTime(startDate, false),
      endDate: formatToLocalDateTime(endDate, true)
    };
    
    console.log('📡 轉換後的參數:', params);
    
    return axiosInstance.get(`/caregiver/${caregiverId}/schedule`, { params });
  },

  /**
   * 獲取照服員預約列表 (照服員專屬功能)
   * 對應：GET /api/caregiver/{caregiverId}/appointments
   */
  getCaregiverAppointments(caregiverId, params = {}) {
    console.log(`📡 API 呼叫 - 取得照服員預約列表: ID=${caregiverId}`, params);
    return axiosInstance.get(`/caregiver/${caregiverId}/appointments`, { params });
  },

  /**
   * 檢查照服員時間衝突 (照服員專屬功能)
   * 對應：GET /api/caregiver/{caregiverId}/check-conflict
   * @param {number} caregiverId - 照服員 ID
   * @param {string} startTime - 開始時間 (ISO格式)
   * @param {string} endTime - 結束時間 (ISO格式)
   * @param {number|null} excludeId - 排除的預約 ID (編輯時使用)
   */
  checkTimeConflict(caregiverId, startTime, endTime, excludeId = null) {
    console.log(`📡 API 呼叫 - 檢查時間衝突: ID=${caregiverId}`, { startTime, endTime, excludeId });
    const params = {
      startTime,
      endTime
    };
    if (excludeId) {
      params.excludeId = excludeId;
    }
    return axiosInstance.get(`/caregiver/${caregiverId}/check-conflict`, { params });
  },

  /**
   * 獲取所有照服員列表 (用於班表選擇)
   * 對應：GET /api/caregiver
   */
  getAllCaregivers() {
    console.log('📡 API 呼叫 - 取得所有照服員列表');
    return axiosInstance.get('/caregiver');
  },

  /**
   * 獲取照服員基本資訊
   * 對應：GET /api/caregiver/{id}
   */
  getCaregiverInfo(caregiverId) {
    console.log(`📡 API 呼叫 - 取得照服員基本資訊: ID=${caregiverId}`);
    return axiosInstance.get(`/caregiver/${caregiverId}`);
  },

  /**
   * 獲取可用時間段
   * 對應：GET /api/caregiver/member/available-slots
   */
  getAvailableSlots(caregiverId, date, duration) {
    console.log(`📡 API 呼叫 - 獲取可用時間段: ID=${caregiverId}, 日期=${date}, 時長=${duration}`);
    const params = {
      caregiverId,
      date,
      duration
    };
    return axiosInstance.get('/caregiver/member/available-slots', { params });
  }
};