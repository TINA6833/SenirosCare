import axios from 'axios';

// [重點] 建立一個 Axios 實例
const axiosInstance = axios.create({
  // [重點] 設定 API 的基礎路徑
  baseURL: 'http://localhost:8080/api', 
});

/**
 * [重點] 請求攔截器 (Request Interceptor)
 * 功能：在每個請求發送前，自動加上 Authorization 標頭
 */
axiosInstance.interceptors.request.use(
  (config) => {
    // [重點] 直接從 localStorage 取得 token，避免 Pinia 初始化問題
    const token = localStorage.getItem('authToken');

    // [重點] 如果 token 存在，則將其加入到請求的 Authorization 標頭中
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    // [重點] 回傳設定好的 config
    return config;
  },
  (error) => {
    // [重點] 如果請求設定出錯，直接回絕
    return Promise.reject(error);
  }
);

/**
 * [重點] 回應攔截器 (Response Interceptor)
 * 功能：處理全域的 API 回應錯誤
 */
axiosInstance.interceptors.response.use(
  // [重點] 對於成功的請求 (HTTP 狀態碼在 2xx 範圍內)，直接回傳 response
  (response) => response,
  
  // [重點] 對於失敗的請求進行處理
  async (error) => {
    const { response } = error;

    if (response?.status === 401) {
      // [重點] Token 無效，清除本地認證資料
      console.log('Token 無效，清除本地認證資料');
      console.warn('Token 無效，清除本地認證資料');
      localStorage.removeItem('authToken');
      localStorage.removeItem('redirectAfterLogin');
      
      // [重點] 如果不是在登入相關頁面，重定向到登入頁面
      if (!window.location.pathname.includes('/auth/') && 
          !window.location.pathname.includes('/login')) {
        localStorage.setItem('redirectAfterLogin', window.location.pathname);
        window.location.href = '/login';
      }
    } else {
      // [重點] 非 401 的錯誤直接放行，並印出錯誤訊息
      console.error(`API 錯誤，狀態碼: ${response?.status}`, response?.data || error.message);
    }

    return Promise.reject(error);
  }
);

export default axiosInstance;