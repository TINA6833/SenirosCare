// src/api/roomTypeApi.js
// ================================
// 重點：import 一定要放最上面，避免初始化錯誤
import axiosInstance from './axiosInstance'; // 重點：只 import axiosInstance，不要 import 其他 API

// ================================
// 🏠 RoomType 基本 CRUD 操作
// ================================

// 只用 axiosInstance，不要在這裡 import 其他 API
export const roomTypeApi = {
  // 取得所有房型
  getAllRoomTypes() {
    return axiosInstance.get('/room-types');
  },

  /**
   * 根據 ID 取得房型
   * GET /api/room-types/{id}
   */
  getRoomTypeById(id) {
    return axiosInstance.get(`/room-types/${id}`);
  },

  /**
   * 新增房型（舊路徑，含圖片）
   * POST /api/room-types/RoomAdd
   */
  addRoomType(formData) {
    // 不要手動設 Content-Type
    return axiosInstance.post('/room-types/RoomAdd', formData);
  },

  /**
   * 完整更新房型（JSON）
   * PUT /api/room-types/{id}
   */
  updateRoomType(id, roomData) {
    return axiosInstance.put(`/room-types/${id}`, roomData);
  },

  /**
   * 部分更新房型（含圖片）
   * PATCH /api/room-types/{id} (multipart/form-data)
   */
  patchRoomTypeWithImage(id, formData) {
    return axiosInstance.patch(`/room-types/${id}`, formData);
  },

  /**
   * 部分更新房型（僅 JSON 欄位）
   * PATCH /api/room-types/{id} (application/json)
   */
  patchRoomType(id, data) {
    return axiosInstance.patch(`/room-types/${id}`, data);
  },

  /**
   * 刪除房型
   * DELETE /api/room-types/{id}
   */
  deleteRoomType(id) {
    return axiosInstance.delete(`/room-types/${id}`);
  },

  // ================================
  // 🔍 RoomType 查詢操作
  // ================================

  /**
   * 根據價格範圍查詢
   * GET /api/room-types/price-range?min={min}&max={max}
   */
  getByPriceRange(min, max) {
    return axiosInstance.get('/room-types/price-range', {
      params: { min, max }
    });
  },

  /**
   * 根據描述關鍵字搜尋（舊版）
   * GET /api/room-types/search/keyword?keyword={keyword}
   */
  getByDescription(keyword) {
    return axiosInstance.get('/room-types/search/keyword', {
      params: { keyword }
    });
  },

  /**
   * 根據容量查詢
   * GET /api/room-types/capacity?capacity={capacity}
   */
  getByCapacity(capacity) {
    return axiosInstance.get('/room-types/capacity', {
      params: { capacity }
    });
  },

  /**
   * 綜合搜尋（新版，功能最完整）
   * GET /api/room-types/search
   */
  searchRoomTypes(params = {}) {
    const {
      keyword = null,
      min = null,
      max = null,
      capacity = null,
      featureIds = null,
      match = 'any',
      sort = 'createdAt',
      order = 'desc',
      page = 1,
      size = 10
    } = params;

    let featureIdsStr = null;
    if (Array.isArray(featureIds)) {
      featureIdsStr = featureIds.join(',');
    } else if (typeof featureIds === 'string') {
      featureIdsStr = featureIds;
    }

    return axiosInstance.get('/room-types/search', {
      params: {
        keyword,
        min,
        max,
        capacity,
        featureIds: featureIdsStr,
        match,
        sort,
        order,
        page,
        size
      }
    });
  },

  // ================================
  // 📄 RoomType 分頁與統計
  // ================================

  /**
   * 分頁取得房型
   * GET /api/room-types/page?page={page}&size={size}
   */
  getRoomTypesWithPagination(page = 1, size = 10) {
    return axiosInstance.get('/room-types/page', {
      params: { page, size }
    });
  },

  /**
   * 取得房型總數
   * GET /api/room-types/count
   */
  getRoomTypeCount() {
    return axiosInstance.get('/room-types/count');
  },

  // ================================
  // 📁 CSV 匯入匯出
  // ================================

  /**
   * 匯出 CSV
   * GET /api/room-types/export
   */
  exportCSV() {
    return axiosInstance.get('/room-types/export', {
      responseType: 'blob',
      headers: {
        'Accept': 'text/csv'
      }
    });
  },

  /**
   * 匯入 CSV
   * POST /api/room-types/import
   */
  importCSV(formData) {
    return axiosInstance.post('/room-types/import', formData);
  },

  // ================================
  // 🏷️ RoomType-Feature 關聯操作
  // ================================

  /**
   * 新增單筆房型-特徵關聯
   * POST /api/room-type-features?roomTypeId={roomTypeId}&featureId={featureId}
   */
  addRoomTypeFeature(roomTypeId, featureId) {
    return axiosInstance.post('/room-type-features', null, {
      params: { roomTypeId, featureId }
    });
  },

  /**
   * 批量新增房型-特徵關聯
   * POST /api/room-type-features/{roomTypeId}/batch
   */
  batchAddRoomTypeFeatures(roomTypeId, featureIds) {
    return axiosInstance.post(`/room-type-features/${roomTypeId}/batch`, featureIds);
  },

  /**
   * 覆蓋房型的所有特徵（推薦使用）
   * PUT /api/room-type-features/{roomTypeId}
   */
  replaceRoomTypeFeatures(roomTypeId, featureIds) {
    return axiosInstance.put(`/room-type-features/${roomTypeId}`, featureIds);
  },

  /**
   * 刪除房型的所有特徵
   * DELETE /api/room-type-features/{roomTypeId}
   */
  deleteAllRoomTypeFeatures(roomTypeId) {
    return axiosInstance.delete(`/room-type-features/${roomTypeId}`);
  },

  /**
   * 刪除特定房型-特徵關聯
   * DELETE /api/room-type-features/{roomTypeId}/{featureId}
   */
  deleteRoomTypeFeature(roomTypeId, featureId) {
    return axiosInstance.delete(`/room-type-features/${roomTypeId}/${featureId}`);
  },

  /**
   * 取得房型的所有特徵 ID
   * GET /api/room-type-features/{roomTypeId}/features
   */
  getRoomTypeFeatures(roomTypeId) {
    return axiosInstance.get(`/room-type-features/${roomTypeId}/features`);
  },

  /**
   * 取得擁有特定特徵的房型 ID 列表
   * GET /api/room-type-features/feature/{featureId}/room-types
   */
  getRoomTypesByFeature(featureId) {
    return axiosInstance.get(`/room-type-features/feature/${featureId}/room-types`);
  },

  /**
   * 計算房型的特徵數量
   * GET /api/room-type-features/{roomTypeId}/count
   */
  countRoomTypeFeatures(roomTypeId) {
    return axiosInstance.get(`/room-type-features/${roomTypeId}/count`);
  },

  /**
   * 計算擁有特定特徵的房型數量
   * GET /api/room-type-features/feature/{featureId}/count
   */
  countRoomTypesByFeature(featureId) {
    return axiosInstance.get(`/room-type-features/feature/${featureId}/count`);
  },

  // ================================
  // 🛠️ 便利方法（組合多個 API）
  // ================================

  /**
   * 取得房型完整資訊（包含特徵）
   */
  async getRoomTypeWithFeatures(roomTypeId) {
    try {
      const [roomResponse, featuresResponse] = await Promise.all([
        this.getRoomTypeById(roomTypeId),
        this.getRoomTypeFeatures(roomTypeId)
      ]);
      return {
        ...roomResponse.data,
        features: featuresResponse.data
      };
    } catch (error) {
      throw error;
    }
  },

  /**
   * 新增房型並設定特徵
   */
  async createRoomTypeWithFeatures(roomData, featureIds = []) {
    try {
      // 1. 先新增房型
      const createResponse = await this.addRoomType(roomData);
      const newRoomTypeId = createResponse.data;

      // 2. 設定特徵（如果有的話）
      if (featureIds.length > 0) {
        await this.replaceRoomTypeFeatures(newRoomTypeId, featureIds);
      }

      return newRoomTypeId;
    } catch (error) {
      throw error;
    }
  },

  /**
   * 更新房型並同時更新特徵
   */
  async updateRoomTypeWithFeatures(roomTypeId, roomData, featureIds = []) {
    try {
      // 1. 更新房型資料
      const updateResponse = await this.patchRoomType(roomTypeId, roomData);

      // 2. 更新特徵關聯
      await this.replaceRoomTypeFeatures(roomTypeId, featureIds);

      return updateResponse.data;
    } catch (error) {
      throw error;
    }
  }
};

// ================================
// 🔧 錯誤處理輔助函數
// ================================
export const handleApiError = (error, defaultMessage = '操作失敗') => {
  if (error.response) {
    const status = error.response.status;
    const message = error.response.data?.message || error.response.data || defaultMessage;

    switch (status) {
      case 400:
        return `資料驗證失敗: ${message}`;
      case 401:
        return '未授權，請重新登入';
      case 403:
        return '沒有權限執行此操作';
      case 404:
        return '找不到指定的房型';
      case 409:
        return '資料衝突，請檢查房型名稱是否重複或關聯資料是否存在';
      case 500:
        return '伺服器內部錯誤，請聯絡管理員';
      default:
        return `錯誤 (${status}): ${message}`;
    }
  } else if (error.request) {
    return '網路連線失敗，請檢查網路狀態或伺服器是否啟動';
  } else {
    return error.message || defaultMessage;
  }
};

// 預設匯出
export default roomTypeApi;
