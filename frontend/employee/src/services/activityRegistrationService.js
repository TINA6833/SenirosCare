import { activityRegistrationApi } from '@/api/activityRegistrationApi';

/**
 * 活動報名服務層 - 處理資料轉換和業務邏輯
 */
export const activityRegistrationService = {
    /**
     * 重點註解：根據活動名稱獲取報名清單 - 使用新的查詢參數格式
     * @param {string} activityName - 活動名稱
     * @returns {Promise<Array>} 轉換後的報名資料陣列
     */
    async getRegistrationsByActivityName(activityName) {
        try {
            // 重點註解：呼叫 API 層獲取報名資料，使用活動名稱查詢
            const response = await activityRegistrationApi.getReservationsByActivityName(activityName);

            if (response.status === 200 && response.data) {
                // 重點註解：從 API 回應中取得報名清單
                const registrations = Array.isArray(response.data) ? response.data : [];

                // 重點註解：轉換資料格式為前端所需的結構
                return registrations.map(reg => this._formatRegistration(reg));
            }

            return [];
        } catch (error) {
            console.error('獲取活動報名資料失敗:', error);
            throw new Error(error.message || '獲取活動報名資料失敗');
        }
    },

    /**
     * 重點註解：根據類別名稱獲取報名清單 - 使用新的查詢參數格式
     * @param {string} categoryName - 類別名稱
     * @returns {Promise<Array>} 轉換後的報名資料陣列
     */
    async getRegistrationsByCategoryName(categoryName) {
        try {
            // 重點註解：呼叫 API 層獲取報名資料，使用類別名稱查詢
            const response = await activityRegistrationApi.getReservationsByCategoryName(categoryName);

            if (response.status === 200 && response.data) {
                const registrations = Array.isArray(response.data) ? response.data : [];
                return registrations.map(reg => this._formatRegistration(reg));
            }

            return [];
        } catch (error) {
            console.error('獲取類別報名資料失敗:', error);
            throw new Error(error.message || '獲取類別報名資料失敗');
        }
    },

    /**
     * 根據會員 ID 獲取報名清單
     * @param {number|string} memberId - 會員 ID
     * @returns {Promise<Array>} 轉換後的報名資料陣列
     */
    async getRegistrationsByMemberId(memberId) {
        try {
            const response = await activityRegistrationApi.getReservationsByMemberId(memberId);

            if (response.status === 200 && response.data) {
                const registrations = Array.isArray(response.data) ? response.data : [];
                return registrations.map(reg => this._formatRegistration(reg));
            }

            return [];
        } catch (error) {
            console.error('獲取會員報名資料失敗:', error);
            throw new Error(error.message || '獲取會員報名資料失敗');
        }
    },

    /**
     * 重點註解：獲取所有報名資料 - 使用新的查詢參數格式
     * @param {Object} queryParams - 查詢參數 (可選)
     * @param {string} queryParams.CategoryName - 類別名稱 (可選)
     * @param {string} queryParams.activityName - 活動名稱 (可選)
     * @param {number} queryParams.memberId - 會員ID (可選)
     * @returns {Promise<Array>} 轉換後的報名資料陣列
     */
    async getAllReservations(queryParams = {}) {
        try {
            const response = await activityRegistrationApi.getAllReservations(queryParams);

            if (response.status === 200 && response.data) {
                const registrations = Array.isArray(response.data) ? response.data : [];
                return registrations.map(reg => this._formatRegistration(reg));
            }

            return [];
        } catch (error) {
            console.error('獲取報名資料失敗:', error);
            throw new Error(error.message || '獲取報名資料失敗');
        }
    },

    /**
     * 獲取所有啟用的活動類別
     * @returns {Promise<Array>} 轉換後的活動類別陣列
     */
    async getActiveCategories() {
        try {
            // 重點註解：呼叫 API 層獲取活動類別資料
            const response = await activityRegistrationApi.getAllActiveCategories();

            if (response.status === 200 && response.data) {
                // 重點註解：從 API 回應中取得活動類別清單
                const categories = Array.isArray(response.data) ? response.data : [];

                // 重點註解：轉換資料格式為前端所需的結構
                return categories.map(category => this._formatCategory(category));
            }

            return [];
        } catch (error) {
            console.error('獲取活動類別資料失敗:', error);
            throw new Error(error.message || '獲取活動類別資料失敗');
        }
    },

    /**
     * 重點註解：審核報名 - 更新報名狀態
     * @param {number} activityId - 活動ID
     * @param {number} memberId - 會員ID
     * @param {string} status - 新狀態：'已取消' 或 '報名成功'
     * @returns {Promise<Object>} 處理結果
     */
    async reviewReservation(activityId, memberId, status) {
        try {
            // 重點註解：驗證狀態參數
            if (!['已取消', '報名成功'].includes(status)) {
                throw new Error(`不支援的狀態: ${status}，請使用 '已取消' 或 '報名成功'`);
            }

            // 重點註解：驗證必要參數
            if (!activityId || !memberId) {
                throw new Error('活動ID和會員ID為必填參數');
            }

            // 重點註解：準備審核資料 - 對應 ReservationUpdateDto
            const reservationData = {
                activityId: Number(activityId),
                memberId: Number(memberId),
                status
            };

            console.log('發送審核請求:', reservationData);

            const response = await activityRegistrationApi.reviewReservation(reservationData);

            return {
                success: response.status >= 200 && response.status < 300,
                message: status === '報名成功' ? '審核通過成功' : '取消報名成功',
                data: response.data
            };
        } catch (error) {
            console.error(`審核報名失敗 (活動ID: ${activityId}, 會員ID: ${memberId}):`, error);
            return {
                success: false,
                message: error.message || '審核報名失敗'
            };
        }
    },

    /**
     * 重點註解：格式化報名資料 - 根據後端更新的 ReservationResponse DTO
     * @param {Object} registration - API 回傳的報名資料（ReservationResponse 格式）
     * @returns {Object} 轉換後的報名資料
     * @private
     */
    _formatRegistration(registration) {
        // 重點註解：檢查欄位是否存在，並進行必要的轉換
        if (!registration || typeof registration !== 'object') {
            return {};
        }

        // 重點註解：根據後端更新的 ReservationResponse DTO 格式化資料
        return {
            // 重點註解：基本欄位（對應 ReservationResponse DTO）
            // 產生一個唯一 ID，用於前端識別
            id: `${registration.activityId || 'unknown'}_${registration.memberId || 'unknown'}_${registration.scheduledAt || Date.now()}`,
            
            // 重點註解：ID 欄位 - 審核功能必需 (現在後端已提供)
            activityId: registration.activityId || null, // 對應 DTO 的 activityId
            memberId: registration.memberId || null,     // 對應 DTO 的 memberId
            
            // 重點註解：活動相關資訊
            activityName: registration.activityName || '未知活動', // 對應 DTO 的 activityName
            activityCategory: registration.activityCategory || null, // 對應 DTO 的 activityCategory
            activityCategoryId: registration.activityCategory, // 儲存原始類別 ID
            
            // 重點註解：會員相關資訊
            memberName: registration.memberName || '未知會員', // 對應 DTO 的 memberName
            memberPhone: registration.phone || 'N/A', // 對應 DTO 的 phone
            
            // 重點註解：報名相關資訊
            people: registration.people || 1, // 對應 DTO 的 people
            num: registration.people || 1, // 為前端相容性保留
            peopleCount: registration.people || 1, // 為前端相容性保留
            
            // 重點註解：時間相關資訊
            scheduledAt: registration.scheduledAt, // 對應 DTO 的 scheduledAt (LocalDateTime)
            registrationTime: registration.scheduledAt, // 為前端相容性保留
            
            // 重點註解：狀態資訊
            status: registration.status || '預約審核中', // 對應 DTO 的 status
            
            // 重點註解：為前端元件相容性保留的欄位別名
            name: registration.memberName || '未知會員',
            phone: registration.phone || 'N/A',
            
            // 重點註解：原始資料，以便需要時參考
            _raw: registration
        };
    },

    /**
     * 格式化活動類別資料 - 根據後端 ActivityCategory 模型
     * @param {Object} category - API 回傳的活動類別資料
     * @returns {Object} 轉換後的活動類別資料
     * @private
     */
    _formatCategory(category) {
        // 重點註解：檢查欄位是否存在，並進行必要的轉換
        if (!category || typeof category !== 'object') {
            return {};
        }

        // 重點註解：根據後端 ActivityCategory 模型格式化資料
        return {
            // 重點註解：基本欄位（對應後端模型）
            id: category.id,
            name: category.name,
            isActive: category.isActive !== undefined ? category.isActive : true,

            // 重點註解：為前端元件相容性保留的欄位
            value: category.id, // 提供給下拉選單使用
            label: category.name, // 提供給下拉選單使用

            // 重點註解：原始資料，以便需要時參考
            _raw: category
        };
    }
};
