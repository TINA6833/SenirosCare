<template>
  <router-view/>
  <ToastContainer />
  <ConfirmDialog />
</template>

<script setup>
// [重點] 引入 onMounted 生命週期鉤子
import { onMounted } from 'vue';
import ToastContainer from '@/components/ToastContainer.vue';
import ConfirmDialog from '@/components/ConfirmDialog.vue';
// [重點] 使用安全的 AuthStore 包裝器
import { useSafeAuthStore } from '@/composables/useSafeStore';
import { memberService } from '@/service/memberService';

// [重點] 使用安全的 AuthStore，即使在 Pinia 未準備好時也不會報錯
const authStore = useSafeAuthStore();

// [重點] 在 onMounted 鉤子中執行初始化邏輯
onMounted(async () => {
  // [重點] 檢查是否真的是有效的 store（不是預設狀態）
  if (authStore.token && typeof authStore.setUser === 'function') {
    try {
      // 透過 memberService 來獲取並轉換使用者資料
      const userData = await memberService.getCurrentMember();
      // 將取得的資料存入 store
      authStore.setUser(userData);
    } catch (error) {
      // 如果 API 呼叫失敗 (例如 token 過期)，axios 攔截器會處理提示
      console.error('App.vue onMounted: 取得使用者資料失敗，將清除本地 token。', error);
      authStore.logout();
    }
  }
});
</script>
