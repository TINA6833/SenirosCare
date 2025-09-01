// Composables 層：負責狀態管理和快取
import { ref, reactive, computed } from 'vue';
import { caregiverAppointmentService } from '@/services/caregiverAppointmentService';

/**
 * 預約訂單狀態管理 Composable
 * 負責預約訂單資料的狀態管理和快取
 */
export function useAppointments(options = { autoLoad: true }) {
  // 響應式狀態
  const appointments = ref([]);
  const loading = ref(false);
  const error = ref(null);
  const currentAppointment = ref(null);
  const statistics = ref({});

  // 分頁狀態
  const pagination = reactive({
    currentPage: 0,
    totalPages: 0,
    total: 0,
    size: 20
  });

  // 篩選條件
  const filters = reactive({
    status: '',              // 訂單狀態
    caregiverId: null,       // 照服員 ID
    memberId: null,          // 會員 ID
    startDate: '',           // 開始日期
    endDate: '',             // 結束日期
    isBlocked: null,         // null: 全部, true: 員工建立, false: 會員建立
    searchQuery: ''          // 搜尋關鍵字
  });

  // 快取管理
  const cache = reactive({
    lastFetched: null,
    cacheExpiry: 5 * 60 * 1000, // 5分鐘快取
    filters: {},
    appointments: []
  });

  // 計算屬性 - 訂單統計
  const stats = computed(() => {
    if (appointments.value.length === 0) {
      return {
        total: 0,
        pending: 0,
        approved: 0,
        inProgress: 0,
        completed: 0,
        cancelled: 0,
        rejected: 0
      };
    }

    return {
      total: appointments.value.length,
      pending: appointments.value.filter(apt => apt.status === 'pending').length,
      approved: appointments.value.filter(apt => apt.status === 'approved').length,
      inProgress: appointments.value.filter(apt => apt.status === 'in_progress').length,
      completed: appointments.value.filter(apt => apt.status === 'completed').length,
      cancelled: appointments.value.filter(apt => apt.status === 'cancelled').length,
      rejected: appointments.value.filter(apt => apt.status === 'rejected').length
    };
  });

  // 計算屬性 - 過濾後的訂單列表
  const filteredAppointments = computed(() => {
    let result = [...appointments.value];

    // 根據搜尋關鍵字過濾 (會員姓名或照服員姓名)
    if (filters.searchQuery.trim()) {
      const searchTerm = filters.searchQuery.trim().toLowerCase();
      result = result.filter(appointment => 
        appointment.memberName.toLowerCase().includes(searchTerm) ||
        appointment.caregiverName.toLowerCase().includes(searchTerm) ||
        appointment.serviceLocation.toLowerCase().includes(searchTerm)
      );
    }

    return result;
  });

  /**
   * 載入預約訂單列表
   * @param {boolean} forceRefresh - 是否強制重新載入
   */
  const loadAppointments = async (forceRefresh = false) => {
    // 檢查快取是否有效
    if (!forceRefresh && isCacheValid()) {
      appointments.value = cache.appointments;
      return;
    }

    loading.value = true;
    error.value = null;

    try {
      // 準備查詢參數
      const queryParams = {
        page: pagination.currentPage,
        size: pagination.size,
        ...filters
      };

      // 呼叫服務層
      const data = await caregiverAppointmentService.getAppointments(queryParams);
      
      // 更新狀態
      appointments.value = data.appointments;
      statistics.value = data.statistics;
      
      // 更新分頁資訊
      pagination.total = data.total;
      pagination.totalPages = data.totalPages;
      pagination.currentPage = data.currentPage;

      // 更新快取
      updateCache(data.appointments, queryParams);

    } catch (err) {
      error.value = err.message || '載入預約訂單失敗';
    } finally {
      loading.value = false;
    }
  };

  /**
   * 根據篩選條件搜尋預約訂單
   */
  const searchAppointments = async () => {
    // 重置分頁到第一頁
    pagination.currentPage = 0;
    await loadAppointments(true); // 強制重新載入
  };

  /**
   * 換頁
   * @param {number} page - 頁碼 (從 0 開始)
   */
  const changePage = async (page) => {
    pagination.currentPage = page;
    await loadAppointments(true);
  };

  /**
   * 載入單一預約訂單詳情
   * @param {number} id - 訂單 ID
   */
  const loadAppointmentById = async (id) => {
    loading.value = true;
    error.value = null;

    try {
      const appointment = await caregiverAppointmentService.getAppointmentById(id);
      currentAppointment.value = appointment;
      return appointment;
    } catch (err) {
      error.value = err.message || '載入訂單詳情失敗';
      throw err;
    } finally {
      loading.value = false;
    }
  };

  /**
   * 審核預約訂單
   * @param {number} id - 訂單 ID
   * @param {string} action - 審核動作 ('approve' | 'reject')
   * @param {string} notes - 審核備註
   */
  const reviewAppointment = async (id, action, notes = '') => {
    loading.value = true;
    error.value = null;

    try {
      await caregiverAppointmentService.reviewAppointment(id, action, notes);
      
      // 審核成功後重新載入列表
      await loadAppointments(true);
      return true;
    } catch (err) {
      error.value = err.message || '審核訂單失敗';
      throw err;
    } finally {
      loading.value = false;
    }
  };

  /**
   * 完成預約訂單
   * @param {number} id - 訂單 ID
   */
  const completeAppointment = async (id) => {
    loading.value = true;
    error.value = null;

    try {
      await caregiverAppointmentService.completeAppointment(id);
      
      // 完成後重新載入列表
      await loadAppointments(true);
      return true;
    } catch (err) {
      error.value = err.message || '完成訂單失敗';
      throw err;
    } finally {
      loading.value = false;
    }
  };

  /**
   * 取消預約訂單
   * @param {number} id - 訂單 ID
   * @param {string} reason - 取消原因
   */
  const cancelAppointment = async (id, reason = '') => {
    loading.value = true;
    error.value = null;

    try {
      await caregiverAppointmentService.cancelAppointment(id, reason);
      
      // 取消後重新載入列表
      await loadAppointments(true);
      return true;
    } catch (err) {
      error.value = err.message || '取消訂單失敗';
      throw err;
    } finally {
      loading.value = false;
    }
  };

  /**
   * 更新預約訂單
   * @param {number} id - 訂單 ID
   * @param {Object} updateData - 更新資料
   */
  const updateAppointment = async (id, updateData) => {
    loading.value = true;
    error.value = null;

    try {
      const result = await caregiverAppointmentService.updateAppointment(id, updateData);
      
      // 更新後重新載入列表
      await loadAppointments(true);
      return result;
    } catch (err) {
      error.value = err.message || '更新訂單失敗';
      throw err;
    } finally {
      loading.value = false;
    }
  };

  /**
   * 重置篩選條件
   */
  const resetFilters = () => {
    Object.keys(filters).forEach(key => {
      if (typeof filters[key] === 'string') {
        filters[key] = '';
      } else {
        filters[key] = null;
      }
    });
    pagination.currentPage = 0;
  };

  /**
   * 檢查快取是否有效
   * @private
   */
  const isCacheValid = () => false; // 強制不使用快取

  /**
   * 更新快取
   * @private
   */
  function updateCache(appointmentsList, queryFilters) {
    cache.lastFetched = new Date();
    cache.appointments = [...appointmentsList];
    cache.filters = { ...queryFilters };
  }

  // 自動載入 (如果開啟選項)
  if (options.autoLoad) {
    loadAppointments();
  }

  return {
    // 狀態
    appointments,
    loading,
    error,
    currentAppointment,
    statistics,
    pagination,
    filters,
    
    // 計算屬性
    stats,
    filteredAppointments,

    // 方法
    loadAppointments,
    searchAppointments,
    changePage,
    loadAppointmentById,
    reviewAppointment,
    completeAppointment,
    cancelAppointment,
    updateAppointment,
    resetFilters
  };
}

/**
 * 將 API 回應的預約資料包裝轉換為內部使用的預約資料格式
 * @param {Object} appointmentWrapper - API 回應的預約資料包裝
 * @returns {Object} 轉換後的預約資料
 * @private
 */
function _transformAppointmentData(appointmentWrapper) {
  if (!appointmentWrapper || !appointmentWrapper.appointment) {
    return {};
  }

  const appointment = appointmentWrapper.appointment;
  return {
    id: appointment.id || null,
    memberId: appointment.memberId || null,
    memberName: appointmentWrapper.creatorType === 'member' ? appointmentWrapper.creatorDisplayName : '未指定會員',
    caregiverId: appointment.caregiverId || null,
    caregiverName: appointmentWrapper.creatorType === 'employee' ? appointmentWrapper.creatorDisplayName : '未知照服員',
    scheduledAt: appointment.scheduledAt || '',
    endTime: appointment.endTime || '',
    status: appointment.status || 'unknown',
    isBlocked: appointment.isBlocked || false,
    serviceTypeId: appointment.serviceTypeId || null,
    serviceLocation: appointment.serviceLocation || '未指定地點',
    totalAmount: appointment.totalAmount || 0,
    notes: appointment.notes || '',
    blockType: appointment.blockType || 'unknown',
    ratingScore: appointment.ratingScore || null,
    ratingComment: appointment.ratingComment || '',
    ratedAt: appointment.ratedAt || null,
    isRated: appointment.isRated || false,
    createdAt: appointment.createdAt || '',
    cancelledAt: appointment.cancelledAt || null,
    creatorDisplayName: appointmentWrapper.creatorDisplayName || '',
    creatorType: appointmentWrapper.creatorType || 'unknown',
    isMemberAppointment: appointmentWrapper.isMemberAppointment || false,
    isEmployeeAppointment: appointmentWrapper.isEmployeeAppointment || false
  };
}