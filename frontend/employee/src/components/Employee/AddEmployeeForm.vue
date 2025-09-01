<template>
  <div class="modal fade" id="addEmployeeModal" tabindex="-1" aria-labelledby="addEmployeeModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content">
        <!-- 表單標題 -->
        <div class="modal-header">
          <h5 class="modal-title" id="addEmployeeModalLabel">新增員工</h5>
          <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="關閉"></button>
        </div>
        
        <!-- 表單內容 -->
        <div class="modal-body">
          <form @submit.prevent="submitForm">
            <div class="row gy-3">
              <!-- 員工姓名輸入框 -->
              <div class="col-12">
                <label class="form-label">姓名</label>
                <div class="icon-field">
                  <span class="icon">
                    <iconify-icon icon="f7:person"></iconify-icon>
                  </span>
                  <input 
                    type="text" 
                    class="form-control" 
                    v-model="formData.empName"
                    :class="{ 'is-invalid': errors.empName }"
                    required
                  >
                  <div class="invalid-feedback" v-if="errors.empName">{{ errors.empName }}</div>
                </div>
              </div>
              
              <!-- 電子郵件輸入框 -->
              <div class="col-12">
                <label class="form-label">電子郵件</label>
                <div class="icon-field">
                  <span class="icon">
                    <iconify-icon icon="mage:email"></iconify-icon>
                  </span>
                  <input 
                    type="email" 
                    class="form-control" 
                    v-model="formData.email"
                    :class="{ 'is-invalid': errors.email }"
                    required
                  >
                  <div class="invalid-feedback" v-if="errors.email">{{ errors.email }}</div>
                </div>
              </div>
              
              <!-- 職位選擇下拉選單 - 修正選項顯示格式 -->
              <div class="col-12">
                <label class="form-label">職位</label>
                <div class="icon-field">
                  <span class="icon">
                    <iconify-icon icon="mdi:account-tie"></iconify-icon>
                  </span>
                  <select 
                    class="form-select" 
                    v-model="formData.role"
                    :class="{ 'is-invalid': errors.role }"
                    :disabled="isLoadingRoles"
                    required
                  >
                    <option value="" disabled>
                      {{ isLoadingRoles ? '載入中...' : '請選擇職位' }}
                    </option>
                    <!-- 使用動態載入的職位列表，顯示格式化後的標籤 -->
                    <option 
                      v-for="role in availableRoles" 
                      :key="role.id || role.value" 
                      :value="role.value"
                    >
                      {{ role.label }}
                    </option>
                  </select>
                  <div class="invalid-feedback" v-if="errors.role">{{ errors.role }}</div>
                </div>
              </div>
              
              <!-- 密碼輸入框 -->
              <div class="col-12">
                <label class="form-label">密碼</label>
                <div class="icon-field">
                  <span class="icon">
                    <iconify-icon icon="solar:lock-password-outline"></iconify-icon>
                  </span>
                  <input 
                    type="password" 
                    class="form-control" 
                    v-model="formData.password"
                    :class="{ 'is-invalid': errors.password }"
                    required
                  >
                  <div class="invalid-feedback" v-if="errors.password">{{ errors.password }}</div>
                </div>
              </div>

              <!-- 員工描述輸入框 - 新增選填欄位 -->
              <div class="col-12">
                <label class="form-label">員工描述 <small class="text-muted">(選填)</small></label>
                <div class="icon-field">
                  <span class="icon">
                    <iconify-icon icon="solar:document-text-outline"></iconify-icon>
                  </span>
                  <textarea 
                    class="form-control" 
                    v-model="formData.describe"
                    rows="3"
                  ></textarea>
                </div>
              </div>
            </div>
          </form>
        </div>
        
        <!-- 表單按鈕 -->
        <div class="modal-footer">
          <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">取消</button>
          <button 
            type="button" 
            class="btn btn-primary" 
            @click="submitForm" 
            :disabled="isSubmitting || isLoadingRoles"
          >
            <span v-if="isSubmitting" class="spinner-border spinner-border-sm me-1" role="status"></span>
            {{ isSubmitting ? '處理中...' : '新增員工' }}
          </button>
        </div>

        <!-- 錯誤訊息顯示 - 新增全域錯誤顯示 -->
        <div v-if="globalError" class="alert alert-danger mx-3 mb-3" role="alert">
          <iconify-icon icon="solar:danger-triangle-outline" class="me-2"></iconify-icon>
          {{ globalError }}
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue';
import { employeeService } from '@/services/employeeService';

export default {
  name: 'AddEmployeeForm',
  emits: ['submit', 'error'],
  setup(props, { emit }) {
    // 表單資料
    const formData = reactive({
      empName: '',
      email: '',
      role: '',
      password: '',
      describe: ''
    });
    
    // 表單錯誤訊息
    const errors = reactive({
      empName: '',
      email: '',
      role: '',
      password: ''
    });
    
    // 狀態管理
    const isSubmitting = ref(false);
    const isLoadingRoles = ref(false);
    const availableRoles = ref([]);
    const globalError = ref('');
    
    /**
     * 載入可用職位列表
     */
    const loadAvailableRoles = async () => {
      try {
        isLoadingRoles.value = true;
        globalError.value = '';
        
        console.log('[AddEmployeeForm] 開始載入職位列表...');
        
        // 從 employeeService 動態獲取職位列表
        const roles = await employeeService.getAllRoles();
        console.log('[AddEmployeeForm] API 回傳職位列表:', roles);
        
        // 確保職位列表格式正確
        if (Array.isArray(roles)) {
          // 轉換為選項格式，使用 role_name 欄位
          availableRoles.value = roles.map(role => ({
            value: role.role_name || role.roleName,
            label: (role.role_name || role.roleName || '').replace('ROLE_', ''),
            id: role.role_id || role.roleId
          }));
        } else {
          throw new Error('職位列表格式錯誤');
        }
        
        console.log('[AddEmployeeForm] 處理後的職位選項:', availableRoles.value);
      } catch (error) {
        console.error('[AddEmployeeForm] 載入職位列表失敗:', error);
        
        // 設定預設職位列表作為備用
        availableRoles.value = [
          { value: 'ROLE_ADMIN', label: 'ADMIN', id: 1 },
          { value: 'ROLE_MANAGER', label: 'MANAGER', id: 2 },
          { value: 'ROLE_EMPLOYEE', label: 'EMPLOYEE', id: 3 }
        ];
        
        globalError.value = '載入職位列表失敗，使用預設選項';
        emit('error', error.message || '載入職位列表時發生錯誤');
      } finally {
        isLoadingRoles.value = false;
      }
    };
    
    /**
     * 驗證表單資料
     * @returns {boolean} 驗證結果，true 為通過，false 為不通過
     */
    const validateForm = () => {
      // 重置所有錯誤訊息
      Object.keys(errors).forEach(key => errors[key] = '');
      globalError.value = '';
      
      let isValid = true;
      
      console.log('[AddEmployeeForm] 驗證表單資料:', formData);
      
      // 驗證員工姓名
      if (!formData.empName || !formData.empName.trim()) {
        errors.empName = '請輸入員工姓名';
        isValid = false;
        console.log('[AddEmployeeForm] 員工姓名驗證失敗');
      } else if (formData.empName.trim().length < 2) {
        errors.empName = '員工姓名至少需要 2 個字元';
        isValid = false;
      }
      
      // 驗證電子郵件
      if (!formData.email || !formData.email.trim()) {
        errors.email = '請輸入電子郵件';
        isValid = false;
        console.log('[AddEmployeeForm] 電子郵件驗證失敗');
      } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email.trim())) {
        errors.email = '請輸入有效的電子郵件地址';
        isValid = false;
      }
      
      // 驗證職位
      if (!formData.role) {
        errors.role = '請選擇職位角色';
        isValid = false;
        console.log('[AddEmployeeForm] 職位驗證失敗');
      }
      
      // 驗證密碼
      if (!formData.password || !formData.password.trim()) {
        errors.password = '請輸入密碼';
        isValid = false;
        console.log('[AddEmployeeForm] 密碼驗證失敗');
      } else if (formData.password.length < 6) {
        errors.password = '密碼長度至少為 6 個字元';
        isValid = false;
      }
      
      console.log('[AddEmployeeForm] 表單驗證結果:', isValid);
      return isValid;
    };
    
    /**
     * 提交表單
     */
    const submitForm = async () => {
      console.log('[AddEmployeeForm] 開始提交表單，當前資料:', formData);
      
      // 驗證表單
      if (!validateForm()) {
        console.log('[AddEmployeeForm] 表單驗證失敗，停止提交');
        return;
      }
      
      try {
        isSubmitting.value = true;
        globalError.value = '';
        
        // 建立要提交的資料結構 - 修正：確保所有欄位都有值
        const employeeData = {
          empName: formData.empName.trim(),
          email: formData.email.trim(),
          password: formData.password,  // 不要 trim 密碼，可能包含空格
          describe: formData.describe ? formData.describe.trim() : '',
          imagePath: ''
        };
        
        console.log('[AddEmployeeForm] 準備提交的員工資料:', employeeData);
        console.log('[AddEmployeeForm] 選擇的職位:', formData.role);
        
        // 發送完整的資料結構給父元件
        const submitData = {
          employeeData: employeeData,
          selectedRole: formData.role
        };
        
        console.log('[AddEmployeeForm] 最終提交資料:', submitData);
        
        // 提交給父元件處理
        emit('submit', submitData);
        
        // 成功後重置表單
        resetForm();
        
        // 關閉 Modal
        closeModal();
        
      } catch (error) {
        console.error('[AddEmployeeForm] 提交表單時出錯:', error);
        globalError.value = error.message || '新增員工時發生錯誤';
        emit('error', error.message || '提交表單時發生錯誤');
      } finally {
        isSubmitting.value = false;
      }
    };

    /**
     * 關閉 Modal
     */
    const closeModal = () => {
      const modal = document.getElementById('addEmployeeModal');
      if (modal) {
        const bootstrapModal = bootstrap.Modal.getInstance(modal);
        if (bootstrapModal) {
          bootstrapModal.hide();
        }
      }
      
      // 清理 Modal 相關的 DOM
      setTimeout(() => {
        const backdrop = document.querySelector('.modal-backdrop');
        if (backdrop) {
          backdrop.remove();
        }
        document.body.classList.remove('modal-open');
        document.body.style.overflow = '';
      }, 150);
    };
    
    /**
     * 重置表單
     */
    const resetForm = () => {
      console.log('[AddEmployeeForm] 重置表單');
      formData.empName = '';
      formData.email = '';
      formData.role = '';
      formData.password = '';
      formData.describe = '';
      
      // 清除錯誤訊息
      Object.keys(errors).forEach(key => errors[key] = '');
      globalError.value = '';
    };
    
    // 元件掛載時載入職位列表
    onMounted(() => {
      loadAvailableRoles();
    });
    
    return {
      // 資料
      formData,
      errors,
      isSubmitting,
      isLoadingRoles,
      availableRoles,
      globalError,
      
      // 方法
      submitForm,
      resetForm,
      loadAvailableRoles
    };
  }
}
</script>

<style scoped>
/* 表單樣式 */
.modal-body {
  padding: 1.5rem;
}

.modal-footer {
  border-top: 1px solid #dee2e6;
  padding: 1rem;
}

/* 圖示欄位樣式 */
.icon-field {
  position: relative;
}

.icon-field .icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  z-index: 5;
  color: #6c757d;
}

.icon-field .form-control,
.icon-field .form-select {
  padding-left: 2.5rem;
}

/* textarea 的 icon 位置調整 */
.icon-field textarea.form-control + .icon {
  top: 20px;
  transform: none;
}

/* 錯誤狀態樣式 */
.is-invalid {
  border-color: #dc3545;
}

.invalid-feedback {
  display: block;
  margin-top: 0.25rem;
  font-size: 0.875rem;
  color: #dc3545;
}

/* 載入狀態樣式 */
.form-select:disabled {
  background-color: #f8f9fa;
  cursor: not-allowed;
}

/* 全域錯誤訊息樣式 */
.alert {
  border-radius: 0.375rem;
  margin-bottom: 0;
}
</style>