<template>
  <div class="appointment-form">
    <!-- 表單標題 -->
    <div class="form-header">
      <h3 class="form-title">
        <i class="fas fa-calendar-plus me-2"></i>
        填寫預約資料
      </h3>
      <p class="form-description">(會員姓名及電話已帶入)</p>
    </div>

    <!-- 載入狀態 -->
    <div v-if="isLoading" class="loading-section">
      <div class="text-center py-4">
        <div class="spinner-border spinner-border-sm text-primary" role="status">
          <span class="sr-only">載入中...</span>
        </div>
        <p class="mt-2 mb-0">載入資料中...</p>
      </div>
    </div>

    <!-- 錯誤提示 -->
    <div v-if="error" class="alert alert-danger">
      <i class="fas fa-exclamation-triangle me-2"></i>
      {{ error }}
    </div>

    <!-- 成功提示 -->
    <div v-if="successMessage" class="alert alert-success">
      <i class="fas fa-check-circle me-2"></i>
      {{ successMessage }}
    </div>

    <!-- 預約表單 -->
    <form @submit.prevent="handleSubmit" class="appointment-form-content">
      
      <!-- 預約須知區塊 -->
      <div class="form-section">
        <h5 class="section-title">
          <i class="fas fa-info-circle me-2"></i>
          預約須知
        </h5>
        <div class="booking-guidelines-card">
          <div class="guidelines-list">
            <div class="guideline-item">
              <i class="fas fa-clock text-primary"></i>
              <span>預約需提前24小時，當天預約恕不受理</span>
            </div>
            
            <div class="guideline-item">
              <i class="fas fa-calendar-check text-success"></i>
              <span>預約成功後將有專人聯繫確認服務細節</span>
            </div>
            
            <div class="guideline-item">
              <i class="fas fa-hand-holding-usd text-warning"></i>
              <span>服務費用採現場支付或轉帳方式</span>
            </div>
            
            <div class="guideline-item">
              <i class="fas fa-phone-alt text-info"></i>
              <span>如需取消或變更，請提前4小時聯繫</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 照服員資訊區塊 -->
      <div class="form-section">
        <h5 class="section-title">
          <i class="fas fa-user-nurse me-2"></i>
          照服員資訊
        </h5>
        <div class="caregiver-info-card">
          <div class="row align-items-center">
            <div class="col-auto">
              <img 
                :src="getCaregiverImage(caregiverInfo?.imagePath || caregiverInfo?.photo || caregiverInfo?.avatar)" 
                :alt="caregiverInfo?.chineseName"
                class="caregiver-avatar"
                @error="handleImageError"
              />
            </div>
            <div class="col">
              <h6 class="caregiver-name">{{ caregiverInfo.chineseName || '載入中...' }}</h6>
              <div class="caregiver-details">
                <span class="detail-item">
                  <i class="fas fa-map-marker-alt text-primary me-1"></i>
                  {{ caregiverInfo.serviceArea || '載入中...' }}
                </span>
                <span class="detail-item">
                  <i class="fas fa-medal text-success me-1"></i>
                  {{ caregiverInfo.experienceYears || 0 }}年經驗
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 服務類型選擇 -->
      <div class="form-section">
        <h5 class="section-title">
          <i class="fas fa-hand-holding-heart me-2"></i>
          服務類型
        </h5>
        <div class="form-group">
          <label class="form-label required">選擇服務類型</label>
          <select 
            v-model="formData.serviceTypeId" 
            class="form-select"
            :class="{ 'is-invalid': errors.serviceTypeId }"
            @change="handleServiceTypeChange"
          >
            <option value="">請選擇服務類型</option>
            <option 
              v-for="serviceType in serviceTypes" 
              :key="serviceType.id" 
              :value="serviceType.id"
            >
              {{ serviceType.displayText }}
            </option>
          </select>
          <div v-if="errors.serviceTypeId" class="invalid-feedback">
            {{ errors.serviceTypeId }}
          </div>
        </div>
      </div>

      <!-- 日期時間選擇 -->
      <div id="booking-time-section" class="form-section">
        <h5 class="section-title">
          <i class="fas fa-clock me-2"></i>
          預約時間
        </h5>
        
        <!-- 日期選擇 -->
        <div class="form-group">
          <label class="form-label required">選擇日期</label>
          <input 
            type="date"
            v-model="formData.selectedDate"
            class="form-control"
            :class="{ 'is-invalid': errors.selectedDate }"
            :min="minDate"
            :max="maxDate"
            @change="handleDateChange"
          />
          <div v-if="errors.selectedDate" class="invalid-feedback">
            {{ errors.selectedDate }}
          </div>
        </div>

        <!-- 時間選擇 -->
        <div v-if="formData.selectedDate" class="form-group">
          <label class="form-label required">選擇服務時間</label>
          
          <div class="time-picker-container">
            <!-- 開始時間 -->
            <div class="time-input-group">
              <label class="time-label">開始時間</label>
              <select 
                v-model="formData.startTime"
                class="form-select"
                :class="{ 'is-invalid': errors.startTime }"
                @change="handleStartTimeChange"
              >
                <option value="">請選擇</option>
                <option 
                  v-for="time in availableTimeSlots"
                  :key="time.value"
                  :value="time.value"
                  :disabled="time.disabled"
                >
                  {{ time.label }}
                </option>
              </select>
            </div>
            
            <!-- 結束時間 -->
            <div class="time-input-group">
              <label class="time-label">結束時間</label>
              <select 
                v-model="formData.endTime"
                class="form-select"
                :class="{ 'is-invalid': errors.endTime }"
                @change="handleEndTimeChange"
                :disabled="!formData.startTime"
              >
                <option value="">請選擇</option>
                <option 
                  v-for="time in availableEndTimes"
                  :key="time.value"
                  :value="time.value"
                  :disabled="time.disabled"
                >
                  {{ time.label }}
                </option>
              </select>
            </div>
          </div>
          
          <!-- 時間衝突提示 -->
          <div v-if="timeConflictMessage" class="alert alert-warning mt-2">
            <i class="fas fa-exclamation-triangle me-2"></i>
            {{ timeConflictMessage }}
          </div>
          
          <!-- 預約衝突警告 -->
          <div v-if="reservationConflictMessage" class="conflict-warning mt-2">
            <div class="warning-message text-danger mb-2">
              {{ reservationConflictMessage }}
            </div>
            <!-- 顯示當日可用時段 -->
            <div v-if="availableTimeSlotsForDay.length > 0" class="available-slots-info">
              <small class="text-muted">
                <i class="fas fa-info-circle me-1"></i>
                當日可預約時段：
                <span v-for="(slot, index) in availableTimeSlotsForDay" :key="slot" class="available-slot">
                  {{ slot }}{{ index < availableTimeSlotsForDay.length - 1 ? '、' : '' }}
                </span>
              </small>
            </div>
          </div>
          
          <!-- 服務時間超過限制警告 -->
          <div v-if="serviceTimeExceedsLimitMessage" class="warning-message text-danger mt-2">
            {{ serviceTimeExceedsLimitMessage }}
          </div>
          
          <!-- 服務時長顯示 -->
          <div v-if="formData.startTime && formData.endTime" class="service-duration-info mt-2">
            <small class="text-muted">
              <i class="fas fa-clock me-1"></i>
              服務時長：{{ calculateServiceHours }} 小時
            </small>
          </div>
          
          <!-- 錯誤訊息 -->
          <div v-if="errors.startTime" class="invalid-feedback d-block">
            {{ errors.startTime }}
          </div>
          <div v-if="errors.endTime" class="invalid-feedback d-block">
            {{ errors.endTime }}
          </div>
        </div>
      </div>

      <!-- 服務地點 -->
      <div class="form-section">
        <h5 class="section-title">
          <i class="fas fa-map-marker-alt me-2"></i>
          服務地點
        </h5>
        <div class="form-group">
          <label class="form-label required">服務地址</label>
          <input 
            type="text"
            v-model="formData.serviceLocation"
            class="form-control"
            :class="{ 'is-invalid': errors.serviceLocation }"
            placeholder="請輸入詳細地址，例如：桃園市中壢區中大路300號"
            maxlength="200"
          />
          <div v-if="errors.serviceLocation" class="invalid-feedback">
            {{ errors.serviceLocation }}
          </div>
          <small class="form-text text-muted">
            請提供準確的服務地址，以便照服員準時到達
          </small>
        </div>
      </div>

      <!-- 備註資訊 -->
      <div class="form-section">
        <h5 class="section-title">
          <i class="fas fa-sticky-note me-2"></i>
          備註資訊
        </h5>
        <div class="form-group">
          <label class="form-label"></label>
          <textarea
            v-model="formData.notes"
            class="form-control"
            rows="4"
            placeholder="請描述特殊需求、注意事項或其他相關資訊..."
            maxlength="500"
          ></textarea>
          <div class="character-count">
            {{ formData.notes.length }} / 500
          </div>
        </div>
      </div>

      <!-- 價格預覽 -->
      <div v-if="priceCalculation && priceCalculation.success" class="form-section">
        <h5 class="section-title">
          <i class="fas fa-calculator me-2"></i>
          費用預覽
        </h5>
        <div class="price-preview-card">
          <div class="price-details">
            <div class="price-row">
              <span class="label">服務類型：</span>
              <span class="value">{{ priceCalculation.serviceType?.serviceName }}</span>
            </div>
            <div class="price-row">
              <span class="label">服務時數：</span>
              <span class="value">{{ priceCalculation.serviceHours }}小時</span>
            </div>
            <div class="price-row">
              <span class="label">時薪：</span>
              <span class="value">NT$ {{ priceCalculation.hourlyRate?.toLocaleString() || '計算中...' }}</span>
            </div>
            <div class="price-row total">
              <span class="label">總金額：</span>
              <span class="value">NT$ {{ priceCalculation.totalAmount?.toLocaleString() || '計算中...' }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 提交按鈕 -->
      <div class="form-actions">
        <button 
          type="button"
          class="btn btn-secondary me-3"
          @click="handleCancel"
          :disabled="submitting"
        >
          <i class="fas fa-times me-1"></i>
          取消
        </button>
        
        <button 
          type="submit"
          class="btn btn-primary"
          :disabled="!isFormValid || submitting"
        >
          <span v-if="submitting">
            <i class="fas fa-spinner fa-spin me-1"></i>
            提交中...
          </span>
          <span v-else>
            <i class="fas fa-paper-plane me-1"></i>
            提交預約
          </span>
        </button>
      </div>
    </form>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAppointment } from '@/composables/useAppointments'

export default {
  name: 'AppointmentForm',
  props: {
    caregiverId: {
      type: [Number, String],
      required: true
    },
    caregiverInfo: {
      type: Object,
      default: () => ({})
    }
  },
  emits: ['success', 'cancel'],
  setup(props, { emit }) {
    const router = useRouter()
    
    // 使用預約組合函數
    const {
      loading,
      submitting,
      error,
      successMessage,
      serviceTypes,
      priceCalculation,
      isLoading,
      currentMember, // 添加 currentMember
      loadServiceTypes,
      loadCurrentMember, // 添加 loadCurrentMember
      calculatePrice,
      createAppointment,
      clearError
    } = useAppointment()

    // 表單資料
    const formData = reactive({
      caregiverId: parseInt(props.caregiverId),
      serviceTypeId: '',
      selectedDate: '',
      startTime: '',
      endTime: '',
      serviceLocation: '',
      notes: ''
    })

    // 表單驗證錯誤
    const errors = reactive({})
    
    // 時間衝突提示訊息
    const timeConflictMessage = ref('')
    
    // 預約衝突警告訊息
    const reservationConflictMessage = ref('')
    
    // 服務時間超過限制警告訊息
    const serviceTimeExceedsLimitMessage = ref('')
    
    // 可用時段列表
    const availableTimeSlotsForDay = ref([])

    // 可用時間選項 (每小時一個選項，從 06:00 到 23:00)
    const availableTimeSlots = computed(() => {
      const slots = []
      for (let hour = 6; hour <= 23; hour++) {
        const timeValue = `${hour.toString().padStart(2, '0')}:00`
        const timeLabel = `${hour.toString().padStart(2, '0')}:00`
        
        slots.push({
          value: timeValue,
          label: timeLabel,
          disabled: false // 後續可加入已預約的時間檢查
        })
      }
      return slots
    })

    // 可用結束時間選項（根據開始時間動態計算）
    const availableEndTimes = computed(() => {
      if (!formData.startTime) return []
      
      const startHour = parseInt(formData.startTime.split(':')[0])
      const slots = []
      
      // 從開始時間後1小時開始，到當天23:00
      for (let hour = startHour + 1; hour <= 23; hour++) {
        const timeValue = `${hour.toString().padStart(2, '0')}:00`
        const timeLabel = `${hour.toString().padStart(2, '0')}:00`
        
        slots.push({
          value: timeValue,
          label: timeLabel,
          disabled: false
        })
      }
      
      return slots
    })

    // 計算服務時長
    const calculateServiceHours = computed(() => {
      if (!formData.startTime || !formData.endTime) return 0
      
      const startHour = parseInt(formData.startTime.split(':')[0])
      const endHour = parseInt(formData.endTime.split(':')[0])
      
      return endHour - startHour
    })

    // 計算屬性
    const minDate = computed(() => {
      const tomorrow = new Date()
      tomorrow.setDate(tomorrow.getDate() + 1)
      return tomorrow.toISOString().split('T')[0]
    })

    const maxDate = computed(() => {
      const maxDate = new Date()
      maxDate.setDate(maxDate.getDate() + 30) // 最多30天後
      return maxDate.toISOString().split('T')[0]
    })

    const isFormValid = computed(() => {
      return formData.serviceTypeId &&
             formData.selectedDate &&
             formData.startTime &&
             formData.endTime &&
             formData.serviceLocation.trim() &&
             Object.keys(errors).length === 0 &&
             !timeConflictMessage.value &&
             !reservationConflictMessage.value &&
             !serviceTimeExceedsLimitMessage.value
    })

    // 方法
    const clearErrors = () => {
      Object.keys(errors).forEach(key => {
        delete errors[key]
      })
    }

    const clearWarnings = () => {
      reservationConflictMessage.value = ''
      serviceTimeExceedsLimitMessage.value = ''
      availableTimeSlotsForDay.value = []
    }

    const validateForm = () => {
      clearErrors()
      clearWarnings()
      
      if (!formData.serviceTypeId) {
        errors.serviceTypeId = '請選擇服務類型'
      }
      
      if (!formData.selectedDate) {
        errors.selectedDate = '請選擇日期'
      }
      
      if (!formData.startTime) {
        errors.startTime = '請選擇開始時間'
      }
      
      if (!formData.endTime) {
        errors.endTime = '請選擇結束時間'
      }
      
      if (formData.startTime && formData.endTime) {
        // 檢查結束時間是否晚於開始時間
        const startHour = parseInt(formData.startTime.split(':')[0])
        const endHour = parseInt(formData.endTime.split(':')[0])
        
        if (endHour <= startHour) {
          errors.endTime = '結束時間必須晚於開始時間'
        }
        
        // 檢查最少服務時間（1小時）
        if (endHour - startHour < 1) {
          errors.endTime = '最少服務時間為1小時'
        }
        
        // 檢查最長服務時間（8小時） - 移到警告區域
        if (endHour - startHour > 8) {
          serviceTimeExceedsLimitMessage.value = '單次服務時間不得超過8小時'
        }
      }
      
      if (!formData.serviceLocation.trim()) {
        errors.serviceLocation = '請輸入服務地址'
      }
      
      return Object.keys(errors).length === 0 && !serviceTimeExceedsLimitMessage.value
    }

    const handleServiceTypeChange = async () => {
      clearErrors()
      clearWarnings()
      
      if (formData.serviceTypeId && formData.selectedTimeSlot && formData.serviceHours) {
        await updatePriceCalculation()
      }
    }

    const handleDateChange = async () => {
      clearErrors()
      clearWarnings()
      // 重置時間選擇
      formData.startTime = ''
      formData.endTime = ''
      timeConflictMessage.value = ''
      
      if (formData.selectedDate) {
        // 這裡可以載入該日期已預約的時間段來檢查衝突
        await checkTimeConflicts()
      }
    }

    /**
     * 處理開始時間變更
     */
    const handleStartTimeChange = () => {
      clearErrors()
      clearWarnings()
      
      // 如果結束時間早於或等於新的開始時間，清空結束時間
      if (formData.endTime && formData.startTime) {
        const startHour = parseInt(formData.startTime.split(':')[0])
        const endHour = parseInt(formData.endTime.split(':')[0])
        
        if (endHour <= startHour) { // 至少1小時差距
          formData.endTime = ''
        }
      }
      
      checkTimeConflicts()
    }

    /**
     * 處理結束時間變更
     */
    const handleEndTimeChange = () => {
      clearErrors()
      clearWarnings()
      checkTimeConflicts()
      
      // 如果有完整的時間資訊，更新價格計算
      if (formData.serviceTypeId && formData.startTime && formData.endTime) {
        updatePriceCalculation()
      }
    }

    /**
     * 檢查時間衝突
     */
    const checkTimeConflicts = async () => {
      timeConflictMessage.value = ''
      
      if (!formData.selectedDate || !formData.startTime || !formData.endTime) {
        return
      }

      try {
        // 檢查營業時間（假設為 6:00-23:00）
        const startHour = parseInt(formData.startTime.split(':')[0])
        const endHour = parseInt(formData.endTime.split(':')[0])
        
        if (startHour < 6) {
          timeConflictMessage.value = '服務時間不能早於上午6:00'
          return
        }
        
        if (endHour > 23) {
          timeConflictMessage.value = '服務時間不能晚於晚上11:00'
          return
        }
        
        // 這裡可以加入實際的預約衝突檢查 API 調用
        // 例如：const conflicts = await checkCaregiverAvailability(props.caregiverId, formData.selectedDate, formData.startTime, formData.endTime)
        
      } catch (error) {
        console.error('檢查時間衝突失敗:', error)
      }
    }

    /**
     * 獲取當日可用時段
     */
    const getAvailableTimeSlotsForDay = async () => {
      if (!formData.selectedDate) return []
      
      try {
        // 這裡調用 API 獲取當日可用時段
        // const result = await loadAvailableSlots(props.caregiverId, formData.selectedDate)
        
        // 暫時使用模擬數據 - 實際應用中需要調用真實的 API
        const mockAvailableSlots = [
          '06:00-07:00', '07:00-08:00', '09:00-10:00', 
          '14:00-15:00', '15:00-16:00', '20:00-21:00'
        ]
        
        return mockAvailableSlots
      } catch (error) {
        console.error('獲取可用時段失敗:', error)
        return []
      }
    }

    const updatePriceCalculation = async () => {
      if (!formData.serviceTypeId || !formData.startTime || !formData.endTime || !formData.selectedDate) {
        return
      }

      try {
        // 構建完整的日期時間字串
        const startDateTime = `${formData.selectedDate}T${formData.startTime}:00`
        const endDateTime = `${formData.selectedDate}T${formData.endTime}:00`
        
        await calculatePrice(formData.serviceTypeId, startDateTime, endDateTime)
      } catch (error) {
        // 錯誤已由 useAppointments 處理，這裡不需要重複日誌
      }
    }

    const handleSubmit = async () => {
      if (!validateForm()) {
        // 如果表單驗證失敗且有時間相關錯誤，滾動到預約時間區域
        if (errors.selectedDate || errors.startTime || errors.endTime || 
            serviceTimeExceedsLimitMessage.value || reservationConflictMessage.value) {
          scrollToTimeSection()
        }
        return
      }

      try {
        // 構建完整的預約開始時間 (組合日期和時間)
        const scheduledAt = `${formData.selectedDate}T${formData.startTime}:00`
        const endTime = `${formData.selectedDate}T${formData.endTime}:00`

        const appointmentData = {
          memberId: currentMember.value?.memberId || currentMember.value?.id, // 添加 memberId
          caregiverId: formData.caregiverId,
          serviceTypeId: formData.serviceTypeId,
          scheduledAt: scheduledAt,
          endTime: endTime,
          serviceLocation: formData.serviceLocation.trim(),
          notes: formData.notes.trim()
        }

        const result = await createAppointment(appointmentData)
        
        if (result && result.success) {
          emit('success', result)
          
          // 3秒後自動跳轉到預約列表
          setTimeout(() => {
            router.push('/my-appointments')
          }, 3000)
        }
      } catch (err) {
        // 檢查是否為預約衝突錯誤
        if (err.message && err.message.includes('該時段已有其他預約')) {
          reservationConflictMessage.value = '預約失敗：該時段已有其他預約'
          // 載入當日可用時段
          availableTimeSlotsForDay.value = await getAvailableTimeSlotsForDay()
          // 滾動到預約時間區域
          scrollToTimeSection()
        } else if (err.message && err.message.includes('時間不可用')) {
          reservationConflictMessage.value = '預約失敗：該時段已有其他預約'
          // 載入當日可用時段
          availableTimeSlotsForDay.value = await getAvailableTimeSlotsForDay()
          // 滾動到預約時間區域
          scrollToTimeSection()
        } else {
          // 對於其他時間相關錯誤，也滾動到預約時間區域
          if (err.message && (err.message.includes('時間') || err.message.includes('預約'))) {
            scrollToTimeSection()
          }
        }
        // 其他錯誤已由 useAppointments 處理和顯示
      }
    }

    const handleCancel = () => {
      emit('cancel')
      router.go(-1) // 返回上一頁
    }

    /**
     * 滾動到預約時間區域
     */
    const scrollToTimeSection = () => {
      const timeSection = document.getElementById('booking-time-section')
      if (timeSection) {
        timeSection.scrollIntoView({ 
          behavior: 'smooth', 
          block: 'start' 
        })
      }
    }

    /**
     * 取得照服員圖片路徑（參考 CaregiverList 實作）
     * @param {string} imagePath - 圖片路徑
     * @returns {string} 完整的圖片URL
     */
    const getCaregiverImage = (imagePath) => {
      if (!imagePath) {
        try {
          return require('@/assets/img/thumb/error.png')
        } catch (err) {
          return '/src/assets/img/thumb/error.png'
        }
      }
      
      // 如果是完整的 URL
      if (imagePath.startsWith('http://') || imagePath.startsWith('https://')) {
        return imagePath
      }
      
      // 如果是 Base64 編碼的圖片數據
      if (imagePath.startsWith('data:image')) {
        return imagePath
      }
      
      // 統一處理相對路徑（參考後台 CaregiverTable.vue 的實作）
      // 移除可能的前導斜線，然後加上正確的基礎 URL
      const cleanPath = imagePath.startsWith('/') ? imagePath : '/' + imagePath
      const fullPath = `http://localhost:8080${cleanPath}`
      
      return fullPath
    }

    /**
     * 處理圖片載入錯誤（參考 CaregiverList 實作）
     * @param {Event} event - 錯誤事件
     */
    const handleImageError = (event) => {
      try {
        event.target.src = require('@/assets/img/thumb/error.png')
      } catch (err) {
        // 作為備用方案，使用一個簡單的 data URL 圖片
        event.target.src = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgdmlld0JveD0iMCAwIDIwMCAyMDAiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+CjxyZWN0IHdpZHRoPSIyMDAiIGhlaWdodD0iMjAwIiBmaWxsPSIjRjNGNEY2Ii8+CjxwYXRoIGQ9Ik0xMDAgNjBDMTE0LjMgNjAgMTI2IDcxLjcgMTI2IDg2QzEyNiAxMDAuMyAxMTQuMyAxMTIgMTAwIDExMkM4NS43IDExMiA3NCAxMDAuMyA3NCA4NkM3NCA3MS43IDg1LjcgNjAgMTAwIDYwWiIgZmlsbD0iIzlDQTNBRiIvPgo8cGF0aCBkPSJNNzQgMTQwSDEyNkMxMzAgMTQwIDEzNCAxNDAuOSAxMzYuOCAxNDMuMkMxMzkuMSAxNDYgMTQwIDE0OSAxNDAgMTUzVjE2NEg2MFYxNTNDNjAgMTQ5IDYwLjkgMTQ2IDYzLjIgMTQzLjJDNjYgMTQwLjkgNjkgMTQwIDc0IDE0MFoiIGZpbGw9IiM5Q0EzQUYiLz4KPC9zdmc+'
      }
    }

    // 監聽器
    watch(() => props.caregiverId, (newId) => {
      if (newId) {
        formData.caregiverId = parseInt(newId)
      }
    }, { immediate: true })

    // 初始化
    onMounted(async () => {
      clearError()
      
      // 載入當前會員資料
      await loadCurrentMember()
      
      // 載入服務類型列表
      await loadServiceTypes(true)
    })

    return {
      // 響應式資料
      formData,
      errors,
      timeConflictMessage,
      reservationConflictMessage,
      serviceTimeExceedsLimitMessage,
      availableTimeSlotsForDay,
      
      // 來自組合函數的狀態
      loading,
      submitting,
      error,
      successMessage,
      serviceTypes,
      priceCalculation,
      isLoading,
      
      // 計算屬性
      minDate,
      maxDate,
      isFormValid,
      availableTimeSlots,
      availableEndTimes,
      calculateServiceHours,
      
      // 方法
      handleServiceTypeChange,
      handleDateChange,
      handleStartTimeChange,
      handleEndTimeChange,
      handleSubmit,
      handleCancel,
      getCaregiverImage,
      handleImageError,
      scrollToTimeSection
    }
  }
}
</script>

<style scoped>
.appointment-form {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.form-header {
  text-align: center;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 2px solid #e9ecef;
}

.form-title {
  color: #333;
  font-weight: 600;
  margin-bottom: 8px;
}

.form-description {
  color: #666;
  margin-bottom: 0;
}

.loading-section {
  margin: 20px 0;
}

.appointment-form-content {
  background: white;
  border-radius: 12px;
  padding: 0;
}

.form-section {
  margin-bottom: 30px;
  padding: 20px;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  background: #fafbfc;
}

.section-title {
  color: #333;
  font-weight: 600;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid #e9ecef;
}

/* 預約須知樣式 */
.booking-guidelines-card {
  background: white;
  padding: 16px;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.guidelines-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.guideline-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  font-size: 0.9rem;
  line-height: 1.4;
}

.guideline-item i {
  margin-top: 2px;
  flex-shrink: 0;
  width: 16px;
}

.guideline-item span {
  color: #555;
}

.caregiver-info-card {
  background: white;
  padding: 16px;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.caregiver-name {
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.caregiver-details {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.caregiver-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #e9ecef;
}

.detail-item {
  font-size: 0.9rem;
  color: #666;
  display: flex;
  align-items: center;
}

.form-group {
  margin-bottom: 16px;
}

.form-label {
  font-weight: 500;
  color: #333;
  margin-bottom: 6px;
}

.form-label.required::after {
  content: " *";
  color: #dc3545;
}

.form-control,
.form-select {
  border: 1px solid #e1e5e9;
  border-radius: 6px;
  padding: 10px 12px;
  transition: all 0.3s ease;
}

.form-control:focus,
.form-select:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 0.2rem rgba(102, 126, 234, 0.25);
}

.form-control.is-invalid,
.form-select.is-invalid {
  border-color: #dc3545;
}

.invalid-feedback {
  color: #dc3545;
  font-size: 0.875rem;
  margin-top: 4px;
}

.time-slots-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 12px;
  margin-top: 12px;
}

/* 新的時間選擇器樣式 */
.time-picker-container {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-top: 12px;
}

.time-input-group {
  display: flex;
  flex-direction: column;
}

.time-label {
  font-weight: 500;
  color: #495057;
  margin-bottom: 6px;
  font-size: 0.9rem;
}

.service-duration-info {
  background: #e3f2fd;
  border: 1px solid #bbdefb;
  border-radius: 6px;
  padding: 8px 12px;
  display: inline-block;
}

.service-duration-info small {
  color: #1976d2;
  font-weight: 500;
}

.warning-message {
  color: #dc3545 !important;
  font-size: 14px;
  font-weight: 500;
}

.conflict-warning {
  background: #f8f9fa;
  border: 1px solid #e9ecef;
  border-radius: 6px;
  padding: 12px;
}

.available-slots-info {
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid #e9ecef;
}

.available-slot {
  display: inline-block;
  background: #e3f2fd;
  color: #1976d2;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
  margin: 0 2px;
}

.time-slot-option {
  background: white;
  border: 2px solid #e9ecef;
  border-radius: 8px;
  padding: 12px 16px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.time-slot-option:hover:not(.disabled) {
  border-color: #667eea;
  background: #f8f9ff;
}

.time-slot-option.selected {
  border-color: #667eea;
  background: #667eea;
  color: white;
}

.time-slot-option.disabled {
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
  font-size: 0.8rem;
  opacity: 0.8;
}

.no-slots-message {
  text-align: center;
  padding: 40px 20px;
  background: white;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.character-count {
  text-align: right;
  font-size: 0.8rem;
  color: #666;
  margin-top: 4px;
}

.price-preview-card {
  background: white;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  padding: 16px;
}

.price-details {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.price-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 4px 0;
}

.price-row.total {
  border-top: 1px solid #e9ecef;
  padding-top: 8px;
  margin-top: 8px;
  font-weight: 600;
  font-size: 1.1rem;
}

.price-row .label {
  color: #666;
}

.price-row .value {
  color: #333;
  font-weight: 500;
}

.price-row.total .value {
  color: #667eea;
  font-size: 1.2rem;
}

.form-actions {
  background: white;
  padding: 20px;
  border-top: 1px solid #e9ecef;
  text-align: center;
  border-radius: 0 0 12px 12px;
}

/* 覆蓋全域 btn 樣式，確保正確顯示 */
.form-actions .btn {
  position: relative;
  display: inline-block;
  border-radius: 6px;
  padding: 10px 24px;
  font-weight: 500;
  transition: all 0.3s ease;
  cursor: pointer;
  border: 1px solid transparent;
  text-align: center;
  font-size: 1rem;
  line-height: 1.5;
  text-decoration: none;
  overflow: visible; /* 覆蓋全域設定 */
}

/* 移除全域 btn 的 ::before 偽元素效果 */
.form-actions .btn::before {
  display: none !important;
}

.form-actions .btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
  border: none !important;
  color: white !important;
}

.form-actions .btn-primary:hover:not(:disabled) {
  background: linear-gradient(135deg, #5a6fd8 0%, #6a4190 100%) !important;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.form-actions .btn-primary:disabled {
  background: #e9ecef !important;
  color: #6c757d !important;
  opacity: 0.6;
  transform: none;
  box-shadow: none;
}

.form-actions .btn-secondary {
  background: #f8f9fa !important;
  border: 1px solid #dee2e6 !important;
  color: #6c757d !important;
}

.form-actions .btn-secondary:hover:not(:disabled) {
  background: #e9ecef !important;
  border-color: #dee2e6 !important;
  color: #495057 !important;
}

/* 響應式設計 */
@media (max-width: 768px) {
  .appointment-form {
    padding: 16px;
  }
  
  .caregiver-details {
    flex-direction: column;
    gap: 8px;
  }
  
  .time-slots-grid {
    grid-template-columns: 1fr;
  }
  
  .time-picker-container {
    grid-template-columns: 1fr;
    gap: 12px;
  }
  
  .form-actions {
    padding: 16px;
  }
  
  .form-actions .btn {
    width: 100%;
    margin: 4px 0;
  }
  
  .price-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 2px;
  }
}

@media (max-width: 480px) {
  .form-section {
    padding: 16px;
  }
  
  .caregiver-info-card {
    padding: 12px;
  }
  
  .caregiver-avatar {
    width: 50px;
    height: 50px;
  }
}

/* 無障礙設計 */
@media (prefers-reduced-motion: reduce) {
  .btn,
  .time-slot-option,
  .form-control,
  .form-select {
    transition: none;
  }
}

/* 高對比度模式支援 */
@media (prefers-contrast: high) {
  .form-section {
    border: 2px solid #000;
  }
  
  .btn-primary {
    background: #000;
    color: #fff;
  }
}
</style>