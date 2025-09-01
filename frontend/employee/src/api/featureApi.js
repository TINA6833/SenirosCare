// src/api/featureApi.js
// ================================
// 重點：統一使用 axiosInstance，避免重複攔截器與設定
import axiosInstance from './axiosInstance'; // 只 import axiosInstance

export const featureApi = {
  // ================================
  // 🏷️ Feature 基本 CRUD 操作
  // ================================

  /**
   * 取得所有特徵
   * GET /api/room-types/features
   */
  getAllFeatures() {
    // 重點：取得全部特徵
    return axiosInstance.get('/room-types/features');
  },

  /**
   * 根據 ID 取得單一特徵
   * GET /api/room-types/features/{id}
   */
  getFeatureById(id) {
    // 重點：取得指定特徵
    return axiosInstance.get(`/room-types/features/${id}`);
  },

  /**
   * 新增特徵
   * POST /api/room-types/features
   */
  addFeature(feature) {
    // 重點：新增特徵
    return axiosInstance.post('/room-types/features', feature);
  },

  /**
   * 批次新增
   * POST /api/room-types/features/batch
   */
  batchCreate(features) {
    // 重點：批次新增特徵
    return axiosInstance.post('/room-types/features/batch', features);
  },

  /**
   * 修改特徵
   * PUT /api/room-types/features/{id}
   */
  updateFeature(id, feature) {
    // 重點：更新特徵
    return axiosInstance.put(`/room-types/features/${id}`, feature);
  },

  /**
   * 刪除特徵
   * DELETE /api/room-types/features/{id}
   */
  deleteFeature(id) {
    // 重點：刪除特徵
    return axiosInstance.delete(`/room-types/features/${id}`);
  },

  /**
   * 搜尋
   * GET /api/room-types/features/search
   */
  searchFeatures(keyword) {
    // 重點：關鍵字搜尋特徵
    return axiosInstance.get(`/room-types/features/search?keyword=${encodeURIComponent(keyword)}`);
  },

  /**
   * 檢查名稱是否存在
   * GET /api/room-types/features/exists
   */
  exists(name) {
    // 重點：檢查特徵名稱是否存在
    return axiosInstance.get(`/room-types/features/exists?name=${encodeURIComponent(name)}`);
  },

  /**
   * 計數
   * GET /api/room-types/features/count
   */
  count() {
    // 重點：取得特徵總數
    return axiosInstance.get('/room-types/features/count');
  }
};

// 預設匯出
export default featureApi;
