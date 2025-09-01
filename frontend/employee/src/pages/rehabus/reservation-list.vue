<template>
  <div class="dashboard-main-body">

    <!-- 預約列表元件 -->
    <ResList />

  </div>
</template>

<script>
import Breadcrumb from '@/components/breadcrumb/Breadcrumb.vue'
import ResList from '@/components/Rehabus/ResList.vue'

export default {
  name: "ReservationList",
  components: {
    Breadcrumb,
    ResList,
  },
};
</script>