<template>
  <div class="card">
    <!-- 卡片標頭 - 照服員選擇和檢視控制 -->
    <div class="card-header border-bottom bg-base py-16 px-24">
      <div class="row g-3 align-items-center">
        <!-- 左側：照服員選擇 -->
        <div class="col-md-4">
          <div class="d-flex align-items-center gap-3">
            <div class="flex-shrink-0">
              <iconify-icon icon="ph:user-circle" class="text-2xl text-primary"></iconify-icon>
            </div>
            <div class="flex-grow-1">
              <select 
                v-model="selectedCaregiverId"
                @change="handleCaregiverChange"
                class="form-select form-select-sm"
              >
                <option value="">請選擇照服員</option>
                <option 
                  v-for="caregiver in caregivers" 
                  :key="caregiver.id" 
                  :value="caregiver.id"
                >
                  {{ caregiver.chineseName }} (ID: {{ caregiver.id }})
                </option>
              </select>
            </div>
          </div>
        </div>

        <!-- 中間：日期範圍選擇 -->
        <div class="col-md-4">
          <div class="d-flex align-items-center gap-2">
            <button 
              @click="previousPeriod"
              class="btn btn-sm btn-outline-secondary"
              :disabled="loading"
            >
              <iconify-icon icon="ph:caret-left"></iconify-icon>
            </button>
            
            <div class="text-center flex-grow-1">
              <div class="fw-medium text-secondary-light">{{ currentPeriodText }}</div>
              <div class="text-sm text-secondary-light">{{ dateRangeText }}</div>
            </div>
            
            <button 
              @click="nextPeriod"
              class="btn btn-sm btn-outline-secondary"
              :disabled="loading"
            >
              <iconify-icon icon="ph:caret-right"></iconify-icon>
            </button>
          </div>
        </div>

        <!-- 右側：檢視類型和篩選 -->
        <div class="col-md-4">
          <div class="d-flex align-items-center gap-2 justify-content-end">
            <!-- 檢視類型切換 -->
            <div class="btn-group" role="group">
              <button
                v-for="viewType in viewTypes"
                :key="viewType.value"
                @click="changeViewType(viewType.value)"
                :class="`btn btn-sm ${filters.viewType === viewType.value ? 'btn-primary' : 'btn-outline-primary'}`"
              >
                {{ viewType.label }}
              </button>
            </div>

            <!-- 篩選下拉選單 -->
            <div class="dropdown">
              <button 
                class="btn btn-sm btn-outline-secondary dropdown-toggle" 
                type="button" 
                data-bs-toggle="dropdown"
              >
                <iconify-icon icon="ph:funnel"></iconify-icon>
                篩選
              </button>
              <div class="dropdown-menu dropdown-menu-end p-3" style="min-width: 250px;">
                <div class="mb-3">
                  <label class="form-label text-sm">顯示內容</label>
                  <div class="form-check">
                    <input 
                      v-model="filters.showCustomerAppointments"
                      class="form-check-input" 
                      type="checkbox" 
                      id="showCustomer"
                    >
                    <label class="form-check-label text-sm" for="showCustomer">
                      客戶預約
                    </label>
                  </div>
                  <div class="form-check">
                    <input 
                      v-model="filters.showBlockedSlots"
                      class="form-check-input" 
                      type="checkbox" 
                      id="showBlocked"
                    >
                    <label class="form-check-label text-sm" for="showBlocked">
                      時間鎖定
                    </label>
                  </div>
                </div>
                <div class="mb-3">
                  <label class="form-label text-sm">狀態篩選</label>
                  <select 
                    v-model="filters.statusFilter"
                    class="form-select form-select-sm"
                  >
                    <option value="">全部狀態</option>
                    <option value="pending">待審核</option>
                    <option value="approved">已確認</option>
                    <option value="in_progress">進行中</option>
                    <option value="completed">已完成</option>
                    <option value="cancelled">已取消</option>
                  </select>
                </div>
                <div class="d-grid">
                  <button 
                    @click="resetFilters"
                    class="btn btn-sm btn-outline-secondary"
                  >
                    重置篩選
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 班表內容 -->
    <div class="card-body p-24">
      <!-- 載入狀態 -->
      <div v-if="loading" class="text-center py-5">
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">載入中...</span>
        </div>
        <div class="mt-2 text-secondary-light">載入班表資料中...</div>
      </div>

      <!-- 錯誤狀態 -->
      <div v-else-if="error" class="alert alert-danger" role="alert">
        <iconify-icon icon="material-symbols:error-outline" class="me-2"></iconify-icon>
        {{ error }}
        <button @click="retryLoad" class="btn btn-sm btn-outline-danger ms-2">重試</button>
      </div>

      <!-- 空狀態：未選擇照服員 -->
      <div v-else-if="!selectedCaregiverId" class="text-center py-5">
        <iconify-icon icon="ph:user-circle" class="text-secondary-light" style="font-size: 4rem;"></iconify-icon>
        <h6 class="text-secondary-light mt-3">請選擇照服員</h6>
        <p class="text-secondary-light">請在上方選擇要查看班表的照服員</p>
      </div>

      <!-- 空狀態：無班表資料 -->
      <div v-else-if="Object.keys(filteredScheduleByDate).length === 0" class="text-center py-5">
        <iconify-icon icon="material-symbols:calendar-month-outline" class="text-secondary-light" style="font-size: 4rem;"></iconify-icon>
        <h6 class="text-secondary-light mt-3">暫無班表資料</h6>
        <p class="text-secondary-light">此時間範圍內沒有預約或排班資料</p>
      </div>

      <!-- 班表資料 -->
      <div v-else>
        <!-- 週檢視 -->
        <div v-if="filters.viewType === 'week'" class="schedule-week-view">
          <div class="table-responsive">
            <table class="table table-bordered schedule-table">
              <thead class="bg-light">
                <tr>
                  <th class="text-center" style="width: 100px;">時間</th>
                  <th 
                    v-for="day in weekDays" 
                    :key="day.date" 
                    class="text-center"
                    :class="{ 'bg-primary-50': day.isToday }"
                  >
                    <div>{{ day.dayName }}</div>
                    <div class="text-sm text-secondary-light">{{ day.dateText }}</div>
                  </th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="hour in workingHours" :key="hour">
                  <td class="text-center bg-light fw-medium">{{ hour }}:00</td>
                  <td 
                    v-for="day in weekDays" 
                    :key="`${day.date}-${hour}`" 
                    class="schedule-cell"
                  >
                    <div 
                      v-for="appointment in getAppointmentsForTimeSlot(day.date, hour)"
                      :key="appointment.id"
                      :class="`schedule-item schedule-${appointment.typeColor} border border-${appointment.statusColor}`"
                      @click="$emit('view-appointment', appointment)"
                    >
                      <div class="schedule-title">{{ appointment.title }}</div>
                      <div class="schedule-time">{{ appointment.timeSlotDisplay }}</div>
                      <div class="schedule-status">
                        <span :class="`badge bg-${appointment.statusColor}-focus text-${appointment.statusColor}-main`">
                          {{ appointment.statusDisplay }}
                        </span>
                      </div>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- 日檢視 - 單日時間軸檢視 -->
        <div v-else-if="filters.viewType === 'day'" class="schedule-day-view">
          <div 
            v-for="(daySchedule, date) in filteredScheduleByDate" 
            :key="date"
            class="single-day-schedule"
          >
            <!-- 日期標頭 -->
            <div class="day-header bg-primary text-white p-4 rounded-top">
              <div class="row align-items-center">
                <div class="col">
                  <h5 class="mb-1 fw-bold text-white">{{ formatDateHeader(date) }}</h5>
                  <div class="text-white-50">
                    {{ getDayOfWeek(date) }} • {{ daySchedule.length }} 項預約 • {{ calculateDayHours(daySchedule) }} 小時
                  </div>
                </div>
                <div class="col-auto">
                  <span class="badge bg-white text-primary">
                    今日排程
                  </span>
                </div>
              </div>
            </div>

            <!-- 時間軸檢視 -->
            <div class="timeline-container bg-white border-start border-end border-bottom rounded-bottom">
              <div class="timeline">
                <!-- 生成一天的時間段 (6:00 - 24:00) -->
                <div 
                  v-for="hour in dayWorkingHours" 
                  :key="hour"
                  class="timeline-slot"
                  :class="{ 'has-appointment': hasAppointmentAtHour(daySchedule, hour) }"
                >
                  <!-- 時間標籤 -->
                  <div class="timeline-time">
                    <span class="hour">{{ formatHour(hour) }}</span>
                  </div>
                  
                  <!-- 預約內容 -->
                  <div class="timeline-content">
                    
                    <!-- 🔧 最簡單的預約顯示邏輯 -->
                    <div v-if="getHourAppointments(hour).length > 0">
                      <!-- 遍歷每個預約，使用最簡單的方式 -->
                      <div v-for="(appointment, index) in getHourAppointments(hour)" 
                           :key="`${hour}-${appointment.id}-${index}`"
                           class="timeline-appointment"
                           @click="$emit('view-appointment', appointment)"
                           :style="{
                             border: appointment.isStartHour ? '2px solid #2d3436' : '2px solid #a855f7',
                             margin: '8px 0',
                             background: 'white',
                             borderRadius: '8px',
                             overflow: 'hidden',
                             boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
                             cursor: 'pointer'
                           }">
                        
                        <!-- 🔍 預約編號顯示 -->
                        <div style="background: orange; padding: 3px 8px; font-size: 11px; text-align: center; color: white; font-weight: bold;">
                          預約 #{{ appointment.id }}
                        </div>
                        
                        <!--   開始小時的完整顯示 -->
                        <div v-if="appointment.isStartHour" 
                             style="background: linear-gradient(135deg, #2d3436 0%, #636e72 100%); padding: 12px; color: white;">
                          <div style="font-weight: 600; font-size: 16px; margin-bottom: 8px;">{{ appointment.title }}</div>
                          <div style="margin-bottom: 6px; color: #f8f9fa;">
                            <iconify-icon icon="ph:clock" style="margin-right: 6px;"></iconify-icon>
                            {{ formatTime(appointment.start) }} - {{ formatTime(appointment.end) }}
                            <span style="margin-left: 8px; background: rgba(255,255,255,0.2); color: white; padding: 2px 6px; border-radius: 8px; font-size: 11px;">
                              {{ getDurationInHours(appointment) }} 小時
                            </span>
                          </div>
                          <div style="color: #55efc4; font-weight: 500;">狀態: {{ appointment.statusDisplay }}</div>
                          <div v-if="appointment.memberName" style="color: #f8f9fa; margin-top: 4px;">
                            <iconify-icon icon="ph:user" style="margin-right: 6px;"></iconify-icon>
                            {{ appointment.memberName }}
                          </div>
                        </div>
                        
                        <!--   延續小時的簡化顯示 -->
                        <div v-else 
                             style="background: linear-gradient(135deg, #ddd6fe 0%, #f1f5f9 100%); padding: 8px 12px; border-left: 4px solid #a855f7;">
                          <div style="display: flex; justify-content: space-between; align-items: center;">
                            <span style="font-weight: 500; color: #2d3436; font-size: 14px;">{{ appointment.title }}</span>
                            <span style="font-size: 12px; color: #636e72;">
                              延續至 {{ hour }}:00
                            </span>
                          </div>
                        </div>
                        
                      </div>
                    </div>
                    
                    <!-- 空時段提示 -->
                    <div v-else class="empty-slot" style="padding: 16px; text-align: center; color: #b2bec3;">
                      <iconify-icon icon="ph:calendar-x" style="font-size: 24px; margin-bottom: 4px; display: block;"></iconify-icon>
                      <span style="font-size: 13px;">空閒時段</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 月檢視 - 列表檢視 -->
        <div v-else class="schedule-list-view">
          <div 
            v-for="(daySchedule, date) in filteredScheduleByDate" 
            :key="date"
            class="day-schedule-group mb-4"
          >
            <div class="day-header bg-light p-3 rounded-top border">
              <div class="row align-items-center">
                <div class="col">
                  <h6 class="mb-0 fw-semibold">{{ formatDateHeader(date) }}</h6>
                  <div class="text-sm text-secondary-light">
                    {{ daySchedule.length }} 項預約 • 
                    {{ calculateDayHours(daySchedule) }} 小時
                  </div>
                </div>
                <div class="col-auto">
                  <span class="badge bg-primary-focus text-primary-main">
                    {{ getDayOfWeek(date) }}
                  </span>
                </div>
              </div>
            </div>
            
            <div class="day-appointments border-start border-end border-bottom rounded-bottom">
              <div 
                v-for="appointment in daySchedule" 
                :key="appointment.id"
                class="appointment-item border-bottom p-3"
                @click="$emit('view-appointment', appointment)"
              >
                <div class="row align-items-center">
                  <div class="col-md-8">
                    <div class="d-flex align-items-center gap-3">
                      <!-- 時間指示器 -->
                      <div class="time-indicator">
                        <div class="fw-semibold text-primary">{{ formatTime(appointment.start) }}</div>
                        <div class="text-sm text-secondary-light">{{ formatTime(appointment.end) }}</div>
                      </div>
                      
                      <!-- 預約資訊 -->
                      <div class="appointment-info">
                        <div class="fw-medium">{{ appointment.title }}</div>
                        <div class="text-sm text-secondary-light mb-1">
                          <iconify-icon icon="ph:clock" class="me-1"></iconify-icon>
                          {{ appointment.duration }} 小時 • {{ appointment.serviceLocation }}
                        </div>
                        <div v-if="appointment.notes" class="text-sm text-secondary-light">
                          <iconify-icon icon="ph:note" class="me-1"></iconify-icon>
                          {{ appointment.notes }}
                        </div>
                      </div>
                    </div>
                  </div>
                  
                  <div class="col-md-4 text-md-end">
                    <div class="d-flex flex-column gap-2 align-items-md-end">
                      <!-- 狀態標籤 -->
                      <span :class="`badge bg-${appointment.statusColor}-focus text-${appointment.statusColor}-main`">
                        {{ appointment.statusDisplay }}
                      </span>
                      
                      <!-- 類型標籤 -->
                      <span :class="`badge bg-${appointment.typeColor}-focus text-${appointment.typeColor}-main`">
                        {{ appointment.typeDisplay }}
                      </span>
                      
                      <!-- 金額 (僅客戶預約顯示) -->
                      <div v-if="!appointment.isBlocked && appointment.totalAmount > 0" class="text-sm fw-medium text-success">
                        ${{ appointment.totalAmount }}
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
// 引入必要的函式
import { ref, computed, watch, onMounted } from 'vue';
import { scheduleService } from '@/services/caregiverScheduleService.js';

// 定義 props
const props = defineProps({
  schedule: {
    type: Array,
    default: () => []
  },
  scheduleByDate: {
    type: Object,
    default: () => ({})
  },
  caregivers: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  },
  error: {
    type: String,
    default: null
  },
  filters: {
    type: Object,
    default: () => ({
      viewType: 'week',
      showBlockedSlots: true,
      showCustomerAppointments: true,
      statusFilter: ''
    })
  },
  dateRange: {
    type: Object,
    default: () => ({
      startDate: '',
      endDate: ''
    })
  }
});

// 定義事件
const emit = defineEmits([
  'caregiver-changed',
  'view-type-changed',
  'period-changed',
  'filters-changed',
  'view-appointment',
  'retry'
]);

// 響應式資料
const selectedCaregiverId = ref('');

// 檢視類型選項
const viewTypes = ref([
  { value: 'week', label: '週' },
  { value: 'month', label: '月' },
  { value: 'day', label: '日' }
]);

// 工作時間 (6:00 - 24:00)
const workingHours = ref([6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23]);

// 日檢視的工作時間（更詳細的時間段）
const dayWorkingHours = ref(Array.from({ length: 18 }, (_, i) => i + 6)); // 6:00 - 23:00

// 計算屬性 - 過濾後的班表資料 - 修正版本
const filteredScheduleByDate = computed(() => {
  const filtered = {};
  const originalData = props.scheduleByDate;

  console.log('🔍 ScheduleTable - 計算過濾後的班表資料:', {
    originalDataKeys: Object.keys(originalData),
    totalDays: Object.keys(originalData).length,
    filters: props.filters,
    viewType: props.filters.viewType,
    showBlockedSlots: props.filters.showBlockedSlots,
    showCustomerAppointments: props.filters.showCustomerAppointments,
    statusFilter: props.filters.statusFilter
  });

  // 🔧 如果原始資料為空，直接返回空物件並記錄日誌
  if (!originalData || Object.keys(originalData).length === 0) {
    console.log('⚠️ 原始班表資料為空:', originalData);
    return filtered;
  }

  Object.keys(originalData).forEach(date => {
    const daySchedule = originalData[date];
    
    console.log(`📅 處理日期 ${date}:`, {
      originalCount: daySchedule.length,
      appointments: daySchedule.map(item => ({
        id: item.id,
        title: item.title,
        isBlocked: item.isBlocked,
        status: item.status
      }))
    });

    console.log('📊 日別班表過濾前資料:', daySchedule.length, '筆');
    console.log('📊 過濾器設定:', {
      showBlockedSlots: props.filters.showBlockedSlots,
      showCustomerAppointments: props.filters.showCustomerAppointments
    });
    
    const filteredDaySchedule = daySchedule.filter(item => {
      let show = true;
      let filterReason = [];
      
      console.log('🔍 檢查項目:', {
        id: item.id,
        isBlocked: item.isBlocked,
        blockType: item.blockType,
        scheduledAt: item.scheduledAt,
        status: item.status
      });
      
      // 🔧 檢查時間鎖定過濾條件
      if (!props.filters.showBlockedSlots && item.isBlocked) {
        show = false;
        filterReason.push('時間鎖定被過濾');
        console.log('❌ 項目被過濾 (時間鎖定):', item.id);
      }
      
      // 🔧 檢查客戶預約過濾條件
      if (!props.filters.showCustomerAppointments && !item.isBlocked) {
        show = false;
        filterReason.push('客戶預約被過濾');
        console.log('❌ 項目被過濾 (客戶預約):', item.id);
      }
      
      if (show) {
        console.log('✅ 項目通過過濾:', item.id);
      }
      
      // 🔧 檢查狀態過濾條件
      if (props.filters.statusFilter && item.status !== props.filters.statusFilter) {
        show = false;
        filterReason.push(`狀態不符 (${item.status} != ${props.filters.statusFilter})`);
        console.log('❌ 項目被過濾 (狀態不符):', item.id);
      }

      // 🔧 記錄過濾結果
      if (!show) {
        console.log(`❌ 預約 ${item.id} 被過濾:`, filterReason.join(', '), {
          isBlocked: item.isBlocked,
          status: item.status,
          title: item.title
        });
      }

      return show;
    });

    if (filteredDaySchedule.length > 0) {
      filtered[date] = filteredDaySchedule;
      console.log(`✅ ${date}: ${filteredDaySchedule.length} 個預約通過過濾`, filteredDaySchedule.map(item => ({
        id: item.id,
        title: item.title,
        isBlocked: item.isBlocked,
        timeSlot: item.timeSlotDisplay
      })));
    } else {
      console.log(`⚠️ ${date}: 所有預約都被過濾掉了`);
    }
  });

  console.log('✅ 過濾後的班表資料:', {
    filteredDays: Object.keys(filtered).length,
    totalAppointments: Object.values(filtered).reduce((sum, appointments) => sum + appointments.length, 0),
    filteredDates: Object.keys(filtered)
  });

  return filtered;
});

// 計算屬性 - 按小時組織的預約資料
const appointmentsByHour = computed(() => {
  const result = {};
  
  console.log('🔄 重新計算 appointmentsByHour...');
  
  // 為每個工作小時創建預約列表
  dayWorkingHours.value.forEach(hour => {
    result[hour] = [];
    
    // 檢查每個日期的預約
    Object.keys(filteredScheduleByDate.value).forEach(date => {
      const daySchedule = filteredScheduleByDate.value[date];
      
      console.log(`🕐 檢查 ${hour}:00 時段，該日有 ${daySchedule.length} 個預約`);
      
      // 檢查該日的每個預約是否跨越這個小時
      daySchedule.forEach(appointment => {
        const startTime = new Date(appointment.start);
        const endTime = new Date(appointment.end);
        
        console.log(`🔍 預約 ${appointment.id}:`, {
          start: startTime.toLocaleString(),
          end: endTime.toLocaleString(),
          startHour: startTime.getHours(),
          endHour: endTime.getHours(),
          checkingHour: hour
        });
        
        // 檢查預約是否跨越這個小時
        const hourStart = new Date(startTime.getDate ? startTime : new Date(startTime));
        hourStart.setHours(hour, 0, 0, 0);
        const hourEnd = new Date(hourStart);
        hourEnd.setHours(hour + 1, 0, 0, 0);
        
        const hasOverlap = (startTime < hourEnd && endTime > hourStart);
        const isStartHour = startTime.getHours() === hour;
        
        console.log(`⏰ 時間重疊檢查:`, {
          hourStart: hourStart.toLocaleString(),
          hourEnd: hourEnd.toLocaleString(),
          hasOverlap: hasOverlap,
          isStartHour: isStartHour
        });
        
        if (hasOverlap) {
          // 添加小時資訊到預約物件
          result[hour].push({
            ...appointment,
            currentHour: hour,
            isStartHour: isStartHour
          });
          
          console.log(`✅ 預約 ${appointment.id} 添加到 ${hour}:00 時段`);
        }
      });
    });
    
    console.log(`📊 ${hour}:00 時段最終有 ${result[hour].length} 個預約`);
  });
  
  console.log('📅 按小時組織的預約資料:', result);
  return result;
});

// 計算屬性 - 當前期間文字
const currentPeriodText = computed(() => {
  console.log('🔍 計算 currentPeriodText:', {
    viewType: props.filters.viewType,
    startDate: props.dateRange.startDate,
    endDate: props.dateRange.endDate
  });
  
  if (!props.dateRange.startDate) {
    console.log('⚠️ dateRange.startDate 為空');
    return '';
  }
  
  const start = new Date(props.dateRange.startDate);
  const end = new Date(props.dateRange.endDate);
  
  if (isNaN(start.getTime()) || isNaN(end.getTime())) {
    console.log('⚠️ 日期格式無效:', props.dateRange);
    return '';
  }
  
  switch (props.filters.viewType) {
    case 'week':
      const weekNumber = getWeekNumber(start);
      console.log('📅 週檢視:', weekNumber);
      return `第 ${weekNumber} 週`;
    case 'month':
      const year = start.getFullYear();
      const month = start.getMonth() + 1;
      console.log('📅 月檢視:', year, '年', month, '月');
      return `${year} 年 ${month} 月`;
    case 'day':
      console.log('📅 日檢視');
      return '單日檢視';
    default:
      console.log('⚠️ 未知檢視類型:', props.filters.viewType);
      return '';
  }
});

// 計算屬性 - 日期範圍文字
const dateRangeText = computed(() => {
  console.log('🔍 計算 dateRangeText:', {
    startDate: props.dateRange.startDate,
    endDate: props.dateRange.endDate
  });
  
  if (!props.dateRange.startDate || !props.dateRange.endDate) {
    console.log('⚠️ 日期範圍資料不完整');
    return '';
  }
  
  const start = formatDate(props.dateRange.startDate);
  const end = formatDate(props.dateRange.endDate);
  
  if (start === end) {
    console.log('📅 單日範圍:', start);
    return start;
  }
  
  console.log('📅 日期範圍:', start, '~', end);
  return `${start} ~ ${end}`;
});

// 計算屬性 - 週檢視的日期 - 修正版本
const weekDays = computed(() => {
  console.log('🔍 計算週檢視日期:', {
    viewType: props.filters.viewType,
    startDate: props.dateRange.startDate,
    endDate: props.dateRange.endDate
  });

  if (props.filters.viewType !== 'week' || !props.dateRange.startDate) {
    console.log('⚠️ 不是週檢視或沒有開始日期');
    return [];
  }

  const days = [];
  const startDate = new Date(props.dateRange.startDate);
  const today = new Date().toDateString();
  
  console.log('📅 週檢視開始日期:', startDate.toISOString());
  
  for (let i = 0; i < 7; i++) {
    const date = new Date(startDate);
    date.setDate(startDate.getDate() + i);
    
    const dateKey = date.toISOString().split('T')[0];
    
    const dayData = {
      date: dateKey,
      dayName: getDayOfWeek(dateKey, true),
      dateText: `${date.getMonth() + 1}/${date.getDate()}`,
      isToday: date.toDateString() === today
    };
    
    days.push(dayData);
    
    console.log(`📅 週檢視日期 ${i}: ${dateKey} (${dayData.dayName})`);
  }
  
  console.log('✅ 週檢視日期計算完成:', days.map(d => ({ date: d.date, dayName: d.dayName })));
  
  return days;
});

// 處理照服員變更
const handleCaregiverChange = () => {
  emit('caregiver-changed', parseInt(selectedCaregiverId.value));
};

// 切換檢視類型
const changeViewType = (viewType) => {
  emit('view-type-changed', viewType);
};

// 上一期間
const previousPeriod = () => {
  emit('period-changed', 'previous');
};

// 下一期間
const nextPeriod = () => {
  emit('period-changed', 'next');
};

// 重置篩選
const resetFilters = () => {
  emit('filters-changed', 'reset');
};

// 重試載入
const retryLoad = () => {
  emit('retry');
};

// 獲取指定時間段的預約 - 修正版本
const getAppointmentsForTimeSlot = (date, hour) => {
  const daySchedule = filteredScheduleByDate.value[date] || [];
  
  console.log(`🔍 檢查時間段 ${date} ${hour}:00 的預約:`, {
    dayScheduleCount: daySchedule.length,
    appointments: daySchedule.map(apt => ({
      id: apt.id,
      title: apt.title,
      start: apt.start,
      end: apt.end,
      isBlocked: apt.isBlocked
    }))
  });
  
  const matchingAppointments = daySchedule.filter(appointment => {
    const startTime = new Date(appointment.start);
    const endTime = new Date(appointment.end);
    
    // 🔧 使用台灣時區工具建立時間段
    const slotStart = scheduleService.createTWDateTime(date, hour);
    const slotEnd = scheduleService.createTWDateTime(date, hour + 1);
    
    // 檢查預約是否在此時間段內，包含跨時段的預約
    const overlaps = scheduleService.isTimeOverlap(startTime, endTime, slotStart, slotEnd);
    
    console.log(`🕐 預約 ${appointment.id} 時間檢查 (台灣時區):`, {
      appointment: scheduleService.debugTimeInfo(startTime),
      appointmentEnd: scheduleService.debugTimeInfo(endTime),
      slot: scheduleService.debugTimeInfo(slotStart),
      slotEnd: scheduleService.debugTimeInfo(slotEnd),
      overlaps
    });
    
    return overlaps;
  });
  
  if (matchingAppointments.length > 0) {
    console.log(`✅ 時間段 ${date} ${hour}:00 找到 ${matchingAppointments.length} 個預約:`, 
      matchingAppointments.map(apt => apt.title));
  }
  
  return matchingAppointments;
};

// 計算一天的總工時
const calculateDayHours = (daySchedule) => {
  const totalHours = daySchedule.reduce((sum, appointment) => {
    return sum + (appointment.duration || 0);
  }, 0);
  
  return totalHours.toFixed(1);
};

// 格式化日期標題
const formatDateHeader = (dateString) => {
  const date = new Date(dateString);
  return date.toLocaleDateString('zh-TW', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  });
};

// 格式化日期 (MM/DD)
const formatDate = (dateString) => {
  const date = new Date(dateString);
  return `${date.getMonth() + 1}/${date.getDate()}`;
};

// 格式化時間 (HH:mm)
const formatTime = (dateTimeString) => {
  const date = new Date(dateTimeString);
  return date.toLocaleTimeString('zh-TW', {
    hour: '2-digit',
    minute: '2-digit',
    hour12: false
  });
};

// 獲取星期幾
const getDayOfWeek = (dateString, short = false) => {
  const date = new Date(dateString);
  const days = short 
    ? ['日', '一', '二', '三', '四', '五', '六']
    : ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六'];
  
  return days[date.getDay()];
};

// 獲取週數
const getWeekNumber = (date) => {
  const startOfYear = new Date(date.getFullYear(), 0, 1);
  const pastDaysOfYear = (date - startOfYear) / 86400000;
  return Math.ceil((pastDaysOfYear + startOfYear.getDay() + 1) / 7);
};

// 日檢視相關方法

/**
 * 檢查指定小時是否有預約
 * @param {Array} daySchedule 該日的預約列表
 * @param {number} hour 小時 (0-23)
 */
const hasAppointmentAtHour = (daySchedule, hour) => {
  return daySchedule.some(appointment => {
    const startTime = new Date(appointment.start);
    const endTime = new Date(appointment.end);
    
    // 檢查預約是否跨越這個小時
    const hourStart = new Date(startTime);
    hourStart.setHours(hour, 0, 0, 0);
    const hourEnd = new Date(hourStart);
    hourEnd.setHours(hour + 1, 0, 0, 0);
    
    return (startTime < hourEnd && endTime > hourStart);
  });
};

/**
 * 獲取指定小時的所有預約（直接從計算屬性獲取）
 * @param {number} hour 小時 (0-23)
 */
const getHourAppointments = (hour) => {
  console.log(`🔍 getHourAppointments 呼叫: ${hour}:00 時段`);
  const appointments = appointmentsByHour.value[hour] || [];
  console.log(`📊 ${hour}:00 時段返回 ${appointments.length} 個預約:`, appointments);
  return appointments;
};

/**
 * 計算預約服務時長（以小時為單位）
 * @param {Object} appointment 預約物件
 */
const getDurationInHours = (appointment) => {
  const startTime = new Date(appointment.start);
  const endTime = new Date(appointment.end);
  const durationMs = endTime - startTime;
  const durationHours = durationMs / (1000 * 60 * 60); // 轉換為小時
  
  // 如果是整數小時，顯示整數；否則顯示一位小數
  return durationHours % 1 === 0 ? durationHours.toString() : durationHours.toFixed(1);
};

/**
 * 獲取指定小時的預約
 * @param {Array} daySchedule 該日的預約列表
 * @param {number} hour 小時 (0-23)
 */
const getAppointmentsAtHour = (daySchedule, hour) => {
  const appointments = daySchedule.filter(appointment => {
    const startTime = new Date(appointment.start);
    const endTime = new Date(appointment.end);
    
    // 📅 檢查預約是否跨越這個小時 (與 hasAppointmentAtHour 邏輯一致)
    const hourStart = new Date(startTime);
    hourStart.setHours(hour, 0, 0, 0);
    const hourEnd = new Date(hourStart);
    hourEnd.setHours(hour + 1, 0, 0, 0);
    
    // 預約時間與該小時有重疊就顯示
    const hasOverlap = (startTime < hourEnd && endTime > hourStart);
    
    // 🔍 調試日誌
    if (daySchedule.length > 0) {
      console.log(`🕐 檢查 ${hour}:00 時段:`, {
        appointmentId: appointment.id,
        appointmentTitle: appointment.title,
        startTime: startTime.toLocaleString(),
        endTime: endTime.toLocaleString(),
        hourStart: hourStart.toLocaleString(),
        hourEnd: hourEnd.toLocaleString(),
        hasOverlap: hasOverlap
      });
    }
    
    return hasOverlap;
  });
  
  // 🔍 調試結果
  if (appointments.length > 0) {
    console.log(`✅ ${hour}:00 時段找到 ${appointments.length} 個預約:`, appointments.map(a => a.title));
  }
  
  return appointments;
};

/**
 * 判斷預約是否在指定小時開始
 * @param {Object} appointment 預約物件  
 * @param {number} hour 小時 (0-23)
 */
const isAppointmentStartHour = (appointment, hour) => {
  const startTime = new Date(appointment.start);
  const result = startTime.getHours() === hour;
  
  // 🔍 調試日誌
  console.log(`🎯 isAppointmentStartHour 檢查:`, {
    appointmentId: appointment.id,
    hour: hour,
    startHour: startTime.getHours(),
    isStartHour: result
  });
  
  return result;
};

/**
 * 獲取預約在當前小時的位置樣式類別
 * @param {Object} appointment 預約物件
 * @param {number} hour 小時 (0-23)
 */
const getAppointmentPositionClass = (appointment, hour) => {
  const startTime = new Date(appointment.start);
  const endTime = new Date(appointment.end);
  const startHour = startTime.getHours();
  const endHour = endTime.getHours();
  
  if (hour === startHour && hour === endHour) {
    return 'appointment-single-hour'; // 單小時預約
  } else if (hour === startHour) {
    return 'appointment-start'; // 預約開始小時
  } else if (hour === endHour) {
    return 'appointment-end'; // 預約結束小時  
  } else {
    return 'appointment-middle'; // 預約中間小時
  }
};

/**
 * 格式化預約在當前小時的時間範圍
 * @param {Object} appointment 預約物件
 * @param {number} hour 小時 (0-23)
 */
const formatHourRange = (appointment, hour) => {
  const startTime = new Date(appointment.start);
  const endTime = new Date(appointment.end);
  const startHour = startTime.getHours();
  const endHour = endTime.getHours();
  
  if (hour === startHour) {
    return `從 ${formatTime(appointment.start)}`;
  } else if (hour === endHour) {
    return `到 ${formatTime(appointment.end)}`;
  } else {
    return `${hour}:00 - ${hour + 1}:00`;
  }
};

/**
 * 格式化小時顯示
 * @param {number} hour 小時
 */
const formatHour = (hour) => {
  return `${hour.toString().padStart(2, '0')}:00`;
};

/**
 * 獲取預約的樣式（用於時間軸顯示）
 * @param {Object} appointment 預約物件
 * @param {number} currentHour 當前顯示的小時
 */
const getAppointmentStyle = (appointment, currentHour) => {
  const startTime = new Date(appointment.start);
  const endTime = new Date(appointment.end);
  const duration = (endTime - startTime) / (1000 * 60 * 60); // 以小時為單位
  
  // 如果預約跨越多個小時，只在開始小時顯示
  if (startTime.getHours() !== currentHour) {
    return { display: 'none' };
  }
  
  // 計算預約的高度（基於持續時間）
  const baseHeight = 60; // 基礎高度 60px
  const height = Math.max(baseHeight, duration * baseHeight);
  
  return {
    height: `${height}px`,
    minHeight: `${baseHeight}px`
  };
};

/**
 * 獲取鎖定類型的顯示文字
 * @param {string} blockType 鎖定類型
 */
const getBlockTypeDisplay = (blockType) => {
  const blockTypeMap = {
    'break': '休息時間',
    'maintenance': '系統維護', 
    'personal': '個人事務',
    'unavailable': '不可用時間',
    'holiday': '假期',
    'training': '培訓時間'
  };
  
  return blockTypeMap[blockType] || '未知類型';
};

// 監聽篩選條件變化
watch(
  () => props.filters,
  (newFilters) => {
    // 篩選變化時可以觸發重新渲染
  },
  { deep: true }
);

// 當照服員列表載入時，設定預設選項
watch(
  () => props.caregivers,
  (newCaregivers) => {
    if (newCaregivers.length > 0 && !selectedCaregiverId.value) {
      // 可以設定預設選擇第一個照服員
      // selectedCaregiverId.value = newCaregivers[0].id;
    }
  }
);

// 元件掛載時的處理
onMounted(() => {
  console.log('班表元件已掛載');
});
</script>

<style scoped>
/* 班表樣式 */
.schedule-table {
  font-size: 0.875rem;
}

.schedule-table th,
.schedule-table td {
  padding: 0.5rem;
  vertical-align: top;
}

.schedule-cell {
  height: 60px;
  min-width: 120px;
  position: relative;
  cursor: pointer;
}

.schedule-item {
  position: absolute;
  top: 2px;
  left: 2px;
  right: 2px;
  bottom: 2px;
  padding: 4px 6px;
  border-radius: 4px;
  font-size: 0.75rem;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.2s ease;
}

.schedule-item:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.schedule-title {
  font-weight: 600;
  line-height: 1.2;
  margin-bottom: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.schedule-time {
  font-size: 0.7rem;
  opacity: 0.8;
  margin-bottom: 2px;
}

.schedule-status {
  position: absolute;
  bottom: 2px;
  right: 2px;
}

.schedule-status .badge {
  font-size: 0.6rem;
  padding: 1px 4px;
}

/* 預約類型顏色 */
.schedule-primary {
  background-color: var(--bs-primary-50);
  border-color: var(--bs-primary-200);
}

.schedule-secondary {
  background-color: var(--bs-secondary-50);
  border-color: var(--bs-secondary-200);
}

/* 日程列表樣式 */
.day-schedule-group {
  border-radius: 0.5rem;
  overflow: hidden;
}

.day-header {
  border-bottom: none !important;
}

.appointment-item {
  transition: background-color 0.2s ease;
  cursor: pointer;
}

.appointment-item:hover {
  background-color: var(--bs-gray-50);
}

.appointment-item:last-child {
  border-bottom: none !important;
}

.time-indicator {
  text-align: center;
  min-width: 60px;
  padding: 0.5rem;
  border-right: 2px solid var(--bs-primary);
  margin-right: 0.5rem;
}

.appointment-info {
  flex-grow: 1;
}

/* 統計卡片樣式 */
.bg-primary-50 {
  background-color: rgba(13, 110, 253, 0.1);
}

.bg-success-50 {
  background-color: rgba(25, 135, 84, 0.1);
}

.bg-warning-50 {
  background-color: rgba(255, 193, 7, 0.1);
}

.bg-info-50 {
  background-color: rgba(13, 202, 240, 0.1);
}

.bg-secondary-50 {
  background-color: rgba(108, 117, 125, 0.1);
}

.bg-dark-50 {
  background-color: rgba(33, 37, 41, 0.1);
}

/* 響應式設計 */
@media (max-width: 768px) {
  .schedule-table {
    font-size: 0.75rem;
  }
  
  .schedule-cell {
    height: 50px;
    min-width: 100px;
  }
  
  .schedule-item {
    padding: 2px 4px;
  }
  
  .schedule-title {
    font-size: 0.7rem;
  }
  
  .time-indicator {
    min-width: 50px;
  }
}

/* 深色模式支援 */
@media (prefers-color-scheme: dark) {
  .schedule-item {
    background-color: var(--bs-dark);
    border-color: var(--bs-gray-600);
  }
  
  .appointment-item:hover {
    background-color: var(--bs-gray-800);
  }
}

/* 日檢視時間軸樣式 */
.schedule-day-view .single-day-schedule {
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  margin-bottom: 2rem;
}

.timeline-container {
  max-height: 70vh;
  overflow-y: auto;
}

.timeline {
  position: relative;
}

.timeline-slot {
  display: flex;
  border-bottom: 1px solid #e9ecef;
  min-height: 80px;
  transition: background-color 0.2s ease;
}

.timeline-slot:hover {
  background-color: #f8f9fa;
}

.timeline-slot.has-appointment {
  background-color: rgba(13, 110, 253, 0.02);
}

.timeline-time {
  flex-shrink: 0;
  width: 80px;
  padding: 1rem 0.5rem;
  border-right: 2px solid #e9ecef;
  text-align: center;
  background-color: #f8f9fa;
}

.timeline-time .hour {
  font-weight: 600;
  color: #495057;
  font-size: 0.875rem;
}

.timeline-content {
  flex-grow: 1;
  padding: 1rem;
  position: relative;
}

.empty-slot {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  min-height: 48px;
  font-style: italic;
}

.timeline-appointment {
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  border: 2px solid #dee2e6;
  border-radius: 8px;
  padding: 0.75rem;
  margin-bottom: 0.5rem;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.timeline-appointment:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  border-color: #0d6efd;
}

.timeline-appointment:last-child {
  margin-bottom: 0;
}

/* 預約類型樣式 */
.appointment-primary {
  border-color: #0d6efd;
  background: linear-gradient(135deg, #e7f1ff 0%, #f0f8ff 100%);
}

.appointment-primary:hover {
  border-color: #0a58ca;
  background: linear-gradient(135deg, #d4e9ff 0%, #e7f1ff 100%);
}

.appointment-secondary {
  border-color: #6c757d;
  background: linear-gradient(135deg, #f1f3f4 0%, #f8f9fa 100%);
}

.appointment-secondary:hover {
  border-color: #565e64;
  background: linear-gradient(135deg, #e9ecef 0%, #f1f3f4 100%);
}

.appointment-success {
  border-color: #198754;
  background: linear-gradient(135deg, #e8f5e8 0%, #f0f9f0 100%);
}

.appointment-success:hover {
  border-color: #146c43;
  background: linear-gradient(135deg, #d1f2d1 0%, #e8f5e8 100%);
}

/* 🕐 跨小時預約的位置樣式 */
.appointment-start {
  border-bottom-left-radius: 4px !important;
  border-bottom-right-radius: 4px !important;
  margin-bottom: 0 !important;
}

.appointment-middle {
  border-radius: 4px !important;
  margin-top: 0 !important;
  margin-bottom: 0 !important;
  border-top: 1px dashed #dee2e6 !important;
  border-bottom: 1px dashed #dee2e6 !important;
}

.appointment-end {
  border-top-left-radius: 4px !important;
  border-top-right-radius: 4px !important;
  margin-top: 0 !important;
}

.appointment-single-hour {
  /* 單小時預約保持預設樣式 */
  border-radius: 8px;
}

/* 🔗 預約延續指示器樣式 */
.appointment-continuation {
  background: linear-gradient(135deg, rgba(13, 110, 253, 0.1) 0%, rgba(13, 110, 253, 0.05) 100%);
  border: 1px dashed #0d6efd;
  border-radius: 4px;
  padding: 0.5rem;
  margin-bottom: 0.25rem;
  opacity: 0.8;
}

.continuation-indicator {
  display: flex;
  align-items: center;
  font-size: 0.875rem;
  color: #0d6efd;
}

.appointment-title-small {
  font-weight: 500;
  margin-right: 0.5rem;
}

/* 📋 完整預約資訊樣式 */
.appointment-full {
  /* 完整預約資訊的容器 */
  width: 100%;
}

.appointment-warning {
  border-color: #ffc107;
  background: linear-gradient(135deg, #fff8e1 0%, #fffbf0 100%);
}

.appointment-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 0.5rem;
}

.appointment-title {
  font-weight: 600;
  color: #212529;
  font-size: 0.9rem;
  line-height: 1.3;
}

.appointment-details {
  font-size: 0.8rem;
  color: #6c757d;
}

.appointment-details > div {
  margin-bottom: 0.25rem;
  display: flex;
  align-items: center;
}

.appointment-details > div:last-child {
  margin-bottom: 0;
}

.appointment-time {
  font-weight: 500;
  color: #495057;
  margin-bottom: 0.5rem;
}

.appointment-info,
.appointment-block-info {
  margin-bottom: 0.5rem;
}

.appointment-notes {
  font-style: italic;
  background-color: rgba(0, 0, 0, 0.02);
  padding: 0.5rem;
  border-radius: 4px;
  border-left: 3px solid #0d6efd;
}

.badge-sm {
  font-size: 0.7rem;
  padding: 0.25rem 0.5rem;
}

/* 響應式設計 - 日檢視 */
@media (max-width: 768px) {
  .timeline-time {
    width: 60px;
    padding: 0.5rem 0.25rem;
  }
  
  .timeline-content {
    padding: 0.5rem;
  }
  
  .timeline-appointment {
    padding: 0.5rem;
  }
  
  .appointment-title {
    font-size: 0.85rem;
  }
  
  .appointment-details {
    font-size: 0.75rem;
  }
  
  .timeline-container {
    max-height: 60vh;
  }
}
</style>