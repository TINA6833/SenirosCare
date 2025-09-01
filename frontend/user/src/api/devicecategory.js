// 商品分類相關 API，負責 HTTP 請求
import axiosInstance from './axiosInstance'; // 改為使用 axiosInstance
const BASE_URL = '/device-categories'; // 調整為相對路徑，避免硬編碼

// 取得所有分類
export function fetchDeviceCategories() {
  return axiosInstance.get(BASE_URL); // 使用 axiosInstance 發送請求
}

// 取得單一分類
export function fetchDeviceCategoryById(id) {
  return axiosInstance.get(`${BASE_URL}/${id}`); // 使用 axiosInstance 發送請求
}