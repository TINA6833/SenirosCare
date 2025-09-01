<template>
  <div class="card">
    <div class="card-header d-flex flex-wrap align-items-center justify-content-between gap-3">
      <!-- 搜尋和篩選區域 -->
      <div class="d-flex flex-wrap align-items-center gap-3">
        <div class="d-flex align-items-center gap-2">
          <span>Show</span>
          <select class="form-select form-select-sm w-auto" v-model="selectedShow">
            <option value="5">5</option>
            <option value="10">10</option>
            <option value="15">15</option>
          </select>
        </div>
        <div class="icon-field">
          <input type="text" class="form-control form-control-sm w-auto" v-model="searchText" placeholder="搜尋設施">
          <span class="icon">
            <iconify-icon icon="ion:search-outline"></iconify-icon>
          </span>
        </div>
        <!-- 匯入與匯出按鈕 -->
        <button class="btn btn-outline-secondary btn-sm ms-2" @click="importFacilities">
          <iconify-icon icon="mdi:import" class="me-1"></iconify-icon>
          匯入資料
        </button>
        <button class="btn btn-outline-secondary btn-sm ms-2" @click="exportFacilities">
          <iconify-icon icon="mdi:export" class="me-1"></iconify-icon>
          匯出資料
        </button>
      </div>
      <div class="d-flex flex-wrap align-items-center gap-3">
        <!-- 上架狀態下拉 -->
        <select class="form-select form-select-sm w-auto" v-model="selectedStatus">
          <option value="">全部</option>
          <option value="1">以上架</option>
          <option value="0">未上架</option>
        </select>
        <button @click="openAddModal" class="btn btn-sm btn-primary">
          <i class="ri-add-line"></i> 新增設施
        </button>
      </div>
    </div>
    <!-- 表格內容 -->
    <div class="card-body">
      <table class="table bordered-table mb-0">
        <thead>
          <tr>
            <th scope="col" @click="sortBy('id')" style="cursor:pointer; user-select:none;">
              編號
              <span
                :style="{ color: sortKey === 'id' && sortOrder === 1 ? '#2563eb' : '#bbb', fontSize: '12px' }">▲</span>
              <span
                :style="{ color: sortKey === 'id' && sortOrder === -1 ? '#2563eb' : '#bbb', fontSize: '12px', marginLeft: '-2px' }">▼</span>
            </th>
            <th scope="col" style="width:150px;">設施名稱</th>
            <th scope="col">描述</th>
            <th scope="col" style="width:150px;">圖片預覽</th>
            <th scope="col" @click="sortBy('isAvailable')" style="cursor:pointer; user-select:none; width:120px;">
              已上架
              <span
                :style="{ color: sortKey === 'isAvailable' && sortOrder === 1 ? '#2563eb' : '#bbb', fontSize: '12px' }">▲</span>
              <span
                :style="{ color: sortKey === 'isAvailable' && sortOrder === -1 ? '#2563eb' : '#bbb', fontSize: '12px', marginLeft: '-2px' }">▼</span>
            </th>
            <th scope="col" style="width:180px; text-align:center;">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(facility, index) in filteredFacilities" :key="facility.id">
            <!-- 編號（移除勾選框，只顯示編號） -->
            <td>
              {{ facility.id }}
            </td>
            <!-- 設施名稱 -->
            <td style="width:150px;">
              <h6 class="text-md mb-0 fw-medium flex-grow-1">{{ facility.name }}</h6>
            </td>
            <!-- 描述 -->
            <td>
              <div class="facility-desc">
                {{ facility.description }}
              </div>
            </td>
            <!-- 圖片預覽 -->
            <td style="width:150px;">
              <div v-if="facility.imagePath" class="image-container">
                <img :src="facility.imagePath" :alt="facility.name" class="flex-shrink-0 radius-8 facility-img"
                  width="120" height="90" style="object-fit: cover; cursor: pointer;"
                  @click="openImageModal(facility.imagePath)" @error="onImageError(facility)"
                  @load="onImageLoad(facility)">
              </div>
              <div v-else class="no-image text-center bg-light d-flex align-items-center justify-content-center"
                style="width: 120px; height: 90px; border-radius: 8px;">
                <small class="text-muted">
                  <iconify-icon icon="material-symbols:image" class="me-1"></iconify-icon>
                  無圖片
                </small>
              </div>
            </td>
            <!-- 已上架滑動開關 -->
            <td class="text-center" style="width:120px;">
              <div class="form-switch d-flex align-items-center justify-content-center">
                <input class="form-check-input" type="checkbox" role="switch" :id="`switch-${facility.id}`"
                  v-model="facility.isAvailable" @change="toggleAvailability(facility)"
                  style="width: 3rem; height: 1.5rem;">
                <label :for="`switch-${facility.id}`" class="form-check-label ms-2 text-sm" style="min-width: 50px;">
                  {{ facility.isAvailable ? '已上架' : '未上架' }}
                </label>
              </div>
            </td>
            <!-- 操作 -->
            <td class="text-center text-nowrap" style="vertical-align: middle; width:180px;">
              <button @click.prevent="openPreviewModal(facility.id)"
                class="w-32-px h-32-px bg-primary-light text-primary-600 rounded-circle d-inline-flex align-items-center justify-content-center mx-1"
                title="預覽" type="button">
                <iconify-icon icon="iconamoon:eye-light"></iconify-icon>
              </button>
              <button @click="openEditModal(facility.id)"
                class="w-32-px h-32-px bg-success-focus text-success-main rounded-circle d-inline-flex align-items-center justify-content-center mx-1"
                title="編輯">
                <iconify-icon icon="lucide:edit"></iconify-icon>
              </button>
              <!-- 刪除鍵：直接刪除資料庫資料 -->
              <button @click="deleteFacility(facility.id, index)"
                class="w-32-px h-32-px bg-danger-focus text-danger-main rounded-circle d-inline-flex align-items-center justify-content-center mx-1"
                title="刪除">
                <iconify-icon icon="mingcute:delete-2-line"></iconify-icon>
              </button>
            </td>
          </tr>
        </tbody>
      </table>
      <!-- 分頁 -->
      <div class="pagination d-flex gap-2 justify-content-center mt-4">
        <button :disabled="currentPage === 1" @click="goToPage(1)" class="btn btn-light">&laquo;</button>
        <button v-for="page in totalPages" :key="page"
          :class="['btn', currentPage === page ? 'btn-success-main' : 'btn-light']" @click="goToPage(page)">
          {{ page }}
        </button>
        <button :disabled="currentPage === totalPages" @click="goToPage(totalPages)"
          class="btn btn-light">&raquo;</button>
      </div>
    </div>
  </div>
  <!-- Add Modal -->
  <div class="modal fade" id="addFacilityModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-lg">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">新增設施</h5>
          <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
        </div>
        <div class="modal-body">
          <FacilityAdd @close="closeAddModal" @success="onAddSuccess" />
        </div>
      </div>
    </div>
  </div>
  <!-- Edit Modal -->
  <div class="modal fade" id="editFacilityModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-lg">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">編輯設施</h5>
          <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
        </div>
        <div class="modal-body">
          <FacilityEdit v-if="selectedFacilityId" :facilityId="selectedFacilityId" @close="closeEditModal"
            @success="onEditSuccess" />
        </div>
      </div>
    </div>
  </div>
  <!-- Preview Modal -->
  <div class="modal fade" id="previewFacilityModal" tabindex="-1" aria-hidden="true" ref="previewModal">
    <div class="modal-dialog modal-lg">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">設施預覽</h5>
          <button type="button" class="btn-close" data-bs-dismiss="modal" @click="closePreviewModal"></button>
        </div>
        <div class="modal-body">
          <FacilityPreview v-if="showPreviewModal && selectedFacilityId" :facilityId="selectedFacilityId"
            @close="closePreviewModal" />
        </div>
      </div>
    </div>
  </div>
  <!-- 刪除確認 Modal -->
  <div class="modal fade" id="deleteConfirmModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content">
        <div class="modal-header border-0 pb-0">
          <h5 class="modal-title text-danger">
            <iconify-icon icon="material-symbols:warning" class="me-2"></iconify-icon>
            確認刪除
          </h5>
          <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
        </div>
        <div class="modal-body">
          <div class="text-center py-3">
            <div class="mb-3">
              <iconify-icon icon="material-symbols:delete-forever" class="text-danger"
                style="font-size: 48px;"></iconify-icon>
            </div>
            <h6 class="mb-2">您即將刪除以下設施：</h6>
            <div class="bg-light p-3 rounded mb-3">
              <div><strong>設施名稱：</strong>{{ facilityToDelete?.name }}</div>
              <div><strong>描述：</strong>{{ facilityToDelete?.description }}</div>
            </div>
            <p class="text-muted small mb-0">
              <iconify-icon icon="material-symbols:info" class="me-1"></iconify-icon>
              此操作無法復原，請確認後再執行
            </p>
          </div>
        </div>
        <div class="modal-footer border-0 pt-0">
          <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">
            <iconify-icon icon="material-symbols:cancel" class="me-1"></iconify-icon>
            取消
          </button>
          <button type="button" class="btn btn-danger" @click="confirmDeleteFacility">
            <iconify-icon icon="material-symbols:delete" class="me-1"></iconify-icon>
            確定刪除
          </button>
        </div>
      </div>
    </div>
  </div>
  <!-- 圖片放大 Modal -->
  <div class="modal fade" id="imagePreviewModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered" style="max-width:90vw;">
      <div class="modal-content">
        <div class="modal-header border-0 pb-0">
          <h5 class="modal-title">圖片預覽</h5>
          <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
        </div>
        <div class="modal-body text-center">
          <img :src="previewImageUrl" alt="設施圖片"
            style="max-width:90vw; max-height:80vh; border-radius:12px; box-shadow:0 0 24px #0002;">
        </div>
      </div>
    </div>
  </div>
  <input ref="importFileInput" type="file" accept=".csv" style="display:none" @change="handleImportFile" />
</template>

<script>
import Pagination from '@/components/pagination/index.vue'
import { facilityService } from '@/services/facilityService';
import FacilityAdd from './FacilityAdd.vue';
import FacilityEdit from './FacilityEdit.vue';
import FacilityPreview from './FacilityPreview.vue';
// 重點註解：引入 useToast 與 useConfirmDialog
import { useToast } from '@/composables/useToast';
import { useConfirmDialog } from '@/composables/useConfirmDialog';

export default {
  name: 'FacilityList',
  components: {
    Pagination,
    FacilityAdd,
    FacilityEdit,
    FacilityPreview
  },
  data() {
    return {
      selectedShow: 10,
      searchText: '',
      selectedStatus: '',
      selectAll: false,
      selectedIds: [],
      currentPage: 1,
      facilities: [],
      loading: false,
      error: '',
      sortKey: '',
      sortOrder: 1,
      selectedFacilityId: null,
      facilityToDelete: null,
      deleteIndex: null,
      showPreviewModal: false,
      toastApi: useToast(),           // 重點註解：toast 實例
      confirmApi: useConfirmDialog(),  // 重點註解：confirmDialog 實例
      previewImageUrl: '', // 圖片放大預覽用
    };
  },
  async mounted() {
    this.loading = true;
    try {
      let facilities = await facilityService.getAllFacilities();
      // 編號由小到大排序
      facilities = facilities.slice().sort((a, b) => Number(a.id) - Number(b.id));
      this.facilities = facilities;
      console.log('=== 🏢 FacilityList Debug Info ===');
      console.log('總共設施數量:', this.facilities.length);
      this.facilities.forEach((facility, index) => {
        console.log(`設施 ${index + 1} (ID: ${facility.id}):`, {
          name: facility.name,
          imagePath: facility.imagePath,
          isAvailable: facility.isAvailable,
          isAvailableType: typeof facility.isAvailable,
          hasImage: !!facility.imagePath,
        });
      });
    } catch (e) {
      this.error = e.message;
      console.error('載入設施資料失敗:', e);
    }
    this.loading = false;
  },
  computed: {
    entriesPerPage() {
      return Number(this.selectedShow);
    },
    filteredData() {
      let result = this.facilities;
      // 搜尋篩選
      if (this.searchText) {
        const search = this.searchText.toLowerCase();
        result = result.filter(facility => {
          const name = facility.name ? facility.name.toLowerCase() : '';
          const desc = facility.description ? facility.description.toLowerCase() : '';
          return name.includes(search) || desc.includes(search);
        });
      }
      // 上架狀態篩選
      if (this.selectedStatus !== '') {
        result = result.filter(facility => {
          return String(facility.isAvailable ? 1 : 0) === this.selectedStatus;
        });
      }
      // 排序
      if (this.sortKey) {
        result = result.slice().sort((a, b) => {
          let valA, valB;
          if (this.sortKey === 'isAvailable') {
            valA = a.isAvailable ? 1 : 0;
            valB = b.isAvailable ? 1 : 0;
          } else {
            valA = Number(a[this.sortKey]);
            valB = Number(b[this.sortKey]);
            if (isNaN(valA) || isNaN(valB)) return 0;
          }
          return (valA - valB) * this.sortOrder;
        });
      }
      return result;
    },
    totalEntries() {
      return this.filteredData.length;
    },
    totalPages() {
      return Math.ceil(this.totalEntries / this.entriesPerPage);
    },
    startIndex() {
      return (this.currentPage - 1) * this.entriesPerPage;
    },
    endIndex() {
      return Math.min(this.startIndex + this.entriesPerPage, this.totalEntries);
    },
    filteredFacilities() {
      return this.filteredData.slice(this.startIndex, this.endIndex);
    }
  },
  watch: {
    selectedShow() {
      this.currentPage = 1;
    },
    searchText() {
      this.currentPage = 1;
    },
    selectedStatus() {
      this.currentPage = 1;
    }
  },
  methods: {
    toggleSelectAll() {
      if (this.selectAll) {
        this.selectedIds = this.filteredFacilities.map(facility => facility.id);
      } else {
        this.selectedIds = [];
      }
    },
    goToPage(page) {
      if (page >= 1 && page <= this.totalPages) {
        this.currentPage = page;
      }
    },
    sortBy(key) {
      if (this.sortKey === key) {
        this.sortOrder = -this.sortOrder;
      } else {
        this.sortKey = key;
        this.sortOrder = 1;
      }
    },
    async deleteFacility(id, index) {
      const confirmed = await this.confirmApi.showConfirmDialog({
        title: '刪除設施',
        message: '確定要刪除這筆設施嗎？此操作會刪除資料庫資料，無法復原。',
        type: 'warning',
        confirmText: '確定刪除',
        cancelText: '取消'
      }).catch(() => false);
      if (!confirmed) return;
      try {
        const success = await facilityService.deleteFacility(id);
        if (success) {
          await this.refreshData(); // 刪除後重新排序
          this.toastApi.showToast({
            title: '刪除成功',
            message: '設施已成功刪除！',
            type: 'success'
          });
        } else {
          this.toastApi.showToast({
            title: '刪除失敗',
            message: '設施刪除失敗！',
            type: 'error'
          });
        }
      } catch (e) {
        this.toastApi.showToast({
          title: '刪除失敗',
          message: e.message || '設施刪除失敗！',
          type: 'error'
        });
      }
    },
    async removeFacility(index) {
      // 重點註解：改用 useConfirmDialog 取代 confirm
      const confirmed = await this.confirmApi.showConfirmDialog({
        title: '移除設施',
        message: '確定要從畫面移除這筆資料嗎？（不會刪除資料庫）',
        type: 'info',
        confirmText: '確定移除',
        cancelText: '取消'
      }).catch(() => false);
      if (!confirmed) return;
      this.facilities.splice(this.startIndex + index, 1);
      // 重點註解：移除成功提示
      this.toastApi.showToast({
        title: '移除成功',
        message: '已從畫面移除！',
        type: 'success'
      });
    },
    openAddModal() {
      const modalElement = document.getElementById('addFacilityModal');
      if (modalElement) {
        const modal = new bootstrap.Modal(modalElement);
        modal.show();
      }
    },
    closeAddModal() {
      const modalElement = document.getElementById('addFacilityModal');
      if (modalElement) {
        const modal = bootstrap.Modal.getInstance(modalElement);
        if (modal) modal.hide();
      }
    },
    openEditModal(facilityId) {
      this.selectedFacilityId = facilityId;
      this.$nextTick(() => {
        const modalElement = document.getElementById('editFacilityModal');
        if (modalElement) {
          const modal = new bootstrap.Modal(modalElement);
          modal.show();
        }
      });
    },
    closeEditModal() {
      const modalElement = document.getElementById('editFacilityModal');
      if (modalElement) {
        const modal = bootstrap.Modal.getInstance(modalElement);
        if (modal) modal.hide();
      }
      this.selectedFacilityId = null;
    },
    openPreviewModal(facilityId) {
      this.selectedFacilityId = facilityId;
      console.log('預覽設施ID:', facilityId); // 應有正確 id
      this.$nextTick(() => {
        const modalElement = document.getElementById('previewFacilityModal');
        if (modalElement) {
          const modal = new bootstrap.Modal(modalElement);
          modal.show();
        }
        this.showPreviewModal = true;
      });
    },
    closePreviewModal() {
      const modalElement = document.getElementById('previewFacilityModal');
      if (modalElement) {
        const modal = bootstrap.Modal.getInstance(modalElement);
        if (modal) modal.hide();
      }
      this.showPreviewModal = false;
      this.selectedFacilityId = null;
    },
    openDeleteModal(facility, index) {
      this.facilityToDelete = facility;
      this.deleteIndex = index;
      const modalElement = document.getElementById('deleteConfirmModal');
      if (modalElement) {
        const modal = new bootstrap.Modal(modalElement);
        modal.show();
      }
    },
    async confirmDeleteFacility() {
      if (!this.facilityToDelete) return;
      try {
        const deleteBtn = document.querySelector('#deleteConfirmModal .btn-danger');
        const originalText = deleteBtn.innerHTML;
        deleteBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>刪除中...';
        deleteBtn.disabled = true;
        const success = await facilityService.deleteFacility(this.facilityToDelete.id);
        if (success) {
          this.facilities.splice(this.deleteIndex, 1);
          const modalElement = document.getElementById('deleteConfirmModal');
          const modal = bootstrap.Modal.getInstance(modalElement);
          if (modal) modal.hide();
          this.showSuccessToast(`設施「${this.facilityToDelete.name}」已成功刪除`);
        } else {
          throw new Error('刪除失敗');
        }
      } catch (e) {
        this.showErrorToast('刪除失敗：' + e.message);
      } finally {
        const deleteBtn = document.querySelector('#deleteConfirmModal .btn-danger');
        if (deleteBtn) {
          deleteBtn.innerHTML = '<iconify-icon icon="material-symbols:delete" class="me-1"></iconify-icon>確定刪除';
          deleteBtn.disabled = false;
        }
        this.facilityToDelete = null;
        this.deleteIndex = null;
      }
    },
    showSuccessToast(message) {
      const toast = document.createElement('div');
      toast.className = 'alert alert-success alert-dismissible fade show position-fixed';
      toast.style.cssText = 'top: 20px; right: 20px; z-index: 9999; min-width: 300px;';
      toast.innerHTML = `
        <iconify-icon icon="material-symbols:check-circle" class="me-2"></iconify-icon>
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
      `;
      document.body.appendChild(toast);
      setTimeout(() => {
        if (toast.parentNode) {
          toast.parentNode.removeChild(toast);
        }
      }, 3000);
    },
    showErrorToast(message) {
      const toast = document.createElement('div');
      toast.className = 'alert alert-danger alert-dismissible fade show position-fixed';
      toast.style.cssText = 'top: 20px; right: 20px; z-index: 9999; min-width: 300px;';
      toast.innerHTML = `
        <iconify-icon icon="material-symbols:error" class="me-2"></iconify-icon>
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
      `;
      document.body.appendChild(toast);
      setTimeout(() => {
        if (toast.parentNode) {
          toast.parentNode.removeChild(toast);
        }
      }, 5000);
    },
    async onAddSuccess() {
      this.closeAddModal();
      await this.refreshData(); // 新增後重新排序
      this.toastApi.showToast({
        title: '新增成功',
        message: '設施新增成功！',
        type: 'success'
      });
    },
    async onEditSuccess() {
      this.closeEditModal();
      await this.refreshData(); // 編輯後重新排序
      this.toastApi.showToast({
        title: '修改成功',
        message: '設施修改成功！',
        type: 'success'
      });
    },
    async refreshData() {
      this.loading = true;
      try {
        let facilities = await facilityService.getAllFacilities();
        // 修改資料後，讓資料排序還是由小到大排序
        facilities = facilities.slice().sort((a, b) => Number(a.id) - Number(b.id));
        this.facilities = facilities;
      } catch (e) {
        this.error = e.message;
      }
      this.loading = false;
    },
    onImageLoad(facility) {
      console.log(`✅ 圖片載入成功 - 設施 ${facility.id}: ${facility.imagePath}`);
    },
    onImageError(facility) {
      console.error(`❌ 圖片載入失敗 - 設施 ${facility.id}: ${facility.imagePath}`);
    },
    async toggleAvailability(facility) {
      const newStatus = facility.isAvailable;
      const action = newStatus ? '上架到前台' : '從前台下架';
      try {
        const success = await facilityService.updateAvailabilityStatus(facility.id, newStatus);
        if (success) {
          facility.isAvailable = newStatus;
          // 使用 useToast 顯示提示框
          this.toastApi.showToast({
            title: '狀態更新成功',
            message: `設施「${facility.name}」已${action}`,
            type: 'success'
          });
        } else {
          throw new Error('更新失敗');
        }
      } catch (e) {
        // 使用 useToast 顯示錯誤提示框
        this.toastApi.showToast({
          title: '狀態更新失敗',
          message: `${action}失敗：${e.message || e}`,
          type: 'error'
        });
      }
    },
    importFacilities() {
      this.$refs.importFileInput.click();
    },
    async handleImportFile(event) {
      const file = event.target.files[0];
      if (!file) return;
      try {
        const res = await facilityService.importCSV(file);
        // 重點註解：匯入成功提示
        this.toastApi.showToast({
          title: '匯入成功',
          message: res || '匯入成功！',
          type: 'success'
        });
        await this.refreshData();
      } catch (e) {
        // 重點註解：匯入失敗提示
        this.toastApi.showToast({
          title: '匯入失敗',
          message: e.message || '匯入失敗！',
          type: 'error'
        });
      }
      event.target.value = '';
    },
    exportFacilities() {
      const dataStr = "data:text/json;charset=utf-8," + encodeURIComponent(JSON.stringify(this.facilities, null, 2));
      const dlAnchorElem = document.createElement('a');
      dlAnchorElem.setAttribute("href", dataStr);
      dlAnchorElem.setAttribute("download", "facilities.json");
      dlAnchorElem.click();
    },
    openImageModal(url) {
      this.previewImageUrl = url;
      const modalElement = document.getElementById('imagePreviewModal');
      if (modalElement) {
        const modal = new bootstrap.Modal(modalElement);
        modal.show();
      }
    },
  }
};
</script>

<style scoped>
/* 你的樣式 */
/* 重點註解：描述欄位最多顯示兩排，超出以...省略 */
.facility-desc {
  display: -webkit-box;
  /* 最多顯示2行 */
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  word-break: break-all;
  white-space: normal;
  min-height: 2.4em;
  /* 依字型大小調整 */
}
</style>
