import axiosInstance from './axiosInstance'; // 改為使用 axiosInstance
const BASE_URL = '/carts'; // 調整為相對路徑，避免硬編碼

export function createOrGetCart() {
  return axiosInstance.post(`${BASE_URL}`); // 使用 axiosInstance 發送請求
}

// 取得購物車明細
export function fetchCartItems(cartId) {
  return axiosInstance.get(`${BASE_URL}/${cartId}/items`); // 使用 axiosInstance 發送請求
}

// 加入商品到購物車
export function addCartItem(cartId, deviceId, quantity) {
  return axiosInstance.post(`${BASE_URL}/${cartId}/items`, {
    deviceId,
    quantity
  });
}

// 修改購物車商品數量
export function updateCartItem(cartId, deviceId, quantity) {
  return axiosInstance.put(`${BASE_URL}/${cartId}/items/${deviceId}`, {
    deviceId,
    quantity
  });
}

// 刪除購物車商品
export function removeCartItem(cartId, deviceId) {
  return axiosInstance.delete(`${BASE_URL}/${cartId}/items/${deviceId}`); // 使用 axiosInstance 發送請求
}

// 清空購物車
export function clearCart(cartId) {
  return axiosInstance.delete(`${BASE_URL}/${cartId}/items`); // 使用 axiosInstance 發送請求
}