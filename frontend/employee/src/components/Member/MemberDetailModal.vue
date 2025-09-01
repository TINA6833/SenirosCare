<template>
  <!-- 會員詳情模態框 -->
  <div class="modal fade" id="memberDetailModal" tabindex="-1" aria-labelledby="memberDetailModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-lg modal-dialog-centered">
      <div class="modal-content">
        <!-- 模態框標題 -->
        <div class="modal-header bg-primary">
          <h1 class="modal-title fs-5 text-white" id="memberDetailModalLabel">
            <iconify-icon icon="solar:user-id-bold" class="me-3"></iconify-icon>
            會員詳細資訊
          </h1>
          <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
        </div>

        <!-- 模態框內容 -->
        <div class="modal-body">
          <!-- 載入中狀態 -->
          <div v-if="loading" class="text-center py-5">
            <div class="spinner-border text-primary" role="status">
              <span class="visually-hidden">載入中...</span>
            </div>
            <p class="mt-3 text-muted">載入會員資料中...</p>
          </div>

          <!-- 錯誤狀態 -->
          <div v-else-if="error" class="alert alert-danger">
            <iconify-icon icon="solar:danger-triangle-bold" class="me-3"></iconify-icon>
            <strong>載入失敗</strong>
            <p class="mb-0 mt-2">{{ error }}</p>
          </div>

          <!-- 會員詳情內容 -->
          <div v-else-if="memberDetail" class="row">
            <!-- 左側：頭像和基本資訊 -->
            <div class="col-md-4 text-center mb-4">
              <!-- 會員頭像 -->
              <div class="position-relative d-inline-block mb-3">
                <img 
                  :src="getMemberImage(memberDetail.imagePath)" 
                  alt="會員頭像" 
                  class="rounded-circle border border-3 border-light shadow"
                  width="120" 
                  height="120"
                  style="object-fit: cover;"
                  @error="handleImageError">
                <!-- 狀態徽章 -->
                <span class="position-absolute bottom-0 end-0 translate-middle badge rounded-pill"
                  :class="memberDetail.active ? 'bg-success' : 'bg-danger'">
                  <iconify-icon :icon="memberDetail.active ? 'solar:check-circle-bold' : 'solar:close-circle-bold'" 
                    width="16" height="16" class="me-1"></iconify-icon>
                  {{ memberDetail.active ? '啟用' : '停用' }}
                </span>
              </div>

              <!-- 會員名稱 -->
              <h4 class="fw-bold text-dark mb-3">{{ memberDetail.memberName }}</h4>
              
              <!-- 活躍度標籤 -->
              <span class="badge px-3 py-2 rounded-pill fw-medium"
                :class="getActivityClass(memberDetail.isActiveUser)">
                <iconify-icon :icon="memberDetail.isActiveUser ? 'solar:user-check-bold' : 'solar:user-cross-bold'" 
                  class="me-2" width="14" height="14"></iconify-icon>
                {{ getActivityLabel(memberDetail.isActiveUser) }}
              </span>
            </div>

            <!-- 右側：詳細資訊 -->
            <div class="col-md-8">
              <!-- 個人基本資訊 -->
              <div class="card border-0 shadow-sm mb-4">
                <div class="card-header bg-light">
                  <h6 class="mb-0 fw-semibold">
                    <iconify-icon icon="solar:user-bold" class="me-3 text-primary"></iconify-icon>
                    個人資訊
                  </h6>
                </div>
                <div class="card-body">
                  <div class="row g-4">
                    <!-- 會員ID -->
                    <div class="col-sm-6">
                      <div class="d-flex align-items-center">
                        <div class="icon-wrapper me-3">
                          <iconify-icon icon="solar:hashtag-bold" class="text-muted" width="20" height="20"></iconify-icon>
                        </div>
                        <div>
                          <small class="text-muted d-block mb-1">會員ID</small>
                          <span class="fw-medium text-dark">{{ memberDetail.memberId }}</span>
                        </div>
                      </div>
                    </div>

                    <!-- 性別 -->
                    <div class="col-sm-6">
                      <div class="d-flex align-items-center">
                        <div class="icon-wrapper me-3">
                          <iconify-icon icon="solar:users-group-two-rounded-bold" class="text-muted" width="20" height="20"></iconify-icon>
                        </div>
                        <div>
                          <small class="text-muted d-block mb-1">性別</small>
                          <span class="badge rounded-pill px-3 py-1" :class="getGenderClass(memberDetail.gender)">
                            {{ memberDetail.gender }}
                          </span>
                        </div>
                      </div>
                    </div>

                    <!-- 生日 -->
                    <div class="col-sm-6">
                      <div class="d-flex align-items-center">
                        <div class="icon-wrapper me-3">
                          <iconify-icon icon="solar:calendar-bold" class="text-muted" width="20" height="20"></iconify-icon>
                        </div>
                        <div>
                          <small class="text-muted d-block mb-1">生日</small>
                          <span class="fw-medium text-dark">{{ memberDetail.birthday }}</span>
                        </div>
                      </div>
                    </div>

                    <!-- 聯絡電話 -->
                    <div class="col-sm-6">
                      <div class="d-flex align-items-center">
                        <div class="icon-wrapper me-3">
                          <iconify-icon icon="solar:phone-bold" class="text-muted" width="20" height="20"></iconify-icon>
                        </div>
                        <div>
                          <small class="text-muted d-block mb-1">聯絡電話</small>
                          <span class="fw-medium text-dark">{{ memberDetail.mainPhone || '無資料' }}</span>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 聯絡資訊 -->
              <div class="card border-0 shadow-sm mb-4">
                <div class="card-header bg-light">
                  <h6 class="mb-0 fw-semibold">
                    <iconify-icon icon="solar:mailbox-bold" class="me-3 text-primary"></iconify-icon>
                    聯絡資訊
                  </h6>
                </div>
                <div class="card-body">
                  <div class="row g-4">
                    <!-- Email -->
                    <div class="col-12">
                      <div class="d-flex align-items-center">
                        <div class="icon-wrapper me-3">
                          <iconify-icon icon="solar:letter-bold" class="text-muted" width="20" height="20"></iconify-icon>
                        </div>
                        <div>
                          <small class="text-muted d-block mb-1">電子信箱</small>
                          <span class="fw-medium text-dark">{{ memberDetail.email || '無資料' }}</span>
                        </div>
                      </div>
                    </div>

                    <!-- 地址 -->
                    <div class="col-12">
                      <div class="d-flex align-items-start">
                        <div class="icon-wrapper me-3 mt-1">
                          <iconify-icon icon="solar:map-point-bold" class="text-muted" width="20" height="20"></iconify-icon>
                        </div>
                        <div>
                          <small class="text-muted d-block mb-1">地址</small>
                          <span class="fw-medium text-dark">{{ memberDetail.address || '無資料' }}</span>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 系統資訊 -->
              <div class="card border-0 shadow-sm">
                <div class="card-header bg-light">
                  <h6 class="mb-0 fw-semibold">
                    <iconify-icon icon="solar:database-bold" class="me-3 text-primary"></iconify-icon>
                    系統資訊
                  </h6>
                </div>
                <div class="card-body">
                  <div class="row g-4">
                    <!-- 註冊日期 -->
                    <div class="col-sm-6">
                      <div class="d-flex align-items-center">
                        <div class="icon-wrapper me-3">
                          <iconify-icon icon="solar:calendar-add-bold" class="text-muted" width="20" height="20"></iconify-icon>
                        </div>
                        <div>
                          <small class="text-muted d-block mb-1">註冊日期</small>
                          <span class="fw-medium text-dark">{{ memberDetail.createdAt }}</span>
                        </div>
                      </div>
                    </div>

                    <!-- 最後更新 -->
                    <div class="col-sm-6">
                      <div class="d-flex align-items-center">
                        <div class="icon-wrapper me-3">
                          <iconify-icon icon="solar:refresh-bold" class="text-muted" width="20" height="20"></iconify-icon>
                        </div>
                        <div>
                          <small class="text-muted d-block mb-1">最後更新</small>
                          <span class="fw-medium text-dark">{{ memberDetail.updatedAt }}</span>
                        </div>
                      </div>
                    </div>

                    <!-- 最後登入 -->
                    <div class="col-sm-6">
                      <div class="d-flex align-items-center">
                        <div class="icon-wrapper me-3">
                          <iconify-icon icon="solar:login-3-bold" class="text-muted" width="20" height="20"></iconify-icon>
                        </div>
                        <div>
                          <small class="text-muted d-block mb-1">最後登入</small>
                          <span class="fw-medium text-dark">{{ memberDetail.loginAt }}</span>
                        </div>
                      </div>
                    </div>

                    <!-- **重點：新增停權原因欄位** -->
                    <div class="col-sm-6">
                      <div class="d-flex align-items-start">
                        <div class="icon-wrapper me-3 mt-1">
                          <iconify-icon 
                            :icon="memberDetail.active ? 'solar:shield-check-bold' : 'solar:shield-cross-bold'" 
                            :class="memberDetail.active ? 'text-success' : 'text-danger'" 
                            width="20" 
                            height="20">
                          </iconify-icon>
                        </div>
                        <div class="flex-grow-1">
                          <small class="text-muted d-block mb-1">停權原因</small>
                          <div v-if="!memberDetail.active && memberDetail.banReason" 
                            class="alert alert-warning py-2 px-3 mb-0" 
                            style="font-size: 0.875rem;">
                            <iconify-icon icon="solar:danger-triangle-bold" class="me-2" width="16" height="16"></iconify-icon>
                            <span class="fw-medium">{{ memberDetail.banReason }}</span>
                          </div>
                          <span v-else class="fw-medium text-dark">
                            {{ getBanReasonDisplay(memberDetail.active, memberDetail.banReason) }}
                          </span>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 模態框底部 -->
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">
            <iconify-icon icon="solar:close-circle-bold" class="me-2"></iconify-icon>
            關閉
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue';
import { memberService } from '@/services/memberService';
import { useToast } from '@/composables/useToast';
import defaultAvatar from '@/assets/images/user-list/user-list1.png';

export default {
  name: 'MemberDetailModal',
  
  setup() {
    // 響應式資料
    const memberDetail = ref(null);
    const loading = ref(false);
    const error = ref(null);
    
    // 引入 Toast 通知功能
    const { showToast } = useToast();

    /**
     * 取得會員圖片 URL
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
     * 圖片載入錯誤處理
     * @param {Event} event - 錯誤事件
     */
    const handleImageError = (event) => {
      console.log('[MemberDetailModal] 圖片載入失敗，使用預設頭像');
      event.target.src = defaultAvatar;
    };

    /**
     * 取得性別樣式類別
     * @param {string} gender - 性別
     * @returns {string} CSS 類別字串
     */
    const getGenderClass = (gender) => {
      return gender === '男' 
        ? 'bg-info text-white' 
        : 'bg-pink text-white';
    };

    /**
     * 取得活躍度樣式類別
     * @param {boolean} isActive - 是否活躍
     * @returns {string} CSS 類別字串
     */
    const getActivityClass = (isActive) => {
      return isActive 
        ? 'bg-success text-white' 
        : 'bg-warning text-dark';
    };

    /**
     * 取得活躍度標籤
     * @param {boolean} isActive - 是否活躍
     * @returns {string} 活躍度標籤
     */
    const getActivityLabel = (isActive) => {
      return isActive ? '活躍會員' : '閒置會員';
    };

    /**
     * **重點：取得停權原因顯示文字**
     * @param {boolean} isActive - 會員是否啟用
     * @param {string} banReason - 停權原因
     * @returns {string} 停權原因顯示文字
     */
    const getBanReasonDisplay = (isActive, banReason) => {
      // **重點：如果會員是啟用狀態，顯示「無」**
      if (isActive) {
        return '無';
      }
      
      // **重點：如果會員停權但沒有停權原因，顯示「無」**
      if (!banReason || banReason.trim() === '') {
        return '無';
      }
      
      // **重點：如果有停權原因，直接返回原因（在特殊樣式中顯示）**
      return banReason;
    };

    /**
     * 載入會員詳細資料
     * @param {string} memberId - 會員ID
     */
    const loadMemberDetail = async (memberId) => {
      if (!memberId) {
        error.value = '會員ID不能為空';
        return;
      }

      loading.value = true;
      error.value = null;
      memberDetail.value = null;

      try {
        console.log('[MemberDetailModal] 開始載入會員詳情:', memberId);
        
        const data = await memberService.getMemberById(memberId);
        memberDetail.value = data;
        
        console.log('[MemberDetailModal] 會員詳情載入成功:', {
          memberId: data.memberId,
          memberName: data.memberName,
          active: data.active,
          banReason: data.banReason || '無'
        });
      } catch (err) {
        console.error('[MemberDetailModal] 載入會員詳情失敗:', err);
        error.value = err.message || '載入會員詳情時發生錯誤';
        
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
     * 顯示模態框
     * @param {string} memberId - 會員ID
     */
    const showModal = async (memberId) => {
      // 載入會員詳情
      await loadMemberDetail(memberId);
      
      // 顯示模態框
      const modal = new bootstrap.Modal(document.getElementById('memberDetailModal'));
      modal.show();
    };

    /**
     * 重設模態框狀態
     */
    const resetModal = () => {
      memberDetail.value = null;
      loading.value = false;
      error.value = null;
    };

    return {
      // 響應式資料
      memberDetail,
      loading,
      error,

      // 方法
      getMemberImage,
      handleImageError,
      getGenderClass,
      getActivityClass,
      getActivityLabel,
      getBanReasonDisplay, // **重點：新增停權原因顯示方法**
      loadMemberDetail,
      showModal,
      resetModal,
    };
  }
}
</script>

<style scoped>
/* **重點：統一圖示容器樣式，確保圖示與文字的對齊和距離** */
.icon-wrapper {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f8f9fa;
  border-radius: 6px;
  flex-shrink: 0;
}

/* **重點：停權狀態圖示的動態顏色** */
.icon-wrapper .text-success {
  color: #28a745 !important;
}

.icon-wrapper .text-danger {
  color: #dc3545 !important;
}

/* 自訂粉色樣式 */
.bg-pink {
  background-color: #ec4899 !important;
}

.text-pink {
  color: #ec4899 !important;
}

/* **重點：停權原因警告框樣式** */
.alert-warning {
  border-radius: 8px;
  border-left: 4px solid #ffc107;
  background-color: #fff3cd;
  border-color: #ffeaa7;
}

.alert-warning .fw-medium {
  color: #856404;
}

/* **重點：模態框樣式優化** */
.modal-header {
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  padding: 1.25rem 1.5rem;
}

.modal-body {
  padding: 2rem 1.5rem;
}

.modal-footer {
  padding: 1.25rem 1.5rem;
  border-top: 1px solid #dee2e6;
}

/* **重點：卡片樣式優化，增加懸停效果** */
.card {
  transition: all 0.3s ease;
  border-radius: 12px;
}

.card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1) !important;
}

.card-header {
  background-color: #f8f9fa !important;
  border-bottom: 1px solid #e9ecef;
  border-radius: 12px 12px 0 0 !important;
  padding: 1rem 1.25rem;
}

.card-body {
  padding: 1.5rem 1.25rem;
}

/* **重點：徽章樣式優化** */
.badge {
  font-size: 0.8rem;
  font-weight: 500;
  letter-spacing: 0.5px;
}

/* **重點：圖片樣式優化** */
.rounded-circle {
  border: 4px solid #f8f9fa !important;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15) !important;
  transition: all 0.3s ease;
}

.rounded-circle:hover {
  transform: scale(1.05);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.2) !important;
}

/* **重點：文字樣式優化** */
.text-dark {
  color: #2d3748 !important;
}

.text-muted {
  color: #6c757d !important;
  font-size: 0.875rem;
}

/* **重點：間距調整** */
.row.g-4 > * {
  padding-bottom: 1rem;
}

/* **重點：狀態徽章位置調整** */
.position-absolute.badge {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  border: 2px solid white;
}

/* **重點：停權原因區域的彈性佈局** */
.flex-grow-1 {
  flex: 1;
  min-width: 0; /* 允許文字換行 */
}

/* **重點：響應式調整** */
@media (max-width: 768px) {
  .modal-dialog {
    margin: 0.75rem;
  }
  
  .modal-body {
    padding: 1.5rem 1rem;
  }
  
  .col-md-4,
  .col-md-8 {
    margin-bottom: 1.5rem;
  }

  .icon-wrapper {
    width: 24px;
    height: 24px;
  }

  .rounded-circle {
    width: 100px !important;
    height: 100px !important;
  }

  /* **重點：小螢幕上停權原因警告框的調整** */
  .alert-warning {
    font-size: 0.8rem !important;
    padding: 0.75rem !important;
  }
}

/* **重點：小尺寸螢幕下的卡片調整** */
@media (max-width: 576px) {
  .card-body {
    padding: 1rem;
  }
  
  .row.g-4 {
    --bs-gutter-y: 1.5rem;
  }

  /* **重點：極小螢幕的停權原因文字調整** */
  .col-sm-6:last-child {
    grid-column: 1 / -1; /* 停權原因佔滿整行 */
  }
}
</style>
