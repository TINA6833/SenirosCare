import axiosInstance from './axiosInstance'

/**
 * 取得所有房型特徵
 */
export const fetchAllFeatures = () => {
  // 取得全部特徵資料
  return axiosInstance.get('/room-types/member/features')
}

/**
 * 取得單一特徵
 * @param {number} id 特徵ID
 */
export const fetchFeatureById = (id) => {
  // 依ID查詢特徵
  return axiosInstance.get(`/room-types/member/features/${id}`)
}

/**
 * 關鍵字搜尋特徵
 * @param {string} keyword 關鍵字
 */
export const searchFeatures = (keyword) => {
  // 依名稱模糊搜尋
  return axiosInstance.get('/room-types/member/features/search', {
    params: { keyword }
  })
}

/**
 * 檢查特徵名稱是否存在
 * @param {string} name 特徵名稱
 */
export const existsFeatureName = (name) => {
  // 檢查名稱是否重複
  return axiosInstance.get('/room-types/member/features/exists', {
    params: { name }
  })
}

/**
 * 取得特徵總數
 */
export const fetchFeatureCount = () => {
  // 取得特徵數量
  return axiosInstance.get('/room-types/member/features/count')
}