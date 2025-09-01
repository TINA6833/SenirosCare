<template>
  <div class="dashboard-main-body">

    <!-- 統計卡片 -->
    <div class="row mb-24 gy-4">
      <div class="col-xl-3 col-sm-6">
        <div class="card shadow-none border bg-gradient-start-1 h-100">
          <div class="card-body p-20">
            <div class="d-flex flex-wrap align-items-center justify-content-between gap-3">
              <div>
                <p class="fw-medium text-primary-light mb-1">總訂單數</p>
                <h6 class="mb-0">{{ stats.total }}</h6>
              </div>
              <div class="w-50-px h-50-px bg-cyan rounded-circle d-flex justify-content-center align-items-center">
                <iconify-icon icon="gridicons:multiple-users" class="text-white text-2xl mb-0"></iconify-icon>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <div class="col-xl-3 col-sm-6">
        <div class="card shadow-none border bg-gradient-start-2 h-100">
          <div class="card-body p-20">
            <div class="d-flex flex-wrap align-items-center justify-content-between gap-3">
              <div>
                <p class="fw-medium text-primary-light mb-1">待審核</p>
                <h6 class="mb-0">{{ stats.pending }}</h6>
              </div>
              <div class="w-50-px h-50-px bg-warning rounded-circle d-flex justify-content-center align-items-center">
                <iconify-icon icon="material-symbols:pending-actions" class="text-white text-2xl mb-0"></iconify-icon>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <div class="col-xl-3 col-sm-6">
        <div class="card shadow-none border bg-gradient-start-3 h-100">
          <div class="card-body p-20">
            <div class="d-flex flex-wrap align-items-center justify-content-between gap-3">
              <div>
                <p class="fw-medium text-primary-light mb-1">已確認</p>
                <h6 class="mb-0">{{ stats.approved }}</h6>
              </div>
              <div class="w-50-px h-50-px bg-success rounded-circle d-flex justify-content-center align-items-center">
                <iconify-icon icon="material-symbols:check-circle" class="text-white text-2xl mb-0"></iconify-icon>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <div class="col-xl-3 col-sm-6">
        <div class="card shadow-none border bg-gradient-start-4 h-100">
          <div class="card-body p-20">
            <div class="d-flex flex-wrap align-items-center justify-content-between gap-3">
              <div>
                <p class="fw-medium text-primary-light mb-1">已完成</p>
                <h6 class="mb-0">{{ stats.completed }}</h6>
              </div>
              <div class="w-50-px h-50-px bg-info rounded-circle d-flex justify-content-center align-items-center">
                <iconify-icon icon="material-symbols:task-alt" class="text-white text-2xl mb-0"></iconify-icon>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 新增虛擬訂單表單 -->
    <CreateAppointmentForm
      v-if="showCreateForm"
      :caregivers="caregivers"
      @submit="handleCreateAppointment"
      @cancel="hideCreateForm"
      @check-conflict="handleCheckConflict"
      class="mb-4"
    />

    <!-- 預約訂單表格元件 -->
    <AppointmentTable
      :appointments="appointments"
      :loading="loading"
      :error="error"
      :total-pages="pagination.totalPages"
      :total-appointments="pagination.total"
      :current-page="pagination.currentPage"
      @view="viewAppointment"
      @review="reviewAppointment"
      @complete="completeAppointment"
      @edit="editAppointment"
      @cancel="cancelAppointment"
      @create="createAppointment"
      @retry="retryLoad"
      @page-changed="handlePageChange"
      @entries-changed="handleEntriesChange"
      @search="handleSearch"
      @status-filter="handleStatusFilter"
    />

    <!-- 審核訂單 Modal -->
    <div class="modal fade" id="reviewModal" tabindex="-1" aria-hidden="true">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">審核預約訂單</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <div v-if="selectedAppointment">
              <div class="mb-3">
                <strong>訂單編號：</strong># {{ selectedAppointment.id }}
              </div>
              <div class="mb-3">
                <strong>會員：</strong>{{ selectedAppointment.memberName }}
              </div>
              <div class="mb-3">
                <strong>照服員：</strong>{{ selectedAppointment.caregiverName }}
              </div>
              <div class="mb-3">
                <strong>服務時間：</strong>{{ selectedAppointment.startTimeDisplay }} ~ {{ selectedAppointment.endTimeDisplay }}
              </div>
              <div class="mb-3">
                <label class="form-label">審核備註</label>
                <textarea 
                  v-model="reviewNotes"
                  class="form-control" 
                  rows="3" 
                  placeholder="請輸入審核備註..."
                ></textarea>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">取消</button>
            <button 
              type="button" 
              class="btn btn-danger me-2"
              @click="handleReviewAction('reject')"
              :disabled="reviewLoading"
            >
              <span v-if="reviewLoading" class="spinner-border spinner-border-sm me-2"></span>
              拒絕
            </button>
            <button 
              type="button" 
              class="btn btn-success"
              @click="handleReviewAction('approve')"
              :disabled="reviewLoading"
            >
              <span v-if="reviewLoading" class="spinner-border spinner-border-sm me-2"></span>
              批准
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 取消訂單 Modal -->
    <div class="modal fade" id="cancelModal" tabindex="-1" aria-hidden="true">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">取消預約訂單</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <div v-if="selectedAppointment">
              <div class="mb-3">
                <strong>訂單編號：</strong># {{ selectedAppointment.id }}
              </div>
              <div class="mb-3">
                <strong>會員：</strong>{{ selectedAppointment.memberName }}
              </div>
              <div class="mb-3">
                <strong>照服員：</strong>{{ selectedAppointment.caregiverName }}
              </div>
              <div class="mb-3">
                <label class="form-label required">取消原因</label>
                <textarea 
                  v-model="cancelReason"
                  class="form-control" 
                  rows="3" 
                  placeholder="請輸入取消原因..."
                  :class="{ 'is-invalid': !cancelReason.trim() && cancelLoading }"
                ></textarea>
                <div class="invalid-feedback">
                  請輸入取消原因
                </div>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">返回</button>
            <button 
              type="button" 
              class="btn btn-danger"
              @click="handleCancelAction"
              :disabled="cancelLoading || !cancelReason.trim()"
            >
              <span v-if="cancelLoading" class="spinner-border spinner-border-sm me-2"></span>
              確認取消
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 完成訂單 Modal -->
    <div class="modal fade" id="completeModal" tabindex="-1" aria-hidden="true">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">標記訂單為已完成</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <div v-if="selectedAppointment">
              <p>
                確定要將訂單 <strong>#{{ selectedAppointment.id }}</strong> 標記為 <span class="text-success">已完成</span> 嗎？
              </p>
              <ul>
                <li><strong>會員：</strong>{{ selectedAppointment.memberName }}</li>
                <li><strong>照服員：</strong>{{ selectedAppointment.caregiverName }}</li>
                <li><strong>服務時間：</strong>{{ selectedAppointment.startTimeDisplay }} ~ {{ selectedAppointment.endTimeDisplay }}</li>
              </ul>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">取消</button>
            <button 
              type="button" 
              class="btn btn-success"
              @click="handleCompleteAction"
              :disabled="completeLoading"
            >
              <span v-if="completeLoading" class="spinner-border spinner-border-sm me-2"></span>
              確認完成
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import Breadcrumb from '@/components/breadcrumb/Breadcrumb.vue';
import AppointmentTable from '@/components/Caregiver/AppointmentTable.vue';
import CreateAppointmentForm from '@/components/Caregiver/CreateAppointmentForm.vue';
import { useAppointments } from '@/composables/useCaregiverAppointment.js'; 
import { useToast } from '@/composables/useToast.js';
import { appointmentApi } from '@/api/caregiverAppointmentApi.js';        
import { scheduleApi } from '@/api/caregiverScheduleApi.js';
import { caregiverAppointmentService } from '@/services/caregiverAppointmentService.js';

export default {
  name: "AppointmentList",
  components: {
    Breadcrumb,
    AppointmentTable,
    CreateAppointmentForm
  },
  setup() {
    // 使用路由
    const router = useRouter();

    // 使用預約訂單 composable
    const {
      appointments,
      loading,
      error,
      pagination,
      filters,
      stats,
      loadAppointments,
      searchAppointments,
      changePage,
      reviewAppointment: apiReviewAppointment,
      completeAppointment: apiCompleteAppointment,
      cancelAppointment: apiCancelAppointment,
      refreshCaregiverCache,
      testCaregiverApi
    } = useAppointments({ autoLoad: true });

    // 使用 Toast 通知
    const { showToast } = useToast();

    // Modal 相關狀態
    const selectedAppointment = ref(null);
    const reviewNotes = ref('');
    const cancelReason = ref('');
    const reviewLoading = ref(false);
    const cancelLoading = ref(false);
    const completeLoading = ref(false);

    // 新增虛擬訂單相關狀態
    const showCreateForm = ref(false);
    const caregivers = ref([]);

    /**
     * 查看預約訂單詳情
     * @param {Object} appointment 預約訂單資料
     */
    const viewAppointment = (appointment) => {
      // // 顯示跳轉通知
      // showToast({
      //   title: '檢視詳情',
      //   message: `正在跳轉到訂單 #${appointment.id} 詳細頁面`,
      //   type: 'info'
      // });
      // 導航到預約詳情頁面
      router.push(`/caregiver/appointments/${appointment.id}`);
    };

    /**
     * 顯示審核訂單 Modal
     * @param {Object} appointment 預約訂單資料
     */
    const reviewAppointment = (appointment) => {
      console.log('審核預約訂單:', appointment);
      selectedAppointment.value = appointment;
      reviewNotes.value = '';
      
      // // 顯示審核通知
      // showToast({
      //   title: '開始審核',
      //   message: `正在審核訂單 #${appointment.id}`,
      //   type: 'info'
      // });
      
      // 顯示 Modal
      const modal = new window.bootstrap.Modal(document.getElementById('reviewModal'));
      modal.show();
    };

    /**
     * 處理審核動作
     * @param {string} action 審核動作 ('approve' | 'reject')
     */
    const handleReviewAction = async (action) => {
      if (!selectedAppointment.value) return;

      reviewLoading.value = true;
      try {
        await apiReviewAppointment(
          selectedAppointment.value.id, 
          action, 
          reviewNotes.value
        );
        
        // 隱藏 Modal
        const modal = window.bootstrap.Modal.getInstance(document.getElementById('reviewModal'));
        modal.hide();
        
        // 顯示成功訊息
        const message = action === 'approve' ? '訂單已批准' : '訂單已拒絕';
        showToast({
          title: '審核完成',
          message: `${message} - 訂單 #${selectedAppointment.value.id}`,
          type: 'success'
        });
        
      } catch (error) {
        console.error('審核訂單失敗:', error);
        showToast({
          title: '審核失敗',
          message: error.message || '審核訂單失敗，請稍後再試',
          type: 'error'
        });
      } finally {
        reviewLoading.value = false;
      }
    };

    /**
     * 完成預約訂單
     * @param {Object} appointment 預約訂單資料
     */
    const completeAppointment = (appointment) => {
      console.log('完成預約訂單:', appointment);
      selectedAppointment.value = appointment;
      
      // // 顯示確認通知
      // showToast({
      //   title: '準備完成',
      //   message: `即將完成訂單 #${appointment.id}`,
      //   type: 'info'
      // });
      
      // 顯示 Modal
      const modal = new window.bootstrap.Modal(document.getElementById('completeModal'));
      modal.show();
    };

    /**
     * 處理標記完成動作
     */
    const handleCompleteAction = async () => {
      if (!selectedAppointment.value) return;

      completeLoading.value = true;
      try {
        await apiCompleteAppointment(selectedAppointment.value.id);
        
        // 隱藏 Modal
        const modal = window.bootstrap.Modal.getInstance(document.getElementById('completeModal'));
        modal.hide();
        
        // 顯示成功訊息
        showToast({
          title: '完成訂單',
          message: `訂單 #${selectedAppointment.value.id} 已標記為完成`,
          type: 'success'
        });
        
        // 重置選中的訂單
        selectedAppointment.value = null;
        
      } catch (error) {
        console.error('完成訂單失敗:', error);
        showToast({
          title: '完成失敗',
          message: error.message || '完成訂單失敗，請稍後再試',
          type: 'error'
        });
      } finally {
        completeLoading.value = false;
      }
    };

    /**
     * 編輯預約訂單
     * @param {Object} appointment 預約訂單資料
     */
    const editAppointment = (appointment) => {
      console.log('編輯預約訂單:', appointment);
      // 顯示跳轉通知
      // showToast({
      //   title: '編輯訂單',
      //   message: `正在跳轉到訂單 #${appointment.id} 編輯頁面`,
      //   type: 'info'
      // });
      // 導航到編輯頁面
      router.push(`/caregiver/appointments/${appointment.id}/edit`);
    };

    /**
     * 顯示取消訂單 Modal
     * @param {Object} appointment 預約訂單資料
     */
    const cancelAppointment = (appointment) => {
      console.log('取消預約訂單:', appointment);
      selectedAppointment.value = appointment;
      cancelReason.value = '';
      
      // // 顯示取消通知
      // showToast({
      //   title: '取消訂單',
      //   message: `正在取消訂單 #${appointment.id}`,
      //   type: 'warning'
      // });
      
      // 顯示 Modal
      const modal = new window.bootstrap.Modal(document.getElementById('cancelModal'));
      modal.show();
    };

    /**
     * 處理取消訂單動作
     */
    const handleCancelAction = async () => {
      if (!selectedAppointment.value) return;

      // 驗證取消原因
      if (!cancelReason.value.trim()) {
        showToast({
          title: '輸入錯誤',
          message: '請輸入取消原因',
          type: 'warning'
        });
        return;
      }

      cancelLoading.value = true;
      try {
        await apiCancelAppointment(
          selectedAppointment.value.id, 
          cancelReason.value.trim()
        );
        
        // 隱藏 Modal
        const modal = window.bootstrap.Modal.getInstance(document.getElementById('cancelModal'));
        modal.hide();
        
        // 顯示成功訊息
        showToast({
          title: '取消完成',
          message: `訂單 #${selectedAppointment.value.id} 已成功取消`,
          type: 'success'
        });
        
        // 重置選中的訂單和取消原因
        selectedAppointment.value = null;
        cancelReason.value = '';
        
      } catch (error) {
        console.error('取消訂單失敗:', error);
        showToast({
          title: '取消失敗',
          message: error.message || '取消訂單失敗，請稍後再試',
          type: 'error'
        });
      } finally {
        cancelLoading.value = false;
      }
    };

    /**
     * 建立新預約訂單
     */
    const createAppointment = async () => {
      console.log('顯示建立新預約訂單表單');
      
      // // 載入照服員列表 (如果還沒載入)
      // if (caregivers.value.length === 0) {
      //   showToast({
      //     title: '載入中',
      //     message: '正在載入照服員列表...',
      //     type: 'info'
      //   });
      //   await loadCaregivers();
      // }
      
      // showToast({
      //   title: '建立訂單',
      //   message: '開始建立新的虛擬訂單',
      //   type: 'info'
      // });
      
      showCreateForm.value = true;
    };

    /**
     * 隱藏建立表單
     */
    const hideCreateForm = () => {
      showCreateForm.value = false;
    };

    /**
 * 載入照服員列表 
 */
/**
 * 載入照服員列表 - 改善版本
 */
const loadCaregivers = async () => {
  try {
    console.log('🔄 載入照服員列表');
    
    // 📡 呼叫 API 取得照服員列表
    const response = await scheduleApi.getAllCaregivers();
    
    console.log('📡 原始 API 回應:', response);
    console.log('📡 回應資料結構:', response.data);
    
    if (response.status === 200) {
      // 🔍 檢查不同可能的資料結構
      let caregiverData = [];
      
      if (response.data.success && response.data.data) {
        // 格式 1: { success: true, data: [...] }
        caregiverData = response.data.data;
      } else if (Array.isArray(response.data)) {
        // 格式 2: 直接是陣列 [...]
        caregiverData = response.data;
      } else if (response.data.caregivers) {
        // 格式 3: { caregivers: [...] }
        caregiverData = response.data.caregivers;
      } else {
        console.warn('⚠️ 未知的回應格式:', response.data);
        caregiverData = [];
      }
      
      console.log('🔍 處理前的照服員資料:', caregiverData);
      
      // 🔧 資料轉換和驗證
      caregivers.value = caregiverData
        .filter(caregiver => caregiver && typeof caregiver === 'object') // 過濾無效資料
        .map(caregiver => {
          // 🔧 處理不同的 ID 欄位名稱
          const id = caregiver.id || caregiver.caregiverId || caregiver.caregiver_id;
          
          console.log('🔧 照服員資料轉換:', {
            原始資料: caregiver,
            提取的ID: id,
            ID類型: typeof id
          });
          
          return {
            id: id,  // 保持原始類型，不強制轉換
            chineseName: caregiver.chineseName || caregiver.chinese_name || caregiver.name,
            serviceArea: caregiver.serviceArea || caregiver.service_area || '未指定',
            isActive: caregiver.isActive !== undefined ? caregiver.isActive : true
          };
        })
        .filter(caregiver => {
          // 🔧 過濾無效資料
          const isValid = caregiver.id && 
                          caregiver.chineseName && 
                          caregiver.isActive;
          
          if (!isValid) {
            console.warn('⚠️ 過濾掉無效照服員:', caregiver);
          }
          
          return isValid;
        });
      
      console.log('✅ 照服員列表載入成功:', caregivers.value);
      console.log('📊 載入照服員數量:', caregivers.value.length);
      
    } else {
      throw new Error(`API 回應錯誤: ${response.status}`);
    }
    
  } catch (error) {
    console.error('❌ 載入照服員列表失敗:', error);
    showToast({
      title: '載入失敗',
      message: '載入照服員列表失敗，請稍後再試',
      type: 'error'
    });
    caregivers.value = []; // 設置空陣列避免錯誤
  }
};

    /**
     * 處理建立虛擬訂單
     */
    
/**
 * 處理建立虛擬訂單
 * @param {Object} appointmentData - 預約資料
 */
const handleCreateAppointment = async (appointmentData) => {
  console.log('🔄 AppointmentList - 建立虛擬訂單:', appointmentData);
  
  // ✅ 數據驗證
  if (!appointmentData.caregiverId) {
    throw new Error('照服員 ID 不能為空');
  }
  
  if (!appointmentData.scheduledAt || !appointmentData.endTime) {
    throw new Error('開始時間和結束時間不能為空');
  }
  
  if (!appointmentData.blockType) {
    throw new Error('鎖定類型不能為空');
  }
  
  // 📡 呼叫後端 API 建立員工預約
  console.log('📡 呼叫 createEmployeeAppointment API');
  const response = await appointmentApi.createEmployeeAppointment(appointmentData);
  
  console.log('📡 API 回應:', response);
  
  // ✅ 檢查回應狀態
  if (response.status === 201 || response.status === 200) {
    console.log('✅ 虛擬訂單建立成功:', response.data);
    
    // 🔄 重新載入預約列表
    console.log('🔄 重新載入預約列表');
    await loadAppointments(true);
    
    // 🔒 隱藏表單
    showCreateForm.value = false;
    
    return response.data;
  } else {
    throw new Error(`伺服器回應錯誤: ${response.status}`);
  }
};

 /**
 * 處理時間衝突檢查
 * @param {Object} conflictData - 衝突檢查資料
 * @param {Function} resolve - 成功回調
 */
const handleCheckConflict = async (conflictData, resolve) => {
  try {
    console.log('🔍 AppointmentList - 檢查時間衝突:', conflictData);
    
    // 📡 呼叫時間衝突檢查 API
    const response = await scheduleApi.checkTimeConflict(
      conflictData.caregiverId,
      conflictData.startTime,
      conflictData.endTime
    );
    
    console.log('📡 衝突檢查 API 回應:', response);
    
    if (response.status === 200) {
      const result = {
        hasConflict: response.data.hasConflict || false,
        message: response.data.message || (response.data.hasConflict ? 
          '該時段已有其他預約，存在時間衝突' : 
          '該時段可以預約，無時間衝突'
        ),
        conflicts: response.data.conflicts || []
      };
      
      console.log('✅ 衝突檢查結果:', result);
      
      // ✅ 呼叫成功回調，傳遞結果給子元件
      if (resolve) {
        resolve(result);
      }
      
    } else {
      throw new Error(`檢查時間衝突失敗: ${response.status}`);
    }
    
  } catch (error) {
    console.error('❌ 檢查時間衝突失敗:', error);
    
    // ❌ 回傳預設結果，讓子元件處理錯誤通知
    if (resolve) {
      resolve({
        hasConflict: false,
        message: '無法檢查時間衝突，但可以繼續建立預約',
        conflicts: [],
        error: error.response?.data?.message || error.message || '檢查時間衝突失敗'
      });
    }
  }
};

    /**
     * 重試載入資料
     */
    const retryLoad = async () => {
      console.log('重試載入預約訂單資料');
      // showToast({
      //   title: '重新載入',
      //   message: '正在重新載入預約訂單資料...',
      //   type: 'info'
      // });
      try {
        await loadAppointments(true);
        // showToast({
        //   title: '載入成功',
        //   message: '預約訂單資料已重新載入',
        //   type: 'success'
        // });
      } catch (error) {
        showToast({
          title: '載入失敗',
          message: '重新載入失敗，請檢查網路連線',
          type: 'error'
        });
      }
    };

    /**
     * 處理換頁
     * @param {number} page 頁碼 (從 0 開始)
     */
    const handlePageChange = async (page) => {
      console.log('換頁:', page);
      await changePage(page);
    };

    /**
     * 處理每頁顯示筆數變更
     * @param {number} size 每頁筆數
     */
    const handleEntriesChange = async (size) => {
      console.log('變更每頁顯示筆數:', size);
      pagination.size = parseInt(size);
      pagination.currentPage = 0; // 重置到第一頁
      await loadAppointments(true);
    };

    /**
     * 處理搜尋
     * @param {string} query 搜尋關鍵字
     */
    const handleSearch = async (query) => {
      console.log('搜尋:', query);
      filters.searchQuery = query;
      
      if (query.trim()) {
        // showToast({
        //   title: '搜尋中',
        //   message: `正在搜尋關鍵字: "${query}"`,
        //   type: 'info'
        // });
      }
      
      try {
        await searchAppointments();
        if (query.trim()) {
          showToast({
            title: '搜尋完成',
            message: `搜尋結果: 找到 ${appointments.value.length} 筆預約訂單`,
            type: 'success'
          });
        }
      } catch (error) {
        showToast({
          title: '搜尋失敗',
          message: '搜尋預約訂單失敗，請稍後再試',
          type: 'error'
        });
      }
    };

    /**
     * 處理狀態篩選
     * @param {string} status 狀態
     */
    const handleStatusFilter = async (status) => {
      console.log('狀態篩選:', status);
      filters.status = status;
      
      const statusMap = {
        'pending': '待審核',
        'approved': '已確認', 
        'completed': '已完成',
        'cancelled': '已取消',
        '': '全部'
      };
      
      // showToast({
      //   title: '篩選中',
      //   message: `正在篩選狀態: ${statusMap[status] || status}`,
      //   type: 'info'
      // });
      
      try {
        await searchAppointments();
        showToast({
          title: '篩選完成',
          message: `篩選結果: 找到 ${appointments.value.length} 筆 ${statusMap[status] || status} 訂單`,
          type: 'success'
        });
      } catch (error) {
        showToast({
          title: '篩選失敗',
          message: '篩選預約訂單失敗，請稍後再試',
          type: 'error'
        });
      }
    };

    // 在元件卸載時清理 Modal
    const cleanupModals = () => {
      const modals = ['reviewModal', 'cancelModal', 'completeModal'];
      modals.forEach(modalId => {
        const modalEl = document.getElementById(modalId);
        if (modalEl) {
          const modalInstance = window.bootstrap.Modal.getInstance(modalEl);
          if (modalInstance) {
            modalInstance.dispose();
          }
        }
      });
    };

    // 元件掛載完成後的處理
    onMounted(() => {
      console.log('預約訂單列表頁面已掛載');
    });

    // 元件卸載時清理
    window.addEventListener('beforeunload', cleanupModals);

    return {
      // 狀態
      appointments,
      loading,
      error,
      pagination,
      filters,
      stats,
      selectedAppointment,
      reviewNotes,
      cancelReason,
      reviewLoading,
      cancelLoading,
      completeLoading,
      showCreateForm,
      caregivers,

      // 方法
      viewAppointment,
      reviewAppointment,
      handleReviewAction,
      completeAppointment,
      handleCompleteAction,
      editAppointment,
      cancelAppointment,
      handleCancelAction,
      createAppointment,
      hideCreateForm,
      handleCreateAppointment,
      handleCheckConflict,
      retryLoad,
      handlePageChange,
      handleEntriesChange,
      handleSearch,
      handleStatusFilter
    };
  }
}
</script>

<style scoped>
.dashboard-main-body {
  padding: 1.5rem;
}

.card {
  box-shadow: 0 0.125rem 0.25rem rgba(0, 0, 0, 0.075);
  border: 1px solid rgba(0, 0, 0, 0.125);
  border-radius: 0.375rem;
}

.bg-gradient-start-1 {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.bg-gradient-start-2 {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.bg-gradient-start-3 {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.bg-gradient-start-4 {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.modal-content {
  border-radius: 0.5rem;
  border: none;
  box-shadow: 0 1rem 3rem rgba(0, 0, 0, 0.175);
}

.modal-header {
  background-color: var(--bs-light);
  border-bottom: 1px solid var(--bs-border-color);
  border-radius: 0.5rem 0.5rem 0 0;
}

.modal-footer {
  border-top: 1px solid var(--bs-border-color);
  background-color: var(--bs-light);
  border-radius: 0 0 0.5rem 0.5rem;
}

.spinner-border-sm {
  width: 1rem;
  height: 1rem;
}

.required {
  position: relative;
}

.required::after {
  content: ' *';
  color: var(--bs-danger);
  font-weight: bold;
}

.is-invalid {
  border-color: var(--bs-danger);
}

.invalid-feedback {
  display: block;
  font-size: 0.875rem;
}

/* 響應式設計 */
@media (max-width: 768px) {
  .dashboard-main-body {
    padding: 1rem;
  }
  
  .row.gy-4 > .col-xl-3 {
    margin-bottom: 1rem;
  }
}
</style>