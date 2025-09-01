<template>
  <div class="dashboard-main-body">

    <!-- 引入預約編輯元件 -->
    <EditRes :reservationId="reservationId" @edit-success="handleEditSuccess" />

  </div>
</template>

<script>
import Breadcrumb from '@/components/breadcrumb/Breadcrumb.vue'
// 引入 EditRes 元件
import EditRes from '@/components/Rehabus/EditRes.vue'
// 引入 Vue Router 的 useRouter 組合式 API
import { useRouter } from 'vue-router'

export default {
  name: "ReservationEdit",
  components: {
    Breadcrumb,
    EditRes  // 註冊元件
  },
  setup() {
    // 獲取路由實例
    const router = useRouter()

    /**
     * 從路由參數獲取預約ID
     */
    const reservationId = router.currentRoute.value.params.id
    console.log('獲取到的預約 ID:', reservationId);
    
    /**
     * 處理編輯預約成功的回調函數
     * 成功編輯預約後導航回列表頁面
     */
    const handleEditSuccess = () => {
      // 導航回預約列表頁面
      router.push('/reservation/list')
    }

    // 返回方法供模板使用
    return {
      reservationId,
      handleEditSuccess
    }
  }
};
</script>