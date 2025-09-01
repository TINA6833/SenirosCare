import axiosInstance from "./axiosInstance";

/**
 * 服務類型相關API呼叫
 * 負責與後端服務類型API進行通訊
 */
export const serviceTypeApi = {
  /**
   * 取得所有啟用的服務類型
   * @returns {Promise<Object>} 服務類型列表
   */
  async getAll() {
    return axiosInstance.get('/caregiver/service-types');
  },

  /**
   * 取得服務類型下拉選單資料（精簡版）
   * @returns {Promise<Object>} 下拉選單用的服務類型資料
   */
  async getDropdown() {
    return axiosInstance.get('/caregiver/service-types/dropdown');
  },

  /**
   * 根據ID取得特定服務類型詳情
   * @param {number} serviceTypeId - 服務類型ID
   * @returns {Promise<Object>} 服務類型詳細資料
   */
  async getById(serviceTypeId) {
    return axiosInstance.get(`/caregiver/service-types/${serviceTypeId}`);
  },

  /**
   * 計算預約價格預覽
   * @param {number} serviceTypeId - 服務類型ID
   * @param {string} startTime - 開始時間 (YYYY-MM-DDTHH:mm:ss)
   * @param {string} endTime - 結束時間 (YYYY-MM-DDTHH:mm:ss)
   * @returns {Promise<Object>} 價格計算結果
   */
  async calculatePrice(serviceTypeId, startTime, endTime) {
    return axiosInstance.get(`/caregiver/service-types/${serviceTypeId}/calculate-price`, {
      params: {
        startTime,
        endTime
      }
    });
  }
};

export default serviceTypeApi;