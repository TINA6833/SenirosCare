<template>
  <div class="dashboard-main-body">
    <!-- 麵包屑導航 -->

    <!-- 載入狀態 -->
    <div v-if="loading" class="text-center py-5">
      <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">載入中...</span>
      </div>
      <p class="mt-3 text-muted">載入預約訂單資料中...</p>
    </div>

    <!-- 錯誤狀態 -->
    <div v-else-if="error" class="alert alert-danger" role="alert">
      <iconify-icon icon="material-symbols:error-outline" class="me-2"></iconify-icon>
      {{ error }}
      <div class="mt-3">
        <button @click="loadAppointmentData" class="btn btn-outline-danger me-2">重新載入</button>
        <router-link to="/caregiver/appointments" class="btn btn-secondary">返回列表</router-link>
      </div>
    </div>

    <!-- 預約訂單詳情 -->
    <div v-else-if="appointmentData" class="card">
      <div class="card-header bg-info text-white">
        <div class="d-flex justify-content-between align-items-center">
          <div>
            <h5 class="card-title mb-0">
              訂單編號 #{{ appointmentData.id }}
            </h5>
            
          </div>
          <!-- 狀態標籤 -->
          <span 
            :class="`badge bg-${appointmentData.statusColor || 'secondary'} fs-6`"
          >
            {{ appointmentData.statusDisplay || '未知狀態' }}
          </span>
        </div>
      </div>
      
      <div class="card-body">
        <!-- 基本資訊區塊 -->
        <div class="row">
          <div class="col-12">
            <h6 class="border-bottom pb-2 mb-3">
              <iconify-icon icon="material-symbols:info-outline" class="me-2"></iconify-icon>
              基本資訊
            </h6>
          </div>

          <!-- 訂單編號 -->
          <div class="col-md-6 mb-3">
            <label class="form-label fw-bold">訂單編號</label>
            <div class="form-control-plaintext bg-light rounded p-2">
              #{{ appointmentData.id }}
            </div>
          </div>

          <!-- 訂單類型 -->
          <div class="col-md-6 mb-3">
            <label class="form-label fw-bold">訂單類型</label>
            <div class="form-control-plaintext bg-light rounded p-2">
              {{ appointmentData.isBlocked ? '虛擬訂單' : '會員預約' }}
            </div>
          </div>

          <!-- 會員/虛擬訂單類型 -->
          <div class="col-md-6 mb-3">
            <label class="form-label fw-bold">
              {{ appointmentData.isBlocked ? '鎖定類型' : '會員' }}
            </label>
            <div class="form-control-plaintext bg-light rounded p-2">
              {{ getMemberDisplayName(appointmentData) }}
            </div>
          </div>

          <!-- 會員 ID (如果是會員預約) -->
          <div v-if="!appointmentData.isBlocked && appointmentData.memberId" class="col-md-6 mb-3">
            <label class="form-label fw-bold">會員 ID</label>
            <div class="form-control-plaintext bg-light rounded p-2">
              {{ appointmentData.memberId }}
            </div>
          </div>

          <!-- 照服員 -->
          <div class="col-md-6 mb-3">
            <label class="form-label fw-bold">指派照服員</label>
            <div class="form-control-plaintext bg-light rounded p-2">
              {{ appointmentData.caregiverName || '未指定' }}
              <span v-if="appointmentData.caregiverId" class="text-muted small">
                (ID: {{ appointmentData.caregiverId }})
              </span>
            </div>
          </div>

          <!-- 訂單狀態 -->
          <div class="col-md-6 mb-3">
            <label class="form-label fw-bold">訂單狀態</label>
            <div class="form-control-plaintext bg-light rounded p-2">
              <span 
                :class="`badge bg-${appointmentData.statusColor || 'secondary'}-focus text-${appointmentData.statusColor || 'secondary'}-main border border-${appointmentData.statusColor || 'secondary'}-main`"
              >
                {{ appointmentData.statusDisplay || '未知狀態' }}
              </span>
            </div>
          </div>
        </div>

        <!-- 時間資訊區塊 -->
        <div class="row mt-4">
          <div class="col-12">
            <h6 class="border-bottom pb-2 mb-3">
              <iconify-icon icon="material-symbols:schedule" class="me-2"></iconify-icon>
              時間資訊
            </h6>
          </div>

          <!-- 開始時間 -->
          <div class="col-md-6 mb-3">
            <label class="form-label fw-bold">開始時間</label>
            <div class="form-control-plaintext bg-light rounded p-2">
              <iconify-icon icon="material-symbols:play-arrow" class="me-1 text-success"></iconify-icon>
              {{ appointmentData.startTimeDisplay || '未設定' }}
            </div>
          </div>

          <!-- 結束時間 -->
          <div class="col-md-6 mb-3">
            <label class="form-label fw-bold">結束時間</label>
            <div class="form-control-plaintext bg-light rounded p-2">
              <iconify-icon icon="material-symbols:stop" class="me-1 text-danger"></iconify-icon>
              {{ appointmentData.endTimeDisplay || '未設定' }}
            </div>
          </div>

          <!-- 總時長 -->
          <div class="col-md-6 mb-3">
            <label class="form-label fw-bold">總時長</label>
            <div class="form-control-plaintext bg-light rounded p-2">
              <iconify-icon icon="material-symbols:timer" class="me-1 text-primary"></iconify-icon>
              {{ appointmentData.durationDisplay || '計算中' }}
            </div>
          </div>

          <!-- 建立時間 -->
          <div class="col-md-6 mb-3">
            <label class="form-label fw-bold">建立時間</label>
            <div class="form-control-plaintext bg-light rounded p-2">
              <iconify-icon icon="material-symbols:event" class="me-1 text-info"></iconify-icon>
              {{ formatDateTime(appointmentData.createdAt) || '未知' }}
            </div>
          </div>
        </div>

        <!-- 服務資訊區塊 (會員預約才顯示) -->
        <div v-if="!appointmentData.isBlocked" class="row mt-4">
          <div class="col-12">
            <h6 class="border-bottom pb-2 mb-3">
              <iconify-icon icon="material-symbols:room-service" class="me-2"></iconify-icon>
              服務資訊
            </h6>
          </div>

          <!-- 服務地點 -->
          <div class="col-md-6 mb-3">
            <label class="form-label fw-bold">服務地點</label>
            <div class="form-control-plaintext bg-light rounded p-2">
              <iconify-icon icon="material-symbols:location-on" class="me-1 text-warning"></iconify-icon>
              {{ appointmentData.serviceLocation || '未設定' }}
            </div>
          </div>

          <!-- 總金額 -->
          <div class="col-md-6 mb-3">
            <label class="form-label fw-bold">總金額</label>
            <div class="form-control-plaintext bg-light rounded p-2">
              <iconify-icon icon="material-symbols:payments" class="me-1 text-success"></iconify-icon>
              ${{ appointmentData.totalAmount || 0 }}
            </div>
          </div>
        </div>

        <!-- 備註說明 -->
        <div v-if="appointmentData.notes" class="row mt-4">
          <div class="col-12">
            <h6 class="border-bottom pb-2 mb-3">
              <iconify-icon icon="material-symbols:note" class="me-2"></iconify-icon>
              備註說明
            </h6>
            <div class="form-control-plaintext bg-light rounded p-3">
              {{ appointmentData.notes }}
            </div>
          </div>
        </div>

        <!-- 操作按鈕 -->
        <div class="d-flex justify-content-between align-items-center mt-4 pt-3 border-top">
          <router-link to="/caregiver/appointments" class="btn btn-secondary">
            <iconify-icon icon="material-symbols:arrow-back" class="me-1"></iconify-icon>
            返回列表
          </router-link>
          
          <!-- 編輯按鈕 (如果可以編輯) -->
          <router-link 
            v-if="appointmentData.availableActions?.includes('edit')"
            :to="`/caregiver/appointments/${appointmentData.id}/edit`" 
            class="btn btn-success"
          >
            <iconify-icon icon="lucide:edit" class="me-1"></iconify-icon>
            編輯訂單
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import Breadcrumb from '@/components/breadcrumb/Breadcrumb.vue';
import { useToast } from '@/composables/useToast';
import { useAppointments } from '@/composables/useCaregiverAppointment';
import { memberApi } from '@/api/memberApi.js';

// 使用 Vue Router
const route = useRoute();
const router = useRouter();

// 使用 Toast 通知
const { showToast } = useToast();

// 使用預約管理 composable
const { loadAppointmentById } = useAppointments({ autoLoad: false });

// 狀態管理
const loading = ref(true);
const error = ref(null);

// 預約訂單資料
const appointmentData = ref(null);

// 會員資料快取
const memberCache = ref(new Map());
const loadingMembers = ref(new Set());

/**
 * 取得顯示的會員名稱
 * @param {Object} appointment - 預約資料
 * @returns {string} 會員名稱
 */
const getMemberDisplayName = (appointment) => {
  // 如果是虛擬訂單，顯示原始名稱
  if (appointment.isBlocked) {
    return appointment.memberName || '未指定會員';
  }
  
  // 如果有會員 ID 且快取中有資料，使用真實姓名
  if (appointment.memberId && memberCache.value.has(appointment.memberId)) {
    const memberData = memberCache.value.get(appointment.memberId);
    return memberData.memberName;
  }
  
  // 如果正在載入中
  if (appointment.memberId && loadingMembers.value.has(appointment.memberId)) {
    return '載入中...';
  }
  
  // 如果有會員 ID 但名稱是系統格式，觸發載入
  if (appointment.memberId && 
      (appointment.memberName === '系統建立' || 
       appointment.memberName?.includes('會員預約 (ID:'))) {
    // 非同步載入會員資料
    loadMemberData(appointment.memberId);
    return '載入中...';
  }
  
  // 其他情況使用原始名稱
  return appointment.memberName || '未指定會員';
};

/**
 * 載入會員資料
 * @param {number} memberId - 會員 ID
 */
const loadMemberData = async (memberId) => {
  if (!memberId || memberCache.value.has(memberId) || loadingMembers.value.has(memberId)) {
    return;
  }
  
  try {
    loadingMembers.value.add(memberId);
    
    const response = await memberApi.getMemberById(memberId);
    if (response.data && response.data.memberName) {
      memberCache.value.set(memberId, response.data);
    }
  } catch (error) {
    console.error('載入會員資料失敗:', error);
  } finally {
    loadingMembers.value.delete(memberId);
  }
};

/**
 * 格式化時間顯示
 * @param {string} dateTimeStr - 日期時間字串
 * @returns {string} 格式化後的時間字串
 */
const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return '未知';
  
  try {
    const date = new Date(dateTimeStr);
    if (isNaN(date.getTime())) return '無效日期';
    
    return date.toLocaleString('zh-TW', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      hour12: false
    });
  } catch (error) {
    console.warn('時間格式化失敗:', error);
    return dateTimeStr;
  }
};

/**
 * 載入預約訂單資料
 */
const loadAppointmentData = async () => {
  try {
    loading.value = true;
    error.value = null;
    
    // 取得路由參數中的預約 ID
    const appointmentId = parseInt(route.params.id);
    if (!appointmentId || isNaN(appointmentId)) {
      throw new Error('無效的預約訂單 ID');
    }

    console.log('🔍 載入預約訂單詳情, ID:', appointmentId);

    // 載入預約訂單詳情
    const appointment = await loadAppointmentById(appointmentId);
    
    if (!appointment) {
      throw new Error('找不到指定的預約訂單');
    }
    
    appointmentData.value = appointment;
    console.log('✅ 預約訂單詳情載入成功:', appointment);

    // 如果是會員預約且有會員 ID，載入會員資料
    if (!appointment.isBlocked && appointment.memberId) {
      await loadMemberData(appointment.memberId);
    }

  } catch (err) {
    console.error('❌ 載入預約訂單詳情失敗:', err);
    error.value = err.message || '載入預約訂單詳情失敗';
    
    // 顯示錯誤通知
    showToast({
      title: '載入失敗',
      message: error.value,
      type: 'error'
    });
  } finally {
    loading.value = false;
  }
};

// 元件掛載時載入資料
onMounted(() => {
  console.log('🚀 AppointmentDetail 元件已掛載，開始載入資料');
  loadAppointmentData();
});
</script>

<style scoped>
.dashboard-main-body {
  padding: 1.5rem;
}

.form-control-plaintext {
  background-color: var(--bs-gray-100);
  border: 1px solid var(--bs-border-color);
  border-radius: 0.375rem;
  color: var(--bs-body-color);
  font-weight: 500;
}

.border-bottom {
  border-bottom: 1px solid var(--bs-border-color) !important;
}

.card-header {
  border-bottom: 1px solid rgba(255, 255, 255, 0.125);
}

.badge {
  font-size: 0.75rem;
  font-weight: 500;
  padding: 0.375rem 0.75rem;
}

.spinner-border {
  width: 2rem;
  height: 2rem;
}

/* 響應式設計 */
@media (max-width: 768px) {
  .dashboard-main-body {
    padding: 1rem;
  }
  
  .d-flex.gap-2 {
    flex-direction: column;
    gap: 0.5rem !important;
  }
  
  .card-header .d-flex {
    flex-direction: column;
    align-items: flex-start !important;
    gap: 1rem;
  }
}

/* 增強視覺效果 */
.btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 0.25rem 0.5rem rgba(0, 0, 0, 0.1);
}

.form-control-plaintext:hover {
  background-color: var(--bs-gray-200);
}

/* 圖示顏色 */
.text-success { color: var(--bs-success) !important; }
.text-danger { color: var(--bs-danger) !important; }
.text-primary { color: var(--bs-primary) !important; }
.text-info { color: var(--bs-info) !important; }
.text-warning { color: var(--bs-warning) !important; }
</style>
