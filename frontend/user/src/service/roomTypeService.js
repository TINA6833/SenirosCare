// 重點註解：此 service 層負責封裝房型業務邏輯，將 image_path 補上主機網址，確保前端可正確顯示資料庫圖片

import {
  fetchAllRoomTypes,
  fetchRoomTypeById,
  fetchRoomTypesByPriceRange,
  fetchRoomTypesByKeyword,
  fetchRoomTypesByCapacity,
  fetchRoomTypesPaged,
  fetchRoomTypeCount,
  searchRoomTypes
} from '@/api/roomTypeApi'

/**
 * 取得所有房型（轉型別，預設排序）
 */
export async function getAllRoomTypes() {
  const res = await fetchAllRoomTypes()
  // 轉型別：可依資料表欄位補充
  return Array.isArray(res.data) ? res.data.map(mapRoomType) : []
}

/**
 * 取得單一房型（依ID）
 * @param {number} id 房型ID
 */
export async function getRoomTypeById(id) {
  const res = await fetchRoomTypeById(id)
  return mapRoomType(res.data)
}

/**
 * 依價格區間查詢房型
 * @param {number} min 最低價
 * @param {number} max 最高價
 */
export async function getRoomTypesByPriceRange(min, max) {
  const res = await fetchRoomTypesByPriceRange(min, max)
  return Array.isArray(res.data) ? res.data.map(mapRoomType) : []
}

/**
 * 依關鍵字查詢房型
 * @param {string} keyword 關鍵字
 */
export async function getRoomTypesByKeyword(keyword) {
  const res = await fetchRoomTypesByKeyword(keyword)
  return Array.isArray(res.data) ? res.data.map(mapRoomType) : []
}

/**
 * 依容量查詢房型
 * @param {number} capacity 容量
 */
export async function getRoomTypesByCapacity(capacity) {
  const res = await fetchRoomTypesByCapacity(capacity)
  return Array.isArray(res.data) ? res.data.map(mapRoomType) : []
}

/**
 * 分頁查詢房型
 * @param {number} page 頁碼
 * @param {number} size 每頁筆數
 */
export async function getRoomTypesPaged(page = 1, size = 10) {
  const res = await fetchRoomTypesPaged(page, size)
  // 若後端回傳 { content, total, ... } 可依資料表欄位補充
  return {
    list: Array.isArray(res.data?.content) ? res.data.content.map(mapRoomType) : [],
    total: res.data?.total || 0
  }
}

/**
 * 查詢房型總數
 */
export async function getRoomTypeCount() {
  const res = await fetchRoomTypeCount()
  return res.data?.count || 0
}

/**
 * 進階搜尋房型
 * @param {object} params 搜尋參數
 */
export async function searchRoomTypesService(params) {
  const res = await searchRoomTypes(params)
  return Array.isArray(res.data) ? res.data.map(mapRoomType) : []
}

/**
 * 型別轉換：後端房型資料 => 前端房型物件
 * @param {object} item 後端房型資料
 * @returns {object} 前端房型物件
 */
function mapRoomType(item) {
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
    image: imageUrl, // 前端直接用 image 欄位顯示
    price: item.price,
    capacity: item.capacity,
    description: item.description,
    features: item.features || [],
    status: item.status,
    isAvailable: item.is_available,
    adminNote: item.admin_note,
    createdAt: item.created_at,
    updatedAt: item.updated_at,
    // 其他欄位請依資料表補充
  }
}