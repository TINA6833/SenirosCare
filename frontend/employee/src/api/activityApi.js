import axiosInstance from './axiosInstance'; 

/**
 * 活動 API 模組 - 負責 HTTP 請求
 * 對應後端 ActivityController 的所有端點
 */
export const activityApi = {
    /**
     * 查詢所有活動 - 對應 @GetMapping
     * @returns {Promise} axios response
     */
    async getAllActivities() {
        try {
            console.log('正在獲取所有活動...');
            // **重點：使用 axiosInstance，它會自動帶上 Token**
            return await axiosInstance.get('/activities');
        } catch (error) {
            console.error('獲取活動列表失敗:', error);
            throw error;
        }
    },

    /**
     * 根據條件查詢活動 - 對應 @GetMapping 帶查詢參數
     * @param {Object} queryParams - 查詢參數物件
     * @param {string} queryParams.name - 活動名稱（模糊查詢）
     * @param {string} queryParams.category - 活動類別
     * @param {string} queryParams.date - 活動日期
     * @param {string} queryParams.instructor - 講師名稱
     * @param {string} queryParams.location - 活動地點
     * @param {boolean} queryParams.status - 活動狀態
     * @returns {Promise} axios response
     */
    async getActivitiesByQuery(queryParams) {
        try {
            // 過濾掉空值參數
            const filteredParams = Object.fromEntries(
                Object.entries(queryParams).filter(([key, value]) =>
                    value !== null && value !== undefined && value !== ''
                )
            );

            console.log('正在查詢活動，條件:', filteredParams);

            // **重點：使用 axiosInstance 進行條件查詢**
            return await axiosInstance.get('/activities', {
                params: filteredParams
            });
        } catch (error) {
            console.error('查詢活動失敗:', error);
            throw error;
        }
    },

    /**
     * 查詢單筆活動 - 對應 @GetMapping("/{id}")
     * @param {number} id - 活動 ID
     * @returns {Promise} axios response
     */
    async getActivityById(id) {
        try {
            console.log(`正在獲取活動 ID: ${id}`);
            // **重點：使用 axiosInstance 獲取單筆活動資料**
            return await axiosInstance.get(`/activities/${id}`);
        } catch (error) {
            console.error(`獲取活動 ${id} 失敗:`, error);
            throw error;
        }
    },

    /**
     * 新增活動 - 對應 @PostMapping
     * @param {Object} activityRequest - 活動請求資料，對應 ActivityRequest DTO
     * @returns {Promise} axios response
     */
    async addActivity(activityRequest) {
        try {
            console.log('準備發送的活動資料:', activityRequest);
            
            // **重點：在發送前再次檢查必填欄位**
            const requiredFields = ['name', 'category', 'location', 'time', 'date', 'end', 'registrationStart', 'registrationEnd'];
            for (const field of requiredFields) {
                if (!activityRequest[field]) {
                    throw new Error(`必填欄位 ${field} 不能為空`);
                }
            }
            
            const response = await axiosInstance.post('/activities', activityRequest);
            return response;
        } catch (error) {
            console.error('新增活動失敗:', error);
            
            // **重點：提供更詳細的錯誤訊息**
            if (error.response?.status === 400) {
                const errorMessage = error.response?.data?.message || '請求資料格式錯誤，請檢查所有必填欄位';
                throw new Error(errorMessage);
            }
            
            throw error;
        }
    },

    /**
     * 更新活動 - 對應 @PutMapping("/{id}")
     * @param {number} id - 活動 ID
     * @param {Object} activityRequest - 活動請求資料，對應 ActivityRequest DTO
     * @returns {Promise} axios response
     */
    async updateActivity(id, activityRequest) {
        try {
            console.log(`正在更新活動 ID: ${id}`, activityRequest);
            // **重點：使用 axiosInstance 更新活動資料**
            return await axiosInstance.put(`/activities/${id}`, activityRequest);
        } catch (error) {
            console.error(`更新活動 ${id} 失敗:`, error);
            throw error;
        }
    },

    /**
     * 刪除活動 - 對應 @DeleteMapping("/{id}")
     * @param {number} id - 活動 ID
     * @returns {Promise} axios response
     */
    async deleteActivity(id) {
        try {
            console.log(`正在刪除活動 ID: ${id}`);
            // **重點：使用 axiosInstance 刪除活動**
            return await axiosInstance.delete(`/activities/${id}`);
        } catch (error) {
            console.error(`刪除活動 ${id} 失敗:`, error);
            throw error;
        }
    },

    /**
     * 為活動新增標籤 - 對應 @PostMapping("/{id}/tags")
     * @param {number} activityId - 活動 ID
     * @param {Array<string>} tags - 標籤陣列
     * @returns {Promise} axios response
     */
    async saveActivityTags(activityId, tags) {
        try {
            console.log(`正在為活動 ${activityId} 儲存標籤:`, tags);
            // **重點：使用 axiosInstance 儲存活動標籤**
            return await axiosInstance.post(`/activities/${activityId}/tags`, tags);
        } catch (error) {
            console.error(`儲存活動 ${activityId} 標籤失敗:`, error);
            throw error;
        }
    },

    /**
     * 根據標籤查詢活動 - 對應 @GetMapping("/tags/{tag}")
     * @param {string} tagName - 標籤名稱
     * @returns {Promise} axios response
     */
    async getActivitiesByTag(tagName) {
        try {
            console.log(`正在查詢標籤 "${tagName}" 的活動`);
            // **重點：使用 axiosInstance 根據標籤查詢活動，並對標籤名稱進行URL編碼**
            return await axiosInstance.get(`/activities/tags/${encodeURIComponent(tagName)}`);
        } catch (error) {
            console.error(`查詢標籤 "${tagName}" 活動失敗:`, error);
            throw error;
        }
    },

    /**
     * 更新活動狀態 - 用於自動更新過期活動狀態
     * @param {number} id - 活動ID
     * @param {boolean} status - 活動狀態 (true: 報名中, false: 已結束/已關閉)
     * @returns {Promise} axios 回應
     */
    async updateActivityStatus(id, status) {
        try {
            console.log(`正在更新活動 ID: ${id} 的狀態為 ${status ? '報名中' : '已結束/已關閉'}`);
            // **重點：使用 axiosInstance 更新活動狀態**
            return await axiosInstance.patch(`/activities/${id}/status`, { status });
        } catch (error) {
            console.error('更新活動狀態失敗:', error);
            throw error;
        }
    },

    /**
     * 提前結束活動報名
     * @param {number} id - 活動 ID
     * @returns {Promise} axios 回應
     */
    async endRegistration(id) {
        try {
            console.log(`正在呼叫後端 API 結束活動 ID: ${id} 的報名...`);
            
            // 重點註解：驗證參數
            if (!id || isNaN(Number(id))) {
                throw new Error('活動 ID 必須是有效的數字');
            }

            // 重點註解：使用 axiosInstance 發送 POST 請求到後端
            const response = await axiosInstance.post(`/activities/${id}/end-registration`);
            
            console.log(`活動 ID: ${id} 結束報名 API 呼叫成功:`, response.data);
            return response;
        } catch (error) {
            console.error(`提前結束活動 ID: ${id} 報名失敗:`, error);
            
            // 重點註解：處理不同類型的錯誤回應
            if (error.response?.status === 404) {
                throw new Error(`找不到 ID 為 ${id} 的活動`);
            } else if (error.response?.status === 400) {
                throw new Error(error.response?.data?.message || '請求參數錯誤');
            } else if (error.response?.status === 500) {
                throw new Error('伺服器內部錯誤，請稍後再試');
            }
            
            throw error;
        }
    }
};

/**
 * 新增活動時，轉換表單資料為 API 格式
 * @param {Object} form - 表單資料物件
 * @returns {Object} 轉換後的 API 請求格式
 */
export function toActivityRequest(form) {
    // **重點註解：轉換表單資料為 API 格式，確保所有必填欄位與型別正確**
    return {
        name: form.name,
        category: form.category,
        limit: Number(form.limit),
        current: Number(form.current) || 0,
        date: form.date ? form.date.slice(0, 10) : '', // yyyy-MM-dd
        end: form.endDate ? form.endDate.slice(0, 10) : '', // yyyy-MM-dd
        time: form.time,
        registrationStart: form.registrationStart ? form.registrationStart.slice(0, 10) : '',
        registrationEnd: form.registrationEnd ? form.registrationEnd.slice(0, 10) : '',
        location: form.location,
        latitude: form.latitude ? Number(form.latitude) : null,
        longitude: form.longitude ? Number(form.longitude) : null,
        instructor: form.instructor,
        status: !!form.status,
        description: form.description,
        image: form.image
    };
}

/**
 * 新增活動的範例實作 - 可供 composables 使用
 * @param {Object} form - 表單資料
 * @param {Object} loading - 載入狀態 ref
 * @param {Object} error - 錯誤狀態 ref
 * @param {Object} success - 成功狀態 ref
 * @param {Function} loadActivities - 重新載入活動列表的函數
 * @returns {Promise<boolean>} 是否新增成功
 */
const addActivity = async (form, loading, error, success, loadActivities) => {
    loading.value = true;
    error.value = '';
    success.value = '';
    
    // **重點註解：先檢查活動類別是否有效**
    if (!form.category) {
        error.value = '請選擇有效的活動類別';
        return false;
    }
    
    try {
        const activityRequest = toActivityRequest(form);
        console.log('送出新增活動:', activityRequest); // **重點註解：送出前檢查**
        
        // **重點：使用 axiosInstance 透過 activityApi 新增活動**
        const response = await activityApi.addActivity(activityRequest);
        success.value = '活動新增成功';
        
        // **重點註解：新增成功後，重新載入活動列表**
        await loadActivities();
        return true;
    } catch (err) {
        error.value = err.message || '新增活動失敗';
        return false;
    } finally {
        loading.value = false;
    }
};