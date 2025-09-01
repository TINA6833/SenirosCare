// 商品 Service，封裝業務邏輯與型別轉換
import { fetchDevices, fetchDeviceById } from '@/api/device'

// 取得所有商品
export async function getAllDevices(params = {}) {
  const { data } = await fetchDevices(params)
  return data
}

// 取得單一商品
export async function getDeviceById(id) {
  const { data } = await fetchDeviceById(id)
  return data
}