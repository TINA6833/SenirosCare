<template>
  <div>
    <main-navbar />
    <div class="container max-w-[1200px] mx-auto py-10">
      <!-- 重點註解：加上 ml-32 讓標題往右移，避免被 LOGO遮住 -->
      <h2 class="text-2xl font-bold mb-6 ml-32">您的購物車</h2>
      <div v-if="cartStore.detailedItems.length === 0" class="text-gray-400 ml-32">購物車內沒有商品</div>
      <div v-else>
        <div class="overflow-x-auto">
          <table class="w-full mb-6 min-w-[800px]">
            <thead>
              <tr>
                <th class="text-center w-28">商品</th>
                <th class="text-center w-48">名稱</th>
                <th class="text-center w-24">單價</th>
                <th class="text-center w-32">數量</th>
                <th class="text-center w-32">小計</th>
                <th class="text-center w-24">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in cartStore.detailedItems" :key="item.id" class="align-middle">
                <td class="text-center">
                  <img :src="getImageUrl(item.image)" alt="商品圖" class="w-20 h-20 object-cover mx-auto" />
                </td>
                <td class="text-center align-middle">{{ item.name }}</td>
                <td class="text-center align-middle">NT$ {{ Number(item.unitPrice).toLocaleString('zh-TW') }}</td>
                <td class="text-center align-middle">
                  <div class="flex items-center justify-center gap-2">
                    <button
                      class="px-2 py-1 border border-gray-400 bg-white text-gray-800 hover:bg-gray-100 rounded"
                      @click="changeQuantity(item.id, item.quantity - 1)"
                      :disabled="item.quantity <= 1"
                    >-</button>
                    <span class="inline-block w-8 text-center">{{ item.quantity }}</span>
                    <button
                      class="px-2 py-1 border border-gray-400 bg-white text-gray-800 hover:bg-gray-100 rounded"
                      @click="changeQuantity(item.id, item.quantity + 1)"
                    >+</button>
                  </div>
                </td>
                <td class="text-center align-middle">NT$ {{ (Number(item.unitPrice) * Number(item.quantity)).toLocaleString('zh-TW') }}</td>
                <td class="text-center align-middle">
                  <button
                    class="px-4 py-2 border border-gray-400 bg-white text-gray-800 hover:bg-gray-100 hover:text-primary rounded transition btn-sm"
                    @click="removeItem(item.id, item.name)"
                  >移除</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <div class="text-right mb-6 font-bold">總計: NT$ {{ cartStore.totalAmount.toLocaleString('zh-TW') }}</div>
        <router-link to="/checkout">
          <button
            class="px-4 py-2 border border-gray-400 bg-white text-gray-800 hover:bg-gray-100 hover:text-primary rounded transition btn-sm"
          >
            前往結帳
          </button>
        </router-link>
      </div>
    </div>
    <!-- Toast 訊息 -->
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
import { useCartStore } from '@/stores/cartStore'

const cartStore = useCartStore()
const toast = ref({ show: false, message: '' })

function getImageUrl(image) {
  if (!image) return ''
  if (image.startsWith('http')) return image
  return `http://localhost:8080/images/${image}`
}

// 數量調整（+/- 按鈕用）
function changeQuantity(deviceId, newQuantity) {
  if (newQuantity < 1) return
  cartStore.updateQuantity(deviceId, newQuantity)
}

// 移除商品並顯示移除成功提示
function removeItem(id, name) {
  cartStore.removeFromCart(id)
  showToast(`「${name}」已從購物車移除`)
}

// 顯示 toast 訊息
function showToast(msg) {
  toast.value = { show: true, message: msg }
  setTimeout(() => {
    toast.value.show = false
  }, 3000)
}

onMounted(() => {
  cartStore.fetchCart()
})
</script>

<script>
export default {
  name: 'CartPage'
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