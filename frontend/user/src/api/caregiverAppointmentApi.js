import axiosInstance from "./axiosInstance";

/**
 * 照服員預約相關API呼叫
 * 負責與後端照服員預約API進行通訊
 */
export const caregiverAppointmentApi = {
  
  // ========== 會員預約相關 ==========
  
  /**
   * 會員建立新預約
   * @param {Object} appointmentData - 預約資料
   * @param {number} appointmentData.caregiverId - 照服員ID
   * @param {string} appointmentData.scheduledAt - 開始時間 (YYYY-MM-DDTHH:mm:ss)
   * @param {string} appointmentData.endTime - 結束時間 (YYYY-MM-DDTHH:mm:ss)
   * @param {number} appointmentData.serviceTypeId - 服務類型ID
   * @param {string} appointmentData.serviceLocation - 服務地點
   * @param {string} appointmentData.notes - 備註
   * @returns {Promise<Object>} 建立結果
   */
  async createMemberAppointment(appointmentData) {
    return axiosInstance.post('/caregiver/member/appointment', appointmentData);
  },

  /**
   * 計算會員預約價格預覽
   * @param {number} serviceTypeId - 服務類型ID
   * @param {string} startTime - 開始時間 (YYYY-MM-DDTHH:mm:ss)
   * @param {string} endTime - 結束時間 (YYYY-MM-DDTHH:mm:ss)
   * @returns {Promise<Object>} 價格計算結果
   */
  async calculateMemberPrice(serviceTypeId, startTime, endTime) {
    return axiosInstance.get('/caregiver/member/calculate-price', {
      params: {
        serviceTypeId,
        startTime,
        endTime
      }
    });
  },

  /**
   * 檢查時間是否可預約
   * @param {number} caregiverId - 照服員ID
   * @param {string} startTime - 開始時間 (YYYY-MM-DDTHH:mm:ss)
   * @param {string} endTime - 結束時間 (YYYY-MM-DDTHH:mm:ss)
   * @returns {Promise<Object>} 可用性檢查結果
   */
  async checkAvailability(caregiverId, startTime, endTime) {
    return axiosInstance.get('/caregiver/member/check-availability', {
      params: {
        caregiverId,
        startTime,
        endTime
      }
    });
  },

  /**
   * 取得照服員可用時間段
   * @param {number} caregiverId - 照服員ID
   * @param {string} date - 查詢日期 (YYYY-MM-DD)
   * @returns {Promise<Object>} 可用時間段列表
   */
  async getAvailableSlots(caregiverId, date) {
    return axiosInstance.get('/caregiver/member/available-slots', {
      params: {
        caregiverId,
        date
      }
    });
  },

  /**
   * 取得會員的預約列表
   * @param {Object} params - 查詢參數
   * @param {string} params.status - 預約狀態（選填）
   * @param {number} params.page - 頁碼（選填，預設0）
   * @param {number} params.size - 每頁筆數（選填，預設10）
   * @returns {Promise<Object>} 會員預約列表
   */
  async getMemberAppointments(params = {}) {
    return axiosInstance.get('/caregiver/member/appointments', {
      params: {
        status: params.status,
        page: params.page || 0,
        size: params.size || 10
      }
    });
  },

  /**
   * 取得單一預約詳情
   * @param {number} appointmentId - 預約ID
   * @returns {Promise<Object>} 預約詳細資料
   */
  async getAppointmentDetail(appointmentId) {
    return axiosInstance.get(`/caregiver/member/appointment/${appointmentId}`);
  },

  /**
   * 取消預約
   * @param {number} appointmentId - 預約ID
   * @param {string} reason - 取消原因（選填）
   * @returns {Promise<Object>} 取消結果
   */
  async cancelAppointment(appointmentId, reason) {
    const payload = reason ? { reason } : {};
    return axiosInstance.post(`/caregiver/member/appointment/${appointmentId}/cancel`, payload);
  },

  /**
   * 評分已完成的預約
   * @param {number} appointmentId - 預約ID
   * @param {Object} ratingData - 評分資料
   * @param {number} ratingData.ratingScore - 評分（1-5）
   * @param {string} ratingData.ratingComment - 評論內容
   * @returns {Promise<Object>} 評分結果
   */
  async rateAppointment(appointmentId, ratingData) {
    return axiosInstance.post(`/caregiver/member/appointment/${appointmentId}/rating`, ratingData);
  },

  /**
   * 取得待評分的預約
   * @returns {Promise<Object>} 待評分預約列表
   */
  async getPendingRatings() {
    return axiosInstance.get('/caregiver/member/appointments/pending-ratings');
  },

  /**
   * 取得會員預約統計
   * @returns {Promise<Object>} 統計資料
   */
  async getMemberStatistics() {
    return axiosInstance.get('/caregiver/member/appointments/statistics');
  },

  // ========== 照服員行程相關 ==========

  /**
   * 取得照服員行程表
   * @param {number} caregiverId - 照服員ID
   * @param {string} startDate - 開始日期時間 (YYYY-MM-DDTHH:mm:ss)
   * @param {string} endDate - 結束日期時間 (YYYY-MM-DDTHH:mm:ss)
   * @returns {Promise<Object>} 照服員行程
   */
  async getCaregiverSchedule(caregiverId, startDate, endDate) {
    return axiosInstance.get(`/caregiver/${caregiverId}/schedule`, {
      params: {
        startDate,
        endDate
      }
    });
  },

  /**
   * 取得特定照服員的預約列表
   * @param {number} caregiverId - 照服員ID
   * @param {Object} params - 查詢參數
   * @returns {Promise<Object>} 照服員預約列表
   */
  async getCaregiverAppointments(caregiverId, params = {}) {
    return axiosInstance.get(`/caregiver/${caregiverId}/appointments`, {
      params: {
        status: params.status,
        isBlocked: params.isBlocked,
        startDate: params.startDate,
        endDate: params.endDate,
        limit: params.limit || 20,
        offset: params.offset || 0
      }
    });
  },

  /**
   * 檢查照服員時間衝突
   * @param {number} caregiverId - 照服員ID
   * @param {string} startTime - 開始時間 (YYYY-MM-DDTHH:mm:ss)
   * @param {string} endTime - 結束時間 (YYYY-MM-DDTHH:mm:ss)
   * @param {number} excludeId - 排除的預約ID（選填）
   * @returns {Promise<Object>} 時間衝突檢查結果
   */
  async checkTimeConflict(caregiverId, startTime, endTime, excludeId) {
    return axiosInstance.get(`/caregiver/${caregiverId}/check-conflict`, {
      params: {
        startTime,
        endTime,
        excludeId
      }
    });
  },

  // ========== 通用預約查詢 ==========

  /**
   * 通用預約查詢
   * @param {Object} params - 查詢參數
   * @returns {Promise<Object>} 預約列表
   */
  async getAppointments(params = {}) {
    return axiosInstance.get('/caregiver/appointments', {
      params: {
        memberId: params.memberId,
        caregiverId: params.caregiverId,
        status: params.status,
        isBlocked: params.isBlocked,
        startDate: params.startDate,
        endDate: params.endDate,
        isRated: params.isRated,
        limit: params.limit || 20,
        offset: params.offset || 0
      }
    });
  },

  /**
   * 更新預約資訊
   * @param {number} appointmentId - 預約ID
   * @param {Object} updateData - 更新資料
   * @returns {Promise<Object>} 更新結果
   */
  async updateAppointment(appointmentId, updateData) {
    return axiosInstance.put(`/caregiver/appointment/${appointmentId}`, updateData);
  }
};

export default caregiverAppointmentApi;