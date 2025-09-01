// API 層：修正為正確的後端路徑
import axiosInstance from './axiosInstance';

// ✅ 根據您的 API 文件修正路徑
const ADMIN_BASE_URL = '/caregiver/admin';
const CAREGIVER_BASE_URL = '/caregiver';

/**
 * 預約訂單 API 模組 - 修正版本
 * 對應後端的 AdminAppointmentController 和 CaregiverAppointmentController
 */
export const appointmentApi = {
  /**
   * 獲取所有預約訂單 (管理員功能)
   * 對應：GET /api/caregiver/admin/appointments
   */
  getAllAppointments(params = {}) {
    console.log('📡 API 呼叫 - 取得所有預約訂單:', params);
    return axiosInstance.get(`${ADMIN_BASE_URL}/appointments`, { params });
  },

  /**
   * 獲取待審核預約 (管理員功能) 
   * 對應：GET /api/caregiver/admin/appointments/pending
   */
  getPendingAppointments() {
    console.log('📡 API 呼叫 - 取得待審核預約');
    return axiosInstance.get(`${ADMIN_BASE_URL}/appointments/pending`);
  },

  /**
   * 根據 ID 獲取預約詳情 (管理員功能)
   * 對應：GET /api/caregiver/admin/appointment/{id}
   */
  getAppointmentById(id) {
    console.log(`📡 API 呼叫 - 取得預約詳情: ID=${id}`);
    return axiosInstance.get(`${ADMIN_BASE_URL}/appointment/${id}`);
  },

  /**
   * 建立員工預約 (管理員功能)
   * 對應：POST /api/caregiver/admin/appointment/employee-create
   */
  createEmployeeAppointment(appointmentData) {
    console.log('📡 API 呼叫 - 建立員工預約:', appointmentData);
    return axiosInstance.post(`${ADMIN_BASE_URL}/appointment/employee-create`, appointmentData);
  },

  /**
   * 審核通過預約 (管理員功能)
   * 對應：PUT /api/caregiver/admin/appointment/{id}/approve
   */
  approveAppointment(id, notes = '') {
    console.log(`📡 API 呼叫 - 審核通過預約: ID=${id}`, { notes });
    return axiosInstance.put(`${ADMIN_BASE_URL}/appointment/${id}/approve`, { notes });
  },

  /**
   * 審核拒絕預約 (管理員功能)
   * 對應：PUT /api/caregiver/admin/appointment/{id}/reject
   */
  rejectAppointment(id, notes = '') {
    console.log(`📡 API 呼叫 - 審核拒絕預約: ID=${id}`, { notes });
    return axiosInstance.put(`${ADMIN_BASE_URL}/appointment/${id}/reject`, { notes });
  },

  /**
   * 完成預約 (管理員功能)
   * 對應：PUT /api/caregiver/admin/appointment/{id}/complete
   */
  completeAppointment(id) {
    console.log(`📡 API 呼叫 - 完成預約: ID=${id}`);
    return axiosInstance.put(`${ADMIN_BASE_URL}/appointment/${id}/complete`);
  },

  /**
   * 取消預約 (管理員功能)
   * 對應：PUT /api/caregiver/admin/appointment/{id}/cancel
   */
  cancelAppointment(id, reason = '') {
    console.log(`📡 API 呼叫 - 取消預約: ID=${id}`, { reason });
    return axiosInstance.put(`${ADMIN_BASE_URL}/appointment/${id}/cancel`, { reason });
  },

  /**
   * 批量更新預約狀態 (管理員功能)
   * 對應：PUT /api/caregiver/admin/appointments/batch-update
   */
  batchUpdateAppointments(ids, status) {
    console.log('📡 API 呼叫 - 批量更新預約:', { ids, status });
    return axiosInstance.put(`${ADMIN_BASE_URL}/appointments/batch-update`, { ids, status });
  },

  /**
   * 獲取預約統計 (管理員功能)
   * 對應：GET /api/caregiver/admin/appointments/statistics
   */
  getAppointmentStatistics() {
    console.log('📡 API 呼叫 - 取得預約統計');
    return axiosInstance.get(`${ADMIN_BASE_URL}/appointments/statistics`);
  },

  /**
   * 通用預約更新 (照服員功能)
   * 對應：PUT /api/caregiver/appointment/{id}
   */
  updateAppointment(id, updateData) {
    console.log(`📡 API 呼叫 - 更新預約: ID=${id}`, updateData);
    return axiosInstance.put(`${CAREGIVER_BASE_URL}/appointment/${id}`, updateData);
  },

  /**
   * 管理員更新預約 (管理員功能)
   * 對應：PUT /api/caregiver/admin/appointment/{id}/update
   */
  adminUpdateAppointment(id, updateData) {
    console.log(`📡 API 呼叫 - 管理員更新預約: ID=${id}`, updateData);
    return axiosInstance.put(`${ADMIN_BASE_URL}/appointment/${id}/update`, updateData);
  },

  /**
   * 通用預約查詢 (照服員功能)
   * 對應：GET /api/caregiver/appointments
   */
  getGeneralAppointments(params = {}) {
    console.log('📡 API 呼叫 - 通用預約查詢:', params);
    return axiosInstance.get(`${CAREGIVER_BASE_URL}/appointments`, { params });
  }
};