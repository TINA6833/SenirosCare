import { caregiverAppointmentApi } from '@/api/caregiverAppointmentApi'
import { caregiverApi } from '@/api/caregiverApi'
// import { serviceTypeApi } from '@/api/serviceTypeApi'
// import { memberApi } from '@/api/memberApi'

/**
 * 預約服務層
 * 負責處理預約相關的業務邏輯和資料轉換
 */
class AppointmentService {

  /**
   * 建立會員預約
   * @param {Object} appointmentData - 預約資料
   * @returns {Promise<Object>} 處理後的預約結果
   */
  async createMemberAppointment(appointmentData) {
    try {
      // 先檢查時間可用性
      const availabilityCheck = await this.checkTimeAvailability(
        appointmentData.caregiverId,
        appointmentData.scheduledAt,
        appointmentData.endTime
      )

      if (!availabilityCheck.available) {
        throw new Error(`預約失敗：${availabilityCheck.message || '時間不可用'}`)
      }

      // 建立預約
      const response = await caregiverAppointmentApi.createMemberAppointment(appointmentData)
      
      if (response.data && response.data.success) {
        return {
          success: true,
          appointmentId: response.data.appointmentId,
          appointment: response.data.appointment,
          message: response.data.message || '預約建立成功',
          priceInfo: response.data.priceInfo,
          availabilityCheck: response.data.availabilityCheck
        }
      } else {
        throw new Error(response.data.message || '建立預約失敗')
      }
    } catch (error) {
      console.error('建立預約失敗:', error)
      throw new Error(error.response?.data?.message || error.message || '建立預約失敗')
    }
  }

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
      
      if (response.data && response.data.success) {
        return {
          available: response.data.available,
          message: response.data.message,
          caregiverId: response.data.caregiverId,
          requestedStartTime: response.data.requestedStartTime,
          requestedEndTime: response.data.requestedEndTime
        }
      } else {
        return {
          available: false,
          message: response.data.message || '時間檢查失敗'
        }
      }
    } catch (error) {
      console.error('時間可用性檢查失敗:', error)
      return {
        available: false,
        message: error.response?.data?.message || error.message || '時間檢查失敗'
      }
    }
  }

  /**
   * 取得照服員可用時間段
   * @param {number} caregiverId - 照服員ID
   * @param {string} date - 查詢日期 (YYYY-MM-DD)
   * @returns {Promise<Array>} 可用時間段列表
   */
  async getAvailableTimeSlots(caregiverId, date) {
    try {
      const response = await caregiverAppointmentApi.getAvailableSlots(caregiverId, date)
      
      if (response.data && response.data.success) {
        // 轉換時間段資料格式，增加前端需要的欄位
        const availableSlots = response.data.availableSlots.map(slot => ({
          id: `${slot.dateFormatted}_${slot.startTimeFormatted}_${slot.endTimeFormatted}`,
          startTime: slot.startTime,
          endTime: slot.endTime,
          duration: slot.duration,
          startTimeFormatted: slot.startTimeFormatted,
          endTimeFormatted: slot.endTimeFormatted,
          dateFormatted: slot.dateFormatted,
          timeSlot: `${slot.startTimeFormatted} - ${slot.endTimeFormatted}`,
          isAvailable: true,
          statusDisplay: `可預約 (${slot.duration}分鐘)`
        }))

        return {
          success: true,
          availableSlots,
          caregiverId: response.data.caregiverId,
          date: response.data.date,
          totalSlots: response.data.totalSlots
        }
      } else {
        return {
          success: false,
          availableSlots: [],
          message: response.data.message || '取得可用時間段失敗'
        }
      }
    } catch (error) {
      console.error('取得可用時間段失敗:', error)
      return {
        success: false,
        availableSlots: [],
        message: error.response?.data?.message || error.message || '取得可用時間段失敗'
      }
    }
  }

  /**
   * 計算預約價格
   * @param {number} serviceTypeId - 服務類型ID
   * @param {string} startTime - 開始時間
   * @param {string} endTime - 結束時間
   * @returns {Promise<Object>} 價格計算結果
   */
  async calculateAppointmentPrice(serviceTypeId, startTime, endTime) {
    try {
      const response = await caregiverAppointmentApi.calculateMemberPrice(serviceTypeId, startTime, endTime)
      
      if (response.data && response.data.success) {
        return {
          success: true,
          serviceType: response.data.serviceType,
          serviceHours: response.data.serviceHours,
          hourlyRate: response.data.hourlyRate,
          totalAmount: response.data.totalAmount,
          message: response.data.message,
          memberId: response.data.memberId
        }
      } else {
        throw new Error(response.data.message || '價格計算失敗')
      }
    } catch (error) {
      console.error('價格計算失敗:', error)
      return {
        success: false,
        message: error.response?.data?.message || error.message || '價格計算失敗'
      }
    }
  }

  /**
   * 批量獲取照服員詳細資料
   * @param {Array} caregiverIds - 照服員ID陣列
   * @returns {Object} 照服員資料映射表 {caregiverId: caregiverData}
   */
  async fetchCaregiversData(caregiverIds) {
    const uniqueIds = [...new Set(caregiverIds)]
    const caregiverData = {}
    
    // console.log('準備獲取照服員資料，IDs:', uniqueIds)
    
    // 並行請求所有照服員資料
    const promises = uniqueIds.map(async (id) => {
      try {
        // console.log(`正在請求照服員 ${id} 的資料...`)
        const response = await caregiverApi.getCaregiverById(id)
        // console.log(`照服員 ${id} API 回應:`, response.data)
        
        if (response.data && response.data.success) {
          caregiverData[id] = response.data.data  // 取得實際的照服員資料
        }
      } catch (error) {
        console.warn(`獲取照服員 ${id} 資料失敗:`, error)
        console.error('錯誤詳情:', error.response?.data || error.message)
        // 如果獲取失敗，設置預設值
        caregiverData[id] = {
          id: id,
          name: `照服員 #${id}`,
          avatar: '/images/nurse-avatar.png'
        }
      }
    })
    
    await Promise.all(promises)
    // console.log('最終獲取的照服員資料:', caregiverData)
    return caregiverData
  }

  /**
   * 取得會員預約列表
   * @param {Object} params - 查詢參數
   * @returns {Promise<Object>} 預約列表
   */
  async getMemberAppointments(params = {}) {
    try {
      const response = await caregiverAppointmentApi.getMemberAppointments(params)
      
      if (response.data && response.data.success) {
        // 收集所有照服員ID
        const caregiverIds = response.data.appointments.map(appointment => appointment.caregiverId)
        
        // 批量獲取照服員資料
        const caregiverData = await this.fetchCaregiversData(caregiverIds)
        
        // 格式化預約資料，將照服員資料附加進去
        const formattedAppointments = response.data.appointments.map(appointment => {
          const appointmentWithCaregiver = {
            ...appointment,
            caregiver: caregiverData[appointment.caregiverId]
          }
          return this.formatAppointmentData(appointmentWithCaregiver)
        })
        
        return {
          success: true,
          appointments: formattedAppointments,
          totalCount: response.data.totalCount,
          currentPage: response.data.currentPage,
          memberId: response.data.memberId
        }
      } else {
        return {
          success: false,
          appointments: [],
          message: response.data.message || '取得預約列表失敗'
        }
      }
    } catch (error) {
      console.error('取得預約列表失敗:', error)
      return {
        success: false,
        appointments: [],
        message: error.response?.data?.message || error.message || '取得預約列表失敗'
      }
    }
  }

  /**
   * 取得預約詳情
   * @param {number} appointmentId - 預約ID
   * @returns {Promise<Object>} 預約詳細資料
   */
  async getAppointmentDetail(appointmentId) {
    try {
      const response = await caregiverAppointmentApi.getAppointmentDetail(appointmentId)
      
      if (response.data && response.data.success) {
        const appointment = response.data.appointment
        
        // 如果預約資料中沒有照服員詳細資料，則主動獲取
        if (appointment.caregiverId && !appointment.caregiver) {
          console.log('預約詳情中缺少照服員資料，主動獲取照服員資訊...')
          try {
            const caregiverData = await this.fetchCaregiversData([appointment.caregiverId])
            appointment.caregiver = caregiverData[appointment.caregiverId]
          } catch (error) {
            console.warn('獲取照服員詳細資料失敗:', error)
          }
        }
        
        return {
          success: true,
          appointment: this.formatAppointmentData(appointment),
          availableActions: response.data.availableActions
        }
      } else {
        throw new Error(response.data.message || '取得預約詳情失敗')
      }
    } catch (error) {
      console.error('取得預約詳情失敗:', error)
      return {
        success: false,
        message: error.response?.data?.message || error.message || '取得預約詳情失敗'
      }
    }
  }

  /**
   * 取消預約
   * @param {number} appointmentId - 預約ID
   * @param {string} reason - 取消原因
   * @returns {Promise<Object>} 取消結果
   */
  async cancelAppointment(appointmentId, reason) {
    try {
      const response = await caregiverAppointmentApi.cancelAppointment(appointmentId, reason)
      
      if (response.data && response.data.success) {
        return {
          success: true,
          appointment: this.formatAppointmentData(response.data.appointment),
          message: response.data.message
        }
      } else {
        throw new Error(response.data.message || '取消預約失敗')
      }
    } catch (error) {
      console.error('取消預約失敗:', error)
      return {
        success: false,
        message: error.response?.data?.message || error.message || '取消預約失敗'
      }
    }
  }

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
          appointment: this.formatAppointmentData(response.data.appointment),
          message: response.data.message
        }
      } else {
        throw new Error(response.data.message || '評分失敗')
      }
    } catch (error) {
      console.error('評分失敗:', error)
      return {
        success: false,
        message: error.response?.data?.message || error.message || '評分失敗'
      }
    }
  }

  /**
   * 取得待評分的預約
   * @returns {Promise<Object>} 待評分預約列表
   */
  async getPendingRatings() {
    try {
      const response = await caregiverAppointmentApi.getPendingRatings()
      
      if (response.data && response.data.success) {
        return {
          success: true,
          appointments: response.data.appointments.map(this.formatAppointmentData),
          count: response.data.count,
          message: response.data.message
        }
      } else {
        return {
          success: false,
          appointments: [],
          message: response.data.message || '取得待評分預約失敗'
        }
      }
    } catch (error) {
      console.error('取得待評分預約失敗:', error)
      return {
        success: false,
        appointments: [],
        message: error.response?.data?.message || error.message || '取得待評分預約失敗'
      }
    }
  }

  /**
   * 取得會員預約統計
   * @returns {Promise<Object>} 統計資料
   */
  async getMemberStatistics() {
    try {
      const response = await caregiverAppointmentApi.getMemberStatistics()
      
      if (response.data && response.data.success) {
        return {
          success: true,
          statistics: response.data.statistics,
          memberId: response.data.memberId
        }
      } else {
        return {
          success: false,
          statistics: {},
          message: response.data.message || '取得統計資料失敗'
        }
      }
    } catch (error) {
      console.error('取得統計資料失敗:', error)
      return {
        success: false,
        statistics: {},
        message: error.response?.data?.message || error.message || '取得統計資料失敗'
      }
    }
  }

  /**
   * 格式化預約資料
   * @param {Object} appointment - 原始預約資料
   * @returns {Object} 格式化後的預約資料
   */
  formatAppointmentData(appointment) {
    if (!appointment) return null

    return {
      ...appointment,
      
      // 處理照服員資料
      caregiverName: appointment.caregiver?.chineseName ||  // 新增：使用 chineseName 欄位
                     appointment.caregiver?.name || 
                     appointment.caregiverName || 
                     (appointment.caregiver?.firstName && appointment.caregiver?.lastName ? 
                      `${appointment.caregiver.firstName} ${appointment.caregiver.lastName}` : null) ||
                     appointment.caregiver?.username ||
                     `照服員 #${appointment.caregiverId}`,
      caregiverAvatar: appointment.caregiver?.imagePath ||  // 新增：優先使用 imagePath 欄位
                       appointment.caregiver?.photo ||    // 新增：也檢查 photo 欄位
                       appointment.caregiver?.avatar || 
                       appointment.caregiver?.profileImage ||
                       appointment.caregiverAvatar || 
                       null, // 移除預設圖片，讓前端處理
      // 新增：照服員聯絡電話
      caregiverPhone: appointment.caregiver?.contactNumber ||
                      appointment.caregiver?.phoneNumber ||
                      appointment.caregiver?.phone ||
                      appointment.caregiverPhone ||
                      null,
      // 新增：照服員更多資訊
      caregiverEmail: appointment.caregiver?.email ||
                      appointment.caregiverEmail ||
                      null,
      caregiverExperience: appointment.caregiver?.experienceYears ||
                          appointment.caregiver?.experience ||
                          null,
      
      // 處理服務類型資料
      serviceTypeName: appointment.serviceTypeName ||
                       appointment.serviceType?.name ||
                       appointment.serviceType?.typeName ||
                       this.getServiceTypeDisplayName(appointment.serviceTypeId),
      
      // 格式化時間顯示
      formattedScheduledAt: this.formatDateTime(appointment.scheduledAt),
      formattedEndTime: this.formatDateTime(appointment.endTime),
      formattedCreatedAt: this.formatDateTime(appointment.createdAt),
      formattedUpdatedAt: appointment.updatedAt ? this.formatDateTime(appointment.updatedAt) : null,
      formattedCancelledAt: appointment.cancelledAt ? this.formatDateTime(appointment.cancelledAt) : null,
      formattedRatedAt: appointment.ratedAt ? this.formatDateTime(appointment.ratedAt) : null,
      
      // 狀態顯示文字
      statusDisplay: this.getStatusDisplayText(appointment.status),
      
      // 可操作標記
      canCancel: this.canCancelAppointment(appointment),
      canRate: this.canRateAppointment(appointment),
      
      // 總金額格式化
      formattedTotalAmount: appointment.totalAmount ? `NT$ ${appointment.totalAmount.toLocaleString()}` : 'N/A',
      
      // 服務時長計算（分鐘）
      serviceDurationMinutes: appointment.scheduledAt && appointment.endTime 
        ? Math.round((new Date(appointment.endTime) - new Date(appointment.scheduledAt)) / (1000 * 60))
        : 0,
        
      // 服務時長格式化顯示
      serviceDurationDisplay: appointment.scheduledAt && appointment.endTime 
        ? this.formatDuration(new Date(appointment.endTime) - new Date(appointment.scheduledAt))
        : 'N/A'
    }
  }

  /**
   * 格式化日期時間
   * @param {string} dateTimeString - 日期時間字串
   * @returns {string} 格式化後的日期時間
   */
  formatDateTime(dateTimeString) {
    if (!dateTimeString) return ''
    
    try {
      const date = new Date(dateTimeString)
      return date.toLocaleString('zh-TW', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        weekday: 'short'
      })
    } catch (error) {
      console.error('日期格式化失敗:', error)
      return dateTimeString
    }
  }

  /**
   * 取得狀態顯示文字
   * @param {string} status - 預約狀態
   * @returns {string} 狀態顯示文字
   */
  getStatusDisplayText(status) {
    const statusMap = {
      'pending': '待審核',
      'approved': '已確認',
      'completed': '已完成',
      'cancelled': '已取消',
      'rejected': '已拒絕'
    }
    return statusMap[status] || status
  }

  /**
   * 根據服務類型 ID 獲取顯示名稱
   * @param {number} serviceTypeId - 服務類型ID
   * @returns {string} 服務類型顯示名稱
   */
  getServiceTypeDisplayName(serviceTypeId) {
    const serviceTypeMap = {
      1: '基礎生活照護',
      2: '醫療照護服務', 
      3: '居家清潔服務',
      4: '陪伴關懷服務',
      5: '專業護理照護'
    }
    return serviceTypeMap[serviceTypeId] || `服務類型 #${serviceTypeId}`
  }

  /**
   * 檢查是否可以取消預約
   * @param {Object} appointment - 預約資料
   * @returns {boolean} 是否可取消
   */
  canCancelAppointment(appointment) {
    if (!appointment) return false
    
    const cancellableStatuses = ['pending', 'approved']
    if (!cancellableStatuses.includes(appointment.status)) {
      return false
    }

    // 檢查是否在允許取消的時間範圍內（預約時間前24小時）
    if (appointment.scheduledAt) {
      const now = new Date()
      const scheduledTime = new Date(appointment.scheduledAt)
      const hoursUntilAppointment = (scheduledTime - now) / (1000 * 60 * 60)
      return hoursUntilAppointment >= 24
    }

    return true
  }

  /**
   * 檢查是否可以評分
   * @param {Object} appointment - 預約資料
   * @returns {boolean} 是否可評分
   */
  canRateAppointment(appointment) {
    return appointment && 
           appointment.status === 'completed' && 
           !appointment.isRated
  }

  /**
   * 格式化時間長度
   * @param {number} milliseconds - 毫秒數
   * @returns {string} 格式化的時間長度
   */
  formatDuration(milliseconds) {
    if (!milliseconds || milliseconds <= 0) return '0分鐘'
    
    const totalMinutes = Math.round(milliseconds / (1000 * 60))
    const hours = Math.floor(totalMinutes / 60)
    const minutes = totalMinutes % 60
    
    if (hours > 0 && minutes > 0) {
      return `${hours}小時${minutes}分鐘`
    } else if (hours > 0) {
      return `${hours}小時`
    } else {
      return `${minutes}分鐘`
    }
  }
}

// 建立並匯出服務實例
export const appointmentService = new AppointmentService()
export default appointmentService