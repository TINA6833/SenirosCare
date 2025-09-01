// 商品分類 Service，封裝業務邏輯
import { fetchDeviceCategories, fetchDeviceCategoryById } from '@/api/devicecategory'

// 取得所有分類
export async function getAllDeviceCategories() {
  const { data } = await fetchDeviceCategories()
  return data
}

// 取得單一分類
export async function getDeviceCategoryById(id) {
  const { data } = await fetchDeviceCategoryById(id)
  return data
}