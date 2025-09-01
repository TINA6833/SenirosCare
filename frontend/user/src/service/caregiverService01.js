import { caregiverApi } from '@/api/caregiverApi'

/**
 * 照服員服務層
 * 負責業務邏輯處理和資料型別轉換
 */
export const caregiverService = {
  /**
   * 取得照服員詳細資料並進行資料轉換
   * @param {number} caregiverId - 照服員ID
   * @returns {Promise<Object>} 轉換後的照服員資料
   */
  async getCaregiverDetails(caregiverId) {
    try {
      // 呼叫 API 層取得原始資料
      const response = await caregiverApi.getCaregiverById(caregiverId)
      const rawData = response.data
      
      // 進行資料轉換和格式化
      return {
        ...rawData,
        // 確保評分為數字格式
        avgRating: rawData.avgRating ? parseFloat(rawData.avgRating) : 0,
        totalRatings: rawData.totalRatings ? parseInt(rawData.totalRatings) : 0,
        
        // 確保經驗年數為數字
        experienceYears: rawData.experienceYears ? parseInt(rawData.experienceYears) : 0,
        
        // 處理服務區域，確保有預設值
        serviceArea: rawData.serviceArea || '未指定區域',
        
        // 格式化電話號碼
        phone: rawData.phone || '',
        
        // 處理圖片路徑
        imagePath: rawData.imagePath || null,
        
        // 確保基本資訊存在
        chineseName: rawData.chineseName || '未知姓名',
        englishName: rawData.englishName || '',
        
        // 處理專業技能列表
        skills: Array.isArray(rawData.skills) ? rawData.skills : [],
        
        // 處理證照列表
        certifications: Array.isArray(rawData.certifications) ? rawData.certifications : [],
        
        // 處理服務類型
        serviceTypes: Array.isArray(rawData.serviceTypes) ? rawData.serviceTypes : [],
        
        // 格式化自我介紹
        selfIntroduction: rawData.selfIntroduction || '尚未提供自我介紹',
        
        // 處理可用時間
        availableTime: rawData.availableTime || null,
        
        // 處理時薪
        hourlyRate: rawData.hourlyRate ? parseFloat(rawData.hourlyRate) : 0
      }
    } catch (error) {
      console.error('照服員服務層錯誤:', error)
      
      // 根據不同錯誤類型拋出適當的錯誤訊息
      if (error.response?.status === 404) {
        throw new Error('找不到指定的照服員')
      } else if (error.response?.status >= 500) {
        throw new Error('伺服器暫時無法回應，請稍後再試')
      } else {
        throw new Error('無法載入照服員資料，請檢查網路連線')
      }
    }
  },

  /**
   * 取得照服員可用時間
   * @param {number} caregiverId - 照服員ID
   * @param {string} date - 查詢日期 (YYYY-MM-DD)
   * @returns {Promise<Array>} 可用時間段列表
   */
  async getCaregiverAvailableSlots(caregiverId, date) {
    try {
      const response = await caregiverApi.getAvailableSlots(caregiverId, date)
      return response.data || []
    } catch (error) {
      console.error('取得可用時間失敗:', error)
      throw new Error('無法取得照服員的可用時間')
    }
  },

  /**
   * 檢查照服員時間可用性
   * @param {number} caregiverId - 照服員ID
   * @param {string} startTime - 開始時間 (ISO格式)
   * @param {string} endTime - 結束時間 (ISO格式)
   * @returns {Promise<boolean>} 是否可用
   */
  async checkCaregiverAvailability(caregiverId, startTime, endTime) {
    try {
      const response = await caregiverApi.checkAvailability(caregiverId, startTime, endTime)
      return response.data?.available || false
    } catch (error) {
      console.error('檢查可用性失敗:', error)
      throw new Error('無法檢查照服員的可用性')
    }
  },

  /**
   * 取得照服員行程表
   * @param {number} caregiverId - 照服員ID
   * @param {string} startDate - 開始日期時間 (ISO格式)
   * @param {string} endDate - 結束日期時間 (ISO格式)
   * @returns {Promise<Array>} 行程表資料
   */
  async getCaregiverSchedule(caregiverId, startDate, endDate) {
    try {
      const response = await caregiverApi.getCaregiverSchedule(caregiverId, startDate, endDate)
      return response.data || []
    } catch (error) {
      console.error('取得行程表失敗:', error)
      throw new Error('無法取得照服員的行程表')
    }
  },

  /**
   * 取得照服員評價列表
   * @param {number} caregiverId - 照服員ID
   * @param {number} limit - 限制筆數
   * @param {number} offset - 偏移量
   * @returns {Promise<Object>} 評價列表和統計資料
   */
  async getCaregiverRatings(caregiverId, limit = 10, offset = 0) {
    try {
      const response = await caregiverApi.getCaregiverRatings(caregiverId, limit, offset)
      const data = response.data || {}
      
      return {
        ratings: data.ratings || [],
        totalCount: data.totalCount || 0,
        avgRating: data.avgRating ? parseFloat(data.avgRating) : 0,
        ratingDistribution: data.ratingDistribution || {
          5: 0, 4: 0, 3: 0, 2: 0, 1: 0
        }
      }
    } catch (error) {
      console.error('取得評價失敗:', error)
      throw new Error('無法取得照服員的評價資料')
    }
  },

  /**
   * 取得所有照服員列表
   * @param {Object} filters - 篩選條件
   * @returns {Promise<Array>} 照服員列表
   */
  async getAllCaregivers(filters = {}) {
    try {
      const response = await caregiverApi.getAllCaregivers()
      let caregivers = response.data || []
      
      // 套用篩選條件
      if (filters.area) {
        caregivers = caregivers.filter(c => c.serviceArea === filters.area)
      }
      
      if (filters.minRating) {
        caregivers = caregivers.filter(c => (c.avgRating || 0) >= filters.minRating)
      }
      
      if (filters.maxHourlyRate) {
        caregivers = caregivers.filter(c => (c.hourlyRate || 0) <= filters.maxHourlyRate)
      }
      
      // 資料轉換
      return caregivers.map(caregiver => ({
        ...caregiver,
        avgRating: caregiver.avgRating ? parseFloat(caregiver.avgRating) : 0,
        totalRatings: caregiver.totalRatings ? parseInt(caregiver.totalRatings) : 0,
        experienceYears: caregiver.experienceYears ? parseInt(caregiver.experienceYears) : 0,
        hourlyRate: caregiver.hourlyRate ? parseFloat(caregiver.hourlyRate) : 0
      }))
      
    } catch (error) {
      console.error('取得照服員列表失敗:', error)
      throw new Error('無法取得照服員列表')
    }
  },

  /**
   * 根據服務區域搜尋照服員
   * @param {string} area - 服務區域
   * @returns {Promise<Array>} 照服員列表
   */
  async searchCaregiversByArea(area) {
    try {
      const response = await caregiverApi.getCaregiversByArea(area)
      return response.data || []
    } catch (error) {
      console.error('根據區域搜尋失敗:', error)
      throw new Error('無法搜尋該區域的照服員')
    }
  },

  /**
   * 根據姓名搜尋照服員
   * @param {string} name - 照服員姓名
   * @returns {Promise<Array>} 照服員列表
   */
  async searchCaregiversByName(name) {
    try {
      const response = await caregiverApi.searchCaregiversByName(name)
      return response.data || []
    } catch (error) {
      console.error('根據姓名搜尋失敗:', error)
      throw new Error('無法搜尋該姓名的照服員')
    }
  }
}

export default caregiverService
