<template>
    <div class="card h-100 p-0 radius-12">
        <!-- 表格功能區 - 保持與 EmployeeTable 相同的功能 -->
        <div
            class="card-header border-bottom bg-base py-16 px-24 d-flex align-items-center flex-wrap gap-3 justify-content-between">
            <div class="d-flex align-items-center flex-wrap gap-3">
                <span class="text-md fw-medium text-secondary-light mb-0">顯示</span>
                <select class="form-select form-select-sm w-auto ps-12 py-6 radius-12 h-40-px" v-model="itemsPerPage">
                    <option value="8">8</option>
                    <option value="12">12</option>
                    <option value="16">16</option>
                    <option value="20">20</option>
                </select>

                <!-- 狀態篩選按鈕 -->
                <div class="btn-group ms-3" role="group">
                    <button @click="filterByActive(true)" type="button" class="btn btn-sm"
                        :class="isActive ? 'btn-primary' : 'btn-outline-primary'">
                        啟用員工
                    </button>
                    <button @click="filterByActive(false)" type="button" class="btn btn-sm"
                        :class="!isActive ? 'btn-primary' : 'btn-outline-primary'">
                        停用員工
                    </button>
                </div>

                <!-- 搜尋框 -->
                <form class="navbar-search">
                    <input type="text" name="search" placeholder="搜尋員工或職位" v-model="searchText" @input="handleSearch"
                        @focus="isSearchFocused = true" @blur="isSearchFocused = false" />
                    <iconify-icon icon="ion:search-outline" class="icon"></iconify-icon>
                </form>
            </div>
        </div>

        <div class="card-body p-24">
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
            <div v-else-if="paginatedEmployees.length === 0" class="alert alert-info text-center">
                <h5>無員工資料</h5>
                <p>目前查詢條件下沒有員工資料。</p>
                <button @click="resetFilters" class="btn btn-outline-primary">重設篩選條件</button>
            </div>

            <!-- 員工圖卡網格 - 調整為一排四個 -->
            <template v-else>
                <div class="row gy-3 gx-3">
                    <div v-for="employee in paginatedEmployees" :key="employee.empId"
                        class="col-xl-3 col-lg-4 col-md-6 col-sm-6 user-grid-card">
                        <div class="position-relative border radius-16 overflow-hidden bg-white">
                            <!-- 背景圖片區域 - 使用隨機背景圖片 -->
                            <div class="user-background-section position-relative"
                                :style="{ backgroundImage: `url(${getRandomBackgroundImage(employee.empId)})` }">
                                <!-- 移除操作選單和狀態標籤 -->
                            </div>

                            <!-- 員工資訊區域 - 調整間距 -->
                            <div class="ps-12 pb-12 pe-12 text-center employee-info-section">
                                <!-- 員工頭像 - 縮小尺寸 -->
                                <div class="employee-avatar-container">
                                    <img :src="getEmployeeImage(employee.imagePath)" :alt="employee.empName"
                                        @error="handleImageError"
                                        class="employee-avatar border border-white border-3 rounded-circle object-fit-cover" />
                                </div>

                                <!-- 員工姓名和郵件 - 調整字體大小 -->
                                <h6 class="text-md mb-0 mt-2">{{ employee.empName }}</h6>
                                <span class="text-secondary-light mb-12 d-block text-sm">{{ employee.email || 'N/A'
                                    }}</span>

                                <!-- 部門和職位資訊 - 修改邏輯和布局 -->
                                <div
                                    class="center-border position-relative bg-primary-gradient-light radius-8 p-8 d-flex align-items-center gap-2 mb-12">
                                    <!-- 左側：部門資訊 -->
                                    <div class="text-center w-50">
                                        <span class="text-secondary-light text-xs mb-0">Department</span>
                                        <h6 class="text-sm mb-0">{{ getDepartmentName(employee.roles) }}</h6>
                                    </div>
                                    <!-- 右側：職位標籤 -->
                                    <div class="text-center w-50 d-flex flex-column align-items-center gap-1">
                                        <span class="text-secondary-light text-xs mb-0 mt-1">Role</span>
                                        <span v-for="role in employee.roles" :key="role.roleId || role.role_id"
                                            :class="getRoleClass(role.role_name)"
                                            class="px-6 py-1 rounded-pill fw-medium text-xxs role-badge">
                                            {{ formatRoleLabel(role.role_name) }}
                                        </span>
                                        <span v-if="!employee.roles || employee.roles.length === 0"
                                            class="px-6 py-1 rounded-pill fw-medium text-xxs bg-secondary text-white role-badge">
                                            無職位
                                        </span>
                                    </div>
                                </div>

                                <!-- View Profile 按鈕 - 調整按鈕大小 -->
                                <button type="button" @click="viewEmployee(employee.empId)"
                                    class="bg-primary-50 text-primary-600 bg-hover-primary-600 hover-text-white p-8 text-xs btn-sm px-8 py-8 radius-6 d-flex align-items-center justify-content-center fw-medium gap-1 w-100 border-0">
                                    查看詳情
                                    <iconify-icon icon="solar:alt-arrow-right-linear"
                                        class="icon text-sm line-height-1"></iconify-icon>
                                </button>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- 分頁區域 -->
                <div class="d-flex flex-wrap align-items-center justify-content-between gap-2 mt-24">
                    <span>
                        顯示 {{ startIndex + 1 }} 至 {{ endIndex }} 筆，共 {{ totalEntries }} 筆資料
                    </span>
                    <ul class="pagination d-flex flex-wrap align-items-center gap-2 justify-content-center">
                        <!-- 首頁按鈕 -->
                        <li class="page-item">
                            <a class="page-link text-secondary-light fw-medium radius-4 border-0 px-10 py-10 d-flex align-items-center justify-content-center h-32-px w-32-px bg-base"
                                href="javascript:void(0)" @click="goToPage(1)" :class="{ disabled: currentPage === 1 }">
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
</template>

<script>
import { ref, computed, watch, onMounted } from 'vue';
// 引入預設頭像圖片
import defaultAvatarImage from '@/assets/images/user-list/user-list1.png';

// 引入所有背景圖片
import userBackground from '@/assets/images/user-grid/user-background.png';
import userBackground2 from '@/assets/images/user-grid/user-list2.png';
import userBackground3 from '@/assets/images/user-grid/user-list3.png';
import userBackground4 from '@/assets/images/user-grid/user-list4.png';
import userBackground5 from '@/assets/images/user-grid/user-list5.png';

export default {
    name: "UserGrid",
    props: {
        // 員工資料陣列 - 與 EmployeeTable 相同的 props
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
    // 與 EmployeeTable 相同的事件定義
    emits: ['search', 'filter', 'view', 'edit', 'delete', 'reset'],
    setup(props, { emit }) {
        // 分頁相關參數 - 調整預設顯示數量為16個（4x4網格）
        const itemsPerPage = ref(16);
        const currentPage = ref(1); // 目前頁數

        // 搜尋與篩選相關參數 - 與 EmployeeTable 保持一致
        const searchText = ref(''); // 搜尋文字
        const isActive = ref(true); // 啟用狀態篩選
        const isSearchFocused = ref(false); // 搜尋框聚焦狀態
        // 預設頭像
        const defaultAvatar = defaultAvatarImage;

        // 背景圖片陣列 - 包含所有可用的背景圖片
        const backgroundImages = [
            userBackground,
            userBackground2,
            userBackground3,
            userBackground4,
            userBackground5
        ];

        /**
         * 根據員工ID獲取隨機背景圖片
         * @param {number} empId - 員工ID
         * @returns {string} 背景圖片URL
         */
        const getRandomBackgroundImage = (empId) => {
            // 使用員工ID作為種子來確保每個員工的背景圖片固定
            // 這樣可以避免重新渲染時背景圖片改變
            const index = empId % backgroundImages.length;
            return backgroundImages[index];
        };

        /**
         * 取得員工圖片 URL - 與 EmployeeTable 相同的邏輯
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
         * 圖片載入錯誤處理 - 與 EmployeeTable 相同
         * @param {Event} event - 錯誤事件
         */
        const handleImageError = (event) => {
            console.log('圖片載入失敗，使用預設頭像');
            event.target.src = defaultAvatar;
        };

        /**
         * 根據職位判斷部門名稱
         * @param {Array} roles - 職位陣列
         * @returns {string} 部門名稱
         */
        const getDepartmentName = (roles) => {
            // 檢查是否有 ADMIN 職位
            const hasAdmin = roles && roles.some(role => role.role_name === 'ROLE_ADMIN');

            // 如果有 ADMIN 職位，顯示 IT 部門，否則顯示客服部門
            return hasAdmin ? 'IT' : '客服';
        };

        /**
         * 取得主要職位名稱用於顯示
         * @param {Array} roles - 職位陣列
         * @returns {string} 格式化的職位名稱
         */
        const getMainRole = (roles) => {
            if (!roles || roles.length === 0) return 'Employee';

            // 優先顯示管理員和經理職位
            const priorityRoles = roles.find(role =>
                role.role_name === 'ROLE_ADMIN' || role.role_name === 'ROLE_MANAGER'
            );

            if (priorityRoles) {
                return formatRoleLabel(priorityRoles.role_name);
            }

            // 否則顯示第一個職位
            return formatRoleLabel(roles[0].role_name);
        };

        /**
         * 根據職位類型返回對應的樣式類別 - 與 EmployeeTable 相同
         * @param {string} role 職位名稱
         * @returns {string} CSS 類別字串
         */
        const getRoleClass = (role) => {
            switch (role) {
                case 'ROLE_ADMIN':
                    return 'bg-danger text-white';
                case 'ROLE_MANAGER':
                    return 'bg-warning text-white';
                case 'ROLE_EMPLOYEE':
                    return 'bg-success text-white';
                default:
                    return 'bg-secondary text-white';
            }
        };

        /**
         * 格式化職位標籤顯示 - 與 EmployeeTable 相同
         * @param {string} role 職位名稱
         * @returns {string} 格式化後的職位名稱
         */
        const formatRoleLabel = (role) => {
            if (!role) return '';
            return role.startsWith('ROLE_') ? role.replace('ROLE_', '') : role;
        };

        /**
         * 設定狀態標籤的樣式 - 與 EmployeeTable 相同
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
         * 計算篩選後的員工資料 - 與 EmployeeTable 相同的邏輯
         */
        const filteredEmployees = computed(() => {
            let result = [...props.employees];

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

            return result;
        });

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
        const totalPages = computed(() => Math.ceil(totalEntries.value / itemsPerPage.value) || 1);

        /**
         * 目前頁面的開始索引
         */
        const startIndex = computed(() => (currentPage.value - 1) * itemsPerPage.value);

        /**
         * 目前頁面的結束索引
         */
        const endIndex = computed(() => Math.min(startIndex.value + itemsPerPage.value, totalEntries.value));

        /**
         * 顯示的頁碼範圍 (最多顯示5頁) - 與 EmployeeTable 相同
         */
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
         * 處理搜尋事件 - 與 EmployeeTable 相同
         */
        const handleSearch = () => {
            currentPage.value = 1; // 重置頁碼
        };

        /**
         * 查看員工詳情 - 觸發 EmployeeDetail 彈窗
         * @param {number} empId 員工ID
         */
        const viewEmployee = (empId) => {
            emit('view', empId);
        };

        /**
         * 編輯員工資料 - 與 EmployeeTable 相同
         * @param {number} empId 員工 ID
         */
        const editEmployee = (empId) => {
            emit('edit', empId);
        };

        /**
         * 確認刪除員工 - 與 EmployeeTable 相同
         * @param {number} empId 員工 ID
         */
        const confirmDelete = (empId) => {
            if (window.confirm('確定要刪除此員工資料嗎？此操作無法恢復。')) {
                emit('delete', empId);
            }
        };

        /**
         * 切換頁碼 - 與 EmployeeTable 相同
         * @param {number} page 目標頁碼
         */
        const goToPage = (page) => {
            if (page >= 1 && page <= totalPages.value) {
                currentPage.value = page;
            }
        };

        /**
         * 根據啟用狀態篩選 - 與 EmployeeTable 相同
         * @param {boolean} active 是否篩選啟用的員工
         */
        const filterByActive = (active) => {
            isActive.value = active;
            currentPage.value = 1;
            emit('filter', active);
        };

        /**
         * 重設所有篩選條件 - 與 EmployeeTable 相同
         */
        const resetFilters = () => {
            searchText.value = '';
            isActive.value = true;
            currentPage.value = 1;
            emit('reset');
        };

        // 監聽每頁顯示數量變更
        watch(itemsPerPage, () => {
            currentPage.value = 1;
        });

        // 監聽員工資料變更
        watch(() => props.employees, () => {
            if (currentPage.value > totalPages.value) {
                currentPage.value = Math.max(1, totalPages.value);
            }
        }, { deep: true });

        return {
            // 狀態參數
            itemsPerPage,
            currentPage,
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
            getRandomBackgroundImage,

            // 資料處理方法
            getDepartmentName,
            getMainRole,
            getRoleClass,
            formatRoleLabel,
            statusClasses,

            // 事件處理方法
            handleSearch,
            viewEmployee,
            editEmployee,
            confirmDelete,
            goToPage,
            filterByActive,
            resetFilters
        };
    }
};
</script>

<style scoped>
/* 員工卡片背景圖片區域 - 移除固定背景圖片 */
.user-background-section {
    height: 80px;
    /* 減小背景區域高度 */
    background-size: cover;
    background-position: center;
    background-repeat: no-repeat;
    /* 移除固定的 background-image，改為使用動態 style */
}

/* 員工資訊區域 - 調整間距以容納頭像 */
.employee-info-section {
    margin-top: -40px;
    /* 調整向上移動距離 */
}

/* 員工頭像容器 - 調整 z-index 避免覆蓋其他元件 */
.employee-avatar-container {
    position: relative;
    z-index: 2;
    /* 降低 z-index，只需要高於背景圖片即可 */
    margin-bottom: 0;
}

/* 員工頭像樣式 - 縮小尺寸 */
.employee-avatar {
    width: 70px;
    /* 縮小頭像寬度 */
    height: 70px;
    /* 縮小頭像高度 */
    object-fit: cover;
}

/* 職位標籤樣式 - 確保上下排列和適當間距 */
.role-badge {
    width: fit-content;
    max-width: 100%;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

/* 分頁按鈕樣式 */
.page-link.disabled {
    opacity: 0.5;
    pointer-events: none;
}

/* 員工頭像圖片樣式 */
img {
    object-fit: cover;
}

/* 狀態標籤樣式 */
.bg-success-focus {
    background-color: rgba(34, 197, 94, 0.1);
}

.text-success-main {
    color: #22c55e;
}

/* 職位標籤間距 */
.user-grid-card .rounded-pill {
    margin-bottom: 0.125rem;
    /* 減小職位標籤間距 */
}

/* 卡片懸停效果 */
.user-grid-card .border:hover {
    box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
    transform: translateY(-2px);
    transition: all 0.3s ease;
}

/* 按鈕懸停效果 */
.bg-hover-primary-600:hover {
    background-color: #3b82f6 !important;
    color: white !important;
}

.hover-text-white:hover {
    color: white !important;
}

/* 卡片整體樣式 - 確保卡片不會影響其他元件 */
.user-grid-card .border {
    border: 1px solid #e5e7eb;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
    position: relative;
    /* 確保卡片本身的定位上下文 */
    z-index: 1;
    /* 設定卡片的基礎層級 */
}

/* 響應式設計確保一排四個 */
@media (min-width: 1200px) {
    .user-grid-card {
        max-width: 25%;
        /* 確保一排四個 */
        flex: 0 0 25%;
    }
}

/* 調整字體大小的額外樣式 */
.text-xxs {
    font-size: 0.625rem;
    /* 10px - 超小字體 */
}

/* 確保背景圖片區域的層級正確 */
.user-background-section {
    position: relative;
    z-index: 1;
    /* 背景圖片的基礎層級 */
}
</style>