<template>
  <div class="dashboard-main-body">

    <!-- 載入中狀態 -->
    <div v-if="isLoading" class="text-center my-5">
      <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">載入中...</span>
      </div>
      <p class="mt-2">正在載入員工資料...</p>
    </div>

    <!-- 錯誤訊息 -->
    <div v-else-if="error" class="alert alert-danger" role="alert">
      <i class="bi bi-exclamation-triangle-fill me-2"></i>
      {{ error }}
      <button class="btn btn-sm btn-outline-danger ms-3" @click="fetchEmployees(null, true)">
        重試
      </button>
    </div>

    <!-- 引入員工角色分配元件 -->
    <EmpAdmin v-else :employees="employees" :roles="availableRoles" :loading="isLoading" :error="error"
      :active-filter="currentFilterActive" @filter="handleFilter" @update-employee-roles="handleUpdateEmployeeRoles"
      @toggle-status="handleToggleStatus" @add-employee="handleAddEmployee" />
  </div>
</template>

<script>
import Breadcrumb from '@/components/breadcrumb/Breadcrumb.vue';
import EmpAdmin from '@/components/Employee/EmpAdmin.vue';
import { useEmployees } from '@/composables/useEmployees';
import { ref, computed, onMounted } from 'vue';
import { useToast } from '@/composables/useToast';

export default {
  name: "EmployeeRolePage",
  components: {
    Breadcrumb,
    EmpAdmin
  },
  setup() {
    // 使用員工資料 composable
    const {
      employees,
      roles,
      isLoading,
      error,
      filterByStatus,
      fetchEmployees,
      fetchRoles,
      updateEmployeeRoles,
      toggleEmployeeStatus: apiToggleStatus,
      addEmployee: apiAddEmployee,
    } = useEmployees({ autoLoad: true });

    const { showToast } = useToast();

    // 開發模式標誌
    const isDev = ref(import.meta.env.DEV);

    // 可用角色列表
    const availableRoles = computed(() => {
      return roles.value;
    });

    // 追蹤當前篩選狀態
    const currentFilterActive = ref(true);

    /**
     * 處理新增員工事件 - 修正：正確處理傳入的資料結構
     * @param {Object} submitData - 包含 employeeData 和 selectedRole 的物件
     * @param {Object} submitData.employeeData - 員工基本資料
     * @param {string} submitData.selectedRole - 選擇的職位
     */
    const handleAddEmployee = async (submitData) => {
      try {
        console.log('[empAdmin.vue] 收到新增員工請求:', submitData);
        
        // 驗證傳入的資料結構
        if (!submitData || !submitData.employeeData) {
          throw new Error('傳入的員工資料格式錯誤');
        }
        
        const { employeeData, selectedRole } = submitData;
        
        // 驗證員工資料必填欄位
        if (!employeeData.empName || !employeeData.email || !employeeData.password) {
          console.error('[empAdmin.vue] 員工資料欄位檢查失敗:', {
            empName: !!employeeData.empName,
            email: !!employeeData.email,
            password: !!employeeData.password
          });
          throw new Error('員工資料缺少必填欄位');
        }
        
        isLoading.value = true;
        
        console.log('[empAdmin.vue] 準備呼叫 API 新增員工, 資料:', employeeData);
        
        // 呼叫 API 新增員工 - 只傳送 employeeData
        const newEmployee = await apiAddEmployee(employeeData);
        console.log('[empAdmin.vue] 員工新增成功:', newEmployee);
        
        // 重新載入員工列表
        await fetchEmployees();
        
        // 顯示成功訊息
        showToast({
          title: '新增成功',
          message: `已成功新增員工 ${employeeData.empName}`,
          type: 'success'
        });

      } catch (err) {
        console.error('[empAdmin.vue] 新增員工失敗:', err);
        
        // 顯示詳細錯誤訊息
        showToast({
          title: '新增失敗',
          message: err.message || '新增員工時發生錯誤',
          type: 'error'
        });
        
        // 重新拋出錯誤讓表單元件也能處理
        throw err;
      } finally {
        isLoading.value = false;
      }
    };

    /**
     * 處理狀態篩選事件
     * @param {boolean} isActive 是否篩選啟用的員工
     */
    const handleFilter = async (isActive) => {
      console.log('處理篩選事件：', isActive ? '啟用' : '停用');
      currentFilterActive.value = isActive;
      await filterByStatus(isActive);
    };

    /**
     * 處理員工職位更新事件
     * @param {number} empId 員工 ID
     * @param {Array<number>} roleIds 新的職位 ID 陣列
     */
    const handleUpdateEmployeeRoles = async (empId, roleIds) => {
      try {
        isLoading.value = true;
        await updateEmployeeRoles(empId, roleIds);
        await fetchEmployees(null, true);
      } catch (error) {
        console.error('更新員工職位失敗:', error);
      } finally {
        isLoading.value = false;
      }
    };

    /**
     * 處理切換員工狀態事件
     * @param {number} id 員工 ID
     */
    const handleToggleStatus = async (id) => {
      try {
        isLoading.value = true;

        const employee = employees.value.find(emp => emp.empId === id);
        const currentStatus = employee ? employee.isActive : null;
        const newStatusText = currentStatus === 'active' ? '停用' : '啟用';

        await apiToggleStatus(id);

        showToast({
          title: '狀態變更成功',
          message: `已${newStatusText}員工 ID: ${id}`,
          type: 'success'
        });

        await fetchEmployees();

      } catch (err) {
      } finally {
        isLoading.value = false;
      }
    };

    // 在元件掛載時確保獲取角色列表
    onMounted(() => {
      fetchRoles().catch(err => {
        console.error('獲取角色列表失敗:', err);
      });
    });

    return {
      // 狀態
      employees,
      isLoading,
      error,
      isDev,
      availableRoles,
      currentFilterActive,

      // 方法
      handleFilter,
      handleUpdateEmployeeRoles,
      handleToggleStatus,
      fetchEmployees,
      handleAddEmployee,
    };
  }
}
</script>