<template>
  <div class="card activity-card">
    <div class="position-relative">
      <img :src="activity.image || '/default-activity.jpg'" 
           class="card-img-top activity-image" 
           :alt="activity.name">
      <span class="badge position-absolute top-0 end-0 m-2" 
            :class="activity.status ? 'bg-success' : 'bg-danger'">
          {{ activity.status ? '報名中' : '過去活動' }}
      </span>
    </div>
    <div class="card-body">
      <h6 class="card-title">{{ activity.name }}</h6>
      <p class="text-muted small mb-2">{{ activity.category }}</p>
      
      <div class="activity-info">
        <div class="d-flex justify-content-between mb-2">
          <span class="text-muted">報名人數：</span>
          <span>{{ activity.current }}/{{ activity.limit }}</span>
        </div>
        <div class="progress mb-2" style="height: 6px;">
          <div class="progress-bar" 
               :style="{width: (activity.current / activity.limit * 100) + '%'}">
          </div>
        </div>
        
        <div class="mb-2">
          <i class="fas fa-calendar text-muted"></i>
          <small class="ms-1">{{ formatDate(activity.date) }} - {{ formatDate(activity.end) }}</small>
        </div>
        <div class="mb-2">
          <i class="fas fa-clock text-muted"></i>
          <small class="ms-1">{{ activity.time }}</small>
        </div>
        <div class="mb-2">
          <i class="fas fa-map-marker-alt text-muted"></i>
          <small class="ms-1">{{ activity.location }}</small>
        </div>
        <div class="mb-2">
          <i class="fas fa-user text-muted"></i>
          <small class="ms-1">講師：{{ activity.instructor }}</small>
        </div>
        
        <div class="registration-period text-muted small">
          報名期間：{{ formatDate(activity.registrationStart) }} ~ {{ formatDate(activity.registrationEnd) }}
        </div>
        
        <!-- 標籤元件 -->
        <ActivityTags :tags="activity.tags" />
      </div>
      
      <div class="mt-3">
        <p class="card-text small text-muted">{{ truncateDescription(activity.description) }}</p>
      </div>
    </div>
    <!-- 操作按鈕區域 -->
    <div class="card-footer d-flex justify-content-between">
      <button class="btn btn-sm btn-outline-info" @click="onView">
        <i class="fas fa-eye"></i> 查看
      </button>
      <button class="btn btn-sm btn-outline-warning" @click="onEdit">
        <i class="fas fa-edit"></i> 編輯
      </button>
      <button 
        class="btn btn-sm btn-outline-danger" 
        @click="onDelete"
        :disabled="loading">
        <i class="fas fa-trash"></i> 刪除
      </button>
    </div>
  </div>
</template>

<script>
import ActivityTags from './ActivityTags.vue';

export default {
  name: 'ActivityCard',
  components: {
    ActivityTags
  },
  props: {
    // 單個活動資料
    activity: {
      type: Object,
      required: true
    },
    // 載入狀態
    loading: {
      type: Boolean,
      default: false
    }
  },
  emits: ['view', 'edit', 'delete'],
  setup(props, { emit }) {
    // 格式化日期為繁體中文格式
    const formatDate = (dateString) => {
      if (!dateString) return '';
      return new Date(dateString).toLocaleDateString('zh-TW');
    };

    // 截斷描述文字，避免卡片過長
    const truncateDescription = (description, maxLength = 50) => {
      if (!description) return '';
      return description.length > maxLength ? 
          description.substring(0, maxLength) + '...' : description;
    };

    // 發送事件至父元件
    const onView = () => emit('view', props.activity);
    const onEdit = () => emit('edit', props.activity);
    const onDelete = () => emit('delete', props.activity.id);

    return {
      formatDate,
      truncateDescription,
      onView,
      onEdit,
      onDelete
    };
  }
}
</script>

<style scoped>
.activity-card {
  transition: transform 0.2s ease-in-out;
  height: 100%;
}

.activity-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(0,0,0,0.1);
}

.activity-image {
  height: 200px;
  object-fit: cover;
}

.activity-info {
  font-size: 0.9rem;
}

.registration-period {
  border-top: 1px solid #eee;
  padding-top: 0.5rem;
}

.progress-bar {
  background: linear-gradient(45deg, #007bff, #0056b3);
}
</style>
