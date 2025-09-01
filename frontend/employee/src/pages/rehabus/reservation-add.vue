<template>
  <div class="dashboard-main-body">

    <!-- 引入預約新增元件 -->
    <AddRes @add-success="handleAddSuccess" />

  </div>
</template>

<script>
import Breadcrumb from '@/components/breadcrumb/Breadcrumb.vue'
// 引入 AddRes 元件
import AddRes from '@/components/Rehabus/AddRes.vue'
// 引入 Vue Router 的 useRouter 組合式 API
import { useRouter } from 'vue-router'

export default {
  name: "ReservationAdd",
  components: {
    Breadcrumb,
    AddRes  // 註冊元件
  },
  setup() {
    // 獲取路由實例
    const router = useRouter()

    /**
     * 處理新增預約成功的回調函數
     * 成功新增預約後導航回列表頁面
     */
    const handleAddSuccess = () => {
      // 顯示成功訊息 (可選)
      // 這裡可以使用 toast 或 notification 組件
      
      // 導航回預約列表頁面
      router.push('/reservation/list')
    }

    // 返回方法供模板使用
    return {
      handleAddSuccess
    }
  }
};
</script>