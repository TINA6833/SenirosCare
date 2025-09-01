<template>
  <div class="dashboard-main-body">

    <!-- 班表元件 -->
    <ScheduleTable
      :schedule="schedule"
      :schedule-by-date="scheduleByDate"
      :caregivers="caregivers"
      :loading="loading"
      :error="error"
      :filters="filters"
      :date-range="dateRange"
      @caregiver-changed="handleCaregiverChange"
      @view-type-changed="handleViewTypeChange"
      @period-changed="handlePeriodChange"
      @filters-changed="handleFiltersChange"
      @view-appointment="viewAppointment"
      @retry="retryLoad"
    />

    <!-- 預約詳情 Modal -->
    <div class="modal fade" id="appointmentModal" tabindex="-1" aria-hidden="true">
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">預約詳情</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <div v-if="selectedAppointment" class="row">
              <!-- 左側：基本資訊 -->
              <div class="col-md-6">
                <div class="card h-100">
                  <div class="card-header">
                    <h6 class="card-title mb-0">基本資訊</h6>
                  </div>
                  <div class="card-body">
                    <div class="mb-3">
                      <strong class="text-muted">預約編號：</strong>
                      <span class="fw-medium"># {{ selectedAppointment.id }}</span>
                    </div>
                    <div class="mb-3">
                      <strong class="text-muted">預約類型：</strong>
                      <span :class="`badge bg-${selectedAppointment.typeColor}-focus text-${selectedAppointment.typeColor}-main ms-2`">
                        {{ selectedAppointment.typeDisplay }}
                      </span>
                    </div>
                    <div class="mb-3">
                      <strong class="text-muted">預約狀態：</strong>
                      <span :class="`badge bg-${selectedAppointment.statusColor}-focus text-${selectedAppointment.statusColor}-main ms-2`">
                        {{ selectedAppointment.statusDisplay }}
                      </span>
                    </div>
                    <div class="mb-3">
                      <strong class="text-muted">服務時間：</strong>
                      <div class="mt-1">
                        <div class="text-primary fw-medium">{{ selectedAppointment.startTimeDisplay }}</div>
                        <div class="text-muted">至</div>
                        <div class="text-primary fw-medium">{{ selectedAppointment.endTimeDisplay }}</div>
                      </div>
                      <div class="mt-2 text-secondary-light">
                        <iconify-icon icon="ph:clock" class="me-1"></iconify-icon>
                        服務時長：{{ selectedAppointment.duration }} 小時
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 右側：詳細資訊 -->
              <div class="col-md-6">
                <div class="card h-100">
                  <div class="card-header">
                    <h6 class="card-title mb-0">詳細資訊</h6>
                  </div>
                  <div class="card-body">
                    <!-- 客戶預約資訊 -->
                    <div v-if="!selectedAppointment.isBlocked">
                      <div class="mb-3">
                        <strong class="text-muted">客戶姓名：</strong>
                        <span v-if="loadingMemberData" class="fw-medium text-muted">
                          <iconify-icon icon="ph:spinner" class="me-1 spinning"></iconify-icon>
                          載入中...
                        </span>
                        <span v-else class="fw-medium">{{ selectedAppointment.memberName }}</span>
                      </div>
                      <div class="mb-3">
                        <strong class="text-muted">服務地點：</strong>
                        <div class="mt-1">{{ selectedAppointment.serviceLocation }}</div>
                      </div>
                      <div class="mb-3">
                        <strong class="text-muted">費用：</strong>
                        <span class="text-success fw-bold">NT$ {{ selectedAppointment.totalAmount?.toLocaleString() || 0 }}</span>
                      </div>
                    </div>

                    <!-- 時間鎖定資訊 -->
                    <div v-else>
                      <div class="mb-3">
                        <strong class="text-muted">鎖定原因：</strong>
                        <div class="mt-1">
                          <span class="badge bg-secondary-focus text-secondary-main">
                            {{ getBlockTypeDisplay(selectedAppointment.blockType) }}
                          </span>
                        </div>
                      </div>
                      <div class="text-muted">
                        <iconify-icon icon="ph:info" class="me-2"></iconify-icon>
                        此時段為系統鎖定時間，不接受客戶預約
                      </div>
                    </div>

                    <!-- 備註 -->
                    <div v-if="selectedAppointment.notes" class="mt-4">
                      <strong class="text-muted">備註：</strong>
                      <div class="mt-1 p-2 bg-light border rounded">
                        {{ selectedAppointment.notes }}
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">關閉</button>
            <button 
              v-if="selectedAppointment && !selectedAppointment.isBlocked"
              type="button" 
              class="btn btn-primary"
              @click="goToAppointmentDetail"
            >
              查看完整預約
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import Breadcrumb from '@/components/breadcrumb/Breadcrumb.vue';
import ScheduleTable from '@/components/Caregiver/ScheduleTable.vue';
import { useSchedule } from '@/composables/useCaregiverSchedule.js'; 
import { useToast } from '@/composables/useToast.js';
import { memberApi } from '@/api/memberApi.js';

export default {
  name: "ScheduleList",
  components: {
    Breadcrumb,
    ScheduleTable
  },
  setup() {
    // 使用路由
    const router = useRouter();

    // 使用班表 composable
    const {
      schedule,
      scheduleByDate,
      caregivers,
      currentCaregiver,
      loading,
      error,
      statistics,
      dateRange,
      filters,
      stats,
      loadSchedule,
      loadWeekSchedule,
      loadMonthSchedule,
      loadDaySchedule,
      loadCaregivers,
      setCurrentCaregiver,
      changeViewType,
      clearAllCache
    } = useSchedule({ autoLoad: true });

    // 使用 Toast 通知
    const { showToast } = useToast();

    // Modal 相關狀態
    const selectedAppointment = ref(null);
    const loadingMemberData = ref(false);

    // 會員資料快取
    const memberCache = new Map();
    const loadingMembers = new Set();

    /**
     * 載入會員資料
     * @param {number} memberId - 會員 ID
     */
    const loadMemberData = async (memberId) => {
      if (!memberId || memberCache.has(memberId) || loadingMembers.has(memberId)) {
        return memberCache.get(memberId);
      }
      
      try {
        loadingMembers.add(memberId);
        const response = await memberApi.getMemberById(memberId);
        if (response.data && response.data.memberName) {
          memberCache.set(memberId, response.data);
          return response.data;
        }
      } catch (error) {
        // 載入會員資料失敗時保持原狀態
      } finally {
        loadingMembers.delete(memberId);
      }
      return null;
    };

    /**
     * 批次載入班表中的會員資料
     * @param {Array} scheduleData - 班表資料
     */
    const loadMemberDataForSchedule = async (scheduleData) => {
      if (!scheduleData || scheduleData.length === 0) return;
      
      // 收集所有需要載入的會員 ID
      const memberIds = new Set();
      scheduleData.forEach(appointment => {
        if (appointment.memberId && 
            (appointment.memberName === '系統建立' || 
             appointment.memberName === '系統預約' ||
             appointment.memberName?.includes('會員預約 (ID:') ||
             !appointment.memberName ||
             appointment.memberName.trim() === '')) {
          memberIds.add(appointment.memberId);
        }
      });
      
      // 批次載入會員資料
      const memberPromises = Array.from(memberIds).map(memberId => loadMemberData(memberId));
      await Promise.all(memberPromises);
      
      // 更新班表中的會員名稱
      scheduleData.forEach(appointment => {
        if (appointment.memberId && memberCache.has(appointment.memberId)) {
          const memberData = memberCache.get(appointment.memberId);
          if (memberData && memberData.memberName) {
            appointment.memberName = memberData.memberName;
          }
        }
      });
    };

    // 當前期間狀態
    const currentPeriod = reactive({
      caregiverId: null,
      baseDate: new Date()
    });

    /**
     * 處理照服員變更 - 修正版本
     * @param {number} caregiverId 照服員 ID
     */
    const handleCaregiverChange = async (caregiverId) => {
      if (!caregiverId) {
        // 清空當前班表資料
        schedule.value = [];
        scheduleByDate.value = {};
        statistics.value = null;
        currentCaregiver.value = null;
        currentPeriod.caregiverId = null;
        return;
      }

      try {
        // 顯示載入狀態
        loading.value = true;
        error.value = null;
        
        // 設定當前照服員
        await setCurrentCaregiver(caregiverId);
        currentPeriod.caregiverId = caregiverId;
        
        // 根據檢視類型載入對應的班表
        await loadCurrentPeriod();
        
        // showToast({
        //   title: '班表切換成功',
        //   message: `已切換至 ${currentCaregiver.value?.chineseName} 的班表`,
        //   type: 'success'
        // });
        
      } catch (error) {
        showToast({
          title: '切換失敗',
          message: error.message || '切換照服員失敗，請稍後再試',
          type: 'error'
        });
        error.value = error.message || '切換照服員失敗';
      } finally {
        loading.value = false;
      }
    };

    /**
     * 載入當前期間的班表 - 修正版本
     */
    const loadCurrentPeriod = async (forceRefresh = false) => {
      if (!currentPeriod.caregiverId) {
        return;
      }

      try {
        // 確保有有效的日期
        if (!currentPeriod.baseDate || isNaN(currentPeriod.baseDate.getTime())) {
          currentPeriod.baseDate = new Date();
        }

        // 根據檢視類型載入對應的班表
        switch (filters.viewType) {
          case 'week':
            await loadWeekSchedule(currentPeriod.caregiverId, currentPeriod.baseDate, forceRefresh);
            break;
          case 'month':
            await loadMonthSchedule(currentPeriod.caregiverId, currentPeriod.baseDate, forceRefresh);
            break;
          case 'day':
            await loadDaySchedule(currentPeriod.caregiverId, currentPeriod.baseDate, forceRefresh);
            break;
          default:
            // 預設載入本週班表
            await loadWeekSchedule(currentPeriod.caregiverId, currentPeriod.baseDate, forceRefresh);
            break;
        }
        
        // 載入班表中的會員資料
        if (schedule.value && schedule.value.length > 0) {
          await loadMemberDataForSchedule(schedule.value);
        }
        
      } catch (error) {
        showToast({
          title: '載入失敗',
          message: '載入班表資料失敗，請檢查網路連線後重試',
          type: 'error'
        });
        throw error;
      }
    };

    /**
     * 處理檢視類型變更 - 修正版本
     * @param {string} viewType 檢視類型
     */
    /**
     * 處理檢視類型變更 - 修正版本
     * @param {string} viewType 檢視類型
     */
    const handleViewTypeChange = async (viewType) => {
      try {
        const previousViewType = filters.viewType;
        
        // 更新檢視類型
        changeViewType(viewType);
        
        // 🔧 智能日期處理：根據不同檢視類型切換調整基準日期
        if (viewType === 'day') {
          // 切換到日檢視時
          if (previousViewType === 'week' && dateRange.startDate && dateRange.endDate) {
            // 從週檢視切換：選擇週範圍內的某一天（優先選擇今天，如果今天不在範圍內則選擇週的第一天）
            const today = new Date();
            const todayStr = today.toLocaleDateString('zh-TW', {
              year: 'numeric',
              month: '2-digit',
              day: '2-digit'
            }).replace(/\//g, '-');
            
            const weekStart = new Date(dateRange.startDate);
            const weekEnd = new Date(dateRange.endDate);
            
            if (today >= weekStart && today <= weekEnd) {
              // 今天在這週範圍內，使用今天
              currentPeriod.baseDate = today;
            } else {
              // 今天不在這週範圍內，使用週的開始日期
              currentPeriod.baseDate = weekStart;
            }
          } else if (previousViewType === 'month' && dateRange.startDate) {
            // 從月檢視切換：使用月份中的今天，如果今天不在該月則使用月初
            const monthStart = new Date(dateRange.startDate);
            const today = new Date();
            
            if (today.getFullYear() === monthStart.getFullYear() && 
                today.getMonth() === monthStart.getMonth()) {
              // 今天在這個月份內
              currentPeriod.baseDate = today;
            } else {
              // 今天不在這個月份內，使用月初
              currentPeriod.baseDate = monthStart;
            }
          }
        }
        
        // 如果當前沒有有效的基準日期，才設定為今天
        if (!currentPeriod.baseDate || isNaN(currentPeriod.baseDate.getTime())) {
          currentPeriod.baseDate = new Date();
        }
        
        // 如果有選擇照服員，重新載入對應檢視的班表
        if (currentPeriod.caregiverId) {
          // 🔧 切換檢視時強制重新載入，確保資料正確
          await loadCurrentPeriod(true);
          
          // const viewTypeText = viewType === 'week' ? '週' : viewType === 'month' ? '月' : '日';
          // showToast({
          //   title: '檢視切換成功',
          //   message: `已切換至${viewTypeText}檢視模式`,
          //   type: 'success'
          // });
        }
      } catch (error) {
        showToast({
          title: '檢視切換失敗',
          message: '切換檢視類型失敗，請稍後再試',
          type: 'error'
        });
      }
    };

    /**
     * 處理期間變更 (上一週/月、下一週/月) - 修正版本
     * @param {string} direction 方向 ('previous' | 'next')
     */
    const handlePeriodChange = async (direction) => {
      if (!currentPeriod.caregiverId) return;
      
      try {
        // 根據檢視類型和方向調整基準日期 - 創建新的 Date 物件確保響應性
        const adjustment = direction === 'previous' ? -1 : 1;
        let newDate = new Date(currentPeriod.baseDate);
        
        if (filters.viewType === 'week') {
          newDate.setDate(newDate.getDate() + (7 * adjustment));
        } else if (filters.viewType === 'month') {
          newDate.setMonth(newDate.getMonth() + adjustment);
        } else if (filters.viewType === 'day') {
          newDate.setDate(newDate.getDate() + adjustment);
        }
        
        // 更新基準日期（重新賦值確保響應性）
        currentPeriod.baseDate = newDate;
        
        // 重新載入班表
        await loadCurrentPeriod(true); // 🔧 期間變更時強制重新載入
        
        // 顯示期間變更成功通知
        const periodText = filters.viewType === 'week' ? '週' : filters.viewType === 'month' ? '月' : '日';
        const directionText = direction === 'previous' ? '上一個' : '下一個';
        // showToast({
        //   title: '期間切換成功',
        //   message: `已切換到${directionText}${periodText}`,
        //   type: 'success'
        // });
        
      } catch (error) {
        showToast({
          title: '期間變更失敗',
          message: '無法切換到指定期間，請檢查網路連線後重試',
          type: 'error'
        });
      }
    };

    /**
     * 處理篩選條件變更
     * @param {string|Object} action 動作或篩選條件
     */
    const handleFiltersChange = (action) => {
      if (action === 'reset') {
        // 重置篩選條件
        filters.showBlockedSlots = true;
        filters.showCustomerAppointments = true;
        filters.statusFilter = '';
        
        showToast({
          title: '篩選重置',
          message: '所有篩選條件已重置為預設值',
          type: 'info'
        });
      } else {
        // 其他篩選變更
        showToast({
          title: '篩選更新',
          message: '班表篩選條件已更新',
          type: 'info'
        });
      }
    };

    /**
     * 檢視預約詳情 - 支援動態取得會員資料
     * @param {Object} appointment 預約資料
     */
    const viewAppointment = async (appointment) => {
      try {
        // 創建預約資料的副本
        const appointmentWithMemberInfo = { ...appointment };
        
        // 先設定預約資料並顯示 Modal
        selectedAppointment.value = appointmentWithMemberInfo;
        
        // 顯示 Modal
        const modalEl = document.getElementById('appointmentModal');
        const modal = new window.bootstrap.Modal(modalEl);
        modal.show();
        
        // 如果是客戶預約且有 memberId，但會員名稱是「系統建立」或包含 "會員預約 (ID:"，則取得真實會員資料
        if (!appointment.isBlocked && 
            appointment.memberId && 
            (appointment.memberName === '系統建立' || 
             !appointment.memberName ||
             appointment.memberName.includes('會員預約 (ID:') ||
             appointment.memberName.includes('系統建立'))) {
          
          loadingMemberData.value = true;
          
          try {
            const memberResponse = await memberApi.getMemberById(appointment.memberId);
            
            // 更新會員名稱
            if (memberResponse.data && memberResponse.data.memberName) {
              appointmentWithMemberInfo.memberName = memberResponse.data.memberName;
              selectedAppointment.value = appointmentWithMemberInfo;
            }
          } catch (memberError) {
            // 如果取得會員資料失敗，保持原本的名稱
            showToast({
              title: '會員資料載入失敗',
              message: '無法載入會員詳細資料，將顯示預設資訊',
              type: 'warning'
            });
          } finally {
            loadingMemberData.value = false;
          }
        }
        
        // 顯示檢視通知
        // showToast({
        //   title: '預約詳情',
        //   message: `正在檢視預約 #${appointment.id} 的詳細資訊`,
        //   type: 'info'
        // });
        
      } catch (error) {
        showToast({
          title: '檢視失敗',
          message: '無法檢視預約詳情，請稍後再試',
          type: 'error'
        });
      }
    };

    /**
     * 跳轉到預約詳細頁面
     */
    const goToAppointmentDetail = () => {
      if (selectedAppointment.value) {
        // 關閉 Modal
        const modal = document.getElementById('appointmentModal');
        if (modal) {
          const modalInstance = window.bootstrap.Modal.getInstance(modal);
          if (modalInstance) {
            modalInstance.hide();
          }
        }
        
        // showToast({
        //   title: '跳轉中',
        //   message: `正在跳轉到訂單 #${selectedAppointment.value.id} 詳細頁面`,
        //   type: 'info'
        // });
        
        // 導航到預約詳情頁面（與預約訂單列表保持一致的路由）
        router.push(`/caregiver/appointments/${selectedAppointment.value.id}`);
      }
    };

    /**
     * 重試載入 - 添加強制重新載入
     */
    const retryLoad = async () => {
      try {
        // 🔧 清除所有快取，確保重新載入
        clearAllCache();
        
        if (currentPeriod.caregiverId) {
          // 🔧 使用強制重新載入
          await loadCurrentPeriod(true);
          showToast({
            title: '重新載入成功',
            message: '班表資料已重新載入',
            type: 'success'
          });
        } else {
          await loadCaregivers();
          showToast({
            title: '載入成功',
            message: '照服員列表已重新載入',
            type: 'success'
          });
        }
      } catch (error) {
        showToast({
          title: '重新載入失敗',
          message: '無法重新載入資料，請檢查網路連線',
          type: 'error'
        });
      }
    };

    /**
     * 獲取鎖定類型顯示文字
     * @param {string} blockType 鎖定類型
     */
    const getBlockTypeDisplay = (blockType) => {
      const blockTypeMap = {
        'break': '休息時間',
        'maintenance': '系統維護',
        'personal': '個人事務',
        'unavailable': '不可用'
      };
      
      return blockTypeMap[blockType] || '未知類型';
    };

    /**
     * 初始化頁面 - 改進版本
     */
    const initializePage = async () => {
      try {
        // 載入照服員列表
        await loadCaregivers();
        
        if (caregivers.value.length > 0) {
          // 🔧 可選：自動選擇第一個照服員並載入其班表
          // 如果不想自動選擇，可以註解掉下面這幾行
          const firstCaregiver = caregivers.value[0];
          
          // 觸發照服員變更，這會自動載入班表
          await handleCaregiverChange(firstCaregiver.id);
          
        } else {
          showToast({
            title: '無照服員資料',
            message: '系統中沒有找到任何照服員資料',
            type: 'warning'
          });
        }
        
      } catch (error) {
        showToast({
          title: '頁面初始化失敗',
          message: '無法載入頁面資料，請重新整理頁面',
          type: 'error'
        });
      }
    };

    // 清理 Modal 資源
    const cleanupModals = () => {
      const modalEl = document.getElementById('appointmentModal');
      if (modalEl) {
        const modalInstance = window.bootstrap.Modal.getInstance(modalEl);
        if (modalInstance) {
          modalInstance.dispose();
        }
      }
    };

    // 元件掛載時的處理
    onMounted(async () => {
      await initializePage();
    });

    // 元件卸載時清理資源
    onUnmounted(() => {
      cleanupModals();
    });

    return {
      // 狀態
      schedule,
      scheduleByDate,
      caregivers,
      currentCaregiver,
      loading,
      error,
      statistics,
      dateRange,
      filters,
      stats,
      selectedAppointment,
      loadingMemberData,

      // 方法
      handleCaregiverChange,
      handleViewTypeChange,
      handlePeriodChange,
      handleFiltersChange,
      viewAppointment,
      goToAppointmentDetail,
      retryLoad,
      getBlockTypeDisplay
    };
  }
}
</script>

<style scoped>
/* Modal 樣式調整 */
.modal-body .card {
  border: none;
  box-shadow: 0 0.125rem 0.25rem rgba(0, 0, 0, 0.075);
}

.modal-body .card-header {
  background-color: #f8f9fa;
  border-bottom: 1px solid #dee2e6;
  padding: 0.75rem 1rem;
}

.modal-body .card-body {
  padding: 1rem;
}

/* 預約詳情樣式 */
.badge {
  font-size: 0.75em;
}

.text-primary.fw-medium {
  color: #0d6efd !important;
  font-weight: 500 !important;
}

.text-success.fw-bold {
  color: #198754 !important;
  font-weight: 700 !important;
}

/* 載入動畫 */
.spinning {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 響應式調整 */
@media (max-width: 768px) {
  .modal-dialog {
    margin: 0.5rem;
  }
  
  .modal-body .row {
    flex-direction: column;
  }
  
  .modal-body .col-md-6 {
    margin-bottom: 1rem;
  }
}
</style>