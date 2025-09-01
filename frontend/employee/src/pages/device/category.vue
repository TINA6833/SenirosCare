<template>
  <div class="dashboard-main-body">

    <!-- 顯示錯誤訊息區域 -->
    <div v-if="error" class="alert alert-danger mb-4">
      <strong>錯誤：</strong> {{ error.message }}
    </div>

    <!-- 主要內容區 -->
    <DeviceCategoryTable />

  </div>
</template>

<script>
import Breadcrumb from '@/components/breadcrumb/Breadcrumb.vue';
import DeviceCategoryTable from '@/components/DeviceCategory/DeviceCategoryTable.vue';
import { useDeviceCategory } from '@/composables/useDeviceCategory';

export default {
  name: "DeviceCategory",
  components: {
    Breadcrumb,
    DeviceCategoryTable
  },
  setup() {
    // 使用 composable 來獲取錯誤狀態
    const { error } = useDeviceCategory();
    return { error };
  }
};
</script>