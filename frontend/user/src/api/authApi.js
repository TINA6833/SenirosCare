import axiosInstance from "./axiosInstance";

/**
 * [重點] 驗證當前 token 是否有效
 * 透過呼叫需要認證的 API 來檢查 token 狀態
 * @returns {Promise<boolean>} token 是否有效
 */
export async function validateToken() {
  try {
    // [重點] 使用會員資料 API 來驗證 token，而不是不存在的 API
    const response = await axiosInstance.get('/members/me');
    return response.status === 200;
  } catch (error) {
    // [重點] 如果 API 回傳 401 或其他錯誤，表示 token 無效
    console.warn('Token 驗證失敗:', error.response?.status);
    return false;
  }
}

/**
 * [重點] 取得 Line 登入的 URL
 * @returns {string} Line OAuth 登入網址
 */
export function getLineLoginUrl() {
  // [重點] 根據您的後端設定，回傳 Line OAuth 的授權網址
  return 'http://localhost:8080/oauth2/authorization/line';
}

/**
 * [重點] 處理登入後的回調，取得 token
 * 這個函數會在使用者從 Line 登入後被呼叫
 * @param {string} code - Line 回傳的授權碼
 * @returns {Promise<Object>} 包含 token 和使用者資料的物件
 */
export async function handleLineCallback(code) {
  try {
    const response = await axiosInstance.post('/auth/line/callback', { code });
    return response.data;
  } catch (error) {
    console.error('Line 登入回調處理失敗:', error);
    throw error;
  }
}