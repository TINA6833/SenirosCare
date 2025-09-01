// 重點註解：此 service 層負責封裝設施業務邏輯，型別轉換、資料結構處理，串接 src/api/facilityApi.js

import {
  fetchAllFacilities,
  fetchFacilityById,
  fetchAvailableFacilities,
  searchFacilities,
  fetchFacilitiesByAvailability,
  fetchPublicFacilities
} from '@/api/facilityApi'

/**
 * 取得所有設施（轉型別，依模板設計）
 */
export async function getAllFacilities() {
  const res = await fetchAllFacilities()
  return Array.isArray(res.data) ? res.data.map(mapFacility) : []
}

/**
 * 取得單一設施（依ID）
 * @param {number} id 設施ID
 */
export async function getFacilityById(id) {
  const res = await fetchFacilityById(id)
  return mapFacility(res.data)
}

/**
 * 取得可用設施
 */
export async function getAvailableFacilities() {
  const res = await fetchAvailableFacilities()
  return Array.isArray(res.data) ? res.data.map(mapFacility) : []
}

/**
 * 關鍵字搜尋設施
 * @param {string} keyword 關鍵字
 */
export async function searchFacilitiesService(keyword) {
  const res = await searchFacilities(keyword)
  return Array.isArray(res.data) ? res.data.map(mapFacility) : []
}

/**
 * 依可用狀態查詢設施
 * @param {boolean} isAvailable 是否可用
 */
export async function getFacilitiesByAvailability(isAvailable) {
  const res = await fetchFacilitiesByAvailability(isAvailable)
  return Array.isArray(res.data) ? res.data.map(mapFacility) : []
}

/**
 * 取得前台公開設施（可排序）
 * @param {string} sort 排序欄位
 * @param {string} order 排序方式
 */
export async function getPublicFacilities(sort = 'createdAt', order = 'desc') {
  const res = await fetchPublicFacilities(sort, order)
  return Array.isArray(res.data) ? res.data.map(mapFacility) : []
}

/**
 * 型別轉換：後端設施資料 => 前端設施物件
 * @param {object} item 後端設施資料
 * @returns {object} 前端設施物件
 */
function mapFacility(item) {
  // 重點註解：圖片路徑若不是 http 開頭，則自動補上主機網址
  let imageUrl = ''
  if (item.image_path) {
    imageUrl = item.image_path.startsWith('http')
      ? item.image_path
      : `http://localhost:8080/${item.image_path.startsWith('/') ? item.image_path.slice(1) : item.image_path}`
  }
  return {
    id: item.id,
    name: item.name,
    description: item.description,
    image: imageUrl, // 前端直接用 image 欄位顯示
    price: item.price,
    capacity: item.capacity,
    isAvailable: item.is_available,
    adminNote: item.admin_note,
    createdAt: item.created_at,
    updatedAt: item.updated_at,
    // 其他欄位請依資料表補充
  }
}