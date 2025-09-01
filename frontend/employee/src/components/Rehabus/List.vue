<template>
  <div class="card border-0 shadow-sm">
    <!-- ...現有的模板內容保持不變... -->
    <div class="card-header bg-transparent d-flex justify-content-between align-items-center">
    
      <div class="d-flex align-items-center">
        <!-- 座位數量搜尋欄位 - -->
         <div class="d-flex me-3 gap-2">
      <div class="input-group">
        <span class="input-group-text bg-transparent">一般座位</span>
        <input 
          type="number" 
          class="form-control search-input" 
          placeholder="最少座位數"
          v-model="minSeats"
          min="0"
        >
      </div>
      <div class="input-group">
      <span class="input-group-text bg-transparent">輪椅座位</span>
        <input 
          type="number" 
          class="form-control search-input" 
          placeholder="最少輪椅數"
          v-model="minWheels"
          min="0"
        >
      </div>
       
      <!-- 搜尋按鈕 - 修改原本的新增按鈕為搜尋按鈕 -->
      <button class="btn btn-primary d-flex align-items-center" @click="handleSearch">
        <i class="bi bi-search me-2"></i>
        <span class="d-flex horizontal-text">
          <span class="me-0">搜</span><span>尋</span>
        </span>
      </button>
       </div>
      </div>
    </div>

    <div class="card-body p-0">
      <!-- 資料表格 -->
      <div class="table-responsive">
        <table class="table mb-0">

          <thead class="table-light">
            <tr>
              <th>巴士編號</th>
              <th>車行名稱</th>
              <th>廠牌</th>
              <th>型號</th>
              <th>一般座位</th>
              <th>輪椅座位</th>
              <th>車牌號碼</th>
              <th>狀態</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
              <!-- 載入中顯示 -->
            <tr v-if="loading">
              <td colspan="9" class="text-center py-4">
                <div class="spinner-border text-primary" role="status">
                  <span class="visually-hidden">載入中...</span>
                </div>
              </td>
            </tr>
            <!-- 無資料顯示 -->
            <tr v-else-if="busList.length === 0">
              <td colspan="9" class="text-center py-4">
                找不到符合條件的復康巴士資料
              </td>
            </tr>
            <!-- 資料列表 -->
            <tr v-for="bus in busList" :key="bus.id">
              <td>{{ bus.id }}</td>
              <td>{{ bus.carDealership }}</td>
              <td>{{ bus.busBrand }}</td>
              <td>{{ bus.busModel }}</td>
              <td>{{ bus.seatCapacity }}</td>
              <td>{{ bus.wheelchairCapacity }}</td>
              <td>{{ bus.licensePlate }}</td>
              <td>
                <span 
                  :class="getStatusClass(bus.status)" 
                  class="badge rounded-pill">
                  {{ getStatusText(bus.status) }}
                </span>
              </td>
              <!-- 修改後的操作欄位 -->
              <td>
                <div class="d-flex gap-2">
                  <!-- 編輯按鈕 - 使用提供的圖示樣式 -->
                     <a href="#" @click.prevent="handleEdit(bus.id)"
                     class="w-32-px h-32-px bg-success-focus text-success-main rounded-circle d-inline-flex align-items-center justify-content-center">
                    <iconify-icon icon="lucide:edit"></iconify-icon>
                  </a>
                  <!-- 刪除按鈕 - 使用提供的圖示樣式 -->
                  <a href="#" @click.prevent="handleDelete(bus.id)"
                     class="w-32-px h-32-px bg-danger-focus text-danger-main rounded-circle d-inline-flex align-items-center justify-content-center">
                    <iconify-icon icon="mingcute:delete-2-line"></iconify-icon>
                  </a>

                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 分頁元件 -->
    <div class="card-footer bg-transparent d-flex justify-content-between align-items-center">
      <div>共 {{ totalItems }} 筆資料</div>
      <nav aria-label="復康巴士列表分頁">
        <ul class="pagination pagination-sm mb-0">
          <li class="page-item" :class="{ disabled: currentPage === 1 }">
            <a class="page-link" href="#" @click.prevent="handlePageChange(currentPage - 1)">上一頁</a>
          </li>
          <li 
            v-for="page in totalPages" 
            :key="page" 
            class="page-item" 
            :class="{ active: page === currentPage }"
          >
            <a class="page-link" href="#" @click.prevent="handlePageChange(page)">{{ page }}</a>
          </li>
          <li class="page-item" :class="{ disabled: currentPage === totalPages }">
            <a class="page-link" href="#" @click.prevent="handlePageChange(currentPage + 1)">下一頁</a>
          </li>
        </ul>
      </nav>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, computed, watch } from 'vue';
import { useRouter } from 'vue-router';
import { rehabusService } from '@/services/rehabusService';
import { useToast } from '@/composables/useToast';
import { useConfirmDialog } from '@/composables/useConfirmDialog';

export default {
  name: 'BusList',
  setup() {
    // 基本狀態
    const busList = ref([]);
    const loading = ref(true);
    const error = ref(null);
    const router = useRouter();
    const { showToast } = useToast();
    const { showConfirmDialog } = useConfirmDialog(); // **重點：引入確認對話框**

    // 分頁相關
    const currentPage = ref(1);
    const pageSize = ref(10);
    const totalItems = ref(0);
    
    // 座位搜尋參數
    const minSeats = ref('');
    const minWheels = ref('');
    const searchQuery = ref(''); // 保留原來的搜尋變數，但不直接使用

    // 強制刷新控制
    const refreshTrigger = ref(0);

    // 計算總頁數
    const totalPages = computed(() => {
      return Math.ceil(totalItems.value / pageSize.value);
    });

    // 載入復康巴士資料
       const loadBuses = async () => {
      loading.value = true;
      try {
        let data;
        // 判斷是否有搜尋條件
        if (minSeats.value || minWheels.value) {
          // 有搜尋條件時使用篩選 API
          data = await rehabusService.searchBusesBySeats({
            minSeats: minSeats.value ? parseInt(minSeats.value) : null,
            minWheels: minWheels.value ? parseInt(minWheels.value) : null
          });
        } else {
          // 無搜尋條件時獲取全部資料
          data = await rehabusService.getAllBuses();
        }
        
        // 資料後處理 - 確保每筆資料都有狀態值
        let filteredData = data.map(bus => ({
          ...bus,
          // 如果狀態為空或未定義，則預設為「可派遣」
          status: bus.status || 'available'
        }));
        
        // 更新總筆數
        totalItems.value = filteredData.length;
        
        // 分頁處理
        const startIndex = (currentPage.value - 1) * pageSize.value;
        const endIndex = startIndex + pageSize.value;
        busList.value = filteredData.slice(startIndex, endIndex);
        
        // 確保狀態顯示正確
        console.log('載入的巴士資料:', busList.value);

      } catch (err) {
        error.value = err.message;
        console.error('載入復康巴士資料失敗:', err);
      } finally {
        loading.value = false;
      }
    };

    // 處理頁面變更
    const handlePageChange = (page) => {
      if (page < 1 || page > totalPages.value) return;
      currentPage.value = page;
      loadBuses();
    };

    // 處理搜尋
    const handleSearch = () => {
      // 驗證輸入的是否為有效數值
      if (minSeats.value && isNaN(parseInt(minSeats.value))) {
        showToast({
          title: '搜尋錯誤',
          message: '一般座位數必須是數字',
          type: 'error'
        });
        return;
      }
      
      if (minWheels.value && isNaN(parseInt(minWheels.value))) {
        showToast({
          title: '搜尋錯誤',
          message: '輪椅座位數必須是數字',
          type: 'error'
        });
        return;
      }
      
      // 執行搜尋前重置為第一頁
      currentPage.value = 1;
      loadBuses();
    };

    // 手動重新載入資料
    const refreshList = () => {
      refreshTrigger.value++; // 觸發重新載入
      loadBuses();
    };

    // 處理編輯
    const handleEdit = (busId) => {
      // 導航到編輯頁面
      router.push(`/rehabus/edit/${busId}`);
    };

    // **重點：使用 useConfirmDialog 處理刪除確認**
    const handleDelete = async (busId) => {
      try {
        // 取得要刪除的巴士資訊用於顯示詳細訊息
        const busToDelete = busList.value.find(bus => bus.id === busId);
        const busInfo = busToDelete ? `${busToDelete.carDealership} ${busToDelete.busBrand} (${busToDelete.licensePlate})` : `ID: ${busId}`;

        // **重點：使用 showConfirmDialog 顯示確認對話框**
        const confirmed = await showConfirmDialog({
          title: '確認刪除復康巴士',
          message: `您確定要刪除以下復康巴士嗎？\n\n${busInfo}\n\n此操作無法復原。`,
          type: 'error', // **重點：使用 error 類型以突出危險性**
          confirmText: '確認刪除',
          cancelText: '取消',
          confirmButtonClass: 'btn-danger',
          icon: 'heroicons:trash'
        });
        

        // **重點：只有在使用者確認後才執行刪除**
        if (confirmed) {
          // 顯示載入狀態
          const busIndex = busList.value.findIndex(bus => bus.id === busId);
          if (busIndex !== -1) {
            // 可以在這裡添加載入狀態指示
          }

          // **重點：執行實際的刪除操作**
          await rehabusService.deleteBus(busId);
          
          // **重點：刪除成功後顯示成功訊息**
          showToast({
            title: '刪除成功',
            message: `復康巴士 ${busInfo} 已成功刪除`,
            type: 'success'
          });

          // **重點：重新載入資料以更新列表**
          await loadBuses();
        }
      } catch (err) {
        console.error('刪除復康巴士失敗:', err);
        
        // **重點：顯示錯誤訊息**
        showToast({
          title: '刪除失敗',
          message: err.message || '刪除復康巴士時發生未知錯誤，請稍後再試',
          type: 'error'
        });
      }
    };

    // 處理新增
    const handleAddNew = () => {
      // 導航到新增頁面
      router.push('/rehabus/add');
    };

    // 狀態樣式對應
    const getStatusClass = (status) => {
      switch (status) {
        case 'available':
          return 'bg-success';
        case 'dispatched':
          return 'bg-warning';
        case 'maintenance':
          return 'bg-danger';
        default:
          return 'bg-success';
      }
    };

    // 狀態文字對應
    const getStatusText = (status) => {
      switch (status) {
        case 'available':
          return '可派遣';
        case 'dispatched':
          return '已派遣';
        case 'maintenance':
          return '維修中';
        default:
          return '可派遣';
      }
    };

    // 監聽路由變更，從編輯頁返回時重新載入資料
    watch(() => router.currentRoute.value.fullPath, (newPath) => {
      if (newPath === '/rehabus/list') {
        loadBuses();
      }
    });

    // 頁面載入時獲取資料
    onMounted(() => {
      loadBuses();

      // 添加全域事件監聽器，用於其他頁面通知列表刷新
      window.addEventListener('bus-status-updated', () => {
        loadBuses();
      });
    });

    return {
      busList,
      loading,
      error,
      currentPage,
      totalPages,
      totalItems,
      minSeats,
      minWheels,
      handlePageChange,
      handleSearch,
      handleEdit,
      handleDelete,
      handleAddNew,
      refreshList,
      getStatusClass,
      getStatusText
    };
  }
};
</script>

<style scoped>
.card {
  border-radius: 0.5rem;
}

.table th {
  font-weight: 600;
  color: #555;
}

.table td, .table th {
  padding: 0.75rem 1rem;
  vertical-align: middle;
}

.badge {
  padding: 0.5em 0.75em;
}

/* 新增水平文字排列樣式 */
.horizontal-text {
  display: flex;
  flex-direction: row; /* 確保文字水平排列 */
  align-items: center;
  letter-spacing: 0.5px; /* 增加字元間距，讓文字更美觀 */
}

/* 確保按鈕內容水平居中對齊 */
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

/* 新增圓形按鈕樣式 */
.w-32-px {
  width: 32px;
}

.h-32-px {
  height: 32px;
}
/* 定義顏色樣式 (如果專案中沒有這些樣式類別) */
.bg-success-focus {
  background-color: rgba(0, 171, 85, 0.12);
}

.text-success-main {
  color: #00ab55;
}
.bg-danger-focus {
  background-color: rgba(255, 48, 48, 0.12);
}

.text-danger-main {
  color: #ff3030;
}
</style>