import { ref, reactive, computed } from 'vue';
import { caregiverService } from '@/services/caregiverService';

/**
 * 照服員狀態管理 Composable
 * 負責照服員資料的狀態管理和快取
 */
export function useCaregivers(options = {}) {
  // 響應式狀態
  const caregivers = ref([]);
  const loading = ref(false);
  const error = ref(null);
  const currentCaregiver = ref(null);

  // 篩選條件
  const filters = reactive({
    isActive: null,        // null: 全部, true: 在職, false: 離職
    name: '',              // 姓名搜尋
    serviceArea: ''        // 服務區域搜尋
  });

  // 計算屬性 - 統計資料
  const stats = computed(() => ({
    total: caregivers.value.length,
    active: caregivers.value.filter(c => c.isActive).length,
    inactive: caregivers.value.filter(c => !c.isActive).length,
    averageExperience: caregivers.value.length > 0 
      ? (caregivers.value.reduce((sum, c) => sum + c.experienceYears, 0) / caregivers.value.length).toFixed(1)
      : 0
  }));

  // 計算屬性 - 篩選後的照服員列表
  const filteredCaregivers = computed(() => {
    let result = [...caregivers.value];

    // 按狀態篩選
    if (filters.isActive !== null) {
      result = result.filter(caregiver => caregiver.isActive === filters.isActive);
    }

    // 按姓名篩選 (本地篩選，用於即時搜尋)
    if (filters.name.trim()) {
      const searchTerm = filters.name.trim().toLowerCase();
      result = result.filter(caregiver => 
        caregiver.chineseName.toLowerCase().includes(searchTerm)
      );
    }

    // 按服務區域篩選
    if (filters.serviceArea.trim()) {
      const searchArea = filters.serviceArea.trim().toLowerCase();
      result = result.filter(caregiver => 
        caregiver.serviceArea && caregiver.serviceArea.toLowerCase().includes(searchArea)
      );
    }

    return result;
  });

  /**
   * 載入照服員列表
   * @param {boolean} forceRefresh - 是否強制重新載入
   */
  const loadCaregivers = async (forceRefresh = false) => {
    // 如果已有資料且不強制重新載入，直接返回
    if (caregivers.value.length > 0 && !forceRefresh) {
      return;
    }

    loading.value = true;
    error.value = null;

    try {
      const data = await caregiverService.getCaregivers();
      caregivers.value = data;
    } catch (err) {
      error.value = err.message || '載入照服員資料失敗';
      console.error('載入照服員錯誤:', err);
    } finally {
      loading.value = false;
    }
  };

  // 如果設定了 autoLoad，則自動載入資料
  if (options.autoLoad) {
    loadCaregivers();
  }

  /**
   * 根據篩選條件搜尋照服員
   */
  const searchCaregivers = async () => {
    loading.value = true;
    error.value = null;

    try {
      const searchFilters = {
        isActive: filters.isActive,
        name: filters.name.trim() || null,
        serviceArea: filters.serviceArea.trim() || null
      };

      const data = await caregiverService.getCaregivers(searchFilters);
      caregivers.value = data;
    } catch (err) {
      error.value = err.message || '搜尋照服員失敗';
      console.error('搜尋照服員錯誤:', err);
    } finally {
      loading.value = false;
    }
  };

  /**
   * 根據 ID 載入照服員詳細資料
   * @param {number} id - 照服員 ID
   */
  const loadCaregiverById = async (id) => {
    loading.value = true;
    error.value = null;

    try {
      console.log('開始載入照服員詳細資料, ID:', id);
      const data = await caregiverService.getCaregiverById(id);
      console.log('載入的照服員資料:', data);
      
      // 檢查照片欄位
      if (data.photo) {
        console.log('照服員照片路徑:', data.photo);
      } else {
        console.log('照服員沒有照片資訊');
      }
      
      currentCaregiver.value = data;
      return data;
    } catch (err) {
      error.value = err.message || '載入照服員詳細資料失敗';
      console.error('載入照服員詳細資料錯誤:', err);
      throw err;
    } finally {
      loading.value = false;
    }
  };

  /**
   * 新增照服員
   * @param {Object} caregiverData - 照服員資料
   */
  const createCaregiver = async (caregiverData) => {
    loading.value = true;
    error.value = null;

    try {
      const newCaregiver = await caregiverService.createCaregiver(caregiverData);
      
      // 更新本地狀態
      caregivers.value.unshift(newCaregiver);
      
      return newCaregiver;
    } catch (err) {
      error.value = err.message || '新增照服員失敗';
      console.error('新增照服員錯誤:', err);
      throw err;
    } finally {
      loading.value = false;
    }
  };

  /**
   * 更新照服員資料
   * @param {number} id - 照服員 ID
   * @param {Object} caregiverData - 更新的資料
   */
  const updateCaregiver = async (id, caregiverData) => {
    loading.value = true;
    error.value = null;

    try {
      const updatedCaregiver = await caregiverService.updateCaregiver(id, caregiverData);
      
      // 更新本地狀態
      const index = caregivers.value.findIndex(c => c.id === id);
      if (index !== -1) {
        caregivers.value[index] = updatedCaregiver;
      }

      // 如果是當前檢視的照服員，也更新並確保評價資訊正確
      if (currentCaregiver.value && currentCaregiver.value.id === id) {
        currentCaregiver.value = {
          ...updatedCaregiver,
          // 確保評價資訊不會遺失
          averageRating: updatedCaregiver.averageRating || 0,
          totalRatings: updatedCaregiver.totalRatings || 0,
          totalPoints: updatedCaregiver.totalPoints || 0
        };
        console.log('currentCaregiver 更新後的評價資訊:', {
          averageRating: currentCaregiver.value.averageRating,
          totalRatings: currentCaregiver.value.totalRatings,
          totalPoints: currentCaregiver.value.totalPoints
        });
      }

      return updatedCaregiver;
    } catch (err) {
      error.value = err.message || '更新照服員失敗';
      console.error('更新照服員錯誤:', err);
      throw err;
    } finally {
      loading.value = false;
    }
  };

 /**
 * 刪除照服員
 * @param {number} id - 照服員 ID
 * @returns {Promise<boolean>} 刪除結果
 */
const deleteCaregiver = async (id) => {
  loading.value = true;
  error.value = null;

  try {
    const success = await caregiverService.deleteCaregiver(id);
    
    if (success) {
      // 從本地狀態中移除
      caregivers.value = caregivers.value.filter(c => c.id !== id);
      
      // 如果是當前檢視的照服員，清空
      if (currentCaregiver.value && currentCaregiver.value.id === id) {
        currentCaregiver.value = null;
      }
      
      return true;
    } else {
      error.value = '刪除照服員失敗';
      return false;
    }
  } catch (err) {
    error.value = err.message || '刪除照服員失敗';
    console.error('刪除照服員錯誤:', err);
    return false;
  } finally {
    loading.value = false;
  }
};

  /**
   * 重置篩選條件
   */
  const resetFilters = () => {
    filters.isActive = null;
    filters.name = '';
    filters.serviceArea = '';
  };

  /**
   * 清除錯誤狀態
   */
  const clearError = () => {
    error.value = null;
  };

  return {
    // 狀態
    caregivers,
    loading,
    error,
    currentCaregiver,
    filters,
    
    // 計算屬性
    stats,
    filteredCaregivers,
    
    // 方法
    loadCaregivers,
    searchCaregivers,
    loadCaregiverById,
    createCaregiver,
    updateCaregiver,
    deleteCaregiver,
    resetFilters,
    clearError
  };
}