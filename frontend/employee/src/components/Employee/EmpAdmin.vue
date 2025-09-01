<template>
  <div class="card ">
    <div class="card-header d-flex flex-wrap align-items-center justify-content-between gap-3"
      style="border-bottom: none; padding-bottom: 0px;">

      <!-- 每頁顯示筆數選擇器 -->
      <div class="d-flex flex-wrap align-items-center gap-3">
        <div class="d-flex align-items-center gap-2">
          <select class="form-select form-select-lr w-auto rounded-3 me-10" v-model="rolesPerPage"
            style="border-radius: 10px; height: 2.4rem;">
            <option value="10">10</option>
            <option value="15">15</option>
            <option value="20">20</option>
          </select>
          <span>筆/頁</span>
        </div>
      </div>

      <!-- 搜尋區塊 -->
      <div class="d-flex align-items-center gap-3">
        <!-- 新增員工按鈕 -->
        <button type="button" class="btn btn-warning" @click="openAddEmployeeModal">
          <iconify-icon icon="solar:user-plus-outline" class="me-1"></iconify-icon>
          新增員工
        </button>

        <!-- 新增員工表單模態框 -->
        <AddEmployeeForm @submit="handleAddEmployee" @error="handleFormError" ref="addEmployeeFormRef" />

        <!-- 狀態篩選按鈕 -->
        <div class="btn-group" role="group">
          <button @click="filterByActive(true)" type="button" class="btn"
            :class="isActive ? 'btn-primary' : 'btn-outline-primary'">啟用員工
          </button>
          <button @click="filterByActive(false)" type="button" class="btn"
            :class="!isActive ? 'btn-primary' : 'btn-outline-primary'">停用員工
          </button>
        </div>

        <!-- 搜尋輸入框 -->
        <form class="navbar-search">
          <input type="text" name="search" placeholder="搜尋員工或職位" v-model="searchText" @input="handleSearch"
            @focus="isSearchFocused = true" @blur="isSearchFocused = false" />
          <iconify-icon icon="ion:search-outline" class="icon"></iconify-icon>
        </form>
      </div>
    </div>

    <div class="card-body">
      <!-- 載入中狀態 -->
      <div v-if="loading" class="text-center p-5">
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">載入中...</span>
        </div>
        <p class="mt-3">載入員工資料中...</p>
      </div>

      <!-- 錯誤訊息 -->
      <div v-else-if="error" class="alert alert-danger">
        <iconify-icon icon="solar:danger-triangle-outline" class="me-2"></iconify-icon>
        <h5>載入錯誤</h5>
        <p>{{ error }}</p>
      </div>

      <!-- 空資料提示 -->
      <div v-else-if="paginatedEmployees.length === 0" class="alert alert-info">
        <iconify-icon icon="solar:info-circle-outline" class="me-2"></iconify-icon>
        <h5>無員工資料</h5>
        <p>目前查詢條件下沒有員工資料。</p>
      </div>

      <!-- 員工資料表格 -->
      <div v-else class="table-responsive border rounded-10 overflow-hidden">
        <table class="table bordered-table mb-0 ">
          <thead class="bg-base">
            <tr>
              <!-- 修正：使用 empId 而非 id -->
              <th class="text-nowrap" style="padding-left: 10px;">員工ID</th>
              <th class="text-nowrap">員工資訊</th>
              <th class="text-nowrap">職位</th>
              <th class="text-nowrap text-center">狀態</th>
              <th class="text-nowrap text-center">操作</th>
            </tr>
          </thead>
          <tbody>
            <!-- 修正：使用 empId 作為 key 和顯示欄位 -->
            <tr v-for="employee in paginatedEmployees" :key="employee.empId">
              <td style="padding-left: 10px;">{{ employee.empId }}</td>
              <td>
                <div class="d-flex align-items-center">
                  <!-- 修正：使用 imagePath 而非 image，使用 getEmployeeImage 方法 -->
                  <img :src="getEmployeeImage(employee.imagePath)" alt="員工頭像" class="flex-shrink-0 me-12 radius-8"
                    width="40" height="40" @error="handleImageError">
                  <div>
                    <!-- 修正：使用 empName 而非 name -->
                    <div class="text-md mb-1 fw-medium text-secondary-light">{{ employee.empName }}</div>
                    <!-- 新增：顯示電子郵件 -->
                    <small class="text-muted">{{ employee.email }}</small>
                  </div>
                </div>
              </td>
              <!-- 修正：職位顯示邏輯，支援多個職位並垂直對齊 - 參考 EmpRole.vue 的排版 -->
              <td>
                <!-- 有職位：全部列出 -->
                <template v-if="employee.roles && employee.roles.length > 0">
                  <span v-for="role in employee.roles" :key="role.roleId || role.role_id"
                    :class="getRoleClass(role.role_name)"
                    class="px-12 py-2 rounded-pill fw-medium text-sm me-2 mb-1 d-inline-block">
                    {{ formatRoleLabel(role.role_name) }}
                  </span>
                </template>

                <!-- 無職位 -->
                <span v-else class="px-12 py-2 rounded-pill fw-medium text-sm bg-secondary text-white">
                  無職位
                </span>
              </td>
              <!-- 註解：修改狀態顯示，移除圖示並更新樣式 -->
              <td class="text-center">
                <span :class="getStatusClass(employee.isActive)" class="px-16 py-4 rounded-pill fw-medium text-sm">
                  {{ employee.isActive === 'active' ? '啟用' : '停用' }}
                </span>
              </td>
              <td class="text-center">
                <!-- 操作按鈕 -->
                <div class="d-flex gap-2 justify-content-center">

                  <!-- 切換狀態按鈕 - 修正：使用 empId 和 isActive -->
                  <button class="btn px-16 py-10"
                    :class="employee.isActive === 'active' ? 'btn-outline-danger' : 'btn-outline-success'"
                    @click="toggleStatus(employee.empId)" :disabled="buttonLoadingId === employee.empId">
                    <span v-if="buttonLoadingId === employee.empId" class="spinner-border spinner-border-sm me-1"
                      role="status"></span>
                    {{ employee.isActive === 'active' ? '停權' : '啟用' }}
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 分頁區域 -->
      <Pagination v-if="!loading && !error && paginatedEmployees.length > 0" :currentPage="currentPage"
        :totalPages="totalPages" :startIndex="startIndex" :endIndex="endIndex" :totalItems="filteredEmployees.length"
        @page-changed="changePage" />
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, watch } from 'vue';
import Pagination from '@/components/pagination/index.vue';
import defaultAvatar from '@/assets/images/user-list/user-list1.png';
import { useToast } from '@/composables/useToast'; // 引入 toast 元件
import AddEmployeeForm from '@/components/Employee/AddEmployeeForm.vue'; // 引入新增員工表單元件
import { useConfirmDialog } from '@/composables/useConfirmDialog';

export default {
  name: 'EmployeeRoleAssignment',
  components: {
    Pagination,
    AddEmployeeForm,
  },

  props: {
    // 員工資料陣列 - 修正：對應新的資料結構
    employees: {
      type: Array,
      required: true,
      default: () => []
    },
    // 載入狀態
    loading: {
      type: Boolean,
      default: false
    },
    // 錯誤訊息
    error: {
      type: String,
      default: null
    },
    // 角色列表
    roles: {
      type: Array,
      default: () => []
    },
    // 新增從父元件接收當前篩選狀態
    activeFilter: {
      type: Boolean,
      default: true
    }
  },

  emits: ['assign-role', 'toggle-status', 'filter', 'add-employee'],

  setup(props, { emit }) {
    // 用於引用 AddEmployeeForm 元件
    const addEmployeeFormRef = ref(null);

    // 分頁相關參數
    const currentPage = ref(1);
    const rolesPerPage = ref(10);
    const searchText = ref('');
    const isSearchFocused = ref(false);
    // 用於篩選啟用或停用員工
    const isActive = ref(props.activeFilter);
    const selectAll = ref(false);
    const selectedUsers = ref([]);
    const { showToast } = useToast(); // 使用 Toast 通知功能
    const { showConfirmDialog } = useConfirmDialog()

    // 定義 buttonLoadingId 用於追蹤正在執行操作的按鈕
    const buttonLoadingId = ref(null);
    // 記錄正在分配角色的員工 ID
    const isRoleAssigning = ref(null);

    // 角色選項 - 使用 props.roles 或預設角色
    const roleOptions = computed(() => {
      // 若有傳入角色列表且不為空，則使用傳入的角色
      if (props.roles && props.roles.length > 0) {
        return props.roles;
      }
      // 否則使用預設角色
      return ['Admin', 'Editor', 'Caregiver'];
    });

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
     * 取得完整職位列表文字 (用於 tooltip)
     * @param {Array} roles - 職位陣列
     * @returns {string} 完整的職位列表
     */
    const getFullRolesList = (roles) => {
      if (!roles || roles.length === 0) return '無職位';
      // 註解：修正欄位名稱為 role_name
      return roles.map(role => role.role_name).join(', ');
    };

    /**
     * 根據職位類型返回對應的樣式類別
     * @param {string} role 職位名稱
     * @returns {string} CSS 類別字串
     */
    const getRoleClass = (role) => {
      // 註解：更新職位樣式以符合圖片和 EmployeeTable.vue 的風格
      switch (role) {
        case 'ROLE_ADMIN':
          return 'bg-danger text-white';
        case 'ROLE_MANAGER':
          return 'bg-warning text-dark';
        case 'ROLE_EMPLOYEE':
          return 'bg-success text-white';
        default:
          return 'bg-secondary text-white';
      }
    };

    /**
     * 獲取狀態樣式類別
     * @param {string} status 狀態
     * @returns {string} CSS 類別字串
     */
    const getStatusClass = (status) => {
      // 註解：更新狀態樣式以符合圖片風格
      return status === 'active'
        ? 'bg-success-focus text-success-main'   // 啟用狀態為淺綠色
        : 'bg-secondary-light text-secondary';   // 停用狀態為灰色
    };

    /**
     * 註解：新增格式化職位標籤的函式，移除 "ROLE_" 前綴
     * @param {string} role 職位名稱
     * @returns {string} 格式化後的職位名稱
     */
    const formatRoleLabel = (role) => {
      if (!role) return '';
      // 去除 ROLE_ 前綴並返回
      return role.startsWith('ROLE_') ? role.replace('ROLE_', '') : role;
    };

    /**
     * 切換所有項目的選中狀態
     */
    const toggleSelectAll = () => {
      if (selectAll.value) {
        selectedUsers.value = paginatedEmployees.value.map(emp => emp.empId);
      } else {
        selectedUsers.value = [];
      }
    };

    /**
     * 變更頁碼
     * @param {number} page 新的頁碼
     */
    const changePage = (page) => {
      currentPage.value = page;
    };

    /**
     * 切換員工狀態 (啟用/停用) - 修正：使用 empId
     * @param {number} empId 員工 ID
     */
    const toggleStatus = async (empId) => {
      try {
        // 顯示按鈕載入狀態
        buttonLoadingId.value = empId;

        const confirmed = await showConfirmDialog({
          title: '停權員工',
          message: '您確定要停權該員工嗎？停權後該員工將無法登入系統。',
          type: 'warning',
          confirmText: '確認停權',
          cancelText: '取消',
          icon: 'mdi:alert-circle-outline'
        });

        // 發送事件給父元件處理，等待結果
        await emit('toggle-status', empId);

        // 不顯示通知，由父元件處理通知
      } catch (error) {
        console.error('狀態切換出錯:', error);
      } finally {
        // 關閉按鈕載入狀態
        buttonLoadingId.value = null;
      }
    };

    // 監聽父元件傳來的篩選狀態變化
    watch(() => props.activeFilter, (newValue) => {
      console.log('父元件篩選狀態變化:', newValue ? '啟用' : '停用');
      isActive.value = newValue;
    });

    /**
     * 根據啟用狀態篩選員工
     * @param {boolean} active 是否篩選啟用的員工
     */
    const filterByActive = (active) => {
      console.log('切換篩選狀態為:', active ? '啟用' : '停用');
      // 發送事件給父元件處理，而不直接修改 isActive
      emit('filter', active);
    };

    /**
     * 處理搜尋事件
     */
    const handleSearch = () => {
      currentPage.value = 1; // 重置頁碼
    };

    /**
     * 處理表單錯誤
     * @param {string} errorMessage - 錯誤訊息
     */
    const handleFormError = (errorMessage) => {
      showToast({
        title: '表單錯誤',
        message: errorMessage,
        type: 'error'
      });
    };

    // 計算篩選後的員工列表 - 修正：使用新的欄位名稱
    const filteredEmployees = computed(() => {
      let result = [...props.employees];

      // 根據啟用狀態篩選 - 修正：使用 isActive 欄位
      result = result.filter(emp => emp.isActive === (isActive.value ? 'active' : 'inactive'));

      // 根據搜尋文字篩選 - 修正：使用新的欄位名稱和職位陣列搜尋
      if (searchText.value) {
        const search = searchText.value.toLowerCase();
        result = result.filter(emp =>
          (emp.empName && emp.empName.toLowerCase().includes(search)) ||
          (emp.email && emp.email.toLowerCase().includes(search)) ||
          (emp.roles && emp.roles.some(role =>
            role.roleName && role.roleName.toLowerCase().includes(search)
          ))
        );
      }

      return result;
    });

    // 計算當前頁面要顯示的員工
    const paginatedEmployees = computed(() => {
      const startIdx = (currentPage.value - 1) * rolesPerPage.value;
      const endIdx = startIdx + parseInt(rolesPerPage.value);
      return filteredEmployees.value.slice(startIdx, endIdx);
    });

    // 計算總頁數
    const totalPages = computed(() => {
      return Math.ceil(filteredEmployees.value.length / rolesPerPage.value) || 1;
    });

    // 計算當前頁面的起始和結束索引
    const startIndex = computed(() => {
      return (currentPage.value - 1) * rolesPerPage.value;
    });

    const endIndex = computed(() => {
      return Math.min(startIndex.value + parseInt(rolesPerPage.value), filteredEmployees.value.length);
    });

    /**
    * 開啟新增員工模態框
    */
    const openAddEmployeeModal = () => {
      // 使用 Bootstrap 的 Modal 方法開啟模態框
      const modal = new bootstrap.Modal(document.getElementById('addEmployeeModal'));
      modal.show();
    };

    /**
    * 處理新增員工表單提交
    * @param {Object} formData - 包含員工資料和選擇職位的物件
    * @param {Object} formData.employeeData - 新員工資料
    * @param {string} formData.selectedRole - 選擇的職位
    */
    const handleAddEmployee = async (formData) => {
      try {
        // 發送事件給父元件處理新增員工邏輯
        await emit('add-employee', formData);
      } catch (error) {
        console.error('新增員工時出錯:', error);
      }
    };

    // 監聽 props.employees 變化，確保數據更新時重置頁碼
    watch(() => props.employees, () => {
      if (currentPage.value > totalPages.value && totalPages.value > 0) {
        currentPage.value = 1;
      }
    }, { deep: true });

    return {
      currentPage,
      rolesPerPage,
      searchText,
      isSearchFocused,
      isActive,
      selectAll,
      selectedUsers,
      roleOptions,
      defaultAvatar,
      isRoleAssigning,

      // 計算屬性
      filteredEmployees,
      paginatedEmployees,
      totalPages,
      startIndex,
      endIndex,

      // 圖片處理方法
      getEmployeeImage,
      handleImageError,
      getFullRolesList,

      // 方法
      getRoleClass,
      getStatusClass,
      formatRoleLabel, // 註解：匯出新函式
      toggleSelectAll,
      changePage,
      buttonLoadingId,
      toggleStatus,
      filterByActive,
      handleSearch,
      handleFormError,
      addEmployeeFormRef,
      openAddEmployeeModal,
      handleAddEmployee,
    };
  }
}
</script>

<style scoped>
/* 新增樣式：職位提示文字 */
.roles-tooltip {
  cursor: help;
}

/* 改善按鈕間距 */
.btn-group .btn+.btn {
  margin-left: 0;
}

/* 表格行高調整 */
.table td {
  vertical-align: middle;
  padding: 12px 8px;
}

/* 圖片樣式增強 */
.table img {
  object-fit: cover;
  border: 2px solid #f8f9fa;
  transition: border-color 0.2s ease;
}

.table img:hover {
  border-color: #dee2e6;
}

/* 職位徽章樣式 */
.badge-role {
  font-size: 0.75rem;
  font-weight: 500;
  line-height: 1.2;
}

/* 狀態徽章樣式 */
.badge-status {
  font-size: 0.75rem;
  font-weight: 500;
  line-height: 1.2;
  min-width: 60px;
}

/* 操作按鈕樣式 */
.btn-action {
  font-size: 0.875rem;
  padding: 6px 12px;
}

/* 響應式調整 */
@media (max-width: 768px) {
  .card-header {
    flex-direction: column;
    align-items: stretch !important;
  }

  .btn-group {
    width: 100%;
  }

  .btn-group .btn {
    flex: 1;
  }

  .navbar-search {
    width: 100%;
    margin-top: 10px;
  }
}
</style>