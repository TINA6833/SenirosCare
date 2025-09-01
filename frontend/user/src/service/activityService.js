// [重點] 更新 import 方式，改用具名匯入或預設匯入
import { 
  getAllActivities, 
  getActivityById, 
  bookActivity, 
  getMyReservations, 
  cancelReservation 
} from '@/api/activityApi'

export const activityService = {
  /**
   * 取得所有活動清單
   * @returns {Promise<Array>} 處理過的活動清單
   */
  async getAllActivities() {
    try {
      // [重點] 使用具名匯入的函數
      const response = await getAllActivities()
      // 轉換活動資料，加上計算欄位
      return response.data.map(activity => ({
        ...activity,
        // 拼接完整圖片網址 - 後端圖片路徑格式：/images/檔名
        imageUrl: activity.image ? `http://localhost:8080${activity.image}` : null,
        // 判斷活動是否已額滿
        isFull: activity.current >= activity.limit,
        // 判斷是否在報名期間
        isRegistrationOpen: this.isRegistrationPeriod(activity.registrationStart, activity.registrationEnd),
        // 格式化顯示用的日期範圍
        displayDate: this.formatDateRange(activity.date, activity.end),
        // 計算剩餘名額
        remainingSlots: Math.max(0, activity.limit - activity.current),
        // 判斷活動是否已結束
        isExpired: new Date(activity.end) < new Date()
      }))
    } catch (error) {
      console.error('取得活動清單失敗:', error)
      throw new Error('無法取得活動清單，請稍後再試')
    }
  },

  /**
   * 根據ID取得單一活動詳情
   * @param {number} id 活動ID
   * @returns {Promise<Object>} 處理過的活動詳情
   */
  async getActivityById(id) {
    try {
      // 驗證活動ID
      if (!id || isNaN(id)) {
        throw new Error('活動ID格式錯誤')
      }

      // [重點] 使用具名匯入的函數
      const response = await getActivityById(id)
      const activity = response.data
      
      return {
        ...activity,
        // 拼接完整圖片網址
        imageUrl: activity.image ? `http://localhost:8080${activity.image}` : null,
        // 活動狀態計算
        isFull: activity.current >= activity.limit,
        isRegistrationOpen: this.isRegistrationPeriod(activity.registrationStart, activity.registrationEnd),
        displayDate: this.formatDateRange(activity.date, activity.end),
        remainingSlots: Math.max(0, activity.limit - activity.current),
        isExpired: new Date(activity.end) < new Date(),
        // 判斷是否可以預約
        canBook: this.canBookActivity(activity)
      }
    } catch (error) {
      console.error('取得活動詳情失敗:', error)
      if (error.response?.status === 404) {
        throw new Error('找不到指定的活動')
      }
      throw new Error('無法取得活動詳情')
    }
  },

  /**
   * 預約活動
   * @param {Object} reservationData 預約資料
   * @param {number} reservationData.activityId 活動ID
   * @param {number} reservationData.num 預約人數 (預設為1)
   * @returns {Promise<boolean>} 預約結果
   */
  async bookActivity(reservationData) {
    try {
      // [重點] 驗證預約資料
      this.validateReservationData(reservationData)

      // [重點] 準備符合 ReservationCreateDto 的資料格式
      const bookingData = {
        activityId: reservationData.activityId,
        // [重點] 確保 num 有值，且為數字類型
        num: Number(reservationData.num) || 1
      }

      // [重點] 在發送前檢查資料
      console.log('發送預約資料:', bookingData)

      // [重點] 使用具名匯入的函數
      const response = await bookActivity(bookingData)
      
      // [重點] 檢查回應狀態
      console.log('預約回應:', response)
      
      return response.status === 200 || response.status === 201
    } catch (error) {
      console.error('預約活動失敗:', error)
      
      // 處理不同類型的錯誤
      if (error.response?.status === 400) {
        throw new Error('預約失敗，可能是活動已額滿或不在報名期間')
      } else if (error.response?.status === 404) {
        throw new Error('找不到指定的活動')
      }
      
      throw new Error(error.message || '預約失敗，請稍後再試')
    }
  },

  /**
   * 取得我的預約清單
   * @returns {Promise<Array>} 處理過的預約清單
   */
  async getMyReservations() {
    try {
      // [重點] 使用具名匯入的函數
      const response = await getMyReservations()
      
      // [重點] 加入調試資訊
      console.log('API 回傳的原始預約資料:', response.data)
      
      // 處理預約資料，加上狀態顯示和操作權限
      return response.data.map(reservation => {
        // [重點] 保留原始狀態，不在此階段轉換
        const processedReservation = {
          ...reservation,
          // [重點] 保留原始狀態用於邏輯判斷
          originalStatus: reservation.status,
          // 轉換狀態顯示文字
          statusText: this.getStatusText(reservation.status),
          // 格式化預約時間
          formattedScheduledAt: this.formatDateTime(reservation.scheduledAt),
          // 判斷是否可以取消 - 使用原始狀態
          canCancel: this.canCancelReservation(reservation.status)
        }
        
        console.log('處理後的預約:', {
          id: processedReservation.id,
          originalStatus: processedReservation.originalStatus,
          statusText: processedReservation.statusText,
          canCancel: processedReservation.canCancel
        })
        
        return processedReservation
      })
    } catch (error) {
      console.error('取得預約清單失敗:', error)
      
      if (error.response?.status === 404) {
        // 404表示沒有預約記錄，回傳空陣列
        return []
      }
      
      throw new Error('無法取得預約清單')
    }
  },

  /**
   * 取消預約
   * @param {number} activityId 活動ID - 前端提供，後端會用當前使用者ID+活動ID找到預約紀錄
   * @returns {Promise<boolean>} 取消結果
   */
  async cancelReservation(activityId) {
    try {
      // [重點] 驗證活動ID而非預約ID
      if (!activityId || isNaN(activityId)) {
        throw new Error('活動ID格式錯誤')
      }

      // [重點] 加入調試資訊
      console.log('準備取消預約 - 活動ID:', activityId)

      // [重點] 使用具名匯入的函數，傳入活動ID
      const response = await cancelReservation(activityId)
      
      // [重點] 檢查回應狀態
      console.log('取消預約 API 回應:', {
        status: response.status,
        data: response.data
      })
      
      return response.status === 200
    } catch (error) {
      console.error('取消預約失敗 - Service 層錯誤:', {
        error: error,
        message: error?.message,
        response: error?.response,
        status: error?.response?.status,
        data: error?.response?.data,
        activityId: activityId
      })
      
      // [重點] 處理不同的錯誤狀況
      if (error.response?.status === 400) {
        throw new Error('無法取消預約，可能是預約狀態不允許或活動不存在')
      } else if (error.response?.status === 404) {
        throw new Error('找不到您對此活動的預約記錄')
      } else if (error.response?.status === 403) {
        throw new Error('您沒有權限取消此預約')
      }
      
      throw new Error(error?.response?.data?.message || error?.message || '取消預約失敗，請稍後再試')
    }
  },

  // ==================== 私有輔助方法 ====================

  /**
   * 驗證預約資料
   * @param {Object} reservationData 預約資料
   */
  validateReservationData(reservationData) {
    if (!reservationData) {
      throw new Error('預約資料不能為空')
    }
    
    if (!reservationData.activityId || isNaN(reservationData.activityId)) {
      throw new Error('活動ID格式錯誤')
    }
    
    // [重點] 加強 num 欄位驗證
    if (!reservationData.num || isNaN(reservationData.num) || reservationData.num <= 0) {
      throw new Error('預約人數必須大於0')
    }
  },

  /**
   * 判斷是否在報名期間內
   * @param {string} startDate 報名開始日期
   * @param {string} endDate 報名結束日期
   * @returns {boolean} 是否在報名期間
   */
  isRegistrationPeriod(startDate, endDate) {
    if (!startDate || !endDate) return false
    
    const now = new Date()
    const start = new Date(startDate)
    const end = new Date(endDate)
    end.setHours(23, 59, 59, 999) // 設定為當天最後一刻
    
    return now >= start && now <= end
  },

  /**
   * 判斷活動是否可以預約
   * @param {Object} activity 活動資料
   * @returns {boolean} 是否可以預約
   */
  canBookActivity(activity) {
    // 首先檢查活動狀態是否啟用
    if (!activity.status) return false
    
    // 檢查活動是否已結束
    if (new Date(activity.end) < new Date()) return false
    
    // 檢查是否在報名期間內
    if (!this.isRegistrationPeriod(activity.registrationStart, activity.registrationEnd)) return false
    
    // 檢查是否額滿
    if (activity.current >= activity.limit) return false
    
    return true
  },

  /**
   * 格式化日期範圍顯示
   * @param {string} startDate 開始日期
   * @param {string} endDate 結束日期
   * @returns {string} 格式化後的日期範圍
   */
  formatDateRange(startDate, endDate) {
    if (!startDate) return '日期未定'
    
    const start = new Date(startDate).toLocaleDateString('zh-TW')
    
    if (!endDate || startDate === endDate) {
      return start
    }
    
    const end = new Date(endDate).toLocaleDateString('zh-TW')
    return `${start} ~ ${end}`
  },

  /**
   * 格式化日期時間
   * @param {string} dateTime ISO日期時間字串
   * @returns {string} 格式化後的日期時間
   */
  formatDateTime(dateTime) {
    if (!dateTime) return '時間未定'
    
    return new Date(dateTime).toLocaleString('zh-TW', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  },

  /**
   * 取得預約狀態顯示文字
   * @param {string} status 預約狀態
   * @returns {string} 狀態顯示文字
   */
  getStatusText(status) {
    // [重點] 狀態對照表
    const statusMap = {
      '預約審核中': '預約審核中',
      '報名審核中': '預約審核中',
      '審核中': '預約審核中',
      '預約成功': '預約成功', 
      '報名完成': '預約成功',
      '成功': '預約成功',
      '取消預約': '已取消',
      '已取消': '已取消',
      '取消': '已取消'
    }
    
    const result = statusMap[status] || status || '未知狀態'
    console.log('狀態文字轉換:', status, '->', result)
    
    return result
  },

  /**
   * 判斷預約是否可以取消
   * @param {string} status 預約狀態 - 使用原始後端狀態
   * @returns {boolean} 是否可以取消
   */
  canCancelReservation(status) {
    // [重點] 簡化邏輯：只有預約審核中才能取消
    const canCancel = status === '預約審核中'
    
    console.log('取消權限檢查:', {
      status: status,
      canCancel: canCancel,
      isReviewing: status === '預約審核中'
    })
    
    return canCancel
  }
}