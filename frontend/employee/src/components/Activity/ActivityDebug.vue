<template>
  <div class="card">
    <div class="card-header">
      <h5 class="card-title">調試頁面</h5>
    </div>
    <div class="card-body">
      <div class="alert alert-info">
        <p>此頁面用於測試活動管理系統是否正常工作</p>
        <p>當前時間: {{ currentTime }}</p>
      </div>
      
      <h6>活動系統狀態:</h6>
      <ul>
        <li>載入狀態: {{ loading ? '載入中' : '已載入' }}</li>
        <li>活動總數: {{ totalActivities }}</li>
        <li>報名中活動: {{ activeActivities.length }}</li>
        <li>已結束活動: {{ endedActivities.length }}</li>
      </ul>
      
      <button class="btn btn-primary" @click="refreshActivities">
        <i class="fas fa-sync" :class="{'fa-spin': loading}"></i> 重新載入
      </button>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue';
import { useActivity } from '@/composables/useActivity';

export default {
  name: 'ActivityDebug',
  setup() {
    const { 
      activities, 
      loading, 
      loadActivities,
      checkActivitiesStatus
    } = useActivity();
    
    const currentTime = ref(new Date().toLocaleString());
    
    // 每秒更新時間
    setInterval(() => {
      currentTime.value = new Date().toLocaleString();
    }, 1000);
    
    // 計算屬性
    const totalActivities = computed(() => activities.value.length);
    
    const activeActivities = computed(() => 
      activities.value.filter(activity => activity.status === true && !activity.isExpired)
    );
    
    const endedActivities = computed(() => 
      activities.value.filter(activity => activity.status === false || activity.isExpired)
    );
    
    // 重新載入活動資料
    const refreshActivities = async () => {
      await loadActivities();
      await checkActivitiesStatus();
    };
    
    // 頁面載入時執行
    onMounted(() => {
      refreshActivities();
    });
    
    return {
      currentTime,
      loading,
      totalActivities,
      activeActivities,
      endedActivities,
      refreshActivities
    };
  }
}
</script>

<style scoped>
.card {
  margin-top: 20px;
}
</style>
