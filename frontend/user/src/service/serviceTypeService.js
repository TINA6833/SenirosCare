import { serviceTypeApi } from '@/api/serviceTypeApi'

/**
 * 服務類型服務層
 * 負責處理服務類型相關的業務邏輯和資料轉換
 */
class ServiceTypeService {

  /**
   * 取得所有啟用的服務類型
   * @returns {Promise<Object>} 處理後的服務類型列表
   */
  async getAllActiveServiceTypes() {
    try {
      const response = await serviceTypeApi.getAll()
      
      if (response.data && response.data.success) {
        return {
          success: true,
          serviceTypes: response.data.data.map(this.formatServiceTypeData),
          total: response.data.total,
          message: response.data.message
        }
      } else {
        return {
          success: false,
          serviceTypes: [],
          message: response.data.message || '取得服務類型失敗'
        }
      }
    } catch (error) {
      console.error('取得服務類型失敗:', error)
      return {
        success: false,
        serviceTypes: [],
        message: error.response?.data?.message || error.message || '取得服務類型失敗'
      }
    }
  }

  /**
   * 取得服務類型下拉選單資料
   * @returns {Promise<Object>} 處理後的下拉選單資料
   */
  async getServiceTypesForDropdown() {
    try {
      const response = await serviceTypeApi.getDropdown()
      
      if (response.data && response.data.success) {
        return {
          success: true,
          serviceTypes: response.data.data.map(item => ({
            id: item.id,
            name: item.name,
            hourlyRate: item.hourlyRate,
            description: item.description,
            // 格式化顯示文字，供下拉選單使用
            displayText: `${item.name} - NT$${item.hourlyRate}/小時`,
            // 格式化價格顯示
            formattedHourlyRate: `NT$ ${item.hourlyRate.toLocaleString()}`
          })),
          total: response.data.total,
          message: response.data.message
        }
      } else {
        return {
          success: false,
          serviceTypes: [],
          message: response.data.message || '取得服務類型選單失敗'
        }
      }
    } catch (error) {
      console.error('取得服務類型選單失敗:', error)
      return {
        success: false,
        serviceTypes: [],
        message: error.response?.data?.message || error.message || '取得服務類型選單失敗'
      }
    }
  }

  /**
   * 根據ID取得特定服務類型詳情
   * @param {number} serviceTypeId - 服務類型ID
   * @returns {Promise<Object>} 處理後的服務類型詳情
   */
  async getServiceTypeById(serviceTypeId) {
    try {
      const response = await serviceTypeApi.getById(serviceTypeId)
      
      if (response.data && response.data.success) {
        return {
          success: true,
          serviceType: this.formatServiceTypeData(response.data.data),
          message: response.data.message
        }
      } else {
        return {
          success: false,
          serviceType: null,
          message: response.data.message || '取得服務類型詳情失敗'
        }
      }
    } catch (error) {
      console.error('取得服務類型詳情失敗:', error)
      return {
        success: false,
        serviceType: null,
        message: error.response?.data?.message || error.message || '取得服務類型詳情失敗'
      }
    }
  }

  /**
   * 計算服務價格預覽
   * @param {number} serviceTypeId - 服務類型ID
   * @param {string} startTime - 開始時間 (YYYY-MM-DDTHH:mm:ss)
   * @param {string} endTime - 結束時間 (YYYY-MM-DDTHH:mm:ss)
   * @returns {Promise<Object>} 價格計算結果
   */
  async calculateServicePrice(serviceTypeId, startTime, endTime) {
    try {
      const response = await serviceTypeApi.calculatePrice(serviceTypeId, startTime, endTime)
      
      if (response.data && response.data.success) {
        return {
          success: true,
          serviceType: this.formatServiceTypeData(response.data.serviceType),
          serviceHours: response.data.serviceHours,
          hourlyRate: response.data.hourlyRate,
          totalAmount: response.data.totalAmount,
          message: response.data.message,
          // 格式化顯示資料
          formattedTotalAmount: `NT$ ${response.data.totalAmount.toLocaleString()}`,
          formattedHourlyRate: `NT$ ${response.data.hourlyRate.toLocaleString()}`,
          serviceHoursDisplay: this.formatServiceHours(response.data.serviceHours)
        }
      } else {
        return {
          success: false,
          message: response.data.message || '價格計算失敗'
        }
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
   * 驗證服務類型是否有效
   * @param {number} serviceTypeId - 服務類型ID
   * @returns {Promise<boolean>} 是否有效
   */
  async isValidServiceType(serviceTypeId) {
    try {
      const result = await this.getServiceTypeById(serviceTypeId)
      return result.success && result.serviceType && result.serviceType.isActive
    } catch (error) {
      console.error('驗證服務類型失敗:', error)
      return false
    }
  }

  /**
   * 格式化服務類型資料
   * @param {Object} serviceType - 原始服務類型資料
   * @returns {Object} 格式化後的服務類型資料
   */
  formatServiceTypeData(serviceType) {
    if (!serviceType) return null

    return {
      ...serviceType,
      // 格式化價格顯示
      formattedHourlyRate: `NT$ ${serviceType.hourlyRate.toLocaleString()}`,
      
      // 格式化建立和更新時間
      formattedCreatedAt: this.formatDateTime(serviceType.createdAt),
      formattedUpdatedAt: serviceType.updatedAt ? this.formatDateTime(serviceType.updatedAt) : null,
      
      // 狀態顯示文字
      statusDisplay: serviceType.isActive ? '啟用中' : '已停用',
      
      // 服務類型完整顯示名稱（包含價格）
      fullDisplayName: `${serviceType.serviceName} - NT$${serviceType.hourlyRate}/小時`,
      
      // 簡短描述（用於卡片顯示）
      shortDescription: serviceType.description && serviceType.description.length > 50 
        ? serviceType.description.substring(0, 50) + '...'
        : serviceType.description
    }
  }

  /**
   * 格式化服務時數顯示
   * @param {number} serviceHours - 服務時數（小數）
   * @returns {string} 格式化的服務時數
   */
  formatServiceHours(serviceHours) {
    if (!serviceHours || serviceHours <= 0) return '0小時'
    
    const hours = Math.floor(serviceHours)
    const minutes = Math.round((serviceHours - hours) * 60)
    
    if (hours > 0 && minutes > 0) {
      return `${hours}小時${minutes}分鐘`
    } else if (hours > 0) {
      return `${hours}小時`
    } else {
      return `${minutes}分鐘`
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
        minute: '2-digit'
      })
    } catch (error) {
      console.error('日期格式化失敗:', error)
      return dateTimeString
    }
  }

  /**
   * 計算時間差（小時）
   * @param {string} startTime - 開始時間
   * @param {string} endTime - 結束時間
   * @returns {number} 時間差（小時）
   */
  calculateHoursDifference(startTime, endTime) {
    try {
      const start = new Date(startTime)
      const end = new Date(endTime)
      const diffInMs = end - start
      const diffInHours = diffInMs / (1000 * 60 * 60)
      return Math.max(0, diffInHours) // 確保不為負數
    } catch (error) {
      console.error('計算時間差失敗:', error)
      return 0
    }
  }

  /**
   * 驗證時間格式
   * @param {string} timeString - 時間字串
   * @returns {boolean} 是否為有效格式
   */
  isValidTimeFormat(timeString) {
    try {
      const date = new Date(timeString)
      return !isNaN(date.getTime()) && timeString.includes('T')
    } catch (error) {
      return false
    }
  }

  /**
   * 轉換時間格式為API需要的格式
   * @param {Date|string} dateInput - 日期輸入
   * @returns {string} YYYY-MM-DDTHH:mm:ss 格式的時間字串
   */
  formatTimeForAPI(dateInput) {
    try {
      const date = dateInput instanceof Date ? dateInput : new Date(dateInput)
      return date.toISOString().slice(0, 19) // 移除毫秒和時區資訊
    } catch (error) {
      console.error('時間格式轉換失敗:', error)
      return ''
    }
  }

  /**
   * 取得預設服務時數選項
   * @returns {Array} 時數選項列表
   */
  getDefaultHourOptions() {
    return [
      { value: 1, label: '1小時', minutes: 60 },
      { value: 2, label: '2小時', minutes: 120 },
      { value: 3, label: '3小時', minutes: 180 },
      { value: 4, label: '4小時', minutes: 240 },
      { value: 6, label: '6小時', minutes: 360 },
      { value: 8, label: '8小時', minutes: 480 }
    ]
  }

  /**
   * 根據時數計算結束時間
   * @param {string} startTime - 開始時間 (YYYY-MM-DDTHH:mm:ss)
   * @param {number} hours - 服務時數
   * @returns {string} 結束時間 (YYYY-MM-DDTHH:mm:ss)
   */
  calculateEndTime(startTime, hours) {
    try {
      const start = new Date(startTime)
      const end = new Date(start.getTime() + (hours * 60 * 60 * 1000))
      return this.formatTimeForAPI(end)
    } catch (error) {
      console.error('計算結束時間失敗:', error)
      return ''
    }
  }
}

// 建立並匯出服務實例
export const serviceTypeService = new ServiceTypeService()
export default serviceTypeService