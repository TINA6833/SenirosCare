import axiosInstance from "./axiosInstance";

export const memberApi = {
  /**
   * **重點：取得所有會員資料 - 支援查詢參數**
   * @param {Object} queryParams - 查詢參數物件
   * @param {string} queryParams.memberName - 會員姓名 (模糊搜尋)
   * @param {string} queryParams.address - 地址 (模糊搜尋) 
   * @param {boolean} queryParams.gender - 性別 (true=男, false=女)
   * @param {boolean} queryParams.isActive - 會員狀態 (true=啟用, false=停權)
   * @returns {Promise<Array>} 會員資料陣列
   */
  getAllMembers(queryParams = {}) {
    // **重點：建構查詢參數，過濾掉空值**
    const params = {};
    
    if (queryParams.memberName !== undefined && queryParams.memberName !== '') {
      params.memberName = queryParams.memberName;
    }
    
    if (queryParams.address !== undefined && queryParams.address !== '') {
      params.address = queryParams.address;
    }
    
    if (queryParams.gender !== undefined && queryParams.gender !== null) {
      params.gender = queryParams.gender;
    }
    
    if (queryParams.isActive !== undefined && queryParams.isActive !== null) {
      params.isActive = queryParams.isActive;
    }

    console.log('[memberApi] 發送查詢參數:', params);
    
    return axiosInstance.get("/members", { params });
  },
  
  /**
   * 取得特定會員資料
   * @param {number} memberId - 會員 ID
   * @returns {Promise<Object>} 會員資料
   */
  getMemberById(memberId) {
    return axiosInstance.get(`/members/${memberId}`);
  },
  
  /**
   * **重點：切換會員狀態 (停權/啟用) - 使用 PUT 方法和 banReasonDto 結構**
   * @param {string} memberId - 會員ID
   * @param {Object} banReasonDto - 停權原因物件
   * @param {boolean} banReasonDto.isActive - 是否啟用 (true=啟用, false=停權)
   * @param {string} banReasonDto.banReason - 停權原因 (停權時必填)
   * @returns {Promise<Object>} 更新後的會員資料
   */
  updateMemberStatus(memberId, banReasonDto) {
    return axiosInstance.put(`/members/${memberId}/status`, banReasonDto);
  }
}