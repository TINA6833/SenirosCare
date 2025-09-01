<template>
    <div class="card h-100 p-0 radius-12">
        <!-- 卡片標頭和過濾選項 -->
        <div
            class="card-header border-bottom bg-base py-16 px-24 d-flex align-items-center flex-wrap gap-3 justify-content-between">
            <div class="d-flex align-items-center flex-wrap gap-3">
                <span class="text-md fw-medium text-secondary-light mb-0">顯示</span>
                <select v-model="entriesPerPage" class="form-select form-select-sm w-auto ps-12 py-6 radius-12 h-40-px">
                    <option v-for="n in [5, 10, 15, 20]" :key="n" :value="n">{{ n }}</option>
                </select>

                <form class="navbar-search" @submit.prevent="handleSearch">
                    <input v-model="searchQuery" type="text" class="bg-base h-40-px w-auto" name="search"
                        placeholder="搜尋照服員姓名" />
                    <iconify-icon icon="ion:search-outline" class="icon"></iconify-icon>
                </form>

                <select v-model="selectedStatus" class="form-select form-select-sm w-auto ps-12 py-6 radius-12 h-40-px"
                       @change="handleStatusChange">
                    <option value="all">在職狀態</option>
                    <option value="在職">在職</option>
                    <option value="離職">離職</option>
                </select>
            </div>
            <router-link to="/add-care-worker"
                class="btn btn-primary text-sm btn-sm px-12 py-12 radius-8 d-flex align-items-center gap-2">
                <iconify-icon icon="ic:baseline-plus" class="icon text-xl line-height-1"></iconify-icon>
                新增照服員
            </router-link>
        </div>

        <!-- 表格主體 -->
        <div class="card-body p-24">
            <!-- 載入中狀態 -->
            <div v-if="loading" class="text-center py-4">
                <div class="spinner-border text-primary" role="status">
                    <span class="visually-hidden">載入中...</span>
                </div>
                <p class="mt-2">正在載入照服員資料...</p>
            </div>

            <!-- 錯誤訊息 -->
            <div v-else-if="error" class="alert alert-danger">
                <iconify-icon icon="akar-icons:circle-alert" class="me-2"></iconify-icon>
                {{ error }}
                <button class="btn btn-sm btn-outline-danger ms-2" @click="loadCaregivers(true)">重試</button>
            </div>

            <!-- 沒有資料時的提示 -->
            <div v-else-if="filteredCareWorkers.length === 0" class="text-center py-4">
                <iconify-icon icon="carbon:no-data" width="48" height="48" class="text-muted mb-3"></iconify-icon>
                <p>沒有符合條件的照服員資料</p>
                <button v-if="searchQuery || selectedStatus !== 'all'" 
                        class="btn btn-sm btn-outline-primary" 
                        @click="resetSearch">
                    清除搜尋條件
                </button>
            </div>

            <!-- 資料表格 -->
            <div v-else class="table-responsive scroll-sm">
                <table class="table bordered-table sm-table mb-0">
                    <thead>
                        <tr>
                            <th scope="col">
                                <div class="d-flex align-items-center gap-10">
                                    <!-- 批量選擇功能已註解 -->
                                    <!-- <div class="form-check style-check d-flex align-items-center">
                                        <input class="form-check-input radius-4 border input-form-dark" type="checkbox"
                                            v-model="selectAll" @change="toggleSelectAll" />
                                    </div> -->
                                    ID
                                </div>
                            </th>
                            <th scope="col">性別</th>
                            <th scope="col">中文姓名</th>
                            <th scope="col">Email</th>
                            <th scope="col">服務區域</th>
                            <th scope="col" class="text-center">是否在職</th>
                            <th scope="col" class="text-center">操作</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="(careWorker, index) in filteredCareWorkers" :key="careWorker.id">
                            <td>
                                <div class="d-flex align-items-center gap-10">
                                    <!-- 個別選擇功能已註解 -->
                                    <!-- <div class="form-check style-check d-flex align-items-center">
                                        <input class="form-check-input radius-4 border border-neutral-400"
                                            type="checkbox" :value="careWorker" v-model="selectedCareWorkers" />
                                    </div> -->
                                    {{ careWorker.id }}
                                </div>
                            </td>
                            <td><span class="text-md mb-0 fw-normal text-secondary-light">{{ careWorker.genderDisplay }}</span></td>
                            <td>
                                <div class="d-flex align-items-center">
                                    <img :src="getPhotoUrl(careWorker.photo)" alt=""
                                        class="w-40-px h-40-px rounded-circle flex-shrink-0 me-12 overflow-hidden object-fit-cover" />
                                    <div class="flex-grow-1">
                                        <span class="text-md mb-0 fw-normal text-secondary-light">{{ careWorker.chineseName }}</span>
                                    </div>
                                </div>
                            </td>
                            <td><span class="text-md mb-0 fw-normal text-secondary-light">{{ careWorker.email }}</span></td>
                            <td>
                                <span class="px-12 py-4 radius-4 fw-medium text-sm bg-primary-50 text-primary-600 border border-primary-200">
                                    {{ careWorker.serviceArea || '未設定' }}
                                </span>
                            </td>
                            <td class="text-center">
                                <span :class="[
                                    'px-24 py-4 radius-4 fw-medium text-sm border',
                                    careWorker.isActive ? 'bg-success-focus text-success-600 border-success-main' : 'bg-neutral-200 text-neutral-600 border-neutral-400'
                                ]">
                                    {{ careWorker.statusDisplay }}
                                </span>
                            </td>
                            <td class="text-center">
                                <div class="d-flex align-items-center gap-10 justify-content-center">
                                    <!-- 查看詳細資料按鈕 -->
                                    <button type="button"
                                        class="bg-info-focus bg-hover-info-200 text-info-600 fw-medium w-40-px h-40-px d-flex justify-content-center align-items-center rounded-circle"
                                        @click="viewCareWorker(careWorker)">
                                        <iconify-icon icon="majesticons:eye-line" class="icon text-xl"></iconify-icon>
                                    </button>
                                    <!-- 編輯按鈕 -->
                                    <button type="button"
                                        class="bg-success-focus text-success-600 bg-hover-success-200 fw-medium w-40-px h-40-px d-flex justify-content-center align-items-center rounded-circle"
                                        @click="editCareWorker(careWorker)">
                                        <iconify-icon icon="lucide:edit" class="menu-icon"></iconify-icon>
                                    </button>
                                    <!-- 刪除按鈕 -->
                                    <button type="button"
                                        class="remove-item-btn bg-danger-focus bg-hover-danger-200 text-danger-600 fw-medium w-40-px h-40-px d-flex justify-content-center align-items-center rounded-circle"
                                        @click="deleteCareWorker(careWorker)">
                                        <iconify-icon icon="fluent:delete-24-regular" class="menu-icon"></iconify-icon>
                                    </button>
                                </div>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>

            <!-- 分頁元件 -->
            <Pagination v-if="filteredCareWorkers.length > 0"
                :currentPage="currentPage" 
                :totalPages="totalPages" 
                :startIndex="startIndex"
                :endIndex="endIndex" 
                :totalItems="totalEntries" 
                @page-changed="changePage" />
        </div>
    </div>
</template>

<script setup>
// 匯入必要的函式和元件
import { ref, computed, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import Pagination from '@/components/pagination/index.vue';
import { useCaregivers } from '@/composables/useCaregivers';
import defaultAvatar from "@/assets/images/user-list/user-list1.png";
import { useToast } from '@/composables/useToast'; // 引入 toast 元件

// 使用路由
const router = useRouter();

// 使用照服員 composable 取得狀態和方法
const { caregivers, loading, error, filters, loadCaregivers, searchCaregivers, deleteCaregiver } = useCaregivers();

// 分頁和篩選狀態
const currentPage = ref(1);
const entriesPerPage = ref(10);
const searchQuery = ref('');
const selectedStatus = ref('all');
// 批量選擇功能已註解
// const selectedCareWorkers = ref([]);
const { showToast } = useToast(); // 使用 Toast 通知功能

// 當元件掛載時載入資料
onMounted(() => {
  loadCaregivers();
});

// 監聽搜尋條件變化
watch(searchQuery, (newVal, oldVal) => {
  if (newVal !== oldVal) {
    // 更新 filters 中的 name
    filters.name = newVal;
  }
});

// 當搜尋條件、分頁設定變更時重置頁碼
watch([entriesPerPage], () => {
  currentPage.value = 1;
});

// 處理搜尋
const handleSearch = () => {
  filters.name = searchQuery.value;
  searchCaregivers();
  currentPage.value = 1;
};

// 處理狀態變更
const handleStatusChange = () => {
  if (selectedStatus.value === 'all') {
    filters.isActive = null;
  } else if (selectedStatus.value === '在職') {
    filters.isActive = true;
  } else {
    filters.isActive = false;
  }
  searchCaregivers();
  currentPage.value = 1;
};

// 重置搜尋條件
const resetSearch = () => {
  searchQuery.value = '';
  selectedStatus.value = 'all';
  filters.isActive = null;
  filters.name = '';
  searchCaregivers();
  currentPage.value = 1;
};

// 批量選擇功能已註解 - 全選狀態
// const selectAll = computed({
//   get() {
//     return filteredCareWorkers.value.length > 0 && selectedCareWorkers.value.length === filteredCareWorkers.value.length;
//   },
//   set(value) {
//     if (value) {
//       selectedCareWorkers.value = [...filteredCareWorkers.value];
//     } else {
//       selectedCareWorkers.value = [];
//     }
//   }
// });

// 計算屬性：過濾後的照服員列表（分頁處理）
const filteredCareWorkers = computed(() => {
  const start = (currentPage.value - 1) * entriesPerPage.value;
  const end = start + entriesPerPage.value;
  
  // 這裡直接使用 caregivers 資料，因為 API 層面已經處理過篩選
  // 如果有本地額外篩選需求，可以在這裡添加
  return caregivers.value.slice(start, end);
});

// 計算屬性：總筆數
const totalEntries = computed(() => {
  return caregivers.value.length;
});

// 計算屬性：總頁數
const totalPages = computed(() => {
  return Math.ceil(totalEntries.value / entriesPerPage.value);
});

// 計算屬性：分頁起始索引
const startIndex = computed(() => {
  return totalEntries.value === 0 ? 0 : (currentPage.value - 1) * entriesPerPage.value + 1;
});

// 計算屬性：分頁結束索引
const endIndex = computed(() => {
  return Math.min(startIndex.value + entriesPerPage.value - 1, totalEntries.value);
});

// 批量選擇功能已註解 - 切換全選狀態
// const toggleSelectAll = () => {
//   if (selectAll.value) {
//     selectedCareWorkers.value = [...filteredCareWorkers.value];
//   } else {
//     selectedCareWorkers.value = [];
//   }
// };

// 方法：查看照服員詳細資料
const viewCareWorker = (careWorker) => {
  router.push(`/care-worker/${careWorker.id}`);
};

// 方法：編輯照服員資料
const editCareWorker = (careWorker) => {
  router.push(`/care-worker/edit/${careWorker.id}`);
};

// 方法：刪除照服員資料
const deleteCareWorker = async (careWorker) => {
  if (confirm(`確定要刪除照服員 ${careWorker.chineseName} 的資料嗎？`)) {
    try {
      // 呼叫 deleteCaregiver API
      await deleteCaregiver(careWorker.id);
      // 成功後可顯示通知
      showToast({
        title: '刪除成功',
        message: `刪除照服員: ${careWorker.chineseName}`,
        type: 'success'
      });
    } catch (err) {
      showToast({
        title: '刪除失敗 !',
        message: err.message,
        type: 'error'
      });
    }
  }
};

// 方法：切換頁面
const changePage = (page) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page;
  }
};

// 方法：處理照片 URL
// 在 CaregiverTable.vue 和 EditCareWorker.vue 中
const getPhotoUrl = (photoPath) => {

  
  if (!photoPath) {
    return defaultAvatar;
  }
  
  // 如果是完整的 URL
  if (photoPath.startsWith('http://') || photoPath.startsWith('https://')) {
    return photoPath;
  }
  
  // 如果是 Base64 編碼的圖片數據
  if (photoPath.startsWith('data:image')) {
    return photoPath;
  }
  
  // 統一處理相對路徑
  // 移除可能的前導斜線，然後加上正確的基礎 URL
  const cleanPath = photoPath.startsWith('/') ? photoPath : '/' + photoPath;
  const fullPath = `http://localhost:8080${cleanPath}`;
  

  return fullPath;
};
</script>