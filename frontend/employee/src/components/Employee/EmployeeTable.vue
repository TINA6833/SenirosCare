<template>
    <div class="card basic-data-table">
        <!-- 表格功能區 -->
        <div class="card">
            <div class="card-header d-flex flex-wrap align-items-center justify-content-between gap-3"
                style="border-bottom: none; padding-bottom: 0px;">

                <!-- 每頁顯示筆數選擇器 -->
                <div class="d-flex flex-wrap align-items-center gap-3">
                    <div class="d-flex align-items-center gap-2">
                        <select class="form-select form-select-lr w-auto rounded-3 me-10" v-model="selectedShow"
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
                        <input type="text" name="search" placeholder="搜尋員工或職位" v-model="searchText"
                            @input="handleSearch" @focus="isSearchFocused = true" @blur="isSearchFocused = false" />
                        <iconify-icon icon="ion:search-outline" class="icon"></iconify-icon>
                    </form>
                </div>
            </div>

            <!-- 表格主體 -->
            <div class="card-body">
                <!-- 載入中顯示 -->
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
                                <!-- 修正：使用正確的欄位名稱 empId -->
                                <th scope="col">ID</th>
                                <th scope="col" @click="sortBy('empName')" class="sortable">
                                    姓名
                                    <span v-if="sortKey === 'empName'">
                                        <iconify-icon
                                            :icon="sortAsc ? 'mdi:arrow-up' : 'mdi:arrow-down'"></iconify-icon>
                                    </span>
                                </th>
                                <th scope="col" @click="sortBy('email')" class="sortable">
                                    電子郵件
                                    <span v-if="sortKey === 'email'">
                                        <iconify-icon
                                            :icon="sortAsc ? 'mdi:arrow-up' : 'mdi:arrow-down'"></iconify-icon>
                                    </span>
                                </th>
                                <th scope="col" @click="sortBy('roles')" class="sortable">
                                    職位
                                    <span v-if="sortKey === 'roles'">
                                        <iconify-icon
                                            :icon="sortAsc ? 'mdi:arrow-up' : 'mdi:arrow-down'"></iconify-icon>
                                    </span>
                                </th>
                                <th scope="col" @click="sortBy('isActive')" class="sortable">
                                    狀態
                                    <span v-if="sortKey === 'isActive'">
                                        <iconify-icon
                                            :icon="sortAsc ? 'mdi:arrow-up' : 'mdi:arrow-down'"></iconify-icon>
                                    </span>
                                </th>
                                <th scope="col">
                                    操作
                                </th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr v-for="(employee, index) in paginatedEmployees" :key="employee.empId">
                                <!-- 修正：使用 empId 而非 id -->
                                <td>{{ employee.empId }}</td>
                                <!-- 姓名列 - 修正：使用 empName 和 imagePath -->
                                <td>
                                    <div class="d-flex align-items-center">
                                        <!-- 顯示員工頭像，使用 imagePath 或預設圖片 -->
                                        <img :src="getEmployeeImage(employee.imagePath)" alt="員工頭像"
                                            class="flex-shrink-0 me-12 radius-8" width="40" height="40"
                                            @error="handleImageError">
                                        <h6 class="text-md mb-0 fw-medium flex-grow-1">{{ employee.empName }}</h6>
                                    </div>
                                </td>
                                <!-- 電子郵件列 -->
                                <td>{{ employee.email || 'N/A' }}</td>
                                <!-- 職位列 - 修正：處理多個職位的情況 -->
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
                                    <span v-else
                                        class="px-12 py-2 rounded-pill fw-medium text-sm bg-secondary text-white">
                                        無職位
                                    </span>
                                </td>
                                <!-- 狀態列 - 修正：使用 isActive -->
                                <td>
                                    <span :class="statusClasses(employee.isActive)"
                                        class="px-24 py-4 rounded-pill fw-medium text-sm">
                                        {{ employee.isActive === 'active' ? '啟用' : '停用' }}
                                    </span>
                                </td>
                                <!-- 操作列 - 修正：使用 empId -->
                                <td>
                                    <!-- 查看按鈕 - 使用眼睛圖示 -->
                                    <button @click="viewEmployee(employee.empId)" type="button"
                                        class="btn btn-icon btn-sm bg-primary-light text-primary-600 rounded-circle"
                                        title="查看員工詳細資料">
                                        <iconify-icon icon="iconamoon:eye-light" width="20" height="20"></iconify-icon>
                                    </button>
                                </td>
                            </tr>
                        </tbody>
                    </table>

                    <!-- 分頁區域 (保持不變) -->
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

        <!-- 註解：移除此處的 EmployeeDetail 元件。彈窗將由父層 empList.vue 統一管理 -->
        <!-- 
        <EmployeeDetail
            :visible="isDetailVisible"
            :employee-id="selectedEmployeeId"
            @close="closeEmployeeDetail"
            @edit="handleEditEmployee"
        /> 
        -->
    </div>
</template>

<script>
import { ref, computed, watch, onMounted } from 'vue';
// 註解：移除對 EmployeeDetail 的引入，因為此元件不再直接使用它
// import EmployeeDetail from './EmployeeDetail.vue';
import { useEmployees } from '@/composables/useEmployees';
// 引入預設頭像圖片
import defaultAvatarImage from '@/assets/images/user-list/user-list1.png';

export default {
    name: 'EmployeeTable',
    // 註解：從 components 中移除 EmployeeDetail
    components: {
        // EmployeeDetail
    },
    props: {
        // 員工資料陣列
        employees: {
            type: Array,
            required: true
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
        }
    },
    emits: ['search', 'filter', 'view', 'edit', 'delete', 'reset'],
    setup(props, { emit }) {
        // 分頁相關參數
        const selectedShow = ref('10'); // 每頁顯示筆數
        const currentPage = ref(1); // 目前頁數

        // 排序相關參數 - 修正：使用正確的欄位名稱
        const sortKey = ref('empId'); // 排序欄位
        const sortAsc = ref(true); // 排序方向 (true: 升冪, false: 降冪)

        // 註解：宣告一個變數來存放 debounce 的計時器 ID
        let searchTimeout = null;

        // 搜尋與篩選相關參數
        const searchText = ref(''); // 搜尋文字
        const isActive = ref(true); // 啟用狀態篩選 (true: 顯示啟用, false: 顯示停用)
        const isSearchFocused = ref(false); // 搜尋框聚焦狀態

        // 預設頭像
        const defaultAvatar = defaultAvatarImage;

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
            return roles.map(role => role.role_name).join(', ');
        };

        /**
         * 查看員工詳情
         * @param {number|string} empId 員工ID
         */
        const viewEmployee = (empId) => {
            // 註解：移除本地控制彈窗的邏輯，只保留發送 'view' 事件給父元件
            emit('view', empId);
        };


        /**
         * 處理從詳情元件發來的編輯請求
         * @param {number|string} empId 員工ID
         */
        const handleEditEmployee = (empId) => {
            // 註解：這個函式現在由父元件處理，但保留 emit 以維持功能
            emit('edit', empId);
            // isDetailVisible.value = false; // 移除本地狀態控制
        };

        /**
         * 處理搜尋事件
         */
        const handleSearch = () => {
            currentPage.value = 1; // 重置頁碼
        };

        /**
         * 處理資料排序
         * @param {string} key 排序欄位名稱
         */
        const sortBy = (key) => {
            // 如果點選同一欄位，則反轉排序方向
            if (sortKey.value === key) {
                sortAsc.value = !sortAsc.value;
            } else {
                // 否則設定排序欄位並預設升冪排序
                sortKey.value = key;
                sortAsc.value = true;
            }
        };

        /**
         * 計算篩選後的員工資料
         */
        const filteredEmployees = computed(() => {
            // 複製一份原始資料以防改變原始陣列
            let result = [...props.employees];

            // 根據搜尋文字篩選 - 修正：使用正確的欄位名稱
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
                        // 數字類型欄位
                        valA = Number(a[sortKey.value]);
                        valB = Number(b[sortKey.value]);
                    } else if (sortKey.value === 'roles') {
                        // 職位欄位 - 使用第一個職位的名稱進行排序
                        valA = a.roles && a.roles.length > 0 ? a.roles[0].roleName.toLowerCase() : '';
                        valB = b.roles && b.roles.length > 0 ? b.roles[0].roleName.toLowerCase() : '';
                    } else if (sortKey.value === 'isActive') {
                        // 狀態欄位 - 將 active/inactive 轉為數字比較
                        valA = a[sortKey.value] === 'active' ? 1 : 0;
                        valB = b[sortKey.value] === 'active' ? 1 : 0;
                    } else {
                        // 字串類型欄位 (確保非 null 且可轉成字串)
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

        /**
         * 每頁顯示筆數
         */
        const entriesPerPage = computed(() => Number(selectedShow.value));

        /**
         * 分頁後的員工資料
         */
        const paginatedEmployees = computed(() => {
            return filteredEmployees.value.slice(startIndex.value, endIndex.value);
        });

        /**
         * 資料總筆數
         */
        const totalEntries = computed(() => filteredEmployees.value.length);

        /**
         * 總頁數
         */
        const totalPages = computed(() => Math.ceil(totalEntries.value / entriesPerPage.value) || 1);

        /**
         * 目前頁面的開始索引
         */
        const startIndex = computed(() => (currentPage.value - 1) * entriesPerPage.value);

        /**
         * 目前頁面的結束索引
         */
        const endIndex = computed(() => Math.min(startIndex.value + entriesPerPage.value, totalEntries.value));

        /**
         * 顯示的頁碼範圍 (最多顯示5頁)
         */
        const displayedPages = computed(() => {
            // 頁數太少時直接返回全部頁碼
            if (totalPages.value <= 5) {
                return [...Array(totalPages.value)].map((_, i) => i + 1);
            }

            // 計算顯示的頁碼範圍
            let start = currentPage.value - 2;
            let end = currentPage.value + 2;

            // 調整頁碼範圍
            if (start < 1) {
                end += (1 - start);
                start = 1;
            }

            if (end > totalPages.value) {
                start -= (end - totalPages.value);
                end = totalPages.value;
            }

            start = Math.max(1, start);

            // 產生頁碼陣列
            return [...Array(end - start + 1)].map((_, i) => start + i);
        });


        /**
         * 設定狀態標籤的樣式 - 修正：使用正確的狀態值
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
         * 根據職位類型返回對應的樣式類別
         * @param {string} role 職位名稱
         * @returns {string} CSS 類別字串
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
         * 格式化職位標籤顯示 - 去除 ROLE_ 前綴
         * @param {string} role 職位名稱
         * @returns {string} 格式化後的職位名稱
         */
        const formatRoleLabel = (role) => {
            if (!role) return '';
            // 去除 ROLE_ 前綴並返回
            return role.startsWith('ROLE_') ? role.replace('ROLE_', '') : role;
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
         * 根據啟用狀態篩選
         * @param {boolean} active 是否篩選啟用的員工
         */
        const filterByActive = (active) => {
            isActive.value = active;
            // 重置頁碼
            currentPage.value = 1;
            emit('filter', active);
        };

        /**
         * 重設所有篩選條件
         */
        const resetFilters = () => {
            searchText.value = '';
            isActive.value = true;
            currentPage.value = 1;
            emit('reset');
        };


        /**
         * 編輯員工資料
         * @param {number} empId 員工 ID
         */
        const editEmployee = (empId) => {
            emit('edit', empId);
        };

        /**
         * 確認刪除員工
         * @param {number} empId 員工 ID
         */
        const confirmDelete = (empId) => {
            if (window.confirm('確定要刪除此員工資料嗎？此操作無法恢復。')) {
                emit('delete', empId);
            }
        };

        // 監聽每頁顯示數量變更
        watch(selectedShow, () => {
            currentPage.value = 1; // 重設為第一頁
        });

        // 監聽員工資料變更，檢查是否需要更新頁碼
        watch(() => props.employees, () => {
            // 如果當前頁碼超出總頁數，則重置為第一頁
            if (currentPage.value > totalPages.value) {
                currentPage.value = Math.max(1, totalPages.value);
            }
        }, { deep: true });

        // 元件掛載時的初始化邏輯
        onMounted(() => {
            // 若有需要的初始化邏輯可放置於此
        });

        return {
            // 狀態參數
            selectedShow,
            currentPage,
            sortKey,
            sortAsc,
            // 註解：移除不再需要的本地狀態和函式
            // isDetailVisible,
            // selectedEmployeeId,
            viewEmployee,
            // closeEmployeeDetail,
            handleEditEmployee,
            searchText,
            isActive,
            isSearchFocused,

            // 計算屬性
            filteredEmployees,
            paginatedEmployees,
            totalEntries,
            totalPages,
            startIndex,
            endIndex,
            displayedPages,

            // 圖片處理方法
            getEmployeeImage,
            handleImageError,
            getFullRolesList,

            // 方法
            sortBy,
            handleSearch,
            statusClasses,
            getRoleClass,
            formatRoleLabel,  // 新增這行
            goToPage,
            filterByActive,
            resetFilters,
            editEmployee,
            confirmDelete
        };
    }
};
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
</style>