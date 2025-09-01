<template>
  <div class="card">
    <div class="card-header d-flex flex-wrap align-items-center justify-content-between gap-3">
      <div class="d-flex flex-wrap align-items-center gap-3">
        <!-- 原本功能：搜尋、分頁、匯出 -->
        <div class="d-flex align-items-center gap-2">
          <span>Show</span>
          <select class="form-select form-select-sm w-auto" v-model="selectedShow">
            <option value="5">5</option>
            <option value="10">10</option>
            <option value="15">15</option>
          </select>
        </div>
        <div class="icon-field">
          <input type="text" class="form-control form-control-sm w-auto" v-model="searchText" placeholder="搜尋特徵名稱">
          <span class="icon">
            <iconify-icon icon="ion:search-outline"></iconify-icon>
          </span>
        </div>
        <button class="btn btn-outline-secondary btn-sm ms-2" @click="exportFeatures">
          <iconify-icon icon="mdi:export" class="me-1"></iconify-icon>
          匯出資料
        </button>
      </div>
      <div class="d-flex flex-wrap align-items-center gap-3">
        <button @click="openAddModal" class="btn btn-sm btn-primary">
          <iconify-icon icon="ri-add-line"></iconify-icon> 新增標籤
        </button>
      </div>
    </div>
    <div class="card-body">
      <!-- 房型標籤管理區塊（移到最上方） -->
      <div class="mb-5 pb-4 border-bottom">
        <h5 class="mb-3">房型標籤管理</h5>
        <div class="mb-3">
          <label class="form-label">選擇房型</label>
          <select v-model="selectedRoomTypeId" class="form-select w-auto" @change="loadRoomTypeFeatures">
            <option v-for="room in roomTypeList" :key="room.id" :value="room.id">
              {{ room.name }}
            </option>
          </select>
        </div>
        <div v-if="selectedRoomTypeId">
          <label class="form-label">設定標籤</label>
          <div>
            <span v-for="feature in features" :key="feature.id" class="badge rounded-pill me-3 mb-3"
              :class="roomTypeFeatures.includes(feature.id) ? 'bg-primary text-white fs-5 py-2 px-4' : 'bg-light text-dark fs-5 py-2 px-4'"
              @click="toggleRoomTypeFeature(feature.id)" style="cursor:pointer; font-size:1.1rem;">
              {{ feature.name }}
            </span>
          </div>
          <div v-if="roomTypeFeatures.length" class="mt-2">
            <strong>已選標籤：</strong>
            <span class="text-primary fw-bold">
              {{roomTypeFeatures.map(id => features.find(f => f.id === id)?.name).filter(Boolean).join('、')}}
            </span>
          </div>
          <button class="btn btn-success mt-3" @click="saveRoomTypeFeatures">儲存房型標籤</button>
        </div>
      </div>
      <!-- 原本特徵標籤列表 -->
      <table class="table bordered-table mb-0">
        <thead>
          <tr>
            <th scope="col">編號</th>
            <th scope="col">特徵名稱</th>
            <th scope="col" style="width:240px; text-align:center;">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(f, index) in filteredFeatures" :key="f.id">
            <td>{{ f.id }}</td>
            <td>{{ f.name }}</td>
            <td class="text-center text-nowrap" style="vertical-align: middle; width:240px;">
              <button @click.prevent="openEditModal(f.id)"
                class="w-32-px h-32-px bg-primary-light text-primary-600 rounded-circle d-inline-flex align-items-center justify-content-center mx-1"
                title="編輯" type="button">
                <iconify-icon icon="lucide:edit"></iconify-icon>
              </button>
              <button @click="deleteFeature(f.id, index)"
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

      <!-- 新增標籤 Modal -->
      <div class="modal fade" id="addFeatureModal" tabindex="-1" aria-hidden="true">
        <div class="modal-dialog modal-lg">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title">新增標籤</h5>
              <button type="button" class="btn-close" @click="closeAddModal"></button>
            </div>
            <div class="modal-body">
              <div>
                <label class="form-label">特徵名稱</label>
                <input v-model="newFeatureName" class="form-control mb-2" />
                <button class="btn btn-primary" @click="addFeature">
                  <iconify-icon icon="ri-add-line"></iconify-icon> 新增
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
      <!-- 編輯浮動視窗 -->
      <div v-if="showEditModal" class="modal fade show" tabindex="-1" aria-modal="true" role="dialog"
        style="display: block; background: rgba(0,0,0,0.5);">
        <div class="modal-dialog modal-lg">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title">編輯特徵</h5>
              <button type="button" class="btn-close" @click="closeEditModal"></button>
            </div>
            <div class="modal-body">
              <div v-if="selectedFeature">
                <label class="form-label">特徵名稱</label>
                <input v-model="selectedFeature.name" class="form-control mb-2" />
                <button class="btn btn-primary" @click="saveFeature">儲存</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
// 重點註解：引入 useToast 與 useConfirmDialog，刪除特徵時改用確認對話框
import { featureService } from '@/services/featureService';
import { roomTypeService } from '@/services/roomTypeService';
import roomTypeApi from '@/api/roomTypeApi';
import { useToast } from '@/composables/useToast';
import { useConfirmDialog } from '@/composables/useConfirmDialog';

export default {
  name: 'FeatureList',
  data() {
    return {
      selectedShow: 10,
      searchText: '',
      currentPage: 1,
      features: [],
      showEditModal: false,
      selectedFeatureId: null,
      selectedFeature: null,
      showAddModal: false,
      newFeatureName: '',
      roomTypeList: [],
      selectedRoomTypeId: '',
      roomTypeFeatures: [],
      toastApi: useToast(),           // 重點註解：toast 實例
      confirmApi: useConfirmDialog()  // 重點註解：confirmDialog 實例
    };
  },
  async mounted() {
    try {
      this.features = await featureService.getAllFeatures();
      this.roomTypeList = await roomTypeService.getAllRoomTypes();
    } catch (e) {
      this.toastApi.showToast({
        title: '載入失敗',
        message: e.message || String(e),
        type: 'error'
      });
    }
  },
  computed: {
    entriesPerPage() {
      return Number(this.selectedShow);
    },
    filteredData() {
      let result = this.features;
      if (this.searchText) {
        const search = this.searchText.toLowerCase();
        result = result.filter(f =>
          f.name && f.name.toLowerCase().includes(search)
        );
      }
      return result;
    },
    filteredFeaturesSorted() {
      return this.filteredData.slice().sort((a, b) => a.id - b.id);
    },
    totalEntries() {
      return this.filteredFeaturesSorted.length;
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
    filteredFeatures() {
      return this.filteredFeaturesSorted.slice(this.startIndex, this.endIndex);
    }
  },
  methods: {
    goToPage(page) {
      if (page >= 1 && page <= this.totalPages) {
        this.currentPage = page;
      }
    },
    async openEditModal(id) {
      this.selectedFeatureId = id;
      this.selectedFeature = this.features.find(f => f.id === id);
      this.showEditModal = true;
    },
    closeEditModal() {
      this.showEditModal = false;
      this.selectedFeatureId = null;
      this.selectedFeature = null;
    },
    async saveFeature() {
      try {
        await featureService.updateFeature(this.selectedFeature.id, this.selectedFeature);
        this.features = await featureService.getAllFeatures();
        this.closeEditModal();
        this.toastApi.showToast({
          title: '儲存成功',
          message: '特徵已儲存！',
          type: 'success'
        });
      } catch (e) {
        this.toastApi.showToast({
          title: '儲存失敗',
          message: e.message || '儲存失敗！',
          type: 'error'
        });
      }
    },
    async deleteFeature(id, index) {
      // 重點註解：改用 useConfirmDialog 取代 confirm
      const confirmed = await this.confirmApi.showConfirmDialog({
        title: '刪除特徵',
        message: '確定要刪除這個特徵嗎？',
        type: 'warning',
        confirmText: '確定刪除',
        cancelText: '取消'
      }).catch(() => false);
      if (!confirmed) return;
      try {
        const success = await featureService.deleteFeature(id);
        if (success) {
          this.features = await featureService.getAllFeatures();
          this.toastApi.showToast({
            title: '刪除成功',
            message: '特徵刪除成功！',
            type: 'success'
          });
        } else {
          this.toastApi.showToast({
            title: '刪除失敗',
            message: '刪除失敗，標籤使用中！',
            type: 'error'
          });
        }
      } catch (e) {
        this.toastApi.showToast({
          title: '刪除失敗',
          message: '刪除失敗，標籤使用中！',
          type: 'error'
        });
        console.error(e);
      }
    },
    exportFeatures() {
      const dataStr = "data:text/json;charset=utf-8," + encodeURIComponent(JSON.stringify(this.features, null, 2));
      const dlAnchorElem = document.createElement('a');
      dlAnchorElem.setAttribute("href", dataStr);
      dlAnchorElem.setAttribute("download", "features.json");
      dlAnchorElem.click();
    },
    openAddModal() {
      this.showAddModal = true;
      this.$nextTick(() => {
        const modalElement = document.getElementById('addFeatureModal');
        if (modalElement) {
          const modal = new bootstrap.Modal(modalElement);
          modal.show();
        }
      });
    },
    closeAddModal() {
      this.showAddModal = false;
      this.newFeatureName = '';
      const modalElement = document.getElementById('addFeatureModal');
      if (modalElement) {
        const modal = bootstrap.Modal.getInstance(modalElement);
        if (modal) modal.hide();
      }
    },
    async addFeature() {
      if (!this.newFeatureName.trim()) {
        this.toastApi.showToast({
          title: '新增失敗',
          message: '請輸入特徵名稱',
          type: 'error'
        });
        return;
      }
      try {
        await featureService.addFeature({ name: this.newFeatureName.trim() });
        this.features = await featureService.getAllFeatures();
        this.closeAddModal();
        this.toastApi.showToast({
          title: '新增成功',
          message: '特徵已新增！',
          type: 'success'
        });
      } catch (e) {
        this.toastApi.showToast({
          title: '新增失敗',
          message: e.message || '新增失敗！',
          type: 'error'
        });
      }
    },
    // 房型標籤管理相關
    async loadRoomTypeFeatures() {
      if (!this.selectedRoomTypeId) {
        this.roomTypeFeatures = [];
        return;
      }
      try {
        const res = await roomTypeApi.getRoomTypeFeatures(this.selectedRoomTypeId);
        this.roomTypeFeatures = Array.isArray(res.data) ? res.data : [];
      } catch (e) {
        this.toastApi.showToast({
          title: '載入失敗',
          message: '載入房型標籤失敗',
          type: 'error'
        });
        this.roomTypeFeatures = [];
      }
    },
    toggleRoomTypeFeature(featureId) {
      const idx = this.roomTypeFeatures.indexOf(featureId);
      if (idx === -1) {
        this.roomTypeFeatures.push(featureId);
      } else {
        this.roomTypeFeatures.splice(idx, 1);
      }
    },
    async saveRoomTypeFeatures() {
      if (!this.selectedRoomTypeId) return;
      try {
        await roomTypeApi.replaceRoomTypeFeatures(this.selectedRoomTypeId, this.roomTypeFeatures);
        this.toastApi.showToast({
          title: '儲存成功',
          message: '房型標籤儲存成功！',
          type: 'success'
        });
      } catch (e) {
        this.toastApi.showToast({
          title: '儲存失敗',
          message: '房型標籤儲存失敗！',
          type: 'error'
        });
      }
    }
  }
};
</script>

<style scoped>
/* 你的樣式 */
</style>
