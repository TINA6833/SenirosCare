// 重點註解：此 service 層封裝預約業務邏輯，型別轉換、資料結構處理，串接 src/api/reservationApi.js

import {
  createReservation,
  fetchReservationsByMember,
  fetchReservationsByRoomType,
  fetchReservationsByDate,
  fetchReservationsByRange,
  fetchReservationsByStatus,
  fetchDailyReservationAnalytics,
  fetchMonthlyReservationAnalytics,
  fetchReservationRanking,
  updateReservation,
  deleteReservation
} from '@/api/reservationApi'

/**
 * 新增預約
 * @param {Object} data 預約資料
 */
export async function addReservation(data) {
  const res = await createReservation(data)
  return mapReservation(res.data)
}

/**
 * 查詢會員預約
 * @param {number} memberId 會員ID
 */
export async function getReservationsByMember(memberId) {
  const res = await fetchReservationsByMember(memberId)
  return Array.isArray(res.data) ? res.data.map(mapReservation) : []
}

/**
 * 查詢房型預約
 * @param {number} roomTypeId 房型ID
 */
export async function getReservationsByRoomType(roomTypeId) {
  const res = await fetchReservationsByRoomType(roomTypeId)
  return Array.isArray(res.data) ? res.data.map(mapReservation) : []
}

/**
 * 查詢指定日期預約
 * @param {string} date 日期（YYYY-MM-DD）
 */
export async function getReservationsByDate(date) {
  const res = await fetchReservationsByDate(date)
  return Array.isArray(res.data) ? res.data.map(mapReservation) : []
}

/**
 * 查詢日期區間預約
 * @param {string} from 起始日期
 * @param {string} to 結束日期
 */
export async function getReservationsByRange(from, to) {
  const res = await fetchReservationsByRange(from, to)
  return Array.isArray(res.data) ? res.data.map(mapReservation) : []
}

/**
 * 依狀態查詢預約
 * @param {number|string} status 狀態值
 */
export async function getReservationsByStatus(status) {
  const res = await fetchReservationsByStatus(status)
  return Array.isArray(res.data) ? res.data.map(mapReservation) : []
}

/**
 * 每日預約統計（圖表用）
 */
export async function getDailyReservationAnalytics() {
  const res = await fetchDailyReservationAnalytics()
  return res.data
}

/**
 * 每月預約統計（圖表用）
 */
export async function getMonthlyReservationAnalytics() {
  const res = await fetchMonthlyReservationAnalytics()
  return res.data
}

/**
 * 房型預約排行（排行榜用）
 */
export async function getReservationRanking() {
  const res = await fetchReservationRanking()
  return Array.isArray(res.data) ? res.data : []
}

/**
 * 修改預約
 * @param {number} id 預約ID
 * @param {Object} data 修改資料
 */
export async function editReservation(id, data) {
  const res = await updateReservation(id, data)
  return mapReservation(res.data)
}

/**
 * 刪除預約
 * @param {number} id 預約ID
 */
export async function removeReservation(id) {
  const res = await deleteReservation(id)
  return res.data
}

/**
 * 型別轉換：後端預約資料 => 前端預約物件
 * @param {object} item 後端預約資料
 * @returns {object} 前端預約物件
 */
function mapReservation(item) {
  // 請依你的資料表欄位補充，以下為範例
  return {
    id: item.id,
    memberId: item.memberId,
    roomTypeId: item.roomTypeId,
    date: item.date,
    status: item.status,
    createdAt: item.createdAt,
    updatedAt: item.updatedAt,
    // 其他欄位請依資料表補充
  }
}