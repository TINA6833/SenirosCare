import axiosInstance from './axiosInstance'

/**
 * 照服員相關 API 呼叫
 * 負責與後端照服員相關的 HTTP 請求
 */
export const caregiverApi = {
  /**
   * 取得所有照服員列表
   * @returns {Promise} 照服員列表
   */
  getAllCaregivers() {
    return axiosInstance.get('/caregiver')
  },

  /**
   * 取得照服員詳細資料
   * @param {number} id - 照服員ID
   * @returns {Promise} 照服員詳細資料
   */
  getCaregiverById(id) {
    return axiosInstance.get(`/caregiver/${id}`)
  },

  /**
   * 根據服務區域查詢照服員
   * @param {string} area - 服務區域
   * @returns {Promise} 照服員列表
   */
  getCaregiversByArea(area) {
    return axiosInstance.get('/caregiver/service-area', {
      params: { area }
    })
  },

  /**
   * 根據姓名搜尋照服員
   * @param {string} name - 照服員姓名
   * @returns {Promise} 照服員列表
   */
  searchCaregiversByName(name) {
    return axiosInstance.get('/caregiver/search', {
      params: { name }
    })
  },

  /**
   * 取得照服員行程表（用於時間選擇）
   * @param {number} caregiverId - 照服員ID
   * @param {string} startDate - 開始日期時間 (ISO格式)
   * @param {string} endDate - 結束日期時間 (ISO格式)
   * @returns {Promise} 行程表資料
   */
  getCaregiverSchedule(caregiverId, startDate, endDate) {
    return axiosInstance.get(`/caregiver/${caregiverId}/schedule`, {
      params: { startDate, endDate }
    })
  },

  /**
   * 檢查時間可用性
   * @param {number} caregiverId - 照服員ID
   * @param {string} startTime - 開始時間 (ISO格式)
   * @param {string} endTime - 結束時間 (ISO格式)
   * @returns {Promise} 可用性檢查結果
   */
  checkAvailability(caregiverId, startTime, endTime) {
    return axiosInstance.get('/caregiver/member/check-availability', {
      params: { caregiverId, startTime, endTime }
    })
  },

  /**
   * 取得某日可用時間段
   * @param {number} caregiverId - 照服員ID
   * @param {string} date - 日期 (YYYY-MM-DD)
   * @returns {Promise} 可用時間段列表
   */
  getAvailableSlots(caregiverId, date) {
    return axiosInstance.get('/caregiver/member/available-slots', {
      params: { caregiverId, date }
    })
  },

  /**
   * 取得照服員的已評分預約列表（作為評論）
   * @param {number} caregiverId - 照服員ID
   * @param {number} limit - 限制筆數
   * @param {number} offset - 偏移量
   * @returns {Promise} 已評分預約列表
   */
  getCaregiverRatings(caregiverId, limit = 10, offset = 0) {
    return axiosInstance.get('/caregiver/appointments', {
      params: { 
        caregiverId,
        status: 'completed',
        isRated: true,
        limit,
        offset
      }
    })
  }
}
