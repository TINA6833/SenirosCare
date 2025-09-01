<template>
  <div class="dashboard-main-body">

    <!-- 引入復康巴士新增元件 -->
    <AddBus @add-success="handleAddSuccess" />

  </div>
</template>

<script>
import Breadcrumb from '@/components/breadcrumb/Breadcrumb.vue'
// 引入 AddBus 元件
import AddBus from '@/components/Rehabus/AddBus.vue'
// 引入 Vue Router 的 useRouter 組合式 API
import { useRouter } from 'vue-router'

export default {
  name: "RehabusAdd",
  components: {
   Breadcrumb,
    AddBus  // 註冊元件
  },
  setup() {
    // 獲取路由實例
    const router = useRouter()

    /**
     * 處理新增巴士成功的回調函數
     * 成功新增巴士後導航回列表頁面
     */
    const handleAddSuccess = () => {
      // 顯示成功訊息 (可選)
      // 這裡可以使用 toast 或 notification 組件
      
      // 導航回巴士列表頁面
      router.push('/rehabus/list')
    }

    // 返回方法供模板使用
    return {
      handleAddSuccess
    }
  }
};
</script>