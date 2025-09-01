<template>
  <!-- [重點] 傳入 redirect-after-login，讓登入後回到結帳頁 -->
  <AuthPrompt
    title="結帳"
    message="請先登入以完成結帳流程"
    login-button-text="使用 Line 登入"
    :show-toast="true"
    :redirect-after-login="currentPath"
  >
    <div>
      <main-navbar />
      <div class="container max-w-[900px] mx-auto py-10 text-lg"> <!-- 字體加大 -->
        <h2 class="text-3xl font-bold mb-8">結帳</h2>
        <div v-if="addresses.length === 0" class="mb-4 text-red-500">請先新增收件地址</div>
        <div v-else>
          <div class="mb-6">
            <b>選擇收件地址</b>
            <div v-for="addr in addresses" :key="addr.id" class="my-2">
              <label>
                <input type="radio" v-model="selectedAddressId" :value="addr.id" class="mr-2" />
                {{ addr.recipient }}，{{ addr.phone }}，
                {{ addr.postalCode }} {{ addr.addressLine1 }}<span v-if="addr.addressLine2"> {{ addr.addressLine2 }}</span>
              </label>
            </div>
          </div>
          <div class="mb-6">
            <b>選擇付款方式</b>
            <select v-model="paymentMethod" class="ml-2 px-3 py-2 border rounded">
              <option value="LINE_PAY">LINE Pay</option>
              <option value="COD">貨到付款</option>
            </select>
          </div>
          <div class="mb-6">
            <b>訂單摘要</b>
            <ul>
              <li v-for="item in cartStore.detailedItems" :key="item.id">
                {{ item.name }} × {{ item.quantity }} - NT$ {{ (item.unitPrice * item.quantity).toLocaleString('zh-TW') }}
              </li>
            </ul>
            <div class="font-bold mt-2">總金額: NT$ {{ cartStore.totalAmount.toLocaleString('zh-TW') }}</div>
          </div>
          <!-- 主要按鈕樣式與 ProductList.vue 一致 -->
          <button
            class="px-6 py-3 border border-gray-400 bg-white text-gray-800 hover:bg-gray-100 hover:text-primary rounded transition btn-sm font-semibold text-lg"
            @click="placeOrder"
            :disabled="!selectedAddressId || cartStore.detailedItems.length === 0 || loading"
          >
            {{ paymentMethod === 'COD' ? '建立訂單' : '建立訂單並前往付款' }}
          </button>
        </div>
      </div>
      <!-- Toast 彈窗改為右上角 -->
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
  </AuthPrompt>
</template>

<script setup>
import AuthPrompt from '@/components/auth/AuthPrompt.vue'
import { ref, onMounted } from 'vue'
import mainNavbar from '@/components/navbar/main-navbar.vue'
import { useCartStore } from '@/stores/cartStore'
import axiosInstance from '@/api/axiosInstance' // 改為使用 axiosInstance
import { useRouter, useRoute } from 'vue-router'

const cartStore = useCartStore()
const addresses = ref([])
const selectedAddressId = ref(null)
const paymentMethod = ref('LINE_PAY')
const loading = ref(false)
const router = useRouter()
const route = useRoute()

// [重點] 取得目前路徑，登入後自動導回
const currentPath = ref(route.fullPath)

// Toast 狀態
const toast = ref({ show: false, message: '' })

function showToast(msg) {
  toast.value = { show: true, message: msg }
  setTimeout(() => {
    toast.value.show = false
    // 跳轉到訂單列表
    router.push('/orders')
  }, 1800)
}

async function fetchAddresses() {
  try {
    // 這裡假設會員ID為1，實際應從登入狀態取得
    const res = await axiosInstance.get('/addresses/member') 
    addresses.value = res.data
  } catch (e) {
    alert('載入地址失敗：' + (e?.message || e))
  }
}

async function placeOrder() {
  if (!selectedAddressId.value) {
    alert('請選擇一個收件地址！')
    return
  }
  try {
    loading.value = true
    const orderRequest = {
      cartId: cartStore.cartId,
      addressId: selectedAddressId.value,
      paymentMethod: paymentMethod.value,
    }
    const createOrderResponse = await axiosInstance.post('/orders', orderRequest) // 改為 axiosInstance
    if (createOrderResponse.status === 200) {
      // 查詢會員最新訂單
      const ordersRes = await axiosInstance.get('/orders') // 改為 axiosInstance
      const newOrder = ordersRes.data[0]
      cartStore.clearCart()
      if (paymentMethod.value === 'LINE_PAY') {
        // 取得付款網址並導向（不顯示 alert）
        const payRes = await axiosInstance.get(`/pay/orders/${newOrder.id}`) // 改為 axiosInstance
        const paymentUrl = payRes.data.paymentUrl
        if (paymentUrl) {
          window.location.href = paymentUrl
        } else {
          alert('無法取得付款連結！')
          router.push('/orders')
        }
      } else if (paymentMethod.value === 'COD') {
        // 顯示美觀 Toast，1.8秒後自動跳轉
        showToast('訂單建立成功！')
      }
    } else {
      alert('訂單建立失敗：' + createOrderResponse.data)
    }
  } catch (e) {
    alert('訂單建立失敗，請稍後再試')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  cartStore.fetchCart()
  fetchAddresses()
})
</script>

<script>
export default {
  name: 'CheckoutPage'
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