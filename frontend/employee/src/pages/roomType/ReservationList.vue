<template>
  <div class="card">
    <div class="card-header d-flex flex-wrap align-items-center justify-content-between gap-3">
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
          <input type="text" class="form-control form-control-sm w-auto" v-model="searchText" placeholder="搜尋申請人或日期">
          <span class="icon">
            <iconify-icon icon="ion:search-outline"></iconify-icon>
          </span>
        </div>
        <!-- 匯入資料紐已移除 -->
        <button class="btn btn-outline-secondary btn-sm ms-2" @click="exportReservations">
          <iconify-icon icon="mdi:export" class="me-1"></iconify-icon>
          匯出資料
        </button>
      </div>
      <div class="d-flex flex-wrap align-items-center gap-3">
        <select class="form-select form-select-sm w-auto" v-model="selectedStatus">
          <option value="">狀態</option>
          <option value="0">待審核</option>
          <option value="1">已預約</option>
          <option value="2">已取消</option>
          <option value="3">已完成</option>
        </select>
        <button @click="openAddModal" class="btn btn-sm btn-primary-600">
          <i class="ri-add-line"></i> 新增預約
        </button>
      </div>
    </div>
    <div class="card-body">
      <table class="table bordered-table mb-0">
        <thead>
          <tr>
            <th scope="col">編號</th>
            <th scope="col">申請人</th>
            <th scope="col">房型</th>
            <th scope="col">人數</th>
            <th scope="col">日期</th>
            <th scope="col">時間</th>
            <th scope="col">狀態</th>
            <th scope="col" style="width:180px; text-align:center;">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(r, index) in filteredReservations" :key="r.reservationId">
            <td>{{ r.reservationId }}</td>
            <td>{{ r.applicantName }}</td>
            <td>{{ getRoomTypeName(r.roomTypeId) }}</td>
            <td>{{ r.partySize }}</td>
            <td>{{ r.preferredDate }}</td>
            <td>{{ r.timeFrom }} ~ {{ r.timeTo }}</td>
            <td>

              <select v-model="r.status" @change="setStatus(r.reservationId, Number(r.status))"
                class="form-select form-select-sm d-inline-block w-auto">
                <option :value="0">待審核</option>
                <option :value="1">已預約</option>
                <option :value="2">已取消</option>
                <option :value="3">已完成</option>
              </select>
            </td>
            <td class="text-center text-nowrap" style="vertical-align: middle; width:180px;">
              <!-- 預覽按鈕 -->
              <button @click.prevent="openPreviewModal(r.reservationId)"
                class="w-32-px h-32-px bg-primary-light text-primary-600 rounded-circle d-inline-flex align-items-center justify-content-center mx-1"
                title="預覽" type="button">
                <iconify-icon icon="iconamoon:eye-light"></iconify-icon>
              </button>
              <!-- 修改按鈕 -->
              <button @click="openEditModal(r.reservationId)"
                class="w-32-px h-32-px bg-success-focus text-success-main rounded-circle d-inline-flex align-items-center justify-content-center mx-1"
                title="編輯" type="button">
                <iconify-icon icon="lucide:edit"></iconify-icon>
              </button>
              <button @click="deleteReservation(r.reservationId, index)"
                class="w-32-px h-32-px bg-danger-focus text-danger-main rounded-circle d-inline-flex align-items-center justify-content-center mx-1"
                title="刪除">
                <iconify-icon icon="mingcute:delete-2-line"></iconify-icon>
              </button>
            </td>
          </tr>
        </tbody>
      </table>
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
  <!-- 預約預覽浮動視窗 -->
  <div v-if="showPreviewModal" class="modal fade show" tabindex="-1" aria-modal="true" role="dialog"
    style="display: block; background: rgba(0,0,0,0.5);">
    <div class="modal-dialog modal-lg">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">預約預覽</h5>
          <button type="button" class="btn-close" @click="closePreviewModal"></button>
        </div>
        <div class="modal-body">
          <ReservationPreview v-if="selectedReservationId" :reservationId="selectedReservationId"
            @close="closePreviewModal" />
        </div>
      </div>
    </div>
  </div>
  <!-- 修改預約浮動視窗 -->
  <div v-if="showEditModal" class="modal fade show" tabindex="-1" aria-modal="true" role="dialog"
    style="display: block; background: rgba(0,0,0,0.5);">
    <div class="modal-dialog modal-lg">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">修改預約</h5>
          <button type="button" class="btn-close" @click="closeEditModal"></button>
        </div>
        <div class="modal-body">
          <ReservationEdit v-if="selectedEditReservationId" :reservationId="selectedEditReservationId"
            @close="closeEditModal" @success="handleEditSuccess" />
        </div>
      </div>
    </div>
  </div>
  <!-- 新增預約浮動視窗 -->
  <div v-if="showAddModal" class="modal fade show" tabindex="-1" aria-modal="true" role="dialog"
    style="display: block; background: rgba(0,0,0,0.5);">
    <div class="modal-dialog modal-lg">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">新增預約</h5>
          <button type="button" class="btn-close" @click="closeAddModal"></button>
        </div>
        <div class="modal-body">
          <ReservationAdd @close="closeAddModal" @success="handleAddSuccess" />
        </div>
      </div>
    </div>
  </div>
</template>

<script>
// 重點註解：狀態欄修改成功或失敗都跳提示框
import { reservationService } from '@/services/reservationService';
import { roomTypeService } from '@/services/roomTypeService';
import ReservationPreview from './ReservationPreview.vue';
import ReservationEdit from './ReservationEdit.vue';
import ReservationAdd from './ReservationAdd.vue';
import { useToast } from '@/composables/useToast';
import { useConfirmDialog } from '@/composables/useConfirmDialog';

export default {
  name: 'ReservationList',
  components: {
    ReservationPreview,
    ReservationEdit,
    ReservationAdd
  },
  data() {
    return {
      selectedShow: 10,
      searchText: '',
      selectedStatus: '',
      currentPage: 1,
      reservations: [],
      roomTypeList: [],
      showPreviewModal: false,
      selectedReservationId: null,
      showEditModal: false,
      selectedEditReservationId: null,
      showAddModal: false,
      searchFromDate: '',
      searchToDate: '',
      toastApi: useToast(),           // 重點註解：toast 實例
      confirmApi: useConfirmDialog()  // 重點註解：confirmDialog 實例
    };
  },
  async mounted() {
    try {
      this.reservations = await reservationService.getAllReservations();
      this.roomTypeList = await roomTypeService.getAllRoomTypes();
    } catch (e) {
      this.toastApi.showToast({
        title: '載入失敗',
        message: '載入預約或房型資料失敗: ' + (e.message || e),
        type: 'error'
      });
    }
  },
  computed: {
    entriesPerPage() {
      return Number(this.selectedShow);
    },
    filteredData() {
      let result = this.reservations;
      if (this.searchText) {
        const search = this.searchText.toLowerCase();
        result = result.filter(r =>
          // 重點註解：同時搜尋申請人名稱與日期
          (r.applicantName && r.applicantName.toLowerCase().includes(search)) ||
          (r.preferredDate && r.preferredDate.toLowerCase().includes(search))
        );
      }
      if (this.selectedStatus !== '') {
        result = result.filter(r => String(r.status) === this.selectedStatus);
      }
      return result;
    },
    filteredReservationsSorted() {
      return this.filteredData.slice().sort((a, b) => a.reservationId - b.reservationId);
    },
    totalEntries() {
      return this.filteredReservationsSorted.length;
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
    filteredReservations() {
      return this.filteredReservationsSorted.slice(this.startIndex, this.endIndex);
    }
  },
  methods: {
    getRoomTypeName(roomTypeId) {
      const room = this.roomTypeList.find(r => r.id === roomTypeId);
      return room ? room.name : '-';
    },
    statusText(status) {
      switch (Number(status)) {
        case 0: return '待審核';
        case 1: return '已預約';
        case 2: return '已取消';
        case 3: return '已完成';
        default: return '未知';
      }
    },
    statusClass(status) {
      switch (Number(status)) {
        case 1: return 'text-success fw-bold';
        case 2: return 'text-danger fw-bold';
        case 3: return 'text-primary fw-bold';
        default: return 'text-secondary';
      }
    },
    goToPage(page) {
      if (page >= 1 && page <= this.totalPages) {
        this.currentPage = page;
      }
    },
    removeReservation(index) {
      this.confirmApi.showConfirmDialog({
        title: '移除資料',
        message: '確定要從畫面移除這筆資料嗎？（不會刪除資料庫）',
        type: 'warning',
        confirmText: '確定移除',
        cancelText: '取消'
      }).then(() => {
        this.reservations.splice(this.startIndex + index, 1);
        this.toastApi.showToast({
          title: '移除成功',
          message: '資料已從畫面移除',
          type: 'success'
        });
      }).catch(() => { });
    },
    importReservations() {
      this.$refs.importFileInput?.click();
    },
    handleImportFile(event) {
      event.target.value = '';
    },
    exportReservations() {
      const dataStr = "data:text/json;charset=utf-8," + encodeURIComponent(JSON.stringify(this.reservations, null, 2));
      const dlAnchorElem = document.createElement('a');
      dlAnchorElem.setAttribute("href", dataStr);
      dlAnchorElem.setAttribute("download", "reservations.json");
      dlAnchorElem.click();
    },
    async setStatus(reservationId, status) {
      // 重點註解：狀態欄修改成功或失敗都跳提示框
      const reservation = this.reservations.find(r => r.reservationId === reservationId);
      if (reservation) {
        const oldStatus = reservation.status;
        reservation.status = status;
        try {
          await reservationService.patchReservationStatus(reservationId, { status });
          this.toastApi.showToast({
            title: '狀態更新成功',
            message: `預約狀態已更新為「${this.statusText(status)}」`,
            type: 'success'
          });
        } catch (e) {
          this.toastApi.showToast({
            title: '狀態更新失敗',
            message: e.message || '狀態更新失敗',
            type: 'error'
          });
          reservation.status = oldStatus;
        }
      }
    },
    openPreviewModal(reservationId) {
      this.selectedReservationId = reservationId;
      this.showPreviewModal = true;
    },
    closePreviewModal() {
      this.showPreviewModal = false;
      this.selectedReservationId = null;
    },
    openEditModal(reservationId) {
      this.selectedEditReservationId = reservationId;
      this.showEditModal = true;
    },
    closeEditModal() {
      this.showEditModal = false;
      this.selectedEditReservationId = null;
    },
    openAddModal() {
      this.showAddModal = true;
    },
    closeAddModal() {
      this.showAddModal = false;
    },
    async handleEditSuccess() {
      this.closeEditModal();
      try {
        this.reservations = await reservationService.getAllReservations();
        this.toastApi.showToast({
          title: '修改成功',
          message: '預約資料已更新',
          type: 'success'
        });
      } catch (e) {
        this.toastApi.showToast({
          title: '載入失敗',
          message: '重新載入預約資料失敗: ' + (e.message || e),
          type: 'error'
        });
      }
    },
    async handleAddSuccess() {
      this.closeAddModal();
      try {
        this.reservations = await reservationService.getAllReservations();
        this.toastApi.showToast({
          title: '新增成功',
          message: '預約資料已新增',
          type: 'success'
        });
      } catch (e) {
        this.toastApi.showToast({
          title: '載入失敗',
          message: '重新載入預約資料失敗: ' + (e.message || e),
          type: 'error'
        });
      }
    },
    async searchByRange() {
      if (!this.searchFromDate || !this.searchToDate) {
        this.toastApi.showToast({
          title: '搜尋失敗',
          message: '請選擇起始與結束日期',
          type: 'error'
        });
        return;
      }
      try {
        const res = await reservationService.getByRange(this.searchFromDate, this.searchToDate);
        this.reservations = res;
        this.currentPage = 1;
      } catch (e) {
        this.toastApi.showToast({
          title: '搜尋失敗',
          message: e.message || '搜尋失敗',
          type: 'error'
        });
      }
    },
    async deleteReservation(id, index) {
      // 重點註解：改用 useConfirmDialog 二次確認，刪除結果用 toast 顯示，API 回傳訊息更明確
      const confirmed = await this.confirmApi.showConfirmDialog({
        title: '刪除預約',
        message: '確定要刪除這筆預約資料嗎？',
        type: 'warning',
        confirmText: '確定刪除',
        cancelText: '取消'
      }).catch(() => false);
      if (!confirmed) return;
      try {
        const response = await reservationService.deleteReservation(id);
        // 根據 API 回傳格式判斷成功與否
        // 假設 API 回傳 { success: true } 或 200
        if (response && (response.success === true || response === true)) {
          this.reservations = await reservationService.getAllReservations();
          this.toastApi.showToast({
            title: '刪除成功',
            message: '預約資料刪除成功！',
            type: 'success'
          });
        } else {
         
        }
      } catch (e) {
        
        // 額外顯示錯誤細節於 console
        console.error('刪除預約失敗:', e);
      }
    },
  }
};
</script>

<style scoped>
/* 你的樣式 */
</style>
