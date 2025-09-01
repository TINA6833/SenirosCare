<template>
  <div class="appointment-detail-page">
    <!-- 頁面標題區域 -->
    <div class="page-header bg-overlay p-8 sm:p-10 before:bg-title before:bg-opacity-70"
         :style="{ backgroundImage: 'url(' + bg + ')' }">
      <div class="container">
        <div class="page-title-section">
          <h1 class="page-title text-white">
            <i class="fas fa-file-alt me-3"></i>
            預約詳情
          </h1>
          
          <!-- 導航按鈕 -->
          <div class="navigation-buttons">
            <button 
              @click="goToHome"
              class="px-6 py-3 bg-gradient-to-r from-blue-500 to-blue-600 text-white rounded-lg hover:from-blue-600 hover:to-blue-700 transition-all duration-200 flex items-center gap-2"
            >
              <i class="fas fa-home"></i>
              <span>回首頁</span>
            </button>
            <button 
              @click="goToMyAppointments"
              class="px-6 py-3 bg-gradient-to-r from-pink-500 to-red-500 text-white rounded-lg hover:from-pink-600 hover:to-red-600 transition-all duration-200 flex items-center gap-2"
            >
              <i class="fas fa-calendar-alt"></i>
              <span>我的預約</span>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 主要內容區域 -->
    <div class="page-content">
      <div class="container">
        <!-- 載入狀態 -->
        <div v-if="loading" class="text-center py-5">
          <div class="spinner-border text-primary" role="status">
            <span class="sr-only">載入中...</span>
          </div>
          <p class="mt-2">載入預約詳情中...</p>
        </div>

        <!-- 錯誤狀態 -->
        <div v-else-if="error" class="alert alert-danger">
          <i class="fas fa-exclamation-triangle me-2"></i>
          {{ error }}
        </div>

        <!-- 預約詳情內容 -->
        <div v-else-if="currentAppointment" class="appointment-detail">
          <div class="row">
            <!-- 左側：預約資訊 -->
            <div class="col-lg-8">
              <!-- 預約基本資訊 -->
              <div class="detail-card mb-4">
                <div class="card">
                  <div class="card-header">
                    <h5 class="mb-0">
                      <i class="fas fa-calendar-alt me-2"></i>
                      預約資訊
                    </h5>
                  </div>
                  <div class="card-body">
                    <div class="row">
                      <div class="col-md-6">
                        <div class="info-group">
                          <p></p>
                        </div>
                      </div>
                      <div class="col-md-6">
                        <div class="info-group">
                          <label>訂單編號</label>
                          <p>#{{ currentAppointment.id }}</p>
                        </div>
                      </div>
                      <div class="col-md-6">
                        <div class="info-group">
                          <label>預約狀態</label>
                          <p>
                            <span 
                              class="badge"
                              :class="getStatusClass(currentAppointment.status)"
                            >
                              {{ currentAppointment.statusDisplay }}
                            </span>
                          </p>
                        </div>
                      </div>
                      <div class="col-md-6">
                        <div class="info-group">
                          <label>服務開始時間</label>
                          <p>{{ currentAppointment.formattedScheduledAt }}</p>
                        </div>
                      </div>
                      <div class="col-md-6">
                        <div class="info-group">
                          <label>服務結束時間</label>
                          <p>{{ currentAppointment.formattedEndTime }}</p>
                        </div>
                      </div>
                      <div class="col-md-6">
                        <div class="info-group">
                          <label>服務時長</label>
                          <p>{{ currentAppointment.serviceDurationDisplay }}</p>
                        </div>
                      </div>
                      <div class="col-md-6">
                        <div class="info-group">
                          <label>服務類型</label>
                          <p>{{ currentAppointment.serviceTypeName }}</p>
                        </div>
                      </div>
                      <div class="col-12">
                        <div class="info-group">
                          <label>服務地點</label>
                          <p>{{ currentAppointment.serviceLocation }}</p>
                        </div>
                      </div>
                      <div class="col-12" v-if="currentAppointment.notes">
                        <div class="info-group">
                          <label>備註說明</label>
                          <p>{{ currentAppointment.notes }}</p>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 照服員詳細資訊 -->
              <div class="caregiver-detail-card">
                <div class="card">
                  <div class="card-header">
                    <h5 class="mb-0">
                      <i class="fas fa-user-nurse me-2"></i>
                      照服員資訊
                    </h5>
                  </div>
                  <div class="card-body">
                    <div class="row align-items-center">
                      <div class="col-auto">
                        <img 
                          :src="getCaregiverImage(currentAppointment)" 
                          :alt="currentAppointment.caregiverName"
                          class="caregiver-avatar-large"
                        />
                      </div>
                      <div class="col">
                        <div class="row">
                          <div class="col-md-6">
                            <div class="info-group">
                              <label>照服員姓名</label>
                              <p class="fw-semibold">{{ currentAppointment.caregiverName }}</p>
                            </div>
                          </div>
                          <div class="col-md-6">
                            <div class="info-group">
                              <label>聯絡電話</label>
                              <p class="text-primary">
                                <i class="fas fa-phone me-1"></i>
                                <span v-if="currentAppointment.caregiverPhone">
                                  {{ formatPhoneNumber(currentAppointment.caregiverPhone) }}
                                </span>
                                <span v-else class="text-muted">
                                  <i class="fas fa-info-circle me-1"></i>
                                  請聯繫客服 (02) 1234-5678 取得聯絡方式
                                </span>
                              </p>
                            </div>
                          </div>
                          <div class="col-md-6">
                            <div class="info-group">
                              <label>專長服務</label>
                              <p>{{ currentAppointment.serviceTypeName }}</p>
                            </div>
                          </div>
                          <div class="col-md-6">
                            <div class="info-group">
                              <label>照服員編號</label>
                              <p>#{{ currentAppointment.caregiverId }}</p>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 右側：費用資訊 -->
            <div class="col-lg-4">
              <!-- 費用資訊 -->
              <div class="price-card">
                <div class="card">
                  <div class="card-header">
                    <h6 class="mb-0">
                      <i class="fas fa-calculator me-2"></i>
                      費用資訊
                    </h6>
                  </div>
                  <div class="card-body">
                    <div class="price-total">
                      <span>總費用</span>
                      <strong>{{ currentAppointment.formattedTotalAmount }}</strong>
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

<script>
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAppointment } from '@/composables/useAppointments'
import bg from '@/assets/img/bg/footer.jpg'

export default {
  name: 'AppointmentDetail',
  props: {
    id: {
      type: [String, Number],
      required: true
    }
  },
  setup(props) {
    const route = useRoute()
    const router = useRouter()
    const {
      currentAppointment,
      loading,
      error,
      loadAppointmentDetail
    } = useAppointment()

    /**
     * 初始化載入預約詳情
     */
    onMounted(async () => {
      const appointmentId = props.id || route.params.id
      if (appointmentId) {
        await loadAppointmentDetail(parseInt(appointmentId))
      }
    })

    /**
     * 取得照服員圖片
     * @param {Object} appointment - 預約資料
     * @returns {string} 圖片路徑
     */
    const getCaregiverImage = (appointment) => {
      if (!appointment) {
        return require('@/assets/img/thumb/error.png')
      }
      
      // 檢查多種可能的圖片欄位
      const imagePath = appointment.caregiverAvatar || 
                       appointment.caregiver?.imagePath || 
                       appointment.caregiver?.photo || 
                       appointment.caregiver?.avatar ||
                       appointment.caregiver?.profileImage
      
      if (imagePath) {
        const fullPath = `http://localhost:8080${imagePath}`
        return fullPath
      } else {
        return require('@/assets/img/thumb/error.png')
      }
    }

    /**
     * 取得狀態樣式類別
     */
    const getStatusClass = (status) => {
      const statusClasses = {
        pending: 'badge-warning',
        approved: 'badge-success', 
        completed: 'badge-info',
        cancelled: 'badge-secondary',
        rejected: 'badge-danger'
      }
      return statusClasses[status] || 'badge-secondary'
    }

    /**
     * 格式化電話號碼（保護隱私）
     * @param {string} phone - 原始電話號碼
     * @returns {string} 格式化後的電話號碼
     */
    const formatPhoneNumber = (phone) => {
      if (!phone) return ''
      
      // 移除所有非數字字符
      const cleanPhone = phone.replace(/\D/g, '')
      
      // 根據不同長度進行格式化
      if (cleanPhone.length === 10) {
        // 手機號碼格式: 09XX-XXX-XXX
        return `${cleanPhone.slice(0, 4)}-${cleanPhone.slice(4, 7)}-${cleanPhone.slice(7)}`
      } else if (cleanPhone.length === 8) {
        // 市話格式: XXXX-XXXX
        return `${cleanPhone.slice(0, 4)}-${cleanPhone.slice(4)}`
      } else if (cleanPhone.length === 9 && cleanPhone.startsWith('02')) {
        // 台北市話: (02) XXXX-XXXX
        return `(02) ${cleanPhone.slice(2, 6)}-${cleanPhone.slice(6)}`
      } else {
        // 其他格式，直接返回
        return phone
      }
    }

    /**
     * 撥打照服員電話
     */
    const callCaregiver = () => {
      if (currentAppointment.value?.caregiverPhone) {
        const cleanPhone = currentAppointment.value.caregiverPhone.replace(/\D/g, '')
        window.location.href = `tel:${cleanPhone}`
      }
    }

    /**
     * 聯絡客服
     */
    const contactCustomerService = () => {
      // 這裡可以根據實際需求調整客服聯絡方式
      const customerServicePhone = '0212345678'
      
      // 可以選擇開啟電話或是其他聯絡方式
      if (confirm('是否要撥打客服電話？')) {
        window.location.href = `tel:${customerServicePhone}`
      }
    }

    /**
     * 查看照服員檔案
     */
    const viewCaregiverProfile = () => {
      if (currentAppointment.value?.caregiverId) {
        // 導航到照服員詳情頁面
        router.push(`/caregiver/${currentAppointment.value.caregiverId}`)
      }
    }

    /**
     * 回到首頁
     */
    const goToHome = () => {
      router.push('/')
    }

    /**
     * 回到我的預約頁面
     */
    const goToMyAppointments = () => {
      router.push('/my-appointments')
    }

    return {
      bg,
      currentAppointment,
      loading,
      error,
      getCaregiverImage,
      getStatusClass,
      formatPhoneNumber,
      callCaregiver,
      contactCustomerService,
      viewCaregiverProfile,
      goToHome,
      goToMyAppointments
    }
  }
}
</script>

<style scoped>
.appointment-detail-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.page-header {
  color: white;
  position: relative;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  min-height: 400px;
  display: flex;
  align-items: center;
}

.bg-overlay {
  position: relative;
}

.bg-overlay::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 1;
}

.page-header .container {
  position: relative;
  z-index: 2;
}

.page-title-section {
  text-align: center;
}

.page-title {
  font-size: 3rem;
  font-weight: 700;
  margin-bottom: 1rem;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
}

.page-subtitle {
  font-size: 1.2rem;
  opacity: 0.9;
  margin-bottom: 2rem;
}

.navigation-buttons {
  display: flex;
  gap: 1rem;
  justify-content: center;
  flex-wrap: wrap;
}

.page-content {
  margin-top: 20px;
  position: relative;
  z-index: 3;
  padding-bottom: 50px;
}

.detail-card .card,
.caregiver-detail-card .card,
.price-card .card {
  border: none;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.card-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-bottom: none;
  border-radius: 12px 12px 0 0;
}

.info-group {
  margin-bottom: 20px;
}

.info-group label {
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
  display: block;
  font-size: 0.9rem;
}

.info-group p {
  color: #555;
  margin-bottom: 0;
  font-size: 1rem;
}

.caregiver-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #e9ecef;
}

.caregiver-avatar-large {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid #e9ecef;
}

.caregiver-detail-card {
  margin-bottom: 20px;
}

.caregiver-detail-card .info-group {
  margin-bottom: 15px;
}

.caregiver-detail-card .fw-semibold {
  font-weight: 600;
  color: #2c3e50;
  font-size: 1.1rem;
}

.text-primary {
  color: #667eea !important;
}

.text-primary .fas {
  font-size: 0.9rem;
}

.text-muted {
  color: #6c757d !important;
}

.text-muted .fas {
  font-size: 0.8rem;
}

.price-total {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 1.1rem;
  padding: 15px 0;
  border-top: 2px solid #e9ecef;
  margin-top: 10px;
}

.price-total span {
  color: #6c757d;
  font-weight: 500;
}

.price-total strong {
  font-size: 1.4rem;
  color: #28a745;
  font-weight: 700;
}

.badge {
  padding: 8px 14px;
  border-radius: 8px;
  font-size: 0.85rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.badge-warning {
  background-color: #ffc107;
  color: #212529;
}

.badge-success {
  background-color: #28a745;
  color: white;
}

.badge-danger {
  background-color: #dc3545;
  color: white;
}

.badge-info {
  background-color: #17a2b8;
  color: white;
}

.badge-secondary {
  background-color: #6c757d;
  color: white;
}

/* 響應式設計 */
@media (max-width: 768px) {
  .page-header {
    min-height: 300px;
  }
  
  .page-title {
    font-size: 2rem;
  }
  
  .navigation-buttons {
    flex-direction: column;
    align-items: center;
  }
  
  .navigation-buttons button {
    width: 200px;
  }
}

/* 響應式設計 */
@media (max-width: 768px) {
  .page-header {
    padding: 30px 0;
  }
  
  .page-title {
    font-size: 1.5rem;
  }
  
  .caregiver-avatar-large {
    width: 60px;
    height: 60px;
  }
  
  .contact-actions {
    flex-direction: column;
  }
  
  .contact-actions .btn {
    width: 100%;
    margin-bottom: 8px;
  }
  
  .info-group label {
    font-size: 0.8rem;
  }
  
  .info-group p {
    font-size: 0.9rem;
  }
}

@media (max-width: 576px) {
  .row.align-items-center {
    text-align: center;
  }
  
  .caregiver-avatar-large {
    margin: 0 auto 15px;
    display: block;
  }
  
  .price-total {
    flex-direction: column;
    text-align: center;
    gap: 5px;
  }
}
</style>
