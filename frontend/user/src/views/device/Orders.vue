<template>
  <div>
    <main-navbar />
    <div class="container max-w-[900px] mx-auto py-10">
      <!-- 重點註解：加上 mt-12 讓訂單列表區塊往下移，避免被 header/LOGO 遮住 -->
      <div class="mt-12">
        <h2 class="text-2xl font-bold mb-6 text-center">訂單列表</h2>
        <div v-if="orders.length === 0" class="text-gray-400 text-center">尚無訂單</div>
        <div v-else class="overflow-x-auto">
          <table class="w-full border rounded-lg overflow-hidden">
            <thead>
              <tr class="bg-gray-100">
                <th class="text-center py-3 w-32">訂單編號</th>
                <th class="text-center py-3 w-56">建立時間</th>
                <th class="text-center py-3 w-32">總金額</th>
                <th class="text-center py-3 w-24">狀態</th>
                <th class="text-center py-3 w-32">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="order in filteredOrders" :key="order.id" class="border-b hover:bg-gray-50">
                <td class="text-center py-2">{{ order.id }}</td>
                <td class="text-center py-2">{{ formatDate(order.createdAt) }}</td>
                <td class="text-center py-2">NT$ {{ Number(order.totalAmount).toLocaleString('zh-TW') }}</td>
                <td class="text-center py-2">{{ order.paymentStatus }}</td>
                <td class="text-center py-2">
                  <router-link
                    :to="`/orders/${order.id}`"
                    class="inline-block min-w-[80px] px-4 py-2 border border-gray-400 bg-white text-gray-800 hover:bg-gray-100 hover:text-primary rounded transition btn-sm text-center"
                  >查看</router-link>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import mainNavbar from '@/components/navbar/main-navbar.vue'
import { getOrdersByMember } from '@/service/orderService'

// 狀態
const orders = ref([])

// 只顯示已付款訂單，並依建立時間倒序
const filteredOrders = computed(() =>
  orders.value
    .filter(o => o.paymentStatus === 'PAID' || o.paymentStatus === 'PENDING')
    .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
)


// 建立時間格式化：2025-08-19-10:39:12.66
function formatDate(str) {
  if (!str) return ''
  // 取日期與時間
  const [date, time] = str.split('T')
  if (!date || !time) return str
  // 取到小數點後2位
  const ms = time.split('.')[1]?.substring(0, 2) || ''
  const t = time.split('.')[0]
  return `${date}-${t}${ms ? '.' + ms : ''}`
}

// 取得訂單
async function fetchOrders() {
  try {
    orders.value = await getOrdersByMember()
  } catch (e) {
    alert('載入訂單失敗：' + (e?.message || e))
  }
}

onMounted(fetchOrders)
</script>

<script>
export default {
  name: 'OrdersPage'
}
</script>