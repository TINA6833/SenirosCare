// src/composables/useSchedule.js
// Composables 層：班表狀態管理和快取
import { ref, reactive, computed } from 'vue';
import { scheduleService } from '@/services/caregiverScheduleService';

/**
 * 照服員班表狀態管理 Composable
 * 負責班表資料的狀態管理和快取
 */
export function useSchedule(options = { autoLoad: false }) {
  // 響應式狀態
  const schedule = ref([]);
  const scheduleByDate = ref({});
  const caregivers = ref([]);
  const currentCaregiver = ref(null);
  const loading = ref(false);
  const error = ref(null);
  const statistics = ref({});

  // 日期範圍狀態
  const dateRange = reactive({
    startDate: '',
    endDate: '',
    currentWeekStart: '',
    currentWeekEnd: ''
  });

  // 篩選條件
  const filters = reactive({
    caregiverId: null,
    viewType: 'week', // 'week', 'month', 'day'
    showBlockedSlots: true,
    showCustomerAppointments: true,
    statusFilter: '' // 狀態篩選
  });

  // 快取管理
  const cache = reactive({
    lastFetched: null,
    cacheExpiry: 5 * 60 * 1000, // 5分鐘快取
    cachedData: {},
    cacheKey: ''
  });

  // 計算屬性 - 班表統計
  const stats = computed(() => {
    const defaultStats = {
      serviceHours: 0,
      customerAppointments: 0,
      blockedSlots: 0,
      completedAppointments: 0,
      pendingAppointments: 0,
      totalRevenue: 0,
      workDays: 0
    };

    // 先嘗試使用服務端返回的統計數據
    if (statistics.value && Object.keys(statistics.value).length > 0) {
      console.log('📊 使用服務端統計數據:', statistics.value);
      return { ...defaultStats, ...statistics.value };
    }

    // 如果服務端沒有統計數據，則本地計算
    if (!schedule.value || schedule.value.length === 0) {
      console.log('📊 沒有班表數據，返回默認統計');
      return defaultStats;
    }

    console.log('📊 開始本地統計計算，班表數據:', {
      '班表項目數': schedule.value.length,
      '前3個項目': schedule.value.slice(0, 3)
    });

    // 本地統計計算
    let serviceHours = 0; // 改名為服務時數，只計算客戶預約
    let customerAppointments = 0;
    let blockedSlots = 0;
    let completedAppointments = 0;
    let pendingAppointments = 0;
    let totalRevenue = 0;
    const workDaysSet = new Set();

    // 遍歷所有預約進行統計
    schedule.value.forEach(appointment => {
      // 只計算客戶預約的服務時數（排除休息時間等）
      if (appointment.duration && !appointment.isBlocked) {
        serviceHours += appointment.duration;
      }

      // 統計預約類型
      if (appointment.isBlocked) {
        blockedSlots++;
      } else {
        customerAppointments++;
      }

      // 統計預約狀態
      if (appointment.status === '已完成') {
        completedAppointments++;
      } else if (appointment.status === '已確認' || appointment.status === '待確認') {
        pendingAppointments++;
      }

      // 累加收入（如果有）
      if (appointment.price) {
        totalRevenue += appointment.price;
      }

      // 統計工作天數
      if (appointment.date) {
        workDaysSet.add(appointment.date);
      }
    });

    const calculatedStats = {
      totalHours: Math.round(serviceHours * 10) / 10, // 只計算客戶服務時數，保留一位小數
      customerAppointments,
      blockedSlots,
      completedAppointments,
      pendingAppointments,
      totalRevenue,
      workDays: workDaysSet.size
    };

    console.log('📊 統計計算結果:', {
      '原始班表數量': schedule.value.length,
      '客戶服務時數': serviceHours,
      '統計結果': calculatedStats,
      '範例預約': schedule.value.slice(0, 2).map(apt => ({
        title: apt.title,
        duration: apt.duration,
        isBlocked: apt.isBlocked,
        status: apt.status,
        date: apt.date
      }))
    });

    return calculatedStats;
  });

  // 計算屬性 - 過濾後的班表
  const filteredSchedule = computed(() => {
    let result = [...schedule.value];

    // 根據顯示設定過濾
    if (!filters.showBlockedSlots) {
      result = result.filter(item => !item.isBlocked);
    }
    
    if (!filters.showCustomerAppointments) {
      result = result.filter(item => item.isBlocked);
    }

    // 根據狀態過濾
    if (filters.statusFilter) {
      result = result.filter(item => item.status === filters.statusFilter);
    }

    return result;
  });

  // 計算屬性 - 過濾後按日期分組的班表
  const filteredScheduleByDate = computed(() => {
    const filtered = {};
    const originalData = scheduleByDate.value;

    Object.keys(originalData).forEach(date => {
      const daySchedule = originalData[date].filter(item => {
        let show = true;
        
        if (!filters.showBlockedSlots && item.isBlocked) {
          show = false;
        }
        
        if (!filters.showCustomerAppointments && !item.isBlocked) {
          show = false;
        }

        if (filters.statusFilter && item.status !== filters.statusFilter) {
          show = false;
        }

        return show;
      });

      if (daySchedule.length > 0) {
        filtered[date] = daySchedule;
      }
    });

    return filtered;
  });

  /**
   * 載入照服員班表
   * @param {number} caregiverId - 照服員 ID
   * @param {string} startDate - 開始日期
   * @param {string} endDate - 結束日期
   * @param {boolean} forceRefresh - 是否強制重新載入
   */
  const loadSchedule = async (caregiverId, startDate, endDate, forceRefresh = false) => {
    if (!caregiverId || !startDate || !endDate) {
      throw new Error('照服員 ID、開始日期和結束日期為必填');
    }

    // 🔧 修正：新增虛擬訂單後強制重新載入，避免快取問題
    const cacheKey = `${caregiverId}-${startDate}-${endDate}`;
    const isCacheDataValid = isCacheValid(cacheKey) && cache.cachedData[cacheKey];
    
    // 🔧 修正：檢查快取資料是否有效且不為空
    if (!forceRefresh && isCacheDataValid) {
      const cachedData = cache.cachedData[cacheKey];
      
      // 檢查快取資料是否包含有效內容
      const hasValidData = cachedData.schedule && 
                          cachedData.scheduleByDate && 
                          (cachedData.schedule.length > 0 || Object.keys(cachedData.scheduleByDate).length > 0);
      
      if (hasValidData) {
        console.log('✅ 使用快取的班表資料 - 資料有效');
        schedule.value = cachedData.schedule;
        scheduleByDate.value = cachedData.scheduleByDate;
        statistics.value = cachedData.statistics;
        
        // 🔧 修正：即使使用快取，也要更新日期範圍
        dateRange.startDate = startDate;
        dateRange.endDate = endDate;
        console.log('📅 快取資料載入完成，dateRange 已更新:', { startDate, endDate });
        
        return;
      } else {
        console.log('⚠️ 快取資料為空，將重新載入');
      }
    }

    loading.value = true;
    error.value = null;

    try {
      console.log('🔄 載入照服員班表:', { caregiverId, startDate, endDate, forceRefresh });

      const data = await scheduleService.getCaregiverSchedule(caregiverId, startDate, endDate);
      
      console.log('📊 後端返回的班表資料:', {
        scheduleCount: data.schedule?.length || 0,
        scheduleDatesCount: Object.keys(data.scheduleByDate || {}).length,
        scheduleDates: Object.keys(data.scheduleByDate || {}),
        scheduleItems: data.schedule?.map(item => ({
          id: item.id,
          title: item.title,
          date: item.start?.split('T')[0],
          isBlocked: item.isBlocked
        })) || []
      });
      
      // 更新狀態
      schedule.value = data.schedule;
      scheduleByDate.value = data.scheduleByDate;
      statistics.value = data.statistics;
      
      // 更新日期範圍
      dateRange.startDate = startDate;
      dateRange.endDate = endDate;
      
      // 更新當前照服員
      filters.caregiverId = caregiverId;

      // 🔧 修正：清除舊快取，確保資料是最新的
      if (forceRefresh) {
        cache.cachedData = {};
        console.log('📅 強制重新載入，已清除舊快取');
      }

      // 更新快取
      updateCache(cacheKey, {
        schedule: data.schedule,
        scheduleByDate: data.scheduleByDate,
        statistics: data.statistics
      });

      console.log(`✅ 班表載入完成: 共 ${data.schedule.length} 筆預約, ${Object.keys(data.scheduleByDate).length} 個工作日`);

    } catch (err) {
      error.value = err.message || '載入班表失敗';
      console.error('載入班表錯誤:', err);
      throw err;
    } finally {
      loading.value = false;
    }
  };

  /**
   * 載入當週班表 - 修正版本
   * @param {number} caregiverId - 照服員 ID
   * @param {Date} baseDate - 基準日期 (預設為今天)
   * @param {boolean} forceRefresh - 是否強制重新載入
   */
  const loadWeekSchedule = async (caregiverId, baseDate = new Date(), forceRefresh = false) => {
    console.log('📅 載入週班表:', { caregiverId, baseDate: baseDate.toISOString(), forceRefresh });
    
    const { startDate, endDate } = getWeekRange(baseDate);
    
    console.log('📅 週範圍:', { startDate, endDate });
    
    dateRange.currentWeekStart = startDate;
    dateRange.currentWeekEnd = endDate;
    
    // 🔧 修正：傳遞 forceRefresh 參數，確保新增訂單後能重新載入
    await loadSchedule(caregiverId, startDate, endDate, forceRefresh);
  };

  /**
   * 載入當月班表 - 修正版本
   * @param {number} caregiverId - 照服員 ID
   * @param {Date} baseDate - 基準日期 (預設為今天)
   * @param {boolean} forceRefresh - 是否強制重新載入
   */
  const loadMonthSchedule = async (caregiverId, baseDate = new Date(), forceRefresh = false) => {
    console.log('📅 載入月班表:', { caregiverId, baseDate: baseDate.toISOString(), forceRefresh });
    
    const { startDate, endDate } = getMonthRange(baseDate);
    
    console.log('📅 月範圍:', { startDate, endDate });
    console.log('📅 目標月份:', baseDate.getFullYear(), '年', baseDate.getMonth() + 1, '月');
    
    // 🔧 修正：傳遞 forceRefresh 參數，確保新增訂單後能重新載入
    await loadSchedule(caregiverId, startDate, endDate, forceRefresh);
    
    console.log('📅 載入完成後 dateRange:', dateRange.startDate, '到', dateRange.endDate);
  };

  /**
   * 載入單日班表
   * @param {number} caregiverId - 照服員 ID
   * @param {Date} baseDate - 目標日期 (預設為今天)
   * @param {boolean} forceRefresh - 是否強制重新載入
   */
  const loadDaySchedule = async (caregiverId, baseDate = new Date(), forceRefresh = false) => {
    // 🔧 修正：日檢視總是強制重新載入，避免快取問題
    const actualForceRefresh = true;
    
    console.log('📅 載入日班表:', { 
      caregiverId, 
      baseDate: baseDate.toISOString(), 
      originalForceRefresh: forceRefresh,
      actualForceRefresh 
    });
    
    const { startDate, endDate } = getDayRange(baseDate);
    
    console.log('📅 日範圍:', { startDate, endDate });
    console.log('📅 日檢視強制重新載入，避免快取問題');
    
    // 傳遞強制重新載入參數
    await loadSchedule(caregiverId, startDate, endDate, actualForceRefresh);
  };

  /**
   * 載入照服員列表
   */
  const loadCaregivers = async () => {
    loading.value = true;
    error.value = null;

    try {
      console.log('🔄 載入照服員列表');
      const data = await scheduleService.getAllCaregivers();
      caregivers.value = data;
    } catch (err) {
      error.value = err.message || '載入照服員列表失敗';
      console.error('載入照服員列表錯誤:', err);
      throw err;
    } finally {
      loading.value = false;
    }
  };

  /**
   * 設定當前照服員
   * @param {number} caregiverId - 照服員 ID
   */
  const setCurrentCaregiver = async (caregiverId) => {
    const caregiver = caregivers.value.find(c => c.id === parseInt(caregiverId));
    if (caregiver) {
      currentCaregiver.value = caregiver;
      filters.caregiverId = caregiverId;
    } else {
      // 如果列表中沒有找到，嘗試從伺服器獲取
      try {
        const caregiverInfo = await scheduleService.getCaregiverInfo(caregiverId);
        currentCaregiver.value = caregiverInfo;
        filters.caregiverId = caregiverId;
      } catch (err) {
        console.error('獲取照服員資訊失敗:', err);
        throw new Error('找不到指定的照服員');
      }
    }
  };

  /**
   * 檢查時間衝突
   * @param {number} caregiverId - 照服員 ID
   * @param {string} startTime - 開始時間
   * @param {string} endTime - 結束時間
   * @param {number|null} excludeId - 排除的預約 ID
   */
  const checkTimeConflict = async (caregiverId, startTime, endTime, excludeId = null) => {
    try {
      return await scheduleService.checkTimeConflict(caregiverId, startTime, endTime, excludeId);
    } catch (err) {
      console.error('檢查時間衝突錯誤:', err);
      throw err;
    }
  };

  /**
   * 獲取可用時間段
   * @param {number} caregiverId - 照服員 ID
   * @param {string} date - 日期 (YYYY-MM-DD)
   */
  const getAvailableTimeSlots = async (caregiverId, date) => {
    try {
      return await scheduleService.getAvailableTimeSlots(caregiverId, date);
    } catch (err) {
      console.error('獲取可用時間段錯誤:', err);
      throw err;
    }
  };

  /**
   * 切換檢視類型 - 修正版本
   * @param {string} viewType - 檢視類型 ('week', 'month', 'day')
   */
  const changeViewType = (viewType) => {
    console.log('⚙️ Composable - 切換檢視類型:', viewType, '→', filters.viewType);
    
    // 更新檢視類型
    filters.viewType = viewType;
    
    // 清空當前日期範圍，讓新的檢視類型重新計算
    dateRange.currentWeekStart = '';
    dateRange.currentWeekEnd = '';
    
    console.log('✅ 檢視類型已更新為:', filters.viewType);
  };

  /**
   * 重置篩選條件
   */
  const resetFilters = () => {
    filters.showBlockedSlots = true;
    filters.showCustomerAppointments = true;
    filters.statusFilter = '';
  };

  /**
   * 獲取週範圍 - 修正版本，使用台灣時區
   * @private
   */
  function getWeekRange(date) {
    const d = new Date(date);
    const day = d.getDay();
    
    // 🔧 修正：確保週一開始，並正確處理跨月的情況，使用台灣時區
    const diff = d.getDate() - day + (day === 0 ? -6 : 1); // 週一開始
    
    const monday = new Date(d.getFullYear(), d.getMonth(), diff);
    const sunday = new Date(monday);
    sunday.setDate(monday.getDate() + 6);

    // 使用台灣時區格式化日期
    const startDate = monday.toLocaleDateString('zh-TW', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit'
    }).replace(/\//g, '-');
    
    const endDate = sunday.toLocaleDateString('zh-TW', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit'
    }).replace(/\//g, '-');

    console.log('📅 週範圍計算:', {
      inputDate: date.toISOString(),
      dayOfWeek: day,
      monday: monday.toISOString(),
      sunday: sunday.toISOString(),
      startDate,
      endDate,
      mondayLocal: monday.toLocaleString('zh-TW'),
      sundayLocal: sunday.toLocaleString('zh-TW')
    });

    return { startDate, endDate };
  }

  /**
   * 獲取月範圍 - 使用台灣時區
   * @private
   */
  function getMonthRange(date) {
    const d = new Date(date);
    const firstDay = new Date(d.getFullYear(), d.getMonth(), 1);
    const lastDay = new Date(d.getFullYear(), d.getMonth() + 1, 0);

    // 使用台灣時區格式化日期
    const startDate = firstDay.toLocaleDateString('zh-TW', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit'
    }).replace(/\//g, '-');
    
    const endDate = lastDay.toLocaleDateString('zh-TW', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit'
    }).replace(/\//g, '-');

    return { startDate, endDate };
  }

  /**
   * 獲取單日範圍 - 使用台灣時區
   * @private
   */
  function getDayRange(date) {
    const d = new Date(date);
    
    // 使用台灣時區格式化日期
    const dateString = d.toLocaleDateString('zh-TW', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit'
    }).replace(/\//g, '-');

    console.log('📅 getDayRange 輸入日期:', date);
    console.log('📅 getDayRange 格式化結果:', dateString);

    return { 
      startDate: dateString, 
      endDate: dateString 
    };
  }

  /**
   * 檢查快取是否有效
   * @private
   */
  function isCacheValid(cacheKey) {
    if (!cache.lastFetched || !cache.cachedData[cacheKey]) {
      return false;
    }

    const now = new Date();
    const isExpired = (now - cache.lastFetched) > cache.cacheExpiry;

    return !isExpired;
  }

  /**
   * 更新快取
   * @private
   */
  function updateCache(cacheKey, data) {
    cache.lastFetched = new Date();
    cache.cachedData[cacheKey] = { ...data };
    cache.cacheKey = cacheKey;
  }

  // 自動載入 (如果開啟選項)
  if (options.autoLoad) {
    loadCaregivers();
  }

  /**
   * 清除所有快取資料
   */
  const clearAllCache = () => {
    console.log('🗑️ 清除所有快取資料');
    cache.cachedData = {};
    cache.lastFetched = null;
    cache.cacheKey = '';
  };

  /**
   * 重新載入當前班表 - 新增方法
   * 用於新增虛擬訂單後強制重新載入班表資料
   * @param {boolean} preserveViewType - 是否保持當前檢視類型 (預設: true)
   */
  const refreshSchedule = async (preserveViewType = true) => {
    if (!filters.caregiverId) {
      console.log('沒有選擇照服員，無法重新載入班表');
      return;
    }

    try {
      console.log('🔄 強制重新載入班表資料:', {
        caregiverId: filters.caregiverId,
        viewType: filters.viewType,
        preserveViewType
      });

      // 🔧 清除所有快取，確保獲取最新資料
      cache.cachedData = {};
      cache.lastFetched = null;

      // 根據當前檢視類型重新載入
      const currentDate = new Date();
      switch (filters.viewType) {
        case 'week':
          await loadWeekSchedule(filters.caregiverId, currentDate, true);
          break;
        case 'month':
          await loadMonthSchedule(filters.caregiverId, currentDate, true);
          break;
        case 'day':
          await loadDaySchedule(filters.caregiverId, currentDate, true);
          break;
        default:
          // 預設載入週檢視
          await loadWeekSchedule(filters.caregiverId, currentDate, true);
          break;
      }

      console.log('✅ 班表資料重新載入完成:', {
        totalAppointments: schedule.value.length,
        workDays: Object.keys(scheduleByDate.value).length
      });

    } catch (error) {
      console.error('❌ 重新載入班表失敗:', error);
      throw error;
    }
  };

  return {
    // 狀態
    schedule,
    scheduleByDate,
    caregivers,
    currentCaregiver,
    loading,
    error,
    statistics,
    dateRange,
    filters,

    // 計算屬性
    stats,
    filteredSchedule,
    filteredScheduleByDate,

    // 方法
    loadSchedule,
    loadWeekSchedule,
    loadMonthSchedule,
    loadDaySchedule,
    loadCaregivers,
    setCurrentCaregiver,
    checkTimeConflict,
    getAvailableTimeSlots,
    changeViewType,
    resetFilters,
    refreshSchedule,  // 🔧 新增：重新載入方法
    clearAllCache    // 🔧 新增：清除快取方法
  };
}