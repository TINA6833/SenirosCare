<template>
  <section class="auth bg-base d-flex flex-wrap">
    <!-- 左側圖片區域 -->
    <div class="auth-left d-lg-block d-none">
      <div class="d-flex align-items-center flex-column h-100 justify-content-center">
        <img src="@/assets/images/auth/auth-img.png" alt="忘記密碼">
      </div>
    </div>

    <!-- 右側表單區域 -->
    <div class="auth-right py-32 px-24 d-flex flex-column justify-content-center">
      <div class="max-w-464-px mx-auto w-100">
        <!-- Logo 和標題 -->
        <div>
          <router-link to="/" class="mb-40 max-w-290-px">
            <img src="@/assets/images/logo.png" alt="Logo">
          </router-link>
          
          <!-- 動態標題 -->
          <h4 class="mb-12">
            {{ currentStep === 'email' ? '忘記密碼' : '重設密碼' }}
          </h4>
          
          <!-- 動態說明文字 -->
          <p class="mb-32 text-secondary-light text-lg">
            <span v-if="currentStep === 'email'">
              請輸入您的電子郵件，我們將發送驗證碼至您的信箱
            </span>
            <span v-else>
              請輸入您收到的 6 位數驗證碼和新密碼
            </span>
          </p>
        </div>

        <!-- 第一步：輸入電子郵件 -->
        <form v-if="currentStep === 'email'" @submit.prevent="handleSendCode">
          <!-- 錯誤訊息顯示 -->
          <div v-if="error" class="alert alert-danger mb-16" role="alert">
            <iconify-icon icon="solar:danger-triangle-outline" class="me-2"></iconify-icon>
            {{ error }}
          </div>

          <!-- 成功訊息顯示 -->
          <div v-if="successMessage" class="alert alert-success mb-16" role="alert">
            {{ successMessage }}
          </div>

          <!-- 電子郵件輸入框 -->
          <div class="icon-field mb-24">
            <span class="icon top-50 translate-middle-y">
              <iconify-icon icon="mage:email"></iconify-icon>
            </span>
            <input 
              type="email" 
              class="form-control h-56-px bg-neutral-50 radius-12" 
              placeholder="請輸入您的電子郵件" 
              v-model="emailForm.email"
              :class="{ 'is-invalid': emailForm.emailError }"
              :disabled="isLoading"
              required
            >
            <!-- 電子郵件錯誤提示 -->
            <div v-if="emailForm.emailError" class="invalid-feedback">
              {{ emailForm.emailError }}
            </div>
          </div>

          <!-- 發送驗證碼按鈕 -->
          <button 
            type="submit" 
            class="btn btn-primary text-sm btn-sm px-12 py-16 w-100 radius-12 mb-16"
            :disabled="isLoading"
          >
            <!-- 載入中圖示 -->
            <span v-if="isLoading" class="spinner-border spinner-border-sm me-2" role="status">
              <span class="visually-hidden">載入中...</span>
            </span>
            {{ isLoading ? '發送中...' : '發送驗證碼' }}
          </button>

          <!-- 返回登入連結 -->
          <div class="text-center">
            <router-link to="/sign-in" class="text-primary-600 fw-medium">
              返回登入
            </router-link>
          </div>
        </form>

        <!-- 第二步：驗證碼和新密碼 -->
        <form v-if="currentStep === 'reset'" @submit.prevent="handleResetPassword">
          <!-- 錯誤訊息顯示 -->
          <div v-if="error" class="alert alert-danger mb-16" role="alert">
            <iconify-icon icon="solar:danger-triangle-outline" class="me-2"></iconify-icon>
            {{ error }}
          </div>

          <!-- 顯示發送驗證碼的電子郵件 -->
          <div class="alert alert-info mb-16" role="alert">
            驗證碼已發送至：<strong>{{ emailForm.email }}</strong>
          </div>

          <!-- 驗證碼輸入框 -->
          <div class="icon-field mb-16">
            <span class="icon top-50 translate-middle-y">
              <iconify-icon icon="solar:shield-keyhole-outline"></iconify-icon>
            </span>
            <input 
              type="text" 
              class="form-control h-56-px bg-neutral-50 radius-12 text-center" 
              placeholder="請輸入 6 位數驗證碼" 
              v-model="resetForm.code"
              :class="{ 'is-invalid': resetForm.codeError }"
              :disabled="isLoading"
              maxlength="6"
              pattern="[0-9]{6}"
              required
            >
            <!-- 驗證碼錯誤提示 -->
            <div v-if="resetForm.codeError" class="invalid-feedback">
              {{ resetForm.codeError }}
            </div>
          </div>

          <!-- 新密碼輸入框 -->
          <div class="position-relative mb-16">
            <div class="icon-field">
              <span class="icon top-50 translate-middle-y">
                <iconify-icon icon="solar:lock-password-outline"></iconify-icon>
              </span>
              <input 
                :type="showNewPassword ? 'text' : 'password'" 
                class="form-control h-56-px bg-neutral-50 radius-12" 
                placeholder="請輸入新密碼" 
                v-model="resetForm.newPassword"
                :class="{ 'is-invalid': resetForm.passwordError }"
                :disabled="isLoading"
                required
              >
              <!-- 新密碼錯誤提示 -->
              <div v-if="resetForm.passwordError" class="invalid-feedback">
                {{ resetForm.passwordError }}
              </div>
            </div>
            <!-- 密碼顯示/隱藏切換按鈕 -->
            <span
              class="toggle-password ri-eye-line cursor-pointer position-absolute end-0 top-50 translate-middle-y me-16 text-secondary-light"
              :class="{'ri-eye-off-line': showNewPassword}"
              @click="toggleNewPassword"
            ></span>
          </div>

          <!-- 確認新密碼輸入框 -->
          <div class="position-relative mb-20">
            <div class="icon-field">
              <span class="icon top-50 translate-middle-y">
                <iconify-icon icon="solar:lock-password-outline"></iconify-icon>
              </span>
              <input 
                :type="showConfirmPassword ? 'text' : 'password'" 
                class="form-control h-56-px bg-neutral-50 radius-12" 
                placeholder="請再次輸入新密碼" 
                v-model="resetForm.confirmPassword"
                :class="{ 'is-invalid': resetForm.confirmPasswordError }"
                :disabled="isLoading"
                required
              >
              <!-- 確認密碼錯誤提示 -->
              <div v-if="resetForm.confirmPasswordError" class="invalid-feedback">
                {{ resetForm.confirmPasswordError }}
              </div>
            </div>
            <!-- 密碼顯示/隱藏切換按鈕 -->
            <span
              class="toggle-password ri-eye-line cursor-pointer position-absolute end-0 top-50 translate-middle-y me-16 text-secondary-light"
              :class="{'ri-eye-off-line': showConfirmPassword}"
              @click="toggleConfirmPassword"
            ></span>
          </div>

          <!-- 重設密碼按鈕 -->
          <button 
            type="submit" 
            class="btn btn-primary text-sm btn-sm px-12 py-16 w-100 radius-12 mb-16"
            :disabled="isLoading"
          >
            <!-- 載入中圖示 -->
            <span v-if="isLoading" class="spinner-border spinner-border-sm me-2" role="status">
              <span class="visually-hidden">載入中...</span>
            </span>
            {{ isLoading ? '重設中...' : '重設密碼' }}
          </button>

          <!-- 重新發送驗證碼和返回按鈕 -->
          <div class="d-flex justify-content-between">
            <button 
              type="button" 
              class="btn btn-outline-secondary text-sm btn-sm px-12 py-8"
              @click="goBackToEmail"
              :disabled="isLoading"
            >
              <iconify-icon icon="solar:arrow-left-outline" class="me-1"></iconify-icon>
              重新輸入信箱
            </button>
            
            <button 
              type="button" 
              class="btn btn-outline-primary text-sm btn-sm px-12 py-8"
              @click="resendCode"
              :disabled="isLoading || resendCooldown > 0"
            >
              <iconify-icon icon="solar:refresh-outline" class="me-1"></iconify-icon>
              {{ resendCooldown > 0 ? `重新發送 (${resendCooldown}s)` : '重新發送驗證碼' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { useEmployees } from '@/composables/useEmployees';
import { useToast } from '@/composables/useToast';

// ===== Composables =====
const router = useRouter();
const { requestPasswordReset, validateResetCode } = useEmployees({ autoLoad: false });
const { showToast } = useToast();

// ===== 狀態管理 =====

// 當前步驟：'email' | 'reset'
const currentStep = ref('email');

// 載入狀態
const isLoading = ref(false);

// 錯誤訊息
const error = ref('');

// 成功訊息
const successMessage = ref('');

// 密碼顯示狀態
const showNewPassword = ref(false);
const showConfirmPassword = ref(false);

// 重新發送驗證碼倒數計時
const resendCooldown = ref(0);
let resendTimer = null;

// 第一步表單資料
const emailForm = reactive({
  email: '',
  emailError: ''
});

// 第二步表單資料
const resetForm = reactive({
  code: '',
  newPassword: '',
  confirmPassword: '',
  codeError: '',
  passwordError: '',
  confirmPasswordError: ''
});

// ===== 方法定義 =====

/**
 * 切換新密碼顯示/隱藏
 */
const toggleNewPassword = () => {
  showNewPassword.value = !showNewPassword.value;
};

/**
 * 切換確認密碼顯示/隱藏
 */
const toggleConfirmPassword = () => {
  showConfirmPassword.value = !showConfirmPassword.value;
};

/**
 * 驗證電子郵件格式
 * @returns {boolean} 驗證結果
 */
const validateEmail = () => {
  emailForm.emailError = '';
  
  if (!emailForm.email.trim()) {
    emailForm.emailError = '請輸入電子郵件';
    return false;
  }
  
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(emailForm.email.trim())) {
    emailForm.emailError = '請輸入有效的電子郵件格式';
    return false;
  }
  
  return true;
};

/**
 * 驗證重設表單
 * @returns {boolean} 驗證結果
 */
const validateResetForm = () => {
  // 重置錯誤訊息
  resetForm.codeError = '';
  resetForm.passwordError = '';
  resetForm.confirmPasswordError = '';
  
  let isValid = true;
  
  // 驗證驗證碼
  if (!resetForm.code.trim()) {
    resetForm.codeError = '請輸入驗證碼';
    isValid = false;
  } else if (!/^\d{6}$/.test(resetForm.code.trim())) {
    resetForm.codeError = '請輸入 6 位數字驗證碼';
    isValid = false;
  }
  
  // 驗證新密碼
  if (!resetForm.newPassword) {
    resetForm.passwordError = '請輸入新密碼';
    isValid = false;
  } else if (resetForm.newPassword.length < 6) {
    resetForm.passwordError = '密碼長度至少需要 6 個字元';
    isValid = false;
  }
  
  // 驗證確認密碼
  if (!resetForm.confirmPassword) {
    resetForm.confirmPasswordError = '請再次輸入新密碼';
    isValid = false;
  } else if (resetForm.newPassword !== resetForm.confirmPassword) {
    resetForm.confirmPasswordError = '兩次輸入的密碼不一致';
    isValid = false;
  }
  
  return isValid;
};

/**
 * 處理發送驗證碼
 */
const handleSendCode = async () => {
  // 驗證電子郵件
  if (!validateEmail()) {
    return;
  }
  
  isLoading.value = true;
  error.value = '';
  successMessage.value = '';
  
  try {
    console.log('ForgotPassword: 發送驗證碼到信箱:', emailForm.email);
    
    // 調用 API 發送驗證碼
    await requestPasswordReset(emailForm.email.trim());
    
    // 發送成功，切換到重設步驟
    successMessage.value = `驗證碼已發送至 ${emailForm.email}，請檢查您的信箱`;
    
    // 延遲切換到下一步，讓使用者看到成功訊息
    setTimeout(() => {
      currentStep.value = 'reset';
      successMessage.value = '';
    }, 2000);
    
    console.log('ForgotPassword: 驗證碼發送成功');
    
  } catch (err) {
    console.error('ForgotPassword: 發送驗證碼失敗', err);
    error.value = err.message || '發送驗證碼失敗，請稍後再試';
  } finally {
    isLoading.value = false;
  }
};

/**
 * 處理重設密碼
 */
const handleResetPassword = async () => {
  // 驗證表單
  if (!validateResetForm()) {
    return;
  }
  
  isLoading.value = true;
  error.value = '';
  
  try {
    console.log('ForgotPassword: 驗證碼重設密碼，信箱:', emailForm.email);
    
    // 準備重設資料
    const resetData = {
      code: resetForm.code.trim(),
      newPassword: resetForm.newPassword
    };
    
    // 調用 API 驗證並重設密碼
    await validateResetCode(emailForm.email.trim(), resetData);
    
    console.log('ForgotPassword: 密碼重設成功');
    
    // 顯示成功訊息
    showToast({
      title: '密碼重設成功',
      message: '您的密碼已成功重設，請使用新密碼登入',
      type: 'success',
      duration: 4000
    });
    
    // 延遲導向登入頁面
    setTimeout(() => {
      router.push('/sign-in');
    }, 2000);
    
  } catch (err) {
    console.error('ForgotPassword: 密碼重設失敗', err);
    error.value = err.message || '密碼重設失敗，請檢查驗證碼是否正確';
  } finally {
    isLoading.value = false;
  }
};

/**
 * 返回到輸入電子郵件步驟
 */
const goBackToEmail = () => {
  currentStep.value = 'email';
  error.value = '';
  
  // 清空重設表單
  resetForm.code = '';
  resetForm.newPassword = '';
  resetForm.confirmPassword = '';
  resetForm.codeError = '';
  resetForm.passwordError = '';
  resetForm.confirmPasswordError = '';
};

/**
 * 重新發送驗證碼
 */
const resendCode = async () => {
  if (resendCooldown.value > 0) {
    return;
  }
  
  try {
    await handleSendCode();
    
    // 設定 60 秒倒數計時
    startResendCooldown();
    
  } catch (err) {
    console.error('ForgotPassword: 重新發送驗證碼失敗', err);
  }
};

/**
 * 開始重新發送倒數計時
 */
const startResendCooldown = () => {
  resendCooldown.value = 60;
  
  resendTimer = setInterval(() => {
    resendCooldown.value--;
    
    if (resendCooldown.value <= 0) {
      clearInterval(resendTimer);
      resendTimer = null;
    }
  }, 1000);
};

/**
 * 清理定時器
 */
const cleanup = () => {
  if (resendTimer) {
    clearInterval(resendTimer);
    resendTimer = null;
  }
};

// ===== 生命週期 =====

onMounted(() => {
  console.log('ForgotPassword: 元件已掛載');
});

onUnmounted(() => {
  cleanup();
});
</script>

<style scoped>
/* 錯誤狀態樣式 */
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

/* 載入中按鈕樣式 */
.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 密碼切換按鈕樣式 */
.toggle-password {
  transition: color 0.2s ease;
}

.toggle-password:hover {
  color: var(--primary-color) !important;
}

/* 驗證碼輸入框樣式 */
input[pattern="[0-9]{6}"] {
  letter-spacing: 0.3em;
  font-weight: 600;
}

/* 改善響應式設計 */
@media (max-width: 768px) {
  .auth-right {
    padding: 1rem;
  }
  
  .max-w-464-px {
    max-width: 100%;
  }
  
  .d-flex.justify-content-between {
    flex-direction: column;
    gap: 0.5rem;
  }
  
  .d-flex.justify-content-between .btn {
    width: 100%;
  }
}

/* Alert 動畫效果 */
.alert {
  animation: fadeIn 0.3s ease-in-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
