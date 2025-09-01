import { ref, computed, reactive } from 'vue'
import { caregiverApi } from '@/api/caregiverApi'
import { caregiverService } from '@/service/caregiverService'
import { useToast } from '@/composables/useToast'

/**
 * 照服員行程表相關狀態管理
 * 提供行程查詢、時間選擇等功能
 */
export function useCaregiverSchedule() {
  // 注入 Toast 功能
  const { showError } = useToast()
  
  // 響應式資料
  const scheduleData = ref({}) // { 'YYYY-MM-DD': [timeSlots] }
  const availableSlots = ref([])
  const selectedDate = ref('')
  const selectedTimeSlot = ref(null)
  const loading = ref(false)
  const error = ref(null)

  // 當前顯示的月份
  const currentMonth = reactive({
    year: new Date().getFullYear(),
    month: new Date().getMonth() // 0-11
  })

  // 計算屬性：當前月份的日期列表
  const currentMonthDates = computed(() => {
    return caregiverService.generateMonthDates(currentMonth.year, currentMonth.month)
  })

  // 計算屬性：下個月份的日期列表
  const nextMonthDates = computed(() => {
    const nextYear = currentMonth.month === 11 ? currentMonth.year + 1 : currentMonth.year
    const nextMonth = currentMonth.month === 11 ? 0 : currentMonth.month + 1
    return caregiverService.generateMonthDates(nextYear, nextMonth)
  })

  // 計算屬性：選中日期的可用時間段
  const selectedDateSlots = computed(() => {
    if (!selectedDate.value || !scheduleData.value[selectedDate.value]) {
      return []
    }
    
    return scheduleData.value[selectedDate.value]
      .filter(slot => slot.isAvailable)
      .sort((a, b) => new Date(a.startTime) - new Date(b.startTime))
  })

  // 計算屬性：月份顯示文字
  const monthDisplay = computed(() => {
    return `${currentMonth.year}年${currentMonth.month + 1}月`
  })

  // 計算屬性：下個月份顯示文字
  const nextMonthDisplay = computed(() => {
    const nextYear = currentMonth.month === 11 ? currentMonth.year + 1 : currentMonth.year
    const nextMonth = currentMonth.month === 11 ? 0 : currentMonth.month + 1
    return `${nextYear}年${nextMonth + 1}月`
  })

  /**
   * 載入照服員行程表
   * @param {number} caregiverId - 照服員ID
   * @param {string} startDate - 開始日期 (YYYY-MM-DD)
   * @param {string} endDate - 結束日期 (YYYY-MM-DD)
   */
  const loadSchedule = async (caregiverId, startDate, endDate) => {
    try {
      loading.value = true
      error.value = null
      
      const response = await caregiverApi.getCaregiverSchedule(caregiverId, startDate, endDate)
      scheduleData.value = caregiverService.formatScheduleData(response.data.schedule || response.data)
      
    } catch (err) {
      error.value = '載入行程表失敗'
      showError('載入行程表失敗，請稍後再試')
      console.error('載入行程表失敗:', err)
    } finally {
      loading.value = false
    }
  }

  /**
   * 載入照服員本月及下月行程
   * @param {number} caregiverId - 照服員ID
   */
  const loadCurrentAndNextMonth = async (caregiverId) => {
    const currentDate = new Date()
    const startDate = new Date(currentDate.getFullYear(), currentDate.getMonth(), 1)
    const endDate = new Date(currentDate.getFullYear(), currentDate.getMonth() + 2, 0) // 下個月最後一天
    
    const startDateStr = startDate.toISOString().split('T')[0]
    const endDateStr = endDate.toISOString().split('T')[0]
    
    await loadSchedule(caregiverId, startDateStr, endDateStr)
  }

  /**
   * 載入特定日期的可用時間段
   * @param {number} caregiverId - 照服員ID
   * @param {string} date - 日期 (YYYY-MM-DD)
   */
  const loadAvailableSlots = async (caregiverId, date) => {
    try {
      const response = await caregiverApi.getAvailableSlots(caregiverId, date)
      availableSlots.value = response.data
    } catch (err) {
      showError('載入可用時間段失敗')
      console.error('載入可用時間段失敗:', err)
    }
  }

  /**
   * 檢查時間可用性
   * @param {number} caregiverId - 照服員ID
   * @param {string} startTime - 開始時間
   * @param {string} endTime - 結束時間
   * @returns {Promise<boolean>} 是否可用
   */
  const checkAvailability = async (caregiverId, startTime, endTime) => {
    try {
      const response = await caregiverApi.checkAvailability(caregiverId, startTime, endTime)
      return response.data.isAvailable
    } catch (err) {
      showError('檢查時間可用性失敗')
      console.error('檢查時間可用性失敗:', err)
      return false
    }
  }

  /**
   * 選擇日期
   * @param {string} date - 日期 (YYYY-MM-DD)
   */
  const selectDate = (date) => {
    // 檢查是否為過去日期
    const selectedDateObj = new Date(date)
    const today = new Date()
    today.setHours(0, 0, 0, 0)
    
    if (selectedDateObj < today) {
      showError('不能選擇過去的日期')
      return
    }
    
    selectedDate.value = date
    selectedTimeSlot.value = null // 重置時間段選擇
  }

  /**
   * 選擇時間段
   * @param {Object} timeSlot - 時間段資料
   */
  const selectTimeSlot = (timeSlot) => {
    if (!timeSlot.isAvailable) {
      showError('此時間段不可預約')
      return
    }
    
    selectedTimeSlot.value = timeSlot
  }

  /**
   * 切換到上個月
   */
  const prevMonth = () => {
    if (currentMonth.month === 0) {
      currentMonth.year--
      currentMonth.month = 11
    } else {
      currentMonth.month--
    }
  }

  /**
   * 切換到下個月
   */
  const nextMonth = () => {
    if (currentMonth.month === 11) {
      currentMonth.year++
      currentMonth.month = 0
    } else {
      currentMonth.month++
    }
  }

  /**
   * 跳轉到今天
   */
  const goToToday = () => {
    const today = new Date()
    currentMonth.year = today.getFullYear()
    currentMonth.month = today.getMonth()
    selectedDate.value = today.toISOString().split('T')[0]
  }

  /**
   * 取得日期的行程狀態
   * @param {string} date - 日期 (YYYY-MM-DD)
   * @returns {Object} 狀態資訊
   */
  const getDateStatus = (date) => {
    const slots = scheduleData.value[date] || []
    const availableSlots = slots.filter(slot => slot.isAvailable)
    const busySlots = slots.filter(slot => !slot.isAvailable)
    
    return {
      hasSchedule: slots.length > 0,
      availableCount: availableSlots.length,
      busyCount: busySlots.length,
      isFullyBooked: slots.length > 0 && availableSlots.length === 0,
      isEmpty: slots.length === 0
    }
  }

  /**
   * 取得日期的 CSS 類別
   * @param {Object} dateInfo - 日期資訊
   * @returns {Array} CSS 類別陣列
   */
  const getDateClasses = (dateInfo) => {
    const classes = ['schedule-date']
    
    if (dateInfo.isPast) {
      classes.push('past-date')
    }
    
    if (dateInfo.isToday) {
      classes.push('today')
    }
    
    if (dateInfo.isWeekend) {
      classes.push('weekend')
    }
    
    if (selectedDate.value === dateInfo.date) {
      classes.push('selected')
    }
    
    const status = getDateStatus(dateInfo.date)
    
    if (status.isEmpty) {
      classes.push('no-schedule')
    } else if (status.isFullyBooked) {
      classes.push('fully-booked')
    } else if (status.availableCount > 0) {
      classes.push('has-available')
    }
    
    return classes
  }

  /**
   * 清除選擇
   */
  const clearSelection = () => {
    selectedDate.value = ''
    selectedTimeSlot.value = null
  }

  /**
   * 重置到當前月份
   */
  const resetToCurrentMonth = () => {
    const now = new Date()
    currentMonth.year = now.getFullYear()
    currentMonth.month = now.getMonth()
  }

  /**
   * 取得選中的時間資訊
   * @returns {Object|null} 時間資訊
   */
  const getSelectedTimeInfo = () => {
    if (!selectedDate.value || !selectedTimeSlot.value) {
      return null
    }
    
    return {
      date: selectedDate.value,
      startTime: selectedTimeSlot.value.startTime,
      endTime: selectedTimeSlot.value.endTime,
      timeSlot: selectedTimeSlot.value.timeSlot,
      duration: caregiverService.calculateDuration(
        selectedTimeSlot.value.startTime,
        selectedTimeSlot.value.endTime
      )
    }
  }

  return {
    // 狀態
    scheduleData,
    availableSlots,
    selectedDate,
    selectedTimeSlot,
    loading,
    error,
    currentMonth,
    
    // 計算屬性
    currentMonthDates,
    nextMonthDates,
    selectedDateSlots,
    monthDisplay,
    nextMonthDisplay,
    
    // 方法
    loadSchedule,
    loadCurrentAndNextMonth,
    loadAvailableSlots,
    checkAvailability,
    selectDate,
    selectTimeSlot,
    prevMonth,
    nextMonth,
    goToToday,
    getDateStatus,
    getDateClasses,
    clearSelection,
    resetToCurrentMonth,
    getSelectedTimeInfo
  }
}
