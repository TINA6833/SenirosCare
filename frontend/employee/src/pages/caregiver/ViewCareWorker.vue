<template>
    <div class="dashboard-main-body">

        <ViewCareWorker :caregiverId="$route.params.id" />
    </div>
</template>

<script>
import { useRoute } from 'vue-router';
import Breadcrumb from '@/components/breadcrumb/Breadcrumb.vue';
import ViewCareWorker from '@/components/Caregiver/ViewCareWorker.vue';

export default {
  name: "ViewCareWorkerPage",
  components: {
    Breadcrumb,
    ViewCareWorker
  },
  setup() {
    const route = useRoute();
    
    return {
      // 返回必要的數據
    };
  }
}
</script>
