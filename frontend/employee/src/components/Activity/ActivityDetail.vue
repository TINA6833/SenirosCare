<template>
  <div class="modal fade" id="viewActivityModal" tabindex="-1" aria-labelledby="viewActivityModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-lg">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title" id="viewActivityModalLabel">活動詳情</h5>
          <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
        </div>
        <div class="modal-body">
          <div v-if="activity">
            <div class="row">
              <!-- 活動圖片 -->
              <div class="col-md-5">
                <div class="activity-detail-image-container">
                  <img 
                    :src="getSafeImageUrl(activity?.image)" 
                    class="img-fluid rounded activity-detail-image" 
                    :alt="activity?.name || '活動圖片'"
                    @error="handleImageError"
                    loading="lazy"
                  >
                  <span class="activity-status-badge" :class="getStatusClass(activity)">
                    {{ getStatusText(activity) }}
                  </span>
                </div>
              </div>
              
              <!-- 活動資訊 -->
              <div class="col-md-7">
                <h4 class="fw-bold text-primary mb-3">{{ activity.name }}</h4>
                
                <!-- 基本資訊 -->
                <div class="activity-detail-info">
                  <div class="info-row">
                    <i class="fas fa-tag text-secondary me-2"></i>
                    <span class="fw-semibold">活動類別：</span>
                    <span>{{ activity.category || '未分類' }}</span>
                  </div>
                  
                  <div class="info-row">
                    <i class="fas fa-calendar-alt text-primary me-2"></i>
                    <span class="fw-semibold">活動日期：</span>
                    <span>{{ formatDate(activity.date) }}</span>
                    <span v-if="activity.end && activity.end !== activity.date">
                      - {{ formatDate(activity.end) }}
                    </span>
                  </div>
                  
                  <div class="info-row" v-if="activity.time">
                    <i class="fas fa-clock text-info me-2"></i>
                    <span class="fw-semibold">活動時間：</span>
                    <span>{{ activity.time }}</span>
                  </div>
                  
                  <div class="info-row" v-if="activity.location">
                    <i class="fas fa-map-marker-alt text-danger me-2"></i>
                    <span class="fw-semibold">活動地點：</span>
                    <span>{{ activity.location }}</span>
                  </div>
                  
                  <div class="info-row" v-if="activity.instructor">
                    <i class="fas fa-user-tie text-success me-2"></i>
                    <span class="fw-semibold">講師：</span>
                    <span>{{ activity.instructor }}</span>
                  </div>
                  
                  <!-- 報名資訊 -->
                  <div class="info-row">
                    <i class="fas fa-users text-warning me-2"></i>
                    <span class="fw-semibold">報名人數：</span>
                    <span class="badge bg-primary">
                      {{ activity.current || activity.currentCount || 0 }} / {{ activity.limit || activity.capacity || 0 }}
                    </span>
                  </div>
                  
                  <!-- 報名進度條 -->
                  <div class="mt-3">
                    <div class="progress" style="height: 8px;">
                      <div 
                        class="progress-bar" 
                        :class="getProgressBarClass(activity)"
                        role="progressbar"
                        :style="`width: ${calculateProgress(activity)}%`"
                        :aria-valuenow="calculateProgress(activity)" 
                        aria-valuemin="0" 
                        aria-valuemax="100"
                      ></div>
                    </div>
                    <small class="text-muted">
                      報名率：{{ calculateProgress(activity) }}%
                    </small>
                  </div>
                </div>
              </div>
            </div>
            
            <!-- 報名時間資訊 -->
            <div class="row mt-4" v-if="activity.registrationStart || activity.registrationEnd">
              <div class="col-12">
                <h6 class="text-secondary mb-3">報名時間</h6>
                <div class="d-flex justify-content-between align-items-center p-3 bg-light rounded">
                  <div v-if="activity.registrationStart">
                    <small class="text-muted">報名開始</small><br>
                    <span class="fw-semibold">{{ formatDate(activity.registrationStart) }}</span>
                  </div>
                  <div class="text-center">
                    <i class="fas fa-arrow-right text-muted"></i>
                  </div>
                  <div v-if="activity.registrationEnd" class="text-end">
                    <small class="text-muted">報名截止</small><br>
                    <span class="fw-semibold">{{ formatDate(activity.registrationEnd) }}</span>
                  </div>
                </div>
              </div>
            </div>
            
            <!-- 活動描述 -->
            <div class="row mt-4" v-if="activity.description">
              <div class="col-12">
                <h6 class="text-secondary mb-3">活動描述</h6>
                <div class="p-3 bg-light rounded">
                  <p class="mb-0" style="white-space: pre-line;">{{ activity.description }}</p>
                </div>
              </div>
            </div>
            
            <!-- 活動標籤 -->
            <div class="row mt-4" v-if="activity.tags && activity.tags.length > 0">
              <div class="col-12">
                <h6 class="text-secondary mb-3">活動標籤</h6>
                <div class="d-flex flex-wrap gap-2">
                  <span 
                    v-for="(tag, index) in activity.tags" 
                    :key="index"
                    class="badge bg-secondary"
                  >
                    {{ typeof tag === 'string' ? tag : (tag.name || tag.tagName || '') }}
                  </span>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 載入中狀態 -->
          <div v-else class="text-center py-5">
            <div class="spinner-border text-primary" role="status">
              <span class="visually-hidden">載入中...</span>
            </div>
            <p class="mt-2">載入活動詳情中...</p>
          </div>
        </div>
        
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">關閉</button>
          <button 
            type="button" 
            class="btn btn-primary" 
            @click="editActivity"
            v-if="activity"
          >
            <i class="fas fa-edit me-1"></i>編輯活動
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ActivityDetail',
  props: {
    activity: {
      type: Object,
      default: null
    }
  },
  emits: ['edit'],
  setup(props, { emit }) {
    
    /**
     * 處理圖片載入錯誤
     */
    const handleImageError = (e) => {
      console.warn('詳情頁面圖片載入失敗，使用預設圖片:', e.target.src);
      e.target.src = '/src/assets/images/activity/default-activity.jpg';
      
      // 如果預設圖片也無法載入，使用備用方案
      e.target.onerror = () => {
        e.target.style.backgroundColor = '#f8f9fa';
        e.target.style.display = 'flex';
        e.target.style.alignItems = 'center';
        e.target.style.justifyContent = 'center';
        e.target.alt = '圖片無法載入';
      };
    };
    
    /**
     * 取得安全的圖片 URL
     */
    const getSafeImageUrl = (imageUrl) => {
      if (!imageUrl || imageUrl.trim() === '') {
        return '/src/assets/images/activity/default-activity.jpg';
      }
      
      // 檢查是否為有效的 URL
      try {
        new URL(imageUrl);
        return imageUrl;
      } catch {
        // 檢查是否為本地路徑
        if (imageUrl.startsWith('/') || imageUrl.startsWith('./') || imageUrl.startsWith('../')) {
          return imageUrl;
        }
        return '/src/assets/images/activity/default-activity.jpg';
      }
    };
    
    /**
     * 格式化日期
     */
    const formatDate = (dateStr) => {
      if (!dateStr) return '';
      try {
        const date = new Date(dateStr);
        return date.toLocaleDateString('zh-TW', {
          year: 'numeric',
          month: '2-digit',
          day: '2-digit'
        });
      } catch (error) {
        console.error('日期格式化錯誤:', error);
        return dateStr;
      }
    };
    
    /**
     * 計算報名進度百分比
     */
    const calculateProgress = (activity) => {
      if (!activity) return 0;
      const current = activity.current || activity.currentCount || 0;
      const total = activity.limit || activity.capacity || 0;
      if (total <= 0) return 0;
      return Math.min(Math.round((current / total) * 100), 100);
    };
    
    /**
     * 取得狀態樣式類別
     */
    const getStatusClass = (activity) => {
      if (!activity) return 'bg-secondary';
      if (activity.status === false || activity.isExpired) {
        return 'bg-secondary';
      }
      return 'bg-success';
    };
    
    /**
     * 取得狀態文字
     */
    const getStatusText = (activity) => {
      if (!activity) return '未知狀態';
      if (activity.status === false || activity.isExpired) {
        return '已結束';
      }
      return '報名中';
    };
    
    /**
     * 取得進度條樣式類別
     */
    const getProgressBarClass = (activity) => {
      const progress = calculateProgress(activity);
      if (progress >= 100) return 'bg-danger';
      if (progress >= 80) return 'bg-warning';
      return 'bg-success';
    };
    
    /**
     * 編輯活動 - 發出編輯事件給父組件
     */
    const editActivity = () => {
      console.log('詳情頁面 - 發出編輯事件:', props.activity);
      emit('edit', props.activity);
    };
    
    return {
      handleImageError,
      getSafeImageUrl,
      formatDate,
      calculateProgress,
      getStatusClass,
      getStatusText,
      getProgressBarClass,
      editActivity
    };
  }
}
</script>

<style scoped>
.activity-detail-image-container {
  position: relative;
  margin-bottom: 1rem;
  border-radius: 8px;
  overflow: hidden;
  background-color: #f8f9fa;
}

.activity-detail-image {
  width: 100%;
  height: 300px; /* 增加高度 */
  object-fit: contain; /* 改為 contain 以完整顯示圖片 */
  object-position: center; /* 圖片置中顯示 */
  background-color: #f8f9fa; /* 圖片周圍的背景色 */
}

.activity-status-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  padding: 5px 15px;
  border-radius: 20px;
  font-size: 0.75rem;
  font-weight: 600;
  color: #fff;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.3);
  z-index: 1;
}

.activity-detail-info .info-row {
  display: flex;
  align-items: center;
  margin-bottom: 0.75rem;
  padding: 0.5rem 0;
  border-bottom: 1px solid #f0f0f0;
}

.activity-detail-info .info-row:last-child {
  border-bottom: none;
}

.fw-semibold {
  font-weight: 600;
  margin-right: 0.5rem;
  min-width: 80px;
}

.progress {
  border-radius: 10px;
  overflow: hidden;
}

.progress-bar {
  transition: width 0.6s ease;
}

/* 響應式設計 */
@media (max-width: 768px) {
  .activity-detail-image {
    height: 200px; /* 小螢幕時降低高度 */
  }
}

@media (max-width: 576px) {
  .activity-detail-image {
    height: 150px; /* 更小螢幕時進一步降低高度 */
  }
}
</style>
