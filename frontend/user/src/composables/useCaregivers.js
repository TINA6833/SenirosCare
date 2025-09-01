import { ref, computed, reactive } from 'vue'
import { caregiverApi } from '@/api/caregiverApi'
import { caregiverService } from '@/service/caregiverService01'
import { useToast } from '@/composables/useToast'

/**
 * 照服員相關狀態管理
 * 提供照服員列表、詳細資料、搜尋等功能
 */
export function useCaregivers() {
  // 注入 Toast 功能
  const { showError } = useToast()
  
  // 響應式資料
  const caregivers = ref([])
  const selectedCaregiver = ref(null)
  const caregiverRatings = ref([])
  const loading = ref(false)
  const error = ref(null)
  
  // 篩選條件
  const filters = reactive({
    serviceArea: '',
    minRating: 0,
    searchName: '',
    isActive: true,
    sortBy: 'rating', // rating, experience, name
    sortOrder: 'desc' // asc, desc
  })

  // 分頁資訊
  const pagination = reactive({
    currentPage: 1,
    pageSize: 12,
    total: 0,
    totalPages: 0
  })

  // 計算屬性：篩選後的照服員列表
  const filteredCaregivers = computed(() => {
    let filtered = caregivers.value.filter(caregiver => {
      // 服務區域篩選
      const areaMatch = !filters.serviceArea || 
                       caregiver.serviceArea.includes(filters.serviceArea)
      
      // 評分篩選
      const ratingMatch = caregiver.rating >= filters.minRating
      
      // 姓名搜尋
      const nameMatch = !filters.searchName || 
                       caregiver.name.toLowerCase().includes(filters.searchName.toLowerCase())
      
      // 活躍狀態篩選
      const activeMatch = filters.isActive ? caregiver.isActive : true
      
      return areaMatch && ratingMatch && nameMatch && activeMatch
    })

    // 排序
    filtered.sort((a, b) => {
      let comparison = 0
      
      switch (filters.sortBy) {
        case 'rating':
          comparison = a.rating - b.rating
          break
        case 'experience':
          comparison = a.experienceYears - b.experienceYears
          break
        case 'name':
          comparison = a.name.localeCompare(b.name, 'zh-TW')
          break
        default:
          comparison = 0
      }
      
      return filters.sortOrder === 'desc' ? -comparison : comparison
    })

    return filtered
  })

  // 計算屬性：分頁後的照服員列表
  const paginatedCaregivers = computed(() => {
    const start = (pagination.currentPage - 1) * pagination.pageSize
    const end = start + pagination.pageSize
    const result = filteredCaregivers.value.slice(start, end)
    
    // 更新分頁資訊
    pagination.total = filteredCaregivers.value.length
    pagination.totalPages = Math.ceil(pagination.total / pagination.pageSize)
    
    return result
  })

  // 計算屬性：取得可用的服務區域選項
  const serviceAreaOptions = computed(() => {
    const areas = [...new Set(caregivers.value.map(c => c.serviceArea).filter(Boolean))]
    return areas.sort()
  })

  /**
   * 載入所有照服員
   */
  const loadCaregivers = async () => {
    try {
      loading.value = true
      error.value = null
      
      const response = await caregiverApi.getAllCaregivers()
      
      // 處理不同的回應格式
      const rawData = response.data
      const caregiverData = Array.isArray(rawData) ? rawData : 
                           rawData.content ? rawData.content : 
                           rawData.data ? rawData.data : []
      
      // 如果沒有資料，設置空陣列並顯示提示
      if (!Array.isArray(caregiverData)) {
        caregivers.value = []
        showError('照服員資料格式異常，請聯繫系統管理員')
        return
      }
      
      if (caregiverData.length === 0) {
        caregivers.value = []
        showError('目前沒有可用的照服員資料')
        return
      }
      
      // 格式化照服員資料，適應不同的後端回應格式
      caregivers.value = caregiverData.map(caregiver => {
        
        const formattedCaregiver = {
          // 基本資訊 - 根據API文檔，主要ID欄位是 caregiverId
          caregiverId: caregiver.caregiverId || caregiver.id, // API文檔中的主要ID欄位
          caregiverMemberId: caregiver.caregiverMemberId || caregiver.caregiverId || caregiver.id, // 保持向後相容
          memberName: caregiver.memberName || caregiver.name || caregiver.fullName || caregiver.chineseName || '未知姓名',
          chineseName: caregiver.chineseName || caregiver.memberName || caregiver.name || caregiver.fullName || '未知姓名',
          imagePath: caregiver.photo || caregiver.imagePath || caregiver.photoUrl || caregiver.avatar,
          
          // 專業資訊
          specialty: caregiver.specialty || caregiver.specialties || '一般照護',
          experienceYears: caregiver.experienceYears || caregiver.experience || 0,
          serviceArea: caregiver.serviceArea || caregiver.area || '桃園市',
          
          // 評分資訊
          avgRating: caregiver.averageRating || caregiver.avgRating || caregiver.rating || 0,
          totalRatings: caregiver.totalRatings || caregiver.ratingCount || 0,
          
          // 價格與服務
          hourlyRate: caregiver.hourlyRate || caregiver.price || 500,
          introduction: caregiver.selfIntroduction || caregiver.introduction || caregiver.description || '專業照服員，提供優質服務',
          
          // 狀態資訊
          isActive: caregiver.isActive !== undefined ? caregiver.isActive : true,
          phone: caregiver.phone || caregiver.phoneNumber,
          gender: caregiver.gender,
          genderDisplay: caregiver.genderDisplay,
          email: caregiver.email,
          address: caregiver.address,
          statusDisplay: caregiver.statusDisplay,
          
          // 格式化後的顯示資料
          ratingDisplay: caregiver.averageRating || caregiver.avgRating || caregiver.rating || 0,
          experienceDisplay: `${caregiver.experienceYears || caregiver.experience || 0}年經驗`
        }
       
        
        return formattedCaregiver
      })
      
      // 重置到第一頁
      pagination.currentPage = 1
      
    } catch (err) {
      error.value = '載入照服員資料失敗'
      
      // 設定空陣列以避免渲染錯誤
      caregivers.value = []
      
      // 提供更詳細的錯誤訊息
      let errorMessage = '載入照服員資料失敗'
      if (err.response?.status === 404) {
        errorMessage = '找不到照服員資料，請確認後端服務是否正常運行'
      } else if (err.response?.status === 500) {
        errorMessage = '伺服器內部錯誤，請聯繫系統管理員'
      } else if (err.code === 'ECONNREFUSED' || err.message.includes('Network Error')) {
        errorMessage = '無法連接到伺服器，請確認後端服務是否啟動 (localhost:8080)'
      } else if (err.response?.status === 401) {
        errorMessage = '未授權存取，請重新登入'
      } else if (err.response?.status === 403) {
        errorMessage = '沒有權限存取照服員資料'
      }
      
      showError(errorMessage)
    } finally {
      loading.value = false
    }
  }

  /**
   * 根據ID取得照服員資料 (直接回傳照服員物件，不設置 selectedCaregiver)
   * @param {number} caregiverId - 照服員ID
   * @returns {Promise<Object>} 照服員資料
   */
  const getCaregiverById = async (caregiverId) => {
    try {
      const response = await caregiverApi.getCaregiverById(caregiverId)
      // 直接返回後端資料，保持與其他地方的一致性
      return response.data
    } catch (err) {
      throw new Error('載入照服員資料失敗')
    }
  }

  /**
   * 載入照服員詳細資料
   * @param {number} caregiverId - 照服員ID
   */
  const loadCaregiverDetail = async (caregiverId) => {
    try {
      loading.value = true
      error.value = null
      
      const response = await caregiverApi.getCaregiverById(caregiverId)
      selectedCaregiver.value = caregiverService.formatCaregiverDetail(response.data)
      
    } catch (err) {
      error.value = '載入照服員詳細資料失敗'
      showError('載入照服員詳細資料失敗，請稍後再試')
    } finally {
      loading.value = false
    }
  }

  /**
   * 載入照服員評價
   * @param {number} caregiverId - 照服員ID
   * @param {number} page - 頁碼
   * @param {number} size - 每頁筆數
   */
  const loadCaregiverRatings = async (caregiverId, page = 0, size = 10) => {
    try {
      const response = await caregiverApi.getCaregiverRatings(caregiverId, page, size)
      caregiverRatings.value = response.data.content || response.data
      
    } catch (err) {
      showError('載入評價資料失敗')
    }
  }

  /**
   * 根據服務區域搜尋照服員
   * @param {string} area - 服務區域
   */
  const searchByArea = async (area) => {
    try {
      loading.value = true
      error.value = null
      
      const response = await caregiverApi.getCaregiversByArea(area)
      caregivers.value = caregiverService.formatCaregiverList(response.data)
      
      // 重置到第一頁
      pagination.currentPage = 1
      
    } catch (err) {
      error.value = '搜尋照服員失敗'
      showError('搜尋照服員失敗，請稍後再試')
    } finally {
      loading.value = false
    }
  }

  /**
   * 根據姓名搜尋照服員
   * @param {string} name - 照服員姓名
   */
  const searchByName = async (name) => {
    if (!name.trim()) {
      await loadCaregivers()
      return
    }

    try {
      loading.value = true
      error.value = null
      
      const response = await caregiverApi.searchCaregiversByName(name)
      caregivers.value = caregiverService.formatCaregiverList(response.data)
      
      // 重置到第一頁
      pagination.currentPage = 1
      
    } catch (err) {
      error.value = '搜尋照服員失敗'
      showError('搜尋照服員失敗，請稍後再試')
    } finally {
      loading.value = false
    }
  }

  /**
   * 更新篩選條件
   * @param {Object} newFilters - 新的篩選條件
   */
  const updateFilters = (newFilters) => {
    Object.assign(filters, newFilters)
    // 重置到第一頁
    pagination.currentPage = 1
  }

  /**
   * 重置篩選條件
   */
  const resetFilters = () => {
    Object.assign(filters, {
      serviceArea: '',
      minRating: 0,
      searchName: '',
      isActive: true,
      sortBy: 'rating',
      sortOrder: 'desc'
    })
    pagination.currentPage = 1
  }

  /**
   * 設定分頁
   * @param {number} page - 頁碼
   */
  const setPage = (page) => {
    if (page >= 1 && page <= pagination.totalPages) {
      pagination.currentPage = page
    }
  }

  /**
   * 下一頁
   */
  const nextPage = () => {
    if (pagination.currentPage < pagination.totalPages) {
      pagination.currentPage++
    }
  }

  /**
   * 上一頁
   */
  const prevPage = () => {
    if (pagination.currentPage > 1) {
      pagination.currentPage--
    }
  }

  /**
   * 清除選中的照服員
   */
  const clearSelectedCaregiver = () => {
    selectedCaregiver.value = null
    caregiverRatings.value = []
  }

  /**
   * 根據ID尋找照服員
   * @param {number} id - 照服員ID
   * @returns {Object|null} 照服員資料
   */
  const findCaregiverById = (id) => {
    return caregivers.value.find(caregiver => caregiver.id === parseInt(id)) || null
  }

  /**
   * 取得熱門照服員（評分最高的前幾名）
   * @param {number} limit - 數量限制
   * @returns {Array} 熱門照服員列表
   */
  const getTopRatedCaregivers = (limit = 5) => {
    return [...caregivers.value]
      .filter(c => c.rating > 0)
      .sort((a, b) => b.rating - a.rating)
      .slice(0, limit)
  }

  return {
    // 狀態
    caregivers,
    selectedCaregiver,
    caregiverRatings,
    loading,
    error,
    filters,
    pagination,
    
    // 計算屬性
    filteredCaregivers,
    paginatedCaregivers,
    serviceAreaOptions,
    
    // 方法
    loadCaregivers,
    getCaregiverById,
    loadCaregiverDetail,
    loadCaregiverRatings,
    searchByArea,
    searchByName,
    updateFilters,
    resetFilters,
    setPage,
    nextPage,
    prevPage,
    clearSelectedCaregiver,
    findCaregiverById,
    getTopRatedCaregivers
  }
}
