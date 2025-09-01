import { ref, reactive, computed, watch } from 'vue'
import { appointmentService } from '@/service/appointmentService'
import { serviceTypeService } from '@/service/serviceTypeService';
import memberApi from '@/api/memberApi'
import { caregiverAppointmentApi } from '@/api/caregiverAppointmentApi';
import { readonly } from 'vue';

/**
 * 預約管理組合函數
 * 提供預約相關的狀態管理和操作方法
 */
export function useAppointment() {
  // ========== 響應式狀態 ==========
  
  // 載入狀態
  const loading = ref(false)
  const submitting = ref(false)
  const error = ref(null)
  const successMessage = ref('')

  // 會員資料
  const currentMember = ref(null)
  
  // 服務類型資料
  const serviceTypes = ref([])
  const selectedServiceType = ref(null)
  
  // 預約資料
  const appointments = ref([])
  const currentAppointment = ref(null)
  const appointmentStatistics = ref({})
  
  // 時間相關
  const availableSlots = ref([])
  const selectedTimeSlot = ref(null)
  
  // 價格計算
  const priceCalculation = ref(null)
  
  // 分頁資訊
  const pagination = reactive({
    currentPage: 0,
    totalCount: 0,
    pageSize: 10,
    hasNext: false
  })

  // 篩選條件
  const filters = reactive({
    status: '',
    dateFrom: '',
    dateTo: '',
    serviceTypeId: ''
  })

  // ========== 計算屬性 ==========
  
  /**
   * 是否有載入中的操作
   */
  const isLoading = computed(() => loading.value || submitting.value)
  
  /**
   * 是否有選擇服務類型
   */
  const hasSelectedService = computed(() => !!selectedServiceType.value)
  
  /**
   * 是否有可用時間段
   */
  const hasAvailableSlots = computed(() => availableSlots.value.length > 0)
  
  /**
   * 是否已選擇時間段
   */
  const hasSelectedTimeSlot = computed(() => !!selectedTimeSlot.value)
  
  /**
   * 格式化的預約統計
   */
  const formattedStatistics = computed(() => {
    if (!appointmentStatistics.value || Object.keys(appointmentStatistics.value).length === 0) {
      return {
        total: 0,
        pending: 0,
        approved: 0,
        completed: 0,
        cancelled: 0,
        rejected: 0,
        pendingRatings: 0
      }
    }
    
    return {
      total: appointmentStatistics.value.totalCount || 0,
      pending: appointmentStatistics.value.pendingCount || 0,
      approved: appointmentStatistics.value.approvedCount || 0,
      completed: appointmentStatistics.value.completedCount || 0,
      cancelled: appointmentStatistics.value.cancelledCount || 0,
      rejected: appointmentStatistics.value.rejectedCount || 0,
      pendingRatings: appointmentStatistics.value.pendingRatingsCount || 0
    }
  })

  /**
   * 篩選後的預約列表
   */
  const filteredAppointments = computed(() => {
    let filtered = [...appointments.value]

    // 按狀態篩選
    if (filters.status) {
      filtered = filtered.filter(apt => apt.status === filters.status)
    }

    // 按日期範圍篩選
    if (filters.dateFrom) {
      const fromDate = new Date(filters.dateFrom)
      filtered = filtered.filter(apt => {
        const aptDate = new Date(apt.scheduledAt)
        return aptDate >= fromDate
      })
    }

    if (filters.dateTo) {
      const toDate = new Date(filters.dateTo)
      toDate.setHours(23, 59, 59, 999) // 設為當天結束
      filtered = filtered.filter(apt => {
        const aptDate = new Date(apt.scheduledAt)
        return aptDate <= toDate
      })
    }

    // 按服務類型篩選
    if (filters.serviceTypeId) {
      filtered = filtered.filter(apt => apt.serviceTypeId === parseInt(filters.serviceTypeId))
    }

    return filtered
  })

  // ========== 方法 ==========

  /**
   * 清除錯誤訊息
   */
  const clearError = () => {
    error.value = null
    successMessage.value = ''
  }

  /**
   * 設置錯誤訊息
   * @param {string} message - 錯誤訊息
   */
  const setError = (message) => {
    error.value = message
    successMessage.value = ''
  }

  /**
   * 設置成功訊息
   * @param {string} message - 成功訊息
   */
  const setSuccess = (message) => {
    successMessage.value = message
    error.value = null
  }

  /**
   * 取得當前會員資料
   */
  const loadCurrentMember = async () => {
    
    try {
      loading.value = true
      clearError()
      
      const response = await memberApi.getMyProfile()
      if (response.data) {
        currentMember.value = response.data
        return response.data
      } else {
        throw new Error('無法取得會員資料')
      }
    } catch (err) {
      console.error('取得會員資料失敗:', err)
      setError(err.response?.data?.message || err.message || '取得會員資料失敗')
      return null
    } finally {
      loading.value = false
    }
  }

  /**
   * 載入服務類型列表
   * @param {boolean} forDropdown - 是否為下拉選單用途
   */
  const loadServiceTypes = async (forDropdown = true) => {
    try {
      loading.value = true
      clearError()
      
      const result = forDropdown 
        ? await serviceTypeService.getServiceTypesForDropdown()
        : await serviceTypeService.getAllActiveServiceTypes()
        
      if (result.success) {
        serviceTypes.value = result.serviceTypes
        return result.serviceTypes
      } else {
        throw new Error(result.message)
      }
    } catch (err) {
      console.error('載入服務類型失敗:', err)
      setError(err.message || '載入服務類型失敗')
      return []
    } finally {
      loading.value = false
    }
  }

  /**
   * 計算預約價格
   * @param {number} serviceTypeId - 服務類型ID
   * @param {string} startTime - 開始時間
   * @param {string} endTime - 結束時間
   */
  const calculatePrice = async (serviceTypeId, startTime, endTime) => {
    try {
      loading.value = true
      clearError()
      
      const result = await appointmentService.calculateAppointmentPrice(serviceTypeId, startTime, endTime)
      
      if (result.success) {
        priceCalculation.value = result
        return result
      } else {
        throw new Error(result.message)
      }
    } catch (err) {
      console.error('計算價格失敗:', err)
      setError(err.message || '計算價格失敗')
      return null
    } finally {
      loading.value = false
    }
  }

  /**
   * 檢查時間可用性
   * @param {number} caregiverId - 照服員ID
   * @param {string} startTime - 開始時間
   * @param {string} endTime - 結束時間
   */
  const checkTimeAvailability = async (caregiverId, startTime, endTime) => {
    try {
      loading.value = true
      clearError()
      
      const result = await appointmentService.checkTimeAvailability(caregiverId, startTime, endTime)
      return result
    } catch (err) {
      console.error('檢查時間可用性失敗:', err)
      setError(err.message || '檢查時間可用性失敗')
      return { available: false, message: '檢查失敗' }
    } finally {
      loading.value = false
    }
  }

  /**
   * 載入可用時間段
   * @param {number} caregiverId - 照服員ID
   * @param {string} date - 日期 (YYYY-MM-DD)
   */
  const loadAvailableSlots = async (caregiverId, date) => {
    try {
      loading.value = true
      clearError()
      
      const result = await appointmentService.getAvailableTimeSlots(caregiverId, date)
      
      if (result.success) {
        availableSlots.value = result.availableSlots
        return result.availableSlots
      } else {
        setError(result.message)
        availableSlots.value = []
        return []
      }
    } catch (err) {
      console.error('載入可用時間段失敗:', err)
      setError(err.message || '載入可用時間段失敗')
      availableSlots.value = []
      return []
    } finally {
      loading.value = false
    }
  }

  /**
   * 建立預約
   * @param {Object} appointmentData - 預約資料
   */
  const createAppointment = async (appointmentData) => {
    try {
      submitting.value = true
      clearError()
      
      const result = await appointmentService.createMemberAppointment(appointmentData)
      
      if (result.success) {
        setSuccess(result.message || '預約建立成功')
        
        // 重新載入預約列表
        await loadMemberAppointments()
        
        return result
      } else {
        throw new Error(result.message)
      }
    } catch (err) {
      console.error('建立預約失敗:', err)
      setError(err.message || '建立預約失敗')
      throw err
    } finally {
      submitting.value = false
    }
  }

  /**
   * 載入會員預約列表
   * @param {Object} params - 查詢參數
   */
  const loadMemberAppointments = async (params = {}) => {
    try {
      loading.value = true
      clearError()
      
      const queryParams = {
        status: params.status,
        page: params.page || pagination.currentPage,
        size: params.size || pagination.pageSize
      }
      
      const result = await appointmentService.getMemberAppointments(queryParams)
      
      if (result.success) {
        appointments.value = result.appointments
        pagination.currentPage = result.currentPage || 0
        pagination.totalCount = result.totalCount || 0
        pagination.hasNext = (pagination.currentPage + 1) * pagination.pageSize < pagination.totalCount
        
        return result.appointments
      } else {
        throw new Error(result.message)
      }
    } catch (err) {
      console.error('載入預約列表失敗:', err)
      setError(err.message || '載入預約列表失敗')
      return []
    } finally {
      loading.value = false
    }
  }

  /**
   * 取得預約詳情
   * @param {number} appointmentId - 預約ID
   */
  const loadAppointmentDetail = async (appointmentId) => {
    try {
      loading.value = true
      clearError()
      
      const result = await appointmentService.getAppointmentDetail(appointmentId)
      
      if (result.success) {
        currentAppointment.value = result.appointment
        return result.appointment
      } else {
        throw new Error(result.message)
      }
    } catch (err) {
      console.error('載入預約詳情失敗:', err)
      setError(err.message || '載入預約詳情失敗')
      return null
    } finally {
      loading.value = false
    }
  }

  /**
   * 取消預約
   * @param {number} appointmentId - 預約ID
   * @param {string} reason - 取消原因
   */
  const cancelAppointment = async (appointmentId, reason) => {
    try {
      submitting.value = true
      clearError()
      
      const result = await appointmentService.cancelAppointment(appointmentId, reason)
      
      if (result.success) {
        setSuccess(result.message || '預約取消成功')
        
        // 更新當前預約資料
        if (currentAppointment.value && currentAppointment.value.id === appointmentId) {
          currentAppointment.value = result.appointment
        }
        
        // 重新載入預約列表
        await loadMemberAppointments()
        
        return result
      } else {
        throw new Error(result.message)
      }
    } catch (err) {
      console.error('取消預約失敗:', err)
      setError(err.message || '取消預約失敗')
      throw err
    } finally {
      submitting.value = false
    }
  }

  /**
   * 評分預約
   * @param {number} appointmentId - 預約ID
   * @param {Object} ratingData - 評分資料
   */
  const rateAppointment = async (appointmentId, ratingData) => {
    try {
      submitting.value = true
      clearError()
      
      const result = await appointmentService.rateAppointment(appointmentId, ratingData)
      
      if (result.success) {
        setSuccess(result.message || '評分成功')
        
        // 更新當前預約資料
        if (currentAppointment.value && currentAppointment.value.id === appointmentId) {
          currentAppointment.value = result.appointment
        }
        
        // 重新載入相關資料
        await loadMemberAppointments()
        await loadMemberStatistics()
        
        return result
      } else {
        throw new Error(result.message)
      }
    } catch (err) {
      console.error('評分失敗:', err)
      setError(err.message || '評分失敗')
      throw err
    } finally {
      submitting.value = false
    }
  }

  /**
   * 載入會員統計資料
   */
  const loadMemberStatistics = async () => {
    try {
      const result = await appointmentService.getMemberStatistics()
      
      if (result.success) {
        appointmentStatistics.value = result.statistics
        return result.statistics
      } else {
        console.warn('載入統計資料失敗:', result.message)
        return {}
      }
    } catch (err) {
      console.error('載入統計資料失敗:', err)
      return {}
    }
  }

  /**
   * 載入待評分預約
   */
  const loadPendingRatings = async () => {
    try {
      loading.value = true
      clearError()
      
      const result = await appointmentService.getPendingRatings()
      
      if (result.success) {
        return result.appointments
      } else {
        throw new Error(result.message)
      }
    } catch (err) {
      console.error('載入待評分預約失敗:', err)
      setError(err.message || '載入待評分預約失敗')
      return []
    } finally {
      loading.value = false
    }
  }

  /**
   * 重置所有狀態
   */
  const resetState = () => {
    clearError()
    currentMember.value = null
    serviceTypes.value = []
    selectedServiceType.value = null
    appointments.value = []
    currentAppointment.value = null
    appointmentStatistics.value = {}
    availableSlots.value = []
    selectedTimeSlot.value = null
    priceCalculation.value = null
    
    // 重置分頁
    pagination.currentPage = 0
    pagination.totalCount = 0
    pagination.hasNext = false
  }

  /**
   * 設置選中的服務類型
   * @param {Object} serviceType - 服務類型資料
   */
  const selectServiceType = (serviceType) => {
    selectedServiceType.value = serviceType
    // 清除之前的價格計算
    priceCalculation.value = null
  }

  /**
   * 設置選中的時間段
   * @param {Object} timeSlot - 時間段資料
   */
  const selectTimeSlot = (timeSlot) => {
    selectedTimeSlot.value = timeSlot
  }

  /**
   * 清除選擇
   */
  const clearSelections = () => {
    selectedServiceType.value = null
    selectedTimeSlot.value = null
    priceCalculation.value = null
    availableSlots.value = []
  }

  /**
   * 更新篩選條件
   * @param {Object} newFilters - 新的篩選條件
   */
  const updateFilters = (newFilters) => {
    Object.assign(filters, newFilters)
  }

  /**
   * 重置篩選條件
   */
  const resetFilters = () => {
    filters.status = ''
    filters.dateFrom = ''
    filters.dateTo = ''
    filters.serviceTypeId = ''
  }

  // ========== 監聽器 ==========
  
  /**
   * 監聽選中服務類型變化，自動清除相關狀態
   */
  watch(selectedServiceType, (newServiceType, oldServiceType) => {
    if (newServiceType?.id !== oldServiceType?.id) {
      // 服務類型改變時，清除時間段和價格計算
      selectedTimeSlot.value = null
      priceCalculation.value = null
    }
  })

  // ========== 返回的響應式狀態和方法 ==========
  
  return {
    // 狀態
    loading: readonly(loading),
    submitting: readonly(submitting),
    error: readonly(error),
    successMessage: readonly(successMessage),
    currentMember: readonly(currentMember),
    serviceTypes: readonly(serviceTypes),
    selectedServiceType: readonly(selectedServiceType),
    appointments: readonly(appointments),
    currentAppointment: readonly(currentAppointment),
    appointmentStatistics: readonly(appointmentStatistics),
    availableSlots: readonly(availableSlots),
    selectedTimeSlot: readonly(selectedTimeSlot),
    priceCalculation: readonly(priceCalculation),
    pagination: readonly(pagination),
    filters: readonly(filters),
    
    // 計算屬性
    isLoading,
    hasSelectedService,
    hasAvailableSlots,
    hasSelectedTimeSlot,
    formattedStatistics,
    filteredAppointments,
    
    // 方法
    clearError,
    setError,
    setSuccess,
    loadCurrentMember,
    loadServiceTypes,
    calculatePrice,
    checkTimeAvailability,
    loadAvailableSlots,
    createAppointment,
    loadMemberAppointments,
    loadAppointmentDetail,
    cancelAppointment,
    rateAppointment,
    loadMemberStatistics,
    loadPendingRatings,
    resetState,
    selectServiceType,
    selectTimeSlot,
    clearSelections,
    updateFilters,
    resetFilters
  }
}

/**
 * 照服員行程組合函數
 * 專門處理照服員行程相關功能
 */
export function useCaregiverSchedule() {
  const loading = ref(false)
  const error = ref(null)
  const schedule = ref([])
  const currentMonth = ref(new Date())
  const selectedDate = ref(null)
  const selectedTimeSlot = ref(null)
  const availableDates = ref([])
  const selectedDateSlots = ref([])

  /**
   * 載入照服員指定時間範圍的行程
   * @param {number} caregiverId - 照服員ID
   * @param {Date} startDate - 開始日期
   * @param {Date} endDate - 結束日期
   */
  const loadCaregiverSchedule = async (caregiverId, startDate, endDate) => {
    try {
      loading.value = true
      error.value = null
      
      const startDateTime = serviceTypeService.formatTimeForAPI(startDate)
      const endDateTime = serviceTypeService.formatTimeForAPI(endDate)
      
      const response = await caregiverAppointmentApi.getCaregiverSchedule(caregiverId, startDateTime, endDateTime)
      
      if (response.data && response.data.success) {
        schedule.value = response.data.schedule
        return response.data.schedule
      } else {
        throw new Error(response.data.message || '載入行程失敗')
      }
    } catch (err) {
      console.error('載入照服員行程失敗:', err)
      error.value = err.response?.data?.message || err.message || '載入行程失敗'
      schedule.value = []
      return []
    } finally {
      loading.value = false
    }
  }

  /**
   * 載入當月和下月的行程資料
   * @param {number} caregiverId - 照服員ID
   */
  const loadCurrentAndNextMonth = async (caregiverId) => {
    const currentDate = currentMonth.value
    const startDate = new Date(currentDate.getFullYear(), currentDate.getMonth(), 1)
    const endDate = new Date(currentDate.getFullYear(), currentDate.getMonth() + 2, 0) // 下個月最後一天
    
    await loadCaregiverSchedule(caregiverId, startDate, endDate)
  }

  /**
   * 選擇日期
   * @param {string} dateString - 日期字串 (YYYY-MM-DD)
   */
  const selectDate = async (dateString) => {
    selectedDate.value = dateString
    selectedTimeSlot.value = null
    
    // 載入該日期的可用時間段
    if (dateString) {
      const caregiverId = getCurrentCaregiverId() // 需要從某處獲取當前照服員ID
      if (caregiverId) {
        await loadDateSlots(caregiverId, dateString)
      }
    }
  }

  /**
   * 載入指定日期的時間段
   * @param {number} caregiverId - 照服員ID
   * @param {string} date - 日期 (YYYY-MM-DD)
   */
  const loadDateSlots = async (caregiverId, date) => {
    try {
      const result = await appointmentService.getAvailableTimeSlots(caregiverId, date)
      
      if (result.success) {
        selectedDateSlots.value = result.availableSlots
      } else {
        selectedDateSlots.value = []
      }
    } catch (err) {
      console.error('載入日期時間段失敗:', err)
      selectedDateSlots.value = []
    }
  }

  /**
   * 選擇時間段
   * @param {Object} timeSlot - 時間段資料
   */
  const selectTimeSlot = (timeSlot) => {
    selectedTimeSlot.value = timeSlot
  }

  /**
   * 切換到上個月
   */
  const prevMonth = () => {
    currentMonth.value = new Date(currentMonth.value.getFullYear(), currentMonth.value.getMonth() - 1, 1)
  }

  /**
   * 切換到下個月
   */
  const nextMonth = () => {
    currentMonth.value = new Date(currentMonth.value.getFullYear(), currentMonth.value.getMonth() + 1, 1)
  }

  /**
   * 回到今天
   */
  const goToToday = () => {
    currentMonth.value = new Date()
  }

  /**
   * 清除選擇
   */
  const clearSelection = () => {
    selectedDate.value = null
    selectedTimeSlot.value = null
    selectedDateSlots.value = []
  }

  /**
   * 取得日期狀態
   * @param {string} dateString - 日期字串
   */
  const getDateStatus = (dateString) => {
    // 這裡應該根據 schedule 資料計算該日期的狀態
    const dateSchedules = schedule.value.filter(item => {
      const scheduleDate = new Date(item.scheduledAt).toISOString().split('T')[0]
      return scheduleDate === dateString
    })

    return {
      hasSchedule: dateSchedules.length > 0,
      availableCount: 0, // 需要計算可用時段數量
      isFullyBooked: dateSchedules.length >= 8 // 假設一天最多8個時段
    }
  }

  /**
   * 取得日期樣式類別
   * @param {Object} dateInfo - 日期資訊
   */
  const getDateClasses = (dateInfo) => {
    const classes = []
    const today = new Date().toISOString().split('T')[0]
    
    if (dateInfo.date === today) {
      classes.push('today')
    }
    
    if (dateInfo.date < today) {
      classes.push('past-date')
    }
    
    if (selectedDate.value === dateInfo.date) {
      classes.push('selected')
    }
    
    const status = getDateStatus(dateInfo.date)
    if (status.hasSchedule) {
      if (status.isFullyBooked) {
        classes.push('fully-booked')
      } else if (status.availableCount > 0) {
        classes.push('has-available')
      }
    } else {
      classes.push('no-schedule')
    }
    
    // 週末樣式
    const dayOfWeek = new Date(dateInfo.date).getDay()
    if (dayOfWeek === 0 || dayOfWeek === 6) {
      classes.push('weekend')
    }
    
    return classes
  }

  /**
   * 取得選中的時間資訊
   */
  const getSelectedTimeInfo = () => {
    if (!selectedDate.value || !selectedTimeSlot.value) {
      return null
    }
    
    return {
      date: selectedDate.value,
      timeSlot: selectedTimeSlot.value.timeSlot,
      startTime: selectedTimeSlot.value.startTime,
      endTime: selectedTimeSlot.value.endTime,
      duration: selectedTimeSlot.value.duration
    }
  }

  // 計算屬性
  const monthDisplay = computed(() => {
    return currentMonth.value.toLocaleDateString('zh-TW', {
      year: 'numeric',
      month: 'long'
    })
  })

  const nextMonthDisplay = computed(() => {
    const nextMonth = new Date(currentMonth.value.getFullYear(), currentMonth.value.getMonth() + 1, 1)
    return nextMonth.toLocaleDateString('zh-TW', {
      year: 'numeric',
      month: 'long'
    })
  })

  const currentMonthDates = computed(() => {
    return generateMonthDates(currentMonth.value)
  })

  const nextMonthDates = computed(() => {
    const nextMonth = new Date(currentMonth.value.getFullYear(), currentMonth.value.getMonth() + 1, 1)
    return generateMonthDates(nextMonth)
  })

  /**
   * 生成月份日期資料
   * @param {Date} month - 月份
   */
  const generateMonthDates = (month) => {
    const dates = []
    const year = month.getFullYear()
    const monthIndex = month.getMonth()
    const firstDay = new Date(year, monthIndex, 1)
    const lastDay = new Date(year, monthIndex + 1, 0)
    const startWeekDay = firstDay.getDay()

    // 補齊前面的空白日期
    for (let i = 0; i < startWeekDay; i++) {
      const date = new Date(year, monthIndex, -startWeekDay + i + 1)
      dates.push({
        date: date.toISOString().split('T')[0],
        day: date.getDate(),
        isCurrentMonth: false
      })
    }

    // 當月日期
    for (let day = 1; day <= lastDay.getDate(); day++) {
      const date = new Date(year, monthIndex, day)
      dates.push({
        date: date.toISOString().split('T')[0],
        day: day,
        isCurrentMonth: true
      })
    }

    // 補齊後面的空白日期
    const remainingDays = 42 - dates.length // 6週 * 7天
    for (let day = 1; day <= remainingDays; day++) {
      const date = new Date(year, monthIndex + 1, day)
      dates.push({
        date: date.toISOString().split('T')[0],
        day: date.getDate(),
        isCurrentMonth: false
      })
    }

    return dates
  }

  /**
   * 獲取當前照服員ID（需要根據實際情況實現）
   */
  const getCurrentCaregiverId = () => {
    // 這裡需要根據實際情況獲取當前照服員ID
    // 可能從路由參數、store或其他地方獲取
    return null
  }

  return {
    // 狀態
    loading: readonly(loading),
    error: readonly(error),
    schedule: readonly(schedule),
    currentMonth: readonly(currentMonth),
    selectedDate: readonly(selectedDate),
    selectedTimeSlot: readonly(selectedTimeSlot),
    availableDates: readonly(availableDates),
    selectedDateSlots: readonly(selectedDateSlots),
    
    // 計算屬性
    monthDisplay,
    nextMonthDisplay,
    currentMonthDates,
    nextMonthDates,
    
    // 方法
    loadCaregiverSchedule,
    loadCurrentAndNextMonth,
    selectDate,
    selectTimeSlot,
    prevMonth,
    nextMonth,
    goToToday,
    clearSelection,
    getDateStatus,
    getDateClasses,
    getSelectedTimeInfo
  }
}