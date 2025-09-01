<template>
  <div class="dashboard-main-body">

    <div class="row gy-4">
      <!-- 活動列表 -->
      <div class="col-12">
        <ActivityList />
      </div>
    </div>
  </div>
</template>

<script>
import Breadcrumb from '@/components/breadcrumb/Breadcrumb.vue';
import ActivityList from '@/components/Activity/ActivityList.vue';
import { onMounted } from 'vue';

export default {
  name: "ActivityBlog",
  components: {
    Breadcrumb,
    ActivityList
  },
  setup() {
    onMounted(() => {
      // 檢查 bootstrap 是否已加載
      if (typeof bootstrap === 'undefined') {
        console.warn('Bootstrap not found in global scope');
        console.info('You may need to import bootstrap in main.js or this component');
      } else {
        console.log('Bootstrap is available');
      }
    });
  }
}
</script>
