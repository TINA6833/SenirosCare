<template>
  <div>
    <MainNavbar />
    <div style="height: 140px;"></div>
    <div class="container-fluid max-w-[1200px] mx-auto py-10">
      <div v-if="device" class="grid grid-cols-1 md:grid-cols-2 gap-10">
        <div>
          <!-- 圖片網址加上 /images/ 前綴 -->
          <img :src="getImageUrl(device.image)" alt="商品圖片" class="w-full rounded-lg shadow" />
        </div>
        <div>
          <h2 class="text-3xl font-bold mb-4">{{ device.name }}</h2>
          <p class="text-xl text-primary mb-2">NT$ {{ device.unitPrice }}</p>
          <p class="mb-4">{{ device.description }}</p>
          <div class="flex gap-4 mb-6">
            <button
              class="px-4 py-2 border border-gray-400 bg-white text-gray-800 hover:bg-gray-100 hover:text-primary rounded transition btn-sm"
              @click="addToCart"
            >加入購物車</button>
            <button
              class="px-4 py-2 border border-gray-400 bg-white text-gray-800 hover:bg-gray-100 hover:text-primary rounded transition btn-sm"
              @click="toggleFavorite"
            >
              {{ isFavorite(device?.id) ? '移除收藏' : '加入收藏' }}
            </button>
          </div>
          <ul class="text-gray-500 text-sm">
            <li>庫存：{{ device.inventory }}</li>
            <!-- 顯示分類名稱 -->
            <li>分類：{{ categoryName }}</li>
            <li>商品編號：{{ device.sku }}</li>
          </ul>
        </div>
      </div>
      <div v-else class="text-center py-20 text-gray-400">載入中...</div>
      <!-- 商品細節分頁 -->
      <detail-tab :description="device?.description" />
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
  </div>
          <FooterThree />
        <ScrollToTop />
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import MainNavbar from '@/components/navbar/main-navbar.vue';
import FooterThree from '@/components/footer/footer-three.vue';
import ScrollToTop from '@/components/scroll-to-top.vue';
import detailTab from '@/components/product/detail-tab.vue'
import { getDeviceById } from '@/service/deviceService'
import { getOrCreateCart, addItemToCart } from '@/service/cartService'
// 新增：引入分類 service
import { getDeviceCategoryById } from '@/service/devicecategoryService'

const route = useRoute()
const device = ref(null)
const categoryName = ref('') // 新增：分類名稱
const favorites = ref(JSON.parse(localStorage.getItem('favorites') || '[]'))
const toast = ref({ show: false, message: '' })

// 圖片網址組合
function getImageUrl(image) {
  if (!image) return ''
  if (image.startsWith('http')) return image
  return `http://localhost:8080/images/${image}`
}

// 顯示彈跳視窗
function showToast(msg) {
  toast.value = { show: true, message: msg }
  setTimeout(() => {
    toast.value.show = false
  }, 3000)
}

// 加入購物車
async function addToCart() {
  if (!device.value) return
  try {
    const cart = await getOrCreateCart()
    await addItemToCart(cart.cartId, device.value.id, 1)
    showToast(`「${device.value.name}」已加入購物車`)
  } catch (e) {
    showToast('加入購物車失敗：' + (e?.response?.data?.message || e?.message || e))
  }
}

// 判斷是否已收藏
function isFavorite(id) {
  return favorites.value.includes(id)
}

// 加入/移除收藏
function toggleFavorite() {
  if (!device.value) return
  const id = device.value.id
  if (isFavorite(id)) {
    favorites.value = favorites.value.filter(fid => fid !== id)
    showToast(`「${device.value.name}」已從收藏移除`)
  } else {
    favorites.value.push(id)
    showToast(`「${device.value.name}」已加入收藏`)
  }
  localStorage.setItem('favorites', JSON.stringify(favorites.value))
}

onMounted(async () => {
  const id = Number(route.params.id)
  device.value = await getDeviceById(id)
  // 取得分類名稱
  if (device.value?.categoryId) {
    const category = await getDeviceCategoryById(device.value.categoryId)
    categoryName.value = category?.name || ''
  }
})
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