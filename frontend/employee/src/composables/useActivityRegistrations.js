import { ref } from 'vue';
import { activityRegistrationService } from '@/services/activityRegistrationService';

/**
 * 活動報名管理 Composable - 管理報名狀態與操作
 * @returns {Object} 報名資料管理功能
 */
export function useActivityRegistrations() {
    // === 響應式狀態 ===
    const registrations = ref([]); // 報名列表
    const categories = ref([]); // 活動類別列表
    const loading = ref(false); // 載入狀態
    const error = ref(''); // 錯誤訊息

    // === 類別管理功能 ===
    /**
     * 載入活動類別列表
     * @returns {Promise<Array>} 回傳活動類別陣列
     */
    const loadCategories = async () => {
        loading.value = true;
        error.value = '';

        try {
            console.log('開始載入活動類別列表...');
            const data = await activityRegistrationService.getActiveCategories();
            categories.value = data || [];
            console.log(`成功載入 ${categories.value.length} 個活動類別`);
            return categories.value;
        } catch (err) {
            console.error('載入活動類別失敗:', err);
            error.value = err.message || '載入活動類別失敗';
            categories.value = [];
            throw err;
        } finally {
            loading.value = false;
        }
    };

    /**
     * 重點註解：取得活動類別選項 - 格式化為下拉選單使用
     * @returns {Array} 活動類別選項陣列
     */
    const getCategoryOptions = () => {
        return categories.value.map(category => ({
            value: category.name, // 重點註解：使用類別名稱作為值，因為後端查詢參數是 CategoryName
            label: category.name,
            id: category.id,
            disabled: !category.isActive
        }));
    };

    // === 查詢功能 ===
    /**
     * 重點註解：根據活動名稱載入報名資料 - 使用新的查詢參數格式
     * @param {string} activityName - 活動名稱
     * @returns {Promise<Array>} 回傳報名資料陣列
     */
    const loadRegistrationsByActivityName = async (activityName) => {
        loading.value = true;
        error.value = '';

        try {
            console.log('開始載入活動報名資料 (依活動名稱):', activityName);
            const data = await activityRegistrationService.getRegistrationsByActivityName(activityName);
            registrations.value = data || [];
            console.log(`成功載入 ${registrations.value.length} 筆報名資料`);
            return registrations.value;
        } catch (err) {
            console.error('載入活動報名資料失敗:', err);
            error.value = err.message || '載入活動報名資料失敗';
            registrations.value = [];
            throw err;
        } finally {
            loading.value = false;
        }
    };

    /**
     * 重點註解：根據類別名稱載入報名資料 - 使用新的查詢參數格式
     * @param {string} categoryName - 類別名稱
     * @returns {Promise<Array>} 回傳報名資料陣列
     */
    const loadRegistrationsByCategoryName = async (categoryName) => {
        loading.value = true;
        error.value = '';

        try {
            console.log('開始載入報名資料 (依類別名稱):', categoryName);
            const data = await activityRegistrationService.getRegistrationsByCategoryName(categoryName);
            registrations.value = data || [];
            console.log(`成功載入 ${registrations.value.length} 筆報名資料`);
            return registrations.value;
        } catch (err) {
            console.error('載入類別報名資料失敗:', err);
            error.value = err.message || '載入類別報名資料失敗';
            registrations.value = [];
            throw err;
        } finally {
            loading.value = false;
        }
    };

    /**
     * 載入指定會員的報名資料
     * @param {number} memberId - 會員ID
     * @returns {Promise<Array>} 回傳報名資料陣列
     */
    const loadMemberRegistrations = async (memberId) => {
        loading.value = true;
        error.value = '';

        try {
            console.log('開始載入會員報名資料:', memberId);
            const data = await activityRegistrationService.getRegistrationsByMemberId(memberId);
            registrations.value = data || [];
            console.log(`成功載入 ${registrations.value.length} 筆會員報名資料`);
            return registrations.value;
        } catch (err) {
            console.error('載入會員報名資料失敗:', err);
            error.value = err.message || '載入會員報名資料失敗';
            registrations.value = [];
            throw err;
        } finally {
            loading.value = false;
        }
    };

    /**
     * 重點註解：載入所有報名資料或根據條件篩選 - 使用新的查詢參數格式
     * @param {Object} queryParams - 查詢參數 (可選)
     * @param {string} queryParams.CategoryName - 類別名稱 (可選)
     * @param {string} queryParams.activityName - 活動名稱 (可選)
     * @param {number} queryParams.memberId - 會員ID (可選)
     * @returns {Promise<Array>} 回傳報名資料陣列
     */
    const loadAllRegistrations = async (queryParams = {}) => {
        loading.value = true;
        error.value = '';

        try {
            console.log('開始載入所有報名資料:', queryParams);
            const data = await activityRegistrationService.getAllReservations(queryParams);
            registrations.value = data || [];
            console.log(`成功載入 ${registrations.value.length} 筆報名資料`);
            return registrations.value;
        } catch (err) {
            console.error('載入報名資料失敗:', err);
            error.value = err.message || '載入報名資料失敗';
            registrations.value = [];
            throw err;
        } finally {
            loading.value = false;
        }
    };

    // === 審核功能 ===
    /**
     * 審核通過報名
     * @param {number} activityId - 活動ID
     * @param {number} memberId - 會員ID
     * @returns {Promise<boolean>} 審核成功返回 true
     */
    const approveRegistration = async (activityId, memberId) => {
        try {
            console.log(`開始審核通過報名 (活動ID: ${activityId}, 會員ID: ${memberId})`);
            const result = await activityRegistrationService.reviewReservation(activityId, memberId, '報名成功');

            if (result.success) {
                console.log('審核通過成功');
                // 重點註解：重新載入資料以更新狀態
                await loadAllRegistrations();
                return true;
            } else {
                console.error('審核通過失敗:', result.message);
                error.value = result.message;
                return false;
            }
        } catch (err) {
            console.error('審核通過時發生錯誤:', err);
            error.value = err.message || '審核通過時發生錯誤';
            return false;
        }
    };

    /**
     * 取消報名
     * @param {number} activityId - 活動ID
     * @param {number} memberId - 會員ID
     * @returns {Promise<boolean>} 取消成功返回 true
     */
    const cancelRegistration = async (activityId, memberId) => {
        try {
            console.log(`開始取消報名 (活動ID: ${activityId}, 會員ID: ${memberId})`);
            const result = await activityRegistrationService.reviewReservation(activityId, memberId, '已取消');

            if (result.success) {
                console.log('取消報名成功');
                // 重點註解：重新載入資料以更新狀態
                await loadAllRegistrations();
                return true;
            } else {
                console.error('取消報名失敗:', result.message);
                error.value = result.message;
                return false;
            }
        } catch (err) {
            console.error('取消報名時發生錯誤:', err);
            error.value = err.message || '取消報名時發生錯誤';
            return false;
        }
    };

    // 重點註解：為了向下相容性保留的方法別名
    /**
     * @deprecated 請使用 loadRegistrationsByActivityName
     */
    const loadRegistrations = loadRegistrationsByActivityName;

    return {
        // === 狀態 ===
        registrations,      // 報名列表
        categories,         // 活動類別列表
        loading,            // 載入狀態
        error,              // 錯誤訊息

        // === 方法 ===
        loadCategories,                     // 載入活動類別列表
        getCategoryOptions,                 // 取得活動類別選項
        loadRegistrationsByActivityName,    // 根據活動名稱載入報名資料
        loadRegistrationsByCategoryName,    // 根據類別名稱載入報名資料
        loadMemberRegistrations,            // 載入會員報名資料
        loadAllRegistrations,               // 載入所有報名資料
        approveRegistration,                // 審核通過報名
        cancelRegistration,                 // 取消報名

        // === 向下相容性別名 ===
        loadRegistrations                   // @deprecated 請使用 loadRegistrationsByActivityName
    };
}
