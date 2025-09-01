import { activityApi } from '@/api/activityApi';

/**
 * 圖片基礎網址設定 - 支援環境變數配置
 */
const IMAGE_BASE_URL = import.meta.env.VITE_IMAGE_BASE_URL || 'http://localhost:8080';

/**
 * 重點註解：處理圖片網址，將相對路徑轉換為完整網址，統一使用茶葉預設圖片
 * @param {string} imagePath - 圖片相對路徑（例如：/images/xxx.png）
 * @returns {string} 完整的圖片網址或預設茶葉圖片
 */
const getImageUrl = (imagePath) => {
    // 重點註解：如果沒有圖片路徑或為空字串，回傳茶葉預設圖片
    if (!imagePath || 
        imagePath.trim() === '' || 
        imagePath === '無圖片' || 
        imagePath === 'null' || 
        imagePath === 'undefined') {
        return '/src/assets/images/tea.png'; // 使用茶葉預設圖片
    }

    // 重點註解：如果已經是完整網址（包含 http 或 https），直接回傳
    if (imagePath.startsWith('http://') || imagePath.startsWith('https://')) {
        return imagePath;
    }

    // 重點註解：如果是本地專案圖片路徑（以 /src/ 開頭），直接回傳
    if (imagePath.startsWith('/src/')) {
        return imagePath;
    }

    // 重點註解：如果已經是茶葉圖片路徑，統一回傳
    if (imagePath.includes('tea.png')) {
        return '/src/assets/images/tea.png';
    }

    // 重點註解：如果是 data URL（base64 圖片），直接回傳
    if (imagePath.startsWith('data:image/')) {
        return imagePath;
    }

    // 重點註解：如果是相對路徑，加上基礎網址
    // 確保路徑以 / 開頭
    const normalizedPath = imagePath.startsWith('/') ? imagePath : `/${imagePath}`;
    return `${IMAGE_BASE_URL}${normalizedPath}`;
};

/**
 * 活動服務模組 - 統一處理活動狀態邏輯
 */
export const activityService = {
    /**
     * 獲取活動清單並統一處理狀態
     * @returns {Promise<Array>} 轉換後的活動資料陣列
     */
    async getActivities() {
        try {
            // 呼叫 API 取得活動列表
            console.log('開始獲取活動資料...');
            const response = await activityApi.getAllActivities();

            if (response.status === 200 && response.data) {
                console.log(`成功獲取 ${response.data.length} 筆活動資料`);
                // 轉換資料格式為前端所需的格式，確保與畫面設計一致
                const processedData = response.data.map(activity => this._processActivityData(activity));
                return processedData;
            }

            console.warn('API 回傳的資料為空');
            return [];
        } catch (error) {
            const errorMsg = this._handleError(error, "獲取活動資料");
            console.error('獲取活動資料失敗:', errorMsg);
            throw new Error(errorMsg);
        }
    },

    /**
     * 新增活動
     * @param {Object} activityData - 來自前端表單的原始活動資料
     * @returns {Promise<Object>} 包含 success、message 和 data 的結果物件
     */
    async addActivity(activityData) {
        try {
            this._validateActivityData(activityData);
            console.log('活動驗證成功');

            // 重點註解：步驟 2 - 將前端資料嚴格轉換為後端 ActivityRequest DTO 格式
            const activityRequestDto = this._transformToApiFormat(activityData);
            console.log('活動格式轉換成功');

            // 重點註解：步驟 3 - 呼叫 API 層，傳送已處理好的 DTO
            const response = await activityApi.addActivity(activityRequestDto);
            console.log('API 呼叫成功，等待處理回應...');
            // 重點註解：步驟 4 - 處理後端回應
            if (response.status === 201 || response.status === 200) { // 後端回應 201 CREATED
                console.log('活動新增成功，後端回應:', response);
                
                // 後端新增成功後，可能不會回傳完整的活動物件，這裡直接回傳成功訊息
                return {
                    success: true,
                    message: `活動「${activityData.name}」已成功建立`,
                    data: response.data // 後端若有回傳資料則一併回傳
                };
            }

            // 若狀態碼非預期，拋出錯誤
            throw new Error(`新增活動失敗：伺服器回應非預期狀態 ${response.status}`);

        } catch (error) {
            // 重點註解：步驟 5 - 統一處理所有流程中發生的錯誤
            const errorMsg = this._handleError(error, "新增活動");
            console.error('Service 層: 新增活動失敗:', errorMsg);
            // 回傳一個標準的失敗物件，讓前端元件可以處理
            return {
                success: false,
                message: errorMsg,
                data: null
            };
        }
    },

    /**
     * 提前結束活動報名
     * @param {number} id - 活動 ID
     * @returns {Promise<Object>} 結果物件
     */
    async endRegistration(id) {
        try {
            console.log(`開始提前結束活動 ID: ${id} 的報名...`);
            
            // 重點註解：驗證 ID 參數
            if (!id || isNaN(Number(id))) {
                throw new Error('活動 ID 無效');
            }

            // 重點註解：呼叫 API 層的 endRegistration 方法
            const response = await activityApi.endRegistration(id);
            
            if (response.status === 200) {
                console.log('提前結束活動報名成功:', response.data);
                return {
                    success: true,
                    message: '活動報名已成功結束',
                    data: response.data
                };
            }
            
            throw new Error(`提前結束活動報名失敗：伺服器回應 ${response.status}`);
        } catch (error) {
            const errorMsg = this._handleError(error, "提前結束活動報名");
            console.error('提前結束活動報名失敗:', errorMsg);
            return {
                success: false,
                message: errorMsg,
                data: null
            };
        }
    },

    /**
     * 更新活動
     * @param {number} id - 活動ID
     * @param {Object} activityData - 更新活動的資料
     * @returns {Promise<Object>} 更新結果
     */
    async updateActivity(id, activityData) {
        try {
            console.log(`開始更新活動 ID: ${id}...`, activityData);

            // 資料驗證
            this._validateActivityData(activityData, true); // true 表示編輯模式

            // 轉換前端資料格式為後端API所需格式
            const apiData = this._transformToApiFormat(activityData);

            console.log('轉換後的API資料:', apiData);

            // 呼叫API更新活動
            const response = await activityApi.updateActivity(id, apiData);

            if (response.status === 200) {
                console.log('活動更新成功:', response.data);

                // 處理更新成功的回應資料
                const updatedActivity = response.data ? this._processActivityData(response.data) : null;

                return {
                    success: true,
                    message: `活動「${activityData.name}」更新成功`,
                    data: updatedActivity
                };
            }

            throw new Error(`更新活動失敗：伺服器回應 ${response.status}`);

        } catch (error) {
            const errorMsg = this._handleError(error, "更新活動");
            console.error('更新活動失敗:', errorMsg);
            return {
                success: false,
                message: errorMsg,
                data: null
            };
        }
    },

    /**
     * 刪除活動
     * @param {number} id - 活動ID
     * @returns {Promise<Object>} 刪除結果
     */
    async deleteActivity(id) {
        try {
            console.log(`開始刪除活動 ID: ${id}...`);

            // 呼叫API刪除活動
            const response = await activityApi.deleteActivity(id);

            if (response.status === 200 || response.status === 204) {
                console.log('活動刪除成功');

                return {
                    success: true,
                    message: '活動刪除成功',
                    data: null
                };
            }

            throw new Error(`刪除活動失敗：伺服器回應 ${response.status}`);

        } catch (error) {
            const errorMsg = this._handleError(error, "刪除活動");
            console.error('刪除活動失敗:', errorMsg);
            return {
                success: false,
                message: errorMsg,
                data: null
            };
        }
    },

    /**
     * 儲存活動標籤
     * @param {number} activityId - 活動ID
     * @param {Array<string>} tags - 標籤陣列
     * @returns {Promise<Object>} 儲存結果
     */
    async saveActivityTags(activityId, tags) {
        try {
            console.log(`開始為活動 ${activityId} 儲存標籤:`, tags);

            // 確保標籤是陣列格式
            const validTags = Array.isArray(tags) ? tags : [];

            // 呼叫API儲存標籤
            const response = await activityApi.saveActivityTags(activityId, validTags);

            if (response.status === 200) {
                console.log('活動標籤儲存成功');

                return {
                    success: true,
                    message: '活動標籤儲存成功',
                    data: response.data
                };
            }

            throw new Error(`儲存活動標籤失敗：伺服器回應 ${response.status}`);

        } catch (error) {
            const errorMsg = this._handleError(error, "儲存活動標籤");
            console.error('儲存活動標籤失敗:', errorMsg);
            return {
                success: false,
                message: errorMsg,
                data: null
            };
        }
    },

    /**
     * 驗證活動資料 - 根據後端 ActivityRequest.java 的 @NotNull 要求
     * @param {Object} activityData - 活動資料
     * @param {boolean} isEdit - 是否為編輯模式
     * @throws {Error} 驗證失敗時拋出錯誤
     * @private
     */
    _validateActivityData(activityData) {
        const errors = [];

        // 重點註解：根據後端 @NotNull 註解進行必填欄位檢查
        if (!activityData.name || activityData.name.trim() === '') {
            errors.push('活動名稱不能為空 (@NotNull)');
        }

        if (!activityData.date) {
            errors.push('活動開始日期不能為空 (@NotNull)');
        }

        // 重點註解：後端要求 end 也是 @NotNull
        if (!activityData.end && !activityData.endDate) {
            errors.push('活動結束日期不能為空 (@NotNull)');
        }

        if (!activityData.time || activityData.time.trim() === '') {
            errors.push('活動時間不能為空 (@NotNull)');
        }

        // 重點註解：後端要求 registrationStart 也是 @NotNull
        if (!activityData.registrationStart) {
            errors.push('報名開始日期不能為空 (@NotNull)');
        }

        // 重點註解：後端要求 registrationEnd 也是 @NotNull
        if (!activityData.registrationEnd) {
            errors.push('報名結束日期不能為空 (@NotNull)');
        }

        // 活動地址
        if (!activityData.location || activityData.location.trim() === '') {
            errors.push('活動地點不能為空 (@NotNull)');
        }

        // 日期格式檢查 - 確保符合 yyyy-MM-dd 格式
        const dateFields = [
            { field: 'date', name: '活動開始日期' },
            { field: 'end', name: '活動結束日期', fallback: 'endDate' },
            { field: 'registrationStart', name: '報名開始日期' },
            { field: 'registrationEnd', name: '報名結束日期' }
        ];

        dateFields.forEach(({ field, name, fallback }) => {
            const dateValue = activityData[field] || (fallback ? activityData[fallback] : null);
            if (dateValue) {
                try {
                    const date = new Date(dateValue);
                    if (isNaN(date.getTime())) {
                        errors.push(`${name}格式不正確，需要 yyyy-MM-dd 格式`);
                    }
                    // 檢查是否符合 yyyy-MM-dd 格式
                    const dateRegex = /^\d{4}-\d{2}-\d{2}$/;
                    if (!dateRegex.test(dateValue)) {
                        errors.push(`${name}格式不正確，需要 yyyy-MM-dd 格式，當前格式: ${dateValue}`);
                    }
                } catch (e) {
                    errors.push(`${name}格式不正確: ${e.message}`);
                }
            }
        });

        // 日期邏輯檢查
        if (activityData.date && (activityData.end || activityData.endDate)) {
            try {
                const startDate = new Date(activityData.date);
                const endDate = new Date(activityData.end || activityData.endDate);

                if (endDate < startDate) {
                    errors.push('活動結束日期不能早於開始日期');
                }
            } catch (e) {
                // 日期格式錯誤在上面已經檢查過了
            }
        }

        // 報名時間檢查
        if (activityData.registrationStart && activityData.registrationEnd) {
            try {
                const regStart = new Date(activityData.registrationStart);
                const regEnd = new Date(activityData.registrationEnd);

                if (regEnd < regStart) {
                    errors.push('報名結束時間不能早於開始時間');
                }
            } catch (e) {
                // 日期格式錯誤在上面已經檢查過了
            }
        }

        // 如果有錯誤，拋出異常
        if (errors.length > 0) {
            const errorMsg = `資料驗證失敗:\n${errors.map((err, idx) => `${idx + 1}. ${err}`).join('\n')}`;
            console.error('驗證錯誤詳情:', {
                錯誤列表: errors,
                原始資料: activityData
            });
            throw new Error(errorMsg);
        }
    },

    /**
     * 處理活動資料，添加顯示所需的格式和狀態
     * @param {Object} activity - 原始活動資料
     * @returns {Object} 轉換後的活動資料
     * @private
     */
    _processActivityData(activity) {
        // 基本資料轉換
        const processedActivity = {
            id: activity.id,
            name: this._decodeActivityName(activity.name) || "未命名活動",
            category: activity.category || "未分類",
            categoryText: activity.categoryName || activity.category || "未分類",
            // 使用 getImageUrl 函數處理圖片 URL
            image: getImageUrl(activity.image || activity.imageUrl),
            date: activity.date,
            endDate: activity.endDate || activity.end || activity.date,
            time: activity.time || "09:00-17:00",
            location: activity.location || "地點未定",
            instructor: activity.instructor || "講師未定",
            description: activity.description || "暫無描述",
            capacity: activity.capacity || activity.limit || 20,
            currentCount: activity.currentCount || activity.current || 0,
            status: activity.status,
            registrationStart: activity.registrationStart,
            registrationEnd: activity.registrationEnd,
            tags: activity.tags || [],
            latitude: activity.latitude,
            longitude: activity.longitude,
            // 添加格式化後的日期
            formattedDate: this._formatDateRange(activity.date, activity.endDate || activity.end),
        };

        // 判斷活動狀態
        return this._determineActivityStatus(processedActivity);
    },

    /**
     * 判斷活動狀態 - 根據日期和當前狀態決定顯示狀態
     * @param {Object} activity - 活動物件
     * @returns {Object} 增強後的活動物件，包含統一的狀態顯示
     * @private
     */
    _determineActivityStatus(activity) {
        console.log('判斷活動狀態:', activity.name, {
            date: activity.date,
            endDate: activity.endDate,
            status: activity.status
        });

        // 獲取當前日期
        const now = new Date();
        const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());

        // 解析活動日期
        const activityDate = activity.date ? new Date(activity.date) : null;
        const activityEndDate = activity.endDate ? new Date(activity.endDate) : activityDate;

        // 如果無法解析日期，返回原始資料
        if (!activityDate) {
            console.log('無法解析活動日期，使用預設狀態');
            activity.statusText = activity.status ? "報名中" : "已關閉";
            activity.statusClass = activity.status ? "bg-success" : "bg-danger";
            activity.statusDisplay = activity.statusText;
            activity.isExpired = false;
            return activity;
        }

        // 判斷活動是否已過期 - 修正判斷邏輯
        // 只有當活動結束日期確實過了今天才算過期
        const isExpired = activityEndDate && activityEndDate < today;

        console.log('日期比較:', {
            今天: today.toDateString(),
            活動結束日期: activityEndDate?.toDateString(),
            是否過期: isExpired
        });

        // 根據時間和原始狀態設置顯示狀態
        if (isExpired) {
            // 活動已過期
            console.log('活動已過期');
            activity.status = false;
            activity.statusText = "已結束";
            activity.statusClass = "bg-secondary";
            activity.statusDisplay = "已結束";
            activity.isExpired = true;
        } else {
            // 活動未過期，根據原始狀態顯示
            console.log('活動未過期，原始狀態:', activity.status);
            if (activity.status === true || activity.status === 1 || activity.status === "true") {
                activity.status = true;  // 確保狀態為布林值
                activity.statusText = "報名中";
                activity.statusClass = "bg-success";
                activity.statusDisplay = "報名中";
            } else {
                activity.status = false;  // 確保狀態為布林值
                activity.statusText = "已關閉";
                activity.statusClass = "bg-danger";
                activity.statusDisplay = "已關閉";
            }
            activity.isExpired = false;
        }

        // 格式化日期顯示
        activity.formattedDate = this._formatDate(activity.date);
        activity.formattedDateRange = activity.endDate && activity.endDate !== activity.date ?
            `${this._formatDate(activity.date)} - ${this._formatDate(activity.endDate)}` :
            this._formatDate(activity.date);

        // 報名時間範圍
        if (activity.registrationStart && activity.registrationEnd) {
            activity.registrationTimeRange = `${this._formatDate(activity.registrationStart)} - ${this._formatDate(activity.registrationEnd)}`;
        }

        // 計算報名進度百分比
        activity.registrationProgress = activity.capacity > 0 ?
            Math.min(Math.round((activity.currentCount / activity.capacity) * 100), 100) : 0;

        console.log('最終活動狀態:', {
            名稱: activity.name,
            狀態: activity.status,
            狀態文字: activity.statusText,
            是否過期: activity.isExpired
        });

        return activity;
    },

    /**
     * 處理圖片路徑，統一轉換為後端所需格式
     * @param {string} imagePath - 原始圖片路徑
     * @returns {string} 處理後的圖片路徑，格式為 /images/檔案名稱
     * @private
     */
    _processImagePathForBackend(imagePath) {
        // 重點註解：如果沒有圖片路徑或為空字串，返回空字串
        if (!imagePath || imagePath.trim() === '' || imagePath === '無圖片') {
            console.log('圖片路徑為空，返回空字串');
            return '';
        }

        // 重點註解：如果是 data URL（base64 圖片），直接返回，讓後端處理
        if (imagePath.startsWith('data:image/')) {
            console.log('偵測到 base64 圖片，直接返回');
            return imagePath;
        }

        // 重點註解：提取檔案名稱，移除所有路徑前綴
        let fileName = '';

        try {
            // 處理各種可能的路徑格式
            if (imagePath.includes('/')) {
                // 取得最後一個 / 後面的檔案名稱
                fileName = imagePath.split('/').pop();
            } else if (imagePath.includes('\\')) {
                // 處理 Windows 路徑格式
                fileName = imagePath.split('\\').pop();
            } else {
                // 如果沒有路徑分隔符，假設整個字串就是檔案名稱
                fileName = imagePath;
            }

            // 重點註解：移除可能的查詢參數（如 ?timestamp=123456）
            if (fileName.includes('?')) {
                fileName = fileName.split('?')[0];
            }

            // 重點註解：移除可能的錨點參數（如 #section）
            if (fileName.includes('#')) {
                fileName = fileName.split('#')[0];
            }

            // 重點註解：確保檔案名稱不為空且有有效的副檔名
            if (!fileName || fileName.trim() === '') {
                console.warn('無法提取有效的檔案名稱:', imagePath);
                return '';
            }

            // 重點註解：檢查是否有副檔名
            const hasExtension = /\.(jpg|jpeg|png|gif|webp|svg|bmp)$/i.test(fileName);
            if (!hasExtension) {
                console.warn('檔案名稱沒有有效的圖片副檔名:', fileName);
                // 可以選擇添加預設副檔名或返回空字串
                // 這裡選擇返回空字串，讓後端處理
                return '';
            }

            // 重點註解：統一格式為 /images/檔案名稱
            const processedPath = `/images/${fileName}`;
            
            console.log('圖片路徑處理結果:', {
                原始路徑: imagePath,
                提取檔案名: fileName,
                最終路徑: processedPath
            });

            return processedPath;

        } catch (error) {
            console.error('處理圖片路徑時發生錯誤:', error, '原始路徑:', imagePath);
            return '';
        }
    },

    /**
     * 新增活動資料格式轉換
     * @param {Object} frontendData - 前端表單資料
     * @returns {Object} 符合後端 ActivityRequest DTO 格式的物件
     * @private
     */
    _transformToApiFormat(frontendData) {
        console.log('開始轉換為 API 格式，原始前端資料:', frontendData);

        // **重點：確保所有 @NotNull 欄位都有值**
        const today = new Date().toISOString().split('T')[0];
        
        // **重點：驗證必填欄位**
        if (!frontendData.name?.trim()) {
            throw new Error('活動名稱不能為空');
        }
        if (!frontendData.category?.trim()) {
            throw new Error('活動類別不能為空');
        }
        if (!frontendData.location?.trim()) {
            throw new Error('活動地點不能為空');
        }

        const apiData = {
            // **重點：所有 @NotNull 欄位必須提供值**
            name: frontendData.name.trim(),
            category: frontendData.category.trim(),
            location: frontendData.location.trim(),
            time: frontendData.time?.trim() || "09:00",
            
            // **重點：確保日期格式正確且不為空**
            date: frontendData.date || today,
            end: frontendData.end || frontendData.date || today,
            registrationStart: frontendData.registrationStart || frontendData.date || today,
            registrationEnd: frontendData.registrationEnd || frontendData.end || frontendData.date || today,

            // 數值型別
            limit: Number(frontendData.limit) || 30,
            current: Number(frontendData.current) || 0,

            // 布林值
            status: frontendData.status !== undefined ? Boolean(frontendData.status) : true,

            // 可選欄位
            instructor: frontendData.instructor?.trim() || null,
            description: frontendData.description?.trim() || null,
            image: this._processImagePathForBackend(frontendData.image) || null,
            latitude: frontendData.latitude ? Number(frontendData.latitude) : null,
            longitude: frontendData.longitude ? Number(frontendData.longitude) : null,
        };
        
        console.log('轉換完成，準備送往後端的 ActivityRequest 物件:', apiData);
        return apiData;
    },

    /**
     * 根據 ID 獲取單一活動 - 確保方法完整
     * @param {number} id - 活動 ID
     * @returns {Promise<Object>} 轉換後的活動資料
     */
    async getActivityById(id) {
        try {
            console.log(`開始獲取活動詳情，ID: ${id}`);

            // 驗證 ID 參數
            if (!id || isNaN(Number(id))) {
                throw new Error('活動 ID 無效');
            }

            const response = await activityApi.getActivityById(id);

            if (response.status === 200 && response.data) {
                console.log('成功獲取活動原始資料:', response.data);

                // 轉換並應用狀態判斷
                const processedActivity = this._processActivityData(response.data);

                console.log('活動資料處理完成:', processedActivity);
                return processedActivity;
            }

            throw new Error(`找不到 ID 為 ${id} 的活動`);
        } catch (error) {
            const errorMsg = this._handleError(error, "獲取活動詳情");
            console.error('獲取活動詳情失敗:', errorMsg);
            throw new Error(errorMsg);
        }
    },

    /**
     * 更新活動狀態 - 用於自動將過期活動標記為已結束
     * @param {number} id - 活動 ID
     * @param {boolean} status - 新狀態
     * @returns {Promise<Object>} 操作結果
     */
    async updateActivityStatus(id, status) {
        try {
            const response = await activityApi.updateActivityStatus(id, status);

            if (response.status >= 200 && response.status < 300) {
                return {
                    success: true,
                    message: `活動狀態已更新為${status ? "報名中" : "已結束"}`
                };
            }

            throw new Error(`更新活動狀態失敗：伺服器回應 ${response.status}`);
        } catch (error) {
            const errorMsg = this._handleError(error, "更新活動狀態");
            return {
                success: false,
                message: errorMsg
            };
        }
    },

    /**
     * 解碼活動名稱，解決可能出現的亂碼問題
     * @param {string} name - 活動名稱
     * @returns {string} 解碼後的名稱
     * @private
     */
    _decodeActivityName(name) {
        if (!name) return '';

        // 記錄原始名稱以便除錯
        console.log('處理活動名稱:', name);

        try {
            // 第一步：嘗試 URI 解碼
            if (/(%[0-9A-F]{2})+/i.test(name)) {
                try {
                    const decoded = decodeURIComponent(name);
                    console.log('URI解碼後:', decoded);
                    return decoded;
                } catch (e) {
                    console.warn('URI解碼失敗:', e);
                }
            }

            // 第二步：處理HTML實體
            if (/&(#\d+|[a-z]+);/i.test(name)) {
                const tempStr = name.replace(/&amp;/g, '&')
                    .replace(/&lt;/g, '<')
                    .replace(/&gt;/g, '>')
                    .replace(/&quot;/g, '"')
                    .replace(/&#39;/g, "'");
                console.log('HTML實體解碼後:', tempStr);
                return tempStr;
            }

            // 第三步：處理其他可能的編碼問題（如UTF-8顯示為ISO-8859-1等）
            if (/[\u00A0-\u00FF]+/.test(name)) {
                try {
                    // 嘗試透過編碼再解碼方式處理某些特殊情況
                    const encoded = encodeURIComponent(name);
                    const decoded = decodeURIComponent(encoded);
                    if (decoded !== name) {
                        console.log('特殊編碼處理後:', decoded);
                        return decoded;
                    }
                } catch (e) {
                    console.warn('特殊編碼處理失敗:', e);
                }
            }

            // 如果不需要解碼，直接返回原始字符串
            return name;
        } catch (error) {
            console.error('解碼活動名稱時發生錯誤:', error);
            // 解碼失敗時返回原始字符串
            return name;
        }
    },

    /**
     * 格式化單一日期為本地字串
     * @param {string} dateStr - 日期字串
     * @returns {string} 格式化後的日期字串
     * @private
     */
    _formatDate(dateStr) {
        if (!dateStr) return "";
        try {
            const date = new Date(dateStr);
            return date.toLocaleDateString("zh-TW");
        } catch (e) {
            return dateStr;
        }
    },

    /**
     * 格式化日期範圍
     * @param {string} startDate - 開始日期
     * @param {string} endDate - 結束日期
     * @returns {string} 格式化後的日期字符串
     * @private
     */
    _formatDateRange(startDate, endDate) {
        if (!startDate) return '日期未定';

        try {
            const start = new Date(startDate);
            const startFormat = `${start.getFullYear()}/${start.getMonth() + 1}/${start.getDate()}`;

            if (endDate && endDate !== startDate) {
                const end = new Date(endDate);
                const endFormat = `${end.getFullYear()}/${end.getMonth() + 1}/${end.getDate()}`;
                return `${startFormat} ~ ${endFormat}`;
            }

            return startFormat;
        } catch (error) {
            console.error('格式化日期時發生錯誤:', error);
            return startDate; // 發生錯誤時返回原始日期字符串
        }
    },

    /**
     * 統一錯誤處理方法
     * @param {Error} error - 錯誤物件
     * @param {string} operation - 操作名稱
     * @returns {string} 格式化的錯誤訊息
     * @private
     */
    _handleError(error, operation) {
        console.error(`${operation}錯誤:`, error);
        return error.response?.data?.message || error.message || `${operation}失敗，請稍後再試`;
    }
};
