<template>
    <div class="dashboard-main-body">

        <!-- 引入員工表格元件 -->
        <EmployeeGrid
            :employees="employees"
            :roles="roles"
            :loading="isLoading"
            :error="error"
            @search="handleSearch"
            @filter="handleFilter"
            @view="viewEmployee"
            @edit="editEmployee"
            @delete="deleteEmployee"
            @reset="resetFilters"
            @update-employee-roles="handleUpdateEmployeeRoles"
        />

        <EmployeeDetail
            :visible="showDetail"
            :employee-id="selectedEmployeeId"
            @close="closeDetail"
            @edit="editEmployee"
        />

        <AddEmployeeForm 
            @submit="handleAddEmployee" 
            @error="handleError"
        />
    </div>
</template>

<script>
import Breadcrumb from '@/components/breadcrumb/Breadcrumb.vue';
import EmployeeGrid from '@/components/Employee/EmployeeGrid.vue';
import EmployeeDetail from '@/components/Employee/EmployeeDetail.vue';
import AddEmployeeForm from '@/components/Employee/AddEmployeeForm.vue';
import { useEmployees } from '@/composables/useEmployees';
import { ref } from 'vue';

export default {
    name: "EmpList",
    components: {
        Breadcrumb,
        EmployeeGrid,
        EmployeeDetail,
        AddEmployeeForm
    },
    setup() {
        // 使用員工資料 composable
        const {
            employees,
            isLoading,
            error,
            queryParams,
            fetchEmployees,
            searchEmployeesByName,
            filterByStatus,
            resetFilters,
            addEmployee,
            updateEmployeeRoles, // 新增：取得職位更新方法
            roles // 新增：取得角色列表
        } = useEmployees({ autoLoad: true });

        // 開發模式標誌
        const isDev = ref(import.meta.env.DEV);

        // 員工詳情相關
        const showDetail = ref(false);
        const selectedEmployeeId = ref(null);

        /**
         * 處理搜尋事件
         * @param {string} searchText 搜尋關鍵字
         */
        const handleSearch = (searchText) => {
            // 呼叫 composable 的搜尋方法
            searchEmployeesByName(searchText);
        };

        /**
         * 處理狀態篩選事件
         * @param {boolean} isActive 是否篩選啟用的員工
         */
        const handleFilter = (isActive) => {
            // 呼叫 composable 的篩選方法
            filterByStatus(isActive);
        };

        /**
         * 查看員工詳情
         * @param {number} id 員工 ID
         */
        const viewEmployee = (id) => {
            console.log('查看員工詳情:', id);
            selectedEmployeeId.value = id;
            showDetail.value = true;
            // TODO: 實作查看員工詳情的邏輯，例如導航到詳情頁面
            // router.push(`/employee/view/${id}`);
        };

        /**
         * 關閉員工詳情
         */
        const closeDetail = () => {
            showDetail.value = false;
        };

        /**
         * 編輯員工資料
         * @param {number} id 員工 ID
         */
        const editEmployee = (id) => {
            console.log('編輯員工資料:', id);
            // TODO: 實作編輯員工的邏輯，例如導航到編輯頁面
            // router.push(`/employee/edit/${id}`);
        };

        /**
         * 刪除員工
         * @param {number} id 員工 ID
         */
        const deleteEmployee = (id) => {
            console.log('刪除員工:', id);
            // TODO: 實作刪除員工的邏輯，通常需要呼叫 API 並重新載入資料
            // 可以在 employeeService.js 中新增 deleteEmployee 方法
        };

        /**
         * 新增員工
         * @param {Object} employeeData 員工資料
         * @param {string} selectedRole 選擇的職位
         */
        const handleAddEmployee = async ({ employeeData, selectedRole }) => {
            try {
                // 先新增員工
                const newEmployee = await addEmployee(employeeData);
                
                // 如果有選擇職位，則需要另外分配職位
                if (selectedRole && newEmployee.empId) {
                  // 這裡需要根據 roleName 找到對應的 roleId
                  // 然後調用 updateEmployeeRoles
                }
                
                console.log('員工新增成功:', newEmployee);
            } catch (error) {
                console.error('新增員工失敗:', error);
            }
        };

        /**
         * 處理員工職位更新
         * @param {number} empId 員工 ID
         * @param {Array<number>} roleIds 新的職位 ID 陣列
         */
        const handleUpdateEmployeeRoles = async (empId, roleIds) => {
            try {
                await updateEmployeeRoles(empId, roleIds);
                console.log('員工職位更新成功:', { empId, roleIds });
            } catch (error) {
                console.error('更新員工職位失敗:', error);
                throw error; // 重新拋出錯誤，讓子元件處理
            }
        };

        const handleError = (errorMessage) => {
            console.error('表單錯誤:', errorMessage);
        };

        return {
            // 狀態
            employees,
            isLoading,
            error,
            queryParams,
            roles, // 新增：匯出角色列表
            isDev,
            showDetail,
            selectedEmployeeId,

            // 方法
            handleSearch,
            handleFilter,
            viewEmployee,
            closeDetail,
            editEmployee,
            deleteEmployee,
            resetFilters,
            handleAddEmployee,
            handleError,
            handleUpdateEmployeeRoles // 新增：匯出職位更新方法
        };
    }
}
</script>