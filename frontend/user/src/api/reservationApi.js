// 重點註解：所有 API 皆使用 axiosInstance，僅負責 HTTP，符合專案分層規範
import axiosInstance from './axiosInstance'

/**
 * 新增預約（會員功能）
 * @param {Object} data 預約資料（memberId, roomTypeId, date, ...）
 */
export const createReservation = (data) => {
  // POST /api/member/reservations
  return axiosInstance.post('/member/reservations', data)
}

/**
 * 查詢會員預約
 * @param {number} memberId 會員ID
 */
export const fetchReservationsByMember = (memberId) => {
  // GET /api/member/reservations/by-member?memberId=1
  return axiosInstance.get('/member/reservations/by-member', {
    params: { memberId }
  })
}

/**
 * 查詢房型預約
 * @param {number} roomTypeId 房型ID
 */
export const fetchReservationsByRoomType = (roomTypeId) => {
  // GET /api/member/reservations/by-roomtype?roomTypeId=1
  return axiosInstance.get('/member/reservations/by-roomtype', {
    params: { roomTypeId }
  })
}

/**
 * 查詢指定日期預約
 * @param {string} date 日期（格式：YYYY-MM-DD）
 */
export const fetchReservationsByDate = (date) => {
  // GET /api/member/reservations/by-date?date=2025-07-15
  return axiosInstance.get('/member/reservations/by-date', {
    params: { date }
  })
}

/**
 * 查詢日期區間預約
 * @param {string} from 起始日期
 * @param {string} to 結束日期
 */
export const fetchReservationsByRange = (from, to) => {
  // GET /api/member/reservations/by-range?from=2025-07-01&to=2025-07-31
  return axiosInstance.get('/member/reservations/by-range', {
    params: { from, to }
  })
}

/**
 * 依狀態查詢預約
 * @param {number|string} status 狀態值
 */
export const fetchReservationsByStatus = (status) => {
  // GET /api/member/reservations/by-status?status=1
  return axiosInstance.get('/member/reservations/by-status', {
    params: { status }
  })
}

/**
 * 每日預約統計（圖表用）
 */
export const fetchDailyReservationAnalytics = () => {
  // GET /api/member/reservations/analytics/daily
  return axiosInstance.get('/member/reservations/analytics/daily')
}

/**
 * 每月預約統計（圖表用）
 */
export const fetchMonthlyReservationAnalytics = () => {
  // GET /api/member/reservations/analytics/monthly
  return axiosInstance.get('/member/reservations/analytics/monthly')
}

/**
 * 房型預約排行（排行榜用）
 */
export const fetchReservationRanking = () => {
  // GET /api/member/reservations/analytics/ranking
  return axiosInstance.get('/member/reservations/analytics/ranking')
}

/**
 * 修改預約（僅限會員本人或管理員）
 * @param {number} id 預約ID
 * @param {Object} data 修改資料
 */
export const updateReservation = (id, data) => {
  // PUT /api/member/reservations/{id}
  return axiosInstance.put(`/member/reservations/${id}`, data)
}

/**
 * 刪除預約（僅限會員本人或管理員）
 * @param {number} id 預約ID
 */
export const deleteReservation = (id) => {
  // DELETE /api/member/reservations/{id}
  return axiosInstance.delete(`/member/reservations/${id}`)
}