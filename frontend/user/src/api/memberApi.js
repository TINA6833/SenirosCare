import axiosInstance from "./axiosInstance";

/**
 * 取得當前登入的會員資料
 * @returns {Promise<Object>} 當前登入會員資料
 */
export function getMyProfile() {
  return axiosInstance.get('/members/me');
}

/**
 * 修改自己會員資料
 * @param {Object} memberUpdateDto - 更新後的會員資料
 * @returns {Promise<Object>} 更新後的會員資料
 */
export function updateProfile(memberUpdateDto) {
  return axiosInstance.put('/members/me', memberUpdateDto);
}

export default {
  getMyProfile,
  updateProfile
};

