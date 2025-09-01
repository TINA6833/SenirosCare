import axiosInstance from './axiosInstance'

/**
 * 取得所有設施
 */
export const fetchAllFacilities = () => {
  // 取得全部設施資料
  return axiosInstance.get('/room-types/member/facilities')
}

/**
 * 取得單一設施
 * @param {number} id 設施ID
 */
export const fetchFacilityById = (id) => {
  // 依ID查詢設施
  return axiosInstance.get(`/room-types/member/facilities/${id}`)
}

/**
 * 取得可用設施
 */
export const fetchAvailableFacilities = () => {
  // 只取得可用設施
  return axiosInstance.get('/room-types/member/facilities/available')
}

/**
 * 關鍵字搜尋設施
 * @param {string} keyword 關鍵字
 */
export const searchFacilities = (keyword) => {
  // 依名稱模糊搜尋設施
  return axiosInstance.get('/room-types/member/facilities/search', {
    params: { keyword }
  })
}

/**
 * 依可用狀態查詢設施
 * @param {boolean} isAvailable 是否可用
 */
export const fetchFacilitiesByAvailability = (isAvailable) => {
  // 依可用狀態查詢設施
  return axiosInstance.get('/room-types/member/facilities/by-availability', {
    params: { isAvailable }
  })
}

/**
 * 取得前台公開設施（排序可選）
 * @param {string} sort 排序欄位（預設 createdAt）
 * @param {string} order 排序方式（預設 desc）
 */
export const fetchPublicFacilities = (sort = 'createdAt', order = 'desc') => {
  // 前台公開設施列表
  return axiosInstance.get('/room-types/member/facilities/public', {
    params: { sort, order }
  })
}