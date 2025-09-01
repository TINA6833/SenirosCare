<!-- filepath: d:\git\SeniorsCare\frontend\employee\src\pages\activity\activityReservationList.vue -->
<template>
  <div class="activity-reservation-page">
    <!-- 重點註解：引入活動報名管理元件 -->
    <ActivityReservationList />
  </div>
</template>

<script setup>
// 重點註解：引入活動報名管理元件
import ActivityReservationList from '@/components/Activity/ActivityReservationList.vue';

// 重點註解：pages 層主要負責掛載元件和處理元件間的資料流
// 頁面標題由路由的 meta.title 統一處理，無需在此設定
</script>

<style scoped>
.activity-reservation-page {
  width: 100%;
  height: 100%;
}
</style>