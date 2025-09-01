<template>
  <div class="card">
    <div class="card-header">
      <h5 class="card-title mb-0">新增虛擬訂單 (時間鎖定)</h5>
      <p class="text-muted mb-0">為照服員建立時間鎖定，該時段將不接受客戶預約</p>
    </div>
    
    <div class="card-body">
      <form @submit.prevent="handleSubmit">
        <div class="row">
          <!-- 照服員選擇 -->
          <div class="col-md-6 mb-3">
            <label class="form-label required">選擇照服員</label>
           <select 
  v-model="formData.caregiverId" 
  class="form-select"
  :class="{ 'is-invalid': errors.caregiverId }"
   @change="(event) => {
    console.log('🔍 照服員選擇事件:', {
      選中值: event.target.value,
      值的類型: typeof event.target.value,
      表單中的值: formData.caregiverId,
      照服員列表: caregivers.map(c => ({ id: c.id, name: c.chineseName, type: typeof c.id }))
    });
    clearConflictCheck();
  }"
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

          <!-- 鎖定類型 -->
          <div class="col-md-6 mb-3">
            <label class="form-label required">鎖定類型</label>
            <select 
              v-model="formData.blockType" 
              class="form-select"
              :class="{ 'is-invalid': errors.blockType }"
            >
              <option value="">請選擇鎖定類型</option>
              <option value="off-work">下班時間</option>
              <option value="break">休息時間</option>
              <option value="training">培訓時間</option>
              <option value="leave">請假</option>
            </select>
            <div v-if="errors.blockType" class="invalid-feedback">
              {{ errors.blockType }}
            </div>
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

          <!-- 備註 -->
          <div class="col-12 mb-3">
            <label class="form-label">備註</label>
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
            預約預覽
          </h6>
          <div class="row">
            <div class="col-md-4">
              <strong>照服員：</strong>{{ selectedCaregiverName }}
            </div>
            <div class="col-md-4">
              <strong>鎖定類型：</strong>{{ blockTypeDisplay }}
            </div>
            <div class="col-md-4">
              <strong>總時長：</strong>{{ totalHours }} 小時
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
          <button 
            type="button" 
            @click="$emit('cancel')"
            class="btn btn-secondary"
          >
            <iconify-icon icon="material-symbols:cancel" class="me-1"></iconify-icon>
            取消
          </button>
          
          <div class="d-flex gap-2 align-items-center">
            <button 
              type="button" 
              @click="fillTestData"
              class="btn btn-warning"
            >
              <iconify-icon icon="solar:test-tube-outline" class="me-1"></iconify-icon>
              測試資料
            </button>
            
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
              class="btn btn-primary"
              :disabled="!canSubmit || submitting"
            >
              <span v-if="submitting" class="spinner-border spinner-border-sm me-2"></span>
              <iconify-icon v-else icon="material-symbols:add" class="me-1"></iconify-icon>
              建立虛擬訂單
            </button>
          </div>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue';
import { useToast } from '@/composables/useToast';

// 定義 props 和 emits
const props = defineProps({
  caregivers: {
    type: Array,
    default: () => []
  }
});

const emit = defineEmits([
  'submit',
  'cancel',
  'check-conflict'
]);

// 使用 Toast 通知
const { showToast } = useToast();

// 表單資料
const formData = reactive({
  caregiverId: '',
  blockType: '',
  startDate: '',
  startTime: '',
  endDate: '',
  endTime: '',
  notes: ''
});

// 錯誤狀態
const errors = reactive({});

// 狀態管理
const submitting = ref(false);
const checkingConflict = ref(false);
const conflictCheckResult = ref(null);

// 🔧 今天日期 (用於日期選擇限制)
const todayDate = computed(() => {
  return new Date().toISOString().split('T')[0];
});

// 🔧 選中照服員名稱
const selectedCaregiverName = computed(() => {
  if (!formData.caregiverId) return '未選擇';
  const caregiver = props.caregivers.find(c => 
  String(c.id) === String(formData.caregiverId) || c.id == formData.caregiverId
);
  return caregiver ? caregiver.chineseName : '未找到';
});

// 🔧 鎖定類型顯示文字
const blockTypeDisplay = computed(() => {
  const typeMap = {
    'off-work': '下班時間',
    'break': '休息時間',
    'training': '培訓時間',
    'leave': '請假',
    'unavailable': '不可用'
  };
  return typeMap[formData.blockType] || '未選擇';
});

// 🔧 開始日期時間
const startDateTime = computed(() => {
  if (!formData.startDate || !formData.startTime) return '';
  return `${formData.startDate} ${formData.startTime}`;
});

// 🔧 結束日期時間
const endDateTime = computed(() => {
  if (!formData.endDate || !formData.endTime) return '';
  return `${formData.endDate} ${formData.endTime}`;
});

// 🔧 改善時間計算邏輯
const totalHours = computed(() => {
  if (!startDateTime.value || !endDateTime.value) {
    console.log('🕒 時間未完整填寫:', { start: startDateTime.value, end: endDateTime.value });
    return 0;
  }
  
  try {
    const startStr = `${startDateTime.value}:00`;
    const endStr = `${endDateTime.value}:00`;
    
    const start = new Date(startStr);
    const end = new Date(endStr);
    
    // 檢查 Date 物件是否有效
    if (isNaN(start.getTime()) || isNaN(end.getTime())) {
      console.error('❌ 無效的日期格式:', { start: startStr, end: endStr });
      return 0;
    }
    
    const diffMs = end - start;
    
    if (diffMs <= 0) {
      console.warn('⚠️ 結束時間不可早於或等於開始時間');
      return 0;
    }
    
    const hours = Math.round(diffMs / (1000 * 60 * 60) * 10) / 10;
    console.log('✅ 計算總時長:', hours, '小時');
    
    return hours > 0 ? hours : 0;
  } catch (error) {
    console.error('❌ 時間計算錯誤:', error);
    return 0;
  }
});

// 🔧 改善表單驗證邏輯
const isFormValid = computed(() => {
  const basicChecks = {
    caregiverId: !!formData.caregiverId,
    blockType: !!formData.blockType,
    startDate: !!formData.startDate,
    startTime: !!formData.startTime,
    endDate: !!formData.endDate,
    endTime: !!formData.endTime,
    validHours: totalHours.value > 0
  };
  
  const isValid = Object.values(basicChecks).every(check => check);
  
  return isValid;
});

// 🔧 是否可以檢查時間衝突
const canCheckConflict = computed(() => {
  return formData.caregiverId && 
         formData.startDate && 
         formData.startTime && 
         formData.endDate && 
         formData.endTime &&
         totalHours.value > 0;
});

// 🔧 改善提交邏輯
const canSubmit = computed(() => {
  // 基本表單必須有效
  if (!isFormValid.value) {
    return false;
  }
  
  // 如果還沒檢查時間衝突，也允許提交
  if (!conflictCheckResult.value) {
    return true;
  }
  
  // 如果檢查過但有衝突，不允許提交
  if (conflictCheckResult.value.hasConflict) {
    return false;
  }
  
  // 檢查過且沒有衝突，允許提交
  return true;
});

// 🔧 監聽結束日期變化，自動設定為開始日期
watch(() => formData.startDate, (newStartDate) => {
  if (newStartDate && !formData.endDate) {
    formData.endDate = newStartDate;
    console.log('📅 自動設定結束日期為:', newStartDate);
  }
});

// 🔧 監聽表單變化，清除錯誤
watch(() => formData, () => {
  // 清除相關錯誤
  Object.keys(errors).forEach(key => {
    if (formData[key]) {
      delete errors[key];
    }
  });
}, { deep: true });

// 🔧 新增除錯資訊切換
const clearConflictCheck = () => {
  conflictCheckResult.value = null;
  console.log('🔄 清除時間衝突檢查結果');
};

// 🔧 格式化時間顯示
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

// 🔧 表單驗證函數
const validateForm = () => {
  const newErrors = {};

  // 必填欄位檢查
  if (!formData.caregiverId) {
    newErrors.caregiverId = '請選擇照服員';
  }

  if (!formData.blockType) {
    newErrors.blockType = '請選擇鎖定類型';
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
    
    // 檢查是否為過去時間
    const now = new Date();
    if (start < now) {
      newErrors.startDate = '開始時間不能是過去時間';
    }
  }

  // 清空舊錯誤並設置新錯誤
  Object.keys(errors).forEach(key => delete errors[key]);
  Object.assign(errors, newErrors);

  console.log('🔍 表單驗證結果:', Object.keys(newErrors).length === 0 ? '通過' : '失敗', newErrors);

  return Object.keys(newErrors).length === 0;
};

// 🔧 檢查時間衝突
const checkTimeConflict = async () => {
  console.log('🔍 開始檢查時間衝突');

  if (!validateForm()) {
    showToast({
      title: '表單未完成',
      message: '請檢查並完整填寫所有必填欄位',
      type: 'warning'
    });
    return;
  }

  checkingConflict.value = true;

  try {
    const conflictData = {
      caregiverId: parseInt(formData.caregiverId),
      startTime: `${formData.startDate}T${formData.startTime}:00`,
      endTime: `${formData.endDate}T${formData.endTime}:00`
    };

    console.log('📡 發送時間衝突檢查請求:', conflictData);

    // 顯示檢查中通知
    // showToast({
    //   title: '檢查中',
    //   message: '正在檢查時間衝突...',
    //   type: 'info'
    // });

    // 觸發父元件的檢查處理，使用 Promise 等待結果
    const result = await new Promise((resolve, reject) => {
      // 設置超時機制
      const timeout = setTimeout(() => {
        reject(new Error('檢查時間衝突超時'));
      }, 10000);

      // 觸發檢查並傳遞 resolve 函數
      emit('check-conflict', conflictData, (result) => {
        clearTimeout(timeout);
        resolve(result);
      });
    });

    // 設置檢查結果
    conflictCheckResult.value = result;
    console.log('✅ 時間衝突檢查完成:', result);

    // 檢查是否有錯誤
    if (result.error) {
      showToast({
        title: '檢查失敗',
        message: result.error,
        type: 'error'
      });
    } else {
      // 顯示檢查結果通知
      if (result.hasConflict) {
        showToast({
          title: '時間衝突',
          message: `該時段存在 ${result.conflicts?.length || 0} 個衝突預約`,
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
    console.error('❌ 檢查時間衝突失敗:', error);
    showToast({
      title: '檢查失敗',
      message: error.message || '檢查時間衝突失敗，請稍後再試',
      type: 'error'
    });
    
    // 設置預設結果
    conflictCheckResult.value = {
      hasConflict: false,
      message: '無法檢查時間衝突，但可以繼續建立預約',
      conflicts: []
    };
  } finally {
    checkingConflict.value = false;
  }
};

// 🔧 處理表單提交
const handleSubmit = async () => {
  console.log('🚀 開始提交虛擬訂單表單');

  if (!validateForm()) {
    showToast({
      title: '表單驗證失敗',
      message: '請檢查表單內容並修正錯誤',
      type: 'warning'
    });
    return;
  }

  // 檢查是否有時間衝突
  if (conflictCheckResult.value?.hasConflict) {
    showToast({
      title: '時間衝突',
      message: '存在時間衝突，無法建立預約',
      type: 'error'
    });
    return;
  }

  submitting.value = true;

  try {
    // 🔧 改善資料準備 - 加入詳細的除錯和驗證
    console.log('🔍 原始表單資料:', formData);
    
    // 🔧 安全的照服員 ID 轉換
    let caregiverIdValue = formData.caregiverId;
    let caregiverIdNumber;
    
    if (typeof caregiverIdValue === 'string') {
      caregiverIdNumber = parseInt(caregiverIdValue, 10);
    } else if (typeof caregiverIdValue === 'number') {
      caregiverIdNumber = caregiverIdValue;
    } else {
      throw new Error('照服員 ID 格式錯誤');
    }
    
    // 🔧 檢查轉換結果
    if (isNaN(caregiverIdNumber) || caregiverIdNumber <= 0) {
      console.error('❌ 照服員 ID 轉換失敗:', {
        原始值: caregiverIdValue,
        原始類型: typeof caregiverIdValue,
        轉換結果: caregiverIdNumber,
        照服員列表: props.caregivers.map(c => ({ id: c.id, type: typeof c.id }))
      });
      throw new Error('請重新選擇照服員');
    }
    
    // 準備提交資料
    const appointmentData = {
      caregiverId: caregiverIdNumber,  // 🔧 使用安全轉換的數字
      scheduledAt: `${formData.startDate}T${formData.startTime}:00`,
      endTime: `${formData.endDate}T${formData.endTime}:00`,
      blockType: formData.blockType,
      notes: formData.notes.trim() || `員工建立的${blockTypeDisplay.value}`
    };

    console.log('📤 提交虛擬訂單資料:', appointmentData);
    
    // 🔧 額外驗證所有必要欄位
    if (!appointmentData.caregiverId || appointmentData.caregiverId <= 0) {
      throw new Error('照服員 ID 無效');
    }
    if (!appointmentData.scheduledAt || !appointmentData.endTime) {
      throw new Error('時間資料無效');
    }
    if (!appointmentData.blockType) {
      throw new Error('鎖定類型無效');
    }

    // 觸發父元件的提交處理
    await emit('submit', appointmentData);

    console.log('✅ 虛擬訂單建立成功');
    showToast({
      title: '建立成功',
      message: `虛擬訂單建立成功！照服員: ${selectedCaregiverName.value}`,
      type: 'success'
    });

    // 重置表單
    resetForm();

  } catch (error) {
    console.error('❌ 建立虛擬訂單失敗:', error);
    showToast({
      title: '建立失敗',
      message: error.message || '建立虛擬訂單失敗，請稍後再試',
      type: 'error'
    });
  } finally {
    submitting.value = false;
  }
};

// 🔧 填入測試資料
const fillTestData = () => {
  console.log('🔄 填入測試資料');
  
  // 清除舊的錯誤和衝突檢查結果
  Object.keys(errors).forEach(key => delete errors[key]);
  conflictCheckResult.value = null;

  // 隨機選擇照服員（如果有的話）
  if (props.caregivers && props.caregivers.length > 0) {
    const randomCaregiver = props.caregivers[Math.floor(Math.random() * props.caregivers.length)];
    formData.caregiverId = String(randomCaregiver.id);
  }

  // 設定測試資料
  const tomorrow = new Date();
  tomorrow.setDate(tomorrow.getDate() + 1);
  const tomorrowStr = tomorrow.toISOString().split('T')[0];

  // 隨機測試資料
  const testData = {
    blockType: ['off-work', 'break', 'training', 'leave'][Math.floor(Math.random() * 4)],
    startDate: tomorrowStr,
    startTime: ['09:00', '10:00', '14:00', '15:00'][Math.floor(Math.random() * 4)],
    endDate: tomorrowStr,
    endTime: ['12:00', '13:00', '17:00', '18:00'][Math.floor(Math.random() * 4)],
    notes: [
      '員工培訓時間，無法接受預約',
      '個人請假，暫停服務',
      '設備維護時間',
      '會議時間，請勿安排服務'
    ][Math.floor(Math.random() * 4)]
  };

  // 確保結束時間晚於開始時間
  const startHour = parseInt(testData.startTime.split(':')[0]);
  const endHour = parseInt(testData.endTime.split(':')[0]);
  if (endHour <= startHour) {
    testData.endTime = String(startHour + 2).padStart(2, '0') + ':00';
  }

  // 填入表單
  Object.keys(testData).forEach(key => {
    if (key in formData) {
      formData[key] = testData[key];
    }
  });

  // 顯示成功訊息
  // showToast({
  //   title: '測試資料已填入',
  //   message: '所有欄位已自動填入測試資料',
  //   type: 'info'
  // });

  console.log('✅ 測試資料填入完成:', { ...formData });
};

// 🔧 重置表單
const resetForm = () => {
  console.log('🔄 重置表單');
  
  Object.keys(formData).forEach(key => {
    formData[key] = '';
  });
  
  Object.keys(errors).forEach(key => delete errors[key]);
  
  conflictCheckResult.value = null;
};

// 🔧 元件載入時的初始化
onMounted(() => {
  console.log('📱 CreateAppointmentForm 元件已載入');
  console.log('👥 可用照服員:', props.caregivers.length, '位');
});

// 暴露方法給父元件
defineExpose({
  resetForm
});
</script>

<style scoped>
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
  background-color: #f8f9fa;
  border-bottom: 1px solid #dee2e6;
}

.card-title {
  color: #495057;
  font-weight: 600;
}

.text-muted {
  font-size: 0.875rem;
}

/* 響應式設計 */
@media (max-width: 768px) {
  .d-flex.gap-2 {
    flex-direction: column;
    gap: 0.5rem !important;
  }
  
  .btn-sm {
    font-size: 0.8rem;
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

/* 成功/錯誤狀態顏色 */
.text-success {
  color: #198754 !important;
  font-weight: 500;
}

.text-danger {
  color: #dc3545 !important;
  font-weight: 500;
}

.text-warning {
  color: #fd7e14 !important;
  font-weight: 500;
}

.text-muted {
  color: #6c757d !important;
}
</style>