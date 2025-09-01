<template>
  <div class="card">
    <div class="card-header d-flex flex-wrap align-items-center justify-content-between gap-3">
      <!-- 搜尋和篩選區域 -->
      <div class="d-flex flex-wrap align-items-center gap-3">
        <!-- ...原有搜尋欄位... -->

        <div class="d-flex align-items-center gap-2">
          <span>Show</span>
          <select class="form-select form-select-sm w-auto" v-model="selectedShow">
            <option value="5">5</option>
            <option value="10">10</option>
            <option value="15">15</option>
          </select>
        </div>

        <div class="icon-field">
          <input type="text" class="form-control form-control-sm w-auto" v-model="searchText" placeholder="搜尋房名">
          <span class="icon">
            <iconify-icon icon="ion:search-outline"></iconify-icon>
          </span>
        </div>

        <!-- 匯入資料按鈕（放回） -->
        <button class="btn btn-outline-secondary btn-sm ms-2" @click="importRooms">
          <iconify-icon icon="mdi:import" class="me-1"></iconify-icon>
          匯入資料
        </button>

        <!-- 匯出資料按鈕 -->
        <button class="btn btn-outline-secondary btn-sm ms-2" @click="exportRooms">
          <iconify-icon icon="mdi:export" class="me-1"></iconify-icon>
          匯出資料
        </button>
      </div>
      <!-- ...右側篩選與新增按鈕... -->
      <div class="d-flex flex-wrap align-items-center gap-3">
        <select class="form-select form-select-sm w-auto" v-model="selectedStatus">
          <option value="">全部</option>
          <option value="1">以上架</option>
          <option value="0">未上架</option>
        </select>
        <button @click="openAddModal" class="btn btn-sm btn-primary-600">
          <i class="ri-add-line"></i> 新增房型
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
            <th scope="col" style="width:150px;">房名</th>
            <th scope="col" @click="sortBy('price')" style="cursor:pointer; user-select:none;">
              價格
              <span
                :style="{ color: sortKey === 'price' && sortOrder === 1 ? '#2563eb' : '#bbb', fontSize: '12px' }">▲</span>
              <span
                :style="{ color: sortKey === 'price' && sortOrder === -1 ? '#2563eb' : '#bbb', fontSize: '12px', marginLeft: '-2px' }">▼</span>
            </th>
            <th scope="col" @click="sortBy('capacity')" style="cursor:pointer; user-select:none;">
              人數
              <span
                :style="{ color: sortKey === 'capacity' && sortOrder === 1 ? '#2563eb' : '#bbb', fontSize: '12px' }">▲</span>
              <span
                :style="{ color: sortKey === 'capacity' && sortOrder === -1 ? '#2563eb' : '#bbb', fontSize: '12px', marginLeft: '-2px' }">▼</span>
            </th>
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
          <tr v-for="(room, index) in filteredInvoices" :key="room.id">
            <!-- 編號（移除勾選框，只顯示編號） -->
            <td>
              {{ room.id }}
            </td>
            <!-- 房名 -->
            <td style="width:150px;">
              <h6 class="text-md mb-0 fw-medium flex-grow-1">{{ room.name }}</h6>
            </td>
            <!-- 價格 -->
            <td>NT$ {{ room.price }}</td>
            <!-- 人數 -->
            <td>
              <span class="px-24 py-4 rounded-pill fw-medium text-sm">
                {{ room.capacity || '-' }} 人
              </span>
            </td>
            <!-- 圖片預覽 -->
            <td style="width:150px;">
              <div v-if="room.imagePath" class="image-container">
                <img :src="room.imagePath" :alt="room.name" class="flex-shrink-0 radius-8" width="120" height="90"
                  style="object-fit: cover; cursor: pointer;" @click="openImageModal(room.imagePath)"
                  @error="onImageError(room)" @load="onImageLoad(room)">
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
                <input class="form-check-input" type="checkbox" role="switch" :id="`switch-${room.id}`"
                  v-model="room.isAvailable" @change="toggleAvailability(room)" style="width: 3rem; height: 1.5rem;">
                <label :for="`switch-${room.id}`" class="form-check-label ms-2 text-sm" style="min-width: 50px;">
                  {{ room.isAvailable ? '已上架' : '未上架' }}
                </label>
              </div>
            </td>
            <!-- 操作 -->
            <td class="text-center text-nowrap" style="vertical-align: middle; width:180px;">
              <!-- 👁️ 預覽按鈕 - 確保點擊事件正確 -->
              <button @click.prevent="openPreviewModal(room.id)"
                class="w-32-px h-32-px bg-primary-light text-primary-600 rounded-circle d-inline-flex align-items-center justify-content-center mx-1"
                title="預覽" type="button">
                <iconify-icon icon="iconamoon:eye-light"></iconify-icon>
              </button>

              <button @click="openEditModal(room.id)"
                class="w-32-px h-32-px bg-success-focus text-success-main rounded-circle d-inline-flex align-items-center justify-content-center mx-1"
                title="編輯">
                <iconify-icon icon="lucide:edit"></iconify-icon>
              </button>

              <!-- 操作欄位內的刪除按鈕 -->
              <button @click="removeRoom(index)"
                class="w-32-px h-32-px bg-danger-focus text-danger-main rounded-circle d-inline-flex align-items-center justify-content-center mx-1"
                title="刪除" type="button">
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
          :class="['btn', currentPage === page ? 'btn-primary' : 'btn-light']" @click="goToPage(page)">
          {{ page }}
        </button>
        <button :disabled="currentPage === totalPages" @click="goToPage(totalPages)"
          class="btn btn-light">&raquo;</button>
      </div>
    </div>
  </div>

  <!-- Add Modal -->
  <div class="modal fade" id="addRoomModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-lg">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">新增房型</h5>
          <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
        </div>
        <div class="modal-body">
          <RoomAdd @close="closeAddModal" @success="onAddSuccess" />
        </div>
      </div>
    </div>
  </div>

  <!-- Edit Modal -->
  <div class="modal fade" id="editRoomModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-lg">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">編輯房型</h5>
          <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
        </div>
        <div class="modal-body">
          <RoomEdit v-if="selectedRoomId" :roomId="selectedRoomId" @close="closeEditModal" @success="onEditSuccess" />
        </div>
      </div>
    </div>
  </div>

  <!-- Preview Modal -->
  <div class="modal fade" id="previewRoomModal" tabindex="-1" aria-hidden="true" ref="previewModal">
    <div class="modal-dialog modal-lg">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">房型預覽</h5>
          <button type="button" class="btn-close" data-bs-dismiss="modal" @click="closePreviewModal"></button>
        </div>
        <div class="modal-body">
          <RoomPreview v-if="showPreviewModal && selectedRoomId" :roomId="selectedRoomId" @close="closePreviewModal" />
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
          <img :src="previewImageUrl" alt="房型圖片"
            style="max-width:90vw; max-height:80vh; border-radius:12px; box-shadow:0 0 24px #0002;">
        </div>
      </div>
    </div>
  </div>

  <input ref="importFileInput" type="file" accept=".csv" style="display:none" @change="handleImportFile" />
</template>

<script>
// 重點註解：統一使用 useToast 與 useConfirmDialog 作為提示框
import Pagination from '@/components/pagination/index.vue'
import { roomTypeService } from '@/services/roomTypeService';
import RoomAdd from './RoomAdd.vue';
import RoomEdit from './RoomEdit.vue';
import RoomPreview from './RoomPreview.vue';
import { useToast } from '@/composables/useToast';
import { useConfirmDialog } from '@/composables/useConfirmDialog';

export default {
  name: 'RoomList',
  components: {
    Pagination,
    RoomAdd,
    RoomEdit,
    RoomPreview
  },
  data() {
    return {
      selectedShow: 10,
      searchText: '',
      selectedStatus: '',
      selectAll: false,
      selectedIds: [],
      currentPage: 1,
      rooms: [],
      loading: false,
      error: '',
      sortKey: '',
      sortOrder: 1,
      selectedRoomId: null,
      showPreviewModal: false,
      roomTypes: [],
      searchFromDate: '',
      searchToDate: '',
      toastApi: useToast(),           // Toast 實例
      confirmApi: useConfirmDialog(),  // ConfirmDialog 實例
      previewImageUrl: '', // 房型圖片放大預覽用
    };
  },
  async mounted() {
    this.loading = true;
    try {
      this.rooms = await roomTypeService.getAllRoomTypes();
    } catch (e) {
      this.error = e.message;
      this.toastApi.showToast({
        title: '載入失敗',
        message: e.message || '載入房型資料失敗',
        type: 'error'
      });
    }
    this.loading = false;
  },
  computed: {
    entriesPerPage() {
      return Number(this.selectedShow);
    },
    filteredData() {
      let result = this.rooms;
      if (this.searchText) {
        const search = this.searchText.toLowerCase();
        result = result.filter(room => {
          const name = room.name ? room.name.toLowerCase() : '';
          const desc = room.description ? room.description.toLowerCase() : '';
          if (search === '單') {
            return (
              (name.includes('單') && !name.includes('雙')) ||
              (desc.includes('單') && !desc.includes('雙'))
            );
          }
          return name.includes(search) || desc.includes(search);
        });
      }
      if (this.selectedStatus !== '') {
        result = result.filter(room => String(room.isAvailable ? 1 : 0) === this.selectedStatus);
      }
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
    filteredInvoices() {
      return this.filteredData.slice(this.startIndex, this.endIndex);
    }
  },
  watch: {
    selectedShow() { this.currentPage = 1; },
    searchText() { this.currentPage = 1; },
    selectedStatus() { this.currentPage = 1; }
  },
  methods: {
    toggleSelectAll() {
      if (this.selectAll) {
        this.selectedIds = this.filteredInvoices.map(inv => inv.id);
      } else {
        this.selectedIds = [];
      }
    },
    goToPage(page) {
      if (page >= 1 && page <= this.totalPages) {
        this.currentPage = page;
      }
    },
    changePage(page) {
      this.goToPage(page);
    },
    sortBy(key) {
      if (this.sortKey === key) {
        this.sortOrder = -this.sortOrder;
      } else {
        this.sortKey = key;
        this.sortOrder = 1;
      }
    },
    // 刪除房型：用 useConfirmDialog 二次確認，結果用 useToast 顯示
    async deleteRoom(id, index) {
      const confirmed = await this.confirmApi.showConfirmDialog({
        title: '刪除房型',
        message: '確定要刪除這筆房型嗎？',
        type: 'warning',
        confirmText: '確定刪除',
        cancelText: '取消'
      }).catch(() => false);
      if (!confirmed) return;
      try {
        const success = await roomTypeService.deleteRoomType(id);
        if (success) {
          this.rooms.splice(this.startIndex + index, 1);
          this.toastApi.showToast({
            title: '刪除成功',
            message: '房型已成功刪除！',
            type: 'success'
          });
        } else {
          this.toastApi.showToast({
            title: '刪除失敗',
            message: '房型刪除失敗！',
            type: 'error'
          });
        }
      } catch (e) {
        this.toastApi.showToast({
          title: '刪除失敗',
          message: e.message || '房型刪除失敗！',
          type: 'error'
        });
      }
    },
    // 移除房型（只移除畫面，不刪資料庫）：用 useConfirmDialog
    async removeRoom(index) {
      const confirmed = await this.confirmApi.showConfirmDialog({
        title: '移除資料',
        message: '確定要從畫面移除這筆資料嗎？（不會刪除資料庫）',
        type: 'warning',
        confirmText: '確定移除',
        cancelText: '取消'
      }).catch(() => false);
      if (!confirmed) return;
      this.rooms.splice(this.startIndex + index, 1);
      this.toastApi.showToast({
        title: '移除成功',
        message: '資料已從畫面移除',
        type: 'success'
      });
    },
    // Modal 相關方法
    openAddModal() {
      const modalElement = document.getElementById('addRoomModal');
      if (modalElement) {
        const modal = new bootstrap.Modal(modalElement);
        modal.show();
      }
    },
    closeAddModal() {
      const modalElement = document.getElementById('addRoomModal');
      if (modalElement) {
        const modal = bootstrap.Modal.getInstance(modalElement);
        if (modal) modal.hide();
      }
    },
    openEditModal(roomId) {
      this.selectedRoomId = roomId;
      this.$nextTick(() => {
        const modalElement = document.getElementById('editRoomModal');
        if (modalElement) {
          const modal = new bootstrap.Modal(modalElement);
          modal.show();
        }
      });
    },
    closeEditModal() {
      const modalElement = document.getElementById('editRoomModal');
      if (modalElement) {
        const modal = bootstrap.Modal.getInstance(modalElement);
        if (modal) modal.hide();
      }
      this.selectedRoomId = null;
    },
    openPreviewModal(roomId) {
      this.selectedRoomId = roomId;
      this.$nextTick(() => {
        const modalElement = document.getElementById('previewRoomModal');
        if (modalElement) {
          const modal = new bootstrap.Modal(modalElement);
          modal.show();
        }
        this.showPreviewModal = true;
      });
    },
    closePreviewModal() {
      const modalElement = document.getElementById('previewRoomModal');
      if (modalElement) {
        const modal = bootstrap.Modal.getInstance(modalElement);
        if (modal) modal.hide();
      }
      this.showPreviewModal = false;
      this.selectedRoomId = null;
    },
    handleEditFromPreview(roomId) {
      this.closePreviewModal();
      this.openEditModal(roomId);
    },
    // 新增成功回調：用 useToast
    async onAddSuccess() {
      this.closeAddModal();
      await this.refreshData();
      this.toastApi.showToast({
        title: '新增成功',
        message: '房型已成功新增！',
        type: 'success'
      });
    },
    // 編輯成功回調：用 useToast
    async onEditSuccess() {
      this.closeEditModal();
      await this.refreshData();
      this.toastApi.showToast({
        title: '修改成功',
        message: '房型已成功修改！',
        type: 'success'
      });
    },
    async refreshData() {
      this.loading = true;
      try {
        this.rooms = await roomTypeService.getAllRoomTypes();
      } catch (e) {
        this.error = e.message;
        this.toastApi.showToast({
          title: '載入失敗',
          message: e.message || '載入房型資料失敗',
          type: 'error'
        });
      }
      this.loading = false;
    },
    // 圖片載入成功/失敗處理
    onImageLoad(room) {
      // 可加除錯訊息
    },
    onImageError(room) {
      // 可加除錯訊息
    },
    // 切換上架狀態：用 useToast 顯示結果
    async toggleAvailability(room) {
      const newStatus = room.isAvailable;
      const action = newStatus ? '上架到前台' : '從前台下架';
      try {
        const success = await roomTypeService.updateAvailabilityStatus(room.id, newStatus);
        if (success) {
          room.isAvailable = newStatus;

        } else {
          throw new Error('更新失敗');
        }
      } catch (e) {
        this.toastApi.showToast({
          title: '狀態更新失敗',
          message: `${action}失敗：${e.message || e}`,
          type: 'error'
        });
      }
    },
    // 匯入資料：用 useToast 顯示結果
    importRooms() {
      this.$refs.importFileInput.click();
    },
    async handleImportFile(event) {
      const file = event.target.files[0];
      if (!file) return;
      try {
        const res = await roomTypeService.importCSV(file);
        this.toastApi.showToast({
          title: '匯入成功',
          message: res || '匯入成功！',
          type: 'success'
        });
        await this.refreshData();
      } catch (e) {
        this.toastApi.showToast({
          title: '匯入失敗',
          message: e.message || '匯入失敗！',
          type: 'error'
        });
      }
      event.target.value = '';
    },
    exportRooms() {
      const dataStr = "data:text/json;charset=utf-8," + encodeURIComponent(JSON.stringify(this.rooms, null, 2));
      const dlAnchorElem = document.createElement('a');
      dlAnchorElem.setAttribute("href", dataStr);
      dlAnchorElem.setAttribute("download", "rooms.json");
      dlAnchorElem.click();
    },
    // 依日期範圍搜尋：用 useToast 顯示錯誤
    async searchByDateRange() {
      if (!this.searchFromDate || !this.searchToDate) {
        this.toastApi.showToast({
          title: '搜尋失敗',
          message: '請選擇起始與結束日期',
          type: 'error'
        });
        return;
      }
      try {
        this.rooms = await roomTypeService.getRoomsByDateRange(this.searchFromDate, this.searchToDate);
        this.currentPage = 1;
      } catch (e) {
        this.toastApi.showToast({
          title: '搜尋失敗',
          message: e.message || '搜尋失敗',
          type: 'error'
        });
      }
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
</style>
