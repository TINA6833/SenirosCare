<template>
  <div>
    <main-navbar />
    <div class="container max-w-[1200px] mx-auto py-10">
      <!-- 重點註解：加上 ml-32 讓標題往右移，避免被 LOGO遮住 -->
      <h2 class="text-2xl font-bold mb-6 ml-32">我的最愛</h2>
      <div v-if="favoriteList.length === 0" class="text-gray-400 ml-32">尚未收藏任何商品</div>
      <div class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-8">
        <div v-for="item in favoriteList" :key="item.id" class="border rounded-lg p-4 flex flex-col">
          <img :src="getImageUrl(item.image)" class="w-full h-48 object-cover mb-2" />
          <h4 class="font-bold mb-1">{{ item.name }}</h4>
          <div class="text-primary mb-2">NT$ {{ Number(item.unitPrice).toLocaleString('zh-TW') }}</div>
          <div class="flex gap-2 mt-auto">
            <!-- 與 ProductList.vue 相同的按鈕樣式 -->
            <button
              class="px-4 py-2 border border-gray-400 bg-white text-gray-800 hover:bg-gray-100 hover:text-primary rounded transition btn-sm"
              @click="addToCart(item)"
            >加入購物車</button>
            <button
              class="px-4 py-2 border border-gray-400 bg-white text-gray-800 hover:bg-gray-100 hover:text-primary rounded transition btn-sm"
              @click="removeFavorite(item)"
            >移除收藏</button>
          </div>
        </div>
      </div>
    </div>
    <!-- Toast 彈窗（右上角，與 Checkout.vue 一致） -->
    <transition name="toast-fade">
      <div
        v-if="toast.show"
        class="fixed top-8 right-8 bg-white text-green-700 px-8 py-5 rounded-xl shadow-2xl z-50 flex flex-row items-center gap-4 text-lg font-bold border border-green-200"
        style="min-width: 280px; max-width: 350px;"
      >
        <div class="flex items-center justify-center">
          <div class="bg-green-500 rounded-full w-10 h-10 flex items-center justify-center shadow-lg">
            <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" stroke-width="3" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" d="M5 13l5 5L19 7"/>
            </svg>
          </div>
        </div>
        <div>{{ toast.message }}</div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import mainNavbar from '@/components/navbar/main-navbar.vue'
import { getAllDevices } from '@/service/deviceService'
import { getOrCreateCart, addItemToCart } from '@/service/cartService'

const favorites = ref(JSON.parse(localStorage.getItem('favorites') || '[]'))
const favoriteList = ref([])
const toast = ref({ show: false, message: '' })

// 顯示彈跳視窗（與 Checkout.vue 一致）
function showToast(msg) {
  toast.value = { show: true, message: msg }
  setTimeout(() => {
    toast.value.show = false
  }, 1800)
}

function getImageUrl(image) {
  if (!image) return ''
  if (image.startsWith('http')) return image
  return `http://localhost:8080/images/${image}`
}

async function fetchFavorites() {
  try {
    const all = await getAllDevices()
    favoriteList.value = all.filter(item => favorites.value.includes(item.id))
  } catch (e) {
    showToast('載入收藏商品失敗：' + (e?.message || e))
  }
}

function removeFavorite(item) {
  favorites.value = favorites.value.filter(fid => fid !== item.id)
  localStorage.setItem('favorites', JSON.stringify(favorites.value))
  fetchFavorites()
  showToast(`「${item.name}」已從收藏移除`)
}

async function addToCart(item) {
  try {
    const cart = await getOrCreateCart()
    await addItemToCart(cart.cartId, item.id, 1)
    showToast(`「${item.name}」已加入購物車`)
  } catch (e) {
    showToast('加入購物車失敗：' + (e?.response?.data?.message || e?.message || e))
  }
}

onMounted(fetchFavorites)
</script>

<script>
export default {
  name: 'WishlistPage'
}
</script>

<style scoped>
.toast-fade-enter-active, .toast-fade-leave-active {
  transition: all 0.4s cubic-bezier(.4,2,.6,1);
}
.toast-fade-enter-from, .toast-fade-leave-to {
  opacity: 0;
  transform: translateY(-20px) scale(0.95);
}
.toast-fade-enter-to, .toast-fade-leave-from {
  opacity: 1;
  transform: translateY(0) scale(1);
}
</style>