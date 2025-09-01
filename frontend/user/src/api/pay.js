import axiosInstance from './axiosInstance'; // 改為使用 axiosInstance
const BASE_URL = '/pay'; // 調整為相對路徑，避免硬編碼

// 取得Line Pay付款網址
export function getLinePayUrl(orderId) {
  return axiosInstance.get(`${BASE_URL}/orders/${orderId}`); // 使用 axiosInstance 發送請求
}