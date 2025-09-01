<template>
  <div class="schedule-picker">
    <!-- 月份導航 -->
    <div class="month-navigation">
      <div class="nav-header">
        <button class="btn btn-outline-primary btn-sm" @click="prevMonth">
          <i class="fas fa-chevron-left"></i>
        </button>
        <h6 class="month-title">{{ monthDisplay }}</h6>
        <button class="btn btn-outline-primary btn-sm" @click="nextMonth">
          <i class="fas fa-chevron-right"></i>
        </button>
      </div>
      <button class="btn btn-link btn-sm" @click="goToToday">
        回到今天
      </button>
    </div>

    <!-- 載入狀態 -->
    <div v-if="loading" class="text-center py-4">
      <div class="spinner-border spinner-border-sm text-primary" role="status">
        <span class="sr-only">載入中...</span>
      </div>
      <p class="mt-2 mb-0">載入行程中...</p>
    </div>

    <!-- 錯誤狀態 -->
    <div v-else-if="error" class="alert alert-warning">
      <i class="fas fa-exclamation-triangle me-2"></i>
      {{ error }}
    </div>

    <!-- 日曆網格 -->
    <div v-else class="calendar-section">
      <!-- 本月日曆 -->
      <div class="month-calendar">
        <h6 class="calendar-title">{{ monthDisplay }}</h6>
        <div class="calendar-grid">
          <!-- 星期標題 -->
          <div class="weekday-headers">
            <div class="weekday-header">日</div>
            <div class="weekday-header">一</div>
            <div class="weekday-header">二</div>
            <div class="weekday-header">三</div>
            <div class="weekday-header">四</div>
            <div class="weekday-header">五</div>
            <div class="weekday-header">六</div>
          </div>
          
          <!-- 日期網格 -->
          <div class="dates-grid">
            <div 
              v-for="dateInfo in currentMonthDates" 
              :key="dateInfo.date"
              class="date-cell"
              :class="getDateClasses(dateInfo)"
              @click="selectDate(dateInfo.date)"
            >
              <span class="date-number">{{ dateInfo.day }}</span>
              <div class="date-indicators">
                <div 
                  v-if="getDateStatus(dateInfo.date).hasSchedule"
                  class="schedule-indicator"
                  :class="{
                    'available': getDateStatus(dateInfo.date).availableCount > 0,
                    'busy': getDateStatus(dateInfo.date).isFullyBooked
                  }"
                ></div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 下月日曆 -->
      <div class="month-calendar">
        <h6 class="calendar-title">{{ nextMonthDisplay }}</h6>
        <div class="calendar-grid">
          <!-- 星期標題 -->
          <div class="weekday-headers">
            <div class="weekday-header">日</div>
            <div class="weekday-header">一</div>
            <div class="weekday-header">二</div>
            <div class="weekday-header">三</div>
            <div class="weekday-header">四</div>
            <div class="weekday-header">五</div>
            <div class="weekday-header">六</div>
          </div>
          
          <!-- 日期網格 -->
          <div class="dates-grid">
            <div 
              v-for="dateInfo in nextMonthDates" 
              :key="dateInfo.date"
              class="date-cell"
              :class="getDateClasses(dateInfo)"
              @click="selectDate(dateInfo.date)"
            >
              <span class="date-number">{{ dateInfo.day }}</span>
              <div class="date-indicators">
                <div 
                  v-if="getDateStatus(dateInfo.date).hasSchedule"
                  class="schedule-indicator"
                  :class="{
                    'available': getDateStatus(dateInfo.date).availableCount > 0,
                    'busy': getDateStatus(dateInfo.date).isFullyBooked
                  }"
                ></div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 時間段選擇 -->
    <div v-if="selectedDate" class="time-slots-section">
      <h6 class="section-title">
        <i class="fas fa-clock me-2"></i>
        選擇時間段 - {{ formatSelectedDate() }}
      </h6>
      
      <div v-if="selectedDateSlots.length > 0" class="time-slots-grid">
        <div 
          v-for="slot in selectedDateSlots" 
          :key="slot.id"
          class="time-slot"
          :class="{
            'selected': selectedTimeSlot?.id === slot.id,
            'disabled': !slot.isAvailable
          }"
          @click="selectTimeSlot(slot)"
        >
          <div class="time-text">{{ slot.timeSlot }}</div>
          <div class="status-text">{{ slot.statusDisplay }}</div>
        </div>
      </div>
      
      <div v-else class="no-slots text-center text-muted py-3">
        <i class="fas fa-calendar-times fa-2x mb-2"></i>
        <p>此日期沒有可預約的時間段</p>
        <small>請選擇其他日期</small>
      </div>
    </div>

    <!-- 選擇結果顯示 -->
    <div v-if="selectedTimeInfo" class="selection-summary">
      <div class="alert alert-success">
        <h6 class="alert-heading">
          <i class="fas fa-check-circle me-2"></i>
          已選擇時間
        </h6>
        <div class="selection-details">
          <div class="detail-item">
            <strong>服務日期：</strong>
            {{ formatDate(selectedTimeInfo.date) }}
          </div>
          <div class="detail-item">
            <strong>服務時間：</strong>
            {{ selectedTimeInfo.timeSlot }}
          </div>
          <div class="detail-item">
            <strong>服務時長：</strong>
            {{ formatDuration(selectedTimeInfo.duration) }}
          </div>
        </div>
        <button class="btn btn-outline-success btn-sm mt-2" @click="clearSelection">
          <i class="fas fa-times me-1"></i>
          重新選擇
        </button>
      </div>
    </div>

    <!-- 圖例說明 -->
    <div class="legend">
      <h6 class="legend-title">圖例說明</h6>
      <div class="legend-items">
        <div class="legend-item">
          <span class="legend-color available"></span>
          <span>有可預約時間</span>
        </div>
        <div class="legend-item">
          <span class="legend-color busy"></span>
          <span>已滿預約</span>
        </div>
        <div class="legend-item">
          <span class="legend-color no-schedule"></span>
          <span>無排班</span>
        </div>
        <div class="legend-item">
          <span class="legend-color past"></span>
          <span>過去日期</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { computed, onMounted, watch } from 'vue'
import { useCaregiverSchedule } from '@/composables/useCaregiverSchedule'

export default {
  name: 'CaregiverSchedulePicker',
  props: {
    caregiverId: {
      type: [Number, String],
      required: true
    }
  },
  emits: ['time-selected', 'time-cleared'],
  setup(props, { emit }) {
    const {
      // 狀態
      selectedDate,
      selectedTimeSlot,
      loading,
      error,
      currentMonth,
      
      // 計算屬性
      currentMonthDates,
      nextMonthDates,
      selectedDateSlots,
      monthDisplay,
      nextMonthDisplay,
      
      // 方法
      loadCurrentAndNextMonth,
      selectDate,
      selectTimeSlot,
      prevMonth,
      nextMonth,
      goToToday,
      getDateStatus,
      getDateClasses,
      clearSelection,
      getSelectedTimeInfo
    } = useCaregiverSchedule()

    // 計算屬性：選中的時間資訊
    const selectedTimeInfo = computed(() => getSelectedTimeInfo())

    /**
     * 初始化載入行程
     */
    onMounted(async () => {
      if (props.caregiverId) {
        await loadCurrentAndNextMonth(parseInt(props.caregiverId))
      }
    })

    /**
     * 監聽照服員ID變化
     */
    watch(
      () => props.caregiverId,
      async (newId) => {
        if (newId) {
          clearSelection()
          await loadCurrentAndNextMonth(parseInt(newId))
        }
      }
    )

    /**
     * 監聽時間選擇變化
     */
    watch(
      selectedTimeInfo,
      (newTimeInfo) => {
        if (newTimeInfo) {
          emit('time-selected', newTimeInfo)
        } else {
          emit('time-cleared')
        }
      }
    )

    /**
     * 格式化選中的日期
     */
    const formatSelectedDate = () => {
      if (!selectedDate.value) return ''
      const date = new Date(selectedDate.value)
      return date.toLocaleDateString('zh-TW', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        weekday: 'long'
      })
    }

    /**
     * 格式化日期
     */
    const formatDate = (dateString) => {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleDateString('zh-TW', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        weekday: 'short'
      })
    }

    /**
     * 格式化時長
     */
    const formatDuration = (duration) => {
      if (!duration) return '0分鐘'
      
      const hours = Math.floor(duration / 60)
      const minutes = duration % 60
      
      if (hours > 0 && minutes > 0) {
        return `${hours}小時${minutes}分鐘`
      } else if (hours > 0) {
        return `${hours}小時`
      } else {
        return `${minutes}分鐘`
      }
    }

    return {
      // 狀態
      selectedDate,
      selectedTimeSlot,
      loading,
      error,
      currentMonth,
      
      // 計算屬性
      currentMonthDates,
      nextMonthDates,
      selectedDateSlots,
      monthDisplay,
      nextMonthDisplay,
      selectedTimeInfo,
      
      // 方法
      selectDate,
      selectTimeSlot,
      prevMonth,
      nextMonth,
      goToToday,
      getDateStatus,
      getDateClasses,
      clearSelection,
      formatSelectedDate,
      formatDate,
      formatDuration
    }
  }
}
</script>

<style scoped>
.schedule-picker {
  background: white;
  border-radius: 12px;
  padding: 20px;
  border: 1px solid #e9ecef;
}

.month-navigation {
  display: flex;
  justify-content: between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e9ecef;
}

.nav-header {
  display: flex;
  align-items: center;
  gap: 16px;
}

.month-title {
  font-weight: 600;
  color: #333;
  margin: 0;
  min-width: 120px;
  text-align: center;
}

.btn-link {
  color: #667eea;
  text-decoration: none;
  font-size: 0.85rem;
}

.btn-link:hover {
  color: #5a6fd8;
}

.calendar-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  margin-bottom: 24px;
}

.month-calendar {
  border: 1px solid #e9ecef;
  border-radius: 8px;
  overflow: hidden;
}

.calendar-title {
  background: #f8f9fa;
  padding: 12px 16px;
  margin: 0;
  font-weight: 600;
  color: #333;
  border-bottom: 1px solid #e9ecef;
  text-align: center;
}

.calendar-grid {
  padding: 16px;
}

.weekday-headers {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 1px;
  margin-bottom: 8px;
}

.weekday-header {
  text-align: center;
  font-weight: 600;
  color: #666;
  padding: 8px 4px;
  font-size: 0.85rem;
}

.dates-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 1px;
}

.date-cell {
  position: relative;
  aspect-ratio: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border: 1px solid transparent;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
  background: #f8f9fa;
}

.date-cell:hover:not(.past-date):not(.no-schedule) {
  background: #e3f2fd;
  border-color: #2196f3;
}

.date-cell.selected {
  background: #667eea;
  color: white;
  font-weight: 600;
}

.date-cell.today {
  border: 2px solid #667eea;
  font-weight: 600;
}

.date-cell.past-date {
  background: #f5f5f5;
  color: #999;
  cursor: not-allowed;
}

.date-cell.weekend {
  background: #fff3e0;
}

.date-cell.has-available {
  background: #e8f5e8;
  border-color: #4caf50;
}

.date-cell.fully-booked {
  background: #ffebee;
  border-color: #f44336;
}

.date-cell.no-schedule {
  background: #f5f5f5;
  color: #ccc;
  cursor: not-allowed;
}

.date-number {
  font-size: 0.9rem;
}

.date-indicators {
  position: absolute;
  bottom: 2px;
  right: 2px;
}

.schedule-indicator {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #ccc;
}

.schedule-indicator.available {
  background: #4caf50;
}

.schedule-indicator.busy {
  background: #f44336;
}

.time-slots-section {
  margin-bottom: 24px;
  padding-top: 20px;
  border-top: 1px solid #e9ecef;
}

.section-title {
  font-weight: 600;
  color: #333;
  margin-bottom: 16px;
}

.time-slots-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 8px;
}

.time-slot {
  border: 1px solid #e9ecef;
  border-radius: 8px;
  padding: 12px;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s ease;
  background: white;
}

.time-slot:hover:not(.disabled) {
  background: #f0f4ff;
  border-color: #667eea;
}

.time-slot.selected {
  background: #667eea;
  color: white;
  border-color: #667eea;
}

.time-slot.disabled {
  background: #f5f5f5;
  color: #999;
  cursor: not-allowed;
  opacity: 0.6;
}

.time-text {
  font-weight: 600;
  margin-bottom: 4px;
}

.status-text {
  font-size: 0.75rem;
  opacity: 0.8;
}

.no-slots {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 32px 16px;
}

.selection-summary {
  margin-bottom: 20px;
}

.alert-success {
  border: none;
  background: linear-gradient(135deg, rgba(76, 175, 80, 0.1) 0%, rgba(139, 195, 74, 0.1) 100%);
  border-left: 4px solid #4caf50;
}

.selection-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 8px;
}

.detail-item {
  font-size: 0.9rem;
}

.legend {
  padding-top: 16px;
  border-top: 1px solid #e9ecef;
}

.legend-title {
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
  font-size: 0.9rem;
}

.legend-items {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 8px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.8rem;
  color: #666;
}

.legend-color {
  width: 12px;
  height: 12px;
  border-radius: 3px;
  border: 1px solid #ddd;
}

.legend-color.available {
  background: #e8f5e8;
  border-color: #4caf50;
}

.legend-color.busy {
  background: #ffebee;
  border-color: #f44336;
}

.legend-color.no-schedule {
  background: #f5f5f5;
  border-color: #ccc;
}

.legend-color.past {
  background: #f5f5f5;
  border-color: #999;
}

/* 響應式設計 */
@media (max-width: 768px) {
  .schedule-picker {
    padding: 16px;
  }
  
  .calendar-section {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .month-navigation {
    flex-direction: column;
    gap: 8px;
  }
  
  .nav-header {
    gap: 12px;
  }
  
  .month-title {
    min-width: 100px;
  }
  
  .time-slots-grid {
    grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  }
  
  .legend-items {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 480px) {
  .date-cell {
    font-size: 0.8rem;
  }
  
  .weekday-header {
    font-size: 0.75rem;
    padding: 6px 2px;
  }
  
  .time-slots-grid {
    grid-template-columns: 1fr;
  }
}
</style>
