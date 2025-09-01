<template>
  <div class="card">
    <div class="card-header d-flex flex-wrap align-items-center justify-content-between gap-3"
      style="border-bottom: none; padding-bottom: 0px;">

      <!-- 每頁顯示筆數選擇器 -->
      <div class="d-flex flex-wrap align-items-center gap-3">
        <div class="d-flex align-items-center gap-2">
          <select class="form-select form-select-lr w-auto rounded-3 me-10" v-model="membersPerPage"
            style="border-radius: 10px; height: 2.4rem;">
            <option value="10">10</option>
            <option value="15">15</option>
            <option value="20">20</option>
          </select>
          <span>筆/頁</span>
        </div>
      </div>

      <!-- 搜尋區塊 -->
      <div class="d-flex align-items-center gap-3">
        <!-- 狀態篩選按鈕 -->
        <div class="btn-group" role="group">
          <button @click="filterByStatus(true)" type="button" class="btn"
            :class="statusFilter ? 'btn-primary' : 'btn-outline-primary'">
            啟用會員
          </button>
          <button @click="filterByStatus(false)" type="button" class="btn"
            :class="!statusFilter ? 'btn-primary' : 'btn-outline-primary'">
            停權會員
          </button>
        </div>

        <!-- 活躍度篩選按鈕 -->
        <div class="btn-group" role="group">
          <button @click="filterByActivity(true)" type="button" class="btn"
            :class="activityFilter ? 'btn-success' : 'btn-outline-success'">
            活躍會員
          </button>
          <button @click="filterByActivity(false)" type="button" class="btn"
            :class="!activityFilter ? 'btn-success' : 'btn-outline-success'">
            閒置會員
          </button>
        </div>

        <!-- 搜尋輸入框 -->
        <form class="navbar-search">
          <input type="text" name="search" placeholder="搜尋會員姓名或電話" v-model="searchText" @input="handleSearch"
            @focus="isSearchFocused = true" @blur="isSearchFocused = false" />
          <iconify-icon icon="solar:minimalistic-magnifer-linear" class="icon"></iconify-icon>
        </form>
      </div>
    </div>

    <div class="card-body">
      <!-- 載入中狀態 -->
      <div v-if="loading" class="text-center p-5">
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">載入中...</span>
        </div>
        <p class="mt-3">載入會員資料中...</p>
      </div>

      <!-- 錯誤訊息 -->
      <div v-else-if="error" class="alert alert-danger">
        <iconify-icon icon="solar:danger-triangle-linear" class="me-2"></iconify-icon>
        <h5>載入錯誤</h5>
        <p>{{ error }}</p>
      </div>

      <!-- 空資料提示 -->
      <div v-else-if="paginatedMembers.length === 0" class="alert alert-info">
        <iconify-icon icon="solar:info-circle-linear" class="me-2"></iconify-icon>
        <h5>無會員資料</h5>
        <p>目前查詢條件下沒有會員資料。</p>
      </div>

      <!-- 會員資料表格 -->
      <div v-else class="table-responsive border rounded-10 overflow-hidden">
        <table class="table bordered-table mb-0">
          <thead class="bg-base">
            <tr>
              <th class="text-nowrap" style="padding-left: 10px;">會員ID</th>
              <th class="text-nowrap">會員資訊</th>
              <th class="text-nowrap text-center">性別</th>
              <th class="text-nowrap text-center">註冊日期</th>
              <th class="text-nowrap text-center">活躍度</th>
              <th class="text-nowrap text-center">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="member in paginatedMembers" :key="member.memberId">
              <!-- 會員ID -->
              <td style="padding-left: 10px;">
                <span class="fw-semibold text-primary">#{{ member.memberId }}</span>
              </td>
              
              <!-- 會員資訊 -->
              <td>
                <div class="d-flex align-items-center">
                  <!-- 會員頭像 -->
                  <div class="position-relative me-12">
                    <img :src="getMemberImage(member.imagePath)" alt="會員頭像" 
                      class="flex-shrink-0 radius-8 member-avatar"
                      width="44" height="44" @error="handleImageError">
                    <!-- **重點：會員狀態指示器** -->
                    <span class="position-absolute status-indicator" 
                      :class="member.active ? 'bg-success' : 'bg-danger'"
                      :title="member.active ? '啟用' : '停權'">
                    </span>
                  </div>
                  <div>
                    <!-- 會員姓名 -->
                    <div class="text-md mb-1 fw-semibold text-secondary-light">{{ member.memberName }}</div>
                    <!-- 聯絡電話 -->
                    <small class="text-muted d-flex align-items-center">
                      <iconify-icon icon="solar:phone-linear" class="me-1" width="12" height="12"></iconify-icon>
                      {{ member.mainPhone || '無電話' }}
                    </small>
                  </div>
                </div>
              </td>

              <!-- 性別 -->
              <td class="text-center">
                <span class="badge badge-gender px-3 py-2 rounded-pill fw-medium"
                  :class="getGenderClass(member.gender)">
                  <iconify-icon :icon="getGenderIcon(member.gender)" class="me-1" width="14" height="14"></iconify-icon>
                  {{ member.gender }}
                </span>
              </td>

              <!-- 創建日期 -->
              <td class="text-center">
                <div class="d-flex flex-column align-items-center">
                  <span class="text-sm fw-medium">{{ member.createdAt }}</span>
                </div>
              </td>

              <!-- 活躍度 -->
              <td class="text-center">
                <span :class="getActivityClass(member.isActiveUser)" 
                  class="badge badge-activity px-3 py-2 rounded-pill fw-medium">
                  <iconify-icon :icon="getActivityIcon(member.isActiveUser)" class="me-1" width="14" height="14"></iconify-icon>
                  {{ getActivityLabel(member.isActiveUser) }}
                </span>
              </td>

              <!-- **重點：改善的操作按鈕** -->
              <td class="text-center">
                <div class="d-flex gap-2 justify-content-center">
                  <!-- **重點：查看按鈕 - 使用透明眼睛圖示** -->
                  <button class="btn btn-action btn-view" 
                    @click="viewMember(member.memberId)"
                    :title="`檢視 ${member.memberName} 的詳細資料`">
                    <iconify-icon icon="solar:eye-linear" width="18" height="18"></iconify-icon>
                    <span class="btn-text">檢視</span>
                  </button>

                  <!-- **重點：停權/啟用按鈕 - 改善的設計** -->
                  <button class="btn btn-action" 
                    :class="getStatusButtonClass(member.active)"
                    @click="toggleMemberStatus(member.memberId)"
                    :disabled="buttonLoadingId === member.memberId"
                    :title="getStatusButtonTitle(member.active, member.memberName)">
                    
                    <!-- **重點：載入狀態** -->
                    <span v-if="buttonLoadingId === member.memberId" 
                      class="spinner-border spinner-border-sm me-1" role="status">
                    </span>
                    
                    <!-- **重點：狀態圖示** -->
                    <iconify-icon v-else 
                      :icon="getStatusButtonIcon(member.active)" 
                      width="18" height="18"
                      class="me-1">
                    </iconify-icon>
                    
                    <!-- **重點：按鈕文字** -->
                    <span class="btn-text">{{ getStatusButtonText(member.active) }}</span>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 分頁區域 -->
      <Pagination v-if="!loading && !error && paginatedMembers.length > 0" 
        :currentPage="currentPage"
        :totalPages="totalPages" 
        :startIndex="startIndex" 
        :endIndex="endIndex" 
        :totalItems="filteredMembers.length"
        @page-changed="changePage" />
    </div>

    <!-- 會員詳情模態框 -->
    <MemberDetailModal ref="memberDetailModal" />
    
    <!-- **重點：停權原因模態框** -->
    <BanReasonModal ref="banReasonModal" @confirm-ban="handleConfirmBan" />
  </div>
</template>

<script>
import { ref, computed, watch } from 'vue';
import Pagination from '@/components/pagination/index.vue';
import MemberDetailModal from './MemberDetailModal.vue';
import BanReasonModal from './BanReasonModal.vue'; 
import defaultAvatar from '@/assets/images/user-list/user-list1.png';
import { useToast } from '@/composables/useToast';
import { useConfirmDialog } from '@/composables/useConfirmDialog';

export default {
  name: 'MemberList',
  components: {
    Pagination,
    MemberDetailModal,
    BanReasonModal, // **重點：註冊停權原因模態框組件**
  },

  props: {
    // 會員資料陣列
    members: {
      type: Array,
      required: true,
      default: () => []
    },
    // 載入狀態
    loading: {
      type: Boolean,
      default: false
    },
    // 錯誤訊息
    error: {
      type: String,
      default: null
    },
    // 當前狀態篩選 (啟用/停用)
    activeFilter: {
      type: Boolean,
      default: true
    },
    // 當前活躍度篩選 (活躍/閒置)
    activityFilter: {
      type: Boolean,
      default: true
    }
  },

  emits: ['toggle-status', 'filter-status', 'filter-activity', 'view-member'],

  setup(props, { emit }) {
    // 分頁相關參數
    const currentPage = ref(1);
    const membersPerPage = ref(10);
    const searchText = ref('');
    const isSearchFocused = ref(false);
    
    // 篩選狀態
    const statusFilter = ref(props.activeFilter);
    const activityFilter = ref(props.activityFilter);
    
    // 載入狀態追蹤
    const buttonLoadingId = ref(null);
    
    // **重點：模態框的 ref 引用**
    const memberDetailModal = ref(null);
    const banReasonModal = ref(null);

    // 引入通知功能
    const { showToast } = useToast();
    const { showConfirmDialog } = useConfirmDialog();

    /**
     * **重點：取得會員圖片 URL**
     * @param {string} imagePath - 圖片路徑
     * @returns {string} 完整的圖片 URL
     */
    const getMemberImage = (imagePath) => {
      if (imagePath && imagePath !== '無圖片' && imagePath.trim() !== '') {
        if (imagePath.startsWith('http')) {
          return imagePath;
        }
        return `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'}${imagePath}`;
      }
      return defaultAvatar;
    };

    /**
     * **重點：圖片載入錯誤處理**
     * @param {Event} event - 錯誤事件
     */
    const handleImageError = (event) => {
      console.log('[MemberList] 圖片載入失敗，使用預設頭像');
      event.target.src = defaultAvatar;
    };

    /**
     * **重點：取得性別樣式類別**
     * @param {string} gender - 性別
     * @returns {string} CSS 類別字串
     */
    const getGenderClass = (gender) => {
      return gender === '男' 
        ? 'badge-male' 
        : 'badge-female';
    };

    /**
     * **重點：取得性別圖示**
     * @param {string} gender - 性別
     * @returns {string} 圖示名稱
     */
    const getGenderIcon = (gender) => {
      return gender === '男' 
        ? 'solar:men-linear' 
        : 'solar:women-linear';
    };

    /**
     * **重點：取得活躍度樣式類別**
     * @param {boolean} isActive - 是否活躍
     * @returns {string} CSS 類別字串
     */
    const getActivityClass = (isActive) => {
      return isActive 
        ? 'badge-active' 
        : 'badge-inactive';
    };

    /**
     * **重點：取得活躍度圖示**
     * @param {boolean} isActive - 是否活躍
     * @returns {string} 圖示名稱
     */
    const getActivityIcon = (isActive) => {
      return isActive 
        ? 'solar:pulse-2-linear' 
        : 'solar:sleeping-linear';
    };

    /**
     * **重點：取得活躍度標籤**
     * @param {boolean} isActive - 是否活躍
     * @returns {string} 活躍度標籤
     */
    const getActivityLabel = (isActive) => {
      return isActive ? '活躍' : '閒置';
    };

    /**
     * **重點：取得狀態按鈕樣式類別**
     * @param {boolean} isActive - 是否啟用
     * @returns {string} CSS 類別字串
     */
    const getStatusButtonClass = (isActive) => {
      return isActive 
        ? 'btn-ban' 
        : 'btn-activate';
    };

    /**
     * **重點：取得狀態按鈕圖示**
     * @param {boolean} isActive - 是否啟用
     * @returns {string} 圖示名稱
     */
    const getStatusButtonIcon = (isActive) => {
      return isActive 
        ? 'solar:shield-cross-linear' 
        : 'solar:shield-check-linear';
    };

    /**
     * **重點：取得狀態按鈕文字**
     * @param {boolean} isActive - 是否啟用
     * @returns {string} 按鈕文字
     */
    const getStatusButtonText = (isActive) => {
      return isActive ? '停權' : '啟用';
    };

    /**
     * **重點：取得狀態按鈕提示文字**
     * @param {boolean} isActive - 是否啟用
     * @param {string} memberName - 會員姓名
     * @returns {string} 提示文字
     */
    const getStatusButtonTitle = (isActive, memberName) => {
      return isActive 
        ? `停權會員 ${memberName}` 
        : `啟用會員 ${memberName}`;
    };

    /**
     * **重點：根據狀態篩選會員 (啟用/停用)**
     * @param {boolean} status - 是否篩選啟用的會員
     */
    const filterByStatus = (status) => {
      console.log('[MemberList] 切換狀態篩選為:', status ? '啟用' : '停用');
      emit('filter-status', status);
    };

    /**
     * **重點：根據活躍度篩選會員 (活躍/閒置)**
     * @param {boolean} activity - 是否篩選活躍的會員
     */
    const filterByActivity = (activity) => {
      console.log('[MemberList] 切換活躍度篩選為:', activity ? '活躍' : '閒置');
      emit('filter-activity', activity);
    };

    /**
     * **重點：處理搜尋事件 - 重置頁碼到第一頁**
     */
    const handleSearch = () => {
      currentPage.value = 1; // 搜尋時重置頁碼
    };

    /**
     * **重點：變更頁碼**
     * @param {number} page - 新的頁碼
     */
    const changePage = (page) => {
      currentPage.value = page;
    };

    /**
     * **重點：檢視會員詳情 - 開啟模態框顯示詳細資料**
     * @param {string} memberId - 會員ID
     */
    const viewMember = async (memberId) => {
      console.log('[MemberList] 檢視會員詳情:', memberId);
      
      try {
        // **重點：檢查模態框 ref 是否存在並呼叫顯示方法**
        if (memberDetailModal.value && memberDetailModal.value.showModal) {
          await memberDetailModal.value.showModal(memberId);
        } else {
          console.error('[MemberList] 會員詳情模態框未正確初始化');
          showToast({
            title: '系統錯誤',
            message: '會員詳情模態框初始化失敗',
            type: 'error'
          });
          return;
        }
        
        // 同時觸發父元件事件 (用於額外的處理邏輯)
        emit('view-member', memberId);
      } catch (error) {
        console.error('[MemberList] 檢視會員詳情失敗:', error);
        
        showToast({
          title: '檢視失敗',
          message: '無法載入會員詳情，請稍後再試',
          type: 'error'
        });
      }
    };

    /**
     * **重點：切換會員狀態 - 整合停權原因輸入功能**
     * @param {string} memberId - 會員ID
     */
    const toggleMemberStatus = async (memberId) => {
      try {
        // 設定載入狀態，防止重複點擊
        buttonLoadingId.value = memberId;

        // 找到對應的會員資料
        const member = props.members.find(m => m.memberId === memberId);
        if (!member) {
          throw new Error('找不到指定的會員資料');
        }

        const isCurrentlyActive = member.active;

        // **重點：如果是要停權，顯示停權原因輸入框**
        if (isCurrentlyActive) {
          // 顯示停權原因模態框
          if (banReasonModal.value && banReasonModal.value.showModal) {
            banReasonModal.value.showModal(memberId);
          } else {
            console.error('[MemberList] 停權原因模態框未正確初始化');
            showToast({
              title: '系統錯誤',
              message: '停權原因模態框初始化失敗',
              type: 'error'
            });
          }
        } else {
          // **重點：如果是要啟用，直接顯示確認對話框**
          const confirmed = await showConfirmDialog({
            title: '啟用會員',
            message: `您確定要啟用會員「${member.memberName}」嗎？`,
            type: 'info',
            confirmText: '確認啟用',
            cancelText: '取消',
            icon: 'solar:shield-check-linear'
          });

          // 如果用戶確認啟用，則執行狀態切換
          if (confirmed) {
            emit('toggle-status', memberId, ''); // 啟用時不需要停權原因
          }
        }
      } catch (error) {
        console.error('[MemberList] 狀態切換出錯:', error);
        
        showToast({
          title: '操作失敗',
          message: error.message || '切換會員狀態時發生錯誤',
          type: 'error'
        });
      } finally {
        // 無論成功或失敗都要清除載入狀態
        buttonLoadingId.value = null;
      }
    };

    /**
     * **重點：處理停權原因確認事件**
     * @param {Object} banData - 停權資料
     * @param {string} banData.memberId - 會員ID
     * @param {string} banData.banReason - 停權原因
     */
    const handleConfirmBan = async (banData) => {
      try {
        const { memberId, banReason } = banData;
        
        console.log('[MemberList] 確認停權會員:', {
          memberId,
          banReason
        });

        // **重點：觸發父元件的狀態切換事件，傳入停權原因**
        emit('toggle-status', memberId, banReason);
      } catch (error) {      
        showToast({
          title: '停權失敗',
          message: error.message || '處理停權請求時發生錯誤',
          type: 'error'
        });
      }
    };

    // **重點：監聽父元件傳來的篩選狀態變化**
    watch(() => props.activeFilter, (newValue) => {
      console.log('[MemberList] 父元件狀態篩選變化:', newValue ? '啟用' : '停用');
      statusFilter.value = newValue;
    });

    watch(() => props.activityFilter, (newValue) => {
      console.log('[MemberList] 父元件活躍度篩選變化:', newValue ? '活躍' : '閒置');
      activityFilter.value = newValue;
    });

    // **重點：計算篩選後的會員列表**
    const filteredMembers = computed(() => {
      let result = [...props.members];

      // 根據啟用狀態篩選
      result = result.filter(member => member.active === statusFilter.value);

      // 根據活躍度篩選
      result = result.filter(member => member.isActiveUser === activityFilter.value);

      // 根據搜尋文字篩選 (姓名、電話、信箱)
      if (searchText.value) {
        const search = searchText.value.toLowerCase();
        result = result.filter(member =>
          (member.memberName && member.memberName.toLowerCase().includes(search)) ||
          (member.mainPhone && member.mainPhone.toLowerCase().includes(search)) ||
          (member.email && member.email.toLowerCase().includes(search))
        );
      }

      return result;
    });

    // **重點：計算當前頁面要顯示的會員資料**
    const paginatedMembers = computed(() => {
      const startIdx = (currentPage.value - 1) * membersPerPage.value;
      const endIdx = startIdx + parseInt(membersPerPage.value);
      return filteredMembers.value.slice(startIdx, endIdx);
    });

    // **重點：計算總頁數**
    const totalPages = computed(() => {
      return Math.ceil(filteredMembers.value.length / membersPerPage.value) || 1;
    });

    // **重點：計算當前頁面的起始和結束索引 (用於分頁顯示)**
    const startIndex = computed(() => {
      return (currentPage.value - 1) * membersPerPage.value;
    });

    const endIndex = computed(() => {
      return Math.min(startIndex.value + parseInt(membersPerPage.value), filteredMembers.value.length);
    });

    // **重點：監聽會員資料變化，確保數據更新時重置頁碼**
    watch(() => props.members, () => {
      if (currentPage.value > totalPages.value && totalPages.value > 0) {
        currentPage.value = 1;
      }
    }, { deep: true });

    return {
      // 響應式資料
      currentPage,
      membersPerPage,
      searchText,
      isSearchFocused,
      statusFilter,
      activityFilter,
      buttonLoadingId,

      // 計算屬性
      filteredMembers,
      paginatedMembers,
      totalPages,
      startIndex,
      endIndex,

      // 圖片和樣式方法
      getMemberImage,
      handleImageError,
      getGenderClass,
      getGenderIcon,
      getActivityClass,
      getActivityIcon,
      getActivityLabel,
      
      // **重點：新增狀態按鈕相關方法**
      getStatusButtonClass,
      getStatusButtonIcon,
      getStatusButtonText,
      getStatusButtonTitle,

      // 功能方法
      filterByStatus,
      filterByActivity,
      handleSearch,
      changePage,
      viewMember,
      toggleMemberStatus,

      // **重點：模態框引用**
      memberDetailModal,
      banReasonModal,
      
      // **重點：停權確認處理方法**
      handleConfirmBan,
    };
  }
}
</script>

<style scoped>
/* **重點：表格基本樣式** */
.table td {
  vertical-align: middle;
  padding: 16px 12px;
  border-bottom: 1px solid #f1f5f9;
}

.table thead th {
  background-color: #f8fafc !important;
  border-bottom: 2px solid #e2e8f0;
  font-weight: 600;
  color: #475569;
  padding: 16px 12px;
}

/* **重點：會員頭像樣式增強** */
.member-avatar {
  object-fit: cover;
  border: 3px solid #ffffff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.member-avatar:hover {
  transform: scale(1.05);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
}

/* **重點：狀態指示器** */
.status-indicator {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  border: 2px solid white;
  bottom: 2px;
  right: 2px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

/* **重點：性別徽章樣式** */
.badge-gender {
  font-size: 0.875rem;
  font-weight: 500;
  letter-spacing: 0.025em;
}

.badge-male {
  background: linear-gradient(135deg, #3b82f6, #1d4ed8);
  color: white;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.3);
}

.badge-female {
  background: linear-gradient(135deg, #ec4899, #be185d);
  color: white;
  box-shadow: 0 2px 8px rgba(236, 72, 153, 0.3);
}

/* **重點：活躍度徽章樣式** */
.badge-activity {
  font-size: 0.875rem;
  font-weight: 500;
  letter-spacing: 0.025em;
}

.badge-active {
  background: linear-gradient(135deg, #10b981, #047857);
  color: white;
  box-shadow: 0 2px 8px rgba(16, 185, 129, 0.3);
}

.badge-inactive {
  background: linear-gradient(135deg, #f59e0b, #d97706);
  color: white;
  box-shadow: 0 2px 8px rgba(245, 158, 11, 0.3);
}

/* **重點：操作按鈕基礎樣式** */
.btn-action {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 80px;
  padding: 8px 16px;
  font-size: 0.875rem;
  font-weight: 500;
  border-radius: 8px;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.btn-action:before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s;
}

.btn-action:hover:before {
  left: 100%;
}

/* **重點：檢視按鈕樣式** */
.btn-view {
  background: linear-gradient(135deg, #6366f1, #4f46e5);
  border: none;
  color: white;
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
}

.btn-view:hover {
  background: linear-gradient(135deg, #4f46e5, #4338ca);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(99, 102, 241, 0.4);
  color: white;
}

.btn-view:active {
  transform: translateY(0);
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.3);
}

/* **重點：停權按鈕樣式** */
.btn-ban {
  background: linear-gradient(135deg, #ef4444, #dc2626);
  border: none;
  color: white;
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.3);
}

.btn-ban:hover {
  background: linear-gradient(135deg, #dc2626, #b91c1c);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(239, 68, 68, 0.4);
  color: white;
}

.btn-ban:active {
  transform: translateY(0);
  box-shadow: 0 2px 8px rgba(239, 68, 68, 0.3);
}

/* **重點：啟用按鈕樣式** */
.btn-activate {
  background: linear-gradient(135deg, #10b981, #059669);
  border: none;
  color: white;
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
}

.btn-activate:hover {
  background: linear-gradient(135deg, #059669, #047857);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(16, 185, 129, 0.4);
  color: white;
}

.btn-activate:active {
  transform: translateY(0);
  box-shadow: 0 2px 8px rgba(16, 185, 129, 0.3);
}

/* **重點：按鈕禁用狀態** */
.btn-action:disabled {
  background: #94a3b8 !important;
  color: #64748b !important;
  cursor: not-allowed;
  transform: none !important;
  box-shadow: none !important;
}

.btn-action:disabled:before {
  display: none;
}

/* **重點：按鈕文字動畫** */
.btn-text {
  transition: all 0.3s ease;
}

.btn-action:hover .btn-text {
  letter-spacing: 0.05em;
}

/* **重點：會員ID樣式** */
.text-primary {
  font-family: 'Courier New', monospace;
  font-weight: 600;
}

/* **重點：表格hover效果** */
.table tbody tr {
  transition: all 0.2s ease;
}

.table tbody tr:hover {
  background-color: #f8fafc;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

/* **重點：響應式調整** */
@media (max-width: 768px) {
  .card-header {
    flex-direction: column;
    align-items: stretch !important;
  }

  .btn-group {
    width: 100%;
    margin-bottom: 10px;
  }

  .btn-group .btn {
    flex: 1;
    font-size: 0.8rem;
    padding: 8px 12px;
  }

  .navbar-search {
    width: 100%;
    margin-top: 10px;
  }

  /* **重點：手機版按鈕調整** */
  .btn-action {
    min-width: 60px;
    padding: 6px 10px;
    font-size: 0.8rem;
  }

  .btn-text {
    display: none; /* 手機版隱藏文字，只顯示圖示 */
  }

  .member-avatar {
    width: 36px !important;
    height: 36px !important;
  }

  .table td {
    padding: 12px 8px;
  }

  /* **重點：手機版徽章調整** */
  .badge-gender,
  .badge-activity {
    font-size: 0.75rem;
    padding: 4px 8px !important;
  }
}

@media (max-width: 576px) {
  /* **重點：極小螢幕優化** */
  .d-flex.gap-2 {
    gap: 1rem !important;
  }

  .btn-action {
    min-width: 44px;
    padding: 8px;
  }

  .table {
    font-size: 0.875rem;
  }

  /* **重點：極小螢幕表格調整** */
  .table th,
  .table td {
    padding: 8px 4px;
  }

  .table th:nth-child(4),
  .table td:nth-child(4) {
    display: none; /* 隱藏創建日期欄位 */
  }
}

/* **重點：自訂滾動條樣式** */
.table-responsive::-webkit-scrollbar {
  height: 8px;
}

.table-responsive::-webkit-scrollbar-track {
  background: #f1f5f9;
  border-radius: 4px;
}

.table-responsive::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 4px;
}

.table-responsive::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}

/* **重點：載入動畫優化** */
.spinner-border-sm {
  width: 16px;
  height: 16px;
  border-width: 2px;
}

/* **重點：搜尋框圖示調整** */
.navbar-search .icon {
  color: #64748b;
  transition: color 0.3s ease;
}

.navbar-search:focus-within .icon {
  color: #3b82f6;
}
</style>
