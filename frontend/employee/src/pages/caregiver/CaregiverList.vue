<template>
  <div class="dashboard-main-body">
    <!-- 引入照服員表格元件 -->
    <CaregiverTable
      :careWorkers="caregivers"
      :loading="isLoading"
      :error="error"
      @view="viewCaregiver"
      @edit="editCaregiver"
      @delete="deleteCaregiver"
    />
  </div>
</template>

<script>
import Breadcrumb from '@/components/breadcrumb/Breadcrumb.vue';
import CaregiverTable from '@/components/Caregiver/CaregiverTable.vue';
import { useCaregivers } from '@/composables/useCaregivers.js';
import { useRouter } from 'vue-router';

export default {
  name: "CaregiverList",
  components: {
    Breadcrumb,
    CaregiverTable
  },
  setup() {
    // 使用照服員資料 composable
    const {
      caregivers,
      loading: isLoading,
      error,
      loadCaregivers,
      searchCaregivers,
      deleteCaregiver: apiDeleteCaregiver
    } = useCaregivers({ autoLoad: true });

    // 使用路由
    const router = useRouter();

    /**
     * 查看照服員詳情
     * @param {Object} caregiver 照服員資料
     */
    const viewCaregiver = (caregiver) => {
      // 導航到照服員詳情頁面
      router.push(`/care-worker/${caregiver.id}`);
    };

    /**
     * 編輯照服員資料
     * @param {Object} caregiver 照服員資料
     */
    const editCaregiver = (caregiver) => {
      // 導航到編輯頁面
      router.push(`/care-worker/edit/${caregiver.id}`);
    };

    /**
     * 刪除照服員
     * @param {Object} caregiver 照服員資料
     */
    const deleteCaregiver = async (caregiver) => {
      try {
        await apiDeleteCaregiver(caregiver.id);
        // 刪除後重新載入資料
        await loadCaregivers(true);
      } catch (error) {
        // TODO: 顯示錯誤訊息給使用者
      }
    };

    return {
      // 狀態
      caregivers,
      isLoading,
      error,

      // 方法
      viewCaregiver,
      editCaregiver,
      deleteCaregiver
    };
  }
}
</script>