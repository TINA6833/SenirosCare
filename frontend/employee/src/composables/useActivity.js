import { ref, reactive } from 'vue';
import { activityService } from '@/services/activityService';

/**
 * 活動相關的 Composable - 提供狀態管理和方法
 * @returns {Object} 活動相關的狀態和方法
 */
export function useActivity() {
    // === 響應式狀態 ===
    const activities = ref([]); // 活動列表
    const loading = ref(false); // 載入狀態
    const error = ref(''); // 錯誤訊息
    const success = ref(''); // 成功訊息

    // === 載入活動列表 ===
    /**
     * 載入所有活動
     * @returns {Promise<Array>} 回傳活動陣列
     */
    const loadActivities = async () => {
        loading.value = true;
        error.value = '';
        success.value = '';

        try {
            console.log('開始載入活動列表...');
            const data = await activityService.getActivities();
            activities.value = data || [];
            console.log(`成功載入 ${activities.value.length} 筆活動`);
            return activities.value; // 直接回傳活動資料
        } catch (err) {
            console.error('載入活動列表失敗:', err);
            error.value = err.message || '載入活動列表失敗';
            activities.value = [];
            throw err; // 拋出錯誤讓呼叫者處理
        } finally {
            loading.value = false;
        }
    };

    /**
     * 取得活動列表 - 提供別名方法以保持向下相容性
     * @returns {Promise<Array>} 回傳活動陣列
     */
    const getActivities = async () => {
        return await loadActivities();
    };

    // === 新增活動 ===
    /**
     * 新增活動
     * @param {Object} activityData - 活動資料
     * @returns {Promise<boolean>} 新增成功返回 true
     */
    const addActivity = async (activityData) => {
        loading.value = true;
        error.value = '';
        success.value = '';

        try {
            console.log('開始新增活動:', activityData);
            const result = await activityService.addActivity(activityData);

            if (result.success) {
                success.value = result.message || '活動新增成功';
                console.log('活動新增成功:', result.data);

                // 重新載入活動列表以取得最新資料
                await loadActivities();

                return true;
            } else {
                error.value = result.message || '新增活動失敗';
                console.error('新增活動失敗:', result.message);
                return false;
            }
        } catch (err) {
            console.error('新增活動時發生錯誤:', err);
            error.value = err.message || '新增活動時發生錯誤';
            return false;
        } finally {
            loading.value = false;
        }
    };

    // === 更新活動 ===
    /**
     * 更新活動
     * @param {number} id - 活動ID
     * @param {Object} activityData - 更新的活動資料
     * @returns {Promise<boolean>} 更新成功返回 true
     */
    const updateActivity = async (id, activityData) => {
        loading.value = true;
        error.value = '';
        success.value = '';

        try {
            console.log(`開始更新活動 ID: ${id}`, activityData);
            const result = await activityService.updateActivity(id, activityData);

            if (result.success) {
                success.value = result.message || '活動更新成功';
                console.log('活動更新成功:', result.data);

                // 重新載入活動列表以取得最新資料
                await loadActivities();

                return true;
            } else {
                error.value = result.message || '更新活動失敗';
                console.error('更新活動失敗:', result.message);
                return false;
            }
        } catch (err) {
            console.error('更新活動時發生錯誤:', err);
            error.value = err.message || '更新活動時發生錯誤';
            return false;
        } finally {
            loading.value = false;
        }
    };

    // === 刪除活動 ===
    /**
     * 刪除活動
     * @param {number} id - 活動ID
     * @returns {Promise<boolean>} 刪除成功返回 true
     */
    const removeActivity = async (id) => {
        loading.value = true;
        error.value = '';
        success.value = '';

        try {
            console.log(`開始刪除活動 ID: ${id}`);
            const result = await activityService.deleteActivity(id);

            if (result.success) {
                success.value = result.message || '活動刪除成功';
                console.log('活動刪除成功');

                // 重新載入活動列表以取得最新資料
                await loadActivities();

                return true;
            } else {
                error.value = result.message || '刪除活動失敗';
                console.error('刪除活動失敗:', result.message);
                return false;
            }
        } catch (err) {
            console.error('刪除活動時發生錯誤:', err);
            error.value = err.message || '刪除活動時發生錯誤';
            return false;
        } finally {
            loading.value = false;
        }
    };

    /**
     * 提供別名方法以保持向下相容性
     * @param {number} id - 活動ID
     * @returns {Promise<boolean>} 刪除成功返回 true
     */
    const deleteActivity = async (id) => {
        return await removeActivity(id);
    };

    // === 儲存活動標籤 ===
    /**
     * 儲存活動標籤
     * @param {number} activityId - 活動ID
     * @param {Array<string>} tags - 標籤陣列
     * @returns {Promise<boolean>} 儲存成功返回 true
     */
    const saveActivityTags = async (activityId, tags) => {
        try {
            console.log(`開始儲存活動 ${activityId} 的標籤:`, tags);
            const result = await activityService.saveActivityTags(activityId, tags);

            if (result.success) {
                console.log('活動標籤儲存成功');
                return true;
            } else {
                console.warn('儲存活動標籤失敗:', result.message);
                return false;
            }
        } catch (err) {
            console.error('儲存活動標籤時發生錯誤:', err);
            return false;
        }
    };

    // === 檢查活動狀態 ===
    /**
     * 檢查並更新過期活動的狀態
     * @returns {Promise<void>}
     */
    const checkActivitiesStatus = async () => {
        try {
            console.log('開始檢查活動狀態...');

            const today = new Date();
            today.setHours(0, 0, 0, 0);

            for (const activity of activities.value) {
                if (activity.date && activity.status) {
                    const activityEndDate = activity.endDate ? new Date(activity.endDate) : new Date(activity.date);

                    if (activityEndDate < today) {
                        console.log(`活動「${activity.name}」已過期，更新狀態為已結束`);
                        await activityService.updateActivityStatus(activity.id, false);
                    }
                }
            }

            await loadActivities();

        } catch (err) {
            console.error('檢查活動狀態時發生錯誤:', err);
        }
    };

    // === 獲取單一活動 ===
    /**
     * 根據ID獲取活動詳情
     * @param {number} id - 活動ID
     * @returns {Promise<Object|null>} 活動資料或 null
     */
    const getActivityById = async (id) => {
        try {
            console.log(`開始獲取活動 ID: ${id}`);
            const activity = await activityService.getActivityById(id);
            console.log('獲取活動詳情成功:', activity);
            return activity;
        } catch (err) {
            console.error('獲取活動詳情失敗:', err);
            error.value = err.message || '獲取活動詳情失敗';
            return null;
        }
    };

    // 返回所有狀態和方法
    return {
        // === 狀態 ===
        activities,      // 活動列表
        loading,         // 載入狀態
        error,           // 錯誤訊息
        success,         // 成功訊息

        // === 方法 ===
        loadActivities,      // 載入活動列表
        getActivities,       // 獲取活動列表（別名）
        addActivity,         // 新增活動
        updateActivity,      // 更新活動
        removeActivity,      // 刪除活動
        deleteActivity,      // 刪除活動（別名）
        saveActivityTags,    // 儲存活動標籤
        checkActivitiesStatus, // 檢查活動狀態
        getActivityById      // 獲取單一活動
    };
}