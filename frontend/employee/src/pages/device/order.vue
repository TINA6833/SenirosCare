<template>
  <div class="dashboard-main-body">

    <!-- 訂單表格 -->
    <OrderTable />

    <!-- 錯誤/成功訊息提示 -->
    <div v-if="toast.show" :class="`toast-container position-fixed bottom-0 end-0 p-3`">
      <div :class="`toast show align-items-center text-white bg-${toast.type} border-0`" role="alert">
        <div class="d-flex">
          <div class="toast-body">{{ toast.message }}</div>
          <button type="button" class="btn-close btn-close-white ms-auto me-2" @click="toast.show = false"></button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import Breadcrumb from '@/components/breadcrumb/Breadcrumb.vue'
import OrderTable from '@/components/DeviceOrder/OrderTable.vue'
import { useOrder } from '@/composables/useOrder'

export default {
  name: "OrderList",
  components: {
    Breadcrumb,
    OrderTable,
  },
  setup() {
    // 使用訂單composable取得toast狀態
    const { toast } = useOrder()
    
    return {
      toast
    }
  }
};

</script>