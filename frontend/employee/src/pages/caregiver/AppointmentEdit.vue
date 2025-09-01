<template>
  <div class="dashboard-main-body">
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

    <!-- 編輯表單 -->
    <div v-else-if="appointmentData" class="card">
      <div class="card-header bg-primary text-white">
        <h5 class="card-title mb-0">
          <iconify-icon icon="lucide:edit" class="me-2"></iconify-icon>
          編輯預約訂單 #{{ appointmentData.id }}
        </h5>
        <p class="mb-0 mt-1 small opacity-75">
          {{ appointmentData.isBlocked ? '虛擬訂單 (時間鎖定)' : '會員預約訂單' }}
        </p>
      </div>
      
      <div class="card-body">
        <form @submit.prevent="handleSubmit">
          <div class="row">
            <!-- 基本資訊區塊 -->
            <div class="col-12">
              <h6 class="border-bottom pb-2 mb-3">
                <iconify-icon icon="material-symbols:info-outline" class="me-2"></iconify-icon>
                基本資訊
              </h6>
            </div>

            <!-- 訂單編號 (唯讀) -->
            <div class="col-md-6 mb-3">
              <label class="form-label">訂單編號</label>
              <input 
                type="text" 
                class="form-control" 
                :value="`#${appointmentData.id}`"
                readonly
              >
            </div>

            <!-- 訂單類型 (唯讀) -->
            <div class="col-md-6 mb-3">
              <label class="form-label">訂單類型</label>
              <input 
                type="text" 
                class="form-control" 
                :value="appointmentData.isBlocked ? '虛擬訂單' : '會員預約'"
                readonly
              >
            </div>

            <!-- 會員/虛擬訂單類型 -->
            <div class="col-md-6 mb-3">
              <label class="form-label">
                {{ appointmentData.isBlocked ? '鎖定類型' : '會員' }}
              </label>
              <input 
                type="text" 
                class="form-control" 
                :value="appointmentData.memberName"
                readonly
              >
            </div>

            <!-- 照服員選擇 -->
            <div class="col-md-6 mb-3">
              <label class="form-label required">指派照服員</label>
              <select 
                v-model="formData.caregiverId" 
                class="form-select"
                :class="{ 'is-invalid': errors.caregiverId }"
                @change="clearConflictCheck"
              >
                <option value="">請選擇照服員</option>
                <option 
                  v-for="caregiver in caregivers" 
                  :key="caregiver.id" 
                  :value="String(caregiver.id)"
                >
                  {{ caregiver.chineseName }} (ID: {{ caregiver.id }})
                </option>
              </select>
              <div v-if="errors.caregiverId" class="invalid-feedback">
                {{ errors.caregiverId }}
              </div>
            </div>

            <!-- 時間設定區塊 -->
            <div class="col-12 mt-3">
              <h6 class="border-bottom pb-2 mb-3">
                <iconify-icon icon="material-symbols:schedule" class="me-2"></iconify-icon>
                時間設定
              </h6>
            </div>

            <!-- 開始日期 -->
            <div class="col-md-6 mb-3">
              <label class="form-label required">開始日期</label>
              <input 
                type="date" 
                v-model="formData.startDate"
                class="form-control"
                :class="{ 'is-invalid': errors.startDate }"
                :min="todayDate"
                @change="clearConflictCheck"
              >
              <div v-if="errors.startDate" class="invalid-feedback">
                {{ errors.startDate }}
              </div>
            </div>

            <!-- 開始時間 -->
            <div class="col-md-6 mb-3">
              <label class="form-label required">開始時間</label>
              <select 
                v-model="formData.startTime"
                class="form-select"
                :class="{ 'is-invalid': errors.startTime }"
                @change="clearConflictCheck"
              >
                <option value="">請選擇開始時間</option>
                <option value="06:00">06:00</option>
                <option value="07:00">07:00</option>
                <option value="08:00">08:00</option>
                <option value="09:00">09:00</option>
                <option value="10:00">10:00</option>
                <option value="11:00">11:00</option>
                <option value="12:00">12:00</option>
                <option value="13:00">13:00</option>
                <option value="14:00">14:00</option>
                <option value="15:00">15:00</option>
                <option value="16:00">16:00</option>
                <option value="17:00">17:00</option>
                <option value="18:00">18:00</option>
                <option value="19:00">19:00</option>
                <option value="20:00">20:00</option>
                <option value="21:00">21:00</option>
                <option value="22:00">22:00</option>
                <option value="23:00">23:00</option>
              </select>
              <div v-if="errors.startTime" class="invalid-feedback">
                {{ errors.startTime }}
              </div>
            </div>

            <!-- 結束日期 -->
            <div class="col-md-6 mb-3">
              <label class="form-label required">結束日期</label>
              <input 
                type="date" 
                v-model="formData.endDate"
                class="form-control"
                :class="{ 'is-invalid': errors.endDate }"
                :min="formData.startDate || todayDate"
                @change="clearConflictCheck"
              >
              <div v-if="errors.endDate" class="invalid-feedback">
                {{ errors.endDate }}
              </div>
            </div>

            <!-- 結束時間 -->
            <div class="col-md-6 mb-3">
              <label class="form-label required">結束時間</label>
              <select 
                v-model="formData.endTime"
                class="form-select"
                :class="{ 'is-invalid': errors.endTime }"
                @change="clearConflictCheck"
              >
                <option value="">請選擇結束時間</option>
                <option value="06:00">06:00</option>
                <option value="07:00">07:00</option>
                <option value="08:00">08:00</option>
                <option value="09:00">09:00</option>
                <option value="10:00">10:00</option>
                <option value="11:00">11:00</option>
                <option value="12:00">12:00</option>
                <option value="13:00">13:00</option>
                <option value="14:00">14:00</option>
                <option value="15:00">15:00</option>
                <option value="16:00">16:00</option>
                <option value="17:00">17:00</option>
                <option value="18:00">18:00</option>
                <option value="19:00">19:00</option>
                <option value="20:00">20:00</option>
                <option value="21:00">21:00</option>
                <option value="22:00">22:00</option>
                <option value="23:00">23:00</option>
              </select>
              <div v-if="errors.endTime" class="invalid-feedback">
                {{ errors.endTime }}
              </div>
            </div>

            <!-- 額外設定區塊 -->
            <div class="col-12 mt-3">
              <h6 class="border-bottom pb-2 mb-3">
                <iconify-icon icon="material-symbols:settings" class="me-2"></iconify-icon>
                額外設定
              </h6>
            </div>

            <!-- 服務地點 (會員預約才顯示) -->
            <div v-if="!appointmentData.isBlocked" class="col-md-6 mb-3">
              <label class="form-label">服務地點</label>
              <input 
                type="text" 
                v-model="formData.serviceLocation"
                class="form-control"
                placeholder="請輸入服務地點..."
              >
            </div>

            <!-- 總金額 (會員預約才顯示) -->
            <div v-if="!appointmentData.isBlocked" class="col-md-6 mb-3">
              <label class="form-label">總金額</label>
              <div class="input-group">
                <span class="input-group-text">$</span>
                <input 
                  type="number" 
                  v-model.number="formData.totalAmount"
                  class="form-control"
                  min="0"
                  step="1"
                  placeholder="0"
                >
              </div>
            </div>

            <!-- 備註 -->
            <div class="col-12 mb-3">
              <label class="form-label">備註說明</label>
              <textarea 
                v-model="formData.notes"
                class="form-control"
                rows="3"
                placeholder="請輸入備註說明..."
              ></textarea>
            </div>
          </div>

          <!-- 時間衝突檢查結果 -->
          <div v-if="conflictCheckResult" class="mb-3">
            <div 
              :class="`alert ${conflictCheckResult.hasConflict ? 'alert-danger' : 'alert-success'}`" 
              role="alert"
            >
              <div class="d-flex align-items-center">
                <iconify-icon 
                  :icon="conflictCheckResult.hasConflict ? 'material-symbols:warning' : 'material-symbols:check-circle'" 
                  class="me-2"
                ></iconify-icon>
                <div>
                  <strong>時間衝突檢查：</strong>
                  {{ conflictCheckResult.message }}
                </div>
              </div>
              <!-- 如果有衝突，顯示衝突詳情 -->
              <div v-if="conflictCheckResult.hasConflict && conflictCheckResult.conflicts && conflictCheckResult.conflicts.length > 0" class="mt-2">
                <small class="text-muted">衝突的預約：</small>
                <ul class="mb-0 mt-1">
                  <li v-for="conflict in conflictCheckResult.conflicts" :key="conflict.id" class="small">
                    {{ conflict.memberName || '系統預約' }} - {{ formatDateTime(conflict.scheduledAt) }} ~ {{ formatDateTime(conflict.endTime) }}
                  </li>
                </ul>
              </div>
            </div>
          </div>

          <!-- 預覽資訊 -->
          <div v-if="isFormValid" class="alert alert-info mb-3">
            <h6 class="alert-heading">
              <iconify-icon icon="material-symbols:preview" class="me-2"></iconify-icon>
              修改預覽
            </h6>
            <div class="row">
              <div class="col-md-4">
                <strong>照服員：</strong>{{ selectedCaregiverName }}
              </div>
              <div class="col-md-4">
                <strong>服務時長：</strong>{{ totalHours }} 小時
              </div>
              <div class="col-md-4">
                <strong>金額：</strong>${{ formData.totalAmount || 0 }}
              </div>
            </div>
            <div class="row mt-2">
              <div class="col-md-6">
                <strong>開始：</strong>{{ startDateTime }}
              </div>
              <div class="col-md-6">
                <strong>結束：</strong>{{ endDateTime }}
              </div>
            </div>
          </div>

          <!-- 操作按鈕 -->
          <div class="d-flex justify-content-between align-items-center">
            <router-link to="/caregiver/appointments" class="btn btn-secondary">
              <iconify-icon icon="material-symbols:arrow-back" class="me-1"></iconify-icon>
              返回列表
            </router-link>
            
            <div class="d-flex gap-2 align-items-center">
              <button 
                type="button" 
                @click="checkTimeConflict"
                class="btn btn-outline-primary"
                :disabled="!canCheckConflict || checkingConflict"
              >
                <span v-if="checkingConflict" class="spinner-border spinner-border-sm me-2"></span>
                <iconify-icon v-else icon="material-symbols:schedule" class="me-1"></iconify-icon>
                檢查時間衝突
              </button>
              
              <button 
                type="submit"
                class="btn btn-success"
                :disabled="!canSubmit || submitting"
              >
                <span v-if="submitting" class="spinner-border spinner-border-sm me-2"></span>
                <iconify-icon v-else icon="material-symbols:save" class="me-1"></iconify-icon>
                儲存修改
              </button>
            </div>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import Breadcrumb from '@/components/breadcrumb/Breadcrumb.vue';
import { useToast } from '@/composables/useToast';
import { useAppointments } from '@/composables/useCaregiverAppointment';
import { scheduleApi } from '@/api/caregiverScheduleApi';
import { appointmentApi } from '@/api/caregiverAppointmentApi';

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
const submitting = ref(false);
const checkingConflict = ref(false);
const conflictCheckResult = ref(null);

// 預約訂單資料
const appointmentData = ref(null);
const caregivers = ref([]);

// 表單資料
const formData = reactive({
  caregiverId: '',
  startDate: '',
  startTime: '',
  endDate: '',
  endTime: '',
  serviceLocation: '',
  totalAmount: 0,
  notes: ''
});

// 錯誤狀態
const errors = reactive({});

// 今天日期 (用於日期選擇限制)
const todayDate = computed(() => {
  return new Date().toISOString().split('T')[0];
});

// 選中照服員名稱
const selectedCaregiverName = computed(() => {
  if (!formData.caregiverId) return '未選擇';
  const caregiver = caregivers.value.find(c => 
    String(c.id) === String(formData.caregiverId) || c.id == formData.caregiverId
  );
  return caregiver ? caregiver.chineseName : '未找到';
});

// 開始日期時間
const startDateTime = computed(() => {
  if (!formData.startDate || !formData.startTime) return '';
  return `${formData.startDate} ${formData.startTime}`;
});

// 結束日期時間
const endDateTime = computed(() => {
  if (!formData.endDate || !formData.endTime) return '';
  return `${formData.endDate} ${formData.endTime}`;
});

// 總時長計算
const totalHours = computed(() => {
  if (!startDateTime.value || !endDateTime.value) return 0;
  
  try {
    const start = new Date(`${startDateTime.value}:00`);
    const end = new Date(`${endDateTime.value}:00`);
    
    if (isNaN(start.getTime()) || isNaN(end.getTime())) return 0;
    
    const diffMs = end - start;
    if (diffMs <= 0) return 0;
    
    const hours = Math.round(diffMs / (1000 * 60 * 60) * 10) / 10;
    return hours > 0 ? hours : 0;
  } catch (error) {
    return 0;
  }
});

// 表單驗證
const isFormValid = computed(() => {
  return formData.caregiverId && 
         formData.startDate && 
         formData.startTime && 
         formData.endDate && 
         formData.endTime &&
         totalHours.value > 0;
});

// 是否可以檢查時間衝突
const canCheckConflict = computed(() => {
  return formData.caregiverId && 
         formData.startDate && 
         formData.startTime && 
         formData.endDate && 
         formData.endTime &&
         totalHours.value > 0;
});

// 是否可以提交
const canSubmit = computed(() => {
  if (!isFormValid.value) return false;
  
  // 如果還沒檢查時間衝突，也允許提交
  if (!conflictCheckResult.value) return true;
  
  // 如果檢查過但有衝突，不允許提交
  if (conflictCheckResult.value.hasConflict) return false;
  
  // 檢查過且沒有衝突，允許提交
  return true;
});

// 監聽結束日期變化，自動設定為開始日期
watch(() => formData.startDate, (newStartDate) => {
  if (newStartDate && !formData.endDate) {
    formData.endDate = newStartDate;
  }
});

// 監聽表單變化，清除錯誤
watch(() => formData, () => {
  Object.keys(errors).forEach(key => {
    if (formData[key]) {
      delete errors[key];
    }
  });
}, { deep: true });

/**
 * 格式化時間顯示
 */
const formatDateTime = (dateTimeStr) => {
  try {
    const date = new Date(dateTimeStr);
    return date.toLocaleString('zh-TW', {
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    });
  } catch (error) {
    return dateTimeStr;
  }
};

/**
 * 清除時間衝突檢查結果
 */
const clearConflictCheck = () => {
  conflictCheckResult.value = null;
};

/**
 * 載入預約訂單資料
 */
const loadAppointmentData = async () => {
  try {
    loading.value = true;
    error.value = null;
    
    const appointmentId = parseInt(route.params.id);
    if (!appointmentId) {
      throw new Error('無效的預約訂單 ID');
    }

    // 載入預約訂單詳情
    const appointment = await loadAppointmentById(appointmentId);
    appointmentData.value = appointment;

    // 解析時間資料並填入表單
    if (appointment.scheduledAt) {
      const startDate = new Date(appointment.scheduledAt);
      formData.startDate = startDate.toISOString().split('T')[0];
      formData.startTime = startDate.toTimeString().slice(0, 5);
    }

    if (appointment.endTime) {
      const endDate = new Date(appointment.endTime);
      formData.endDate = endDate.toISOString().split('T')[0];
      formData.endTime = endDate.toTimeString().slice(0, 5);
    }

    // 填入其他表單資料
    formData.caregiverId = String(appointment.caregiverId || '');
    formData.serviceLocation = appointment.serviceLocation || '';
    formData.totalAmount = appointment.totalAmount || 0;
    formData.notes = appointment.notes || '';

    // 載入照服員列表
    await loadCaregivers();

  } catch (err) {
    error.value = err.message || '載入預約訂單失敗';
  } finally {
    loading.value = false;
  }
};

/**
 * 載入照服員列表
 */
const loadCaregivers = async () => {
  try {
    const response = await scheduleApi.getAllCaregivers();
    
    if (response.status === 200) {
      let caregiverData = [];
      
      if (response.data.success && response.data.data) {
        caregiverData = response.data.data;
      } else if (Array.isArray(response.data)) {
        caregiverData = response.data;
      } else if (response.data.caregivers) {
        caregiverData = response.data.caregivers;
      }
      
      caregivers.value = caregiverData
        .filter(caregiver => caregiver && typeof caregiver === 'object')
        .map(caregiver => ({
          id: caregiver.id || caregiver.caregiverId || caregiver.caregiver_id,
          chineseName: caregiver.chineseName || caregiver.chinese_name || caregiver.name,
          serviceArea: caregiver.serviceArea || caregiver.service_area || '未指定',
          isActive: caregiver.isActive !== undefined ? caregiver.isActive : true
        }))
        .filter(caregiver => caregiver.id && caregiver.chineseName && caregiver.isActive);
    }
  } catch (error) {
    showToast('載入照服員列表失敗', 'error');
    caregivers.value = [];
  }
};

/**
 * 檢查時間衝突
 */
const checkTimeConflict = async () => {
  if (!validateForm()) {
    showToast({
      title: '表單驗證失敗',
      message: '請先完整填寫表單',
      type: 'warning'
    });
    return;
  }

  checkingConflict.value = true;

  // 🔧 顯示正在檢查的提示
  // showToast({
  //   title: '正在檢查',
  //   message: '正在檢查時間衝突，請稍候...',
  //   type: 'info'
  // });

  try {
    const conflictData = {
      caregiverId: parseInt(formData.caregiverId),
      startTime: `${formData.startDate}T${formData.startTime}:00`,
      endTime: `${formData.endDate}T${formData.endTime}:00`,
      excludeAppointmentId: appointmentData.value.id // 排除當前編輯的預約
    };

    const response = await scheduleApi.checkTimeConflict(
      conflictData.caregiverId,
      conflictData.startTime,
      conflictData.endTime,
      conflictData.excludeAppointmentId
    );

    if (response.status === 200) {
      conflictCheckResult.value = {
        hasConflict: response.data.hasConflict || false,
        message: response.data.message || (response.data.hasConflict ? 
          '該時段已有其他預約，存在時間衝突' : 
          '該時段可以預約，無時間衝突'
        ),
        conflicts: response.data.conflicts || []
      };

      if (conflictCheckResult.value.hasConflict) {
        showToast({
          title: '發現時間衝突',
          message: '該時段存在時間衝突，請調整時間後再試',
          type: 'warning'
        });
      } else {
        showToast({
          title: '檢查完成',
          message: '該時段可以預約，無時間衝突',
          type: 'success'
        });
      }
    }

  } catch (error) {
    showToast({
      title: '檢查失敗',
      message: '檢查時間衝突失敗，但仍可繼續修改預約',
      type: 'error'
    });
    
    conflictCheckResult.value = {
      hasConflict: false,
      message: '無法檢查時間衝突，但可以繼續修改預約',
      conflicts: []
    };
  } finally {
    checkingConflict.value = false;
  }
};

/**
 * 表單驗證
 */
const validateForm = () => {
  const newErrors = {};

  if (!formData.caregiverId) {
    newErrors.caregiverId = '請選擇照服員';
  }

  if (!formData.startDate) {
    newErrors.startDate = '請選擇開始日期';
  }

  if (!formData.startTime) {
    newErrors.startTime = '請選擇開始時間';
  }

  if (!formData.endDate) {
    newErrors.endDate = '請選擇結束日期';
  }

  if (!formData.endTime) {
    newErrors.endTime = '請選擇結束時間';
  }

  // 時間邏輯檢查
  if (formData.startDate && formData.endDate && formData.startTime && formData.endTime) {
    const start = new Date(`${formData.startDate}T${formData.startTime}`);
    const end = new Date(`${formData.endDate}T${formData.endTime}`);
    
    if (end <= start) {
      newErrors.endTime = '結束時間必須晚於開始時間';
    }
  }

  // 清空舊錯誤並設置新錯誤
  Object.keys(errors).forEach(key => delete errors[key]);
  Object.assign(errors, newErrors);

  return Object.keys(newErrors).length === 0;
};

/**
 * 處理表單提交
 */
const handleSubmit = async () => {
  if (!validateForm()) {
    showToast('請檢查表單內容', 'warning');
    return;
  }

  // 檢查是否有時間衝突
  if (conflictCheckResult.value?.hasConflict) {
    showToast('存在時間衝突，無法修改預約', 'error');
    return;
  }

  submitting.value = true;

  // 🔧 顯示正在儲存的提示
  // showToast({
  //   title: '正在儲存',
  //   message: '正在儲存預約訂單修改，請稍候...',
  //   type: 'info'
  // });

  try {
    // 準備更新資料
    const updateData = {
      caregiverId: parseInt(formData.caregiverId),
      scheduledAt: `${formData.startDate}T${formData.startTime}:00`,
      endTime: `${formData.endDate}T${formData.endTime}:00`,
      serviceLocation: formData.serviceLocation?.trim() || '',
      totalAmount: formData.totalAmount || 0,
      notes: formData.notes?.trim() || ''
    };

    // 呼叫更新 API
    const response = await appointmentApi.updateAppointment(appointmentData.value.id, updateData);

    if (response.status === 200) {
      // showToast({
      //   title: '儲存成功',
      //   message: '預約訂單修改成功！即將返回列表頁面',
      //   type: 'success'
      // });
      
      // 延遲導航，讓使用者看到成功訊息
      setTimeout(() => {
        router.push('/caregiver/appointments');
      }, 1500);
    } else {
      throw new Error('修改失敗');
    }

  } catch (error) {
    
    let errorMessage = '修改預約訂單失敗';
    if (error.response?.data?.message) {
      errorMessage = error.response.data.message;
    } else if (error.message) {
      errorMessage = error.message;
    }
    
    showToast({
      title: '儲存失敗',
      message: errorMessage,
      type: 'error'
    });
  } finally {
    submitting.value = false;
  }
};

// 元件掛載時載入資料
onMounted(() => {
  loadAppointmentData();
});
</script>

<style scoped>
.dashboard-main-body {
  padding: 1.5rem;
}

.required {
  position: relative;
}

.required::after {
  content: ' *';
  color: var(--bs-danger);
  font-weight: bold;
}

.alert-heading {
  margin-bottom: 0.75rem;
  font-weight: 600;
}

.spinner-border-sm {
  width: 1rem;
  height: 1rem;
}

.form-control:focus,
.form-select:focus {
  border-color: var(--bs-primary);
  box-shadow: 0 0 0 0.2rem rgba(var(--bs-primary-rgb), 0.25);
}

.is-invalid {
  border-color: var(--bs-danger);
}

.invalid-feedback {
  display: block;
  font-size: 0.875rem;
}

.card-header {
  border-bottom: 1px solid rgba(255, 255, 255, 0.125);
}

.border-bottom {
  border-bottom: 1px solid var(--bs-border-color) !important;
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
}

/* 增強視覺效果 */
.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.form-select option {
  padding: 0.5rem;
}
</style>
