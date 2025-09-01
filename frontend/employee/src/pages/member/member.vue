<template>
  <div>
    <!-- 頁面標題 -->
    <div class="d-flex flex-wrap align-items-center justify-content-between gap-3 mb-24">
    </div>

    <!-- 會員列表組件 -->
    <MemberList 
      :members="members"
      :loading="loading"
      :error="error"
      :active-filter="statusFilter"
      :activity-filter="activityFilter"
      @toggle-status="handleToggleStatus"
      @filter-status="handleStatusFilter"
      @filter-activity="handleActivityFilter"
      @view-member="handleViewMember"
    />

    <!-- 確認對話框組件 -->
    <ConfirmDialog />

    <!-- Toast 通知組件 -->
    <ToastNotification />
  </div>
</template>

<script>
import { ref, onMounted, watch } from 'vue';
import MemberList from '@/components/Member/MemberList.vue';
import ConfirmDialog from '@/components/common/ConfirmDialog.vue';
import ToastNotification from '@/components/ToastContainer.vue';
import { memberService } from '@/services/memberService';
import { useToast } from '@/composables/useToast';

export default {
  name: 'MemberPage',
  components: {
    MemberList,
    ConfirmDialog,
    ToastNotification,
  },

  setup() {
    // 響應式資料
    const members = ref([]);
    const loading = ref(false);
    const error = ref(null);
    const statusFilter = ref(true); // 預設顯示啟用會員
    const activityFilter = ref(true); // 預設顯示活躍會員

    // 引入 Toast 通知功能
    const { showToast } = useToast();

    /**
     * **重點：載入會員資料 - 支援查詢參數**
     * @param {Object} queryOptions - 查詢選項
     */
    const loadMembers = async (queryOptions = {}) => {
      loading.value = true;
      error.value = null;
      
      try {
        console.log('[MemberPage] 開始載入會員資料，查詢條件:', queryOptions);
        
        // **重點：根據當前篩選狀態設定查詢參數**
        const searchParams = {
          ...queryOptions,
          isActive: statusFilter.value // 根據當前狀態篩選
        };
        
        const memberData = await memberService.getAllMembers(searchParams);
        
        members.value = memberData;
        console.log('[MemberPage] 會員資料載入成功:', {
          總數: memberData.length,
          啟用會員: memberData.filter(m => m.active).length,
          停權會員: memberData.filter(m => !m.active).length,
          查詢條件: searchParams
        });
      } catch (err) {
        console.error('[MemberPage] 載入會員資料失敗:', err);
        error.value = err.message || '載入會員資料時發生錯誤';
        
        showToast({
          title: '載入失敗',
          message: error.value,
          type: 'error'
        });
      } finally {
        loading.value = false;
      }
    };

    /**
     * **重點：處理會員狀態切換 - 支援停權原因參數**
     * @param {string} memberId - 會員ID
     * @param {string} banReason - 停權原因 (停權時使用)
     */
    const handleToggleStatus = async (memberId, banReason = '') => {
      try {
        console.log('[MemberPage] 開始切換會員狀態:', {
          memberId,
          banReason: banReason || '無'
        });
        
        // **重點：找到對應的會員資料**
        const member = members.value.find(m => m.memberId === memberId);
        if (!member) {
          throw new Error('找不到指定的會員');
        }

        const currentStatus = member.active;
        const newStatus = !currentStatus;
        const actionText = newStatus ? '啟用' : '停權';
        
        console.log('[MemberPage] 會員狀態變更:', {
          memberId,
          memberName: member.memberName,
          currentStatus: currentStatus ? '啟用' : '停權',
          newStatus: newStatus ? '啟用' : '停權',
          banReason: newStatus ? '無' : banReason
        });
        
        // **重點：呼叫 memberService 的切換狀態方法，傳入停權原因**
        const updatedMember = await memberService.toggleMemberStatus(
          memberId, 
          newStatus, 
          banReason || (newStatus ? '' : '管理員停權')
        );
        
        // **重點：更新本地資料狀態**
        const memberIndex = members.value.findIndex(m => m.memberId === memberId);
        if (memberIndex !== -1) {
          members.value[memberIndex] = updatedMember;
        }
        
        // **重點：顯示成功通知，包含停權原因資訊**
        const successMessage = newStatus 
          ? `會員「${member.memberName}」已啟用`
          : `會員「${member.memberName}」已停權${banReason ? `，原因：${banReason}` : ''}`;
        
        showToast({
          title: `${actionText}成功`,
          message: successMessage,
          type: 'success',
          icon: newStatus ? 'solar:shield-check-bold' : 'solar:shield-cross-bold'
        });
        
        console.log('[MemberPage] 會員狀態切換成功:', {
          memberId,
          newStatus: newStatus ? '啟用' : '停權',
          banReason: banReason || '無'
        });
      } catch (err) {
        console.error('[MemberPage] 切換會員狀態失敗:', err);
        
        // **重點：顯示錯誤通知**
        showToast({
          title: '狀態更新失敗',
          message: err.message || '切換會員狀態時發生錯誤',
          type: 'error',
          icon: 'solar:danger-triangle-bold'
        });
      }
    };

    /**
     * **重點：處理狀態篩選變更 - 重新載入資料**
     * @param {boolean} isActiveFilter - 是否篩選啟用會員
     */
    const handleStatusFilter = async (isActiveFilter) => {
      console.log('[MemberPage] 變更狀態篩選條件:', isActiveFilter ? '啟用會員' : '停權會員');
      statusFilter.value = isActiveFilter;
      
      // **重點：狀態篩選變更時重新載入資料**
      await loadMembers();
    };

    /**
     * **重點：處理活躍度篩選變更 (活躍/閒置)**
     * @param {boolean} isActivityFilter - 是否篩選活躍會員
     */
    const handleActivityFilter = (isActivityFilter) => {
      console.log('[MemberPage] 變更活躍度篩選條件:', isActivityFilter ? '活躍會員' : '閒置會員');
      activityFilter.value = isActivityFilter;
    };

    /**
     * **重點：處理檢視會員詳情**
     * @param {string} memberId - 會員ID
     */
    const handleViewMember = (memberId) => {
      console.log('[MemberPage] 檢視會員詳情觸發:', memberId);
      // 實際的檢視邏輯已在 MemberList 組件中實作
      // 這裡可以添加額外的處理邏輯，如分析追蹤等
    };

    // **重點：監聽狀態篩選變化，自動重新載入資料**
    watch(statusFilter, () => {
      console.log('[MemberPage] 狀態篩選變化，重新載入資料');
      loadMembers();
    });

    // **重點：組件載入時執行**
    onMounted(() => {
      loadMembers();
    });

    return {
      // 響應式資料
      members,
      loading,
      error,
      statusFilter,
      activityFilter,

      // 方法
      loadMembers,
      handleToggleStatus,
      handleStatusFilter,
      handleActivityFilter,
      handleViewMember,
    };
  }
}
</script>

<style scoped>
/* 頁面標題樣式 */
h6 {
  color: var(--neutral-700);
}

/* 麵包屑導航樣式 */
.hover-text-primary:hover {
  color: var(--primary-600) !important;
}

/* 響應式調整 */
@media (max-width: 768px) {
  .d-flex.justify-content-between {
    flex-direction: column;
    gap: 1rem;
  }
}
</style>