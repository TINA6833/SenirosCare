<template>
  <div class="dashboard-main-body">

    <!-- DeviceTable 元件顯示列表 -->
    <DeviceTable />
  </div>
</template>

<script setup>
import Breadcrumb from '@/components/breadcrumb/Breadcrumb.vue'
import DeviceTable from '@/components/device/DeviceTable.vue'
</script>

<style scoped>
/* 可依需求調整 page 區塊樣式 */
.dashboard-main-body {
  padding: 1rem;
}
</style>