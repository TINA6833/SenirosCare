// 重點註解：所有 API 呼叫統一使用 axiosInstance，符合專案規範
import axiosInstance from './axiosInstance'

/* ========= 房型收藏 API ========= */

/**
 * 新增收藏
 * @param {number} memberId 會員ID
 * @param {number} roomTypeId 房型ID
 */
export const addFavorite = (memberId, roomTypeId) => {
  // 參數放在 params，符合 @RequestParam 設計
  return axiosInstance.post('/room-types/member/favorites', null, {
    params: { memberId, roomTypeId }
  })
}

/**
 * 批次新增收藏
 * @param {number} memberId 會員ID
 * @param {number[]} roomTypeIds 房型ID陣列
 */
export const batchAddFavorite = (memberId, roomTypeIds) => {
  // 以 body 傳遞房型ID陣列
  return axiosInstance.post('/room-types/member/favorites/batch', { memberId, roomTypeIds })
}

/**
 * 取消收藏
 * @param {number} memberId 會員ID
 * @param {number} roomTypeId 房型ID
 */
// 重點註解：取消收藏 API，參數放在 query string，符合後端設計
export const removeFavorite = (memberId, roomTypeId) => {
  return axiosInstance.delete('/room-types/member/favorites', {
    params: { memberId, roomTypeId }
  })
}

/**
 * 查詢某會員收藏清單
 * @param {number} memberId 會員ID
 * @returns {Promise<array>} 回傳該會員已收藏的房型資料陣列（可取得房型 id）
 */
// 重點註解：呼叫後端 API，取得指定會員的所有收藏房型資料
export const fetchFavoritesByMember = (memberId) => {
  return axiosInstance.get(`/room-types/member/favorites/${memberId}`)
  // 回傳格式範例：[{ id: 1, memberId: 1, roomTypeId: 2, ... }, ...]
}

/**
 * 判斷是否已收藏
 * @param {number} memberId 會員ID
 * @param {number} roomTypeId 房型ID
 */
export const existsFavorite = (memberId, roomTypeId) => {
  return axiosInstance.get('/room-types/member/favorites/exists', {
    params: { memberId, roomTypeId }
  })
}

/**
 * 查詢房型收藏數
 * @param {number} roomTypeId 房型ID
 */
export const countFavoriteByRoomType = (roomTypeId) => {
  return axiosInstance.get(`/room-types/member/favorites/count/roomtype/${roomTypeId}`)
}

/**
 * 查詢會員收藏數
 * @param {number} memberId 會員ID
 */
export const countFavoriteByMember = (memberId) => {
  return axiosInstance.get(`/room-types/member/favorites/count/member/${memberId}`)
}

/* ========= 收藏統計/排行 API ========= */

/**
 * 取得收藏排行 TopN
 * @param {number} limit 筆數
 * @param {string} keyword 關鍵字（可選）
 */
export const fetchFavoriteTop = (limit = 10, keyword = '') => {
  return axiosInstance.get('/room-types/member/favorites/stats/top', {
    params: { limit, keyword }
  })
}

/**
 * 收藏統計（支援搜尋、排序、分頁）
 * @param {object} params { keyword, order, page, size }
 */
export const fetchFavoriteStats = (params = {}) => {
  return axiosInstance.get('/room-types/member/favorites/stats', {
    params
  })
}

/**
 * 匯出收藏統計 CSV
 * @param {object} params { keyword, order }
 * @returns {Promise<Blob>} 回傳 CSV 檔案 blob
 */
export const exportFavoriteStatsCsv = (params = {}) => {
  // 下載 CSV 檔案，responseType 設為 blob
  return axiosInstance.get('/room-types/member/favorites/export', {
    params,
    responseType: 'blob'
  })
}