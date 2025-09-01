<template>
  <!-- [重點] 登入狀態檢查載入中畫面 -->
  <div v-if="isCheckingAuth" class="min-h-screen flex items-center justify-center">
    <div class="text-center space-y-4">
      <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-primary mx-auto"></div>
      <p class="text-gray-600 dark:text-gray-400">檢查登入狀態中...</p>
    </div>
  </div>

  <!-- [重點] 未登入時顯示登入提示畫面 -->
  <div v-else-if="!isAuthenticated" class="min-h-screen bg-gray-50 dark:bg-dark-primary">
    <!-- [重點] 頂部橫幅區域 -->
    <div class="flex items-center gap-4 flex-wrap bg-overlay p-14 sm:p-16 before:bg-title before:bg-opacity-70"
         :style="{ backgroundImage: 'url(' + bgImage + ')' }">
      <div class="text-center w-full">
        <h2 class="text-white text-8 md:text-[40px] font-normal leading-none text-center">{{ title }}</h2>
      </div>
    </div>

    <!-- [重點] 主要登入提示內容區域 -->
    <div class="s-py-100 flex items-center min-h-[calc(100vh-200px)]">
      <div class="container-fluid">
        <div class="max-w-6xl mx-auto">
          <div class="flex flex-col lg:flex-row items-center gap-8 lg:gap-16">
            
            <!-- [重點] 左側圖片區域 -->
            <div class="w-full lg:w-1/2 flex justify-center">
              <div class="relative max-w-lg">
                <img 
                  :src="loginImage" 
                  alt="銀髮照護平台登入示意圖" 
                  class="w-full h-auto object-contain drop-shadow-2xl rounded-2xl"
                />
                <!-- [重點] 圖片裝飾背景 -->
                <div class="absolute -z-10 top-4 left-4 w-full h-full bg-primary/10 rounded-2xl"></div>
              </div>
            </div>

            <!-- [重點] 右側登入提示區域 -->
            <div class="w-full lg:w-1/2">
              <div class="bg-white dark:bg-dark-secondary p-8 lg:p-12 rounded-2xl shadow-xl">
                <div class="space-y-6">
                  <!-- [重點] 鎖定圖示 -->
                  <div class="flex justify-center lg:justify-start">
                    <div class="w-16 h-16 bg-primary/10 rounded-full flex items-center justify-center">
                      <i class="fa-solid fa-lock text-primary text-2xl"></i>
                    </div>
                  </div>

                  <!-- [重點] 標題區域 -->
                  <div class="text-center lg:text-left">
                    <h3 class="text-2xl md:text-3xl font-bold text-title dark:text-white mb-3">
                      會員專區
                    </h3>
                    <p class="text-base md:text-lg text-gray-600 dark:text-gray-300 leading-relaxed">
                      {{ message }}
                    </p>
                  </div>

                    <!-- [重點] 功能特色列表 -->
                    <div class="space-y-4">
                    <h4 class="text-lg font-semibold text-title dark:text-white">登入後可享有：</h4>
                    <div class="space-y-3">
                      <div class="flex items-center gap-3">
                      <div class="w-8 h-8 bg-green-100 dark:bg-green-900/30 rounded-full flex items-center justify-center flex-none">
                        <i class="fa-solid fa-calendar-check text-green-600 dark:text-green-400 text-sm"></i>
                      </div>
                      <span class="text-gray-700 dark:text-gray-300">照服員預約</span>
                      </div>
                      <div class="flex items-center gap-3">
                      <div class="w-8 h-8 bg-blue-100 dark:bg-blue-900/30 rounded-full flex items-center justify-center flex-none">
                        <i class="fa-solid fa-users text-blue-600 dark:text-blue-400 text-sm"></i>
                      </div>
                      <span class="text-gray-700 dark:text-gray-300">活動報名</span>
                      </div>
                      <div class="flex items-center gap-3">
                      <div class="w-8 h-8 bg-purple-100 dark:bg-purple-900/30 rounded-full flex items-center justify-center flex-none">
                        <i class="fa-solid fa-shopping-cart text-purple-600 dark:text-purple-400 text-sm"></i>
                      </div>
                      <span class="text-gray-700 dark:text-gray-300">輔具選購</span>
                      </div>
                    </div>
                    </div>

                  <!-- [重點] 登入按鈕區域 -->
                  <div class="pt-4">
                    <button 
                      @click="handleLogin" 
                      class="w-full inline-flex items-center justify-center gap-3 px-8 py-4 bg-primary text-white rounded-xl hover:bg-primary-dark transition-all duration-300 font-semibold text-lg shadow-lg hover:shadow-xl transform hover:-translate-y-0.5 disabled:opacity-50 disabled:cursor-not-allowed disabled:transform-none"
                      :disabled="isLoggingIn"
                    >
                      <i class="fab fa-line text-2xl"></i>
                      <span v-if="isLoggingIn">登入中...</span>
                      <span v-else>{{ loginButtonText }}</span>
                      <i v-if="isLoggingIn" class="fa-solid fa-spinner fa-spin"></i>
                    </button>
                  </div>

                  <!-- [重點] 安全提示區域 -->
                  <div class="mt-6 p-4 bg-blue-50 dark:bg-blue-900/20 rounded-lg border border-blue-200 dark:border-blue-800">
                    <div class="flex items-start gap-3">
                      <i class="fa-solid fa-shield-alt text-blue-600 dark:text-blue-400 mt-0.5"></i>
                      <div class="text-sm text-blue-700 dark:text-blue-300">
                        <p class="font-medium mb-1">安全登入保障</p>
                        <p>我們使用 Line 官方認證機制，確保您的個人資料安全無虞</p>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- [重點] 已登入時顯示實際內容 -->
  <div v-else>
    <slot />
  </div>
</template>

<script setup>
import { defineProps } from 'vue';
import { useAuthGuard } from '@/composables/useAuthGuard';

// [重點] 引入圖片資源
import loginImage from '@/assets/img/login.png';
import bgImage from '@/assets/img/shortcode/breadcumb.jpg';

// [重點] 定義元件 props
const props = defineProps({
  // [重點] 登入提示標題
  title: {
    type: String,
    default: '會員中心'
  },
  // [重點] 登入提示訊息
  message: {
    type: String,
    default: '請先登入以查看您的會員資訊'
  },
  // [重點] 登入按鈕文字
  loginButtonText: {
    type: String,
    default: '使用 Line 登入'
  },
  // [重點] 登入後重導向路徑
  redirectAfterLogin: {
    type: String,
    default: null
  },
  // [重點] 是否自動檢查登入狀態
  autoCheck: {
    type: Boolean,
    default: true
  },
  // [重點] 是否顯示 Toast 提示
  showToast: {
    type: Boolean,
    default: false
  }
});

// [重點] 使用登入守護 Composable
const {
  isCheckingAuth,
  isLoggingIn,
  isAuthenticated,
  requireLogin
} = useAuthGuard({
  autoCheck: props.autoCheck,
  showToast: props.showToast,
  redirectAfterLogin: props.redirectAfterLogin
});

/**
 * [重點] 處理登入按鈕點擊
 */
async function handleLogin() {
  await requireLogin();
}
</script>

<style scoped>
/* [重點] 載入動畫樣式 */
@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.animate-spin {
  animation: spin 1s linear infinite;
}

/* [重點] 按鈕樣式增強 */
button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* [重點] 響應式圖片樣式 */
img {
  max-height: 500px;
}

/* [重點] 背景遮罩樣式 */
.bg-overlay::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
}

/* [重點] 動畫效果 */
@media (prefers-reduced-motion: no-preference) {
  .transform {
    transition: transform 0.2s ease-in-out;
  }
}

/* [重點] 深色模式適配 */
@media (prefers-color-scheme: dark) {
  .drop-shadow-2xl {
    filter: drop-shadow(0 25px 25px rgba(255, 255, 255, 0.1));
  }
}

/* [重點] 工具類別樣式 */
.s-py-100 {
  padding-top: 100px;
  padding-bottom: 100px;
}

.container-fluid {
  width: 100%;
  padding-left: 15px;
  padding-right: 15px;
  margin-left: auto;
  margin-right: auto;
}
</style>