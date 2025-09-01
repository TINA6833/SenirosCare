// Service 層：呼叫 api 並回傳 res.data，讓組件好用
import api from '@/api/order'

// 取全部（可帶 status）
export async function fetchOrders(status) {
  const res = await api.listAll(status)
  return res.data // 後端目前可能回 List<Order> 或 List<OrderResponse>
}

// 取某會員全部
export async function fetchOrdersByMember(memberId) {
  const res = await api.listByMember(memberId)
  return res.data
}

// 取單筆
export async function fetchOrder(id) {
  const res = await api.getOne(id)
  return res.data
}

// 建立訂單（回字串："購買成功" 或錯誤訊息）
export async function createOrder(orderRequest) {
  const res = await api.create(orderRequest)
  return res.data
}

// 更新訂單狀態
export async function updateOrder(updateData) {
  const res = await api.update(updateData)
  return res.data
}

// 刪除（作廢）訂單
export async function deleteOrder(id) {
  // 只傳 ID，不傳理由
  const res = await api.delete(id);
  return res.data;
}

// 更新訂單狀態 - 確保參數格式正確
export async function updateOrderStatus(id, status) {
  console.log(`Service 層 - 更新訂單狀態: ID=${id}, 狀態=${status}`);
  
  try {
    // 直接傳遞參數，讓 API 層處理包裝
    const res = await api.updateOrderStatus(id, status);
    console.log('訂單狀態更新成功');
    return res.data;
  } catch (error) {
    console.error('訂單狀態更新失敗:', error);
    throw error;
  }
}

// 更新付款狀態 - 保持參數結構一致
export async function updatePaymentStatus(id, paymentUpdateRequest) {
  console.log(`Service 層 - 更新付款狀態: ID=${id}`);
  console.log('付款資料:', paymentUpdateRequest);
  
  try {
    const res = await api.updatePaymentStatus(id, paymentUpdateRequest);
    console.log('付款狀態更新成功');
    return res.data;
  } catch (error) {
    console.error('付款狀態更新失敗:', error);
    throw error;
  }
}
