<template>
  <div class="card">
    <!-- 卡片標頭 - 搜尋和篩選 -->
    <div class="card-header border-bottom bg-base py-16 px-24">
      <div class="row g-3 align-items-center">
        <!-- 左側：顯示筆數選擇 -->
        <div class="col-auto">
          <div class="d-flex align-items-center gap-2">
            <span class="text-secondary-light fw-medium">顯示</span>
            <select 
              v-model="entriesPerPage"
              @change="changeEntriesPerPage"
              class="form-select form-select-sm w-auto"
            >
              <option value="10">10</option>
              <option value="20">20</option>
              <option value="50">50</option>
              <option value="100">100</option>
            </select>
            <span class="text-secondary-light fw-medium">筆</span>
          </div>
        </div>

        <!-- 中間：搜尋框 -->
        <div class="col-md-4">
          <div class="position-relative">
            <input
              v-model="searchQuery"
              @input="handleSearch"
              type="text"
              class="form-control form-control-sm ps-40"
              placeholder="搜尋照服員姓名"
            >
            <iconify-icon 
              icon="ion:search-outline" 
              class="position-absolute top-50 start-0 translate-middle-y ms-16 text-secondary-light"
            ></iconify-icon>
          </div>
        </div>

        <!-- 右側：狀態篩選 -->
        <div class="col-auto">
          <select 
            v-model="selectedStatus"
            @change="handleStatusFilter"
            class="form-select form-select-sm w-auto"
          >
            <option value="">全部狀態</option>
            <option value="pending">待審核</option>
            <option value="approved">已確認</option>
            <!-- 重點註解：移除進行中選項，因為用戶要求不顯示此選項 -->
            <!-- <option value="in_progress">進行中</option> -->
            <option value="completed">已完成</option>
            <option value="cancelled">已取消</option>
            <option value="rejected">已拒絕</option>
          </select>
        </div>

        <!-- 右側：新增訂單按鈕 -->
        <div class="col-auto ms-auto">
          <button 
            @click="$emit('create')"
            class="btn btn-primary btn-sm d-flex align-items-center gap-1"
          >
            <iconify-icon icon="ic:round-plus" class="text-xl"></iconify-icon>
            排班安排
          </button>
        </div>
      </div>
    </div>

    <!-- 表格內容 -->
    <div class="card-body p-24">
      <!-- 載入狀態 -->
      <div v-if="loading" class="text-center py-4">
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">載入中...</span>
        </div>
      </div>

      <!-- 錯誤狀態 -->
      <div v-else-if="error" class="alert alert-danger" role="alert">
        <iconify-icon icon="material-symbols:error-outline" class="me-2"></iconify-icon>
        {{ error }}
        <button @click="$emit('retry')" class="btn btn-sm btn-outline-danger ms-2">重試</button>
      </div>

      <!-- 空狀態 -->
      <div v-else-if="filteredAppointments.length === 0" class="text-center py-5">
        <iconify-icon icon="material-symbols:inbox-outline" class="text-secondary-light" style="font-size: 4rem;"></iconify-icon>
        <h6 class="text-secondary-light mt-3">暫無預約訂單資料</h6>
        <p class="text-secondary-light">請建立新的預約訂單或調整篩選條件</p>
      </div>

      <!-- 資料表格 -->
      <div v-else class="table-responsive">
        <table class="table table-striped">
          <thead>
            <tr>
              <!-- <th scope="col">
                <div class="form-check">
                  <input 
                    v-model="selectAll"
                    @change="toggleSelectAll"
                    class="form-check-input" 
                    type="checkbox"
                  >
                </div>
              </th> -->
              <th scope="col">訂單編號</th>
              <th scope="col">會員</th>
              <th scope="col">照服員</th>
              <th scope="col">開始時間</th>
              <th scope="col">結束時間</th>
              <th scope="col">總時長</th>
              <th scope="col">訂單狀態</th>
              <th scope="col">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr 
              v-for="appointment in paginatedAppointments" 
              :key="appointment.id"
              class="appointment-row"
            >
              <!-- 選擇框
              <td>
                <div class="form-check">
                  <input 
                    v-model="selectedAppointments"
                    :value="appointment.id"
                    class="form-check-input" 
                    type="checkbox"
                  >
                </div>
              </td> -->

              <!-- 訂單編號 -->
              <td>
                <span class="fw-medium text-primary-light"># {{ appointment.id }}</span>
              </td>

              <!-- 會員名字 -->
              <td>
                <div class="d-flex align-items-center">
                  <div>
                    <div class="fw-medium">
                      {{ getMemberDisplayName(appointment) }}
                      <span v-if="appointment.isBlocked" class="badge bg-warning-focus text-warning-main border border-warning-main ms-1 small">
                        虛擬
                      </span>
                    </div>
                    <div class="text-sm text-secondary-light">
                      {{ appointment.isBlocked ? `類型: ${appointment.blockType}` : `ID: ${appointment.memberId || '系統建立'}` }}
                    </div>
                  </div>
                </div>
              </td>

              <!-- 照服員名字 -->
              <td>
                <div class="d-flex align-items-center">
                  <div>
                    <div class="fw-medium">{{ appointment.caregiverName || '未指定照服員' }}</div>
                    <div class="text-sm text-secondary-light">ID: {{ appointment.caregiverId }}</div>
                  </div>
                </div>
              </td>

              <!-- 開始時間 -->
              <td>
                <span class="text-secondary-light">{{ appointment.startTimeDisplay }}</span>
              </td>

              <!-- 結束時間 -->
              <td>
                <span class="text-secondary-light">{{ appointment.endTimeDisplay }}</span>
              </td>

              <!-- 總時長 -->
              <td>
                <span class="badge bg-info-focus text-info-main border border-info-main">
                  {{ appointment.durationDisplay }}
                </span>
              </td>

              <!-- 訂單狀態 -->
              <td>
                <span 
                  :class="`badge bg-${appointment.statusColor}-focus text-${appointment.statusColor}-main border border-${appointment.statusColor}-main`"
                >
                  {{ appointment.statusDisplay }}
                </span>
              </td>

              <!-- 操作按鈕 -->
              <td>
                <div class="d-flex align-items-center gap-2">
                  <!-- 查看詳細 -->
                  <button
                    v-if="appointment.availableActions && appointment.availableActions.includes('view')"
                    @click="$emit('view', appointment)"
                    class="w-32-px h-32-px bg-primary-50 text-primary-600 rounded-circle d-inline-flex align-items-center justify-content-center"
                    title="查看詳細"
                  >
                    <iconify-icon icon="iconamoon:eye-light"></iconify-icon>
                  </button>

                  <!-- 審核按鈕 -->
                  <button
                    v-if="appointment.availableActions.includes('review')"
                    @click="$emit('review', appointment)"
                    class="w-32-px h-32-px bg-warning-50 text-warning-600 rounded-circle d-inline-flex align-items-center justify-content-center"
                    title="審核"
                  >
                    <iconify-icon icon="material-symbols:rate-review-outline"></iconify-icon>
                  </button>

                  <!-- 完成按鈕 -->
                  <button
                    v-if="appointment.availableActions.includes('complete')"
                    @click="$emit('complete', appointment)"
                    class="w-32-px h-32-px bg-success-50 text-success-600 rounded-circle d-inline-flex align-items-center justify-content-center"
                    title="標記完成"
                  >
                    <iconify-icon icon="material-symbols:check-circle-outline"></iconify-icon>
                  </button>

                  <!-- 修改按鈕 -->
                  <button
                    v-if="appointment.availableActions.includes('edit')"
                    @click="$emit('edit', appointment)"
                    class="w-32-px h-32-px bg-success-50 text-success-600 rounded-circle d-inline-flex align-items-center justify-content-center"
                    title="修改"
                  >
                    <iconify-icon icon="lucide:edit"></iconify-icon>
                  </button>

                  <!-- 取消按鈕
                  <button
                    v-if="appointment.availableActions.includes('cancel')"
                    @click="$emit('cancel', appointment)"
                    class="w-32-px h-32-px bg-danger-50 text-danger-600 rounded-circle d-inline-flex align-items-center justify-content-center"
                    title="取消訂單"
                  >
                    <iconify-icon icon="material-symbols:cancel-outline"></iconify-icon>
                  </button> -->
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 分頁元件 -->
      <Pagination
        v-if="filteredAppointments.length > 0"
        :currentPage="currentPage + 1"
        :totalPages="totalPages"
        :startIndex="startIndex"
        :endIndex="endIndex"
        :totalItems="totalAppointments"
        @page-changed="changePage"
      />
    </div>
  </div>
</template>

<script setup>
// 引入必要的函式和元件
import { ref, computed, watch, onMounted } from 'vue';
import Pagination from '@/components/pagination/index.vue';
import { memberApi } from '@/api/memberApi.js';

// 定義 props
const props = defineProps({
  appointments: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  },
  error: {
    type: String,
    default: null
  },
  totalPages: {
    type: Number,
    default: 0
  },
  totalAppointments: {
    type: Number,
    default: 0
  },
  currentPage: {
    type: Number,
    default: 0
  }
});

// 定義事件
const emit = defineEmits([
  'view',
  'review', 
  'complete',
  'edit',
  'cancel',
  'create',
  'retry',
  'page-changed',
  'entries-changed',
  'search',
  'status-filter'
]);

// 響應式資料
const entriesPerPage = ref(20);
const searchQuery = ref('');
const selectedStatus = ref('');
const selectedAppointments = ref([]);

// 會員資料快取
const memberCache = ref(new Map());
const loadingMembers = ref(new Set());
const selectAll = ref(false);

// 計算屬性 - 篩選後的預約訂單
const filteredAppointments = computed(() => {
  let result = [...props.appointments];

  // 根據搜尋關鍵字過濾
  if (searchQuery.value.trim()) {
    const searchTerm = searchQuery.value.trim().toLowerCase();
    result = result.filter(appointment => 
      (appointment.memberName && appointment.memberName.toLowerCase().includes(searchTerm)) ||
      (appointment.caregiverName && appointment.caregiverName.toLowerCase().includes(searchTerm)) ||
      (appointment.serviceLocation && appointment.serviceLocation.toLowerCase().includes(searchTerm)) ||
      appointment.id.toString().includes(searchTerm)
    );
  }

  return result;
});

// 計算屬性 - 分頁後的資料
const paginatedAppointments = computed(() => {
  // 由於後端已處理分頁，這裡直接返回篩選結果
  return filteredAppointments.value;
});

// 計算屬性 - 分頁資訊
const startIndex = computed(() => {
  if (props.totalAppointments === 0) return 0;
  return props.currentPage * entriesPerPage.value + 1;
});

const endIndex = computed(() => {
  const end = (props.currentPage + 1) * entriesPerPage.value;
  return Math.min(end, props.totalAppointments);
});

// 處理每頁顯示筆數變更
const changeEntriesPerPage = () => {
  emit('entries-changed', entriesPerPage.value);
};

// 處理搜尋
const handleSearch = () => {
  // 使用防抖處理搜尋
  clearTimeout(handleSearch.timer);
  handleSearch.timer = setTimeout(() => {
    emit('search', searchQuery.value);
  }, 500);
};

// 處理狀態篩選
const handleStatusFilter = () => {
  emit('status-filter', selectedStatus.value);
};

// 處理換頁
const changePage = (page) => {
  emit('page-changed', page - 1); // 轉換為從0開始的頁碼
};

// 處理全選
const toggleSelectAll = () => {
  if (selectAll.value) {
    selectedAppointments.value = filteredAppointments.value.map(appointment => appointment.id);
  } else {
    selectedAppointments.value = [];
  }
};

/**
 * 載入會員資料（批次處理）
 * @param {Array} appointments - 預約陣列
 */
const loadMemberDataBatch = async (appointments) => {
  if (!appointments || appointments.length === 0) return;
  
  // 收集需要載入的會員 ID
  const memberIds = [...new Set(
    appointments
      .filter(appointment => appointment.memberId && !appointment.isBlocked)
      .map(appointment => appointment.memberId)
      .filter(id => !memberCache.value.has(id) && !loadingMembers.value.has(id))
  )];
  
  // 批次載入會員資料
  for (const memberId of memberIds) {
    await loadMemberData(memberId);
  }
};

/**
 * 載入會員資料
 * @param {number} memberId - 會員 ID
 */
const loadMemberData = async (memberId) => {
  if (!memberId || memberCache.value.has(memberId) || loadingMembers.value.has(memberId)) {
    return;
  }
  
  try {
    loadingMembers.value.add(memberId);
    
    const response = await memberApi.getMemberById(memberId);
    if (response.data && response.data.memberName) {
      memberCache.value.set(memberId, response.data);
    }
  } catch (error) {
    console.error('載入會員資料失敗:', error);
  } finally {
    loadingMembers.value.delete(memberId);
  }
};

/**
 * 取得顯示的會員名稱
 * @param {Object} appointment - 預約資料
 * @returns {string} 會員名稱
 */
const getMemberDisplayName = (appointment) => {
  // 如果是虛擬訂單，顯示原始名稱
  if (appointment.isBlocked) {
    return appointment.memberName || '未指定會員';
  }
  
  // 如果有會員 ID 且快取中有資料，使用真實姓名
  if (appointment.memberId && memberCache.value.has(appointment.memberId)) {
    const memberData = memberCache.value.get(appointment.memberId);
    return memberData.memberName;
  }
  
  // 如果正在載入中
  if (appointment.memberId && loadingMembers.value.has(appointment.memberId)) {
    return '載入中...';
  }
  
  // 如果有會員 ID 但名稱是系統格式，觸發載入
  if (appointment.memberId && 
      (appointment.memberName === '系統建立' || 
       appointment.memberName?.includes('會員預約 (ID:'))) {
    // 非同步載入會員資料
    loadMemberData(appointment.memberId);
    return '載入中...';
  }
  
  // 其他情況使用原始名稱
  return appointment.memberName || '未指定會員';
};

// 監聽選中的預約變化，更新全選狀態
watch(selectedAppointments, (newVal) => {
  if (newVal.length === 0) {
    selectAll.value = false;
  } else if (newVal.length === filteredAppointments.value.length) {
    selectAll.value = true;
  } else {
    selectAll.value = false;
  }
}, { deep: true });

// 監聽預約列表變化，重置選擇並載入會員資料
watch(() => props.appointments, (newAppointments) => {
  selectedAppointments.value = [];
  selectAll.value = false;
  if (newAppointments && newAppointments.length > 0) {
    loadMemberDataBatch(newAppointments);
  }
}, { deep: true, immediate: true });

// 暴露選中的預約給父元件使用
defineExpose({
  selectedAppointments,
  clearSelection: () => {
    selectedAppointments.value = [];
    selectAll.value = false;
  }
});
</script>

<style scoped>
.appointment-row:hover {
  background-color: var(--bs-gray-50);
}

.table th {
  font-weight: 600;
  color: var(--bs-gray-700);
  border-bottom: 2px solid var(--bs-gray-200);
  padding: 1rem 0.75rem;
}

.table td {
  padding: 1rem 0.75rem;
  vertical-align: middle;
}

.badge {
  font-size: 0.75rem;
  font-weight: 500;
  padding: 0.375rem 0.75rem;
}

.btn-sm {
  --bs-btn-padding-y: 0.375rem;
  --bs-btn-padding-x: 0.75rem;
  --bs-btn-font-size: 0.875rem;
}

/* 重點註解：修正已拒絕狀態的文字顏色，確保文字可讀性 */
.badge.bg-danger-focus.text-danger-main {
  color: #721c24 !important; /* 深色文字以確保在淺色背景上可讀 */
}

/* 重點註解：修正其他 danger 相關狀態的文字顏色 */
.badge.text-danger-main {
  color: #721c24 !important; /* 統一深色文字 */
}
</style>