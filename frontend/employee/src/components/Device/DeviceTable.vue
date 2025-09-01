<template>
  <div class="card">
    <!-- 表頭：條數選擇 / 搜尋 / 上架篩選 / 新增按鈕 -->
    <div
      class="card-header d-flex flex-wrap align-items-center justify-content-between gap-3"
    >
      <div class="d-flex align-items-center gap-2">
        <span>顯示</span>
        <select
          v-model="selectedShow"
          class="form-select form-select-sm w-auto"
        >
          <option v-for="n in [10, 15, 20]" :key="n" :value="n">{{ n }}</option>
        </select>

        <!-- 修改：批次修改按鈕始終顯示，但在無選擇時顯示禁用狀態 -->
        <button
          @click="openBatchEditModal"
          class="btn btn-sm btn-outline-primary ms-3"
          :disabled="selectedIds.length === 0"
          :class="{ 'opacity-75': selectedIds.length === 0 }"
        >
          <i class="ri-edit-2-line"></i> 批次修改
          <span v-if="selectedIds.length > 0" class="badge bg-primary ms-1">{{
            selectedIds.length
          }}</span>
        </button>

        <!-- 新增：匯出 CSV 按鈕 -->
        <button
          @click="exportToCsv"
          class="btn btn-sm btn-outline-secondary ms-2"
          :disabled="exporting"
        >
          <i class="ri-file-download-line"></i>
          {{ exporting ? "匯出中..." : "匯出 CSV" }}
        </button>
      </div>

      <!-- 原先的批次操作區塊移除，因為我們已將按鈕移至頂部 -->

      <div class="icon-field position-relative">
        <input
          v-model="searchText"
          type="text"
          class="form-control form-control-sm w-auto"
          placeholder="搜尋名稱或 SKU"
        />
        <span class="icon"><iconify-icon icon="ion:search-outline" /></span>
      </div>
      <div class="d-flex align-items-center gap-2">
        <select
          v-model="selectedStatus"
          class="form-select form-select-sm w-auto"
        >
          <option value="">全部狀態</option>
          <option value="已上架">已上架</option>
          <option value="未上架">未上架</option>
        </select>
        <button @click="goToCreate" class="btn btn-sm btn-primary-600">
          <i class="ri-add-line"></i> 新增輔具
        </button>

        <!-- 批次刪除按鈕，同樣始終顯示 -->
        <button
          @click="confirmBatchDelete"
          class="btn btn-sm btn-outline-danger"
          :disabled="selectedIds.length === 0"
          :class="{ 'opacity-75': selectedIds.length === 0 }"
        >
          <i class="ri-delete-bin-line"></i> 批次刪除
          <span v-if="selectedIds.length > 0" class="badge bg-danger ms-1">{{
            selectedIds.length
          }}</span>
        </button>
      </div>
    </div>

    <!-- 表格主體 -->
    <div class="card-body table-responsive">
      <table class="table bordered-table mb-0">
        <thead>
          <tr>
            <!-- 修改：增加全選勾選框的標籤文字 -->
            <th class="align-middle">
              <div class="form-check">
                <input
                  type="checkbox"
                  class="form-check-input"
                  id="select-all"
                  v-model="selectAll"
                  @change="toggleSelectAll"
                />
                <label
                  class="form-check-label small text-muted"
                  for="select-all"
                  >全選</label
                >
              </div>
            </th>
            <th>S.L</th>
            <th>圖片</th>
            <th>名稱</th>
            <th>SKU</th>
            <th>單價</th>
            <th>庫存</th>
            <th>狀態</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(d, idx) in pagedData" :key="d.id">
            <!-- 修改：增加每行勾選框的樣式 -->
            <td class="align-middle">
              <div class="form-check">
                <input
                  type="checkbox"
                  class="form-check-input"
                  v-model="selectedIds"
                  :value="d.id"
                  :id="`select-item-${d.id}`"
                />
              </div>
            </td>
            <td class="align-middle">{{ startIndex + idx + 1 }}</td>
            <td class="align-middle">
              <div class="device-image">
                <img
                  :src="getImageUrl(d.image)"
                  alt="輔具圖片"
                  class="rounded"
                  @error="handleImageError"
                />
              </div>
            </td>
            <td class="align-middle">{{ d.name }}</td>
            <td class="align-middle">{{ d.sku }}</td>
            <td class="align-middle">{{ d.unitPrice }}</td>
            <td class="align-middle">{{ d.inventory }}</td>
            <td class="align-middle">
              <div class="d-flex align-items-center">
                <!-- 開關元件 -->
                <div class="form-check form-switch me-2">
                  <input
                    type="checkbox"
                    class="form-check-input status-toggle"
                    :id="`status-toggle-${d.id}`"
                    :checked="d.isOnline"
                    @change="handleStatusChange(d.id, $event)"
                  />
                </div>

                <!-- 狀態文字 (保留原有的文字顯示) -->
                <span :class="d.isOnline ? 'text-success' : 'text-muted'">
                  {{ d.isOnline ? "上架" : "下架" }}
                </span>
              </div>
            </td>
            <!-- 修改操作按鈕部分 -->
            <td class="align-middle">
              <div class="d-flex gap-2">
                <button
                  @click="showDeviceDetail(d.id)"
                  class="w-32-px h-32-px bg-primary-light text-primary-600 rounded-circle d-inline-flex align-items-center justify-content-center border-0"
                >
                  <iconify-icon icon="iconamoon:eye-light" />
                </button>
                <button
                  @click="edit(d.id)"
                  class="w-32-px h-32-px bg-success-focus text-success-main rounded-circle d-inline-flex align-items-center justify-content-center border-0"
                >
                  <iconify-icon icon="lucide:edit" />
                </button>
                <button
                  @click="remove(d.id)"
                  class="w-32-px h-32-px bg-danger-focus text-danger-main rounded-circle d-inline-flex align-items-center justify-content-center border-0"
                >
                  <iconify-icon icon="mingcute:delete-2-line" />
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>

      <!-- 分頁元件 -->
      <Pagination
        :currentPage="currentPage"
        :totalPages="totalPages"
        :startIndex="startIndex"
        :endIndex="endIndex"
        :totalItems="totalEntries"
        @page-changed="changePage"
      />
    </div>

    <!-- 各種彈窗元件 -->
    <DeviceDetailModal
      :show="showModal"
      :deviceId="selectedDeviceId"
      @close="closeModal"
      @deleted="handleDeviceDeleted"
    />

    <DeviceEditModal
      :show="showEditModal"
      :deviceId="selectedDeviceId"
      @close="closeEditModal"
      @updated="handleDeviceUpdated"
    />

    <DeviceCreateModal
      :show="showCreateModal"
      @close="closeCreateModal"
      @created="handleDeviceCreated"
    />

    <DeviceBatchEditModal
      :show="showBatchEditModal"
      :selectedIds="selectedIds"
      @close="closeBatchEditModal"
      @updated="handleBatchUpdated"
    />

    <!-- Toast 通知元件 -->
    <div v-if="toast.show" class="toast" :class="toastClass">
      <div class="d-flex justify-content-between align-items-center">
        <span>{{ toast.message }}</span>
        <button
          type="button"
          class="btn-close btn-sm text-white"
          @click="closeToast"
        ></button>
      </div>
    </div>

    <!-- 新增批次刪除確認彈窗 -->
    <div
      v-if="showDeleteConfirmModal"
      class="modal fade show delete-dialog-wrapper"
      tabindex="-1"
      role="dialog"
      style="display: block"
    >
      <div
        class="modal-dialog modal-dialog-centered delete-dialog-container"
        role="document"
      >
        <div class="modal-content delete-dialog-content">
          <div class="modal-header">
            <h5 class="modal-title">
              <i class="ri-delete-bin-line text-danger me-2"></i>
              確認刪除輔具
            </h5>
            <button
              type="button"
              class="btn-close"
              @click="cancelBatchDelete"
              aria-label="關閉"
            ></button>
          </div>
          <div class="modal-body">
            <div class="text-center mb-3">
              <div class="delete-icon-wrapper mb-3">
                <i class="ri-error-warning-line"></i>
              </div>
              <h5 class="mb-2">
                確定要刪除選定的 {{ selectedIds.length }} 個輔具嗎？
              </h5>
              <p class="text-muted">此操作無法復原，所有相關資料將永久刪除。</p>
            </div>

            <!-- 顯示要刪除的輔具清單 -->
            <div
              v-if="selectedDevicesForDelete.length > 0"
              class="device-list-container mt-3"
            >
              <h6 class="mb-2">即將刪除：</h6>
              <ul class="device-list">
                <li v-for="device in selectedDevicesForDelete" :key="device.id">
                  {{ device.name || `輔具 #${device.id}` }} (ID:
                  {{ device.id }})
                </li>
              </ul>
            </div>

            <!-- 警告提示區塊 -->
            <div class="alert alert-danger mt-3">
              <i class="ri-information-line me-2"></i>
              刪除後，該輔具的借用紀錄仍會保留，但將無法再借用此輔具。
            </div>
          </div>
          <div class="modal-footer delete-buttons">
            <button
              type="button"
              class="btn btn-secondary delete-btn-cancel"
              @click="cancelBatchDelete"
            >
              取消
            </button>
            <button
              type="button"
              class="btn btn-danger delete-btn-confirm"
              @click="executeBatchDelete"
              :disabled="batchDeleteProcessing"
            >
              <span
                v-if="batchDeleteProcessing"
                class="spinner-border spinner-border-sm me-2"
                role="status"
                aria-hidden="true"
              ></span>
              {{ batchDeleteProcessing ? "處理中..." : "確認刪除" }}
            </button>
          </div>
        </div>
      </div>
      <!-- 背景遮罩 -->
      <div class="modal-backdrop fade show delete-backdrop"></div>
    </div>

    <!-- 成功通知 -->
    <div
      v-if="showDeleteSuccessModal"
      class="modal fade show success-notification-wrapper"
      tabindex="-1"
      role="dialog"
      style="display: block"
    >
      <div
        class="modal-dialog modal-dialog-centered modal-sm success-dialog-container"
        role="document"
      >
        <div class="modal-content success-dialog-content">
          <div class="modal-body text-center p-4">
            <div class="mb-3">
              <i class="ri-check-line text-success success-icon"></i>
            </div>
            <h5 class="success-title">刪除成功</h5>
            <p class="success-message">
              已成功刪除 {{ deleteSuccessCount }} 個輔具
            </p>
            <button
              type="button"
              class="btn btn-success mt-2 success-btn-confirm"
              @click="closeDeleteSuccessModal"
            >
              確認
            </button>
          </div>
        </div>
      </div>
      <!-- 成功通知的背景遮罩 -->
      <div class="modal-backdrop fade show success-backdrop"></div>
    </div>
  </div>
</template>

<script setup>
// 引入所需模組
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import Pagination from "@/components/pagination/index.vue";
import { useDevice } from "@/composables/useDevice";
import DeviceDetailModal from "./DeviceDetailModal.vue";
import DeviceEditModal from './DeviceEditModal.vue';
import DeviceCreateModal from './DeviceCreateModal.vue';
import DeviceBatchEditModal from './DeviceBatchEditModal.vue';
// 確認刪除對話框
import { useConfirmDialog } from '@/composables/useConfirmDialog';

// 從 useDevice 取得所需方法與狀態
const {
  devices,
  loadDevices,
  toggleDeviceStatus,
  toast,
  closeToast,
  batchDelete,
  showToast
} = useDevice();

// 設定圖片基礎 URL
const baseImageUrl = "http://localhost:8080";
const router = useRouter();

// 新增：匯出 CSV 相關狀態
const exporting = ref(false);

// 狀態變數
const selectedShow = ref(10);
const searchText = ref("");
const selectedStatus = ref("");
const selectAll = ref(false);
const selectedIds = ref([]);
const currentPage = ref(1);

// 彈窗相關狀態
const showModal = ref(false);
const selectedDeviceId = ref(null);
const showEditModal = ref(false);
const showCreateModal = ref(false);
const showBatchEditModal = ref(false);

const showDeleteConfirmModal = ref(false);
const showDeleteSuccessModal = ref(false);
const batchDeleteProcessing = ref(false);
const selectedDevicesForDelete = ref([]);
const deleteSuccessCount = ref(0);
const { showConfirmDialog } = useConfirmDialog()

// 載入資料
onMounted(() => {
  loadDevices();
});

// 過濾與分頁計算
const filteredData = computed(() => {
  let data = devices.value || [];

  if (searchText.value) {
    const s = searchText.value.toLowerCase();
    data = data.filter(
      (d) => d.name.toLowerCase().includes(s) || d.sku.toLowerCase().includes(s)
    );
  }

  if (selectedStatus.value !== "") {
    if (selectedStatus.value === "已上架") {
      data = data.filter(d => d.isOnline === true);
    } else if (selectedStatus.value === "未上架") {
      data = data.filter(d => d.isOnline === false);
    }
  }

  return data;
});

const entriesPerPage = computed(() => selectedShow.value);
const totalEntries = computed(() => filteredData.value.length);
const totalPages = computed(() =>
  Math.ceil(totalEntries.value / entriesPerPage.value)
);
const startIndex = computed(
  () => (currentPage.value - 1) * entriesPerPage.value
);
const endIndex = computed(() =>
  Math.min(startIndex.value + entriesPerPage.value, totalEntries.value)
);
const pagedData = computed(() =>
  filteredData.value.slice(startIndex.value, endIndex.value)
);

// 全選/取消全選 - 修改為更安全的實作
function toggleSelectAll() {
  try {
    // 取得當前頁面的所有 ID
    const pageIds = pagedData.value.map(d => d.id);

    if (selectAll.value) {
      // 全選當前頁面的項目
      selectedIds.value = [...pageIds];
    } else {
      // 取消選擇
      selectedIds.value = [];
    }

    // 安全檢查，避免出現 undefined 或 null
    selectedIds.value = selectedIds.value.filter(id => id != null);

    console.log('選擇的 IDs:', selectedIds.value);
  } catch (error) {
    console.error('全選功能出錯:', error);
  }
}

// 換頁
function changePage(page) {
  currentPage.value = page;

  // 頁面切換時重設全選狀態
  selectAll.value = false;
}

// 顯示輔具詳情彈窗
function showDeviceDetail(id) {
  selectedDeviceId.value = id;
  showModal.value = true;
}

// 關閉詳情彈窗
function closeModal() {
  showModal.value = false;
  setTimeout(() => {
    selectedDeviceId.value = null;
  }, 300);
}

// 處理輔具被刪除的情況
function handleDeviceDeleted() {
  loadDevices();
}

// 編輯輔具
function edit(id) {
  selectedDeviceId.value = id;
  showEditModal.value = true;
}

// 刪除輔具
async function remove(id) {
  try {
    const confirmed = await showConfirmDialog({
      title: '刪除輔具',
      message: '您確定要刪除此輔具嗎？此操作無法復原。',
      type: 'warning',
      confirmText: '確認刪除',
      cancelText: '取消',
      icon: 'mdi:alert-circle-outline'
    });
    if (!confirmed) return;

    await useDevice().removeDevice(id);

    // 顯示刪除成功視窗
    deleteSuccessCount.value = 1; // 單筆刪除成功數
    showDeleteSuccessModal.value = true;

    // 重新載入資料
    loadDevices();
  } catch (err) {
    if (err.response) {
      if (err.response.status === 404) {
        showToast("找不到該輔具", "error");
      } else if (err.response.status === 409) {
        showToast(err.response.data, "error"); 
      } else {
        showToast("刪除失敗，請稍後再試", "error");
      }
    } else {
      showToast("無法連線伺服器", "error");
    }
  }
}

// 創建輔具
function goToCreate() {
  showCreateModal.value = true;
}

// 關閉創建彈窗
function closeCreateModal() {
  showCreateModal.value = false;
}

// 處理輔具新增成功
function handleDeviceCreated() {
  loadDevices();
  showToast('輔具新增成功！', 'success');
}

// 圖片 URL 處理
function getImageUrl(imagePath) {
  if (!imagePath) {
    return '/assets/images/default-device.jpg';
  }

  if (imagePath.startsWith('http')) {
    return imagePath;
  }

  return `${baseImageUrl}/${imagePath}`;
}

// 處理圖片載入失敗
function handleImageError(e) {
  e.target.src = '/assets/images/default-device.jpg';
}

// 關閉編輯彈窗
function closeEditModal() {
  showEditModal.value = false;
  setTimeout(() => {
    selectedDeviceId.value = null;
  }, 300);
}

// 處理輔具更新成功
function handleDeviceUpdated() {
  loadDevices();
}

// 處理狀態切換
function handleStatusChange(id, event) {
  const newStatus = event.target.checked;

  toggleDeviceStatus(id, newStatus).catch(error => {
    console.error('狀態切換失敗:', error);
    event.target.checked = !newStatus;
  });
}

// 計算 toast 的樣式類別
const toastClass = computed(() => {
  if (!toast.value.show) return '';

  switch (toast.value.type) {
    case 'success': return 'toast-success';
    case 'error': return 'toast-error';
    default: return 'toast-info';
  }
});

// 批次修改相關功能
function openBatchEditModal() {
  if (selectedIds.value.length === 0) {
    showToast('請至少選擇一項輔具進行批次修改', 'warning');
    return;
  }
  showBatchEditModal.value = true;
}

function closeBatchEditModal() {
  showBatchEditModal.value = false;
}

function handleBatchUpdated() {
  loadDevices();
  selectedIds.value = [];
  selectAll.value = false;
}

// 批次刪除
function confirmBatchDelete() {
  if (selectedIds.value.length === 0) {
    showToast('請至少選擇一項輔具', 'error');
    return;
  }

  // 收集要刪除的輔具基本資訊
  selectedDevicesForDelete.value = pagedData.value
    .filter(device => selectedIds.value.includes(device.id))
    .map(device => ({
      id: device.id,
      name: device.name
    }));

  // 顯示確認對話框
  showDeleteConfirmModal.value = true;
}

// 取消批次刪除
function cancelBatchDelete() {
  showDeleteConfirmModal.value = false;
}

// 執行批次刪除
async function executeBatchDelete() {
  batchDeleteProcessing.value = true;

  try {
    await batchDelete(selectedIds.value);

    // 成功刪除的數量
    deleteSuccessCount.value = selectedIds.value.length;

    // 關閉確認對話框，顯示成功通知
    showDeleteConfirmModal.value = false;
    showDeleteSuccessModal.value = true;

    // 重新載入資料並清空選擇
    await loadDevices();
    selectedIds.value = [];
    selectAll.value = false;
  } catch (err) {
    if (err.response) {
      if (err.response.status === 404) {
        showToast("部分或全部輔具不存在，刪除失敗", "error");
      } else if (err.response.status === 409) {
        showToast(err.response.data, "error"); 
      } else {
        showToast("批次刪除失敗，請稍後再試", "error");
      }
    } else {
      showToast("無法連線伺服器", "error");
    }
    showDeleteConfirmModal.value = false;
  } finally {
    batchDeleteProcessing.value = false;
  }
}

// 關閉刪除成功通知
function closeDeleteSuccessModal() {
  showDeleteSuccessModal.value = false;
}

// 匯出 CSV 功能
async function exportToCsv() {
  // 避免重複點擊
  if (exporting.value) return;

  exporting.value = true;

  try {
    // 1. 呼叫 API 取得 CSV 資料
    const csvContent = await useDevice().exportCsv();

    // 2. 檢查回傳的資料是否有效
    if (!csvContent) {
      throw new Error('無法取得匯出資料');
    }

    // 3. 設定檔案名稱 (使用當前時間戳記)
    const fileName = `輔具資料_${new Date().toISOString().slice(0, 10)}.csv`;

    // 4. 建立 Blob 物件和下載連結
    const blob = new Blob(["\ufeff" + csvContent], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');

    // 5. 設定下載連結屬性
    link.setAttribute('href', url);
    link.setAttribute('download', fileName);
    link.style.display = 'none';

    // 6. 將連結加到 DOM 中並觸發點擊
    document.body.appendChild(link);
    link.click();

    // 7. 清理 DOM
    document.body.removeChild(link);
    URL.revokeObjectURL(url);

    // 8. 顯示成功訊息
    showToast('輔具資料已成功匯出為 CSV 檔案', 'success');

  } catch (error) {
    console.error('匯出 CSV 失敗:', error);
    showToast(`匯出失敗: ${error.message || '未知錯誤'}`, 'error');
  } finally {
    exporting.value = false;
  }
}
</script>

<style scoped>
.icon-field {
  position: relative;
}
.icon-field .icon {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  pointer-events: none;
}

/* 圓形按鈕樣式 */
.w-32-px {
  width: 32px;
}

.h-32-px {
  height: 32px;
}

/* 背景和文字顏色 */
.bg-primary-light {
  background-color: rgba(69, 128, 255, 0.1);
}

.text-primary-600 {
  color: #4580ff;
}

.bg-success-focus {
  background-color: rgba(52, 195, 143, 0.1);
}

.text-success-main {
  color: #34c38f;
}

.bg-danger-focus {
  background-color: rgba(244, 106, 106, 0.1);
}

.text-danger-main {
  color: #f46a6a;
}

/* 圓形按鈕懸停效果 */
.rounded-circle:hover {
  opacity: 0.8;
}

/* 輔具圖片樣式 */
.device-image {
  width: 40px;
  height: 40px;
  overflow: hidden;
  border-radius: 50%;
}
.device-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 表格樣式調整 */
.table {
  width: 100%;
  table-layout: fixed; /* 固定表格佈局 */
}
.table th,
.table td {
  height: 60px; /* 固定行高 */
  vertical-align: middle; /* 垂直居中 */
}
.bordered-table th,
.bordered-table td {
  border-bottom: 1px solid #e9ecef;
  border-right: none;
}
.bordered-table tbody tr:last-child td {
  border-bottom: none;
}

/* toast 通知樣式 */
.toast {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 1050;
  min-width: 200px;
  max-width: 300px;
  padding: 16px;
  border-radius: 8px;
  color: #fff;
  font-size: 14px;
  line-height: 1.5;
  opacity: 0.9;
  transition: opacity 0.3s ease;
}
.toast-success {
  background-color: #28a745;
}
.toast-error {
  background-color: #dc3545;
}
.toast-info {
  background-color: #007bff;
}

/* 刪除對話框樣式 */
.delete-dialog-wrapper {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 9999 !important; /* 確保顯示在最上層 */
  overflow-x: hidden;
  overflow-y: auto;
  outline: 0;
  pointer-events: auto !important;
}

.delete-dialog-container {
  pointer-events: auto !important;
  position: relative;
  z-index: 10000 !important;
}

.delete-dialog-content {
  pointer-events: auto !important;
  position: relative;
  z-index: 10001 !important;
  box-shadow: 0 0.5rem 1rem rgba(0, 0, 0, 0.15);
}

/* 設置刪除按鈕區域樣式 */
.delete-buttons {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
  position: relative;
  z-index: 10002 !important;
  pointer-events: auto !important;
}

/* 設置確認和取消按鈕樣式 */
.delete-btn-cancel,
.delete-btn-confirm {
  position: relative;
  z-index: 10003 !important;
  pointer-events: auto !important;
  cursor: pointer !important;
}

/* 設置背景遮罩樣式 */
.delete-backdrop {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 9998 !important; /* 略低於對話框但高於其他元素 */
  background-color: rgba(0, 0, 0, 0.5);
  pointer-events: auto !important;
}

/* 刪除圖標樣式 */
.delete-icon-wrapper {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background-color: rgba(244, 67, 54, 0.1);
  color: #f44336;
}

.delete-icon-wrapper i {
  font-size: 40px;
}

/* 輔具列表樣式 */
.device-list-container {
  background-color: #f8f9fa;
  border-radius: 0.25rem;
  padding: 1rem;
}

.device-list {
  max-height: 150px;
  overflow-y: auto;
  padding-left: 1.5rem;
  margin-bottom: 0;
}

/* 成功通知樣式 */
.success-notification-wrapper {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 11000 !important; /* 使用極高的 z-index 確保顯示在最上層 */
  overflow-x: hidden;
  overflow-y: auto;
  outline: 0;
  pointer-events: auto !important;
}

.success-dialog-container {
  pointer-events: auto !important;
  position: relative;
  z-index: 11001 !important;
}

.success-dialog-content {
  pointer-events: auto !important;
  position: relative;
  z-index: 11002 !important;
  box-shadow: 0 0.5rem 1rem rgba(0, 0, 0, 0.15);
}

/* 成功圖標樣式 */
.success-icon {
  font-size: 3rem;
  display: inline-block;
  width: 80px;
  height: 80px;
  line-height: 80px;
  border-radius: 50%;
  background-color: rgba(52, 195, 143, 0.2);
  color: #34c38f;
}

/* 成功訊息相關元素樣式 */
.success-title {
  font-weight: 600;
  margin-bottom: 0.5rem;
  color: #34c38f;
}

.success-message {
  font-size: 1rem;
  margin-bottom: 1rem;
}

/* 設置確認按鈕樣式 */
.success-btn-confirm {
  position: relative;
  z-index: 11003 !important;
  pointer-events: auto !important;
  cursor: pointer !important;
  min-width: 100px;
}

/* 設置背景遮罩樣式 */
.success-backdrop {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 10999 !important; /* 略低於對話框但高於其他元素 */
  background-color: rgba(0, 0, 0, 0.5);
  pointer-events: auto !important;
}

/* 修改：增加全選勾選框的標籤文字 */
.form-check-label.small {
  font-size: 0.75rem;
  margin-left: 0.25rem;
}

/* 讓勾選框更明顯 */
.form-check-input {
  border: 1.5px solid #ced4da;
  cursor: pointer;
}

.form-check-input:checked {
  background-color: #4580ff;
  border-color: #4580ff;
}
</style>
