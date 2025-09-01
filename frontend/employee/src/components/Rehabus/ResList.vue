<template>
  <div class="card border-0 shadow-sm">
    <div class="card-header bg-transparent d-flex justify-content-between align-items-center">
      <div class="d-flex align-items-center">
        <!-- 搜尋欄位 -->
        <div class="d-flex me-3 gap-2">
          <div class="input-group">
            <span class="input-group-text bg-transparent">會員編號</span>
            <input 
              type="number" 
              class="form-control search-input" 
              placeholder="會員ID"
              v-model="searchMemberId"
              min="0"
            >
          </div>
          
          <div class="input-group">
            <span class="input-group-text bg-transparent">預約日期</span>
            <input 
              type="date" 
              class="form-control search-input"
              v-model="searchScheduledDate"
            >
          </div>
        
          <!-- 搜尋按鈕 -->
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
              <th>#</th>
              <th>會員編號</th>
              <th>巴士編號</th>
              <th>起點</th>
              <th>終點</th>
              <th>預約時間</th>
              <th>距離</th>
              <th>價格</th>
              <th>狀態</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <!-- 載入中顯示 -->
            <tr v-if="loading">
              <td colspan="10" class="text-center py-4">
                <div class="spinner-border text-primary" role="status">
                  <span class="visually-hidden">載入中...</span>
                </div>
              </td>
            </tr>
            <!-- 無資料顯示 -->
            <tr v-else-if="reservationList.length === 0">
              <td colspan="10" class="text-center py-4">
                找不到符合條件的預約資料
              </td>
            </tr>
            <!-- 資料列表 -->
            <tr v-for="reservation in reservationList" :key="reservation.id">
              <td>{{ reservation.id }}</td>
              <td>{{ reservation.memberId }}</td>
              <td>{{ reservation.busId }}</td>
              <td>{{ reservation.startAddress }}</td>
              <td>{{ reservation.endAddress }}</td>
              <td>{{ formatDateTime(reservation.scheduledAt) }}</td>
              <td>{{ reservation.formattedDistance || '—' }}</td>
              <td>{{ reservation.formattedPrice || '—' }}</td>
              <td>
                <span
                  :class="getStatusClass(reservation.statusNormalized)"
                  class="badge rounded-pill"
                  :title="reservation.completedAt ? '完成時間：' + formatDateTime(reservation.completedAt) : '尚未完成'">
                  {{ getStatusText(reservation.statusNormalized) }}
                </span>
              </td>
              <!-- 操作欄位 -->
              <td>
                <div class="d-flex gap-2">
                  <div class="d-flex gap-2 position-relative">
                    <!-- 編輯按鈕 -->
                    <a href="#" @click.prevent="handleEdit(reservation.id)"
                       class="w-32-px h-32-px bg-success-focus text-success-main rounded-circle d-inline-flex align-items-center justify-content-center">
                      <iconify-icon icon="lucide:edit"></iconify-icon>
                    </a>
                    <!-- 刪除按鈕 -->
                    <a href="#" @click.prevent="handleDelete(reservation.id)"
                       class="w-32-px h-32-px bg-danger-focus text-danger-main rounded-circle d-inline-flex align-items-center justify-content-center">
                      <iconify-icon icon="mingcute:delete-2-line"></iconify-icon>
                    </a>
                    <!-- 完成 / 檢視完乘時間（同位置：未完成→藍色勾；已完成→灰色眼睛） -->
                    <a
                      href="#"
                      @click.prevent="handleCompleteOrShow(reservation)"
                      class="w-32-px h-32-px rounded-circle d-inline-flex align-items-center justify-content-center"
                      :class="reservation.statusNormalized !== 'completed'
                                ? 'bg-info-focus text-info-main'
                                : 'bg-secondary-focus text-secondary-main'"
                      :title="reservation.statusNormalized !== 'completed' ? '標記為已完成' : '查看完乘時間'">
                      <iconify-icon :icon="reservation.statusNormalized !== 'completed'
                                            ? 'mdi:check'
                                            : 'mdi:eye-outline'"></iconify-icon>
                    </a>

                    <!-- 小框框 Popover（就地顯示完乘時間，不跳頁） -->
                    <div v-if="popover.visible && popover.targetId === reservation.id"
                         class="popover-box">
                      <div class="popover-arrow"></div>
                      <div class="small">
                        完乘時間<br/>
                        <strong>{{ formatDateTime(popover.completedAt) }}</strong>
                      </div>
                    </div>
                  </div>
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
      <nav aria-label="預約列表分頁">
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
import { reservationService } from '@/services/busReservationService';
import { useToast } from '@/composables/useToast';
import { useConfirmDialog } from '@/composables/useConfirmDialog'; // **重點：引入確認對話框**

export default {
  name: 'ReservationList',
  setup() {
    // 基本狀態
    const reservationList = ref([]);
    const loading = ref(true);
    const error = ref(null);
    const router = useRouter();
    const { showToast } = useToast();
    const { showConfirmDialog } = useConfirmDialog(); // **重點：解構出確認對話框方法**

    // 分頁相關
    const currentPage = ref(1);
    const pageSize = ref(10);
    const totalItems = ref(0);
    
    // 搜尋參數
    const searchMemberId = ref('');
    const searchStartAddress = ref('');
    const searchEndAddress = ref('');
    const searchScheduledDate = ref('');
    
    // 強制刷新控制
    const refreshTrigger = ref(0);

    // 計算總頁數
    const totalPages = computed(() => {
      return Math.ceil(totalItems.value / pageSize.value) || 1;
    });

    // 載入預約資料
    const loadReservations = async (searchParams = {}) => {
      loading.value = true;
      try {
        console.log('搜尋參數:', searchParams);
        
        // **重點：清理搜尋參數，確保沒有空白或 undefined**
        const cleanParams = {};
        Object.entries(searchParams).forEach(([key, value]) => {
          if (value !== undefined && value !== null && String(value).trim() !== '' && value !== 'undefined') {
            cleanParams[key] = value;
          }
        });
        
        let data;
        // 判斷是否有搜尋條件
        if (Object.keys(cleanParams).length > 0) {
          // 有搜尋條件時使用搜尋 API
          console.log('使用搜尋條件:', cleanParams);
          data = await reservationService.searchReservations(cleanParams);
        } else {
          // 無搜尋條件時獲取全部資料
          console.log('獲取所有資料');
          data = await reservationService.getAllReservation();
        }
        
        // **重點：資料後處理 - 確保每筆資料都有狀態值**
        let filteredData = data.map(r => {
          const norm = normalizeStatus(r.reservationStatus);
          const completed = r.completedAt ?? r.COMPLETED_AT ?? r.completed_at ?? null;
          return {
            ...r,
            completedAt: completed,
            statusNormalized: norm,
            reservationStatus: r.reservationStatus ?? norm,
            display: {
              ...(r.display || {}),
              statusText: getStatusText(norm)
            }
          };
        });

        // 更新總筆數
        totalItems.value = filteredData.length;
        
        // 分頁處理
        const startIndex = (currentPage.value - 1) * pageSize.value;
        const endIndex = startIndex + pageSize.value;
        reservationList.value = filteredData.slice(startIndex, endIndex);
        
        // 確保狀態顯示正確
        console.log('載入的預約資料:', reservationList.value);

      } catch (err) {
        error.value = err.message;
        console.error('載入預約資料失敗:', err);
      
        // **重點：出錯時顯示空資料而非中斷**
        reservationList.value = [];
        totalItems.value = 0;
      } finally {
        loading.value = false;
      }
    };

    // 處理頁面變更
    const handlePageChange = (page) => {
      if (page < 1 || page > totalPages.value) return;
      currentPage.value = page;
      loadReservations();
    };

    // 處理搜尋
    const handleSearch = () => {
      // **重點：驗證會員 ID**
      if (searchMemberId.value && isNaN(parseInt(searchMemberId.value))) {
        showToast({
          title: '搜尋錯誤',
          message: '會員編號必須是數字',
          type: 'error'
        });
        return;
      }

      // **重點：清理參數，移除空白項目**
      const params = {};
    
      if (searchMemberId.value) {
        params.memberId = parseInt(searchMemberId.value);
      }

      if (searchScheduledDate.value) {
        params.scheduledDate = searchScheduledDate.value;
      }

      // 執行搜尋前重置為第一頁
      currentPage.value = 1;
      
      // 使用篩選後的參數進行搜尋
      loadReservations(params);
    };

    // 手動重新載入資料
    const refreshList = () => {
      refreshTrigger.value++; // 觸發重新載入
      loadReservations();
    };

    // 處理編輯
    const handleEdit = (id) => {
      // **重點：導航到編輯頁面前驗證 ID**
      if (id === undefined || id === null) {
        showToast({
          title: '編輯錯誤',
          message: '這筆資料沒有 id，無法編輯',
          type: 'error'
        });
        return;
      }
      router.push({ name: 'reservation-edit', params: { id: String(id) } });
    };

    // **重點：使用 useConfirmDialog 處理刪除確認**
    const handleDelete = async (id) => {
      try {
        // **重點：取得要刪除的預約資訊用於顯示詳細訊息**
        const reservationToDelete = reservationList.value.find(reservation => reservation.id === id);
        const reservationInfo = reservationToDelete ? 
          `預約編號：${reservationToDelete.id}\n會員編號：${reservationToDelete.memberId}\n起點：${reservationToDelete.startAddress}\n終點：${reservationToDelete.endAddress}\n預約時間：${formatDateTime(reservationToDelete.scheduledAt)}` : 
          `預約編號：${id}`;

        // **重點：使用 showConfirmDialog 顯示確認對話框**
        const confirmed = await showConfirmDialog({
          title: '確認刪除預約記錄',
          message: `您確定要刪除以下預約記錄嗎？\n\n${reservationInfo}\n\n此操作無法復原。`,
          type: 'error', // **重點：使用 error 類型以突出危險性**
          confirmText: '確認刪除',
          cancelText: '取消',
          confirmButtonClass: 'btn-danger',
          icon: 'heroicons:trash'
        });

        // **重點：只有在使用者確認後才執行刪除**
        if (confirmed) {
          // **重點：顯示載入狀態（可選）**
          const reservationIndex = reservationList.value.findIndex(reservation => reservation.id === id);
          if (reservationIndex !== -1) {
            // 可以在這裡添加載入狀態指示
          }

          // **重點：執行實際的刪除操作**
          await reservationService.deleteReservation(id);
          
          // **重點：刪除成功後顯示成功訊息**
          showToast({
            title: '刪除成功',
            message: `預約記錄 ${id} 已成功刪除`,
            type: 'success'
          });

          // **重點：重新載入資料以更新列表**
          await loadReservations();
        }
      } catch (err) {
        console.error('刪除預約失敗:', err);
        
        // **重點：顯示錯誤訊息**
        showToast({
          title: '刪除失敗',
          message: err.message || '刪除預約記錄時發生未知錯誤，請稍後再試',
          type: 'error'
        });
      }
    };

    // Popover 狀態
    const popover = ref({ visible: false, targetId: null, completedAt: null });
    let popTimer = null;

    function openPopover(id, completedAt) {
      popover.value = { visible: true, targetId: id, completedAt };
      if (popTimer) clearTimeout(popTimer);
      popTimer = setTimeout(() => (popover.value.visible = false), 2500); // 2.5 秒自動關
    }

    // 完成或顯示（同一顆按鈕）
    const handleCompleteOrShow = async (reservation) => {
      const id = reservation.id;

      // **重點：未完成 → 送後端標記完成，更新該列，並顯示小框框**
      if (reservation.statusNormalized !== 'completed') {
        // 改用自訂確認對話框
        const confirmed = await showConfirmDialog({
          title: '標記為已完成',
          message: `確定要將此預約標記為已完成嗎？\n\n預約編號：${reservation.id}\n會員編號：${reservation.memberId}\n起點：${reservation.startAddress}\n終點：${reservation.endAddress}\n預約時間：${formatDateTime(reservation.scheduledAt)}`,
          type: 'info',
          confirmText: '確定',
          cancelText: '取消',
          confirmButtonClass: 'btn-info',
          icon: 'mdi:check'
        });
        if (!confirmed) return;
        try {
          const updated = await reservationService.completeReservation(id);
          if (updated) {
            const idx = reservationList.value.findIndex(r => String(r.id) === String(updated.id));
            if (idx !== -1) {
              Object.assign(reservationList.value[idx], {
                reservationStatus: updated.reservationStatus,
                statusNormalized: updated.reservationStatus,
                completedAt: updated.completedAt,
                distanceMeters: updated.distanceMeters,
                price: updated.price,
                formattedDistance: updated.formattedDistance,
                formattedPrice: updated.formattedPrice,
                display: { ...(reservationList.value[idx].display || {}), statusText: updated.display.statusText }
              });
            }
            openPopover(id, updated.completedAt || new Date().toISOString());
          } else {
            // 後端回 204：全表重載再顯示
            await loadReservations();
            const row = reservationList.value.find(r => String(r.id) === String(id));
            openPopover(id, row?.completedAt ?? new Date().toISOString());
          }
        } catch (err) {
          showToast({
            title: '操作失敗',
            message: err.message,
            type: 'error'
          });
        }
        return;
      }

      // **重點：已完成 → 直接顯示小框框（若沒有 cached 時間，就補打一筆查詢）**
      let time = reservation.completedAt;
      if (!time) {
        try {
          const detail = await reservationService.getReservationById(id);
          time = detail.completedAt || detail.COMPLETED_AT || null;
        } catch (e) { /* 忽略錯誤，顯示空 */ }
      }
      openPopover(id, time);
    };

    // **重點：後端→前端狀態正規化**
    const normalizeStatus = (raw) => {
      const s = String(raw || '').trim().toLowerCase();
      if (!s) return 'pending';

      // 直接支援的
      if (['pending', 'in_progress', 'completed', 'cancelled'].includes(s)) return s;

      // 常見別名／資料庫值對應
      if (['active', 'waiting'].includes(s)) return 'pending';
      if (['processing'].includes(s)) return 'in_progress';
      if (['done', 'complete'].includes(s)) return 'completed';
      if (['canceled', 'cancel'].includes(s)) return 'cancelled';

      // 其他未知 → 當成 pending
      return 'pending';
    };

    // **重點：狀態中文**
    const getStatusText = (status) => {
      switch (normalizeStatus(status)) {
        case 'pending': return '待處理';
        case 'in_progress': return '進行中';
        case 'completed': return '已完成';
        case 'cancelled': return '已取消';
        default: return '未知狀態';
      }
    };

    // **重點：格式化日期時間**
    const formatDateTime = (dateTimeString) => {
      if (!dateTimeString) return '未設定';
      try {
        const date = new Date(dateTimeString);
        return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
      } catch (error) {
        return dateTimeString;
      }
    };

    // **重點：狀態樣式（用正規化結果）**
    const getStatusClass = (status) => {
      switch (normalizeStatus(status)) {
        case 'pending':     return 'bg-warning'; // 黃
        case 'in_progress': return 'bg-primary'; // 藍
        case 'completed':   return 'bg-success'; // 綠
        case 'cancelled':   return 'bg-danger';  // 紅
        default:            return 'bg-warning';
      }
    };

    // **重點：監聽路由變更，從編輯頁返回時重新載入資料**
    watch(() => router.currentRoute.value.fullPath, (newPath) => {
      if (newPath === '/reservation/list') {
        loadReservations();
      }
    });

    // **重點：頁面載入時獲取資料**
    onMounted(() => {
      loadReservations();
    
      // 添加全域事件監聽器，用於其他頁面通知列表刷新
      window.addEventListener('reservation-status-updated', () => {
        loadReservations();
      });
    });

    return {
      reservationList,
      loading,
      error,
      currentPage,
      totalPages,
      totalItems,
      searchMemberId,
      searchStartAddress,
      searchEndAddress,
      searchScheduledDate,
      handlePageChange,
      handleSearch,
      handleEdit,
      handleDelete,
      handleCompleteOrShow,
      popover,
      refreshList,
      normalizeStatus,
      getStatusText, 
      getStatusClass,
      formatDateTime
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

/* 水平文字排列樣式 */
.horizontal-text {
  display: flex;
  flex-direction: row;
  align-items: center;
  letter-spacing: 0.5px;
}

/* 確保按鈕內容水平居中對齊 */
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

/* 圓形按鈕樣式 */
.w-32-px {
  width: 32px;
}

.h-32-px {
  height: 32px;
}

/* 顏色樣式 */
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

.bg-info-focus {
  background-color: rgba(24, 144, 255, 0.12);
}

.text-info-main {
  color: #1890ff;
}

/* 狀態標記樣式 */
.status-badge {
  width: 12px;
  height: 12px;
}

.bg-warning {
  background-color: #ffc107 !important; /* 黃色 - 待處理 */
}

.bg-primary {
  background-color: #0d6efd !important; /* 藍色 - 進行中 */
}

.bg-success {
  background-color: #198754 !important; /* 綠色 - 已完成 */
}

.bg-danger {
  background-color: #dc3545 !important; /* 紅色 - 已取消 */
}

/* 灰色眼睛鈕 */
.bg-secondary-focus { 
  background-color: rgba(108, 117, 125, .12); 
}

.text-secondary-main { 
  color: #6c757d; 
}

/* 小框框 Popover */
.popover-box{
  z-index: 1050;
  position: absolute;
  top: 50%;
  left: calc(100% + 8px);      /* 在按鈕群右側 */
  transform: translateY(-50%);
  background: #fff;
  border: 1px solid rgba(0,0,0,.125);
  border-radius: 8px;
  padding: 6px 10px;
  box-shadow: 0 4px 16px rgba(0,0,0,.12);
  z-index: 10;
  white-space: nowrap;
}

.popover-arrow{
  position: absolute;
  left: -6px;
  top: 50%;
  transform: translateY(-50%);
  width: 0; height: 0;
  border-top: 6px solid transparent;
  border-bottom: 6px solid transparent;
  border-right: 6px solid #fff;
  filter: drop-shadow(-1px 0 0 rgba(0,0,0,.125));
}
</style>