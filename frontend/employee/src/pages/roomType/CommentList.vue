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
          <input type="text" class="form-control form-control-sm w-auto" v-model="searchText" placeholder="搜尋留言內容">
          <span class="icon">
            <iconify-icon icon="ion:search-outline"></iconify-icon>
          </span>
        </div>
        <button class="btn btn-outline-secondary btn-sm ms-2" @click="exportComments">
          <iconify-icon icon="mdi:export" class="me-1"></iconify-icon>
          匯出資料
        </button>
      </div>
      <div class="d-flex flex-wrap align-items-center gap-3">
        <select class="form-select form-select-sm w-auto" v-model="selectedStatus">
          <option value="">狀態</option>
          <option value="1">已通過</option>
          <option value="0">未通過</option>
        </select>
      </div>
    </div>
    <div class="card-body">
      <table class="table bordered-table mb-0">
        <thead>
          <tr>
            <th scope="col">編號</th>
            <th scope="col">留言者</th>
            <th scope="col">留言內容</th>
            <th scope="col">房型</th>
            <th scope="col">建立時間</th>
            <th scope="col">狀態</th>
            <th scope="col" style="width:180px; text-align:center;">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(c, index) in filteredComments" :key="c.id">
            <td>{{ c.id }}</td>
            <td>{{ c.memberId }}</td>
            <td>{{ c.content }}</td>
            <td>{{ getRoomTypeName(c.roomTypeId) }}</td>
            <td>{{ formatDate(c.createdAt) }}</td>
            <td>
              
              <label class="form-switch ms-2">
                <input type="checkbox"
                  :checked="c.approved"
                  @change="setStatus(c.id, !c.approved)"
                  class="form-check-input"
                />
                <span class="form-check-label">
                  {{ c.approved ? '已通過' : '未通過' }}
                </span>
              </label>
            </td>
            <td class="text-center text-nowrap" style="vertical-align: middle; width:180px;">
              <button @click.prevent="openPreviewModal(c.id)"
                class="w-32-px h-32-px bg-primary-light text-primary-600 rounded-circle d-inline-flex align-items-center justify-content-center mx-1"
                title="預覽" type="button">
                <iconify-icon icon="iconamoon:eye-light"></iconify-icon>
              </button>
              <button @click="deleteComment(c.id, index)"
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
    <!-- 預覽浮動視窗 -->
    <div
      v-if="showPreviewModal"
      class="modal fade show"
      tabindex="-1"
      aria-modal="true"
      role="dialog"
      style="display: block; background: rgba(0,0,0,0.5);"
    >
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">留言預覽</h5>
            <button type="button" class="btn-close" @click="closePreviewModal"></button>
          </div>
          <div class="modal-body">
            <div v-if="selectedComment">
              <p><strong>留言內容：</strong> {{ selectedComment.content }}</p>
              <p><strong>留言者：</strong> {{ selectedComment.memberName }}</p>
              <p><strong>房型：</strong> {{ getRoomTypeName(selectedComment.roomTypeId) }}</p>
              <p><strong>建立時間：</strong> {{ formatDate(selectedComment.createdAt) }}</p>
              <p><strong>狀態：</strong> {{ statusText(selectedComment.approved) }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { roomCommentService } from '@/services/roomCommentService';
import { roomTypeService } from '@/services/roomTypeService';
import { useToast } from '@/composables/useToast';
import { useConfirmDialog } from '@/composables/useConfirmDialog';

export default {
  name: 'CommentList',
  data() {
    return {
      selectedShow: 10,
      searchText: '',
      selectedStatus: '',
      currentPage: 1,
      comments: [],
      roomTypeList: [],
      showPreviewModal: false,
      selectedCommentId: null,
      selectedComment: null,
      toastApi: useToast(),
      confirmApi: useConfirmDialog()
    };
  },
  async mounted() {
    try {
      this.comments = await roomCommentService.getAll();
      this.roomTypeList = await roomTypeService.getAllRoomTypes();
    } catch (e) {
      this.toastApi.showToast({
        title: '載入失敗',
        message: '載入留言或房型資料失敗: ' + (e.message || e),
        type: 'error'
      });
    }
  },
  computed: {
    entriesPerPage() {
      return Number(this.selectedShow);
    },
    filteredData() {
      let result = this.comments;
      if (this.searchText) {
        const search = this.searchText.toLowerCase();
        result = result.filter(c =>
          (c.content && c.content.toLowerCase().includes(search)) ||
          (c.memberName && c.memberName.toLowerCase().includes(search))
        );
      }
      // 重點註解：只篩選「已通過」與「未通過」
      if (this.selectedStatus !== '') {
        result = result.filter(c => String(c.approved ? 1 : 0) === this.selectedStatus);
      }
      return result;
    },
    filteredCommentsSorted() {
      return this.filteredData.slice().sort((a, b) => a.id - b.id);
    },
    totalEntries() {
      return this.filteredCommentsSorted.length;
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
    filteredComments() {
      return this.filteredCommentsSorted.slice(this.startIndex, this.endIndex);
    }
  },
  methods: {
    getRoomTypeName(roomTypeId) {
      const room = this.roomTypeList.find(r => r.id === roomTypeId);
      return room ? room.name : '-';
    },
    statusText(approved) {
      return approved ? '已通過' : '未通過';
    },
    statusClass(approved) {
      return approved ? 'text-success fw-bold' : 'text-secondary';
    },
    goToPage(page) {
      if (page >= 1 && page <= this.totalPages) {
        this.currentPage = page;
      }
    },
    formatDate(dateStr) {
      if (!dateStr) return '';
      return dateStr.replace('T', ' ').slice(0, 19);
    },
    async setStatus(id, approved) {
      const confirmText = approved ? '確定要將此留言設為「已通過」嗎？' : '確定要將此留言設為「未通過」嗎？';
      const confirmed = await this.confirmApi.showConfirmDialog({
        title: '狀態切換確認',
        message: confirmText,
        type: 'info',
        confirmText: '確定',
        cancelText: '取消'
      }).catch(() => false);
      if (!confirmed) return;

      try {
        if (approved === true || approved === 'true') {
          await roomCommentService.approve(id);
        } else {
          await roomCommentService.unapprove(id);
        }
        this.comments = await roomCommentService.getAll();
        this.toastApi.showToast({
          title: '狀態更新',
          message: approved ? '留言已設為「已通過」' : '留言已設為「未通過」',
          type: 'success'
        });
      } catch (e) {
        this.toastApi.showToast({
          title: '狀態更新失敗',
          message: e.message || e,
          type: 'error'
        });
      }
    },
    openPreviewModal(id) {
      this.selectedCommentId = id;
      this.selectedComment = this.comments.find(c => c.id === id);
      this.showPreviewModal = true;
    },
    closePreviewModal() {
      this.showPreviewModal = false;
      this.selectedCommentId = null;
      this.selectedComment = null;
    },
    async deleteComment(id, index) {
      const confirmed = await this.confirmApi.showConfirmDialog({
        title: '刪除留言',
        message: '確定要刪除這筆留言嗎？',
        type: 'warning',
        confirmText: '確定刪除',
        cancelText: '取消'
      }).catch(() => false);
      if (!confirmed) return;
      try {
        const success = await roomCommentService.delete(id);
        if (success) {
          // 重點註解：改用 getAll() 取得全部留言，避免 400 Bad Request
          this.comments = await roomCommentService.getAll();
          this.toastApi.showToast({
            title: '刪除成功',
            message: '留言刪除成功！',
            type: 'success'
          });
        } else {
          this.toastApi.showToast({
            title: '刪除失敗',
            message: '刪除失敗！',
            type: 'error'
          });
        }
      } catch (e) {
        this.toastApi.showToast({
          title: '刪除失敗',
          message: e.message || e,
          type: 'error'
        });
        console.error(e);
      }
    },
    exportComments() {
      const dataStr = "data:text/json;charset=utf-8," + encodeURIComponent(JSON.stringify(this.comments, null, 2));
      const dlAnchorElem = document.createElement('a');
      dlAnchorElem.setAttribute("href", dataStr);
      dlAnchorElem.setAttribute("download", "comments.json");
      dlAnchorElem.click();
    }
  }
};
</script>

<style scoped>
/* 你的樣式 */
</style>
