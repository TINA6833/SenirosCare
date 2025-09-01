// 重點註解：此 service 層封裝房型-特徵關聯業務邏輯，型別轉換、資料結構處理，串接 src/api/roomTypeFeatureApi.js

import {
  addRoomTypeFeature,
  deleteRoomTypeFeaturesByRoomType,
  deleteRoomTypeFeature,
  fetchFeatureIdsByRoomType,
  fetchRoomTypeIdsByFeature,
  countFeaturesByRoomType,
  countRoomTypesByFeature,
  replaceAllFeaturesForRoomType,
  batchAddRoomTypeFeatures
} from '@/api/roomTypeFeatureApi'

/**
 * 新增單筆房型-特徵關聯
 * @param {number} roomTypeId 房型ID
 * @param {number} featureId 特徵ID
 */
export async function addRoomTypeFeatureService(roomTypeId, featureId) {
  const res = await addRoomTypeFeature(roomTypeId, featureId)
  return res.data
}

/**
 * 刪除某房型的所有特徵關聯
 * @param {number} roomTypeId 房型ID
 */
export async function removeAllFeaturesByRoomType(roomTypeId) {
  const res = await deleteRoomTypeFeaturesByRoomType(roomTypeId)
  return res.data
}

/**
 * 刪除單一房型-特徵關聯
 * @param {number} roomTypeId 房型ID
 * @param {number} featureId 特徵ID
 */
export async function removeRoomTypeFeature(roomTypeId, featureId) {
  const res = await deleteRoomTypeFeature(roomTypeId, featureId)
  return res.data
}

/**
 * 查詢某房型擁有的特徵 IDs
 * @param {number} roomTypeId 房型ID
 */
export async function getFeatureIdsByRoomType(roomTypeId) {
  const res = await fetchFeatureIdsByRoomType(roomTypeId)
  // 預期回傳陣列 [featureId, ...]
  return Array.isArray(res.data) ? res.data : []
}

/**
 * 查詢擁有某特徵的房型 IDs
 * @param {number} featureId 特徵ID
 */
export async function getRoomTypeIdsByFeature(featureId) {
  const res = await fetchRoomTypeIdsByFeature(featureId)
  // 預期回傳陣列 [roomTypeId, ...]
  return Array.isArray(res.data) ? res.data : []
}

/**
 * 查詢某房型特徵數量
 * @param {number} roomTypeId 房型ID
 */
export async function getFeatureCountByRoomType(roomTypeId) {
  const res = await countFeaturesByRoomType(roomTypeId)
  return res.data?.count || 0
}

/**
 * 查詢某特徵被多少房型擁有
 * @param {number} featureId 特徵ID
 */
export async function getRoomTypeCountByFeature(featureId) {
  const res = await countRoomTypesByFeature(featureId)
  return res.data?.count || 0
}

/**
 * 覆蓋某房型所有特徵（先清空再批量新增）
 * @param {number} roomTypeId 房型ID
 * @param {number[]} featureIds 特徵ID陣列
 */
export async function replaceAllFeaturesForRoomTypeService(roomTypeId, featureIds) {
  const res = await replaceAllFeaturesForRoomType(roomTypeId, featureIds)
  return res.data
}

/**
 * 批量新增房型特徵關聯
 * @param {number} roomTypeId 房型ID
 * @param {number[]} featureIds 特徵ID陣列
 */
export async function batchAddRoomTypeFeaturesService(roomTypeId, featureIds) {
  const res = await batchAddRoomTypeFeatures(roomTypeId, featureIds)
  return res.data
}