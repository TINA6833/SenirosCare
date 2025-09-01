import axiosInstance from './axiosInstance'; // **重點：引入我們建立的實例**

// **重點：統一分類 API 路徑前綴**
const BASE_PATH = '/device-categories';

export default {
  // 取得所有分類
  getCategories() {
    // **重點：加上 BASE_PATH，確保路徑正確**
    return axiosInstance.get(BASE_PATH)
  },

  // 取得單一分類
  getCategory(id) {
    return axiosInstance.get(`${BASE_PATH}/${id}`)
  },

  // 新增分類
  createCategory(payload) {
    return axiosInstance.post(BASE_PATH, payload)
  },

  // 更新分類
  updateCategory(id, payload) {
    return axiosInstance.put(`${BASE_PATH}/${id}`, payload)
  },

  // 刪除分類
  deleteCategory(id) {
    return axiosInstance.delete(`${BASE_PATH}/${id}`)
  },

  // 檢查分類是否存在
  existsCategory(id) {
    return axiosInstance.get(`${BASE_PATH}/exists/${id}`)
  },

  // 模糊搜尋分類名稱
  searchCategories(keyword) {
    return axiosInstance.get(`${BASE_PATH}/search`, { params: { keyword } })
  },

  // 查詢分類總數
  countCategories() {
    return axiosInstance.get(`${BASE_PATH}/count`)
  }
}