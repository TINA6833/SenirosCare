<template>
  <div class="dashboard-main-body">

    <!-- 復康巴士列表元件 -->
    <List />

  </div>
</template>

<script>
import Breadcrumb from '@/components/breadcrumb/Breadcrumb.vue'
import List from '@/components/Rehabus/List.vue'

export default {
  name: "RehabusList",
  components: {
    Breadcrumb,
    List,
  },
};
</script>