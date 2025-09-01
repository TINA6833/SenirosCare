import { serviceTypeApi } from '@/api/serviceTypeApi'

/**
 * 服務類型服務層
 * 負責服務類型相關的業務邏輯處理
 */
export const serviceTypeService = {
  /**
   * 取得所有啟用的服務類型
   * @returns {Promise<Object>} 包含成功狀態和服務類型列表的結果
   */
  async getAllActiveServiceTypes() {
    try {
      const response = await serviceTypeApi.getAll()
      const serviceTypes = response.data || []
      
      // 篩選出啟用的服務類型並格式化
      const activeServiceTypes = serviceTypes
        .filter(type => type.isActive !== false)
        .map(type => this.formatServiceType(type))
      
      return {
        success: true,
        serviceTypes: activeServiceTypes,
        message: '服務類型載入成功'
      }
    } catch (error) {
      console.error('載入服務類型失敗:', error)
      return {
        success: false,
        serviceTypes: [],
        message: error.response?.data?.message || '載入服務類型失敗'
      }
    }
  },

  /**
   * 取得用於下拉選單的服務類型列表
   * @returns {Promise<Object>} 包含成功狀態和格式化的服務類型列表
   */
  async getServiceTypesForDropdown() {
    try {
      const result = await this.getAllActiveServiceTypes()
      
      if (result.success) {
        const dropdownOptions = result.serviceTypes.map(type => ({
          ...type,
          displayText: `${type.serviceName} - ${type.formattedPrice}`,
          value: type.id,
          label: type.serviceName
        }))
        
        return {
          success: true,
          serviceTypes: dropdownOptions,
          message: '服務類型選項載入成功'
        }
      }
      
      return result
    } catch (error) {
      console.error('載入服務類型選項失敗:', error)
      return {
        success: false,
        serviceTypes: [],
        message: '載入服務類型選項失敗'
      }
    }
  },

  /**
   * 根據ID取得特定服務類型
   * @param {number} serviceTypeId - 服務類型ID
   * @returns {Promise<Object>} 包含成功狀態和服務類型詳情的結果
   */
  async getServiceTypeById(serviceTypeId) {
    try {
      const response = await serviceTypeApi.getById(serviceTypeId)
      const serviceType = response.data
      
      if (serviceType) {
        return {
          success: true,
          serviceType: this.formatServiceType(serviceType),
          message: '服務類型詳情載入成功'
        }
      } else {
        throw new Error('找不到指定的服務類型')
      }
    } catch (error) {
      console.error('載入服務類型詳情失敗:', error)
      return {
        success: false,
        serviceType: null,
        message: error.response?.data?.message || '載入服務類型詳情失敗'
      }
    }
  },

  /**
   * 格式化服務類型資料
   * @param {Object} rawServiceType - 原始服務類型資料
   * @returns {Object} 格式化後的服務類型資料
   */
  formatServiceType(rawServiceType) {
    return {
      id: rawServiceType.id || rawServiceType.serviceTypeId,
      serviceName: rawServiceType.serviceName || '未知服務',
      description: rawServiceType.description || '',
      basePrice: parseFloat(rawServiceType.basePrice) || 0,
      pricePerHour: parseFloat(rawServiceType.pricePerHour) || 0,
      minHours: parseInt(rawServiceType.minHours) || 1,
      maxHours: parseInt(rawServiceType.maxHours) || 8,
      isActive: rawServiceType.isActive !== false,
      category: rawServiceType.category || '一般服務',
      
      // 格式化後的顯示文字
      formattedPrice: this.formatPrice(rawServiceType.pricePerHour || rawServiceType.basePrice),
      formattedBasePrice: this.formatPrice(rawServiceType.basePrice),
      formattedPricePerHour: this.formatPrice(rawServiceType.pricePerHour),
      
      // 小時範圍說明
      hourRangeText: `${rawServiceType.minHours || 1}-${rawServiceType.maxHours || 8}小時`,
      
      // 完整的顯示文字
      fullDisplayText: `${rawServiceType.serviceName} (${this.formatPrice(rawServiceType.pricePerHour || rawServiceType.basePrice)}/小時)`
    }
  },

  /**
   * 格式化價格顯示
   * @param {number} price - 價格
   * @returns {string} 格式化後的價格字串
   */
  formatPrice(price) {
    if (!price || isNaN(price)) return 'NT$ 0'
    return `NT$ ${parseInt(price).toLocaleString()}`
  },

  /**
   * 計算服務總價
   * @param {number} pricePerHour - 每小時價格
   * @param {number} hours - 服務小時數
   * @param {number} basePrice - 基本價格（可選）
   * @returns {Object} 價格計算結果
   */
  calculateTotalPrice(pricePerHour, hours, basePrice = 0) {
    const hourlyRate = parseFloat(pricePerHour) || 0
    const serviceHours = parseFloat(hours) || 1
    const base = parseFloat(basePrice) || 0
    
    const subtotal = hourlyRate * serviceHours
    const total = base + subtotal
    
    return {
      basePrice: base,
      hourlyRate: hourlyRate,
      serviceHours: serviceHours,
      subtotal: subtotal,
      total: total,
      formattedBasePrice: this.formatPrice(base),
      formattedHourlyRate: this.formatPrice(hourlyRate),
      formattedSubtotal: this.formatPrice(subtotal),
      formattedTotal: this.formatPrice(total)
    }
  },

  /**
   * 取得預設的服務時長選項
   * @returns {Array} 時長選項陣列
   */
  getDefaultHourOptions() {
    return [
      { value: 1, label: '1小時' },
      { value: 2, label: '2小時' },
      { value: 3, label: '3小時' },
      { value: 4, label: '4小時' },
      { value: 6, label: '6小時' },
      { value: 8, label: '8小時' },
      { value: 12, label: '12小時（半日）' },
      { value: 24, label: '24小時（全日）' }
    ]
  },

  /**
   * 驗證服務時長是否在允許範圍內
   * @param {number} hours - 服務時長
   * @param {number} minHours - 最小時長
   * @param {number} maxHours - 最大時長
   * @returns {Object} 驗證結果
   */
  validateServiceHours(hours, minHours = 1, maxHours = 24) {
    const serviceHours = parseFloat(hours)
    
    if (isNaN(serviceHours) || serviceHours <= 0) {
      return {
        valid: false,
        message: '請輸入有效的服務時長'
      }
    }
    
    if (serviceHours < minHours) {
      return {
        valid: false,
        message: `服務時長不能少於 ${minHours} 小時`
      }
    }
    
    if (serviceHours > maxHours) {
      return {
        valid: false,
        message: `服務時長不能超過 ${maxHours} 小時`
      }
    }
    
    return {
      valid: true,
      message: '服務時長有效'
    }
  },

  /**
   * 計算結束時間
   * @param {string} startTime - 開始時間 (ISO 格式或 HH:mm)
   * @param {number} hours - 服務時長
   * @returns {string} 結束時間 (ISO 格式)
   */
  calculateEndTime(startTime, hours) {
    let startDate
    
    // 處理不同的時間格式
    if (startTime.includes('T')) {
      // ISO 格式
      startDate = new Date(startTime)
    } else if (startTime.includes(':')) {
      // 時間格式 HH:mm，假設是今天
      const today = new Date()
      const [hour, minute] = startTime.split(':')
      startDate = new Date(today.getFullYear(), today.getMonth(), today.getDate(), 
                          parseInt(hour), parseInt(minute))
    } else {
      throw new Error('無效的時間格式')
    }
    
    if (isNaN(startDate.getTime())) {
      throw new Error('無效的開始時間')
    }
    
    // 加上服務時長
    const endDate = new Date(startDate.getTime() + (hours * 60 * 60 * 1000))
    
    return endDate.toISOString()
  },

  /**
   * 格式化時間為 API 所需格式
   * @param {Date|string} dateTime - 日期時間
   * @returns {string} 格式化後的時間字串
   */
  formatTimeForAPI(dateTime) {
    let date
    
    if (dateTime instanceof Date) {
      date = dateTime
    } else if (typeof dateTime === 'string') {
      date = new Date(dateTime)
    } else {
      throw new Error('無效的日期時間格式')
    }
    
    if (isNaN(date.getTime())) {
      throw new Error('無效的日期時間')
    }
    
    return date.toISOString()
  },

  /**
   * 取得服務類型的分類列表
   * @returns {Array} 分類選項陣列
   */
  getServiceCategories() {
    return [
      { value: 'basic', label: '基本照護', description: '日常生活協助' },
      { value: 'medical', label: '醫療照護', description: '專業醫療相關服務' },
      { value: 'companion', label: '陪伴服務', description: '社交陪伴與心理支持' },
      { value: 'housework', label: '家務協助', description: '家庭清潔與整理' },
      { value: 'transport', label: '交通接送', description: '出行協助服務' },
      { value: 'emergency', label: '緊急服務', description: '緊急情況處理' }
    ]
  },

  /**
   * 根據分類篩選服務類型
   * @param {Array} serviceTypes - 服務類型列表
   * @param {string} category - 分類
   * @returns {Array} 篩選後的服務類型列表
   */
  filterByCategory(serviceTypes, category) {
    if (!category || category === 'all') {
      return serviceTypes
    }
    
    return serviceTypes.filter(type => type.category === category)
  },

  /**
   * 搜尋服務類型
   * @param {Array} serviceTypes - 服務類型列表
   * @param {string} keyword - 搜尋關鍵字
   * @returns {Array} 搜尋結果
   */
  searchServiceTypes(serviceTypes, keyword) {
    if (!keyword || keyword.trim() === '') {
      return serviceTypes
    }
    
    const searchTerm = keyword.toLowerCase().trim()
    
    return serviceTypes.filter(type => 
      type.serviceName.toLowerCase().includes(searchTerm) ||
      type.description.toLowerCase().includes(searchTerm) ||
      type.category.toLowerCase().includes(searchTerm)
    )
  },

  /**
   * 排序服務類型
   * @param {Array} serviceTypes - 服務類型列表
   * @param {string} sortBy - 排序依據 (name, price, category)
   * @param {string} order - 排序順序 (asc, desc)
   * @returns {Array} 排序後的服務類型列表
   */
  sortServiceTypes(serviceTypes, sortBy = 'name', order = 'asc') {
    const sorted = [...serviceTypes].sort((a, b) => {
      let comparison = 0
      
      switch (sortBy) {
        case 'name':
          comparison = a.serviceName.localeCompare(b.serviceName, 'zh-TW')
          break
        case 'price':
          comparison = a.pricePerHour - b.pricePerHour
          break
        case 'category':
          comparison = a.category.localeCompare(b.category, 'zh-TW')
          break
        default:
          comparison = 0
      }
      
      return order === 'desc' ? -comparison : comparison
    })
    
    return sorted
  }
}

export default serviceTypeService
