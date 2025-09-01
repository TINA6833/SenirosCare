<!-- filepath: d:\git\SeniorsCare\frontend\employee\src\components\Activity\ActivityReservationList.vue -->
<template>
  <div class="card basic-data-table">
    <!-- 表格功能區 -->
    <div class="card">
      <div class="card-header d-flex flex-wrap align-items-center justify-content-between gap-3"
        style="border-bottom: none; padding-bottom: 0px;">

        <!-- 每頁顯示筆數選擇器和統計資訊 -->
        <div class="d-flex flex-wrap align-items-center gap-3">
          <div class="d-flex align-items-center gap-2">
            <select class="form-select form-select-lr w-auto rounded-3 me-10" v-model="itemsPerPage"
              style="border-radius: 10px; height: 2.4rem;">
              <option value="10">10</option>
              <option value="15">15</option>
              <option value="20">20</option>
              <option value="50">50</option>
            </select>
            <span>筆/頁</span>
          </div>

          <!-- 重點註解：統計資訊顯示 - 放大按鈕尺寸 -->
          <div class="d-flex align-items-center gap-3" v-if="registrations.length > 0">
            <span class="badge bg-primary status-badge">總報名: {{ totalCount }}</span>
            <span class="badge bg-warning status-badge">待審核: {{ pendingCount }}</span>
            <span class="badge bg-success status-badge">已通過: {{ approvedCount }}</span>
            <span class="badge bg-danger status-badge">已取消: {{ cancelledCount }}</span>
          </div>
        </div>

        <!-- 操作區塊 -->
        <div class="d-flex align-items-center gap-3">

          <!-- 重點註解：活動類別篩選下拉選單 - 移除標籤文字 -->
          <div class="d-flex align-items-center gap-2">
            <select 
              class="form-select form-select-sm" 
              v-model="selectedCategoryName" 
              @change="handleCategoryChange"
              style="min-width: 150px;"
            >
              <option value="">全部類別</option>
              <option 
                v-for="option in categoryOptions" 
                :key="option.value" 
                :value="option.value"
                :disabled="option.disabled"
              >
                {{ option.label }}
              </option>
            </select>
          </div>

          <!-- 重新整理按鈕 -->
          <button 
            type="button" 
            class="btn btn-primary btn-sm" 
            @click="handleRefresh"
            :disabled="loading"
          >
            <span v-if="loading" class="spinner-border spinner-border-sm me-1" role="status"></span>
            <iconify-icon v-else icon="material-symbols:refresh" class="me-1"></iconify-icon>
            重新整理
          </button>

          <!-- 搜尋輸入框 -->
          <form class="navbar-search">
            <input 
              type="text" 
              name="search" 
              placeholder="搜尋會員或活動" 
              v-model="searchText" 
              @input="handleSearch"
              @focus="isSearchFocused = true" 
              @blur="isSearchFocused = false" 
            />
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
          <p class="mt-3">載入報名資料中...</p>
        </div>

        <!-- 錯誤訊息 -->
        <div v-else-if="error" class="alert alert-danger">
          <h5>載入錯誤</h5>
          <p>{{ error }}</p>
          <button @click="handleRefresh" class="btn btn-outline-primary">重新載入</button>
        </div>

        <!-- 空資料提示 -->
        <div v-else-if="paginatedRegistrations.length === 0" class="alert alert-info">
          <h5>無報名資料</h5>
          <p>目前查詢條件下沒有報名資料。</p>
          <button @click="resetFilters" class="btn btn-outline-primary">重設篩選條件</button>
        </div>

        <!-- 報名資料表格 -->
        <template v-else>
          <table class="table bordered-table mb-0">
            <thead>
              <tr>
                <!-- 重點註解：移除 # 欄位，活動資訊作為第一欄 -->
                <th scope="col" @click="sortBy('activityName')" class="sortable">
                  活動資訊
                  <span v-if="sortKey === 'activityName'">
                    <iconify-icon :icon="sortAsc ? 'mdi:arrow-up' : 'mdi:arrow-down'"></iconify-icon>
                  </span>
                </th>
                <th scope="col" @click="sortBy('memberName')" class="sortable">
                  會員資訊
                  <span v-if="sortKey === 'memberName'">
                    <iconify-icon :icon="sortAsc ? 'mdi:arrow-up' : 'mdi:arrow-down'"></iconify-icon>
                  </span>
                </th>
                <th scope="col" @click="sortBy('people')" class="sortable">
                  報名人數
                  <span v-if="sortKey === 'people'">
                    <iconify-icon :icon="sortAsc ? 'mdi:arrow-up' : 'mdi:arrow-down'"></iconify-icon>
                  </span>
                </th>
                <th scope="col" @click="sortBy('scheduledAt')" class="sortable">
                  報名時間
                  <span v-if="sortKey === 'scheduledAt'">
                    <iconify-icon :icon="sortAsc ? 'mdi:arrow-up' : 'mdi:arrow-down'"></iconify-icon>
                  </span>
                </th>
                <th scope="col" @click="sortBy('status')" class="sortable">
                  狀態
                  <span v-if="sortKey === 'status'">
                    <iconify-icon :icon="sortAsc ? 'mdi:arrow-up' : 'mdi:arrow-down'"></iconify-icon>
                  </span>
                </th>
                <th scope="col">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(registration, index) in paginatedRegistrations" :key="registration.id">
                <!-- 重點註解：活動資訊欄位作為第一欄 - 顯示活動名稱和類別 -->
                <td>
                  <div>
                    <h6 class="text-md mb-1 fw-medium text-secondary-light">
                      {{ registration.activityName || '未知活動' }}
                    </h6>
                    <small class="text-muted">
                      <span class="badge bg-secondary me-1">{{ getCategoryName(registration.activityCategoryId) || '未分類' }}</span>
                    </small>
                  </div>
                </td>
                
                <!-- 重點註解：會員資訊欄位 - 顯示會員姓名和電話 -->
                <td>
                  <div>
                    <h6 class="text-md mb-1 fw-medium text-secondary-light">
                      {{ registration.memberName || '未知會員' }}
                    </h6>
                    <small class="text-muted">{{ registration.memberPhone || 'N/A' }}</small>
                  </div>
                </td>
                
                <!-- 重點註解：報名人數欄位 - 使用 people 欄位 -->
                <td>
                  <span class="badge bg-info">{{ registration.people || 1 }} 人</span>
                </td>
                
                <!-- 報名時間欄位 -->
                <td>
                  <small>{{ formatDateTime(registration.scheduledAt) }}</small>
                </td>
                
                <!-- 狀態欄位 -->
                <td>
                  <span :class="getStatusClasses(registration.status)" 
                    class="px-12 py-4 rounded-pill fw-medium text-sm">
                    {{ registration.status || '未知狀態' }}
                  </span>
                </td>
                
                <!-- 重點註解：操作欄位 - 提供審核和取消按鈕（使用簡單文字圖示） -->
                <td>
                  <div class="d-flex gap-2 align-items-center">
                    <!-- 重點註解：只有待審核狀態才顯示操作按鈕 -->
                    <template v-if="registration.status === '預約審核中'">
                      <!-- 重點註解：檢查是否有必要的 ID 欄位 -->
                      <template v-if="registration.activityId && registration.memberId">
                        <!-- 重點註解：審核通過按鈕 (綠色勾勾) -->
                        <button 
                          type="button" 
                          class="btn btn-outline-success btn-sm btn-icon-simple" 
                          @click="handleApprove(registration)"
                          :disabled="actionLoading === `approve_${registration.id}`"
                          title="審核通過"
                        >
                          <span v-if="actionLoading === `approve_${registration.id}`" 
                            class="spinner-border spinner-border-sm" role="status"></span>
                          <span v-else class="icon-text">✓</span>
                        </button>

                        <!-- 重點註解：取消報名按鈕 (紅色叉叉) -->
                        <button 
                          type="button" 
                          class="btn btn-outline-danger btn-sm btn-icon-simple" 
                          @click="handleCancel(registration)"
                          :disabled="actionLoading === `cancel_${registration.id}`"
                          title="取消報名"
                        >
                          <span v-if="actionLoading === `cancel_${registration.id}`" 
                            class="spinner-border spinner-border-sm" role="status"></span>
                          <span v-else class="icon-text">✗</span>
                        </button>
                      </template>

                      <!-- 重點註解：缺少 ID 欄位的提示 -->
                      <template v-else>
                        <span class="text-muted small">
                          缺少 ID 欄位
                        </span>
                      </template>
                    </template>

                    <!-- 重點註解：已處理狀態顯示 -->
                    <template v-else>
                      <span class="text-muted small">
                        {{ registration.status === '報名成功' ? '已通過' : '已取消' }}
                      </span>
                    </template>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>

          <!-- 分頁區域 -->
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
  </div>
</template>

<script>
// === 重點註解：引入 Vue 相關功能 ===
import { ref, computed, watch, onMounted } from 'vue';

// === 重點註解：引入活動報名相關的 Composable ===
import { useActivityRegistrations } from '@/composables/useActivityRegistrations';

// === 重點註解：引入 Toast 通知功能 ===
import { useToast } from '@/composables/useToast';

export default {
  name: 'ActivityReservationList',

  setup() {
    // === 重點註解：使用 Composable 取得狀態和方法 ===
    const {
      registrations,
      categories,
      loading,
      error,
      loadAllRegistrations,
      loadRegistrationsByCategoryName,
      loadCategories,
      getCategoryOptions,
      approveRegistration,
      cancelRegistration
    } = useActivityRegistrations();

    // === 重點註解：使用 Toast 通知功能 ===
    const { showToast } = useToast();

    // === 重點註解：分頁相關參數 ===
    const currentPage = ref(1);
    const itemsPerPage = ref(20);
    
    // === 重點註解：排序相關參數 ===
    const sortKey = ref('activityName'); // 重點註解：預設排序改為活動名稱
    const sortAsc = ref(true);
    
    // === 重點註解：搜尋與篩選相關參數 ===
    const searchText = ref('');
    const isSearchFocused = ref(false);
    const selectedCategoryName = ref(''); // 重點註解：改為使用類別名稱
    
    // === 重點註解：記錄正在處理的報名 ID ===
    const actionLoading = ref(null);

    /**
     * 重點註解：根據類別 ID 取得類別名稱
     * @param {number} categoryId - 類別 ID
     * @returns {string} 類別名稱
     */
    const getCategoryName = (categoryId) => {
      if (!categoryId) return '未分類';
      const category = categories.value.find(cat => cat.id === categoryId);
      return category ? category.name : `${categoryId}`;
    };

    /**
     * 重點註解：格式化日期時間顯示
     * @param {string} dateTime - 日期時間字串
     * @returns {string} 格式化後的日期時間
     */
    const formatDateTime = (dateTime) => {
      if (!dateTime) return '-';
      try {
        return new Date(dateTime).toLocaleString('zh-TW', {
          year: 'numeric',
          month: '2-digit',
          day: '2-digit',
          hour: '2-digit',
          minute: '2-digit'
        });
      } catch {
        return dateTime;
      }
    };

    /**
     * 重點註解：根據狀態設定 CSS 樣式類別
     * @param {string} status - 狀態值
     * @returns {string} CSS 類別字串
     */
    const getStatusClasses = (status) => {
      switch (status) {
        case '預約審核中':
          return 'bg-warning-focus text-warning-main';
        case '報名成功':
          return 'bg-success-focus text-success-main';
        case '已取消':
          return 'bg-danger-focus text-danger-main';
        default:
          return 'bg-secondary text-white';
      }
    };

    /**
     * 重點註解：處理審核通過操作 - 綠色勾勾按鈕
     * @param {Object} registration - 報名資料
     */
    const handleApprove = async (registration) => {
      const actionId = `approve_${registration.id}`;
      
      try {
        // 重點註解：設定載入狀態
        actionLoading.value = actionId;
        
        console.log('開始審核通過操作:', registration);
        
        // 重點註解：呼叫 Composable 的審核方法，傳入 "報名成功" 狀態
        const success = await approveRegistration(registration.activityId, registration.memberId);
        
        if (success) {
          console.log('審核通過成功');
          
          // 重點註解：顯示成功提示
          showToast({
            title: '審核成功',
            message: `${registration.memberName} 的活動報名已審核通過`,
            type: 'success',
            duration: 3000
          });
        } else {
          console.error('審核通過失敗');
          
          // 重點註解：顯示失敗提示
          showToast({
            title: '審核失敗',
            message: '報名審核操作失敗，請稍後再試',
            type: 'error',
            duration: 3000
          });
        }
      } catch (err) {
        console.error('審核通過時發生錯誤:', err);
        
        // 重點註解：顯示錯誤提示
        showToast({
          title: '操作錯誤',
          message: '審核操作時發生錯誤，請檢查網路連線',
          type: 'error',
          duration: 3000
        });
      } finally {
        // 重點註解：清除載入狀態
        actionLoading.value = null;
      }
    };

    /**
     * 重點註解：處理取消報名操作 - 紅色叉叉按鈕
     * @param {Object} registration - 報名資料
     */
    const handleCancel = async (registration) => {
      const actionId = `cancel_${registration.id}`;
      
      try {
        // 重點註解：設定載入狀態
        actionLoading.value = actionId;
        
        console.log('開始取消報名操作:', registration);
        
        // 重點註解：呼叫 Composable 的取消方法，傳入 "已取消" 狀態
        const success = await cancelRegistration(registration.activityId, registration.memberId);
        
        if (success) {
          console.log('取消報名成功');
          
          // 重點註解：顯示成功提示
          showToast({
            title: '取消成功',
            message: `${registration.memberName} 的活動報名已取消`,
            type: 'warning',
            duration: 3000
          });
        } else {
          console.error('取消報名失敗');
          
          // 重點註解：顯示失敗提示
          showToast({
            title: '取消失敗',
            message: '報名取消操作失敗，請稍後再試',
            type: 'error',
            duration: 3000
          });
        }
      } catch (err) {
        console.error('取消報名時發生錯誤:', err);
        
        // 重點註解：顯示錯誤提示
        showToast({
          title: '操作錯誤',
          message: '取消操作時發生錯誤，請檢查網路連線',
          type: 'error',
          duration: 3000
        });
      } finally {
        // 重點註解：清除載入狀態
        actionLoading.value = null;
      }
    };

    /**
     * 重點註解：處理資料排序
     * @param {string} key - 排序欄位名稱
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
     * 重點註解：切換頁碼
     * @param {number} page - 目標頁碼
     */
    const goToPage = (page) => {
      if (page >= 1 && page <= totalPages.value) {
        currentPage.value = page;
      }
    };

    /**
     * 重點註解：處理活動類別變更 - 使用類別名稱查詢
     * @param {Event} event - 變更事件
     */
    const handleCategoryChange = async (event) => {
      const categoryName = event.target.value;
      console.log('活動類別變更:', categoryName);
      
      try {
        if (categoryName) {
          // 重點註解：使用類別名稱查詢
          await loadRegistrationsByCategoryName(categoryName);
        } else {
          // 重點註解：載入全部資料
          await loadAllRegistrations();
        }
        // 重點註解：重置分頁
        currentPage.value = 1;
      } catch (err) {
        console.error('載入報名資料失敗:', err);
        
        // 重點註解：顯示載入錯誤提示
        showToast({
          title: '載入失敗',
          message: '載入報名資料失敗，請重新整理頁面',
          type: 'error',
          duration: 3000
        });
      }
    };

    /**
     * 重點註解：處理重新整理
     */
    const handleRefresh = async () => {
      try {
        // 重點註解：重新載入類別列表
        await loadCategories();
        
        // 重點註解：根據當前篩選條件重新載入資料
        if (selectedCategoryName.value) {
          await loadRegistrationsByCategoryName(selectedCategoryName.value);
        } else {
          await loadAllRegistrations();
        }
        
        // 重點註解：顯示重新整理成功提示
        showToast({
          title: '重新整理完成',
          message: '報名資料已更新',
          type: 'info',
          duration: 2000
        });
      } catch (err) {
        console.error('重新整理失敗:', err);
        
        // 重點註解：顯示重新整理失敗提示
        showToast({
          title: '重新整理失敗',
          message: '無法重新載入資料，請檢查網路連線',
          type: 'error',
          duration: 3000
        });
      }
    };

    /**
     * 重點註解：處理搜尋事件
     */
    const handleSearch = () => {
      currentPage.value = 1; // 重置頁碼
    };

    /**
     * 重點註解：重設所有篩選條件
     */
    const resetFilters = () => {
      searchText.value = '';
      selectedCategoryName.value = '';
      currentPage.value = 1;
      // 重點註解：重新載入全部資料
      loadAllRegistrations();
      
      // 重點註解：顯示重設提示
      showToast({
        title: '篩選條件已重設',
        message: '已恢復顯示所有報名資料',
        type: 'info',
        duration: 2000
      });
    };

    // === 重點註解：計算篩選後的報名列表 ===
    const filteredRegistrations = computed(() => {
      let result = [...registrations.value];

      // 重點註解：根據搜尋文字篩選
      if (searchText.value) {
        const search = searchText.value.toLowerCase();
        result = result.filter(reg =>
          (reg.memberName && reg.memberName.toLowerCase().includes(search)) ||
          (reg.activityName && reg.activityName.toLowerCase().includes(search)) ||
          (reg.memberPhone && reg.memberPhone.toLowerCase().includes(search))
        );
      }

      // 重點註解：根據排序設定進行排序
      if (sortKey.value) {
        result.sort((a, b) => {
          let valA, valB;

          if (sortKey.value === 'people') {
            valA = Number(a[sortKey.value]) || 0;
            valB = Number(b[sortKey.value]) || 0;
          } else if (sortKey.value === 'scheduledAt') {
            valA = new Date(a[sortKey.value] || '1970-01-01').getTime();
            valB = new Date(b[sortKey.value] || '1970-01-01').getTime();
          } else {
            valA = a[sortKey.value] ? String(a[sortKey.value]).toLowerCase() : '';
            valB = b[sortKey.value] ? String(b[sortKey.value]).toLowerCase() : '';
          }

          if (valA === valB) return 0;
          return sortAsc.value ? (valA > valB ? 1 : -1) : (valA < valB ? 1 : -1);
        });
      }

      return result;
    });

    // === 重點註解：計算活動類別選項 ===
    const categoryOptions = computed(() => getCategoryOptions());

    // === 重點註解：計算統計數據 ===
    const totalCount = computed(() => registrations.value.length);
    const pendingCount = computed(() => 
      registrations.value.filter(reg => reg.status === '預約審核中').length
    );
    const approvedCount = computed(() => 
      registrations.value.filter(reg => reg.status === '報名成功').length
    );
    const cancelledCount = computed(() => 
      registrations.value.filter(reg => reg.status === '已取消').length
    );

    // === 重點註解：分頁相關計算屬性 ===
    const entriesPerPage = computed(() => Number(itemsPerPage.value));
    const paginatedRegistrations = computed(() => {
      return filteredRegistrations.value.slice(startIndex.value, endIndex.value);
    });
    const totalEntries = computed(() => filteredRegistrations.value.length);
    const totalPages = computed(() => {
      return Math.ceil(filteredRegistrations.value.length / entriesPerPage.value) || 1;
    });
    const startIndex = computed(() => {
      return (currentPage.value - 1) * entriesPerPage.value;
    });
    const endIndex = computed(() => {
      return Math.min(startIndex.value + entriesPerPage.value, filteredRegistrations.value.length);
    });
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

    // === 重點註解：監聽每頁顯示數量變更 ===
    watch(itemsPerPage, () => {
      currentPage.value = 1;
    });

    // === 重點註解：元件掛載時初始化 ===
    onMounted(async () => {
      try {
        console.log('活動報名管理頁面初始化...');
        
        // 重點註解：同時載入類別和全部報名資料
        await Promise.all([
          loadCategories(),
          loadAllRegistrations()
        ]);
        
        console.log('頁面初始化完成');
      } catch (err) {
        console.error('頁面初始化失敗:', err);
        
        // 重點註解：顯示初始化失敗提示
        showToast({
          title: '載入失敗',
          message: '頁面初始化失敗，請重新整理頁面',
          type: 'error',
          duration: 5000
        });
      }
    });

    return {
      // 狀態參數
      registrations,
      categories,
      loading,
      error,
      currentPage,
      itemsPerPage,
      sortKey,
      sortAsc,
      searchText,
      isSearchFocused,
      selectedCategoryName,
      actionLoading,

      // 計算屬性
      filteredRegistrations,
      categoryOptions,
      totalCount,
      pendingCount,
      approvedCount,
      cancelledCount,
      paginatedRegistrations,
      totalEntries,
      totalPages,
      startIndex,
      endIndex,
      displayedPages,
      entriesPerPage,

      // 方法
      formatDateTime,
      getStatusClasses,
      getCategoryName,
      sortBy,
      goToPage,
      handleCategoryChange,
      handleRefresh,
      handleSearch,
      resetFilters,
      handleApprove,    // 重點註解：審核通過方法
      handleCancel      // 重點註解：取消報名方法
    };
  }
}
</script>

<style scoped>
/* === 重點註解：樣式設定 === */
.sortable {
  cursor: pointer;
  user-select: none;
}

.sortable:hover {
  text-decoration: underline;
}

.table th,
.table td {
  vertical-align: middle;
}

.page-link.disabled {
  opacity: 0.5;
  pointer-events: none;
}

.badge {
  font-size: 0.75rem;
  font-weight: 500;
}

/* 重點註解：放大狀態按鈕樣式 */
.status-badge {
  font-size: 0.85rem !important;
  padding: 0.5rem 0.75rem !important;
  font-weight: 600;
}

/* 重點註解：簡單圖示按鈕樣式 */
.btn-icon-simple {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  border-radius: 6px;
  border-width: 1px;
  background-color: transparent;
}

.btn-icon-simple .icon-text {
  font-size: 14px;
  font-weight: bold;
  line-height: 1;
}

.btn-icon-simple:hover {
  transform: scale(1.05);
  transition: transform 0.2s ease;
}

.btn-icon-simple:disabled {
  opacity: 0.6;
  transform: none;
}

/* 重點註解：綠色勾勾按鈕樣式 */
.btn-outline-success.btn-icon-simple:hover {
  background-color: rgba(25, 135, 84, 0.1);
  border-color: #198754;
}

/* 重點註解：紅色叉叉按鈕樣式 */
.btn-outline-danger.btn-icon-simple:hover {
  background-color: rgba(220, 53, 69, 0.1);
  border-color: #dc3545;
}

@media (max-width: 768px) {
  .card-header {
    flex-direction: column;
    align-items: stretch !important;
  }

  .navbar-search {
    width: 100%;
    margin-top: 10px;
  }

  .d-flex.gap-3 {
    flex-wrap: wrap;
  }
  
  /* 重點註解：行動裝置上的狀態按鈕調整 */
  .status-badge {
    font-size: 0.75rem !important;
    padding: 0.4rem 0.6rem !important;
  }
}
</style>