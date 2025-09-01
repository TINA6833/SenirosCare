import axiosInstance from './axiosInstance';
const BASE_URL = '/addresses'; // 調整為相對路徑，避免硬編碼

// 查詢指定會員的所有收件地址
export function fetchAddressesByMember() {
  // 使用 axiosInstance 發送請求
  return axiosInstance.get(`${BASE_URL}/member`);
}