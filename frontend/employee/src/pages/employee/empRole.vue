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
    <EmpRole v-else
      :employees="employees"
      :roles="availableRoles"
      :loading="isLoading"
      :error="error"
      :active-filter="currentFilterActive" 
      @filter="handleFilter"
      @update-employee-roles="handleUpdateEmployeeRoles"
      @toggle-status="handleToggleStatus"
      @add-employee="handleAddEmployee"
    />
  </div>
</template>

<script>
import Breadcrumb from '@/components/breadcrumb/Breadcrumb.vue';
import EmpRole from '@/components/Employee/EmpRole.vue';
import { useEmployees } from '@/composables/useEmployees';
import { ref, computed, onMounted } from 'vue';
import { useToast } from '@/composables/useToast';

export default {
  name: "EmployeeRolePage",
  components: {
    Breadcrumb,
    EmpRole
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
      updateEmployeeRoles, // 修正：使用正確的方法名稱
      toggleEmployeeStatus: apiToggleStatus,
      addEmployee: apiAddEmployee,
    } = useEmployees({ autoLoad: true });
    
    const { showToast } = useToast();

    // 開發模式標誌
    const isDev = ref(import.meta.env.DEV);

    // 可用角色列表 - 修正：直接使用完整的角色物件
    const availableRoles = computed(() => {
      return roles.value; // 直接返回完整的角色物件陣列
    });

    // 追蹤當前篩選狀態
    const currentFilterActive = ref(true);

    /**
     * 處理新增員工事件
     * @param {Object} employeeData - 新員工資料
     */
    const handleAddEmployee = async (employeeData) => {
      try {
        isLoading.value = true;
        await apiAddEmployee(employeeData);
        await fetchEmployees();
        
      } catch (err) {
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
     * 處理員工職位更新事件 - 新增此方法
     * @param {number} empId 員工 ID
     * @param {Array<number>} roleIds 新的職位 ID 陣列
     */
    const handleUpdateEmployeeRoles = async (empId, roleIds) => {

      try {
        isLoading.value = true;
        await updateEmployeeRoles(empId, roleIds);

        // 重新載入員工資料以反映變更
        await fetchEmployees(null, true);
        showToast({
          title: '職位更新成功',
          message: `已成功更新員工 ID : ${empId} 的職位`,
          type: 'success'
        });
        
      } catch (error) {
        console.error('[EmpRole.vue] 處理職位更新失敗:', error);
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
        showToast({
          title: '操作失敗',
          message: err.message || '變更員工狀態時發生錯誤',
          type: 'error'
        });
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
      handleUpdateEmployeeRoles, // 新增：匯出職位更新方法
      handleToggleStatus,
      fetchEmployees,
      handleAddEmployee,
    };
  }
}
</script>