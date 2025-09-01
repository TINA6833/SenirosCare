import {
  createOrGetCart,
  fetchCartItems,
  addCartItem,
  updateCartItem,
  removeCartItem,
  clearCart
} from '@/api/cart'

// 建立或取得購物車
export async function getOrCreateCart() {
  const { data } = await createOrGetCart()
  return data
}

// 取得購物車明細（轉型給前端用）
export async function getCartItems(cartId) {
  const { data } = await fetchCartItems(cartId)
  // 轉型：確保每個 item 都有 deviceId, deviceName, image, unitPrice, quantity
  return data.map(item => ({
    deviceId: item.device_id || item.deviceId || item.id,
    deviceName: item.device_name || item.deviceName || item.name || '',
    image: item.image || '',
    unitPrice: item.unit_price || item.unitPrice || item.price || 0,
    quantity: item.quantity || 1,
    price: item.unit_price || item.unitPrice || item.price || 0 // 兼容舊模板
  }))
}

// 加入商品
export async function addItemToCart(cartId, deviceId, quantity) {
  const { data } = await addCartItem(cartId, deviceId, quantity)
  return data
}

// 修改數量
export async function updateItemQuantity(cartId, deviceId, quantity) {
  const { data } = await updateCartItem(cartId, deviceId, quantity)
  return data
}

// 刪除商品
export async function removeItemFromCart(cartId, deviceId) {
  const { data } = await removeCartItem(cartId, deviceId)
  return data
}

// 清空購物車
export async function clearAllCart(cartId) {
  const { data } = await clearCart(cartId)
  return data
}