// 商品相關 API，負責 HTTP 請求
import axiosInstance from './axiosInstance'; // 改為使用 axiosInstance

// 設定後端 API 基本路徑
const BASE_URL = '/devices'; // 調整為相對路徑，避免硬編碼

// 取得所有商品（可帶分類查詢）
export function fetchDevices(params = {}) {
  return axiosInstance.get(BASE_URL, { params }); // 使用 axiosInstance 發送請求
}

// 取得單一商品
export function fetchDeviceById(id) {
  return axiosInstance.get(`${BASE_URL}/${id}`); // 使用 axiosInstance 發送請求
}