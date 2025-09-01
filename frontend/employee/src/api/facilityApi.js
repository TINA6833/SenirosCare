// src/api/facilityApi.js
// ================================
// 重點：統一使用 axiosInstance，避免重複攔截器與設定
import axiosInstance from './axiosInstance'; // 只 import axiosInstance

export const facilityApi = {
  // ================================
  // 🏢 Facility 基本 CRUD 操作
  // ================================

  /**
   * 取得所有設施
   * GET /api/room-types/facilities
   */
  getAllFacilities() {
    return axiosInstance.get('/room-types/facilities');
  },

  /**
   * 根據 ID 取得單一設施
   * GET /api/room-types/facilities/{id}
   */
  getFacilityById(id) {
    return axiosInstance.get(`/room-types/facilities/${id}`);
  },

  /**
   * 新增設施（含圖片）
   * POST /api/room-types/facilities
   */
  addFacility(formData) {
    // multipart/form-data 由瀏覽器自動處理
    return axiosInstance.post('/room-types/facilities', formData);
  },

  /**
   * 更新設施（JSON）
   * PUT /api/room-types/facilities/{id}
   */
  updateFacility(id, facilityData) {
    return axiosInstance.put(`/room-types/facilities/${id}`, facilityData);
  },

  /**
   * 部分更新設施（含圖片）
   * PATCH /api/room-types/facilities/{id} (multipart/form-data)
   */
  patchFacilityWithImage(id, formData) {
    return axiosInstance.patch(`/room-types/facilities/${id}`, formData);
  },

  /**
   * 部分更新設施（僅 JSON 欄位）
   * PATCH /api/room-types/facilities/{id} (application/json)
   */
  patchFacility(id, data) {
    return axiosInstance.patch(`/room-types/facilities/${id}`, data);
  },

  /**
   * 刪除設施
   * DELETE /api/room-types/facilities/{id}
   */
  deleteFacility(id) {
    return axiosInstance.delete(`/room-types/facilities/${id}`);
  },

  // ================================
  // 📁 CSV 匯入匯出
  // ================================

  /**
   * 匯出 CSV
   * GET /api/room-types/facilities/export
   */
  exportCSV() {
    return axiosInstance.get('/room-types/facilities/export', {
      responseType: 'blob',
      headers: {
        'Accept': 'text/csv'
      }
    });
  },

  /**
   * 匯入 CSV
   * POST /api/room-types/facilities/import
   */
  importCSV(formData) {
    return axiosInstance.post('/room-types/facilities/import', formData);
  },
};

// 預設匯出
export default facilityApi;
