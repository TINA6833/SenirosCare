<template>
    <section class="auth bg-base d-flex flex-wrap">
      <div class="auth-left d-lg-block d-none">
        <div class="d-flex align-items-center flex-column h-100 justify-content-center">
          <img src="@/assets/images/auth/auth-img.png" alt="">
        </div>
      </div>
      <div class="auth-right py-32 px-24 d-flex flex-column justify-content-center">
        <div class="max-w-464-px mx-auto w-100">
          <div>
            <router-link to="/" class="mb-40 max-w-290-px">
              <img src="@/assets/images/logo.png" alt="">
            </router-link>
            <h4 class="mb-12">員工系統</h4>
            <p class="mb-32 text-secondary-light text-lg">歡迎回來，請輸入您的登入資料</p>
          </div>
          
          <!-- 登入表單 -->
          <form @submit.prevent="handleLogin">
            <!-- 電子郵件輸入框 -->
            <div class="icon-field mb-16">
              <span class="icon top-50 translate-middle-y">
                <iconify-icon icon="mage:email"></iconify-icon>
              </span>
              <input 
                type="email" 
                class="form-control h-56-px bg-neutral-50 radius-12" 
                placeholder="請輸入電子郵件" 
                v-model="loginForm.email"
                :class="{ 'is-invalid': formErrors.email }"
                :disabled="isLoading"
                required
              >
              <!-- 電子郵件錯誤提示 -->
              <div v-if="formErrors.email" class="invalid-feedback">
                {{ formErrors.email }}
              </div>
            </div>

            <!-- 密碼輸入框 -->
            <div class="position-relative mb-20">
              <div class="icon-field">
                <span class="icon top-50 translate-middle-y">
                  <iconify-icon icon="solar:lock-password-outline"></iconify-icon>
                </span>
                <input 
                  :type="showPassword ? 'text' : 'password'" 
                  class="form-control h-56-px bg-neutral-50 radius-12" 
                  id="your-password" 
                  placeholder="請輸入密碼" 
                  v-model="loginForm.password"
                  :class="{ 'is-invalid': formErrors.password }"
                  :disabled="isLoading"
                  required
                >
                <!-- 密碼錯誤提示 -->
                <div v-if="formErrors.password" class="invalid-feedback">
                  {{ formErrors.password }}
                </div>
              </div>
              <!-- 密碼顯示/隱藏切換按鈕 -->
              <span
                class="toggle-password ri-eye-line cursor-pointer position-absolute end-0 top-50 translate-middle-y me-16 text-secondary-light"
                :class="{'ri-eye-off-line': showPassword}"
                @click="togglePassword"
              ></span>
            </div>

            <!-- 記住我與忘記密碼 -->
            <div class="d-flex justify-content-between gap-2">
              <router-link to="/forgot-password" class="text-primary-600 fw-medium">忘記密碼？</router-link>
            </div>

            <!-- 登入按鈕 -->
            <button 
              type="submit" 
              class="btn btn-primary text-sm btn-sm px-12 py-16 w-100 radius-12 mt-32"
              :disabled="isLoading"
            >
              <!-- 載入中圖示 -->
              <span v-if="isLoading" class="spinner-border spinner-border-sm me-2" role="status">
                <span class="visually-hidden">載入中...</span>
              </span>
              {{ isLoading ? '登入中...' : '登入' }}
            </button>
          </form>
        </div>
      </div>
    </section>
</template>

<script>
import { ref, reactive, onMounted, computed } from 'vue';
import { useAuthStore } from '@/stores/authStore';
import { useRouter } from 'vue-router';

export default {
  name: 'SignIn',
  setup() {
    // ===== 依賴注入 =====
    const authStore = useAuthStore();
    const router = useRouter();
    
    // ===== 表單資料 =====
    
    // **重點：登入表單資料**
    const loginForm = reactive({
      email: '',
      password: '',
      remember: false
    });

    // **重點：表單驗證錯誤**
    const formErrors = reactive({
      email: '',
      password: ''
    });

    // ===== UI 狀態 =====
    
    // **重點：密碼顯示/隱藏狀態**
    const showPassword = ref(false);
    
    // **重點：本地處理狀態**
    const isProcessing = ref(false);

    // **重點：載入狀態計算屬性**
    const isLoading = computed(() => {
      return isProcessing.value;
    });

    // ===== 方法定義 =====

    /**
     * **重點：切換密碼顯示/隱藏**
     */
    const togglePassword = () => {
      showPassword.value = !showPassword.value;
    };

    /**
     * **重點：驗證表單資料**
     * @returns {boolean} 驗證結果
     */
    const validateForm = () => {
      // 重置錯誤訊息
      formErrors.email = '';
      formErrors.password = '';

      let isValid = true;

      // 驗證電子郵件
      if (!loginForm.email.trim()) {
        formErrors.email = '請輸入電子郵件';
        isValid = false;
      } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(loginForm.email.trim())) {
        formErrors.email = '請輸入有效的電子郵件格式';
        isValid = false;
      }

      // 驗證密碼
      if (!loginForm.password) {
        formErrors.password = '請輸入密碼';
        isValid = false;
      } else if (loginForm.password.length < 6) {
        formErrors.password = '密碼長度至少需要 6 個字元';
        isValid = false;
      }

      return isValid;
    };

    /**
     * **重點簡化：處理登入表單提交 - 移除錯誤狀態碼處理**
     */
    const handleLogin = async () => {
      // 驗證表單
      if (!validateForm()) {
        return;
      }

      // 設定載入狀態
      isProcessing.value = true;
      
      try {
        console.log('[SignIn] 開始登入流程，帳號:', loginForm.email);

        // **重點：使用 Auth Store 進行登入**
        await authStore.login(loginForm.email.trim(), loginForm.password);
        
        console.log('[SignIn] 登入成功');

        // **重點：處理記住我功能**
        if (loginForm.remember) {
          localStorage.setItem('rememberLogin', 'true');
          localStorage.setItem('lastLoginEmail', loginForm.email.trim());
        } else {
          localStorage.removeItem('rememberLogin');
          localStorage.removeItem('lastLoginEmail');
        }

        // **重點：登入成功後導向首頁**
        router.push('/');
        
      } catch (error) {
        // **重點簡化：移除複雜的錯誤狀態碼處理邏輯**
        // **所有錯誤處理都由 axiosInstance 攔截器統一處理**
        console.error('[SignIn] 登入失敗:', error.message);
        
        // **重點：只處理本地表單狀態重置（如果需要的話）**
        // 錯誤對話框和使用者提示都由攔截器處理
        
      } finally {
        // **重點：關閉載入狀態**
        isProcessing.value = false;
      }
    };

    /**
     * **重點：載入記住的登入資訊**
     */
    const loadRememberedLogin = () => {
      const rememberLogin = localStorage.getItem('rememberLogin');
      const lastLoginEmail = localStorage.getItem('lastLoginEmail');
      
      if (rememberLogin === 'true' && lastLoginEmail) {
        loginForm.email = lastLoginEmail;
        loginForm.remember = true;
        console.log('[SignIn] 載入記住的登入資訊:', lastLoginEmail);
      }
    };

    // ===== 生命週期 =====

    /**
     * **重點：元件掛載時執行**
     */
    onMounted(() => {
      // 載入記住的登入資訊
      loadRememberedLogin();
      
      // 檢查是否已經登入
      if (authStore.isAuthenticated) {
        console.log('[SignIn] 使用者已登入，導向首頁');
        router.push('/dashboard');
        return;
      }
      
      console.log('[SignIn] 元件已掛載');
    });

    // ===== 返回值 =====
    
    return {
      // 表單資料
      loginForm,
      formErrors,
      
      // UI 狀態
      showPassword,
      isLoading,
      
      // 方法
      togglePassword,
      handleLogin
    };
  }
};
</script>

<style scoped>
/* **重點：錯誤狀態樣式** */
.is-invalid {
  border-color: #dc3545 !important;
}

.invalid-feedback {
  display: block;
  width: 100%;
  margin-top: 0.25rem;
  font-size: 0.875rem;
  color: #dc3545;
}

/* **重點：載入中按鈕樣式** */
.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* **重點：改善響應式設計** */
@media (max-width: 768px) {
  .auth-right {
    padding: 1rem;
  }
  
  .max-w-464-px {
    max-width: 100%;
  }
}

/* **重點：密碼切換按鈕樣式** */
.toggle-password {
  transition: color 0.2s ease;
}

.toggle-password:hover {
  color: var(--primary-color) !important;
}
</style>
