import axiosInstance from './axiosInstance'; // **重點：引入我們建立的實例**

// **重點：統一裝置 API 路徑前綴**
const BASE_PATH = '/devices';

export const deviceApi = {
  // 取得所有裝置或依 categoryId 篩選
  getDevices(categoryId) {
    return axiosInstance.get(BASE_PATH, {
      params: categoryId != null ? { categoryId } : {}
    })
  },

  // 取得單一裝置
  getDevice(id) {
    return axiosInstance.get(`${BASE_PATH}/${id}`)
  },

  // 新增裝置
  createDevice(payload) {
    return axiosInstance.post(BASE_PATH, payload)
  },

  // 修改裝置 (PUT /api/devices?id=)
  updateDevice(id, payload) {
    return axiosInstance.put(BASE_PATH, payload, { params: { id } })
  },

  // 刪除裝置
  deleteDevice(id) {
    return axiosInstance.delete(`${BASE_PATH}/${id}`)
  },

  // 模糊搜尋
  searchDevices(keyword) {
    return axiosInstance.get(`${BASE_PATH}/search`, { params: { keyword } })
  },

  // 取得總數
  countDevices() {
    return axiosInstance.get(`${BASE_PATH}/count`)
  },

  // 排序查詢
  sortDevices(sortBy) {
    return axiosInstance.get(`${BASE_PATH}/sort`, { params: { sortBy } })
  },

  // 分頁查詢
  getPagedDevices(offset, limit) {
    return axiosInstance.get(`${BASE_PATH}/page`, { params: { offset, limit } })
  },

  // 分頁 + 排序
  getPagedSortedDevices(offset, limit, sortBy) {
    return axiosInstance.get(`${BASE_PATH}/page-sort`, { params: { offset, limit, sortBy } })
  },

  // 匯入 CSV
  importDevices(file) {
    const form = new FormData()
    form.append('file', file)
    return axiosInstance.post(`${BASE_PATH}/import`, form, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },

  // 匯出 CSV
  exportDevices() {
    return axiosInstance.get(`${BASE_PATH}/export`)
  },

  // 批次更新
  batchUpdateDevices(requests) {
    console.log('API 層 - 批次更新請求資料:', JSON.stringify(requests, null, 2));
    const validRequests = requests.map(req => {
      const id = typeof req.id === 'string' ? parseInt(req.id) : req.id;
      return {
        id,
        name: req.name,
        sku: req.sku,
        unitPrice: req.unitPrice,
        inventory: req.inventory,
        isOnline: req.isOnline,
        categoryId: req.categoryId,
        description: req.description || null,
        image: req.image || null,
        createdByEmpId: req.createdByEmpId || null
      };
    });
    return axiosInstance.put(`${BASE_PATH}/batch`, validRequests)
      .catch(error => {
        console.error('批次更新 API 錯誤:', error);
        if (error.response) {
          console.error('狀態碼:', error.response.status);
          console.error('回應資料:', error.response.data);
        }
        throw error;
      });
  },

  // 批次刪除
  batchDeleteDevices(ids) {
    return axiosInstance.delete(`${BASE_PATH}/batch`, { data: ids })
  },

  // 上傳裝置圖片
  uploadDeviceImage(id, file) {
    const form = new FormData()
    form.append('file', file)
    return axiosInstance.post(`${BASE_PATH}/${id}/upload-image`, form, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  }
}
