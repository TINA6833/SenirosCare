import axiosInstance from './axiosInstance'; // 改為使用 axiosInstance
const BASE_URL = '/orders'; // 調整為相對路徑，避免硬編碼

// 建立訂單（需帶 memberId）
export function createOrder(memberId, cartId, addressId, paymentMethod) {
  return axiosInstance.post(BASE_URL, {
    memberId,      // 必填
    cartId,
    addressId,
    paymentMethod
  });
}

// 查詢會員全部訂單
export function fetchOrdersByMember() {
  return axiosInstance.get(`${BASE_URL}`); // 使用 axiosInstance 發送請求
}

// 查詢單筆訂單
export function fetchOrderById(orderId) {
  return axiosInstance.get(`${BASE_URL}/${orderId}`); // 使用 axiosInstance 發送請求
}