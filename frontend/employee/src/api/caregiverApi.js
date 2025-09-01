import axiosInstance from './axiosInstance';
// import axios from 'axios';

// // 設定 axios 實例的基礎 URL
// const apiClient = axios.create({
//   baseURL: 'http://localhost:8080/api', // 與員工 API 使用相同的基礎路徑
//   headers: {
//     'Content-Type': 'application/json'
//   }
// });

/**
 * 照服員 API 模組 - 只負責處理 HTTP 請求和回應
 * 根據 CaregiverController.java 中的端點實作對應的方法
 */
export const caregiverApi = {
  /**
   * 取得所有照服員列表
   * @returns {Promise} 包含照服員資料的 Promise 物件
   */
  getAllCaregivers() {
    // 直接返回 axios 請求結果，由上層處理成功/錯誤情況
    return axiosInstance.get('/caregiver');
  },

  /**
   * 根據 ID 取得單一照服員
   * @param {number} id - 照服員 ID (caregiverId)
   * @returns {Promise} 包含照服員資料的 Promise 物件
   */
  getCaregiverById(id) {
    return axiosInstance.get(`/caregiver/${id}`);
  },

  /**
   * 根據姓名搜尋照服員
   * @param {string} name - 照服員姓名
   * @returns {Promise} 包含搜尋結果的 Promise 物件
   */
  searchCaregivers(name) {
    return axiosInstance.get('/caregiver/search', { 
      params: { name } 
    });
  },

  /**
   * 根據狀態篩選照服員
   * @param {boolean} isActive - 是否啟用 (true: 在職照服員, false: 離職照服員)
   * @returns {Promise} 包含照服員資料的 Promise 物件
   */
  getCaregiversByStatus(isActive) {
    return axiosInstance.get(`/caregiver/status/${isActive}`);
  },

  /**
   * 根據服務區域搜尋照服員
   * @param {string} area - 服務區域
   * @returns {Promise} 包含搜尋結果的 Promise 物件
   */
  getCaregiversByServiceArea(area) {
    return axiosInstance.get('/caregiver/service-area', { 
      params: { area } 
    });
  },

  /**
   * 新增照服員
   * @param {Object} caregiverRequestDto - 照服員建立資料
   * @param {string} caregiverRequestDto.chineseName - 中文姓名
   * @param {boolean} caregiverRequestDto.gender - 性別 (true: 男性, false: 女性)
   * @param {string} caregiverRequestDto.phone - 連絡電話
   * @param {string} caregiverRequestDto.email - 電子信箱
   * @param {number} caregiverRequestDto.experienceYears - 服務年資
   * @param {string} caregiverRequestDto.photo - 照片路徑
   * @param {string} caregiverRequestDto.address - 居住地址
   * @param {string} caregiverRequestDto.serviceArea - 服務區域
   * @returns {Promise} 包含新建照服員資料的 Promise 物件
   */
  createCaregiver(caregiverRequestDto) {
    return axiosInstance.post('/caregiver', caregiverRequestDto);
  },

  /**
   * 更新照服員資料
   * @param {number} id - 照服員 ID
   * @param {Object} caregiverRequestDto - 照服員更新資料
   * @returns {Promise} 包含更新後照服員資料的 Promise 物件
   */
  updateCaregiver(id, caregiverRequestDto) {
    return axiosInstance.put(`/caregiver/${id}`, caregiverRequestDto);
  },

 /**
 * 刪除照服員
 * @param {number} id - 照服員 ID
 * @returns {Promise} 包含刪除結果的 Promise 物件
 */
deleteCaregiver(id) {
  return axiosInstance.delete(`/caregiver/${id}`);
},

  /**
   * 更新照服員評價
   * @param {number} id - 照服員 ID
   * @param {number} rating - 評價 (1-5分)
   * @returns {Promise} 包含更新結果的 Promise 物件
   */
  updateCaregiverRating(id, rating) {
    return axiosInstance.post(`/caregiver/${id}/rating`, null, {
      params: { rating }
    });
  },

  /**
   * 檢查電話號碼是否已存在
   * @param {string} phone - 電話號碼
   * @param {number|null} excludeId - 排除的照服員 ID (更新時使用)
   * @returns {Promise} 包含檢查結果的 Promise 物件
   */
  checkPhoneExists(phone, excludeId = null) {
    const params = { phone };
    if (excludeId) {
      params.excludeId = excludeId;
    }
    return axiosInstance.get('/caregiver/check-phone', { params });
  },

  /**
   * 上傳照服員照片
   * @param {number} id - 照服員 ID
   * @param {File} file - 圖片檔案
   * @returns {Promise} 包含上傳結果的 Promise 物件
   */
  uploadPhoto(id, file) {
    console.log('執行照片上傳 API，照服員ID:', id, '檔案大小:', file.size, '檔案類型:', file.type);
    const formData = new FormData();
    formData.append('file', file);
    
    // 顯示 FormData 內容用於調試
    for (let pair of formData.entries()) {
      console.log('FormData 內容:', pair[0], pair[1]);
    }
    
    return axiosInstance.post(`/caregiver/${id}/photo`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    }).then(response => {
      console.log('照片上傳 API 回應:', response);
      return response;
    }).catch(error => {
      console.error('照片上傳 API 錯誤:', error.response || error);
      throw error;
    });
  }
};