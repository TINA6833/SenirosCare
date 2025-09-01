// 重點註解：此 service 層封裝房型特徵業務邏輯，型別轉換、資料結構處理，串接 src/api/featureApi.js

import {
  fetchAllFeatures,
  fetchFeatureById,
  searchFeatures,
  existsFeatureName,
  fetchFeatureCount
} from '@/api/featureApi'

/**
 * 取得所有房型特徵（轉型別，依模板設計）
 */
export async function getAllFeatures() {
  const res = await fetchAllFeatures()
  return Array.isArray(res.data) ? res.data.map(mapFeature) : []
}

/**
 * 取得單一特徵（依ID）
 * @param {number} id 特徵ID
 */
export async function getFeatureById(id) {
  const res = await fetchFeatureById(id)
  return mapFeature(res.data)
}

/**
 * 關鍵字搜尋特徵
 * @param {string} keyword 關鍵字
 */
export async function searchFeaturesService(keyword) {
  const res = await searchFeatures(keyword)
  return Array.isArray(res.data) ? res.data.map(mapFeature) : []
}

/**
 * 檢查特徵名稱是否存在
 * @param {string} name 特徵名稱
 */
export async function isFeatureNameExists(name) {
  const res = await existsFeatureName(name)
  return !!res.data?.exists
}

/**
 * 取得特徵總數
 */
export async function getFeatureCount() {
  const res = await fetchFeatureCount()
  return res.data?.count || 0
}

/**
 * 型別轉換：後端特徵資料 => 前端特徵物件
 * @param {object} item 後端特徵資料
 * @returns {object} 前端特徵物件
 */
function mapFeature(item) {
  // 請依你的資料表欄位補充，以下為範例
  return {
    id: item.id,
    name: item.name,
    description: item.description,
    icon: item.icon,
    createdAt: item.createdAt,
    updatedAt: item.updatedAt,
    // 其他欄位請依資料表補充
  }
}