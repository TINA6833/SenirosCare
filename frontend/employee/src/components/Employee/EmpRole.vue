<template>
  <div class="card basic-data-table">
    <!-- 表格功能區 -->
    <div class="card">
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

        <!-- 操作區塊 -->
        <div class="d-flex align-items-center gap-3">

          <!-- 狀態篩選按鈕 -->
          <div class="btn-group" role="group">
            <button @click="filterByActive(true)" type="button" class="btn"
              :class="isActive ? 'btn-primary' : 'btn-outline-primary'">
              啟用員工
            </button>
            <button @click="filterByActive(false)" type="button" class="btn"
              :class="!isActive ? 'btn-primary' : 'btn-outline-primary'">
              停用員工
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

      <!-- 表格主體 -->
      <div class="card-body">
        <!-- 載入中狀態 -->
        <div v-if="loading" class="text-center p-5">
          <div class="spinner-border" role="status">
            <span class="visually-hidden">載入中...</span>
          </div>
          <p class="mt-3">載入員工資料中...</p>
        </div>

        <!-- 錯誤訊息 -->
        <div v-else-if="error" class="alert alert-danger">
          <h5>載入錯誤</h5>
          <p>{{ error }}</p>
        </div>

        <!-- 空資料提示 -->
        <div v-else-if="paginatedEmployees.length === 0" class="alert alert-info">
          <h5>無員工資料</h5>
          <p>目前查詢條件下沒有員工資料。</p>
          <button @click="resetFilters" class="btn btn-outline-primary">重設篩選條件</button>
        </div>

        <!-- 員工資料表格 -->
        <template v-else>
          <table class="table bordered-table mb-0">
            <thead>
              <tr>
                <th scope="col" @click="sortBy('empId')" class="sortable">
                  員工ID
                  <span v-if="sortKey === 'empId'">
                    <iconify-icon :icon="sortAsc ? 'mdi:arrow-up' : 'mdi:arrow-down'"></iconify-icon>
                  </span>
                </th>
                <th scope="col" @click="sortBy('empName')" class="sortable">
                  員工資訊
                  <span v-if="sortKey === 'empName'">
                    <iconify-icon :icon="sortAsc ? 'mdi:arrow-up' : 'mdi:arrow-down'"></iconify-icon>
                  </span>
                </th>
                <th scope="col" @click="sortBy('roles')" class="sortable">
                  職位
                  <span v-if="sortKey === 'roles'">
                    <iconify-icon :icon="sortAsc ? 'mdi:arrow-up' : 'mdi:arrow-down'"></iconify-icon>
                  </span>
                </th>
                <th scope="col" @click="sortBy('isActive')" class="sortable">
                  狀態
                  <span v-if="sortKey === 'isActive'">
                    <iconify-icon :icon="sortAsc ? 'mdi:arrow-up' : 'mdi:arrow-down'"></iconify-icon>
                  </span>
                </th>
                <th scope="col">職位修改</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="employee in paginatedEmployees" :key="employee.empId">
                <!-- 員工ID欄位 -->
                <td>{{ employee.empId }}</td>
                
                <!-- 員工資訊欄位 - 包含頭像、姓名和信箱 -->
                <td>
                  <div class="d-flex align-items-center">
                    <img :src="getEmployeeImage(employee.imagePath)" alt="員工頭像" 
                      class="flex-shrink-0 me-12 radius-8"
                      width="40" height="40" @error="handleImageError">
                    <div>
                      <h6 class="text-md mb-1 fw-medium text-secondary-light">{{ employee.empName }}</h6>
                      <small class="text-muted">{{ employee.email || 'N/A' }}</small>
                    </div>
                  </div>
                </td>
                
                <!-- 職位欄位 - 支援多個職位並垂直對齊 -->
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
                
                <!-- 狀態欄位 -->
                <td>
                  <span :class="statusClasses(employee.isActive)" 
                    class="px-24 py-4 rounded-pill fw-medium text-sm">
                    {{ employee.isActive === 'active' ? '啟用' : '停用' }}
                  </span>
                </td>
                
                <!-- 操作欄位 -->
                <td>
                  <div class="d-flex gap-2 align-items-center">
                    <!-- 職位修改按鈕 -->
                    <button 
                      class="btn btn-icon btn-sm bg-warning-light text-warning-600 rounded-circle" 
                      type="button"
                      @click="openRoleEditModal(employee)"
                      :disabled="isRoleAssigning === employee.empId"
                      title="編輯員工職位">
                      <span v-if="isRoleAssigning === employee.empId" 
                        class="spinner-border spinner-border-sm" role="status">
                      </span>
                      <iconify-icon v-else icon="solar:user-id-outline" width="20" height="20"></iconify-icon>
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>

          <!-- 分頁區域 - 與 EmployeeTable 完全一致 -->
          <div class="d-flex flex-wrap align-items-center justify-content-between gap-2 mt-24">
            <span>
              顯示 {{ startIndex + 1 }} 至 {{ endIndex }} 筆，共 {{ totalEntries }} 筆資料
            </span>
            <ul class="pagination d-flex flex-wrap align-items-center gap-2 justify-content-center">
              <!-- 首頁按鈕 -->
              <li class="page-item">
                <a class="page-link text-secondary-light fw-medium radius-4 border-0 px-10 py-10 d-flex align-items-center justify-content-center h-32-px w-32-px bg-base"
                  href="javascript:void(0)" @click="goToPage(1)"
                  :class="{ disabled: currentPage === 1 }">
                  <iconify-icon icon="ep:d-arrow-left" class="text-xl"></iconify-icon>
                </a>
              </li>
              
              <!-- 上一頁按鈕 -->
              <li class="page-item">
                <a class="page-link text-secondary-light fw-medium radius-4 border-0 px-10 py-10 d-flex align-items-center justify-content-center h-32-px w-32-px bg-base"
                  href="javascript:void(0)" @click="goToPage(currentPage - 1)"
                  :class="{ disabled: currentPage === 1 }">
                  <iconify-icon icon="ep:arrow-left"></iconify-icon>
                </a>
              </li>

              <!-- 頁碼按鈕 -->
              <li v-for="page in displayedPages" :key="page" class="page-item">
                <a href="javascript:void(0)"
                  class="page-link fw-medium radius-4 border-0 px-10 py-10 d-flex align-items-center justify-content-center h-32-px w-32-px"
                  :class="{
                    'bg-primary-600 text-white': currentPage === page,
                    'bg-primary-50 text-secondary-light': currentPage !== page
                  }" @click="goToPage(page)">
                  {{ page }}
                </a>
              </li>

              <!-- 下一頁按鈕 -->
              <li class="page-item">
                <a class="page-link text-secondary-light fw-medium radius-4 border-0 px-10 py-10 d-flex align-items-center justify-content-center h-32-px w-32-px bg-base"
                  href="javascript:void(0)" @click="goToPage(currentPage + 1)"
                  :class="{ disabled: currentPage === totalPages }">
                  <iconify-icon icon="ep:arrow-right" class="text-xl"></iconify-icon>
                </a>
              </li>

              <!-- 尾頁按鈕 -->
              <li class="page-item">
                <a class="page-link text-secondary-light fw-medium radius-4 border-0 px-10 py-10 d-flex align-items-center justify-content-center h-32-px w-32-px bg-base"
                  href="javascript:void(0)" @click="goToPage(totalPages)"
                  :class="{ disabled: currentPage === totalPages }">
                  <iconify-icon icon="ep:d-arrow-right" class="text-xl"></iconify-icon>
                </a>
              </li>
            </ul>
          </div>
        </template>
      </div>
    </div>

    <!-- 新增員工表單模態框 -->
    <AddEmployeeForm @submit="handleAddEmployee" @error="handleFormError" ref="addEmployeeFormRef" />

    <!-- 職位編輯 Modal -->
    <RoleEditModal
      modal-id="roleEditModal"
      :employee-to-edit="selectedEmployee"
      :all-roles="roles"
      @update-roles="handleUpdateRoles"
    />
  </div>
</template>

<script>
import { ref, computed, watch, onMounted } from 'vue';
import Pagination from '@/components/pagination/index.vue';
import RoleEditModal from '@/components/Employee/RoleEditModal.vue';
import defaultAvatarImage from '@/assets/images/user-list/user-list1.png';
import { useToast } from '@/composables/useToast';
import AddEmployeeForm from '@/components/Employee/AddEmployeeForm.vue';
import { Modal } from 'bootstrap';

export default {
  name: 'EmployeeRoleAssignment',
  components: {
    Pagination,
    RoleEditModal,
    AddEmployeeForm,
  },

  props: {
    // 員工資料陣列
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
    // 當前篩選狀態
    activeFilter: {
      type: Boolean,
      default: true
    }
  },

  emits: ['update-employee-roles', 'filter', 'add-employee'],

  setup(props, { emit }) {
    // 用於引用 AddEmployeeForm 元件
    const addEmployeeFormRef = ref(null);

    // 分頁相關參數
    const currentPage = ref(1);
    const rolesPerPage = ref(10);
    
    // 排序相關參數
    const sortKey = ref('empId');
    const sortAsc = ref(true);
    
    // 搜尋與篩選相關參數
    const searchText = ref('');
    const isSearchFocused = ref(false);
    
    // 用於篩選啟用或停用員工
    const isActive = ref(props.activeFilter);
    
    const { showToast } = useToast();
    
    // 記錄正在分配角色的員工 ID
    const isRoleAssigning = ref(null);
    
    // 選中的員工（用於 Modal）
    const selectedEmployee = ref(null);

    // 預設頭像
    const defaultAvatar = defaultAvatarImage;

    /**
     * 取得員工圖片 URL
     * @param {string} imagePath - 圖片路徑
     * @returns {string} 完整的圖片 URL
     */
    const getEmployeeImage = (imagePath) => {
      if (imagePath && imagePath !== '無圖片' && imagePath.trim() !== '') {
        if (imagePath.startsWith('http')) {
          return imagePath;
        }
        return `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'}${imagePath}`;
      }
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
     * 根據職位類型返回對應的樣式類別
     * @param {string} role 職位名稱
     * @returns {string} CSS 類別字串
     */
    const getRoleClass = (role) => {
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
     * 設定狀態標籤的樣式
     * @param {string} status 狀態值
     * @returns {Object} CSS 類別物件
     */
    const statusClasses = (status) => {
      return {
        'bg-success-focus text-success-main': status === 'active',
        'bg-secondary text-white': status !== 'active'
      };
    };

    /**
     * 格式化職位標籤的函式，移除 "ROLE_" 前綴
     * @param {string} role 職位名稱
     * @returns {string} 格式化後的職位名稱
     */
    const formatRoleLabel = (role) => {
      if (!role) return '';
      return role.startsWith('ROLE_') ? role.replace('ROLE_', '') : role;
    };

    /**
     * 處理資料排序
     * @param {string} key 排序欄位名稱
     */
    const sortBy = (key) => {
      if (sortKey.value === key) {
        sortAsc.value = !sortAsc.value;
      } else {
        sortKey.value = key;
        sortAsc.value = true;
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
     * 切換頁碼
     * @param {number} page 目標頁碼
     */
    const goToPage = (page) => {
      if (page >= 1 && page <= totalPages.value) {
        currentPage.value = page;
      }
    };

    /**
     * 開啟職位編輯 Modal
     * @param {Object} employee 員工物件
     */
    const openRoleEditModal = (employee) => {
      console.log('[EmpRole.vue] 開啟職位編輯 Modal，員工:', employee);
      selectedEmployee.value = employee;
      const modal = new Modal(document.getElementById('roleEditModal'));
      modal.show();
    };

    /**
     * 處理職位更新事件
     * @param {Object} updateData 更新資料 { empId, roleIds }
     */
    const handleUpdateRoles = async (updateData) => {
      console.log('[EmpRole.vue] 收到 Modal 傳來的更新資料:', updateData);
      
      // 設定載入狀態
      isRoleAssigning.value = updateData.empId;
      
      try {
        // 發送事件給父元件處理
        emit('update-employee-roles', updateData.empId, updateData.roleIds);
        
      } catch (error) {
        console.error('[EmpRole.vue] 處理職位更新失敗:', error);
      } finally {
        // 清除載入狀態和選中的員工
        setTimeout(() => {
          isRoleAssigning.value = null;
          selectedEmployee.value = null;
        }, 1000);
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
      isActive.value = active;
      currentPage.value = 1; // 重置頁碼
      console.log('切換篩選狀態為:', active ? '啟用' : '停用');
      emit('filter', active);
    };

    /**
     * 處理搜尋事件
     */
    const handleSearch = () => {
      currentPage.value = 1; // 重置頁碼
    };

    /**
     * 重設所有篩選條件
     */
    const resetFilters = () => {
      searchText.value = '';
      isActive.value = true;
      currentPage.value = 1;
      // 可以新增 emit 事件通知父元件重設
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

    // 計算篩選後的員工列表
    const filteredEmployees = computed(() => {
      let result = [...props.employees];

      // 根據啟用狀態篩選
      result = result.filter(emp => emp.isActive === (isActive.value ? 'active' : 'inactive'));

      // 根據搜尋文字篩選
      if (searchText.value) {
        const search = searchText.value.toLowerCase();
        result = result.filter(emp =>
          (emp.empName && emp.empName.toLowerCase().includes(search)) ||
          (emp.email && emp.email.toLowerCase().includes(search)) ||
          (emp.roles && emp.roles.some(role =>
            role.role_name && role.role_name.toLowerCase().includes(search)
          ))
        );
      }

      // 根據排序設定進行排序
      if (sortKey.value) {
        result.sort((a, b) => {
          let valA, valB;

          // 處理不同類型的欄位
          if (sortKey.value === 'empId') {
            valA = Number(a[sortKey.value]);
            valB = Number(b[sortKey.value]);
          } else if (sortKey.value === 'roles') {
            valA = a.roles && a.roles.length > 0 ? a.roles[0].role_name.toLowerCase() : '';
            valB = b.roles && b.roles.length > 0 ? b.roles[0].role_name.toLowerCase() : '';
          } else if (sortKey.value === 'isActive') {
            valA = a[sortKey.value] === 'active' ? 1 : 0;
            valB = b[sortKey.value] === 'active' ? 1 : 0;
          } else {
            valA = a[sortKey.value] ? String(a[sortKey.value]).toLowerCase() : '';
            valB = b[sortKey.value] ? String(b[sortKey.value]).toLowerCase() : '';
          }

          // 根據排序方向比較值
          if (valA === valB) return 0;
          return sortAsc.value ? (valA > valB ? 1 : -1) : (valA < valB ? 1 : -1);
        });
      }

      return result;
    });

    // 每頁顯示筆數
    const entriesPerPage = computed(() => Number(rolesPerPage.value));

    // 計算當前頁面要顯示的員工
    const paginatedEmployees = computed(() => {
      return filteredEmployees.value.slice(startIndex.value, endIndex.value);
    });

    // 資料總筆數
    const totalEntries = computed(() => filteredEmployees.value.length);

    // 計算總頁數
    const totalPages = computed(() => {
      return Math.ceil(filteredEmployees.value.length / entriesPerPage.value) || 1;
    });

    // 計算當前頁面的起始和結束索引
    const startIndex = computed(() => {
      return (currentPage.value - 1) * entriesPerPage.value;
    });

    const endIndex = computed(() => {
      return Math.min(startIndex.value + entriesPerPage.value, filteredEmployees.value.length);
    });

    // 顯示的頁碼範圍 (最多顯示5頁)
    const displayedPages = computed(() => {
      if (totalPages.value <= 5) {
        return [...Array(totalPages.value)].map((_, i) => i + 1);
      }

      let start = currentPage.value - 2;
      let end = currentPage.value + 2;

      if (start < 1) {
        end += (1 - start);
        start = 1;
      }

      if (end > totalPages.value) {
        start -= (end - totalPages.value);
        end = totalPages.value;
      }

      start = Math.max(1, start);

      return [...Array(end - start + 1)].map((_, i) => start + i);
    });

    /**
    * 處理新增員工表單提交
    * @param {Object} formData - 包含員工資料和選擇職位的物件
    */
    const handleAddEmployee = async (formData) => {
      try {
        emit('add-employee', formData);

        showToast({
          title: '新增成功',
          message: `已成功新增員工 ${formData.employeeData.empName}`,
          type: 'success'
        });
      } catch (error) {
        console.error('新增員工時出錯:', error);

        showToast({
          title: '新增失敗',
          message: error.message || '新增員工時發生錯誤',
          type: 'error'
        });
      }
    };

    // 監聽每頁顯示數量變更
    watch(rolesPerPage, () => {
      currentPage.value = 1; // 重設為第一頁
    });

    // 監聽 props.employees 變化，確保數據更新時重置頁碼
    watch(() => props.employees, () => {
      if (currentPage.value > totalPages.value && totalPages.value > 0) {
        currentPage.value = Math.max(1, totalPages.value);
      }
    }, { deep: true });

    // 元件掛載時的初始化邏輯
    onMounted(() => {
      // 初始化邏輯
    });

    return {
      // 狀態參數
      currentPage,
      rolesPerPage,
      sortKey,
      sortAsc,
      searchText,
      isSearchFocused,
      isActive,
      isRoleAssigning,
      selectedEmployee,

      // 計算屬性
      filteredEmployees,
      paginatedEmployees,
      totalEntries,
      totalPages,
      startIndex,
      endIndex,
      displayedPages,
      entriesPerPage,

      // 圖片處理方法
      getEmployeeImage,
      handleImageError,

      // 方法
      getRoleClass,
      statusClasses,
      formatRoleLabel,
      sortBy,
      changePage,
      goToPage,
      openRoleEditModal,
      handleUpdateRoles,
      filterByActive,
      handleSearch,
      resetFilters,
      handleFormError,
      addEmployeeFormRef,
      handleAddEmployee,
    };
  }
}
</script>

<style scoped>
/* 可排序欄位樣式 */
.sortable {
  cursor: pointer;
  user-select: none;
}

.sortable:hover {
  text-decoration: underline;
}

/* 表格樣式 */
.table th,
.table td {
  vertical-align: middle;
}

/* 分頁按鈕樣式 */
.page-link.disabled {
  opacity: 0.5;
  pointer-events: none;
}

/* 員工頭像樣式 */
.table img {
  object-fit: cover;
  border-radius: 8px;
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