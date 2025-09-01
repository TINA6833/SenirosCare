import axiosInstance from './axiosInstance'; // **重點：引入我們建立的實例**

// **重點：統一訂單 API 路徑前綴**
const BASE_PATH = '/orders';

export default {
  // 取得全部訂單（可選 status 篩選）
  listAll(status) {
    const params = {}
    if (status) params.status = status
    // **重點：加上 BASE_PATH，確保路徑正確**
    return axiosInstance.get(BASE_PATH, { params })
  },

  // 依會員取得訂單
  listByMember(memberId) {
    return axiosInstance.get(BASE_PATH, { params: { memberId } })
  },

  // 取得單筆訂單
  getOne(id) {
    return axiosInstance.get(`${BASE_PATH}/${id}`)
  },

  // 建立訂單（由購物車轉訂單）
  // 參數結構：{ cartId, addressId, paymentMethod }
  create(payload) {
    return axiosInstance.post(BASE_PATH, payload)
  },

  // 更新訂單狀態
  updateOrderStatus(id, status) {
    // 將 id 轉為字串並移除非數字字元
    const cleanId = String(id).replace(/[^0-9]/g, '');
    
    // 重要：嚴格按照後端 StatusUpdateRequest 格式包裝
    const payload = { status: status };
    console.log(`API 呼叫 - 更新訂單狀態 ID: ${cleanId}, 狀態: ${status}`);
    console.log('請求內容:', payload);
    
    return axiosInstance.patch(`${BASE_PATH}/${cleanId}/status`, payload);
  },

  // 更新付款狀態
  updatePaymentStatus(id, paymentData) {
    // 將 id 轉為字串並移除非數字字元
    const cleanId = String(id).replace(/[^0-9]/g, '');
    
    // 記錄傳送的資料內容，方便除錯
    console.log(`API 呼叫 - 更新付款狀態 ID: ${cleanId}`);
    console.log('請求內容:', paymentData);
    
    return axiosInstance.patch(`${BASE_PATH}/${cleanId}/payment-status`, paymentData);
  },

  // 刪除訂單（實際上可能是軟刪除/作廢）
  delete(id) {
    // 確保 id 參數格式正確
    const cleanId = String(id).replace(/[^0-9]/g, '');
    
    return axiosInstance.delete(`${BASE_PATH}/${cleanId}`);
  }
}
