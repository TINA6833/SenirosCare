import {
  createOrder,
  fetchOrdersByMember,
  fetchOrderById
} from '@/api/order'

// 建立訂單（需帶 memberId）
export async function createNewOrder(cartId, addressId, paymentMethod) {
  const memberId = 1 // 固定會員1
  const { data } = await createOrder(memberId, cartId, addressId, paymentMethod)
  return data
}

// 查詢會員全部訂單
export async function getOrdersByMember() {
  const { data } = await fetchOrdersByMember()
  return data
}

// 查詢單筆訂單
export async function getOrderById(orderId) {
  const { data } = await fetchOrderById(orderId)
  return data
}