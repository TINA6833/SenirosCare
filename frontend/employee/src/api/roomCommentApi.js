// 重點：統一使用 axiosInstance，避免重複攔截器與設定
import axiosInstance from './axiosInstance'; // 只 import axiosInstance

/**
 * 員工 API 模組 - 只負責處理 HTTP 請求和回應
 * 根據 EmployeeController.java 中的端點實作對應的方法
 */
export const employeeApi = {
  /**
   * 取得員工列表
   * @param {Object} params - 查詢參數
   * @param {boolean} params.isActive - 是否啟用 (true: 啟用的員工, false: 停用的員工)
   * @param {string|null} params.empName - 員工姓名模糊查詢 (若為 null 則查詢全部)
   * @returns {Promise} 包含員工資料的 Promise 物件
   */
  getEmployees(params = { isActive: true, empName: null }) {
    // 直接返回 axios 請求結果，由上層處理成功/錯誤情況
    return axiosInstance.get('/employees', { params });
  },

  /**
   * 根據 ID 取得單一員工
   * @param {number} id - 員工 ID (empId)
   * @returns {Promise} 包含員工資料的 Promise 物件
   */
  getEmployeeById(id) {
    return axiosInstance.get(`/employees/${id}`);
  },

  /**
   * 取得所有職等列表
   * @returns {Promise} 包含所有職等的 Promise 物件
   */
  getAllRoles() {
    return axiosInstance.get('/employees/roles');
  },

  /**
   * 新增員工
   * @param {Object} employeeCreateDto - 員工建立資料
   * @returns {Promise} 包含新建員工資料的 Promise 物件
   */
  addEmployee(employeeCreateDto) {
    return axiosInstance.post('/employees', employeeCreateDto);
  },

  /**
   * 更新員工資料
   * @param {number} empId - 員工 ID
   * @param {Object} employeeCreateDto - 員工更新資料
   * @returns {Promise} 包含更新後員工資料的 Promise 物件
   */
  updateEmployee(empId, employeeCreateDto) {
    return axiosInstance.put(`/employees/${empId}`, employeeCreateDto);
  },

  /**
   * 切換員工啟用/停用狀態
   * @param {number} empId - 員工 ID
   * @returns {Promise} 包含更新後員工資料的 Promise 物件
   */
  toggleEmployeeStatus(empId) {
    return axiosInstance.put(`/employees/${empId}/status`);
  },

  /**
   * 員工登入
   * @param {Object} employeeLoginDto - 登入資訊
   * @param {string|number} employeeLoginDto.empId - 員工 ID
   * @param {string} employeeLoginDto.password - 員工密碼
   * @returns {Promise} 包含員工資料的 Promise 物件
   */
  login(employeeLoginDto) {
    return axiosInstance.post('/employees/login', employeeLoginDto);
  }
};

export const roomCommentApi = {
  // 新增留言
  create(comment) {
    return axiosInstance.post('/room-comments', comment);
  },

  // 取得全部留言
  getAll() {
    return axiosInstance.get('/room-comments');
  },

  // 取得單筆留言
  getById(id) {
    return axiosInstance.get(`/room-comments/${id}`);
  },

  // 依房型查詢留言（可選 approved）
  getByRoomType(roomTypeId, approved) {
    let url = `/room-comments/by-roomtype/${roomTypeId}`;
    if (approved !== undefined) url += `?approved=${approved}`;
    return axiosInstance.get(url);
  },

  // 依會員查詢留言（可選 approved）
  getByMember(memberId, approved) {
    let url = `/room-comments/by-member/${memberId}`;
    if (approved !== undefined) url += `?approved=${approved}`;
    return axiosInstance.get(url);
  },

  // 取得某房型最新通過留言
  getLatestApproved(roomTypeId, limit = 5) {
    return axiosInstance.get(`/room-comments/latest?roomTypeId=${roomTypeId}&limit=${limit}`);
  },

  // 分頁查詢（後台）
  getPage(roomTypeId, approved, page = 1, size = 10) {
    let url = `/room-comments/page?roomTypeId=${roomTypeId}&page=${page}&size=${size}`;
    if (approved !== undefined) url += `&approved=${approved}`;
    return axiosInstance.get(url);
  },

  // 計數（後台）
  getCount(roomTypeId, approved) {
    let url = `/room-comments/count?roomTypeId=${roomTypeId}`;
    if (approved !== undefined) url += `&approved=${approved}`;
    return axiosInstance.get(url);
  },

  // 審核通過
  approve(id) {
    return axiosInstance.patch(`/room-comments/${id}/approve`);
  },

  // 退回/不通過
  unapprove(id) {
    return axiosInstance.patch(`/room-comments/${id}/unapprove`);
  },

  // 修改內容
  updateContent(id, content) {
    return axiosInstance.patch(`/room-comments/${id}/content`, { content });
  },

  // 管理員回覆
  setReply(id, adminReply) {
    return axiosInstance.patch(`/room-comments/${id}/reply`, { adminReply });
  },

  // 刪除留言
  delete(id) {
    return axiosInstance.delete(`/room-comments/${id}`);
  }
};