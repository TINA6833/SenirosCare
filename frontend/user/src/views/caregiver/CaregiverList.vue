<template>
  <div>
    <!-- 導覽列 -->
    <main-navbar />
    
    <!-- 頂部留白，避免導覽列壓住內容 -->
    <div style="height: 140px;"></div>
    
    <!-- 頁面標題區 -->
    <div class="flex items-center gap-4 flex-wrap bg-overlay p-14 sm:p-16 before:bg-title before:bg-opacity-70"
         :style="{ backgroundImage: 'url(' + bg + ')' }">
      <div class="text-center w-full">
        <h2 class="text-white text-8 md:text-[40px] font-normal leading-none text-center">照服員預約</h2>
        <ul class="flex items-center justify-center gap-[10px] text-base md:text-lg leading-none font-normal text-white mt-3 md:mt-4">
          <li><router-link to="/">首頁</router-link></li>
          <li>/</li>
          <li class="text-primary">照服員預約</li>
        </ul>
      </div>
    </div>

    <!-- 主內容區域 -->
    <div class="container-fluid max-w-[1720px] mx-auto py-10">
      <!-- 頂部搜尋欄和篩選 -->
      <div class="mb-8">
        <div class="flex flex-col md:flex-row gap-4 items-center justify-between">
          <!-- 左側：搜尋欄 -->
          <div class="flex gap-3 flex-wrap items-center">
            <input
              v-model="searchQuery"
              @input="handleSearchInput"
              type="text"
              placeholder="請輸入照服員中文姓名關鍵字"
              class="border border-gray-300 rounded-lg px-4 py-2 w-64 focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent"
            />
            <button
              @click="search"
              class="px-6 py-2 bg-primary text-white rounded-lg hover:bg-primary-dark transition-colors duration-200"
            >
              搜尋
            </button>
          </div>
          
          <!-- 右側：篩選選項 -->
          <div class="flex gap-3 flex-wrap items-center">
            <!-- 桃園市行政區篩選 -->
            <select v-model="filters.district" @change="applyFilters" 
                    class="border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-primary">
              <option value="">全部區域</option>
              <option value="桃園區">桃園區</option>
              <option value="中壢區">中壢區</option>
              <option value="平鎮區">平鎮區</option>
              <option value="八德區">八德區</option>
              <option value="楊梅區">楊梅區</option>
              <option value="蘆竹區">蘆竹區</option>
              <option value="大溪區">大溪區</option>
              <option value="龍潭區">龍潭區</option>
              <option value="龜山區">龜山區</option>
              <option value="大園區">大園區</option>
              <option value="觀音區">觀音區</option>
              <option value="新屋區">新屋區</option>
              <option value="復興區">復興區</option>
            </select>
            
            <!-- 評分篩選 -->
            <select v-model="filters.minRating" @change="applyFilters" 
                    class="border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-primary">
              <option value="">全部評分</option>
              <option value="4.5">4.5星以上</option>
              <option value="4">4星以上</option>
              <option value="3.5">3.5星以上</option>
              <option value="3">3星以上</option>
              <option value="2.5">2.5星以上</option>
            </select>
            
            <select v-model="filters.sortBy" @change="applyFilters" 
                    class="border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-primary">
              <option value="">預設排序</option>
              <option value="rating">評分排序</option>
              <option value="experience">經驗排序</option>
            </select>
            
            <router-link
              to="/my-appointments"
              class="px-4 py-2 border border-primary text-primary rounded-lg hover:bg-primary hover:text-white transition-colors duration-200"
            >
              我的預約
            </router-link>
          </div>
        </div>
        
        <!-- 結果統計 -->
        <div class="mt-4 text-gray-600">
          共找到 {{ filteredCaregivers.length }} 位照服員
        </div>
      </div>

      <!-- 照服員卡片網格 -->
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
        <div
          v-for="caregiver in paginatedCaregivers"
          :key="caregiver.caregiverId || caregiver.caregiverMemberId || caregiver.id"
          class="bg-white rounded-lg shadow-lg overflow-hidden hover:shadow-xl transition-shadow duration-300"
        >
          <!-- 照服員照片 -->
          <div class="relative">
            <img
              :src="getCaregiverImage(caregiver.imagePath)"
              :alt="caregiver.memberName"
              class="w-full h-48 object-contain bg-gray-50"
              @error="handleImageError"
            />
            <!-- 評分標籤 -->
            <div class="absolute top-3 right-3 bg-white bg-opacity-90 px-2 py-1 rounded-full text-sm font-semibold text-primary">
              ⭐ {{ caregiver.avgRating && caregiver.avgRating > 0 ? caregiver.avgRating.toFixed(1) : '暫無評分' }}
            </div>
          </div>
          
          <!-- 照服員資訊 -->
          <div class="p-4">
            <!-- 中文姓名 -->
            <h3 class="font-bold text-lg mb-2 text-gray-800">
              {{ caregiver.chineseName || caregiver.memberName || '未知姓名' }}
            </h3>
            
            <div class="space-y-1 text-sm text-gray-600 mb-3">
              <div class="flex items-center">
                <span class="font-medium whitespace-nowrap min-w-[70px]">工作經驗：</span>
                <span>{{ caregiver.experienceYears || 0 }}年</span>
              </div>
              <div class="flex items-center">
                <span class="font-medium whitespace-nowrap min-w-[70px]">服務地區：</span>
                <span>{{ caregiver.serviceArea || '桃園市' }}</span>
              </div>
            </div>
            
            <!-- 簡介 -->
            <p class="text-sm text-gray-700 mb-4 h-12 overflow-hidden">
              {{ caregiver.introduction && caregiver.introduction !== '專業照服員，提供優質服務' ? 
                 (caregiver.introduction.substring(0, 60) + (caregiver.introduction.length > 60 ? '...' : '')) : 
                 '專業照服員，提供優質服務' }}
            </p>
            
            <!-- 操作按鈕 -->
            <div class="flex gap-2">
              <button
                @click="viewCaregiverDetail(caregiver)"
                class="flex-1 px-3 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors duration-200 text-sm"
              >
                查看評論
              </button>
              <button
                @click="bookCaregiver(caregiver)"
                class="flex-1 px-3 py-2 bg-primary text-white rounded-lg hover:bg-primary-dark transition-colors duration-200 text-sm"
              >
                立即預約
              </button>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 空狀態 -->
      <div v-if="filteredCaregivers.length === 0" class="text-center py-12">
        <div class="text-gray-400 mb-4">
          <svg class="w-16 h-16 mx-auto" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.172 16.172a4 4 0 015.656 0M9 12h6m-6-4h6m2 5.291A7.962 7.962 0 0112 15c-2.34 0-4.47-.881-6.08-2.33"></path>
          </svg>
        </div>
        <h3 class="text-lg font-medium text-gray-900 mb-2">找不到符合條件的照服員</h3>
        <p class="text-gray-500 mb-4">請試著調整搜尋條件或篩選選項</p>
        <button
          @click="resetFilters"
          class="px-4 py-2 bg-primary text-white rounded-lg hover:bg-primary-dark transition-colors duration-200"
        >
          重置篩選
        </button>
      </div>

      <!-- 分頁控制 -->
      <div v-if="totalPages > 1" class="flex justify-center items-center gap-2 mt-8">
        <button
          @click="prevPage"
          :disabled="currentPage === 1"
          class="px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          上一頁
        </button>
        
        <div class="flex gap-1">
          <button
            v-for="page in getVisiblePages()"
            :key="page"
            @click="setPage(page)"
            :class="[
              'px-3 py-2 rounded-lg text-sm',
              currentPage === page 
                ? 'bg-primary text-white' 
                : 'border border-gray-300 hover:bg-gray-50'
            ]"
          >
            {{ page }}
          </button>
        </div>
        
        <button
          @click="nextPage"
          :disabled="currentPage >= totalPages"
          class="px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          下一頁
        </button>
      </div>
    </div>

    <!-- 照服員詳細資料 Modal -->
    <CaregiverDetailModal
      v-if="showDetailModal"
      :caregiver="selectedCaregiverForDetail"
      @close="closeDetailModal"
      @book="bookCaregiver"
    />

    <!-- Toast 提示 -->
    <div class="fixed top-8 right-8 z-50 space-y-4">
      <transition-group name="toast-fade" tag="div" class="space-y-4">
        <div
          v-for="toast in toasts"
          :key="toast.id"
          :class="[
            'px-6 py-4 rounded-lg shadow-xl flex items-center gap-3 max-w-sm',
            toast.type === 'success' ? 'bg-white text-green-700' : 
            toast.type === 'error' ? 'bg-white text-red-700' : 
            'bg-white text-blue-700'
          ]"
        >
          <div :class="[
            'rounded-full w-8 h-8 flex items-center justify-center',
            toast.type === 'success' ? 'bg-green-500' : 
            toast.type === 'error' ? 'bg-red-500' : 
            'bg-blue-500'
          ]">
            <svg v-if="toast.type === 'success'" class="w-5 h-5 text-white" fill="none" stroke="currentColor" stroke-width="3" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" d="M5 13l5 5L19 7"/>
            </svg>
            <svg v-else-if="toast.type === 'error'" class="w-5 h-5 text-white" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12"/>
            </svg>
            <svg v-else class="w-5 h-5 text-white" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
            </svg>
          </div>
          <div>
            <div v-if="toast.title" class="font-medium">{{ toast.title }}</div>
            <div class="text-sm">{{ toast.message }}</div>
          </div>
        </div>
      </transition-group>
    </div>

    <!-- 自訂登入提示彈窗 -->
    <div v-if="showLoginDialog" class="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-40">
      <div class="bg-white rounded-lg shadow-lg p-8 max-w-xs w-full text-center">
        <div class="mb-4 text-lg font-medium text-gray-900">登入提示</div>
        <div class="mb-6 text-gray-700">您需要先登入才能預約照服員服務，是否現在登入？</div>
        <div class="flex justify-center gap-4">
          <button @click="showLoginDialog = false" class="px-4 py-2 rounded bg-gray-200 hover:bg-gray-300 text-gray-700">取消</button>
          <button @click="handleLoginConfirm" class="px-4 py-2 rounded bg-primary text-white hover:bg-primary-dark">確定</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCaregivers } from '@/composables/useCaregivers'
import { useToast } from '@/composables/useToast'
import MainNavbar from '@/components/navbar/main-navbar.vue'
import CaregiverDetailModal from '@/components/caregiver/CaregiverDetailModal.vue'
import bg from '@/assets/img/bg/footer.jpg'

export default {
  name: 'CaregiverList',
  components: {
    MainNavbar,
    CaregiverDetailModal
  },
  setup() {
    const router = useRouter()
    const { toasts, showError } = useToast()
    
    // 照服員資料和狀態管理
    const {
      caregivers,
      loading,
      error,
      loadCaregivers
    } = useCaregivers()

    // 本地狀態
    const searchQuery = ref('')
    const showDetailModal = ref(false)
    const selectedCaregiverForDetail = ref(null)
    const currentPage = ref(1)
    const itemsPerPage = ref(12)
    
    // 篩選條件
    const filters = ref({
      district: '',     // 桃園市行政區
      minRating: '',    // 最低評分
      sortBy: ''        // 排序方式
    })

    // 計算屬性：篩選後的照服員
    const filteredCaregivers = computed(() => {
      let result = [...caregivers.value]

      // 搜尋姓名篩選（支援中文姓名和英文姓名）
      if (searchQuery.value.trim()) {
        const searchTerm = searchQuery.value.toLowerCase().trim()
        result = result.filter(caregiver => 
          (caregiver.chineseName && caregiver.chineseName.toLowerCase().includes(searchTerm)) ||
          (caregiver.memberName && caregiver.memberName.toLowerCase().includes(searchTerm))
        )
      }

      // 桃園市行政區篩選
      if (filters.value.district) {
        result = result.filter(caregiver => 
          caregiver.serviceArea?.includes(filters.value.district)
        )
      }

      // 評分篩選
      if (filters.value.minRating) {
        const minRating = parseFloat(filters.value.minRating)
        result = result.filter(caregiver => {
          const rating = caregiver.avgRating || caregiver.ratingDisplay || 0
          return rating >= minRating
        })
      }

      // 排序
      if (filters.value.sortBy) {
        result.sort((a, b) => {
          switch(filters.value.sortBy) {
            case 'rating':
              return (b.avgRating || b.ratingDisplay || 0) - (a.avgRating || a.ratingDisplay || 0)
            case 'experience':
              return (b.experienceYears || 0) - (a.experienceYears || 0)
            case 'price':
              return (a.hourlyRate || 0) - (b.hourlyRate || 0)
            default:
              return 0
          }
        })
      }

      return result
    })

    // 計算屬性：分頁後的照服員
    const paginatedCaregivers = computed(() => {
      const start = (currentPage.value - 1) * itemsPerPage.value
      const end = start + itemsPerPage.value
      return filteredCaregivers.value.slice(start, end)
    })

    // 計算屬性：總頁數
    const totalPages = computed(() => {
      return Math.ceil(filteredCaregivers.value.length / itemsPerPage.value)
    })

    // 方法：取得照服員圖片（參考後台實作）
    const getCaregiverImage = (imagePath) => {
      if (!imagePath) {
        try {
          return require('@/assets/img/thumb/error.png')
        } catch (err) {
          return '/src/assets/img/thumb/error.png'
        }
      }
      
      // 如果是完整的 URL
      if (imagePath.startsWith('http://') || imagePath.startsWith('https://')) {
        return imagePath
      }
      
      // 如果是 Base64 編碼的圖片數據
      if (imagePath.startsWith('data:image')) {
        return imagePath
      }
      
      // 統一處理相對路徑（參考後台 CaregiverTable.vue 的實作）
      // 移除可能的前導斜線，然後加上正確的基礎 URL
      const cleanPath = imagePath.startsWith('/') ? imagePath : '/' + imagePath
      const fullPath = `http://localhost:8080${cleanPath}`
      
      return fullPath
    }

    // 方法：處理圖片載入錯誤
    const handleImageError = (event) => {
      try {
        event.target.src = require('@/assets/img/thumb/error.png')
      } catch (err) {
        // 作為備用方案，使用一個簡單的 data URL 圖片
        event.target.src = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgdmlld0JveD0iMCAwIDIwMCAyMDAiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+CjxyZWN0IHdpZHRoPSIyMDAiIGhlaWdodD0iMjAwIiBmaWxsPSIjRjNGNEY2Ii8+CjxwYXRoIGQ9Ik0xMDAgNjBDMTE0LjMgNjAgMTI2IDcxLjcgMTI2IDg2QzEyNiAxMDAuMyAxMTQuMyAxMTIgMTAwIDExMkM4NS43IDExMiA3NCAxMDAuMyA3NCA4NkM3NCA3MS43IDg1LjcgNjAgMTAwIDYwWiIgZmlsbD0iIzlDQTNBRiIvPgo8cGF0aCBkPSJNNzQgMTQwSDEyNkMxMzAgMTQwIDEzNCAxNDAuOSAxMzYuOCAxNDMuMkMxMzkuMSAxNDYgMTQwIDE0OSAxNDAgMTUzVjE2NEg2MFYxNTNDNjAgMTQ5IDYwLjkgMTQ2IDYzLjIgMTQzLjJDNjYgMTQwLjkgNjkgMTQwIDc0IDE0MFoiIGZpbGw9IiM5Q0EzQUYiLz4KPC9zdmc+'
      }
    }

    // 方法：搜尋
    const search = () => {
      currentPage.value = 1
      // 搜尋邏輯已在 computed 中處理
    }

    // 方法：處理搜尋輸入
    const handleSearchInput = () => {
      currentPage.value = 1
    }

    // 方法：應用篩選
    const applyFilters = () => {
      currentPage.value = 1
    }

    // 方法：重置篩選
    const resetFilters = () => {
      searchQuery.value = ''
      filters.value = {
        district: '',
        minRating: '',
        sortBy: ''
      }
      currentPage.value = 1
    }

    // 方法：查看照服員評論
    const viewCaregiverDetail = (caregiver) => {
      // 優先使用 caregiverId，回退到其他可能的ID欄位
      const caregiverId = caregiver.caregiverId || caregiver.caregiverMemberId || caregiver.id
      
      if (!caregiverId) {
        alert('無法找到照服員ID，請重新整理頁面後再試')
        return
      }
      
      router.push({
        name: 'CaregiverReviews',
        params: { caregiverId: caregiverId.toString() } // 確保ID是字串格式
      })
    }

    // 方法：關閉詳情模態框
    const closeDetailModal = () => {
      showDetailModal.value = false
      selectedCaregiverForDetail.value = null
    }

      // [重點] 方法：預約照服員
    const bookCaregiver = (caregiver) => {
      const caregiverId = caregiver.caregiverId || caregiver.caregiverMemberId || caregiver.id
      if (!caregiverId) {
        showError('無法找到照服員ID，請重新整理頁面後再試')
        return
      }
      const isAuthenticated = checkAuthStatus()
      if (!isAuthenticated) {
        // 顯示自訂彈窗
        pendingCaregiverId = caregiverId
        showLoginDialog.value = true
        return
      }
      // 已登入，直接跳轉
      router.push({
        name: 'CaregiverBooking',
        params: { caregiverId: caregiverId.toString() } // 確保ID是字串格式
      })
    }

    // [重點] 控制自訂登入提示彈窗顯示
    const showLoginDialog = ref(false)
    // [重點] 暫存預約照服員ID
    let pendingCaregiverId = null

    // [重點] 處理自訂彈窗「確定」按鈕
    const handleLoginConfirm = () => {
      showLoginDialog.value = false
      if (pendingCaregiverId) {
        const targetPath = `/caregiver/${pendingCaregiverId}/booking`
        localStorage.setItem('redirectAfterLogin', targetPath)
        // 跳轉到 Line 登入頁面
        window.location.href = 'http://localhost:8080/oauth2/authorization/line'
        pendingCaregiverId = null
      }
    }

    // [重點] 檢查認證狀態的輔助函數
    const checkAuthStatus = () => {
      try {
        // [重點] 檢查 localStorage 中的 token
        const token = localStorage.getItem('authToken') || localStorage.getItem('token')
        if (token) {
          return true
        }
        
        // [重點] 檢查使用者資料
        const userProfile = localStorage.getItem('userProfile')
        if (userProfile) {
          return true
        }
        
        return false
      } catch (error) {
        console.error('檢查認證狀態失敗:', error)
        return false
      }
    }
    // 方法：設定頁面
    const setPage = (page) => {
      if (page >= 1 && page <= totalPages.value) {
        currentPage.value = page
      }
    }

    // 方法：上一頁
    const prevPage = () => {
      if (currentPage.value > 1) {
        currentPage.value--
      }
    }

    // 方法：下一頁
    const nextPage = () => {
      if (currentPage.value < totalPages.value) {
        currentPage.value++
      }
    }

    // 方法：取得可見頁碼
    const getVisiblePages = () => {
      const total = totalPages.value
      const current = currentPage.value
      const delta = 2
      
      let start = Math.max(1, current - delta)
      let end = Math.min(total, current + delta)
      
      if (end - start < 4) {
        if (start === 1) {
          end = Math.min(total, start + 4)
        } else if (end === total) {
          start = Math.max(1, end - 4)
        }
      }
      
      const pages = []
      for (let i = start; i <= end; i++) {
        pages.push(i)
      }
      return pages
    }

    // 初始化載入資料
    onMounted(async () => {
      try {
        await loadCaregivers()
      } catch (err) {
        showError('載入照服員資料失敗：' + (err.message || '未知錯誤'))
      }
    })

    return {
      // 資料
      bg,
      toasts,
      caregivers,
      loading,
      error,
      searchQuery,
      filters,
      showDetailModal,
      selectedCaregiverForDetail,
      currentPage,
      
      // 計算屬性
      filteredCaregivers,
      paginatedCaregivers,
      totalPages,
      
      // 方法
      getCaregiverImage,
      handleImageError,
      search,
      handleSearchInput,
      applyFilters,
      resetFilters,
      viewCaregiverDetail,
      closeDetailModal,
      bookCaregiver,
      setPage,
      prevPage,
      nextPage,
      getVisiblePages,
      showLoginDialog,
      handleLoginConfirm
    }
  }
}
</script>

<style scoped>
/* Toast 動畫 */
.toast-fade-enter-active,
.toast-fade-leave-active {
  transition: all 0.3s ease;
}

.toast-fade-enter-from {
  opacity: 0;
  transform: translateX(100%);
}

.toast-fade-leave-to {
  opacity: 0;
  transform: translateX(100%);
}

.toast-fade-move {
  transition: transform 0.3s ease;
}

/* 自定義樣式 */
.bg-overlay {
  position: relative;
}

.bg-overlay::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: -1;
}

/* 響應式設計 */
@media (max-width: 768px) {
  .container-fluid {
    padding-left: 1rem;
    padding-right: 1rem;
  }
  
  .grid {
    grid-template-columns: 1fr;
  }
  
  .flex {
    flex-direction: column;
    align-items: stretch;
  }
  
  .gap-4 {
    gap: 0.75rem;
  }
}

@media (min-width: 640px) {
  .grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (min-width: 1024px) {
  .grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (min-width: 1280px) {
  .grid {
    grid-template-columns: repeat(4, 1fr);
  }
}
</style>
