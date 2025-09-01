import { getLinePayUrl } from '@/api/pay'

// 取得Line Pay付款網址
export async function fetchLinePayUrl(orderId) {
  const { data } = await getLinePayUrl(orderId)
  return data.paymentUrl
}