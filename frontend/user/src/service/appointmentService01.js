import { caregiverAppointmentApi } from '@/api/caregiverAppointmentApi'
import { serviceTypeApi } from '@/api/serviceTypeApi'
import { serviceTypeService } from '@/service/serviceTypeService01'

/**
 * 預約服務層
 * 負責預約相關的業務邏輯處理和資料轉換
 */
export const appointmentService = {
  /**
   * 創建會員預約
   * @param {Object} appointmentData - 預約資料
   * @returns {Promise<Object>} 創建結果
   */
  async createMemberAppointment(appointmentData) {
    try {
      // 驗證預約資料
      const validation = this.validateAppointmentData(appointmentData)
      if (!validation.valid) {
        return {
          success: false,
          message: validation.message
        }
      }

      // 格式化預約資料
      const formattedData = this.formatAppointmentData(appointmentData)
      
      // 呼叫 API 創建預約
      const response = await caregiverAppointmentApi.createMemberAppointment(formattedData)
      
      if (response.data && response.data.success) {
        return {
          success: true,
          appointment: response.data.appointment,
          appointmentId: response.data.appointment?.id,
          message: '預約建立成功，我們將盡快為您安排服務'
        }
      } else {
        throw new Error(response.data?.message || '預約建立失敗')
      }
    } catch (error) {
      console.error('創建預約失敗:', error)
      return {
        success: false,
        message: this.getErrorMessage(error)
      }
    }
  },

  /**
   * 取得會員預約列表
   * @param {Object} queryParams - 查詢參數
   * @returns {Promise<Object>} 預約列表結果
   */
  async getMemberAppointments(queryParams = {}) {
    try {
      const response = await caregiverAppointmentApi.getMemberAppointments(queryParams)
      
      if (response.data && response.data.success) {
        const appointments = response.data.content || response.data.appointments || []
        
        return {
          success: true,
          appointments: appointments.map(appointment => this.formatAppointmentForDisplay(appointment)),
          currentPage: response.data.currentPage || 0,
          totalCount: response.data.totalCount || appointments.length,
          hasNext: response.data.hasNext || false,
          message: '預約列表載入成功'
        }
      } else {
        throw new Error(response.data?.message || '載入預約列表失敗')
      }
    } catch (error) {
      console.error('載入預約列表失敗:', error)
      return {
        success: false,
        appointments: [],
        currentPage: 0,
        totalCount: 0,
        hasNext: false,
        message: this.getErrorMessage(error)
      }
    }
  },

  /**
   * 取得預約詳情
   * @param {number} appointmentId - 預約ID
   * @returns {Promise<Object>} 預約詳情結果
   */
  async getAppointmentDetail(appointmentId) {
    try {
      const response = await caregiverAppointmentApi.getAppointmentDetail(appointmentId)
      
      if (response.data && response.data.success) {
        return {
          success: true,
          appointment: this.formatAppointmentForDisplay(response.data.appointment),
          message: '預約詳情載入成功'
        }
      } else {
        throw new Error(response.data?.message || '載入預約詳情失敗')
      }
    } catch (error) {
      console.error('載入預約詳情失敗:', error)
      return {
        success: false,
        appointment: null,
        message: this.getErrorMessage(error)
      }
    }
  },

  /**
   * 取消預約
   * @param {number} appointmentId - 預約ID
   * @param {string} reason - 取消原因
   * @returns {Promise<Object>} 取消結果
   */
  async cancelAppointment(appointmentId, reason) {
    try {
      const response = await caregiverAppointmentApi.cancelAppointment(appointmentId, { reason })
      
      if (response.data && response.data.success) {
        return {
          success: true,
          appointment: this.formatAppointmentForDisplay(response.data.appointment),
          message: '預約取消成功'
        }
      } else {
        throw new Error(response.data?.message || '預約取消失敗')
      }
    } catch (error) {
      console.error('取消預約失敗:', error)
      return {
        success: false,
        message: this.getErrorMessage(error)
      }
    }
  },

  /**
   * 評分預約
   * @param {number} appointmentId - 預約ID
   * @param {Object} ratingData - 評分資料
   * @returns {Promise<Object>} 評分結果
   */
  async rateAppointment(appointmentId, ratingData) {
    try {
      const response = await caregiverAppointmentApi.rateAppointment(appointmentId, ratingData)
      
      if (response.data && response.data.success) {
        return {
          success: true,
          appointment: this.formatAppointmentForDisplay(response.data.appointment),
          message: '評分成功，感謝您的回饋'
        }
      } else {
        throw new Error(response.data?.message || '評分失敗')
      }
    } catch (error) {
      console.error('評分失敗:', error)
      return {
        success: false,
        message: this.getErrorMessage(error)
      }
    }
  },

  /**
   * 計算預約價格
   * @param {number} serviceTypeId - 服務類型ID
   * @param {string} startTime - 開始時間
   * @param {string} endTime - 結束時間
   * @returns {Promise<Object>} 價格計算結果
   */
  async calculateAppointmentPrice(serviceTypeId, startTime, endTime) {
    try {
      const response = await serviceTypeApi.calculatePrice(serviceTypeId, startTime, endTime)
      
      if (response.data && response.data.success) {
        const calculation = response.data
        
        return {
          success: true,
          serviceType: calculation.serviceType,
          serviceHours: calculation.serviceHours,
          hourlyRate: calculation.hourlyRate,
          totalAmount: calculation.totalAmount,
          formattedHourlyRate: serviceTypeService.formatPrice(calculation.hourlyRate),
          formattedTotalAmount: serviceTypeService.formatPrice(calculation.totalAmount),
          message: '價格計算成功'
        }
      } else {
        throw new Error(response.data?.message || '價格計算失敗')
      }
    } catch (error) {
      console.error('價格計算失敗:', error)
      return {
        success: false,
        message: this.getErrorMessage(error)
      }
    }
  },

  /**
   * 檢查時間可用性
   * @param {number} caregiverId - 照服員ID
   * @param {string} startTime - 開始時間
   * @param {string} endTime - 結束時間
   * @returns {Promise<Object>} 可用性檢查結果
   */
  async checkTimeAvailability(caregiverId, startTime, endTime) {
    try {
      const response = await caregiverAppointmentApi.checkAvailability(caregiverId, startTime, endTime)
      
      return {
        success: true,
        available: response.data?.available || false,
        message: response.data?.message || (response.data?.available ? '時間可用' : '時間不可用')
      }
    } catch (error) {
      console.error('檢查時間可用性失敗:', error)
      return {
        success: false,
        available: false,
        message: this.getErrorMessage(error)
      }
    }
  },

  /**
   * 取得可用時間段
   * @param {number} caregiverId - 照服員ID
   * @param {string} date - 日期 (YYYY-MM-DD)
   * @returns {Promise<Object>} 可用時間段結果
   */
  async getAvailableTimeSlots(caregiverId, date) {
    try {
      const response = await caregiverAppointmentApi.getAvailableSlots(caregiverId, date)
      
      if (response.data && response.data.success) {
        const slots = response.data.availableSlots || []
        
        return {
          success: true,
          availableSlots: slots.map(slot => this.formatTimeSlot(slot)),
          message: '可用時間段載入成功'
        }
      } else {
        return {
          success: true,
          availableSlots: [],
          message: '此日期沒有可用時間段'
        }
      }
    } catch (error) {
      console.error('載入可用時間段失敗:', error)
      return {
        success: false,
        availableSlots: [],
        message: this.getErrorMessage(error)
      }
    }
  },

  /**
   * 取得會員統計資料
   * @returns {Promise<Object>} 統計資料結果
   */
  async getMemberStatistics() {
    try {
      const response = await caregiverAppointmentApi.getMemberStatistics()
      
      if (response.data && response.data.success) {
        return {
          success: true,
          statistics: response.data.statistics,
          message: '統計資料載入成功'
        }
      } else {
        throw new Error(response.data?.message || '載入統計資料失敗')
      }
    } catch (error) {
      console.error('載入統計資料失敗:', error)
      return {
        success: false,
        statistics: {},
        message: this.getErrorMessage(error)
      }
    }
  },

  /**
   * 取得待評分預約
   * @returns {Promise<Object>} 待評分預約結果
   */
  async getPendingRatings() {
    try {
      const response = await caregiverAppointmentApi.getPendingRatings()
      
      if (response.data && response.data.success) {
        const appointments = response.data.appointments || []
        
        return {
          success: true,
          appointments: appointments.map(appointment => this.formatAppointmentForDisplay(appointment)),
          message: '待評分預約載入成功'
        }
      } else {
        throw new Error(response.data?.message || '載入待評分預約失敗')
      }
    } catch (error) {
      console.error('載入待評分預約失敗:', error)
      return {
        success: false,
        appointments: [],
        message: this.getErrorMessage(error)
      }
    }
  },

  /**
   * 驗證預約資料
   * @param {Object} appointmentData - 預約資料
   * @returns {Object} 驗證結果
   */
  validateAppointmentData(appointmentData) {
    const errors = []
    
    if (!appointmentData.caregiverId) {
      errors.push('請選擇照服員')
    }
    
    if (!appointmentData.serviceTypeId) {
      errors.push('請選擇服務類型')
    }
    
    if (!appointmentData.scheduledAt) {
      errors.push('請選擇預約時間')
    }
    
    if (!appointmentData.endTime) {
      errors.push('請選擇結束時間')
    }
    
    if (!appointmentData.serviceLocation?.trim()) {
      errors.push('請輸入服務地點')
    }
    
    // 檢查時間邏輯
    if (appointmentData.scheduledAt && appointmentData.endTime) {
      const startTime = new Date(appointmentData.scheduledAt)
      const endTime = new Date(appointmentData.endTime)
      
      if (startTime >= endTime) {
        errors.push('結束時間必須晚於開始時間')
      }
      
      if (startTime <= new Date()) {
        errors.push('預約時間必須是未來時間')
      }
    }
    
    return {
      valid: errors.length === 0,
      message: errors.length > 0 ? errors.join('、') : '資料驗證通過'
    }
  },

  /**
   * 格式化預約資料用於API提交
   * @param {Object} appointmentData - 原始預約資料
   * @returns {Object} 格式化後的預約資料
   */
  formatAppointmentData(appointmentData) {
    return {
      caregiverId: parseInt(appointmentData.caregiverId),
      serviceTypeId: parseInt(appointmentData.serviceTypeId),
      scheduledAt: appointmentData.scheduledAt,
      endTime: appointmentData.endTime,
      serviceLocation: appointmentData.serviceLocation?.trim(),
      notes: appointmentData.notes?.trim() || ''
    }
  },

  /**
   * 格式化預約資料用於顯示
   * @param {Object} appointment - 原始預約資料
   * @returns {Object} 格式化後的預約資料
   */
  formatAppointmentForDisplay(appointment) {
    if (!appointment) return null
    
    return {
      ...appointment,
      // 格式化時間
      formattedScheduledAt: this.formatDateTime(appointment.scheduledAt),
      formattedEndTime: this.formatDateTime(appointment.endTime),
      formattedDate: this.formatDate(appointment.scheduledAt),
      formattedTimeRange: this.formatTimeRange(appointment.scheduledAt, appointment.endTime),
      
      // 格式化狀態
      statusDisplay: this.getStatusDisplay(appointment.status),
      statusColor: this.getStatusColor(appointment.status),
      
      // 計算服務時長
      serviceDuration: this.calculateDuration(appointment.scheduledAt, appointment.endTime),
      
      // 格式化價格
      formattedTotalAmount: appointment.totalAmount ? 
        serviceTypeService.formatPrice(appointment.totalAmount) : 'NT$ 0',
      
      // 照服員資訊
      caregiverName: appointment.caregiver?.chineseName || appointment.caregiverName || '未知照服員',
      caregiverPhone: appointment.caregiver?.phone || appointment.caregiverPhone,
      
      // 服務類型資訊
      serviceTypeName: appointment.serviceType?.serviceName || appointment.serviceTypeName || '未知服務',
      
      // 評分資訊
      hasRating: !!appointment.rating,
      canRate: appointment.status === 'completed' && !appointment.rating,
      canCancel: ['pending', 'approved'].includes(appointment.status),
      
      // 時間狀態
      isUpcoming: appointment.status === 'approved' && new Date(appointment.scheduledAt) > new Date(),
      isPast: new Date(appointment.endTime) < new Date(),
      isToday: this.isToday(appointment.scheduledAt)
    }
  },

  /**
   * 格式化時間段
   * @param {Object} slot - 原始時間段資料
   * @returns {Object} 格式化後的時間段資料
   */
  formatTimeSlot(slot) {
    return {
      ...slot,
      timeSlot: this.formatTimeRange(slot.startTime, slot.endTime),
      statusDisplay: slot.isAvailable ? '可預約' : '已預約',
      isAvailable: slot.isAvailable !== false
    }
  },

  /**
   * 格式化日期時間
   * @param {string} dateTime - 日期時間字串
   * @returns {string} 格式化後的日期時間
   */
  formatDateTime(dateTime) {
    if (!dateTime) return ''
    
    const date = new Date(dateTime)
    return date.toLocaleDateString('zh-TW', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      weekday: 'short'
    })
  },

  /**
   * 格式化日期
   * @param {string} dateTime - 日期時間字串
   * @returns {string} 格式化後的日期
   */
  formatDate(dateTime) {
    if (!dateTime) return ''
    
    const date = new Date(dateTime)
    return date.toLocaleDateString('zh-TW', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      weekday: 'short'
    })
  },

  /**
   * 格式化時間範圍
   * @param {string} startTime - 開始時間
   * @param {string} endTime - 結束時間
   * @returns {string} 格式化後的時間範圍
   */
  formatTimeRange(startTime, endTime) {
    if (!startTime || !endTime) return ''
    
    const start = new Date(startTime)
    const end = new Date(endTime)
    
    const startTimeStr = start.toLocaleTimeString('zh-TW', {
      hour: '2-digit',
      minute: '2-digit'
    })
    
    const endTimeStr = end.toLocaleTimeString('zh-TW', {
      hour: '2-digit',
      minute: '2-digit'
    })
    
    return `${startTimeStr} - ${endTimeStr}`
  },

  /**
   * 計算服務時長
   * @param {string} startTime - 開始時間
   * @param {string} endTime - 結束時間
   * @returns {string} 服務時長字串
   */
  calculateDuration(startTime, endTime) {
    if (!startTime || !endTime) return ''
    
    const start = new Date(startTime)
    const end = new Date(endTime)
    const diffMs = end.getTime() - start.getTime()
    const diffHours = Math.round(diffMs / (1000 * 60 * 60))
    
    return diffHours === 1 ? '1小時' : `${diffHours}小時`
  },

  /**
   * 取得狀態顯示文字
   * @param {string} status - 狀態代碼
   * @returns {string} 狀態顯示文字
   */
  getStatusDisplay(status) {
    const statusMap = {
      'pending': '待確認',
      'approved': '已確認',
      'in_progress': '進行中',
      'completed': '已完成',
      'cancelled': '已取消',
      'rejected': '已拒絕'
    }
    
    return statusMap[status] || status || '未知狀態'
  },

  /**
   * 取得狀態顏色
   * @param {string} status - 狀態代碼
   * @returns {string} 狀態顏色類別
   */
  getStatusColor(status) {
    const colorMap = {
      'pending': 'warning',
      'approved': 'info',
      'in_progress': 'primary',
      'completed': 'success',
      'cancelled': 'secondary',
      'rejected': 'danger'
    }
    
    return colorMap[status] || 'secondary'
  },

  /**
   * 檢查是否為今天
   * @param {string} dateTime - 日期時間字串
   * @returns {boolean} 是否為今天
   */
  isToday(dateTime) {
    if (!dateTime) return false
    
    const date = new Date(dateTime)
    const today = new Date()
    
    return date.toDateString() === today.toDateString()
  },

  /**
   * 取得錯誤訊息
   * @param {Error} error - 錯誤物件
   * @returns {string} 格式化後的錯誤訊息
   */
  getErrorMessage(error) {
    if (error.response?.data?.message) {
      return error.response.data.message
    }
    
    if (error.response?.status === 401) {
      return '請重新登入後再試'
    }
    
    if (error.response?.status === 403) {
      return '沒有權限執行此操作'
    }
    
    if (error.response?.status === 404) {
      return '找不到相關資料'
    }
    
    if (error.response?.status >= 500) {
      return '伺服器錯誤，請稍後再試'
    }
    
    if (error.code === 'ECONNREFUSED' || error.message.includes('Network Error')) {
      return '網路連接失敗，請檢查網路狀態'
    }
    
    return error.message || '發生未知錯誤'
  }
}

export default appointmentService
