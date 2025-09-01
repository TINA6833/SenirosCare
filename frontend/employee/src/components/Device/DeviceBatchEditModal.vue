<template>
  <div
    v-if="show"
    class="modal fade show"
    tabindex="-1"
    role="dialog"
    style="display: block"
  >
    <div class="modal-dialog modal-xl" role="document">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">批次修改輔具</h5>
          <button
            type="button"
            class="btn-close"
            @click="$emit('close')"
            aria-label="關閉"
          ></button>
        </div>
        <div class="modal-body">
          <div v-if="loading" class="text-center py-5">
            <div class="spinner-border text-primary" role="status">
              <span class="visually-hidden">載入中...</span>
            </div>
            <p class="mt-2">載入輔具資料中...</p>
          </div>

          <div v-else>
            <!-- 說明資訊區塊 -->
            <div class="alert alert-info mb-4">
              <i class="ri-information-line me-2"></i>
              已選擇 {{ selectedIds.length }} 個輔具進行個別修改。
              <strong>您可以針對每個輔具進行不同欄位的修改，未修改的欄位將保持原值。</strong>
            </div>
            
            <!-- 個別輔具修改表格 -->
            <div class="table-responsive">
              <table class="table table-bordered">
                <thead class="table-light">
                  <tr>
                    <th style="width: 60px;">編號</th>
                    <th style="width: 200px;">輔具名稱</th>
                    <th style="width: 120px;">單價 (NT$)</th>
                    <th style="width: 100px;">庫存</th>
                    <th style="width: 100px;">狀態</th>
                    <th style="width: 160px;">分類</th>
                    <th style="width: 120px;">SKU</th>
                  </tr>
                </thead>
                <tbody>
                  <!-- 為每個輔具顯示一行編輯欄位 -->
                  <tr v-for="(device, index) in devicesData" :key="device.id">
                    <td class="align-middle">{{ device.id }}</td>
                    <td>
                      <input 
                        type="text" 
                        class="form-control form-control-sm form-field"
                        v-model="device.name"
                        placeholder="輔具名稱"
                      />
                    </td>
                    <td>
                      <input 
                        type="number" 
                        class="form-control form-control-sm form-field" 
                        v-model.number="device.unitPrice"
                        min="0"
                        step="1"
                        placeholder="單價"
                      />
                    </td>
                    <td>
                      <input 
                        type="number" 
                        class="form-control form-control-sm form-field" 
                        v-model.number="device.inventory"
                        min="0"
                        step="1"
                        placeholder="庫存"
                      />
                    </td>
                    <td>
                      <div class="form-check form-switch d-flex justify-content-center">
                        <input 
                          type="checkbox" 
                          class="form-check-input status-toggle form-field" 
                          :id="`status-toggle-${device.id}`"
                          v-model="device.isOnline"
                        />
                        <label class="form-check-label ms-1 small" :for="`status-toggle-${device.id}`">
                          {{ device.isOnline ? '上架' : '下架' }}
                        </label>
                      </div>
                    </td>
                    <td>
                      <select 
                        class="form-select form-select-sm form-field" 
                        v-model="device.categoryId"
                      >
                        <option value="" disabled>請選擇分類</option>
                        <option 
                          v-for="category in categories" 
                          :key="category.id" 
                          :value="category.id"
                        >
                          {{ category.name }}
                        </option>
                      </select>
                    </td>
                    <td>
                      <input 
                        type="text" 
                        class="form-control form-control-sm form-field" 
                        v-model="device.sku"
                        placeholder="SKU 編號"
                      />
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
            
            <!-- 提示訊息 -->
            <div v-if="devicesData.length === 0" class="alert alert-warning">
              無法載入輔具資料，請重試
            </div>
          </div>
        </div>
        <!-- 底部按鈕區域 -->
        <div class="modal-footer footer-buttons">
          <button
            type="button"
            class="btn btn-secondary action-button"
            @click="$emit('close')"
          >
            取消
          </button>
          <button
            type="button"
            class="btn btn-primary action-button"
            :disabled="processing || devicesData.length === 0"
            @click="showConfirmDialog"
          >
            <span v-if="processing" class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
            {{ processing ? '處理中...' : '確認批次修改' }}
          </button>
        </div>
      </div>
    </div>
    <!-- 背景遮罩 -->
    <div class="modal-backdrop fade show"></div>
  </div>

  <!-- 確認對話框 - 重新設計以確保可見性和交互性 -->
  <div 
    v-if="confirmDialogVisible" 
    class="modal fade show confirm-dialog-wrapper" 
    tabindex="-1" 
    role="dialog"
    style="display: block"
  >
    <div class="modal-dialog modal-dialog-centered confirm-dialog-container" role="document">
      <div class="modal-content confirm-dialog-content">
        <div class="modal-header">
          <h5 class="modal-title">確認批次修改</h5>
          <button 
            type="button" 
            class="btn-close" 
            @click="confirmDialogVisible = false"
            aria-label="關閉"
          ></button>
        </div>
        <div class="modal-body">
          <p class="mb-2">您確定要批次修改以下 {{ devicesData.length }} 個輔具嗎？</p>
          <ul class="small text-muted mb-0 device-list">
            <li v-for="device in devicesData" :key="device.id">
              {{ device.name }} (ID: {{ device.id }})
            </li>
          </ul>
          <div class="alert alert-warning mt-3 mb-0">
            <i class="ri-information-line me-1"></i>
            此操作將立即生效，且無法復原。
          </div>
        </div>
        <div class="modal-footer confirm-buttons">
          <button 
            type="button" 
            class="btn btn-secondary confirm-btn-cancel" 
            @click="confirmDialogVisible = false"
          >
            取消
          </button>
          <button 
            type="button" 
            class="btn btn-danger confirm-btn-confirm" 
            @click="handleConfirm"
            :disabled="processing"
          >
            <span v-if="processing" class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
            {{ processing ? '處理中...' : '確認修改' }}
          </button>
        </div>
      </div>
    </div>
    <!-- 確認對話框的背景遮罩 - 提高 z-index 以確保顯示在最上層 -->
    <div class="modal-backdrop fade show confirm-backdrop"></div>
  </div>
  
  <!-- 成功通知 -->
  <div 
    v-if="successNotificationVisible" 
    class="modal fade show success-notification-wrapper" 
    tabindex="-1" 
    role="dialog"
    style="display: block"
  >
    <div class="modal-dialog modal-dialog-centered modal-sm success-dialog-container" role="document">
      <div class="modal-content success-dialog-content">
        <div class="modal-body text-center p-4">
          <div class="mb-3">
            <i class="ri-check-line text-success success-icon"></i>
          </div>
          <h5 class="success-title">修改成功</h5>
          <p class="success-message">已成功更新 {{ devicesData.length }} 個輔具</p>
          <button 
            type="button" 
            class="btn btn-success mt-2 success-btn-confirm" 
            @click="handleSuccessConfirm"
          >
            確認
          </button>
        </div>
      </div>
    </div>
    <!-- 成功通知的背景遮罩，使用最高的 z-index -->
    <div class="modal-backdrop fade show success-backdrop"></div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue';
import { useDevice } from '@/composables/useDevice';
import { useDeviceCategory } from '@/composables/useDeviceCategory';
import * as deviceService from '@/services/deviceService';

// 組件 props 定義
const props = defineProps({
  show: {
    type: Boolean,
    default: false
  },
  selectedIds: {
    type: Array,
    required: true
  }
});

// 定義事件
const emit = defineEmits(['close', 'updated']);

// 使用 composables
const { showToast } = useDevice();
const { categories, loadCategories } = useDeviceCategory();

// 狀態管理
const loading = ref(false);                  // 載入狀態
const processing = ref(false);               // 處理狀態
const devicesData = ref([]);                 // 輔具資料陣列
const confirmDialogVisible = ref(false);     // 確認對話框顯示狀態
const successNotificationVisible = ref(false); // 成功通知顯示狀態

// 監聽彈窗顯示狀態
watch(() => props.show, async (newVal) => {
  if (newVal && props.selectedIds.length > 0) {
    await loadDevicesData();
    await loadCategories();
    
    // 確保對話框和通知是關閉的
    confirmDialogVisible.value = false;
    successNotificationVisible.value = false;
  }
});

// 載入所選輔具的資料
async function loadDevicesData() {
  loading.value = true;
  
  try {
    // 清空現有資料
    devicesData.value = [];
    
    // 並行載入所有選中輔具的資料
    const promises = props.selectedIds.map(id => deviceService.fetchDevice(id));
    const results = await Promise.all(promises);
    
    // 將結果填入資料陣列
    devicesData.value = results;
    
    console.log('已載入輔具資料:', devicesData.value);
  } catch (error) {
    console.error('載入輔具資料失敗:', error);
    showToast('載入輔具資料失敗，請重試', 'error');
  } finally {
    loading.value = false;
  }
}

// 顯示確認對話框
function showConfirmDialog() {
  // 如果資料為空或正在處理中則不顯示確認對話框
  if (devicesData.value.length === 0 || processing.value) return;
  
  // 顯示確認對話框
  confirmDialogVisible.value = true;
}

// 處理確認對話框的確認按鈕點擊
async function handleConfirm() {
  // 關閉確認對話框
  confirmDialogVisible.value = false;
  
  // 執行批次修改
  await submitBatchEdit();
}

// 處理成功通知的確認按鈕點擊
function handleSuccessConfirm() {
  // 關閉成功通知
  successNotificationVisible.value = false;
  
  // 關閉批次修改模態框並通知父組件更新
  emit('updated');
  emit('close');
}

// 提交批次修改 - 每個輔具個別修改
async function submitBatchEdit() {
  if (devicesData.value.length === 0) return;
  
  processing.value = true;
  
  try {
    // 準備批次更新請求
    const batchRequests = devicesData.value.map(device => {
      // 確保 ID 是數字型別
      const numericId = typeof device.id === 'string' ? parseInt(device.id, 10) : device.id;
      
      // 為每個輔具構建更新請求
      return {
        id: numericId,
        name: device.name,
        sku: device.sku,
        unitPrice: device.unitPrice,
        inventory: device.inventory,
        isOnline: device.isOnline,
        categoryId: device.categoryId,
        // 這些欄位不在表格中修改，但可能需要保留原值
        description: device.description,
        image: device.image,
        createdByEmpId: device.createdByEmpId
      };
    });
    
    console.log('送出個別批次更新請求:', JSON.stringify(batchRequests, null, 2));
    
    // 使用 service 層進行批次更新
    const result = await deviceService.batchUpdateDevices(batchRequests);
    
    console.log('批次修改成功:', result);
    
    // 顯示成功通知
    successNotificationVisible.value = true;
    
  } catch (error) {
    console.error('批次更新失敗:', error);
    
    let errorMessage = '批次更新失敗';
    
    if (error.response) {
      console.error('詳細錯誤資訊:', {
        status: error.response.status,
        data: error.response.data
      });
      
      // 如果後端有回傳明確的錯誤訊息
      if (typeof error.response.data === 'string') {
        errorMessage += `: ${error.response.data}`;
      } else if (error.response.data && error.response.data.message) {
        errorMessage += `: ${error.response.data.message}`;
      } else {
        errorMessage += ` (伺服器錯誤 ${error.response.status})`;
      }
    } else if (error.message) {
      errorMessage += `: ${error.message}`;
    }
    
    showToast(errorMessage, 'error');
  } finally {
    processing.value = false;
  }
}

// 在元件載入時預先載入分類數據
onMounted(async () => {
  if (props.show && props.selectedIds.length > 0) {
    await loadDevicesData();
    await loadCategories();
  }
});
</script>

<style scoped>
/* 狀態切換開關樣式 */
.status-toggle {
  width: 36px;
  height: 20px;
  cursor: pointer;
}

.status-toggle:checked {
  background-color: #34c38f; /* 綠色 */
  border-color: #34c38f;
}

/* 讓開關變得更加明顯 */
.form-check.form-switch .form-check-input:focus {
  box-shadow: 0 0 0 0.25rem rgba(52, 195, 143, 0.25);
}

/* 確保 modal 顯示在上層 */
.modal {
  z-index: 2050 !important;
  pointer-events: auto;
}

.modal-dialog {
  pointer-events: auto;
  position: relative;
  z-index: 2060 !important;
}

.modal-content {
  pointer-events: auto;
  position: relative;
  z-index: 2070 !important;
}

.modal-body {
  pointer-events: auto;
  max-height: 70vh; /* 較大的高度，確保表格可以顯示 */
  overflow-y: auto;
  position: relative;
  z-index: 2080 !important;
}

/* 修正底部按鈕區域樣式 */
.modal-footer.footer-buttons {
  position: relative;
  z-index: 3000 !important;
  pointer-events: auto !important;
  background-color: #fff;
  border-top: 1px solid #dee2e6;
  padding: 1rem;
  border-bottom-right-radius: 0.3rem;
  border-bottom-left-radius: 0.3rem;
}

/* 修正按鈕本身的樣式 */
.action-button {
  position: relative;
  z-index: 3100 !important;
  pointer-events: auto !important;
  cursor: pointer !important;
}

/* 確保按鈕可見 */
.btn {
  position: relative;
  z-index: 3100 !important;
  pointer-events: auto !important;
}

.modal-backdrop {
  z-index: 2040 !important;
}

/* 表格樣式優化 */
.table {
  margin-bottom: 0;
}

.table th {
  background-color: #f8f9fa;
  font-weight: 500;
}

.table td {
  vertical-align: middle;
  padding: 0.5rem;
}

/* 表格內表單元素樣式優化 */
.form-control, .form-select {
  font-size: 14px;
  padding: 0.25rem 0.5rem;
}

.form-field {
  position: relative;
  z-index: 2080;
  pointer-events: auto !important;
}

/* 微調 form-switch 在表格內的樣式 */
.form-check.form-switch {
  margin-bottom: 0;
}

/* 表格響應式設計 */
.table-responsive {
  max-height: 60vh;
  overflow-y: auto;
}

/* 確認對話框樣式 */
.confirm-dialog-wrapper {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 9999 !important; /* 使用極高的 z-index 確保顯示在最上層 */
  overflow-x: hidden;
  overflow-y: auto;
  outline: 0;
  pointer-events: auto !important;
}

.confirm-dialog-container {
  pointer-events: auto !important;
  position: relative;
  z-index: 10000 !important;
}

.confirm-dialog-content {
  pointer-events: auto !important;
  position: relative;
  z-index: 10001 !important;
  box-shadow: 0 0.5rem 1rem rgba(0, 0, 0, 0.15);
}

/* 設置確認按鈕區域樣式 */
.confirm-buttons {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
  position: relative;
  z-index: 10002 !important;
  pointer-events: auto !important;
}

/* 設置確認和取消按鈕樣式 */
.confirm-btn-cancel, .confirm-btn-confirm {
  position: relative;
  z-index: 10003 !important;
  pointer-events: auto !important;
  cursor: pointer !important;
}

/* 設置背景遮罩樣式 */
.confirm-backdrop {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 9998 !important; /* 略低於對話框但高於其他元素 */
  background-color: rgba(0, 0, 0, 0.5);
  pointer-events: auto !important;
}

/* 限制輔具列表的最大高度 */
.device-list {
  max-height: 150px;
  overflow-y: auto;
  padding-left: 1.5rem;
  margin-bottom: 1rem;
}

/* 針對按鈕點擊事件特別加強 */
.btn {
  pointer-events: auto !important;
  cursor: pointer !important;
  user-select: none !important;
  -webkit-user-select: none !important;
  -moz-user-select: none !important;
  -ms-user-select: none !important;
}

/* 確保成功通知在確認對話框之上 */
.success-notification {
  z-index: 10010 !important;
}

.success-notification .modal-content {
  z-index: 10011 !important;
}

/* 修改成功通知樣式，確保它顯示在最上層 */
.success-notification-wrapper {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 11000 !important; /* 使用極高的 z-index 確保顯示在最上層，比確認對話框更高 */
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

/* 移除可能影響點擊的樣式 */
.modal-body button::before,
.modal-body button::after {
  content: none !important;
}

/* 強制啟用所有與成功通知相關的點擊事件 */
.success-notification-wrapper *,
.success-dialog-container *,
.success-dialog-content *,
.modal-body * {
  pointer-events: auto !important;
}

/* 確保按鈕始終可點擊 */
button.success-btn-confirm {
  position: relative;
  z-index: 11005 !important; /* 更高的層級 */
  pointer-events: auto !important;
  cursor: pointer !important;
  touch-action: manipulation !important;
}

/* 修復 Safari 中的點擊問題 */
@supports (-webkit-touch-callout: none) {
  button.success-btn-confirm {
    -webkit-transform: translateZ(0);
    transform: translateZ(0);
    will-change: transform;
  }
}

/* 移除可能的點擊干擾 */
.success-notification-wrapper {
  isolation: isolate;
}
</style>