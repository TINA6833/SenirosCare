<template>
  <div>
    <!-- [重點] 移除 AuthPrompt 包裝，讓所有使用者都能瀏覽商品 -->
    <main-navbar />
    <!-- 頂部留白，避免導覽列壓住內容 -->
    <div style="height: 140px;"></div>
    
    <!-- [重點] 頁首橫幅區域 -->
    <div class="flex items-center gap-4 flex-wrap bg-overlay p-14 sm:p-16 before:bg-title before:bg-opacity-70"
         :style="{ backgroundImage: 'url(' + bg + ')' }">
      <div class="text-center w-full">
        <h2 class="text-white text-8 md:text-[40px] font-normal leading-none text-center">輔具商城</h2>
        <ul class="flex items-center justify-center gap-[10px] text-base md:text-lg leading-none font-normal text-white mt-3 md:mt-4">
          <li><router-link to="/">首頁</router-link></li>
          <li>/</li>
          <li class="text-primary">輔具商城</li>
        </ul>
      </div>
    </div>

    <div class="container-fluid max-w-[1720px] mx-auto py-10">
      <h2 class="text-3xl font-bold mb-8">商品總覽</h2>
      
      <!-- [重點] 搜尋和篩選區域 -->
      <div class="mb-6 flex gap-4 flex-wrap">
        <!-- 分類下拉選單 -->
        <select v-model="selectedCategory" @change="onCategoryChange" 
                class="px-6 py-3 rounded-md transition-all bg-primary text-white font-medium hover:bg-primary-dark">
          <option value="">全部分類</option>
          <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
        </select>
        
        <!-- 搜尋輸入框 -->
        <input v-model="keyword" @keyup.enter="search" type="text" placeholder="請輸入商品名稱關鍵字"
               class="border rounded px-3 py-2 w-64" />
        
        <!-- [重點] 統一按鈕樣式 -->
        <button @click="search" 
                class="px-6 py-3 rounded-md transition-all bg-primary text-white font-medium hover:bg-primary-dark">
          搜尋
        </button>
        
        <!-- [重點] 會員功能按鈕 - 需要登入才能使用 -->
        <button @click="handleCartAccess"
                class="px-6 py-3 rounded-md transition-all bg-primary text-white font-medium hover:bg-primary-dark">
          購物車
        </button>
        
        <button @click="handleWishlistAccess"
                class="px-6 py-3 rounded-md transition-all bg-primary text-white font-medium hover:bg-primary-dark">
          我的最愛
        </button>
        
        <button @click="handleOrdersAccess"
                class="px-6 py-3 rounded-md transition-all bg-primary text-white font-medium hover:bg-primary-dark">
          訂單查詢
        </button>
      </div>

      <!-- [重點] 商品列表 -->
      <div class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-8">
        <div v-for="item in deviceList" :key="item.id" class="border rounded-lg p-4 flex flex-col">
          <!-- 修改圖片樣式 -->
          <img :src="getImageUrl(item.image)" class="w-full h-48 object-contain mb-2" />
          
          <h4 class="font-bold mb-1">
            <router-link :to="`/product-details/${item.id}`" class="hover:text-primary underline">
              {{ item.name }}
            </router-link>
          </h4>
          
          <div class="text-primary mb-2">NT$ {{ Number(item.unitPrice).toLocaleString('zh-TW') }}</div>
          
          <!-- [重點] 商品操作按鈕區域 -->
          <div class="flex gap-2 mt-auto">
            <!-- [重點] 加入購物車按鈕 - 需要登入檢查 -->
            <button @click="handleAddToCart(item)" 
                    :disabled="isAddingToCart"
                    class="px-6 py-3 rounded-md transition-all bg-primary text-white font-medium hover:bg-primary-dark disabled:opacity-50 disabled:cursor-not-allowed">
              <span v-if="isAddingToCart">加入中...</span>
              <span v-else>加入購物車</span>
            </button>
            
            <!-- [重點] 收藏按鈕 - 需要登入檢查 -->
            <button @click="handleToggleFavorite(item)" 
                    :disabled="isTogglingFavorite"
                    class="px-6 py-3 rounded-md transition-all bg-primary text-white font-medium hover:bg-primary-dark disabled:opacity-50 disabled:cursor-not-allowed">
              <span v-if="isFavorite(item.id)">已收藏</span>
              <span v-else>收藏</span>
            </button>
          </div>
        </div>
      </div>

      <!-- [重點] 分頁按鈕 -->
      <div class="flex justify-center mt-8 gap-2">
        <button :disabled="page === 1" @click="prevPage" 
                class="px-6 py-3 rounded-md transition-all bg-primary text-white font-medium hover:bg-primary-dark disabled:opacity-50 disabled:cursor-not-allowed">
          上一頁
        </button>
        
        <span class="px-4 py-2">第 {{ page }} 頁</span>
        
        <button :disabled="!hasNext" @click="nextPage" 
                class="px-6 py-3 rounded-md transition-all bg-primary text-white font-medium hover:bg-primary-dark disabled:opacity-50 disabled:cursor-not-allowed">
          下一頁
        </button>
      </div>
    </div>

    <!-- [重點] 登入提示彈窗 -->
    <div v-if="showLoginModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50" @click="closeLoginModal">
      <div class="bg-white dark:bg-dark-secondary rounded-2xl p-8 max-w-md w-full mx-4" @click.stop>
        <div class="text-center space-y-6">
          <!-- [重點] 鎖定圖示 -->
          <div class="flex justify-center">
            <div class="w-16 h-16 bg-primary/10 rounded-full flex items-center justify-center">
              <i class="fa-solid fa-lock text-primary text-2xl"></i>
            </div>
          </div>

          <!-- [重點] 標題和訊息 -->
          <div>
            <h3 class="text-xl font-bold text-title dark:text-white mb-2">需要登入</h3>
            <p class="text-gray-600 dark:text-gray-300">{{ loginPromptMessage }}</p>
          </div>

          <!-- [重點] 按鈕區域 -->
          <div class="flex gap-3">
            <button @click="closeLoginModal" 
                    class="flex-1 px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors">
              取消
            </button>
            <button @click="handleLoginRedirect" 
                    :disabled="isLoggingIn"
                    class="flex-1 px-4 py-2 bg-primary text-white rounded-lg hover:bg-primary-dark transition-colors disabled:opacity-50">
              <span v-if="isLoggingIn">登入中...</span>
              <span v-else>登入</span>
            </button>
          </div>
        </div>
      </div>
    </div>

    <FooterThree />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import mainNavbar from '@/components/navbar/main-navbar.vue'
import FooterThree from '@/components/footer/footer-three.vue'

import { getAllDevices } from '@/service/deviceService'
import { getOrCreateCart, addItemToCart } from '@/service/cartService'
import { getAllDeviceCategories } from '@/service/devicecategoryService'
import { useToast } from '@/composables/useToast'
// [重點] 引入登入檢查功能
import { useAuth } from '@/composables/useAuth'

// [重點] 引入背景圖片
import bg from '@/assets/img/shortcode/breadcumb.jpg'

// [重點] 路由和 Composables
const router = useRouter()
const { showToast } = useToast()
const { isAuthenticated, login } = useAuth()

// [重點] 基本狀態
const deviceList = ref([])
const keyword = ref('')
const page = ref(1)
const pageSize = 12
const hasNext = ref(false)
const favorites = ref(JSON.parse(localStorage.getItem('favorites') || '[]'))
const categories = ref([])
const selectedCategory = ref('')

// [重點] 操作狀態
const isAddingToCart = ref(false)
const isTogglingFavorite = ref(false)
const isLoggingIn = ref(false)

// [重點] 登入提示彈窗狀態
const showLoginModal = ref(false)
const loginPromptMessage = ref('')
const pendingAction = ref(null)

/**
 * [重點] 圖片網址組合
 */
function getImageUrl(image) {
  if (!image) return ''
  if (image.startsWith('http')) return image
  return `http://localhost:8080/images/${image}`
}

/**
 * [重點] 取得所有分類
 */
async function fetchCategories() {
  try {
    categories.value = await getAllDeviceCategories()
  } catch (error) {
    console.error('載入分類失敗:', error)
    showToast('載入分類資料失敗', 'error')
  }
}

/**
 * [重點] 取得分頁商品
 */
async function fetchDevices() {
  try {
    // 取得所有商品
    const all = await getAllDevices()
    // 只顯示已上架的商品
    let onlineDevices = all.filter(device => device.isOnline)
    
    // 依分類篩選
    if (selectedCategory.value) {
      onlineDevices = onlineDevices.filter(d => d.categoryId === selectedCategory.value)
    }
    
    // 關鍵字搜尋
    let filtered = onlineDevices
    if (keyword.value.trim()) {
      filtered = onlineDevices.filter(d => d.name.includes(keyword.value.trim()))
      page.value = 1
    }
    
    // 分頁處理
    const start = (page.value - 1) * pageSize
    const end = start + pageSize
    deviceList.value = filtered.slice(start, end)
    hasNext.value = end < filtered.length
  } catch (error) {
    console.error('載入商品失敗:', error)
    showToast('載入商品資料失敗，請重新整理頁面', 'error')
  }
}

/**
 * [重點] 分類變更處理
 */
function onCategoryChange() {
  page.value = 1
  fetchDevices()
}

/**
 * [重點] 搜尋功能
 */
function search() {
  page.value = 1
  fetchDevices()
}

/**
 * [重點] 分頁導航
 */
function prevPage() {
  if (page.value > 1) {
    page.value--
    fetchDevices()
  }
}

function nextPage() {
  if (hasNext.value) {
    page.value++
    fetchDevices()
  }
}

/**
 * [重點] 收藏功能
 */
function isFavorite(id) {
  return favorites.value.includes(id)
}

/**
 * [重點] 檢查登入狀態的通用方法
 */
function checkLoginRequired(action, message) {
  if (!isAuthenticated.value) {
    loginPromptMessage.value = message
    pendingAction.value = action
    showLoginModal.value = true
    return false
  }
  return true
}

/**
 * [重點] 處理需要登入的頁面訪問
 */
function handleCartAccess() {
  if (checkLoginRequired('cart', '請先登入以查看購物車內容')) {
    router.push('/cart')
  }
}

function handleWishlistAccess() {
  if (checkLoginRequired('wishlist', '請先登入以查看我的最愛')) {
    router.push('/wishlist')
  }
}

function handleOrdersAccess() {
  if (checkLoginRequired('orders', '請先登入以查看訂單紀錄')) {
    router.push('/orders')
  }
}

/**
 * [重點] 處理收藏切換 - 需要登入
 */
async function handleToggleFavorite(item) {
  if (!checkLoginRequired('favorite', '請先登入以使用收藏功能')) {
    return
  }

  isTogglingFavorite.value = true
  try {
    if (isFavorite(item.id)) {
      favorites.value = favorites.value.filter(fid => fid !== item.id)
      showToast(`「${item.name}」已從收藏移除`, 'success')
    } else {
      favorites.value.push(item.id)
      showToast(`「${item.name}」已加入收藏`, 'success')
    }
    localStorage.setItem('favorites', JSON.stringify(favorites.value))
  } finally {
    isTogglingFavorite.value = false
  }
}

/**
 * [重點] 處理加入購物車 - 需要登入
 */
async function handleAddToCart(item) {
  if (!checkLoginRequired('addToCart', '請先登入以使用購物車功能')) {
    return
  }

  isAddingToCart.value = true
  try {
    const cart = await getOrCreateCart()
    await addItemToCart(cart.cartId, item.id, 1)
    showToast(`「${item.name}」已加入購物車`, 'success')
  } catch (error) {
    console.error('加入購物車失敗:', error)
    showToast('加入購物車失敗：' + (error?.response?.data?.message || error?.message || error), 'error')
  } finally {
    isAddingToCart.value = false
  }
}

/**
 * [重點] 關閉登入提示彈窗
 */
function closeLoginModal() {
  showLoginModal.value = false
  loginPromptMessage.value = ''
  pendingAction.value = null
}

/**
 * [重點] 處理登入重導向
 */
async function handleLoginRedirect() {
  isLoggingIn.value = true
  try {
    // [重點] 儲存當前頁面路徑，登入後返回
    localStorage.setItem('redirectAfterLogin', window.location.pathname + window.location.search)
    
    // [重點] 執行登入
    await login()
    
    // [重點] 關閉彈窗
    closeLoginModal()
  } catch (error) {
    console.error('登入失敗:', error)
    showToast('登入失敗，請稍後再試', 'error')
  } finally {
    isLoggingIn.value = false
  }
}

// [重點] 初始化
onMounted(() => {
  fetchCategories()
  fetchDevices()
})
</script>

<style scoped>
/* [重點] 背景遮罩樣式 */
.bg-overlay::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
}

/* [重點] 容器樣式 */
.container-fluid {
  width: 100%;
  padding-left: 15px;
  padding-right: 15px;
  margin-left: auto;
  margin-right: auto;
}

/* [重點] 響應式設計 */
@media (max-width: 768px) {
  .flex-wrap {
    flex-direction: column;
  }
  
  .gap-4 > * {
    width: 100%;
  }
}

/* [重點] 按鈕禁用狀態 */
button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* [重點] 商品卡片懸停效果 */
.border:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
  transition: all 0.3s ease;
}

/* [重點] 彈窗樣式 */
.fixed {
  position: fixed;
}

.inset-0 {
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
}

.z-50 {
  z-index: 50;
}

/* 修改圖片樣式，確保完整顯示 */
img {
  object-fit: contain;
}
</style>