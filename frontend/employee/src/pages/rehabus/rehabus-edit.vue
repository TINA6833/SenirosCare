<template>
  <div class="dashboard-main-body">

    <!-- 此處放入編輯復康巴士的表單 -->
    <div class="card">
      <div class="card-header">
        <h5>編輯復康巴士資料</h5>
      </div>
      <div class="card-body">
        <!-- 表單內容將在後續實作 -->
    
      </div>
    </div>

    <!-- 引入復康巴士編輯元件 -->
    <EditBus :busId="busId" @edit-success="handleEditSuccess" /> 


  </div>
</template>

<script>
import Breadcrumb from '@/components/breadcrumb/Breadcrumb.vue'
// 引入 EditBus 元件
import EditBus from '@/components/Rehabus/EditBus.vue'
// 引入 Vue Router 的 useRouter 組合式 API
import { useRouter } from 'vue-router'

export default {
  name: "RehabusEdit",
  components: {
    Breadcrumb,
    EditBus  // 註冊元件
   },
   setup() {
    // 獲取路由實例
    const router = useRouter()

    /**
     * 從路由參數獲取巴士ID
     */
    const busId = router.currentRoute.value.params.id

    /**
     * 處理編輯巴士成功的回調函數
     * 成功編輯巴士後導航回列表頁面
     */
    const handleEditSuccess = () => {
      // 導航回巴士列表頁面
      router.push('/rehabus/list')
    }
 // 返回方法供模板使用
    return {
      busId,
      handleEditSuccess
    }
  }
};
</script>