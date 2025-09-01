import axiosInstance from './axiosInstance'

/**
 * 取得所有房型
 */
export const fetchAllRoomTypes = () => {
  return axiosInstance.get('/member/room-types')
}

/**
 * 取得單一房型
 * @param {number} id 房型ID
 */
export const fetchRoomTypeById = (id) => {
  return axiosInstance.get(`/member/room-types/${id}`)
}

/**
 * 依價格區間查詢房型
 * @param {number} min 最低價
 * @param {number} max 最高價
 */
export const fetchRoomTypesByPriceRange = (min, max) => {
  return axiosInstance.get('/member/room-types/price-range', {
    params: { min, max }
  })
}

/**
 * 依關鍵字查詢房型
 * @param {string} keyword 關鍵字
 */
export const fetchRoomTypesByKeyword = (keyword) => {
  return axiosInstance.get('/member/room-types/search/keyword', {
    params: { keyword }
  })
}

/**
 * 依容量查詢房型
 * @param {number} capacity 容量
 */
export const fetchRoomTypesByCapacity = (capacity) => {
  return axiosInstance.get('/member/room-types/capacity', {
    params: { capacity }
  })
}

/**
 * 分頁查詢房型
 * @param {number} page 頁碼
 * @param {number} size 每頁筆數
 */
export const fetchRoomTypesPaged = (page = 1, size = 10) => {
  return axiosInstance.get('/member/room-types/page', {
    params: { page, size }
  })
}

/**
 * 查詢房型總數
 */
export const fetchRoomTypeCount = () => {
  return axiosInstance.get('/member/room-types/count')
}

/**
 * 進階搜尋房型
 * @param {object} params 搜尋參數
 * 例如：{ keyword, min, max, capacity, featureIds, match, sort, order, page, size }
 */
export const searchRoomTypes = (params) => {
  return axiosInstance.get('/member/room-types/search', {
    params
  })
}