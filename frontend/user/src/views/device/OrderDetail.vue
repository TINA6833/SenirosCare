<template>
  <div>
    <main-navbar />
    <div class="container max-w-[900px] mx-auto py-10">
      <h2 class="text-2xl font-bold mb-8">訂單明細</h2>
      <div v-if="!order" class="text-gray-400">載入中...</div>
      <div v-else>
        <!-- 訂單資訊卡片 -->
        <div class="bg-white rounded-lg shadow p-6 mb-8">
          <div class="mb-2"><span class="font-semibold">訂單編號：</span>{{ order.id }}</div>
          <div class="mb-2"><span class="font-semibold">建立時間：</span>{{ formatDate(order.createdAt) }}</div>
          <div class="mb-2"><span class="font-semibold">總金額：</span>NT$ {{ Number(order.totalAmount).toLocaleString('zh-TW') }}</div>
          <div class="mb-2"><span class="font-semibold">狀態：</span>{{ order.paymentStatus }}</div>
          <!-- 顯示出貨狀態 -->
          <div class="mb-2">
            <span class="font-semibold">出貨狀態：</span>
            {{ order.status === 'SHIPPED' ? '已出貨' : order.status === 'PENDING' ? '未出貨' : order.status === 'CANCELLED' ? '已取消' : order.status === 'COMPLETED' ? '已完成' : order.status === 'RETURNED' ? '已退貨' : order.status || '尚未出貨' }}
          </div>
          <div><span class="font-semibold">出貨地址：</span>
            <template v-if="shippingAddress">
              {{ shippingAddress.recipient }}，{{ shippingAddress.phone }}，
              {{ shippingAddress.postalCode }} {{ shippingAddress.addressLine1 }}{{ shippingAddress.addressLine2 || '' }}
            </template>
            <template v-else>
              尚未填寫
            </template>
          </div>
        </div>
        <h3 class="text-xl font-bold mb-4">商品明細</h3>
        <div class="overflow-x-auto">
          <table class="w-full border rounded-lg overflow-hidden">
            <thead>
              <tr class="bg-gray-100">
                <th class="text-left py-3 px-4">商品名稱</th>
                <th class="text-center py-3 px-4">單價</th>
                <th class="text-center py-3 px-4">數量</th>
                <th class="text-center py-3 px-4">小計</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="item in order.items"
                :key="item.deviceId"
                class="border-b hover:bg-gray-50"
              >
                <!-- 這裡將 deviceName 改為 name -->
                <td class="text-left py-2 px-4">{{ item.name }}</td>
                <td class="text-center py-2 px-4">NT$ {{ Number(item.unitPrice).toLocaleString('zh-TW') }}</td>
                <td class="text-center py-2 px-4">{{ Number(item.quantity) }}</td>
                <td class="text-center py-2 px-4">NT$ {{ (Number(item.unitPrice) * Number(item.quantity)).toLocaleString('zh-TW') }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import mainNavbar from '@/components/navbar/main-navbar.vue'
import { useRoute } from 'vue-router'
import { getOrderById } from '@/service/orderService'
import { fetchAddressesByMember } from '@/api/address'

const route = useRoute()
const order = ref(null)
const shippingAddress = ref(null) // 這裡改成物件

function formatDate(str) {
  if (!str) return ''
  const [date, time] = str.split('T')
  if (!date || !time) return str
  const ms = time.split('.')[1]?.substring(0, 2) || ''
  const t = time.split('.')[0]
  return `${date}-${t}${ms ? '.' + ms : ''}`
}

onMounted(async () => {
  try {
    order.value = await getOrderById(route.params.id)
    // 若 order.value.addressId 存在，查詢地址
    if (order.value.addressId) {
      const { data } = await fetchAddressesByMember(order.value.memberId || 1)
      const addr = data.find(a => a.id === order.value.addressId)
      // 請先 console.log(addr) 看欄位
      console.log('查到的地址物件:', addr)
      shippingAddress.value = addr || null
    }
  } catch (e) {
    alert('載入訂單明細失敗：' + (e?.message || e))
  }
})
</script>