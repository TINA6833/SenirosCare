<template>
    <div class="dashboard-main-body">


        <EditCareWorkerForm :caregiverId="$route.params.id" />
    </div>
</template>

<script>
import { useRoute } from 'vue-router';
import Breadcrumb from '@/components/breadcrumb/Breadcrumb.vue';
import EditCareWorkerForm from '@/components/Caregiver/EditCareWorker.vue'

export default {
  name: "EditCareWorkerPage",
  components: {
    Breadcrumb,
    EditCareWorkerForm,
  },
  setup() {
    const route = useRoute();
    
    return {
      // 返回必要的數據
    };
  }
}
</script>
