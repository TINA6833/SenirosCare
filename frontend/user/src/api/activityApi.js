// src/api/activityApi.js
import axiosInstance from './axiosInstance'

/**
 * 查詢全部活動
 * @returns {Promise<Object>} 活動清單回應
 */
export function getAllActivities() {
  return axiosInstance.get('/activities')
}

/**
 * 根據ID查詢單筆活動
 * @param {number} id - 活動ID
 * @returns {Promise<Object>} 活動詳情回應
 */
export function getActivityById(id) {
  return axiosInstance.get(`/activities/${id}`)
}

/**
 * 預約活動
 * @param {Object} reservationData - 預約資料物件
 * @param {number} reservationData.activityId - 活動ID
 * @param {number} reservationData.num - 預約人數
 * @returns {Promise<Object>} 預約結果回應
 */
export function bookActivity(reservationData) {
  return axiosInstance.post('/activities/reservation', reservationData)
}

/**
 * 查詢我的預約清單
 * @returns {Promise<Object>} 我的預約清單回應
 */
export function getMyReservations() {
  return axiosInstance.get('/activities/reservation/me')
}

/**
 * 取消預約
 * @param {number} registrationId - 預約ID
 * @returns {Promise<Object>} 取消預約結果回應
 */
export function cancelReservation(activityId) {
  return axiosInstance.delete(`/activities/reservation/${activityId}`)
}

// [重點] 匯出所有 API 方法，方便統一管理
export default {
  getAllActivities,
  getActivityById,
  bookActivity,
  getMyReservations,
  cancelReservation
}
