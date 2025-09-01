<template>
  <div class="card">
    <!-- 表格標頭：包含顯示筆數、搜尋欄位和新增按鈕 -->
    <div class="card-header d-flex flex-wrap align-items-center justify-content-between gap-3">
      <div class="d-flex flex-wrap align-items-center gap-3">
        <div class="d-flex align-items-center gap-2">
          <span>顯示筆數</span>
          <select class="form-select form-select-sm w-auto" v-model="selectedShow">
            <option value="10">10</option>
            <option value="15">15</option>
            <option value="20">20</option>
          </select>
        </div>

        <div class="icon-field">
          <input type="text" class="form-control form-control-sm w-auto" v-model="searchText" placeholder="搜尋分類名稱">
          <span class="icon">
            <iconify-icon icon="ion:search-outline"></iconify-icon>
          </span>
        </div>
      </div>

      <div class="d-flex flex-wrap align-items-center gap-3">
        <button class="btn btn-sm btn-primary-600" @click="openAddModal">
          <i class="ri-add-line"></i> 新增輔具分類
        </button>
      </div>
    </div>

    <!-- 表格主體部分 -->
    <div class="card-body">
      <!-- 載入中提示 -->
      <div v-if="loading" class="text-center py-3">
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">載入中...</span>
        </div>
      </div>

      <!-- 資料為空提示 -->
      <div v-else-if="categories.length === 0" class="text-center py-4">
        <p class="text-muted">尚無輔具分類資料</p>
      </div>

      <!-- 資料表格 -->
      <table v-else class="table bordered-table mb-0">
        <thead>
          <tr>
            <th scope="col">
              <div class="form-check style-check d-flex align-items-center">
                <input class="form-check-input" type="checkbox" v-model="selectAll" @change="toggleSelectAll">
                <label class="form-check-label">序號</label>
              </div>
            </th>
            <th scope="col">分類ID</th>
            <th scope="col">分類名稱</th>
            <th scope="col">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="category in filteredCategories" :key="category.id">
            <td>
              <div class="form-check style-check d-flex align-items-center">
                <input class="form-check-input" type="checkbox" v-model="selectedIds" :value="category.id">
                <label class="form-check-label">{{ category.id }}</label>
              </div>
            </td>
            <td>{{ category.categoryId }}</td>
            <td>{{ category.name }}</td>
            <td class="d-flex gap-2">
              <!-- 檢視按鈕 -->
              <a href="javascript:void(0)" @click="viewCategory(category)" 
                class="w-32-px h-32-px bg-primary-light text-primary-600 rounded-circle d-inline-flex align-items-center justify-content-center">
                <iconify-icon icon="iconamoon:eye-light"></iconify-icon>
              </a>
              <!-- 編輯按鈕 -->
              <a href="javascript:void(0)" @click="editCategory(category)"
                class="w-32-px h-32-px bg-success-focus text-success-main rounded-circle d-inline-flex align-items-center justify-content-center">
                <iconify-icon icon="lucide:edit"></iconify-icon>
              </a>
              <!-- 刪除按鈕 -->
              <a href="javascript:void(0)" @click="confirmDelete(category)"
                class="w-32-px h-32-px bg-danger-focus text-danger-main rounded-circle d-inline-flex align-items-center justify-content-center">
                <iconify-icon icon="mingcute:delete-2-line"></iconify-icon>
              </a>
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
        @page-changed="changePage" />
    </div>

    <!-- 新增/編輯輔具分類的模態框 -->
    <div class="modal fade" id="categoryModal" tabindex="-1" aria-labelledby="categoryModalLabel" aria-hidden="true" ref="modalEl">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title" id="categoryModalLabel">{{ isEditing ? '編輯輔具分類' : '新增輔具分類' }}</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <form @submit.prevent="saveCategory">
              <div class="mb-3">
                <label for="categoryName" class="form-label">分類名稱</label>
                <input type="text" class="form-control" id="categoryName" v-model="formData.name" required>
              </div>
              <div class="mb-3">
                <label for="categoryId" class="form-label">分類ID</label>
                <input type="number" class="form-control" id="categoryId" v-model="formData.categoryId" required>
                <small class="form-text text-muted">用於分類的排序與識別</small>
              </div>
              <div class="d-flex justify-content-end gap-2">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">取消</button>
                <button type="submit" class="btn btn-primary" :disabled="loading">
                  <span v-if="loading" class="spinner-border spinner-border-sm me-1" role="status" aria-hidden="true"></span>
                  儲存
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>

    <!-- 檢視分類詳情及對應輔具的模態框 -->
    <div class="modal fade" id="viewModal" tabindex="-1" aria-labelledby="viewModalLabel" aria-hidden="true" ref="viewModalEl">
      <div class="modal-dialog modal-lg modal-dialog-centered">
        <div class="modal-content">
          <div class="modal-header bg-light">
            <h5 class="modal-title" id="viewModalLabel">分類詳情</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <!-- 分類詳情 -->
            <div class="card mb-4">
              <div class="card-header bg-primary bg-opacity-10">
                <h6 class="mb-0">{{ currentCategory?.name || '未知分類' }} (ID: {{ currentCategory?.categoryId }})</h6>
              </div>
              <div class="card-body">
                <div class="row">
                  <div class="col-sm-4 text-muted">系統 ID：</div>
                  <div class="col-sm-8">{{ currentCategory?.id }}</div>
                </div>
                <div class="row mt-2">
                  <div class="col-sm-4 text-muted">分類 ID：</div>
                  <div class="col-sm-8">{{ currentCategory?.categoryId }}</div>
                </div>
                <div class="row mt-2">
                  <div class="col-sm-4 text-muted">分類名稱：</div>
                  <div class="col-sm-8">{{ currentCategory?.name }}</div>
                </div>
              </div>
            </div>
            
            <!-- 該分類下的輔具列表 -->
            <h6 class="mb-3 border-bottom pb-2">此分類下的輔具列表</h6>
            
            <!-- 輔具載入中 -->
            <div v-if="deviceLoading" class="text-center py-3">
              <div class="spinner-border spinner-border-sm text-primary" role="status">
                <span class="visually-hidden">載入中...</span>
              </div>
              <span class="ms-2">正在載入輔具資料...</span>
            </div>
            
            <!-- 沒有輔具資料 -->
            <div v-else-if="categoryDevices.length === 0" class="text-center py-3">
              <p class="text-muted mb-0">此分類下尚無輔具資料</p>
            </div>
            
            <!-- 輔具列表 -->
            <div v-else class="table-responsive">
              <table class="table table-hover table-sm">
                <thead class="table-light">
                  <tr>
                    <th scope="col">ID</th>
                    <th scope="col">輔具名稱</th>
                    <th scope="col">庫存</th>
                    <th scope="col">狀態</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="device in categoryDevices" :key="device.id">
                    <td>{{ device.id }}</td>
                    <td>{{ device.name }}</td>
                    <!-- 修改庫存顯示邏輯 -->
                    <td>{{ device.inventory || device.stock || '無庫存資料' }}</td>
                    <td>
                      <span 
                        class="badge"
                        :class="{
                          'bg-success': device.isOnline || device.status === 'AVAILABLE',
                          'bg-warning': device.inventory <= 5 && device.inventory > 0,
                          'bg-danger': device.inventory === 0 || device.status === 'OUT_OF_STOCK',
                          'bg-secondary': !device.isOnline && device.status !== 'AVAILABLE'
                        }"
                      >
                        {{ getDeviceStatus(device) }}
                      </span>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">關閉</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 刪除確認模態框 -->
    <div class="modal fade" id="deleteModal" tabindex="-1" aria-hidden="true" ref="deleteModalEl">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">確認刪除</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            確定要刪除「{{ deleteTarget?.name || '' }}」這個分類嗎？此操作無法復原。
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">取消</button>
            <button type="button" class="btn btn-danger" @click="deleteCategory" :disabled="loading">
              <span v-if="loading" class="spinner-border spinner-border-sm me-1" role="status" aria-hidden="true"></span>
              確認刪除
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Toast 通知 - 修改位置為右上方 -->
    <div class="position-fixed top-0 end-0 p-3" style="z-index: 1050">
      <div class="toast" :class="[toast.type, { show: toast.show }]" ref="toastEl">
        <div class="toast-header" :class="`bg-${toast.type}`">
          <strong class="me-auto text-white">系統通知</strong>
          <button type="button" class="btn-close" @click="toast.show = false" aria-label="Close"></button>
        </div>
        <div class="toast-body">
          {{ toast.message }}
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, watch } from 'vue';
import Pagination from '@/components/pagination/index.vue';
import { useDeviceCategory } from '@/composables/useDeviceCategory';
import { useDevice } from '@/composables/useDevice'; // 引入輔具 composable
import { Modal } from 'bootstrap';
// 引入設備服務
import { fetchDevices } from '@/services/deviceService';

export default {
  components: { Pagination },
  setup() {
    // 使用 composable 管理分類資料
    const {
      categories,
      loading,
      error,
      toast,
      loadCategories,
      addCategory,
      modifyCategory,
      removeCategory,
      showToast
    } = useDeviceCategory();

    // 使用輔具 composable 獲取更多功能
    const { devices } = useDevice();

    // 分頁和篩選相關資料
    const selectedShow = ref(10);
    const searchText = ref('');
    const currentPage = ref(1);
    const selectAll = ref(false);
    const selectedIds = ref([]);
    
    // 模態框相關
    const modalEl = ref(null);
    const deleteModalEl = ref(null);
    const viewModalEl = ref(null); // 新增查看詳情的模態框引用
    const modal = ref(null);
    const deleteModal = ref(null);
    const viewModal = ref(null); // 新增查看詳情的模態框實例
    
    // 表單資料
    const formData = ref({
      id: null,
      name: '',
      categoryId: ''
    });
    
    const isEditing = ref(false);
    const deleteTarget = ref(null);

    // 當前查看的分類
    const currentCategory = ref(null);
    // 當前分類下的輔具列表
    const categoryDevices = ref([]);
    // 輔具載入狀態
    const deviceLoading = ref(false);

    // 載入資料
    onMounted(async () => {
      await loadCategories();
      
      // 初始化模態框
      if (modalEl.value) {
        modal.value = new Modal(modalEl.value);
      }
      if (deleteModalEl.value) {
        deleteModal.value = new Modal(deleteModalEl.value);
      }
      if (viewModalEl.value) {
        viewModal.value = new Modal(viewModalEl.value);
      }
    });

    // 新增分類模態框
    const openAddModal = () => {
      isEditing.value = false;
      formData.value = {
        id: null,
        name: '',
        categoryId: ''
      };
      modal.value?.show();
    };

    // 編輯分類模態框
    const editCategory = (category) => {
      isEditing.value = true;
      formData.value = {
        id: category.id,
        name: category.name,
        categoryId: category.categoryId
      };
      modal.value?.show();
    };

    // 儲存分類資料
    const saveCategory = async () => {
      try {
        if (isEditing.value) {
          await modifyCategory(formData.value.id, {
            name: formData.value.name,
            categoryId: formData.value.categoryId
          });
          // 顯示成功通知
          showToast(`分類 "${formData.value.name}" 更新成功`, 'success');
        } else {
          await addCategory({
            name: formData.value.name,
            categoryId: formData.value.categoryId
          });
          // 顯示成功通知
          showToast(`新增分類 "${formData.value.name}" 成功`, 'success');
        }
        await loadCategories();
        modal.value?.hide();
      } catch (err) {
        console.error('保存失敗:', err);
        showToast(`保存失敗: ${err.message}`, 'danger');
      }
    };

    /**
     * 查看分類詳情和對應輔具
     * 1. 設置當前分類
     * 2. 載入該分類下的所有輔具
     * 3. 顯示詳情模態框
     */
    const viewCategory = async (category) => {
      try {
        // 設置當前查看的分類
        currentCategory.value = category;
        // 打開詳情模態框
        viewModal.value?.show();
        
        // 載入該分類下的輔具
        await loadCategoryDevices(category.categoryId);
      } catch (err) {
        console.error('載入分類詳情失敗:', err);
        showToast(`載入分類詳情失敗: ${err.message}`, 'danger');
      }
    };

    /**
     * 根據分類ID載入對應的輔具
     * 使用 deviceService 中的方法載入特定分類的輔具並處理資料格式
     */
    const loadCategoryDevices = async (categoryId) => {
      deviceLoading.value = true;
      categoryDevices.value = [];
      try {
        // 使用 deviceService 中的方法載入指定分類的輔具
        const response = await fetchDevices(categoryId);
        
        // 確保返回的資料是陣列形式
        if (Array.isArray(response)) {
          categoryDevices.value = response;
        } else if (response && Array.isArray(response.content)) {
          // 如果是分頁格式的回應，取出 content 陣列
          categoryDevices.value = response.content;
        } else {
          // 如果是單一物件，轉成陣列
          categoryDevices.value = response ? [response] : [];
        }
        
        console.log('載入的輔具資料:', categoryDevices.value);
      } catch (err) {
        console.error('載入分類輔具失敗:', err);
        showToast(`載入分類輔具失敗: ${err.message}`, 'danger');
      } finally {
        deviceLoading.value = false;
      }
    };

    /**
     * 獲取輔具狀態文字
     * 根據輔具的狀態屬性返回對應的中文描述
     * @param {Object} device 輔具物件
     * @return {String} 狀態描述文字
     */
    const getDeviceStatus = (device) => {
      // 根據實際資料結構判斷狀態
      if (device.status) {
        // 如果有明確的 status 欄位
        const statusMap = {
          'AVAILABLE': '可用',
          'LOW_STOCK': '庫存不足',
          'OUT_OF_STOCK': '無庫存',
          'UNAVAILABLE': '不可用'
        };
        return statusMap[device.status] || '未知狀態';
      } else {
        // 根據 isOnline 和 inventory 判斷
        if (device.isOnline === false) return '下架中';
        if (device.isOnline === true) {
          if (device.inventory === 0) return '無庫存';
          if (device.inventory && device.inventory <= 5) return '庫存不足';
          return '可用';
        }
        
        // 嘗試從庫存數量推斷
        if (device.inventory === 0 || device.stock === 0) return '無庫存';
        if (device.inventory <= 5 || device.stock <= 5) return '庫存不足';
        if (device.inventory > 5 || device.stock > 5) return '可用';
        
        return '狀態未知';
      }
    };

    // 確認刪除
    const confirmDelete = (category) => {
      deleteTarget.value = category;
      deleteModal.value?.show();
    };

    // 執行刪除
    const deleteCategory = async () => {
      try {
        if (deleteTarget.value) {
          // 1. 刪除前先檢查該分類下是否有輔具
          const devices = await fetchDevices(deleteTarget.value.categoryId);
          // 判斷 devices 是否有資料（陣列或分頁格式都處理）
          let hasDevices = false;
          if (Array.isArray(devices)) {
            hasDevices = devices.length > 0;
          } else if (devices && Array.isArray(devices.content)) {
            hasDevices = devices.content.length > 0;
          }
          if (hasDevices) {
            // 2. 前端提示禁止刪除
            showToast(`「${deleteTarget.value.name}」分類下尚有輔具，請先移除所有輔具後再刪除分類。`, 'danger');
            deleteModal.value?.hide();
            return;
          }
          // 3. 無輔具才允許刪除
          await removeCategory(deleteTarget.value.id);
          await loadCategories();
          deleteModal.value?.hide();
        }
      } catch (err) {
        console.error('刪除失敗:', err);
        showToast(`刪除失敗: ${err.message}`, 'danger');
      }
    };

    // 計算篩選後的分類列表 - 修正為計算屬性
    const filteredCategories = computed(() => {
      let result = categories.value;

      if (searchText.value) {
        const search = searchText.value.toLowerCase();
        result = result.filter(cat => 
          cat.name.toLowerCase().includes(search) || 
          String(cat.categoryId).includes(search)
        );
      }

      // 分頁處理
      return result.slice(startIndex.value, endIndex.value);
    });

    // 計算篩選後的總數 - 修正為計算屬性
    const totalEntries = computed(() => {
      let result = categories.value;

      if (searchText.value) {
        const search = searchText.value.toLowerCase();
        result = result.filter(cat => 
          cat.name.toLowerCase().includes(search) || 
          String(cat.categoryId).includes(search)
        );
      }

      return result.length;
    });
    
    // 每頁顯示筆數 - 修正為計算屬性
    const entriesPerPage = computed(() => Number(selectedShow.value));
    
    // 總頁數 - 修正為計算屬性
    const totalPages = computed(() => Math.ceil(totalEntries.value / entriesPerPage.value));
    
    // 開始索引 - 修正為計算屬性
    const startIndex = computed(() => (currentPage.value - 1) * entriesPerPage.value);
    
    // 結束索引 - 修正為計算屬性
    const endIndex = computed(() => Math.min(startIndex.value + entriesPerPage.value, totalEntries.value));
    
    // 切換分頁
    const changePage = (page) => {
      currentPage.value = page;
    };

    // 全選/取消全選 - 修正方法
    const toggleSelectAll = () => {
      if (selectAll.value) {
        // 選取所有顯示的項目
        const visibleIds = filteredCategories.value.map(c => c.id);
        selectedIds.value = visibleIds;
      } else {
        // 清空選取
        selectedIds.value = [];
      }
    };

    // 當篩選條件或顯示筆數變更時，重置到第一頁
    watch([searchText, selectedShow], () => {
      currentPage.value = 1;
    });

    return {
      // 資料狀態
      categories,
      loading,
      error,
      toast,
      
      // 篩選與分頁
      selectedShow,
      searchText,
      currentPage,
      selectAll,
      selectedIds,
      filteredCategories,
      totalEntries,
      totalPages,
      startIndex,
      endIndex,
      changePage,
      
      // 模態框相關
      modalEl,
      deleteModalEl,
      viewModalEl, // 新增
      formData,
      isEditing,
      deleteTarget,
      
      // 操作方法
      loadCategories,
      openAddModal,
      editCategory,
      viewCategory,
      saveCategory,
      confirmDelete,
      deleteCategory,
      toggleSelectAll,
      showToast,
      
      // 分類詳情與輔具相關
      currentCategory,
      categoryDevices,
      deviceLoading,
      getDeviceStatus, // 更新為新的狀態獲取方法
    };
  }
};
</script>

<style scoped>
/* 輔具狀態標籤樣式 */
.badge {
  font-size: 0.75rem;
  padding: 0.35em 0.65em;
}

/* 通用 Toast 樣式：圓角、陰影、間距（縮小尺寸） */
.toast {
  min-width: 260px;           /* 原本 380px，縮小 */
  max-width: 340px;           /* 原本 480px，縮小 */
  font-size: 1rem;            /* 字體略小 */
  font-weight: 500;
  padding: 1rem 1.2rem 0.9rem 1.2rem; /* 內距縮小 */
  border-radius: 0.9rem;
  box-shadow: 0 2px 16px rgba(0,0,0,0.10);
  border: none;
  background: #fff;
  transition: box-shadow 0.2s;
}

/* 成功通知：柔和綠色邊框與淡綠背景 */
.toast.success {
  border: 2px solid #6fd18c;
  background: linear-gradient(135deg, #eafaf1 80%, #d1f5e2 100%);
}
.toast-header.bg-success {
  background: #6fd18c !important;
  color: #fff !important;
  border-radius: 0.9rem 0.9rem 0 0;
  font-size: 1.02rem;
  font-weight: bold;
  letter-spacing: 0.5px;
  border-bottom: none;
  box-shadow: 0 2px 8px rgba(111,209,140,0.08);
}

/* 失敗/警告通知：柔和紅色邊框與淡紅背景 */
.toast.danger {
  border: 2px solid #f08080;
  background: linear-gradient(135deg, #fff0f0 80%, #ffeaea 100%);
}
.toast-header.bg-danger {
  background: #f08080 !important;
  color: #fff !important;
  border-radius: 0.9rem 0.9rem 0 0;
  font-size: 1.02rem;
  font-weight: bold;
  letter-spacing: 0.5px;
  border-bottom: none;
  box-shadow: 0 2px 8px rgba(240,128,128,0.08);
}

.toast-body {
  padding: 0.7rem 0.2rem 0.4rem 0.2rem;
  font-size: 1.02rem;
  font-weight: 500;
  color: #222;
  word-break: break-all;
  line-height: 1.6;
}

/* 讓通知浮層更明顯 */
.position-fixed.top-0.end-0.p-3 {
  z-index: 3000 !important;
}
</style>
