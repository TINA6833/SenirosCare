<template>
  <div class="row gy-4">
    <div class="col-lg-4">
      <div class="user-grid-card position-relative border radius-16 overflow-hidden bg-base h-100">
        <!-- 示意背景照片 -->
        <img src="@/assets/images/user-grid/user-background.png" alt="" class="w-100 object-fit-cover" />
        <div class="pb-24 ms-16 mb-24 me-16 mt--100">
          <div class="text-center border border-top-0 border-start-0 border-end-0">
            <!-- **重點修正：使用父元件傳入的圖片處理方法** -->
            <img :src="profileImageUrl" alt=""
              class="border br-white border-width-2-px w-200-px h-200-px rounded-circle object-fit-cover" 
              @error="handleImageError" />
            <!-- 顯示員工姓名 -->
            <h6 class="mb-0 mt-16">{{ employeeName }}</h6>
            <!-- 顯示員工電子郵件 -->
            <span class="text-secondary-light mb-16">{{ employeeEmail }}</span>
          </div>

          <!-- 個人資訊區塊 -->
          <div class="mt-24">
            <h6 class="text-xl mb-16">個人資訊</h6>
            <ul class="list-unstyled">
              <!-- 姓名 -->
              <li class="profile-info-item mb-12">
                <span class="profile-label">姓名</span>
                <span class="profile-value">: {{ employeeName }}</span>
              </li>

              <!-- 電子郵件 -->
              <li class="profile-info-item mb-12">
                <span class="profile-label">電子郵件</span>
                <span class="profile-value">: {{ employeeEmail }}</span>
              </li>

              <!-- 職位 - 修正顯示方式 -->
              <li class="profile-info-item mb-12">
                <span class="profile-label">職位</span>
                <div class="profile-roles">
                  <!-- 有職位：使用與 EmployeeTable 相同的標籤樣式 -->
                  <template v-if="employeeRoles && employeeRoles.length > 0">
                    <span v-for="role in employeeRoles" :key="role.roleId || role.role_id"
                      :class="getRoleClass(role.role_name)" class="role-badge">
                      {{ formatRoleLabel(role.role_name) }}
                    </span>
                  </template>
                  <!-- 無職位 -->
                  <span v-else class="role-badge bg-secondary text-white">
                    無職位
                  </span>
                </div>
              </li>

              <!-- 入職時間 -->
              <li class="profile-info-item mb-12">
                <span class="profile-label">入職時間</span>
                <span class="profile-value">: {{ employeeCreatedAt }}</span>
              </li>

              <!-- 描述 -->
              <li class="profile-info-item">
                <span class="profile-label">描述</span>
                <div class="profile-description">
                  : {{ employeeDescribe }}
                </div>
              </li>
            </ul>
          </div>
        </div>
      </div>
    </div>

    <!-- Right Panel -->
    <div class="col-lg-8">
      <div class="card h-100">
        <div class="card-body p-24">
          <!-- Tabs -->
          <ul class="nav border-gradient-tab nav-pills mb-20 d-inline-flex" role="tablist">
            <li class="nav-item" role="presentation">
              <button class="nav-link d-flex align-items-center px-24" :class="{ active: activeTab === 'edit-profile' }"
                @click="activeTab = 'edit-profile'">
                編輯個人資料
              </button>
            </li>
            <li class="nav-item" role="presentation">
              <button class="nav-link d-flex align-items-center px-24"
                :class="{ active: activeTab === 'change-password' }" @click="activeTab = 'change-password'">
                修改密碼
              </button>
            </li>
          </ul>

          <!-- Tabs Content -->
          <div class="tab-content">
            <!-- Edit Profile Tab -->
            <div v-if="activeTab === 'edit-profile'">
              <h6 class="text-md text-primary-light mb-16">個人頭像</h6>
              <div class="mb-24 mt-16">
                <div class="avatar-upload">
                  <div class="avatar-edit position-absolute bottom-0 end-0 me-24 mt-16 z-1 cursor-pointer">
                    <input type="file" id="imageUpload" accept=".png, .jpg, .jpeg" @change="onImageChange" hidden />
                    <label for="imageUpload"
                      class="w-32-px h-32-px d-flex justify-content-center align-items-center bg-primary-50 text-primary-600 border border-primary-600 bg-hover-primary-100 text-lg rounded-circle">
                      <iconify-icon icon="solar:camera-outline" class="icon"></iconify-icon>
                    </label>
                  </div>
                  <div class="avatar-preview">
                    <!-- **重點修正：使用父元件的圖片處理方法** -->
                    <div id="imagePreview" :style="{ backgroundImage: 'url(' + currentImagePreview + ')' }" 
                         @error="handleImageError"></div>
                  </div>
                </div>
              </div>

              <!-- Profile Form -->
              <form @submit.prevent="handleUpdateProfile">
                <div class="row">
                  <!-- Full Name -->
                  <div class="col-sm-6 mb-20">
                    <label class="form-label fw-semibold text-primary-light text-sm mb-8">姓名 <span
                        class="text-danger-600">*</span></label>
                    <input type="text" class="form-control radius-8" placeholder="請輸入姓名" v-model="formData.empName"
                      required />
                  </div>
                  <!-- Email -->
                  <div class="col-sm-6 mb-20">
                    <label class="form-label fw-semibold text-primary-light text-sm mb-8">電子郵件 <span
                        class="text-danger-600">*</span></label>
                    <input type="email" class="form-control radius-8" placeholder="請輸入電子郵件" v-model="formData.email"
                      required />
                  </div>
                  <!-- Description -->
                  <div class="col-sm-12 mb-20">
                    <label class="form-label fw-semibold text-primary-light text-sm mb-8">個人描述</label>
                    <textarea class="form-control radius-8" placeholder="請輸入個人描述..." v-model="formData.describe"
                      rows="4"></textarea>
                  </div>
                </div>

                <div class="d-flex align-items-center justify-content-center gap-3">
                    <button type="button"
                    class="border border-success-600 bg-success text-white text-md px-56 py-11 radius-8"
                    @click="resetForm">
                    復原
                    </button>
                  <button type="submit" class="btn btn-primary border border-primary-600 text-md px-56 py-12 radius-8"
                    :disabled="isUpdating">
                    {{ isUpdating ? '更新中...' : '儲存' }}
                  </button>
                </div>
              </form>
            </div>

            <!-- Change Password Tab -->
            <div v-if="activeTab === 'change-password'">
              <form @submit.prevent="handleChangePassword">
                <div class="mb-20">
                  <label class="form-label fw-semibold text-primary-light text-sm mb-8">新密碼 <span
                      class="text-danger-600">*</span></label>
                  <div class="position-relative">
                    <input :type="passwordVisible ? 'text' : 'password'" class="form-control radius-8"
                      placeholder="請輸入新密碼*" v-model="passwordData.newPassword" required />
                    <span @click="togglePassword"
                      class="ri-eye-line cursor-pointer position-absolute end-0 top-50 translate-middle-y me-16 text-secondary-light"></span>
                  </div>
                </div>
                <div class="mb-20">
                  <label class="form-label fw-semibold text-primary-light text-sm mb-8">確認密碼 <span
                      class="text-danger-600">*</span></label>
                  <div class="position-relative">
                    <input :type="confirmPasswordVisible ? 'text' : 'password'" class="form-control radius-8"
                      placeholder="請再次輸入新密碼*" v-model="passwordData.confirmPassword" required />
                    <span @click="toggleConfirmPassword"
                      class="ri-eye-line cursor-pointer position-absolute end-0 top-50 translate-middle-y me-16 text-secondary-light"></span>
                  </div>
                </div>

                <div class="d-flex align-items-center justify-content-center gap-3">
                  <button type="button"
                    class="border border-danger-600 bg-hover-danger-200 text-danger-600 text-md px-56 py-11 radius-8"
                    @click="resetPasswordForm">
                    取消
                  </button>
                  <button type="submit" class="btn btn-primary border border-primary-600 text-md px-56 py-12 radius-8"
                    :disabled="isChangingPassword">
                    {{ isChangingPassword ? '更新中...' : '更新密碼' }}
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { computed, reactive, ref, onMounted } from 'vue';
import { useAuth } from '@/composables/useAuth';
import { useEmployees } from '@/composables/useEmployees';
import { useToast } from '@/composables/useToast';
import defaultUserImage from "@/assets/images/user-grid/user-grid-img13.png";

export default {
  name: 'ViewProfile',
  props: {
    // **重點：接收父元件傳入的圖片處理方法**
    getEmployeeImage: {
      type: Function,
      required: true
    }
  },
  setup(props) {
    // 使用認證相關的 composable
    const { currentEmployee, employeeName, employeeEmail, employeeRoles } = useAuth();

    // 使用員工管理的 composable 來取得更新方法
    const { updateEmployee, getEmployeeDetail, resetPassword } = useEmployees({ autoLoad: false });
    const { showToast } = useToast();

    // 響應式狀態
    const activeTab = ref('edit-profile');
    const profileImage = ref(defaultUserImage);
    const passwordVisible = ref(false);
    const confirmPasswordVisible = ref(false);
    const isUpdating = ref(false);
    const isChangingPassword = ref(false);

    // 表單資料
    const formData = reactive({
      empName: '',
      email: '',
      describe: '',
      imagePath: ''
    });

    // 密碼變更表單資料
    const passwordData = reactive({
      newPassword: '',
      confirmPassword: ''
    });

    /**
     * **重點修正：計算屬性 - 個人頭像 URL**
     * 使用父元件傳入的圖片處理方法
     */
    const profileImageUrl = computed(() => {
      console.log('[ViewProfile.vue] 檢查員工資料:', {
        currentEmployee: currentEmployee.value,
        imagePath: currentEmployee.value?.imagePath
      });
      
      // **重點修正：使用 imagePath 而非 originalImagePath**
      if (currentEmployee.value?.imagePath && 
          currentEmployee.value.imagePath !== '無圖片' && 
          currentEmployee.value.imagePath.trim() !== '') {
        
        const processedUrl = props.getEmployeeImage(currentEmployee.value.imagePath);
        console.log('[ViewProfile.vue] 處理後的圖片網址:', processedUrl);
        return processedUrl;
      }
      
      console.log('[ViewProfile.vue] 使用預設圖片');
      return props.getEmployeeImage('');
    });

    /**
     * **重點修正：計算屬性 - 編輯表單中的圖片預覽**
     */
    const currentImagePreview = computed(() => {
      // 如果使用者選擇了新圖片（base64），優先顯示
      if (profileImage.value && profileImage.value !== defaultUserImage) {
        return profileImage.value;
      }
      
      // 否則使用當前員工的圖片
      return profileImageUrl.value;
    });

    // 計算屬性 - 員工狀態顯示
    const employeeStatus = computed(() => {
      if (!currentEmployee.value) return '未知';
      return currentEmployee.value.isActive === 'active' ? '啟用' : '停用';
    });

    // 計算屬性 - 建立時間
    const employeeCreatedAt = computed(() => {
      return currentEmployee.value?.createdAt || '未知';
    });

    // 計算屬性 - 個人描述
    const employeeDescribe = computed(() => {
      return currentEmployee.value?.describe || '無描述';
    });

    /**
     * **重點修正：處理圖片載入錯誤**
     * 使用父元件的預設圖片處理
     */
    const handleImageError = (event) => {
      console.error('[ViewProfile.vue] 圖片載入失敗，使用預設圖片');
      event.target.src = props.getEmployeeImage('');
    };

    /**
     * 根據職位類型返回對應的樣式類別（與 EmployeeTable 一致）
     */
    const getRoleClass = (role) => {
      switch (role) {
        case 'ROLE_ADMIN':
          return 'bg-danger text-white';        // 管理員 - 紅色
        case 'ROLE_MANAGER':
          return 'bg-warning text-white';       // 經理 - 黃色
        case 'ROLE_EMPLOYEE':
          return 'bg-success text-white';       // 員工 - 綠色
        default:
          return 'bg-secondary text-white';     // 未知職位 - 灰色
      }
    };

    /**
     * 格式化職位標籤顯示 - 去除 ROLE_ 前綴（與 EmployeeTable 一致）
     */
    const formatRoleLabel = (role) => {
      if (!role) return '';
      return role.startsWith('ROLE_') ? role.replace('ROLE_', '') : role;
    };

    /**
     * **重點修正：初始化表單資料**
     */
    const initializeFormData = () => {
      if (currentEmployee.value) {
        formData.empName = currentEmployee.value.empName || '';
        formData.email = currentEmployee.value.email || '';
        formData.describe = currentEmployee.value.describe || '';
        // **重點修正：使用 imagePath 而非 originalImagePath**
        formData.imagePath = currentEmployee.value.imagePath || '';
        
        console.log('[ViewProfile.vue] 初始化表單資料:', {
          imagePath: formData.imagePath,
          fullImageUrl: profileImageUrl.value
        });
      }
    };

    /**
     * 重新載入個人資料並更新 Auth Store
     */
    const refreshProfile = async () => {
      try {
        // 從後端重新獲取最新的個人資料
        const updatedEmployee = await getEmployeeDetail(currentEmployee.value.empId);

        // 動態引入 Auth Store 並更新資料
        const { useAuthStore } = await import('@/stores/authStore');
        const authStore = useAuthStore();
        authStore.updateEmployee(updatedEmployee);

        // 重新初始化表單資料
        initializeFormData();

        console.log('[ViewProfile.vue] 個人資料已重新載入並更新 Auth Store');
      } catch (error) {
        console.error('[ViewProfile.vue] 重新載入個人資料失敗:', error);
      }
    };

    /**
     * 重置表單
     */
    const resetForm = () => {
      initializeFormData();
      profileImage.value = defaultUserImage; // **重點：重置預覽圖片**
      activeTab.value = 'edit-profile';
    };

    /**
     * 重置密碼表單
     */
    const resetPasswordForm = () => {
      passwordData.newPassword = '';
      passwordData.confirmPassword = '';
      activeTab.value = 'edit-profile';
    };

    /**
     * **重點修正：處理圖片上傳**
     */
    const onImageChange = (event) => {
      const file = event.target.files[0];
      if (file) {
        // 驗證檔案大小（例如：限制 5MB）
        const maxSize = 5 * 1024 * 1024; // 5MB
        if (file.size > maxSize) {
          showToast({
            title: '檔案過大',
            message: '圖片檔案大小不能超過 5MB',
            type: 'warning'
          });
          return;
        }

        // 驗證檔案類型
        const allowedTypes = ['image/png', 'image/jpg', 'image/jpeg'];
        if (!allowedTypes.includes(file.type)) {
          showToast({
            title: '檔案格式錯誤',
            message: '只支援 PNG、JPG、JPEG 格式的圖片',
            type: 'warning'
          });
          return;
        }

        const reader = new FileReader();
        reader.onload = e => {
          profileImage.value = e.target.result;
          formData.imagePath = e.target.result; // **重點：暫時儲存 base64**
          
          console.log('[ViewProfile.vue] 圖片已選擇，準備預覽');
        };
        reader.readAsDataURL(file);
      }
    };

    /**
     * 切換密碼顯示狀態
     */
    const togglePassword = () => {
      passwordVisible.value = !passwordVisible.value;
    };

    /**
     * 切換確認密碼顯示狀態
     */
    const toggleConfirmPassword = () => {
      confirmPasswordVisible.value = !confirmPasswordVisible.value;
    };

    /**
     * 處理個人資料更新
     */
    const handleUpdateProfile = async () => {
      try {
        isUpdating.value = true;

        // 驗證必填欄位
        if (!formData.empName.trim() || !formData.email.trim()) {
          throw new Error('姓名和電子郵件為必填欄位');
        }

        // 準備更新資料
        const updateData = {
          empName: formData.empName,
          email: formData.email,
          describe: formData.describe,
          imagePath: formData.imagePath,
          password: 'KEEP_CURRENT_PASSWORD' // 特殊標記，保持現有密碼
        };

        console.log('[ViewProfile.vue] 準備更新個人資料:', updateData);

        // 呼叫 updateEmployee 更新資料
        const updatedEmployee = await updateEmployee(currentEmployee.value.empId, updateData);

        // 動態引入 Auth Store 並更新資料
        const { useAuthStore } = await import('@/stores/authStore');
        const authStore = useAuthStore();
        authStore.updateEmployee(updatedEmployee);

        // 重新初始化表單資料以反映最新狀態
        initializeFormData();

        showToast({
          title: '更新成功',
          message: `已成功更新員工 ${currentEmployee.value?.empName} 的資料`,
          type: 'success'
        });

        // 可選：重新載入以確保資料一致性
        await refreshProfile();

      } catch (error) {
        console.error('[ViewProfile.vue] 更新個人資料失敗:', error);
        showToast({
          title: '更新失敗',
          message: `更新員工 ${currentEmployee.value?.empName} 的資料時發生錯誤：${error.message}`,
          type: 'error'
        });
      } finally {
        isUpdating.value = false;
      }
    };

    /**
     * 處理密碼變更 - 修正：使用新的直接修改密碼 API
     */
    const handleChangePassword = async () => {
      try {
        isChangingPassword.value = true;

        // 驗證密碼
        if (!passwordData.newPassword || !passwordData.confirmPassword) {
          throw new Error('請填寫所有密碼欄位');
        }

        if (passwordData.newPassword !== passwordData.confirmPassword) {
          throw new Error('新密碼與確認密碼不符');
        }

        if (passwordData.newPassword.length < 6) {
          throw new Error('密碼長度至少需要 6 個字元');
        }

        console.log('[ViewProfile.vue] 準備修改密碼，員工 ID:', currentEmployee.value.empId);

        // 呼叫新的直接修改密碼 API
        await resetPassword(currentEmployee.value.empId, passwordData.newPassword);

        // 顯示成功訊息
        showToast({
          title: '密碼更新成功',
          message: `已成功更新 ${currentEmployee.value?.empName} 的密碼`,
          type: 'success'
        });

        // 重置密碼表單
        resetPasswordForm();

      } catch (error) {
        console.error('[ViewProfile.vue] 更新密碼失敗:', error);
        
        // 顯示錯誤訊息
        showToast({
          title: '密碼更新失敗',
          message: `更新 ${currentEmployee.value?.empName} 的密碼時發生錯誤：${error.message}`,
          type: 'error'
        });
      } finally {
        isChangingPassword.value = false;
      }
    };

    // 元件掛載時初始化資料
    onMounted(() => {
      console.log('[ViewProfile.vue] 元件掛載時的員工資料:', {
        currentEmployee: currentEmployee.value,
        availableFields: currentEmployee.value ? Object.keys(currentEmployee.value) : 'null',
        imagePath: currentEmployee.value?.imagePath
      });
      
      initializeFormData();
    });

    return {
      // 狀態
      activeTab,
      profileImage,
      passwordVisible,
      confirmPasswordVisible,
      isUpdating,
      isChangingPassword,

      // 表單資料
      formData,
      passwordData,

      // 計算屬性
      employeeName,
      employeeEmail,
      employeeRoles,
      employeeStatus,
      employeeCreatedAt,
      employeeDescribe,
      profileImageUrl,
      currentImagePreview, // **重點：預覽圖片計算屬性**

      // 方法
      onImageChange,
      togglePassword,
      toggleConfirmPassword,
      resetForm,
      resetPasswordForm,
      handleUpdateProfile,
      handleChangePassword,
      getRoleClass,
      formatRoleLabel,
      refreshProfile,
      handleImageError, // **重點：圖片錯誤處理**
    };
  }
};
</script>

<style scoped>
/* 個人資訊項目佈局 */
.profile-info-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  min-height: 32px;
}

/* 標籤樣式 */
.profile-label {
  font-size: 14px;
  font-weight: 600;
  color: var(--bs-primary);
  min-width: 80px;
  flex-shrink: 0;
}

/* 值樣式 */
.profile-value {
  font-size: 14px;
  font-weight: 500;
  color: var(--bs-secondary);
  flex: 1;
  word-break: break-word;
}

/* 職位標籤容器 */
.profile-roles {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  align-items: center;
}

/* 職位標籤樣式 */
.role-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 50rem;
  font-size: 12px;
  font-weight: 500;
  line-height: 1.2;
  white-space: nowrap;
}

/* 描述區域 */
.profile-description {
  font-size: 14px;
  font-weight: 500;
  color: var(--bs-secondary);
  word-break: break-word;
  line-height: 1.5;
  flex: 1;
}

/* 頭像預覽 */
.avatar-upload {
  position: relative;
  display: inline-block;
}

.avatar-preview {
  width: 100px;
  height: 100px;
  border-radius: 8px;
  overflow: hidden;
}

#imagePreview {
  width: 100%;
  height: 100%;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
}

/* 響應式調整 */
@media (max-width: 768px) {
  .profile-info-item {
    flex-direction: column;
    gap: 4px;
  }

  .profile-label {
    min-width: auto;
  }

  .profile-value,
  .profile-description {
    margin-left: 0;
  }
}
</style>
