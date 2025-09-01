// 重點註解：此 service 層封裝房型收藏業務邏輯，型別轉換、資料結構處理，串接 src/api/favoriteApi.js

import {
  addFavorite,
  batchAddFavorite,
  removeFavorite,
  fetchFavoritesByMember,
  existsFavorite,
  countFavoriteByRoomType,
  countFavoriteByMember,
  fetchFavoriteTop,
  fetchFavoriteStats,
  exportFavoriteStatsCsv
} from '@/api/favoriteApi'

/**
 * 新增收藏（單筆）
 * @param {number} memberId 會員ID
 * @param {number} roomTypeId 房型ID
 * @returns {Promise<{success: boolean, message: string, data?: any}>}
 */
export async function addRoomTypeFavorite(memberId, roomTypeId) {
  try {
    const res = await addFavorite(memberId, roomTypeId)
    // 重點註解：回傳標準物件，包含成功狀態與訊息
    return {
      success: true,
      message: res.data || '收藏成功',
      data: res.data
    }
  } catch (err) {
    // 重點註解：失敗時回傳錯誤訊息
    return {
      success: false,
      message: err?.response?.data || '收藏失敗',
      error: err
    }
  }
}

/**
 * 批次新增收藏
 * @param {number} memberId 會員ID
 * @param {number[]} roomTypeIds 房型ID陣列
 * @returns {Promise<{success: boolean, message: string, data?: any}>}
 */
export async function batchAddRoomTypeFavorite(memberId, roomTypeIds) {
  try {
    const res = await batchAddFavorite(memberId, roomTypeIds)
    return {
      success: true,
      message: res.data || '批次收藏成功',
      data: res.data
    }
  } catch (err) {
    return {
      success: false,
      message: err?.response?.data || '批次收藏失敗',
      error: err
    }
  }
}

/**
 * 取消收藏
 * @param {number} memberId 會員ID
 * @param {number} roomTypeId 房型ID
 * @returns {Promise<{success: boolean, message: string, data?: any}>}
 */
export async function removeRoomTypeFavorite(memberId, roomTypeId) {
  try {
    // 重點註解：呼叫 API，參數放在 query string，符合後端設計
    const res = await removeFavorite(memberId, roomTypeId)
    return {
      success: true,
      message: res.data || '取消收藏成功',
      data: res.data
    }
  } catch (err) {
    return {
      success: false,
      message: err?.response?.data || '取消收藏失敗',
      error: err
    }
  }
}

/**
 * 查詢某會員收藏清單
 * @param {number} memberId 會員ID
 * @returns {Promise<{success: boolean, data: any[]}>}
 */
export async function getFavoritesByMember(memberId) {
  try {
    const res = await fetchFavoritesByMember(memberId)
    // 轉型別：依資料表欄位補充
    const data = Array.isArray(res.data) ? res.data.map(mapFavorite) : []
    return {
      success: true,
      data
    }
  } catch (err) {
    return {
      success: false,
      data: [],
      message: err?.response?.data || '取得收藏清單失敗',
      error: err
    }
  }
}

/**
 * 判斷是否已收藏
 * @param {number} memberId 會員ID
 * @param {number} roomTypeId 房型ID
 * @returns {Promise<{success: boolean, exists: boolean}>}
 */
export async function isRoomTypeFavorited(memberId, roomTypeId) {
  try {
    const res = await existsFavorite(memberId, roomTypeId)
    return {
      success: true,
      exists: !!res.data?.exists
    }
  } catch (err) {
    return {
      success: false,
      exists: false,
      message: err?.response?.data || '判斷失敗',
      error: err
    }
  }
}

/**
 * 查詢房型收藏數
 * @param {number} roomTypeId 房型ID
 * @returns {Promise<{success: boolean, count: number}>}
 */
export async function getFavoriteCountByRoomType(roomTypeId) {
  try {
    const res = await countFavoriteByRoomType(roomTypeId)
    return {
      success: true,
      count: res.data?.count || 0
    }
  } catch (err) {
    return {
      success: false,
      count: 0,
      message: err?.response?.data || '查詢失敗',
      error: err
    }
  }
}

/**
 * 查詢會員收藏數
 * @param {number} memberId 會員ID
 * @returns {Promise<{success: boolean, count: number}>}
 */
export async function getFavoriteCountByMember(memberId) {
  try {
    const res = await countFavoriteByMember(memberId)
    return {
      success: true,
      count: res.data?.count || 0
    }
  } catch (err) {
    return {
      success: false,
      count: 0,
      message: err?.response?.data || '查詢失敗',
      error: err
    }
  }
}

/**
 * 取得收藏排行 TopN
 * @param {number} limit 筆數
 * @param {string} keyword 關鍵字（可選）
 * @returns {Promise<{success: boolean, data: any[]}>}
 */
export async function getFavoriteTop(limit = 10, keyword = '') {
  try {
    const res = await fetchFavoriteTop(limit, keyword)
    const data = Array.isArray(res.data) ? res.data.map(mapFavoriteStats) : []
    return {
      success: true,
      data
    }
  } catch (err) {
    return {
      success: false,
      data: [],
      message: err?.response?.data || '取得排行失敗',
      error: err
    }
  }
}

/**
 * 收藏統計（支援搜尋、排序、分頁）
 * @param {object} params { keyword, order, page, size }
 * @returns {Promise<{success: boolean, data: any[]}>}
 */
export async function getFavoriteStats(params = {}) {
  try {
    const res = await fetchFavoriteStats(params)
    const data = Array.isArray(res.data) ? res.data.map(mapFavoriteStats) : []
    return {
      success: true,
      data
    }
  } catch (err) {
    return {
      success: false,
      data: [],
      message: err?.response?.data || '取得統計失敗',
      error: err
    }
  }
}

/**
 * 匯出收藏統計 CSV
 * @param {object} params { keyword, order }
 * @returns {Promise<{success: boolean, blob?: Blob}>}
 */
export async function exportFavoriteStats(params = {}) {
  try {
    const res = await exportFavoriteStatsCsv(params)
    return {
      success: true,
      blob: res.data
    }
  } catch (err) {
    return {
      success: false,
      blob: null,
      message: err?.response?.data || '匯出失敗',
      error: err
    }
  }
}

/**
 * 型別轉換：後端收藏資料 => 前端收藏物件
 * @param {object} item 後端收藏資料
 * @returns {object} 前端收藏物件
 */
function mapFavorite(item) {
  // 請依你的資料表欄位補充，以下為範例
  return {
    id: item.id,
    memberId: item.memberId,
    roomTypeId: item.roomTypeId,
    roomTypeName: item.roomTypeName,
    createdAt: item.createdAt,
    // 其他欄位請依資料表補充
  }
}

/**
 * 型別轉換：收藏統計資料
 * @param {object} item 後端統計資料
 * @returns {object} 前端統計物件
 */
function mapFavoriteStats(item) {
  return {
    roomTypeId: item.roomTypeId,
    roomTypeName: item.roomTypeName,
    favoriteCount: item.favoriteCount,
    rank: item.rank,
    // 其他欄位請依資料表補充
  }
}