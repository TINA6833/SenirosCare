import axiosInstance from './axiosInstance'; 

/**
 * 活動報名 API 模組 - 負責 HTTP 請求
 * 對應後端 ActivityReservationController 的報名相關端點
 * 以及 ActivityCategoryController 的活動類別端點
 */
export const activityRegistrationApi = {
    /**
     * 審核預約
     * 更新預約狀態為 '已取消' 或 '報名成功'
     * @param {Object} reservationData - 預約更新資料
     * @param {number} reservationData.activityId - 活動ID
     * @param {number} reservationData.memberId - 會員ID
     * @param {string} reservationData.status - 狀態：'已取消' 或 '報名成功'
     * @returns {Promise} - HTTP 請求 Promise
     */
    reviewReservation(reservationData) {
        return axiosInstance.put('/activities/reservation/status', reservationData);
    },

    /**
     * 重點註解：客製化查詢活動報名 - 根據後端新的查詢參數格式
     * 不傳參數則回傳全部預約
     * @param {Object} queryParams - 查詢參數 (可選)
     * @param {string} queryParams.CategoryName - 活動類別名稱 (可選)
     * @param {string} queryParams.activityName - 活動名稱 (可選)
     * @param {number} queryParams.memberId - 會員ID (可選)
     * @returns {Promise<Array>} - 回傳預約清單的 Promise
     */
    getAllReservations(queryParams = {}) {
        // 重點註解：將前端參數轉換為後端期望的格式
        const backendParams = {};
        
        // 重點註解：處理類別名稱參數
        if (queryParams.CategoryName) {
            backendParams.CategoryName = queryParams.CategoryName;
        }
        
        // 重點註解：處理活動名稱參數
        if (queryParams.activityName) {
            backendParams.activityName = queryParams.activityName;
        }
        
        // 重點註解：處理會員ID參數
        if (queryParams.memberId) {
            backendParams.memberId = queryParams.memberId;
        }

        console.log('發送查詢參數到後端:', backendParams);
        
        return axiosInstance.get('/activities/reservation', {
            params: backendParams
        });
    },

    /**
     * 重點註解：根據活動名稱查詢該活動的所有預約
     * @param {string} activityName - 活動名稱
     * @returns {Promise<Array>} - 回傳該活動預約清單的 Promise
     */
    getReservationsByActivityName(activityName) {
        return axiosInstance.get('/activities/reservation', {
            params: { activityName }
        });
    },

    /**
     * 重點註解：根據類別名稱查詢該類別的所有預約
     * @param {string} categoryName - 類別名稱
     * @returns {Promise<Array>} - 回傳該類別預約清單的 Promise
     */
    getReservationsByCategoryName(categoryName) {
        return axiosInstance.get('/activities/reservation', {
            params: { CategoryName: categoryName }
        });
    },

    /**
     * 根據會員ID查詢該會員的所有預約
     * @param {number} memberId - 會員ID
     * @returns {Promise<Array>} - 回傳該會員預約清單的 Promise
     */
    getReservationsByMemberId(memberId) {
        return axiosInstance.get('/activities/reservation', {
            params: { memberId }
        });
    },

    // === 活動類別相關 API ===
    
    /**
     * 獲取所有啟用的活動類別
     * 對應後端 ActivityCategoryController.list() 方法
     * @returns {Promise} - 回傳活動類別清單的 Promise
     */
    getAllActiveCategories() {
        try {
            console.log('正在獲取所有啟用的活動類別...');
            // 重點註解：使用 axiosInstance，自動帶上 Token，對應後端 /api/activity-categories 端點
            return axiosInstance.get('/activity-categories');
        } catch (error) {
            console.error('獲取活動類別列表失敗:', error);
            throw error;
        }
    }
};


