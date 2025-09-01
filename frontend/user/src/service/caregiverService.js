import axiosInstance from '@/api/axiosInstance';

/**
 * 照服員業務邏輯處理
 * 負責照服員資料的格式化與業務邏輯
 */
export const caregiverService = {
  /**
   * 格式化照服員列表資料
   * @param {Array} caregivers - 原始照服員資料
   * @returns {Array} 格式化後的照服員資料
   */
  formatCaregiverList(caregivers) {
    return caregivers.map(caregiver => ({
      id: caregiver.id,
      name: caregiver.name,
      avatar: this.getAvatarUrl(caregiver.photoUrl),
      serviceArea: caregiver.serviceArea,
      rating: caregiver.averageRating || 0,
      totalRatings: caregiver.totalRatings || 0,
      isActive: caregiver.isActive,
      phone: caregiver.phone,
      experienceYears: caregiver.experienceYears || 0,
      introduction: caregiver.introduction || '',
      // 格式化顯示用資料
      formattedRating: this.formatRating(caregiver.averageRating, caregiver.totalRatings),
      serviceAreaDisplay: this.formatServiceArea(caregiver.serviceArea),
      experienceDisplay: this.formatExperience(caregiver.experienceYears),
      ratingStars: this.generateRatingStars(caregiver.averageRating)
    }))
  },

  /**
   * 格式化照服員詳細資料
   * @param {Object} caregiver - 原始照服員詳細資料
   * @returns {Object} 格式化後的詳細資料
   */
  formatCaregiverDetail(caregiver) {
    return {
      ...caregiver,
      avatar: this.getAvatarUrl(caregiver.photoUrl),
      formattedPhone: this.formatPhone(caregiver.phone),
      formattedRating: this.formatRating(caregiver.averageRating, caregiver.totalRatings),
      experienceDisplay: this.formatExperience(caregiver.experienceYears),
      specialtiesDisplay: this.formatSpecialties(caregiver.specialties),
      certificationsDisplay: this.formatCertifications(caregiver.certifications),
      serviceAreaDisplay: this.formatServiceArea(caregiver.serviceArea),
      ratingStars: this.generateRatingStars(caregiver.averageRating),
      introductionFormatted: caregiver.introduction || '這位照服員還沒有填寫自我介紹。'
    }
  },

  /**
   * 取得大頭貼 URL
   * @param {string} photoUrl - 照片路徑
   * @returns {string} 完整的圖片 URL
   */
  getAvatarUrl(photoUrl) {
    if (!photoUrl) return '/images/nurse-avatar.png';
    const baseUrl = process.env.VUE_APP_API_BASE_URL || 'http://localhost:8080';
    return photoUrl.startsWith('http') ? photoUrl : `${baseUrl}${photoUrl}`;
  },

  /**
   * 格式化評分顯示
   * @param {number} rating - 評分
   * @param {number} totalRatings - 總評分次數
   * @returns {string} 格式化後的評分
   */
  formatRating(rating, totalRatings = 0) {
    if (!rating || totalRatings === 0) return '尚無評分'
    return `${rating.toFixed(1)} ⭐ (${totalRatings}則評價)`
  },

  /**
   * 生成評分星星
   * @param {number} rating - 評分 (0-5)
   * @returns {Array} 星星陣列
   */
  generateRatingStars(rating) {
    const stars = []
    const fullStars = Math.floor(rating || 0)
    const hasHalfStar = (rating || 0) % 1 >= 0.5
    
    // 滿星
    for (let i = 0; i < fullStars; i++) {
      stars.push({ type: 'full', key: `full-${i}` })
    }
    
    // 半星
    if (hasHalfStar) {
      stars.push({ type: 'half', key: 'half' })
    }
    
    // 空星
    const emptyStars = 5 - Math.ceil(rating || 0)
    for (let i = 0; i < emptyStars; i++) {
      stars.push({ type: 'empty', key: `empty-${i}` })
    }
    
    return stars
  },

  /**
   * 格式化服務區域
   * @param {string} area - 服務區域
   * @returns {string} 格式化後的區域
   */
  formatServiceArea(area) {
    if (!area) return '服務區域未設定'
    // 可以根據需求進一步格式化，例如將代碼轉換為中文名稱
    return area
  },

  /**
   * 格式化經驗年數
   * @param {number} years - 經驗年數
   * @returns {string} 格式化後的經驗
   */
  formatExperience(years) {
    if (!years || years === 0) return '新手照服員'
    return `${years} 年經驗`
  },

  /**
   * 格式化專長
   * @param {Array} specialties - 專長陣列
   * @returns {string} 格式化後的專長
   */
  formatSpecialties(specialties) {
    if (!specialties || specialties.length === 0) return '無特殊專長'
    return specialties.join(' • ')
  },

  /**
   * 格式化證照
   * @param {Array} certifications - 證照陣列
   * @returns {string} 格式化後的證照
   */
  formatCertifications(certifications) {
    if (!certifications || certifications.length === 0) return '無相關證照'
    return certifications.join(' • ')
  },

  /**
   * 格式化電話號碼
   * @param {string} phone - 電話號碼
   * @returns {string} 格式化後的電話
   */
  formatPhone(phone) {
    if (!phone) return ''
    // 格式化為 09XX-XXX-XXX
    const cleaned = phone.replace(/\D/g, '') // 移除非數字字符
    if (cleaned.length === 10) {
      return cleaned.replace(/(\d{4})(\d{3})(\d{3})/, '$1-$2-$3')
    }
    return phone
  },

  /**
   * 格式化行程表資料 (根據附圖API格式)
   * @param {Array} scheduleData - 原始行程表資料
   * @returns {Object} 格式化後的行程表 {date: [timeSlots]}
   */
  formatScheduleData(scheduleData) {
    const formattedSchedule = {}
    
    if (!scheduleData || !Array.isArray(scheduleData)) {
      return formattedSchedule
    }
    
    scheduleData.forEach(schedule => {
      // 根據API回傳格式進行解析
      const scheduleDate = schedule.scheduledAt ? 
        schedule.scheduledAt.split('T')[0] : // 從scheduledAt提取日期部分
        schedule.date
      
      if (!formattedSchedule[scheduleDate]) {
        formattedSchedule[scheduleDate] = []
      }
      
      formattedSchedule[scheduleDate].push({
        id: schedule.id,
        startTime: schedule.scheduledAt,
        endTime: schedule.endTime,
        isAvailable: !schedule.isBlocked && schedule.status === 'approved',
        isBlocked: schedule.isBlocked,
        status: schedule.status,
        notes: schedule.notes || '',
        memberId: schedule.memberId,
        serviceTypeId: schedule.serviceTypeId,
        // 格式化時間顯示
        timeSlot: this.formatTimeSlot(schedule.scheduledAt, schedule.endTime),
        statusDisplay: this.getScheduleStatusDisplay(schedule.status, schedule.isBlocked)
      })
    })
    
    return formattedSchedule
  },

  /**
   * 格式化時間段顯示
   * @param {string} startTime - 開始時間
   * @param {string} endTime - 結束時間
   * @returns {string} 格式化的時間段
   */
  formatTimeSlot(startTime, endTime) {
    if (!startTime || !endTime) return ''
    
    const start = new Date(startTime)
    const end = new Date(endTime)
    
    const formatTime = (date) => {
      return date.toLocaleTimeString('zh-TW', {
        hour: '2-digit',
        minute: '2-digit',
        hour12: false
      })
    }
    
    return `${formatTime(start)} - ${formatTime(end)}`
  },

  /**
   * 取得行程狀態顯示
   * @param {string} status - 狀態
   * @param {boolean} isBlocked - 是否被封鎖
   * @returns {string} 狀態顯示文字
   */
  getScheduleStatusDisplay(status, isBlocked) {
    if (isBlocked) return '不可預約'
    
    const statusMap = {
      'approved': '可預約',
      'pending': '待確認',
      'rejected': '已拒絕',
      'completed': '已完成',
      'cancelled': '已取消'
    }
    
    return statusMap[status] || '未知狀態'
  },

  /**
   * 生成月份的可用日期
   * @param {number} year - 年份
   * @param {number} month - 月份 (0-11)
   * @returns {Array} 日期陣列
   */
  generateMonthDates(year, month) {
    const dates = []
    const daysInMonth = new Date(year, month + 1, 0).getDate()
    const today = new Date()
    
    for (let day = 1; day <= daysInMonth; day++) {
      const date = new Date(year, month, day)
      const dateString = date.toISOString().split('T')[0]
      
      dates.push({
        date: dateString,
        day: day,
        isToday: date.toDateString() === today.toDateString(),
        isPast: date < today,
        isWeekend: date.getDay() === 0 || date.getDay() === 6,
        weekday: date.toLocaleDateString('zh-TW', { weekday: 'short' })
      })
    }
    
    return dates
  },

  /**
   * 取得照服員詳細資料
   * @param {number} id - 照服員ID
   * @returns {Promise<Object>} 照服員詳細資料
   */
  getCaregiverById(id) {
    return axiosInstance.get(`/caregivers/${id}`).then((response) => {
      const caregiver = response.data;
      return {
        ...caregiver,
        avatar: this.getAvatarUrl(caregiver.photoUrl),
      };
    });
  }
}
