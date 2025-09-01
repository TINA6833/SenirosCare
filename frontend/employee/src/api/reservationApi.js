// src/api/reservationApi.js
import axiosInstance from './axiosInstance'; // 統一使用 axiosInstance
import { reservationService } from '@/services/reservationService';

export const reservationApi = {
  // ================================
  // 🏨 訂房基本 CRUD 操作
  // ================================

  /**
   * 取得所有訂房資料
   * GET /api/reservations
   */
  getAll() {
    return axiosInstance.get('/reservations');
  },

  /**
   * 根據 ID 取得訂房資料
   * GET /api/reservations/{id}
   */
  getById(id) {
    return axiosInstance.get(`/reservations/${id}`);
  },

  /**
   * 新增訂房資料
   * POST /api/reservations
   */
  create(data) {
    return axiosInstance.post('/reservations', data);
  },

  /**
   * 更新訂房資料
   * PUT /api/reservations/{id}
   */
  update(id, data) {
    return axiosInstance.put(`/reservations/${id}`, data);
  },

  /**
   * 刪除訂房資料
   * DELETE /api/reservations/{id}
   */
  delete(id) {
    return axiosInstance.delete(`/reservations/${id}`);
  },

  /**
   * 更新訂房狀態
   * PATCH /api/reservations/{id}/status
   */
  patchStatus(id, data) {
    return axiosInstance.patch(`/reservations/${id}/status`, data);
  },

  // ================================
  // 🔍 訂房查詢操作
  // ================================

  /**
   * 根據會員 ID 查詢訂房資料
   * GET /api/reservations/by-member
   */
  getByMember(memberId) {
    return axiosInstance.get('/reservations/by-member', { params: { memberId } });
  },

  /**
   * 根據房型 ID 查詢訂房資料
   * GET /api/reservations/by-roomtype
   */
  getByRoomType(roomTypeId) {
    return axiosInstance.get('/reservations/by-roomtype', { params: { roomTypeId } });
  },

  /**
   * 根據日期查詢訂房資料
   * GET /api/reservations/by-date
   */
  getByDate(date) {
    return axiosInstance.get('/reservations/by-date', { params: { date } });
  },

  /**
   * 根據日期範圍查詢訂房資料
   * GET /api/reservations/by-range
   */
  getByRange(from, to) {
    return axiosInstance.get('/reservations/by-range', { params: { from, to } });
  },

  /**
   * 根據狀態查詢訂房資料
   * GET /api/reservations/by-status
   */
  getByStatus(status) {
    return axiosInstance.get('/reservations/by-status', { params: { status } });
  },

  // ================================
  // 📊 訂房統計操作
  // ================================

  /**
   * 計算特定日期的訂房數量
   * GET /api/reservations/count/date
   */
  countOnDate(date) {
    return axiosInstance.get('/reservations/count/date', { params: { date } });
  },

  /**
   * 計算特定房型的訂房數量
   * GET /api/reservations/count/roomtype
   */
  countByRoomType(roomTypeId) {
    return axiosInstance.get('/reservations/count/roomtype', { params: { roomTypeId } });
  },

  // ================================
  // 📈 訂房趨勢與排行
  // ================================

  /**
   * 取得每日訂房趨勢
   * GET /api/reservations/analytics/daily
   */
  getDailyTrend(days = 30) {
    return axiosInstance.get('/reservations/analytics/daily', { params: { days } });
  },

  /**
   * 取得每月訂房趨勢
   * GET /api/reservations/analytics/monthly
   */
  getMonthlyTrend(months = 12) {
    return axiosInstance.get('/reservations/analytics/monthly', { params: { months } });
  },

  /**
   * 取得房型訂房數量排行
   * GET /api/reservations/analytics/ranking
   */
  getRoomTypeRanking(topN = 10) {
    return axiosInstance.get('/reservations/analytics/ranking', { params: { topN } });
  },

  // ================================
  // 📄 訂房詳細資料
  // ================================

  /**
   * 取得訂房詳細資料
   * GET /api/reservations/{id}/detail
   */
  getDetail(id) {
    return axiosInstance.get(`/${id}/detail`);
  },

  /**
   * 取得每月訂房統計資料
   * GET /api/reservations/analytics/monthly
   */
  getMonthlyReservationStatsByDate() {
    // 重點：統一使用 axiosInstance，確保攔截器與 header 一致
    return axiosInstance.get('/reservations/analytics/monthly');
  }
};

// ================================
// 🔧 錯誤處理輔助函數
// ================================

export const handleApiError = (error, defaultMessage = '操作失敗') => {
  if (error.response) {
    const status = error.response.status;
    const message = error.response.data?.message || error.response.data || defaultMessage;

    switch (status) {
      case 400:
        return `資料驗證失敗: ${message}`;
      case 401:
        return '未授權，請重新登入';
      case 403:
        return '沒有權限執行此操作';
      case 404:
        return '找不到指定的訂房資料';
      case 409:
        return '資料衝突，請檢查訂房資料是否重複或關聯資料是否存在';
      case 500:
        return '伺服器內部錯誤，請聯絡管理員';
      default:
        return `錯誤 (${status}): ${message}`;
    }
  } else if (error.request) {
    return '網路連線失敗，請檢查網路狀態或伺服器是否啟動';
  } else {
    return error.message || defaultMessage;
  }
};

// 預設匯出
export default reservationApi;
