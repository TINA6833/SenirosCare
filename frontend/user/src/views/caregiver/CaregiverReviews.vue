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
        <h2 class="text-white text-8 md:text-[40px] font-normal leading-none text-center">照服員評論</h2>
        <ul
          class="flex items-center justify-center gap-[10px] text-base md:text-lg leading-none font-normal text-white mt-3 md:mt-4">
          <li><router-link to="/">首頁</router-link></li>
          <li>/</li>
          <li><router-link to="/caregiver">照服員預約</router-link></li>
          <li>/</li>
          <li class="text-primary">評論詳情</li>
        </ul>
      </div>
    </div>

    <!-- 主內容區域 -->
    <div class="container mx-auto py-8 px-4 max-w-6xl">
      <!-- 載入狀態 -->
      <div v-if="loading" class="flex justify-center items-center py-12">
        <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"></div>
      </div>

      <!-- 照服員基本資訊卡片 -->
      <div v-else-if="caregiver" class="bg-white rounded-lg shadow-lg p-6 mb-8">
        <div class="flex flex-col md:flex-row gap-6">
          <!-- 照服員照片 -->
          <div class="flex-shrink-0">
            <img :src="getCaregiverImage(caregiver.imagePath || caregiver.photo)"
              :alt="caregiver.chineseName || caregiver.memberName"
              class="w-32 h-32 rounded-full object-cover border-4 border-gray-200" @error="handleImageError" />
          </div>

          <!-- 照服員資訊 -->
          <div class="flex-1">
            <h1 class="text-2xl font-bold text-gray-800 mb-2">
              {{ caregiver.chineseName || caregiver.memberName || '未知姓名' }}
            </h1>

            <div class="flex flex-wrap gap-4 text-sm text-gray-600 mb-4">
              <div class="flex items-center">
                <i class="fas fa-briefcase text-primary mr-2"></i>
                <span>工作經驗：{{ caregiver.experienceYears || 0 }}年</span>
              </div>
              <div class="flex items-center">
                <i class="fas fa-map-marker-alt text-primary mr-2"></i>
                <span>服務地區：{{ caregiver.serviceArea || '桃園市' }}</span>
              </div>
            </div>

            <!-- 簡介 -->
            <div v-if="caregiver.introduction" class="text-gray-700 mb-4 p-3 bg-gray-50 rounded-lg">
              <p class="text-sm font-medium text-gray-800 mb-1">個人簡介：</p>
              <p>{{ caregiver.introduction }}</p>
            </div>
            <div v-else class="text-gray-500 mb-4 p-3 bg-gray-50 rounded-lg italic">
              <p class="text-sm">專業照服員，提供優質服務</p>
            </div>

            <!-- 整體評分摘要 -->
            <div class="bg-gray-50 rounded-lg p-4">
              <div class="flex items-center justify-between">
                <div class="flex items-center gap-4">
                  <div class="text-center">
                    <div class="text-3xl font-bold text-primary">
                      {{ (caregiver.averageRating || averageRating).toFixed(1) }}
                    </div>
                    <div class="flex justify-center mb-1">
                      <div class="flex">
                        <i v-for="star in 5" :key="star" class="fas fa-star text-lg"
                          :class="star <= Math.round(caregiver.averageRating || averageRating) ? 'text-yellow-400' : 'text-gray-300'"></i>
                      </div>
                    </div>
                    <div class="text-sm text-gray-600">總計 {{ caregiver.totalRatings || totalReviews }} 則評論</div>
                  </div>
                </div>

                <!-- 評分分布 -->
                <div class="flex-1 max-w-md ml-8">
                  <div v-for="rating in [5, 4, 3, 2, 1]" :key="rating" class="flex items-center gap-2 mb-1">
                    <span class="text-sm text-gray-600 w-3">{{ rating }}</span>
                    <i class="fas fa-star text-yellow-400 text-xs"></i>
                    <div class="flex-1 bg-gray-200 rounded-full h-2">
                      <div class="bg-yellow-400 h-2 rounded-full transition-all duration-300"
                        :style="{ width: getRatingPercentage(rating) + '%' }"></div>
                    </div>
                    <span class="text-sm text-gray-600 w-8 text-right">{{ getRatingCount(rating) }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 篩選和排序 -->
      <div class="bg-white rounded-lg shadow-sm p-4 mb-6">
        <div class="flex flex-col sm:flex-row gap-4 items-center justify-between">
          <div class="flex gap-3 items-center">
            <label class="text-sm font-medium text-gray-700">篩選評分：</label>
            <select v-model="selectedRatingFilter" @change="filterReviews"
              class="border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary">
              <option value="">全部評分</option>
              <option value="5">5星</option>
              <option value="4">4星</option>
              <option value="3">3星</option>
              <option value="2">2星</option>
              <option value="1">1星</option>
            </select>
          </div>

          <div class="flex gap-3 items-center">
            <label class="text-sm font-medium text-gray-700">排序：</label>
            <select v-model="sortOrder" @change="sortReviews"
              class="border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary">
              <option value="newest">最新優先</option>
              <option value="oldest">最舊優先</option>
              <option value="highest">評分最高</option>
              <option value="lowest">評分最低</option>
            </select>
          </div>
        </div>
      </div>

      <!-- 評論列表 -->
      <div class="space-y-4">
        <div v-for="review in filteredReviews" :key="review.id"
          class="bg-white rounded-lg shadow-sm border border-gray-200 p-6 hover:shadow-md transition-shadow duration-200">
          <!-- 評論標題 -->
          <div class="flex items-start justify-between mb-4">
            <div class="flex items-center gap-3">
              <!-- 會員頭像 -->
              <div class="w-12 h-12 bg-primary rounded-full flex items-center justify-center text-white font-semibold">
                <i class="fas fa-user"></i>
              </div>

              <div>
                <h3 class="font-semibold text-gray-800">會員評論</h3>
                <div class="flex items-center gap-2">
                  <div class="flex">
                    <i v-for="star in 5" :key="star" class="fas fa-star text-sm"
                      :class="star <= review.rating ? 'text-yellow-400' : 'text-gray-300'"></i>
                  </div>
                  <span class="text-sm text-gray-500">{{ formatDate(review.createdAt) }}</span>
                </div>
              </div>
            </div>

            <!-- 服務日期 -->
            <div v-if="review.serviceDate" class="text-right">
              <div class="text-sm text-gray-500">服務日期</div>
              <div class="text-sm font-medium text-gray-700">{{ formatDate(review.serviceDate) }}</div>
            </div>
          </div>

          <!-- 評論內容 -->
          <div class="mb-4">
            <p class="text-gray-700 leading-relaxed">{{ review.comment || '此評論沒有留言內容' }}</p>
          </div>

          <!-- 評論標籤（如果有） -->
          <div v-if="review.tags && review.tags.length > 0" class="flex flex-wrap gap-2 mb-4">
            <span v-for="tag in review.tags" :key="tag"
              class="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
              {{ tag }}
            </span>
          </div>

          <!-- 照服員回覆（如果有） -->
          <div v-if="review.reply" class="mt-4 ml-6 p-4 bg-gray-50 rounded-lg border-l-4 border-primary">
            <div class="flex items-center gap-2 mb-2">
              <i class="fas fa-reply text-primary"></i>
              <span class="text-sm font-medium text-gray-700">照服員回覆</span>
              <span class="text-sm text-gray-500">{{ formatDate(review.replyDate) }}</span>
            </div>
            <p class="text-gray-700 text-sm">{{ review.reply }}</p>
          </div>
        </div>
      </div>

      <!-- 空狀態 -->
      <div v-if="filteredReviews.length === 0 && !loading" class="text-center py-12">
        <div class="text-gray-400 mb-4">
          <i class="fas fa-comments text-6xl"></i>
        </div>
        <h3 class="text-lg font-medium text-gray-900 mb-2">目前沒有評論</h3>
        <p class="text-gray-500">此照服員尚未收到任何評論</p>
      </div>

      <!-- 載入更多按鈕 -->
      <div v-if="hasMoreReviews && !loading" class="text-center mt-8">
        <button @click="loadMoreReviews"
          class="px-6 py-3 bg-primary text-white rounded-lg hover:bg-primary-dark transition-colors duration-200"
          :disabled="loadingMore">
          <span v-if="loadingMore">載入中...</span>
          <span v-else>載入更多評論</span>
        </button>
      </div>
    </div>

    <!-- 返回按鈕 -->
    <div class="fixed bottom-6 right-6">
      <button @click="goBack"
        class="bg-primary text-white rounded-full w-14 h-14 flex items-center justify-center shadow-lg hover:bg-primary-dark transition-all duration-200 hover:scale-110">
        <i class="fas fa-arrow-left text-lg"></i>
      </button>
    </div>
  </div>
  <FooterThree />
  <ScrollToTop />
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { caregiverApi } from '@/api/caregiverApi'
import { caregiverReviewService } from '@/service/caregiverReviewService'
import MainNavbar from '@/components/navbar/main-navbar.vue'
import FooterThree from '@/components/footer/footer-three.vue';
import ScrollToTop from '@/components/scroll-to-top.vue';

// 背景圖片
import bg from '@/assets/img/bg/footer.jpg'

export default {
  name: 'CaregiverReviews',
  components: {
    MainNavbar,
    FooterThree,
    ScrollToTop
  },
  setup() {
    const route = useRoute()
    const router = useRouter()

    // 響應式資料
    const caregiver = ref(null)
    const reviews = ref([])
    const loading = ref(true)
    const loadingMore = ref(false)
    const selectedRatingFilter = ref('')
    const sortOrder = ref('newest')
    const hasMoreReviews = ref(true)

    // 計算屬性
    const averageRating = computed(() => {
      if (reviews.value.length === 0) return 0
      const sum = reviews.value.reduce((acc, review) => acc + review.rating, 0)
      return sum / reviews.value.length
    })

    const totalReviews = computed(() => reviews.value.length)

    const filteredReviews = computed(() => {
      let filtered = [...reviews.value]

      // 評分篩選
      if (selectedRatingFilter.value) {
        filtered = filtered.filter(review => review.rating === parseInt(selectedRatingFilter.value))
      }

      // 排序
      filtered.sort((a, b) => {
        switch (sortOrder.value) {
          case 'newest':
            return new Date(b.createdAt) - new Date(a.createdAt)
          case 'oldest':
            return new Date(a.createdAt) - new Date(b.createdAt)
          case 'highest':
            return b.rating - a.rating
          case 'lowest':
            return a.rating - b.rating
          default:
            return 0
        }
      })

      return filtered
    })

    // 方法
    // 方法：取得照服員圖片（與 CaregiverList.vue 完全一致）
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

    const handleImageError = (event) => {
      try {
        event.target.src = require('@/assets/img/thumb/error.png')
      } catch (err) {
        event.target.src = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgdmlld0JveD0iMCAwIDIwMCAyMDAiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+CjxyZWN0IHdpZHRoPSIyMDAiIGhlaWdodD0iMjAwIiBmaWxsPSIjRjNGNEY2Ii8+CjxwYXRoIGQ9Ik0xMDAgNjBDMTE0LjMgNjAgMTI2IDcxLjcgMTI2IDg2QzEyNiAxMDAuMyAxMTQuMyAxMTIgMTAwIDExMkM4NS43IDExMiA3NCAxMDAuMyA3NCA4NkM3NCA3MS43IDg1LjcgNjAgMTAwIDYwWiIgZmlsbD0iIzlDQTNBRiIvPgo8cGF0aCBkPSJNNzQgMTQwSDEyNkMxMzAgMTQwIDEzNCAxNDAuOSAxMzYuOCAxNDMuMkMxMzkuMSAxNDYgMTQwIDE0OSAxNDAgMTUzVjE2NEg2MFYxNTNDNjAgMTQ5IDYwLjkgMTQ2IDYzLjIgMTQzLjJDNjYgMTQwLjkgNjkgMTQwIDc0IDE0MFoiIGZpbGw9IiM5Q0EzQUYiLz4KPC9zdmc+'
      }
    }

    const formatDate = (dateString) => {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleDateString('zh-TW', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
      })
    }

    const getRatingPercentage = (rating) => {
      if (totalReviews.value === 0) return 0
      const count = reviews.value.filter(review => review.rating === rating).length
      return (count / totalReviews.value) * 100
    }

    const getRatingCount = (rating) => {
      return reviews.value.filter(review => review.rating === rating).length
    }

    const filterReviews = () => {
      // 篩選邏輯已在 computed 中處理
    }

    const sortReviews = () => {
      // 排序邏輯已在 computed 中處理
    }

    const loadMoreReviews = async () => {
      if (loadingMore.value) return

      loadingMore.value = true

      try {
        const caregiverId = route.params.caregiverId
        const response = await caregiverReviewService.getCaregiverReviews(caregiverId, 10, reviews.value.length)

        if (response && response.content) {
          reviews.value.push(...response.content)
          hasMoreReviews.value = response.hasMore
        }
      } catch (error) {
        console.error('載入更多評論失敗:', error)
      } finally {
        loadingMore.value = false
      }
    }

    const goBack = () => {
      router.go(-1)
    }

    // 載入資料
    const loadCaregiverData = async () => {
      try {
        const caregiverId = route.params.caregiverId

        // 並行載入照服員資料和評論
        const [caregiverResponse, reviewsResponse] = await Promise.all([
          caregiverApi.getCaregiverById(caregiverId),
          caregiverReviewService.getCaregiverReviews(caregiverId, 10, 0)
        ])

        // 處理照服員資料
        if (caregiverResponse && caregiverResponse.data && caregiverResponse.data.success && caregiverResponse.data.data) {
          // 修正 API 回應結構處理
          const apiData = caregiverResponse.data.data
          caregiver.value = {
            caregiverId: apiData.caregiverId || caregiverId,
            chineseName: apiData.chineseName,
            memberName: apiData.chineseName || apiData.memberName,
            experienceYears: apiData.experienceYears || 0,
            serviceArea: apiData.serviceArea || '桃園市',
            imagePath: apiData.photo || apiData.imagePath,
            photo: apiData.photo,
            introduction: apiData.selfIntroduction || apiData.introduction || '專業照服員，提供優質服務',
            averageRating: apiData.averageRating || 0,
            totalRatings: apiData.totalRatings || 0
          }
        } else {
          // 如果 API 資料不正確，設定預設值
          caregiver.value = {
            caregiverId: caregiverId,
            chineseName: '未知姓名',
            memberName: '未知姓名',
            experienceYears: 0,
            serviceArea: '桃園市',
            imagePath: null,
            photo: null,
            introduction: '專業照服員，提供優質服務',
            averageRating: 0,
            totalRatings: 0
          }
        }

        // 處理評論資料
        if (reviewsResponse && reviewsResponse.content) {
          reviews.value = reviewsResponse.content
          hasMoreReviews.value = reviewsResponse.hasMore
        } else {
          // 如果沒有評論資料，使用模擬資料進行展示
          reviews.value = generateMockReviews(caregiverId)
          hasMoreReviews.value = false
        }

      } catch (error) {
        console.error('載入照服員資料失敗:', error)
        // 載入失敗時也使用模擬資料
        const caregiverId = route.params.caregiverId
        caregiver.value = {
          caregiverId: caregiverId,
          chineseName: '未知姓名',
          memberName: '未知姓名',
          experienceYears: 0,
          serviceArea: '桃園市',
          imagePath: null,
          photo: null,
          introduction: '專業照服員，提供優質服務',
          averageRating: 0,
          totalRatings: 0
        }
        if (caregiverId) {
          reviews.value = generateMockReviews(caregiverId)
          hasMoreReviews.value = false
        }
      } finally {
        loading.value = false
      }
    }

    // 生成模擬評論資料（用於展示）
    const generateMockReviews = (caregiverId) => {
      return [
        {
          id: 1001,
          caregiverMemberId: caregiverId,
          memberName: '會員 1',
          rating: 5,
          comment: '照服員非常專業且細心，對我家中長輩照顧得很好。準時到達，服務態度親切，讓家人很放心。',
          serviceDate: '2024-01-15T09:00:00',
          createdAt: '2024-01-16T10:30:00Z',
          helpfulCount: 12,
          tags: ['基礎照護', '極力推薦']
        },
        {
          id: 1002,
          caregiverMemberId: caregiverId,
          memberName: '會員 2',
          rating: 4,
          comment: '服務品質很不錯，照服員有經驗且有耐心。唯一小建議是希望能提前聯絡確認時間。',
          serviceDate: '2024-01-10T14:00:00',
          createdAt: '2024-01-11T14:20:00Z',
          helpfulCount: 8,
          reply: '感謝您的建議，我們會在服務前一天主動聯絡確認時間，謝謝您的信任！',
          replyDate: '2024-01-12T09:15:00Z',
          tags: ['醫療照護', '推薦']
        },
        {
          id: 1003,
          caregiverMemberId: caregiverId,
          memberName: '會員 3',
          rating: 5,
          comment: '第一次使用線上預約照護服務，整個過程很順利。照服員很專業，對長輩也很有耐心，會持續使用這個服務。',
          serviceDate: '2024-01-08T10:00:00',
          createdAt: '2024-01-09T16:45:00Z',
          helpfulCount: 15,
          tags: ['陪伴關懷', '極力推薦']
        },
        {
          id: 1004,
          caregiverMemberId: caregiverId,
          memberName: '會員 4',
          rating: 4,
          comment: '照服員很認真負責，但希望能更主動溝通長輩的狀況。整體服務還是很滿意的。',
          serviceDate: '2024-01-05T15:00:00',
          createdAt: '2024-01-06T11:10:00Z',
          helpfulCount: 5,
          tags: ['居家清潔', '推薦']
        },
        {
          id: 1005,
          caregiverMemberId: caregiverId,
          memberName: '會員 5',
          rating: 5,
          comment: '非常滿意的服務體驗！照服員不僅專業技能佳，人也很親切溫暖，讓長輩感到很舒適安心。',
          serviceDate: '2024-01-03T08:00:00',
          createdAt: '2024-01-04T13:30:00Z',
          helpfulCount: 20,
          tags: ['專業護理', '極力推薦']
        }
      ]
    }

    // 初始化
    onMounted(() => {
      loadCaregiverData()
    })

    return {
      // 資料
      bg,
      caregiver,
      reviews,
      loading,
      loadingMore,
      selectedRatingFilter,
      sortOrder,
      hasMoreReviews,

      // 計算屬性
      averageRating,
      totalReviews,
      filteredReviews,

      // 方法
      getCaregiverImage,
      handleImageError,
      formatDate,
      getRatingPercentage,
      getRatingCount,
      filterReviews,
      sortReviews,
      loadMoreReviews,
      goBack
    }
  }
}
</script>

<style scoped>
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

/* 評分條動畫 */
.bg-yellow-400 {
  transition: width 0.6s ease-in-out;
}

/* 浮動按鈕陰影 */
.shadow-lg {
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
}

/* 響應式設計 */
@media (max-width: 768px) {
  .container {
    padding-left: 1rem;
    padding-right: 1rem;
  }

  .flex-col {
    align-items: stretch;
  }

  .gap-4 {
    gap: 0.75rem;
  }
}
</style>
