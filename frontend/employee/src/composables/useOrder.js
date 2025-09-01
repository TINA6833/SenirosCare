// Composable：集中管理訂單頁面的狀態與動作
import { ref } from 'vue'
import * as orderService from '@/services/orderService'

// 可用狀態值（來自後端 Model 註解）：PENDING, SHIPPED, CANCELLED, COMPLETED, RETURNED
// 可用付款狀態：PENDING, PAID, FAILED, REFUNDED
export const ORDER_STATUS = ['PENDING','SHIPPED','CANCELLED','COMPLETED','RETURNED']
export const PAYMENT_STATUS = ['PENDING','PAID','FAILED','REFUNDED']

export function useOrder() {
  const orders = ref([])         // 訂單列表
  const order = ref(null)        // 單筆訂單
  const loading = ref(false)     // 載入中
  const error = ref(null)        // 錯誤
  const toast = ref({ show: false, message: '', type: 'info' })

  // 取得訂單列表（memberId 與 status 擇一使用）
  async function loadOrders({ memberId = null, status = null } = {}) {
    loading.value = true
    error.value = null
    try {
      if (memberId != null && memberId !== '') {
        orders.value = await orderService.fetchOrdersByMember(memberId)
      } else {
        orders.value = await orderService.fetchOrders(status)
      }
    } catch (err) {
      error.value = err
      showToast(err.message || '載入訂單失敗', 'error')
    } finally {
      loading.value = false
    }
  }

  // 取得單筆詳情
  async function loadOrder(id) {
    loading.value = true
    error.value = null
    try {
      order.value = await orderService.fetchOrder(id)
      return order.value
    } catch (err) {
      error.value = err
      showToast(err.message || '取得訂單失敗', 'error')
      throw err
    } finally {
      loading.value = false
    }
  }

  // 建立訂單（由購物車轉訂單）
  // orderRequest: { cartId, addressId, paymentMethod }（totalAmount 由後端計算）
  async function create(orderRequest) {
    loading.value = true
    error.value = null
    try {
      const msg = await orderService.createOrder(orderRequest)
      showToast(msg || '建立訂單完成', 'success')
      return msg
    } catch (err) {
      error.value = err
      const msg = err?.response?.data || err.message || '建立訂單失敗'
      showToast(msg, 'error')
      throw err
    } finally {
      loading.value = false
    }
  }

  // 更新訂單出貨狀態
  async function updateOrderStatus(id, status) {
    console.log(`Composable 層 - 更新訂單狀態: ID=${id}, 狀態=${status}`);
    loading.value = true;
    error.value = null;
    
    try {
      // 直接傳遞參數，確保格式統一
      await orderService.updateOrderStatus(id, status);
      showToast(`訂單狀態已更新為 ${status}`, 'success');
      return true;
    } catch (err) {
      error.value = err;
      const errorMsg = err.response?.data || err.message || '更新訂單狀態失敗';
      showToast(errorMsg, 'error');
      throw err;
    } finally {
      loading.value = false;
    }
  }

  // 刪除訂單
  async function deleteOrder(id) {
    loading.value = true;
    error.value = null;
    try {
      await orderService.deleteOrder(id);
      showToast('訂單已成功作廢', 'success');
      return true;
    } catch (err) {
      error.value = err;
      showToast(err.message || '作廢訂單失敗', 'error');
      throw err;
    } finally {
      loading.value = false;
    }
  }

  // 更新付款狀態
  async function updatePaymentStatus(id, paymentData) {
    console.log(`Composable 層 - 更新付款狀態: ID=${id}`);
    console.log('付款資料:', paymentData);
    loading.value = true;
    error.value = null;
    
    try {
      await orderService.updatePaymentStatus(id, paymentData);
      showToast(`付款狀態已更新為 ${paymentData.paymentStatus}`, 'success');
      return true;
    } catch (err) {
      error.value = err;
      const errorMsg = err.response?.data || err.message || '更新付款狀態失敗';
      showToast(errorMsg, 'error');
      throw err;
    } finally {
      loading.value = false;
    }
  }

  // ===== 工具：金額／時間格式化（可依需求改）=====
  function formatAmount(n) {
    if (n == null) return '-'
    try {
      return Number(n).toLocaleString('zh-TW', { minimumFractionDigits: 0 })
    } catch { return String(n) }
  }

  function formatDateTime(dt) {
    if (!dt) return '-'
    // 若後端回 ISO 字串即可直接 new Date
    const d = new Date(dt)
    if (isNaN(d)) return String(dt)
    return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`
  }

  function showToast(message, type='info') {
    toast.value = { show: true, message, type }
    setTimeout(() => (toast.value.show = false), 2500)
  }

  // 小幫手：從訂單明細小計出總額（若後端還沒回 totalAmount 時使用）
  function calcTotal(items = []) {
    return items.reduce((sum, it) => sum + Number(it.unitPrice || 0) * Number(it.quantity || 0), 0)
  }

  return {
    // state
    orders, order, loading, error, toast,
    ORDER_STATUS, PAYMENT_STATUS,
    // actions
    loadOrders, loadOrder, create, updateOrderStatus, updatePaymentStatus, deleteOrder,
    // utils
    formatAmount, formatDateTime, calcTotal, showToast
  }
}
