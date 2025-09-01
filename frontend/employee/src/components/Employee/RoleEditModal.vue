<template>
  <!-- Bootstrap Modal -->
  <div class="modal fade" :id="modalId" tabindex="-1" aria-labelledby="roleEditModalLabel" aria-hidden="true" ref="modalRef">
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title" id="roleEditModalLabel">
            編輯 {{ employeeToEdit?.empName }} 的職位
          </h5>
          <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
        </div>
        <div class="modal-body">
          <p class="text-muted">請勾選要指派給此員工的職位：</p>
          
          <!-- 職位複選框列表 -->
          <div v-if="allRoles.length > 0">
            <div v-for="role in allRoles" :key="role.role_id" class="form-check form-check-lg mb-2">
              <input 
                class="form-check-input" 
                type="checkbox" 
                :value="role.role_id" 
                :id="`modal-role-${role.role_id}`"
                v-model="selectedRoleIds"
                :disabled="isSaving"
              >
              <label class="form-check-label" :for="`modal-role-${role.role_id}`">
                {{ formatRoleLabel(role.role_name) }}
              </label>
            </div>
          </div>
          <div v-else class="alert alert-warning">
            沒有可用的職位選項。
          </div>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" @click="cancelModal" :disabled="isSaving">
            取消
          </button>
          <button type="button" class="btn btn-primary" @click="saveChanges" :disabled="isSaving">
            <span v-if="isSaving" class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
            {{ isSaving ? '儲存中...' : '儲存變更' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, watch, onMounted, onUnmounted } from 'vue';
import { Modal } from 'bootstrap';

export default {
  name: 'RoleEditModal',
  props: {
    // 要顯示的 Modal 的唯一 ID
    modalId: {
      type: String,
      default: 'roleEditModal'
    },
    // 正在編輯的員工物件
    employeeToEdit: {
      type: Object,
      default: null
    },
    // 所有可選的職位列表
    allRoles: {
      type: Array,
      required: true
    }
  },
  emits: ['update-roles'],

  setup(props, { emit }) {
    const modalRef = ref(null); // 用於獲取 Modal DOM 元素
    let modalInstance = null; // 存放 Bootstrap Modal 實例

    // 存放使用者勾選的職位 ID
    const selectedRoleIds = ref([]);
    
    // 儲存狀態，防止重複提交
    const isSaving = ref(false);

    /**
     * 初始化 Modal 實例
     */
    const initModal = () => {
      if (modalRef.value && !modalInstance) {
        modalInstance = new Modal(modalRef.value, {
          backdrop: 'static', // 防止點擊背景關閉，確保只能透過按鈕關閉
          keyboard: false, // 防止 ESC 鍵關閉
          focus: true
        });

        // 監聽 Modal 完全隱藏事件
        modalRef.value.addEventListener('hidden.bs.modal', handleModalHidden);
        
        // 監聽 Modal 顯示事件
        modalRef.value.addEventListener('shown.bs.modal', handleModalShown);
        
        console.log('[RoleEditModal.vue] Modal 實例已初始化');
      }
    };

    /**
     * 處理 Modal 隱藏事件
     */
    const handleModalHidden = () => {
      console.log('[RoleEditModal.vue] Modal 已完全關閉');
      
      // 重置所有狀態
      isSaving.value = false;
      
      // 移除可能殘留的 backdrop
      removeBackdrop();
      
      // 恢復 body 的滾動
      document.body.classList.remove('modal-open');
      document.body.style.overflow = '';
      document.body.style.paddingRight = '';
    };

    /**
     * 處理 Modal 顯示事件
     */
    const handleModalShown = () => {
      console.log('[RoleEditModal.vue] Modal 已完全顯示');
    };

    /**
     * 移除殘留的 backdrop
     */
    const removeBackdrop = () => {
      // 移除所有可能殘留的 backdrop
      const backdrops = document.querySelectorAll('.modal-backdrop');
      backdrops.forEach(backdrop => {
        backdrop.remove();
      });
    };

    /**
     * 強制關閉 Modal 的方法
     */
    const forceCloseModal = () => {
      console.log('[RoleEditModal.vue] 強制關閉 Modal');
      
      if (modalInstance) {
        modalInstance.dispose(); // 銷毀 Modal 實例
        modalInstance = null;
      }
      
      // 手動移除 Modal 相關的 CSS 類別和元素
      if (modalRef.value) {
        modalRef.value.classList.remove('show');
        modalRef.value.style.display = 'none';
        modalRef.value.setAttribute('aria-hidden', 'true');
        modalRef.value.removeAttribute('aria-modal');
      }
      
      // 移除 backdrop 和恢復 body 狀態
      removeBackdrop();
      document.body.classList.remove('modal-open');
      document.body.style.overflow = '';
      document.body.style.paddingRight = '';
      
      // 重新初始化 Modal 實例
      setTimeout(() => {
        initModal();
      }, 100);
    };

    // 元件掛載時初始化
    onMounted(() => {
      initModal();
    });

    // 元件卸載時清理
    onUnmounted(() => {
      if (modalRef.value) {
        modalRef.value.removeEventListener('hidden.bs.modal', handleModalHidden);
        modalRef.value.removeEventListener('shown.bs.modal', handleModalShown);
      }
      
      if (modalInstance) {
        modalInstance.dispose();
        modalInstance = null;
      }
      
      // 確保清理所有殘留效果
      removeBackdrop();
      document.body.classList.remove('modal-open');
      document.body.style.overflow = '';
      document.body.style.paddingRight = '';
    });

    // 監聽傳入的員工物件變化
    watch(() => props.employeeToEdit, (newEmployee) => {
      console.log('[RoleEditModal.vue] 員工資料變更:', newEmployee);
      
      if (newEmployee && newEmployee.roles) {
        // 當員工資料更新時，同步更新複選框的勾選狀態
        selectedRoleIds.value = newEmployee.roles.map(role => role.role_id);
        console.log('[RoleEditModal.vue] 設定現有職位 IDs:', selectedRoleIds.value);
      } else {
        selectedRoleIds.value = [];
      }
    }, { immediate: true, deep: true });

    /**
     * 格式化職位標籤，移除 "ROLE_" 前綴
     */
    const formatRoleLabel = (roleName) => {
      if (!roleName) return '';
      return roleName.startsWith('ROLE_') ? roleName.replace('ROLE_', '') : roleName;
    };

    /**
     * 取消 Modal
     */
    const cancelModal = () => {
      console.log('[RoleEditModal.vue] 使用者點擊取消');
      closeModal();
    };

    /**
     * 點擊儲存按鈕
     */
    const saveChanges = async () => {
      if (!props.employeeToEdit) {
        console.error('[RoleEditModal.vue] 沒有選中的員工');
        return;
      }

      if (isSaving.value) {
        console.log('[RoleEditModal.vue] 正在儲存中，忽略重複點擊');
        return;
      }
      
      isSaving.value = true;
      
      console.log('[RoleEditModal.vue] 準備儲存變更:', {
        empId: props.employeeToEdit.empId,
        selectedRoleIds: selectedRoleIds.value
      });
      
      try {
        // 發送 'update-roles' 事件給父元件
        emit('update-roles', {
          empId: props.employeeToEdit.empId,
          roleIds: selectedRoleIds.value
        });

        console.log('[RoleEditModal.vue] 職位更新事件已發送');
        
        // 延遲關閉 Modal，讓使用者看到儲存完成的狀態
        setTimeout(() => {
          closeModal();
        }, 500);
        
      } catch (error) {
        console.error('[RoleEditModal.vue] 儲存時發生錯誤:', error);
        isSaving.value = false;
      }
    };

    /**
     * 關閉 Modal 的統一方法
     */
    const closeModal = () => {
      console.log('[RoleEditModal.vue] 開始關閉 Modal');
      
      if (modalInstance) {
        try {
          modalInstance.hide();
          console.log('[RoleEditModal.vue] Modal.hide() 已呼叫');
        } catch (error) {
          console.error('[RoleEditModal.vue] Modal.hide() 失敗:', error);
          // 如果正常關閉失敗，使用強制關閉
          forceCloseModal();
        }
      } else {
        console.warn('[RoleEditModal.vue] Modal 實例不存在，使用強制關閉');
        forceCloseModal();
      }
    };

    return {
      modalRef,
      selectedRoleIds,
      isSaving,
      formatRoleLabel,
      saveChanges,
      cancelModal,
      closeModal,
      forceCloseModal
    };
  }
};
</script>

<style scoped>
/* 確保 Modal 在儲存時的視覺回饋 */
.modal-footer .btn:disabled {
  opacity: 0.6;
}

.spinner-border-sm {
  width: 1rem;
  height: 1rem;
}

/* 確保 Modal 正確顯示和隱藏 */
.modal {
  z-index: 1055;
}

.modal-backdrop {
  z-index: 1040;
}
</style>