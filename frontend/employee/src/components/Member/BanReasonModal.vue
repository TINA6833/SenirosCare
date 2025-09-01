<template>
  <!-- **重點：停權原因輸入模態框** -->
  <div class="modal fade" id="banReasonModal" tabindex="-1" aria-labelledby="banReasonModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content">
        <!-- 模態框標題 -->
        <div class="modal-header bg-warning">
          <h1 class="modal-title fs-5 text-dark" id="banReasonModalLabel">
            停權會員
          </h1>
          <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
        </div>

        <!-- 模態框內容 -->
        <div class="modal-body">
          <div class="mb-3">
            <label class="form-label fw-semibold">停權原因 <span class="text-danger">*</span></label>
            <textarea 
              v-model="banReason" 
              class="form-control" 
              rows="4" 
              placeholder="請輸入停權原因..."
              :class="{ 'is-invalid': showError && !banReason.trim() }">
            </textarea>
            <div v-if="showError && !banReason.trim()" class="invalid-feedback">
              請輸入停權原因
            </div>
          </div>
          
          <div class="alert alert-warning">
            <strong>注意：</strong>停權後該會員將無法使用服務，直到重新啟用為止。
          </div>
        </div>

        <!-- 模態框底部 -->
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">
            取消
          </button>
          <button type="button" class="btn btn-danger" @click="confirmBan" :disabled="loading">
            <span v-if="loading" class="spinner-border spinner-border-sm me-2" role="status"></span>
            確認停權
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue';

export default {
  name: 'BanReasonModal',
  
  emits: ['confirm-ban'],
  
  setup(props, { emit }) {
    // **重點：響應式資料**
    const banReason = ref('');
    const showError = ref(false);
    const loading = ref(false);
    const currentMemberId = ref(null);

    /**
     * **重點：顯示停權原因輸入模態框**
     * @param {string} memberId - 會員ID
     */
    const showModal = (memberId) => {
      currentMemberId.value = memberId;
      banReason.value = '';
      showError.value = false;
      loading.value = false;
      
      const modal = new bootstrap.Modal(document.getElementById('banReasonModal'));
      modal.show();
    };

    /**
     * **重點：確認停權操作**
     */
    const confirmBan = () => {
      // 驗證停權原因
      if (!banReason.value.trim()) {
        showError.value = true;
        return;
      }

      showError.value = false;
      loading.value = true;

      // 觸發停權事件
      emit('confirm-ban', {
        memberId: currentMemberId.value,
        banReason: banReason.value.trim()
      });

      // 關閉模態框
      const modal = bootstrap.Modal.getInstance(document.getElementById('banReasonModal'));
      modal.hide();
      
      // 重置狀態
      setTimeout(() => {
        loading.value = false;
        banReason.value = '';
        currentMemberId.value = null;
      }, 500);
    };

    return {
      // 響應式資料
      banReason,
      showError,
      loading,

      // 方法
      showModal,
      confirmBan,
    };
  }
}
</script>

<style scoped>
/* **重點：模態框樣式優化** */
.modal-header {
  border-bottom: 1px solid #dee2e6;
}

.modal-footer {
  border-top: 1px solid #dee2e6;
}

/* **重點：表單樣式** */
.form-control {
  border-radius: 8px;
  resize: vertical;
}

.form-control:focus {
  border-color: #ffc107;
  box-shadow: 0 0 0 0.2rem rgba(255, 193, 7, 0.25);
}

/* **重點：警告訊息樣式** */
.alert-warning {
  border-radius: 8px;
  border-left: 4px solid #ffc107;
}
</style>