import axiosInstance from './axiosInstance'

/**
 * 新增單筆房型-特徵關聯
 * @param {number} roomTypeId 房型ID
 * @param {number} featureId 特徵ID
 */
export const addRoomTypeFeature = (roomTypeId, featureId) => {
  // 主要用於建立房型與特徵的關聯
  return axiosInstance.post('/member/room-type-features', null, {
    params: { roomTypeId, featureId }
  })
}

/**
 * 刪除某房型的所有特徵關聯
 * @param {number} roomTypeId 房型ID
 */
export const deleteRoomTypeFeaturesByRoomType = (roomTypeId) => {
  // 用於移除某房型所有特徵關聯
  return axiosInstance.delete(`/member/room-type-features/${roomTypeId}`)
}

/**
 * 刪除單一房型-特徵關聯
 * @param {number} roomTypeId 房型ID
 * @param {number} featureId 特徵ID
 */
export const deleteRoomTypeFeature = (roomTypeId, featureId) => {
  // 用於移除某房型的某個特徵關聯
  return axiosInstance.delete(`/member/room-type-features/${roomTypeId}/${featureId}`)
}

/**
 * 查詢某房型擁有的特徵 IDs
 * @param {number} roomTypeId 房型ID
 */
export const fetchFeatureIdsByRoomType = (roomTypeId) => {
  // 取得某房型所有特徵ID
  return axiosInstance.get(`/member/room-type-features/${roomTypeId}/features`)
}

/**
 * 查詢擁有某特徵的房型 IDs
 * @param {number} featureId 特徵ID
 */
export const fetchRoomTypeIdsByFeature = (featureId) => {
  // 取得擁有某特徵的所有房型ID
  return axiosInstance.get(`/member/room-type-features/feature/${featureId}/room-types`)
}

/**
 * 查詢某房型特徵數量
 * @param {number} roomTypeId 房型ID
 */
export const countFeaturesByRoomType = (roomTypeId) => {
  // 取得某房型擁有的特徵數量
  return axiosInstance.get(`/member/room-type-features/${roomTypeId}/count`)
}

/**
 * 查詢某特徵被多少房型擁有
 * @param {number} featureId 特徵ID
 */
export const countRoomTypesByFeature = (featureId) => {
  // 取得某特徵被多少房型擁有
  return axiosInstance.get(`/member/room-type-features/feature/${featureId}/count`)
}

/**
 * 覆蓋某房型所有特徵（先清空再批量新增）
 * @param {number} roomTypeId 房型ID
 * @param {number[]} featureIds 特徵ID陣列
 */
export const replaceAllFeaturesForRoomType = (roomTypeId, featureIds) => {
  // 批量覆蓋房型特徵，body 為特徵ID陣列
  return axiosInstance.put(`/member/room-type-features/${roomTypeId}`, featureIds)
}

/**
 * 批量新增房型特徵關聯
 * @param {number} roomTypeId 房型ID
 * @param {number[]} featureIds 特徵ID陣列
 */
export const batchAddRoomTypeFeatures = (roomTypeId, featureIds) => {
  // 批量新增房型特徵關聯，body 為特徵ID陣列
  return axiosInstance.post(`/member/room-type-features/${roomTypeId}/batch`, featureIds)
}