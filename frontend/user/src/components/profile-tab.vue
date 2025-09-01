<template>
    <ul class="divide-y dark:divide-paragraph text-title dark:text-white text-base sm:text-lg lg:text-xl flex flex-col justify-center leading-none">
      <li class="py-3 lg:py-6 pl-6 lg:pl-12" :class="{'active text-primary' : current === '/my-account'}">
            <router-link class="duration-300 hover:text-primary" to="/my-account">個人資料</router-link>
        </li>
        <!-- [重點] 將登出連結改為按鈕，並加上點擊處理邏輯 -->
        <li class="pt-3 lg:pt-6 pl-6 lg:pl-12">
            <button 
                @click="handleLogout" 
                class="duration-300 hover:text-primary text-left w-full"
                :class="{'text-primary' : isLoggingOut}"
                :disabled="isLoggingOut"
            >
                <!-- [重點] 根據狀態顯示不同文字 -->
                <span v-if="isLoggingOut">登出中...</span>
                <span v-else>登出</span>
            </button>
        </li>
    </ul>
</template>

<script setup>
import { ref } from 'vue';
import { useRoute } from 'vue-router';
import { useAuth } from '@/composables/useAuth';
import { useToast } from '@/composables/useToast';
import { useConfirmDialog } from '@/composables/useConfirmDialog';

// [重點] 取得路由和認證相關功能
const route = useRoute();
const { logout } = useAuth();
const { showToast } = useToast();
const { showWarningDialog } = useConfirmDialog();

// [重點] 狀態管理
const current = ref(route.path);
const isLoggingOut = ref(false);

/**
 * [重點] 處理登出按鈕點擊事件 - 加入確認對話框
 */
async function handleLogout() {
  try {
    // [重點] 顯示確認對話框，詢問使用者是否確定要登出
    const confirmed = await showWarningDialog(
      '登出後需要重新登入才能存取會員功能，您確定要登出嗎？',
      '確認登出',
      {
        confirmText: '確定登出',
        cancelText: '取消'
      }
    );
    
    // [重點] 如果使用者取消，直接返回
    if (!confirmed) {
      return;
    }
    
    // [重點] 使用者確認後，開始執行登出流程
    isLoggingOut.value = true;
    
    
    // [重點] 執行登出邏輯
    const result = await logout();
    
    if (result.success) {
      showToast(result.message, 'success');
    } else {
      showToast(result.message, 'warning');
    }
    
  } catch (error) {
    // [重點] 檢查是否是使用者取消的錯誤
    if (error === false) {
      // [重點] 使用者取消登出，不顯示錯誤訊息
      return;
    }
    
    showToast('登出失敗，請稍後再試', 'error');
    
    // [重點] 即使失敗也要嘗試跳轉回首頁
    try {
      window.location.href = '/';
    } catch (redirectError) {
      console.error('跳轉失敗:', redirectError);
    }
  } finally {
    isLoggingOut.value = false;
  }
}
</script>

<style scoped>
/* [重點] 登出按鈕的樣式 */
button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.text-primary {
  color: var(--primary-color, #007bff);
}
</style>
