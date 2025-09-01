<template>
  <!-- 遮罩層，點擊時關閉詳情視窗 -->
  <div v-if="visible" class="modal-overlay" @click.self="close">
    <!-- 詳情卡片 -->
    <div class="card employee-detail-card">
      <!-- 卡片標頭與關閉按鈕 -->
      <div class="card-header d-flex justify-content-between align-items-center">
        <h5 class="card-title mb-0">員工詳細資訊</h5>
        <button type="button" class="btn-close" @click="close"></button>
      </div>

      <!-- 卡片內容 - 載入中狀態 -->
      <div v-if="loading" class="card-body text-center p-5">
        <div class="spinner-border" role="status">
          <span class="visually-hidden">載入中...</span>
        </div>
        <p class="mt-3">載入員工資料中...</p>
      </div>
      
      <!-- 卡片內容 - 錯誤訊息 -->
      <div v-else-if="error" class="card-body">
        <div class="alert alert-danger">
          <iconify-icon icon="solar:danger-triangle-outline" class="me-2"></iconify-icon>
          <h5>載入錯誤</h5>
          <p>{{ error }}</p>
          <button type="button" class="btn btn-outline-danger btn-sm mt-2" @click="retryLoad">
            <iconify-icon icon="solar:refresh-outline" class="me-1"></iconify-icon>
            重新載入
          </button>
        </div>
      </div>

      <!-- 卡片內容 - 員工資料 -->
      <div v-else-if="employee" class="card-body">
        <!-- 個人資訊區域 -->
        <div class="employee-profile">
          <!-- 頭像和基本資訊 -->
          <div class="text-center mb-4">
            <div class="profile-image mb-3">
              <!-- 使用 imagePath 而非 avatar -->
              <img 
                :src="getEmployeeImage(employee.imagePath)" 
                alt="員工頭像" 
                class="rounded-circle" 
                width="100" 
                height="100"
                @error="handleImageError"
              >
            </div>
            <!-- 使用 empName 而非 name -->
            <h4 class="fw-bold mb-1">{{ employee.empName }}</h4>
            <p class="text-muted">{{ employee.email }}</p>
            
            <!-- 職位徽章 - 顯示所有職位 -->
            <div class="roles-container">
              <!-- 註解：修正 v-for 迴圈，使用 role.role_name -->
              <span 
                v-for="role in employee.roles" 
                :key="role.role_id"
                :class="getRoleClass(role.role_name)" 
                class="px-3 py-1 rounded-pill fw-medium me-2 mb-2 d-inline-block"
              >
                <!-- 註解：使用 formatRoleLabel 格式化顯示文字 -->
                {{ formatRoleLabel(role.role_name) }}
              </span>
              <!-- 如果沒有職位，顯示預設訊息 -->
              <span 
                v-if="!employee.roles || employee.roles.length === 0"
                class="px-3 py-1 rounded-pill fw-medium bg-secondary text-white"
              >
                無職位
              </span>
            </div>
          </div>

          <!-- 詳細資訊區塊 -->
          <div class="personal-info mt-4">
            <h5 class="border-bottom pb-2 mb-3">
              <iconify-icon icon="solar:user-outline" class="me-2"></iconify-icon>
              個人資訊
            </h5>
            
            <!-- 員工 ID -->
            <div class="info-item d-flex mb-3">
              <div class="info-label fw-bold" style="width: 120px;">
                <iconify-icon icon="solar:hashtag-outline" class="me-1"></iconify-icon>
                員工 ID：
              </div>
              <div class="info-value">{{ employee.empId }}</div>
            </div>
            
            <!-- 員工姓名 -->
            <div class="info-item d-flex mb-3">
              <div class="info-label fw-bold" style="width: 120px;">
                <iconify-icon icon="f7:person" class="me-1"></iconify-icon>
                姓名：
              </div>
              <div class="info-value">{{ employee.empName }}</div>
            </div>
            
            <!-- Email -->
            <div class="info-item d-flex mb-3">
              <div class="info-label fw-bold" style="width: 120px;">
                <iconify-icon icon="mage:email" class="me-1"></iconify-icon>
                Email：
              </div>
              <div class="info-value">{{ employee.email || 'N/A' }}</div>
            </div>

            
            <!-- 到職日 - 使用 createdAt -->
            <div class="info-item d-flex mb-3">
              <div class="info-label fw-bold" style="width: 120px;">
                <iconify-icon icon="solar:calendar-outline" class="me-1"></iconify-icon>
                到職日：
              </div>
              <div class="info-value">{{ formatDate(employee.createdAt) }}</div>
            </div>
            
            <!-- 最後更新日期 -->
            <div class="info-item d-flex mb-3">
              <div class="info-label fw-bold" style="width: 120px;">
                <iconify-icon icon="solar:clock-circle-outline" class="me-1"></iconify-icon>
                更新日期：
              </div>
              <div class="info-value">{{ formatDate(employee.updatedAt) }}</div>
            </div>
            
            <!-- 狀態 - 使用 isActive -->
            <div class="info-item d-flex mb-3">
              <div class="info-label fw-bold" style="width: 120px;">
                <iconify-icon icon="solar:check-circle-outline" class="me-1"></iconify-icon>
                狀態：
              </div>
              <div class="info-value">
                <span :class="getStatusClass(employee.isActive)" class="px-2 py-1 rounded-pill fw-medium">
                  <!-- 註解：移除此處的 iconify-icon 元素，以解決對齊問題 -->
                  {{ employee.isActive === 'active' ? '啟用' : '停用' }}
                </span>
              </div>
            </div>
          </div>

          <!-- 個人簡介區塊 - 使用 describe 欄位 -->
          <!-- 註解：在 class 中增加 mt-5 來增加與上方區塊的間距 -->
          <div class="additional-info mt-5" v-if="employee.describe && employee.describe !== '無描述'">
            <h5 class="border-bottom pb-2 mb-3">
              <iconify-icon icon="solar:document-text-outline" class="me-2"></iconify-icon>
              個人簡介
            </h5>
            <div class="bg-light p-3 rounded">
              <p class="mb-0">{{ employee.describe }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, watch } from 'vue';
import { useEmployees } from '@/composables/useEmployees';
// 正確引入圖片資源
import defaultAvatarImage from '@/assets/images/user-list/user-list1.png';

export default {
  name: 'EmployeeDetail',
  
  props: {
    // 控制元件顯示/隱藏
    visible: {
      type: Boolean,
      default: false
    },
    // 要顯示的員工 ID
    employeeId: {
      type: [Number, String],
      default: null
    }
  },
  
  emits: ['close', 'edit'],
  
  setup(props, { emit }) {
    // 引入員工資料 composable
    const { getEmployeeDetail } = useEmployees();
    
    // 本地狀態
    const loading = ref(false);
    const error = ref(null);
    const employee = ref(null);
    // 修正：使用正確引入的圖片
    const defaultAvatar = defaultAvatarImage;
    
    /**
     * 載入員工資料
     */
    const loadEmployeeData = async () => {
      if (!props.employeeId) return;
      
      loading.value = true;
      error.value = null;
      
      try {
        // 使用 composable 獲取員工詳細資料
        const result = await getEmployeeDetail(props.employeeId);
        employee.value = result;
        console.log('載入的員工資料:', result); // 調試用
      } catch (err) {
        console.error('載入員工資料失敗:', err);
        error.value = err.message || '無法載入員工資料，請稍後再試。';
      } finally {
        loading.value = false;
      }
    };
    
    /**
     * 重新載入資料
     */
    const retryLoad = () => {
      loadEmployeeData();
    };
    
    /**
     * 取得員工圖片 URL
     * @param {string} imagePath - 圖片路徑
     * @returns {string} 完整的圖片 URL
     */
    const getEmployeeImage = (imagePath) => {
      // 如果有圖片路徑且不是預設值，則使用該路徑
      if (imagePath && imagePath !== '無圖片' && imagePath.trim() !== '') {
        // 如果是完整 URL，直接使用
        if (imagePath.startsWith('http')) {
          return imagePath;
        }
        // 如果是相對路徑，組合基礎 URL
        return `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'}${imagePath}`;
      }
      // 使用預設頭像
      return defaultAvatar;
    };
    
    /**
     * 圖片載入錯誤處理
     * @param {Event} event - 錯誤事件
     */
    const handleImageError = (event) => {
      console.log('圖片載入失敗，使用預設頭像');
      event.target.src = defaultAvatar;
    };
    
    /**
     * 關閉詳情視窗
     */
    const close = () => {
      emit('close');
    };
    
    /**
     * 編輯員工資料
     */
    const editEmployee = () => {
      emit('edit', props.employeeId);
      close();
    };
    
    /**
     * 設定職位徽章的樣式
     * @param {string} roleName - 職位名稱
     * @returns {string} CSS 類別名稱
     */
    const getRoleClass = (roleName) => {
      // 註解：根據使用者提供的圖片更新顏色樣式
      const roleClasses = {
        'ROLE_ADMIN': 'bg-danger text-white',        // ADMIN 紅色
        'ROLE_MANAGER': 'bg-warning text-dark',      // MANAGER 橘黃色
        'ROLE_EMPLOYEE': 'bg-success text-white',    // EMPLOYEE 綠色
      };
      
      return roleClasses[roleName] || 'bg-secondary text-white';
    };
    
    /**
     * 設定狀態徽章的樣式
     * @param {string} status - 員工狀態
     * @returns {object} CSS 類別物件
     */
    const getStatusClass = (status) => {
      return {
        'bg-success-focus text-success-main': status === 'active',
        'bg-danger-focus text-danger-main': status === 'inactive'
      };
    };
    
    /**
     * 格式化日期顯示
     * @param {string|Date} dateString - 日期字串或日期物件
     * @returns {string} 格式化後的日期字串
     */
    const formatDate = (dateString) => {
      if (!dateString) return 'N/A';
      
      try {
        const date = new Date(dateString);
        return new Intl.DateTimeFormat('zh-TW', {
          year: 'numeric',
          month: '2-digit',
          day: '2-digit',
          hour: '2-digit',
          minute: '2-digit'
        }).format(date);
      } catch (e) {
        console.error('日期格式化錯誤:', e);
        return dateString;
      }
    };
    
    /**
     * 格式化職位標籤，去除 'ROLE_' 前綴
     * @param {string} roleName - 原始職位名稱
     * @returns {string} 格式化後的職位名稱
     */
    const formatRoleLabel = (roleName) => {
      if (!roleName) return '';
      return roleName.startsWith('ROLE_') ? roleName.substring(5) : roleName;
    };
    
    // 監聽 employeeId 變化，重新載入資料
    watch(() => props.employeeId, (newVal) => {
      if (newVal && props.visible) {
        loadEmployeeData();
      }
    });
    
    // 監聽可見性變化
    watch(() => props.visible, (newVal) => {
      if (newVal && props.employeeId) {
        loadEmployeeData();
      } else if (!newVal) {
        // 清空資料，下次開啟時重新載入
        employee.value = null;
        error.value = null;
      }
    });
    
    return {
      loading,
      error,
      employee,
      defaultAvatar,
      loadEmployeeData,
      retryLoad,
      getEmployeeImage,
      handleImageError,
      close,
      editEmployee,
      getRoleClass,
      getStatusClass,
      formatDate,
      formatRoleLabel // 註解：將新函式回傳給模板使用
    };
  }
}
</script>

<style scoped>
/* 遮罩層樣式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1050;
  overflow-y: auto;
  padding: 30px;
}

/* 詳情卡片樣式 */
.employee-detail-card {
  width: 100%;
  max-width: 700px;
  max-height: 90vh;
  overflow-y: auto;
  border-radius: 12px;
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.15);
  background-color: #ffffff;
}

/* 確保卡片背景色正確 */
.employee-detail-card.card {
  background-color: #ffffff !important; 
}

.employee-detail-card .card-header,
.employee-detail_card .card-footer,
.employee-detail-card .card-body {
  background-color: #ffffff;
  border-color: #e9ecef;
}

/* 暗黑模式支援 */
@media (prefers-color-scheme: dark) {
  .employee-detail-card {
    background-color: #2d3748;
  }
  
  .employee-detail-card.card {
    background-color: #2d3748 !important;
  }

  .employee-detail-card .card-header,
  .employee-detail-card .card-footer,
  .employee-detail-card .card-body {
    background-color: #2d3748;
    border-color: #4a5568;
  }
}

/* 頭像樣式 */
.profile-image {
  position: relative;
  width: 100px;
  height: 100px;
  margin: 0 auto;
  border-radius: 50%;
  overflow: hidden;
  border: 3px solid var(--border-color, #e9ecef);
}

.profile-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 職位徽章容器 */
.roles-container {
  min-height: 32px;
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 4px;
}

/* 資訊標籤樣式 */
.info-label {
  color: var(--text-secondary, #6c757d);
  display: flex;
  align-items: center;
}

.info-value {
  flex: 1;
  word-break: break-word;
}

/* 個人簡介區塊樣式 */
.additional-info .bg-light {
  background-color: #f8f9fa !important;
}

@media (prefers-color-scheme: dark) {
  .additional-info .bg-light {
    background-color: #374151 !important;
    color: #f3f4f6;
  }
}

/* 響應式設計 */
@media (max-width: 576px) {
  .modal-overlay {
    padding: 15px;
  }
  
  .employee-detail-card {
    max-width: 100%;
  }
  
  .info-item {
    flex-direction: column;
    align-items: flex-start !important;
  }
  
  .info-label {
    width: 100% !important;
    margin-bottom: 4px;
  }
}
</style>