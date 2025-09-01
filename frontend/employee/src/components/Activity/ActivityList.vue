<template>
  <div class="card">
    <!-- 卡片標題和工具列 -->
    <div class="card-header">
      <div class="row align-items-center">
        <div class="col">
          <h5 class="card-title mb-0">活動管理</h5>
        </div>
        <div class="col-auto">
          <!-- 新增活動按鈕 -->
          <button class="btn btn-primary" @click="openAddModal">
            <i class="fas fa-plus me-2"></i>新增活動
          </button>
        </div>
      </div>
    </div>

    <div class="card-body">
      <!-- 搜尋和篩選區域 -->
      <div class="row mb-4">
        <div class="col-md-6">
          <!-- 搜尋框 -->
          <div class="input-group">
            <input type="text" class="form-control" placeholder="搜尋活動名稱或講師..." v-model="searchText"
              @input="performSearch">
            <button class="btn btn-outline-secondary" type="button" @click="clearSearch" v-if="searchText">
              <i class="fas fa-times"></i>
            </button>
          </div>
        </div>

        <div class="col-md-6">
          <!-- 篩選按鈕組 -->
          <div class="d-flex justify-content-end gap-2">
            <!-- 類別篩選 -->
            <div class="dropdown">
              <button class="btn btn-outline-primary dropdown-toggle" type="button" data-bs-toggle="dropdown">
                <i class="fas fa-filter me-1"></i>
                {{ selectedCategory || '所有類別' }}
              </button>
              <ul class="dropdown-menu">
                <li><a class="dropdown-item" href="#" @click="setCategory('')">所有類別</a></li>
                <li>
                  <hr class="dropdown-divider">
                </li>
                <li v-for="category in categories" :key="category">
                  <a class="dropdown-item" href="#" @click="setCategory(category)">{{ category }}</a>
                </li>
              </ul>
            </div>

            <!-- 清除所有篩選 -->
            <button v-if="isAnyFilterActive" class="btn btn-outline-danger" @click="clearAllFilters">
              <i class="fas fa-times-circle me-1"></i>清除篩選
            </button>
          </div>
        </div>
      </div>

      <!-- 活動狀態頁籤 -->
      <ul class="nav nav-tabs mb-4" id="activityTabs" role="tablist">
        <li class="nav-item" role="presentation">
          <button class="nav-link" :class="{ 'active': activeTab === 'active' }" @click="activeTab = 'active'"
            type="button">
            <i class="fas fa-calendar-check me-2"></i>
            報名中活動 ({{ activeActivities.length }})
          </button>
        </li>
        <li class="nav-item" role="presentation">
          <button class="nav-link" :class="{ 'active': activeTab === 'ended' }" @click="activeTab = 'ended'"
            type="button">
            <i class="fas fa-calendar-times me-2"></i>
            已結束活動 ({{ endedActivities.length }})
          </button>
        </li>
      </ul>

      <!-- 載入中狀態 -->
      <div v-if="loading" class="text-center py-5">
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">載入中...</span>
        </div>
        <p class="mt-2 text-muted">載入活動資料中...</p>
      </div>

      <!-- 報名中活動列表 -->
      <div v-else-if="activeTab === 'active'" class="tab-pane fade show active">
        <!-- 無資料狀態 -->
        <div v-if="filteredActiveActivities.length === 0" class="text-center py-5">
          <div class="mb-3">
            <i class="fas fa-calendar-plus fa-3x text-muted"></i>
          </div>
          <h5 class="text-muted">{{ searchText ? '找不到符合條件的活動' : '暫無進行中的活動' }}</h5>
          <p class="text-muted">{{ searchText ? '請嘗試調整搜尋條件' : '點擊上方「新增活動」按鈕來建立第一個活動' }}</p>
        </div>

        <!-- 活動卡片列表 -->
        <div v-else class="row gy-4">
          <div v-for="activity in filteredActiveActivities" :key="`active-${activity.id}`" class="col-md-6 col-lg-4">
            <div class="card h-100 activity-card">
              <!-- 活動圖片 -->
              <div class="activity-image-container">
                <img :src="getSafeImageUrl(activity.image)" class="card-img-top activity-image" :alt="activity.name"
                  @error="handleImageError" loading="lazy">
                <span class="activity-status-badge bg-success">
                  {{ activity.statusText || '報名中' }}
                </span>
              </div>

              <!-- 活動內容 -->
              <div class="card-body">
                <h5 class="card-title activity-title">{{ activity.name || '未命名活動' }}</h5>

                <div class="activity-info">
                  <p class="mb-1">
                    <i class="fas fa-calendar-alt text-primary me-2"></i>
                    {{ formatActivityDate(activity) }}
                  </p>
                  <p class="mb-1">
                    <i class="fas fa-clock text-info me-2"></i>
                    {{ activity.time || '時間未定' }}
                  </p>
                  <p class="mb-1">
                    <i class="fas fa-map-marker-alt text-danger me-2"></i>
                    {{ activity.location || '地點未定' }}
                  </p>
                  <p class="mb-3">
                    <i class="fas fa-user-tie text-warning me-2"></i>
                    {{ activity.instructor || '講師未定' }}
                  </p>

                  <!-- 重點註解：報名進度區塊 -->
                  <div class="registration-section">
                    <!-- 報名人數 -->
                    <div class="d-flex justify-content-between align-items-center mb-2">
                      <small class="fw-semibold">報名人數</small>
                      <small class="fw-bold">{{ activity.currentCount || 0 }}/{{ activity.capacity || 0 }}</small>
                    </div>
                    <div class="progress mb-3" style="height: 6px;">
                      <div class="progress-bar bg-primary" role="progressbar"
                        :style="`width: ${calculateProgress(activity.currentCount, activity.capacity)}%`"
                        :aria-valuenow="calculateProgress(activity.currentCount, activity.capacity)" aria-valuemin="0"
                        aria-valuemax="100"></div>
                    </div>

                    <!-- 重點註解：報名期間資訊移到此處 -->
                    <div class="registration-period">
                      <small class="text-muted d-block">
                        <i class="fas fa-calendar-check me-1"></i>
                        {{ getRegistrationDateInfo(activity) }}
                      </small>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 重點註解：優化後的操作按鈕區域 - 固定在底部 -->
              <div class="card-footer bg-light border-top">
                <div class="row g-2">
                  <div class="col-4">
                    <button 
                      class="btn btn-sm btn-outline-warning w-100 action-btn" 
                      @click="endRegistration(activity)" 
                      title="結束報名"
                      :disabled="loading"
                    >
                      <i class="fas fa-stop-circle d-block mb-1"></i>
                      <small>結束報名</small>
                    </button>
                  </div>
                  <div class="col-4">
                    <button 
                      class="btn btn-sm btn-outline-primary w-100 action-btn" 
                      @click="openEditModal(activity)" 
                      title="編輯活動"
                      :disabled="loading"
                    >
                      <i class="fas fa-edit d-block mb-1"></i>
                      <small>編輯</small>
                    </button>
                  </div>
                  <div class="col-4">
                    <button 
                      class="btn btn-sm btn-outline-info w-100 action-btn" 
                      @click="viewActivity(activity)" 
                      title="查看詳情"
                      :disabled="loading"
                    >
                      <i class="fas fa-eye d-block mb-1"></i>
                      <small>查看</small>
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 已結束活動列表 -->
      <div v-else-if="activeTab === 'ended'" class="tab-pane fade show active">
        <!-- 無資料狀態 -->
        <div v-if="filteredEndedActivities.length === 0" class="text-center py-5">
          <div class="mb-3">
            <i class="fas fa-calendar-times fa-3x text-muted"></i>
          </div>
          <h5 class="text-muted">{{ searchText ? '找不到符合條件的已結束活動' : '暫無已結束的活動' }}</h5>
          <p class="text-muted">{{ searchText ? '請嘗試調整搜尋條件' : '當活動結束後會顯示在這裡' }}</p>
        </div>

        <!-- 已結束活動卡片列表 -->
        <div v-else class="row gy-4">
          <div v-for="activity in filteredEndedActivities" :key="`ended-${activity.id}`" class="col-md-6 col-lg-4">
            <div class="card h-100 activity-card ended-activity">
              <!-- 活動圖片 -->
              <div class="activity-image-container">
                <img :src="getSafeImageUrl(activity.image)" class="card-img-top activity-image" :alt="activity.name"
                  @error="handleImageError" loading="lazy">
                <span class="activity-status-badge bg-secondary">
                  {{ activity.statusText || '已結束' }}
                </span>
              </div>

              <!-- 活動內容 -->
              <div class="card-body">
                <h5 class="card-title activity-title">{{ activity.name || '未命名活動' }}</h5>

                <div class="activity-info">
                  <p class="mb-1">
                    <i class="fas fa-calendar-alt text-primary me-2"></i>
                    {{ formatActivityDate(activity) }}
                  </p>
                  <p class="mb-1">
                    <i class="fas fa-clock text-info me-2"></i>
                    {{ activity.time || '時間未定' }}
                  </p>
                  <p class="mb-1">
                    <i class="fas fa-map-marker-alt text-danger me-2"></i>
                    {{ activity.location || '地點未定' }}
                  </p>
                  <p class="mb-3">
                    <i class="fas fa-user-tie text-warning me-2"></i>
                    {{ activity.instructor || '講師未定' }}
                  </p>

                  <!-- 重點註解：已結束活動的最終報名統計 -->
                  <div class="registration-section">
                    <div class="d-flex justify-content-between align-items-center mb-2">
                      <small class="fw-semibold">最終報名人數</small>
                      <small class="fw-bold text-secondary">{{ activity.currentCount || 0 }}/{{ activity.capacity || 0 }}</small>
                    </div>
                    <div class="progress mb-3" style="height: 6px;">
                      <div class="progress-bar bg-secondary" role="progressbar"
                        :style="`width: ${calculateProgress(activity.currentCount, activity.capacity)}%`"
                        :aria-valuenow="calculateProgress(activity.currentCount, activity.capacity)" aria-valuemin="0"
                        aria-valuemax="100"></div>
                    </div>

                    <!-- 重點註解：活動結束時間資訊 -->
                    <div class="registration-period">
                      <small class="text-muted d-block">
                        <i class="fas fa-calendar-times me-1"></i>
                        活動已於 {{ formatEndDate(activity) }} 結束
                      </small>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 重點註解：已結束活動的操作按鈕 -->
              <div class="card-footer bg-light border-top">
                <div class="row g-2">
                  <div class="col-6">
                    <button 
                      class="btn btn-sm btn-outline-secondary w-100 action-btn" 
                      @click="viewActivity(activity)" 
                      title="查看詳情"
                      :disabled="loading"
                    >
                      <i class="fas fa-eye d-block mb-1"></i>
                      <small>查看詳情</small>
                    </button>
                  </div>
                  <div class="col-6">
                    <button 
                      class="btn btn-sm btn-outline-danger w-100 action-btn" 
                      @click="deleteActivity(activity)" 
                      title="刪除活動"
                      :disabled="loading"
                    >
                      <i class="fas fa-trash d-block mb-1"></i>
                      <small>刪除</small>
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div v-if="error" class="alert alert-danger">{{ error }}</div>
    </div>

    <!-- 活動表單 Modal -->
    <ActivityForm 
      :is-edit-mode="isEditMode" 
      :form="form" 
      :loading="loading" 
      :categories="categories"
      @submit="submitForm"
      @add-tag="addTagToForm"
      @remove-tag="removeTagFromForm"
      @add-success="handleActivityAdded"
    />

    <!-- 活動詳情 Modal -->
    <ActivityDetail :activity="selectedActivity" @edit="editFromView" />
  </div>
</template>

<script>
import { ref, computed, onMounted, reactive } from 'vue';
import { useToast } from '@/composables/useToast';
import { useActivity } from '@/composables/useActivity';
import { useConfirmDialog } from '@/composables/useConfirmDialog'; // 重點註解：引入確認對話框
import { activityService } from '@/services/activityService';

// 引入子元件
import ActivityForm from './ActivityForm.vue';
import ActivityDetail from './ActivityDetail.vue';

export default {
  name: 'ActivityList',
  components: {
    ActivityForm,
    ActivityDetail
  },
  setup() {
    // === Composables ===
    const { showToast } = useToast();
    const { showConfirmDialog } = useConfirmDialog(); // 重點註解：引入確認對話框方法

    const {
      activities: composableActivities,
      loading: composableLoading,
      loadActivities,
      addActivity,
      updateActivity,
      removeActivity
    } = useActivity();

    // === 本地狀態管理 ===
    const searchText = ref('');
    const activeTab = ref('active');
    const isEditMode = ref(false);
    const selectedActivity = ref(null);
    const detailLoading = ref(false); // 詳情載入狀態

    // 篩選相關狀態
    const selectedCategory = ref('');
    const selectedLocation = ref('');
    const selectedInstructor = ref('');

    // 表單資料
    const form = reactive({
      name: '',
      category: '',
      date: '',
      endDate: '',
      time: '',
      location: '',
      instructor: '',
      description: '',
      image: '',
      limit: 0,
      current: 0,
      status: true,
      tags: []
    });

    // === 計算屬性 ===
    // 重點註解：直接指定活動分類選單
    const categories = ref(['課程', '團康', '講座', '展覽']);

    const locations = computed(() => {
      return [...new Set(composableActivities.value.map(a => a.location).filter(Boolean))];
    });

    const instructors = computed(() => {
      return [...new Set(composableActivities.value.map(a => a.instructor).filter(Boolean))];
    });

    const isAnyFilterActive = computed(() => {
      return selectedCategory.value || selectedLocation.value || selectedInstructor.value || searchText.value;
    });

    const activeActivities = computed(() => {
      const active = composableActivities.value.filter(activity =>
        activity.status === true && !activity.isExpired
      );
      return active;
    });

    const endedActivities = computed(() => {
      const ended = composableActivities.value.filter(activity =>
        activity.status === false || activity.isExpired
      );
      return ended;
    });

    const filteredActiveActivities = computed(() => {
      return filterActivities(activeActivities.value);
    });

    const filteredEndedActivities = computed(() => {
      return filterActivities(endedActivities.value);
    });

    // === 方法 ===

    /**
     * 篩選活動的共用方法
     */
    const filterActivities = (activityList) => {
      let filtered = [...activityList];

      if (searchText.value) {
        const search = searchText.value.toLowerCase();
        filtered = filtered.filter(activity =>
          (activity.name || '').toLowerCase().includes(search) ||
          (activity.instructor || '').toLowerCase().includes(search) ||
          (activity.location || '').toLowerCase().includes(search)
        );
      }

      if (selectedCategory.value) {
        filtered = filtered.filter(activity => activity.category === selectedCategory.value);
      }

      if (selectedLocation.value) {
        filtered = filtered.filter(activity => activity.location === selectedLocation.value);
      }

      if (selectedInstructor.value) {
        filtered = filtered.filter(activity => activity.instructor === selectedInstructor.value);
      }

      return filtered;
    };

    /**
     * 重新載入活動資料
     */
    const refreshActivities = async () => {
      try {
        const activities = await loadActivities();
      } catch (error) {
        console.error('載入活動失敗:', error);
        showToast({
          title: '載入失敗',
          message: error.message || '無法載入活動資料，請稍後再試',
          type: 'error'
        });
      }
    };

    /**
     * 格式化活動日期顯示
     */
    const formatActivityDate = (activity) => {
      if (!activity.date) return '日期未定';

      try {
        const startDate = new Date(activity.date);
        let result = startDate.toLocaleDateString('zh-TW');

        if (activity.endDate && activity.endDate !== activity.date) {
          const endDate = new Date(activity.endDate);
          result += ` - ${endDate.toLocaleDateString('zh-TW')}`;
        }

        return result;
      } catch (error) {
        return activity.date;
      }
    };

    /**
     * 重點註解：格式化活動結束日期 - 修復缺失的方法
     * @param {Object} activity - 活動物件
     * @returns {string} 格式化後的結束日期
     */
    const formatEndDate = (activity) => {
      if (!activity) return '未知';
      
      try {
        // 重點註解：優先使用結束日期，再使用活動日期
        const endDate = activity.endDate || activity.end || activity.date;
        if (!endDate) return '未知';

        const date = new Date(endDate);
        if (isNaN(date.getTime())) {
          // 重點註解：如果日期格式無效，回傳原始字串
          return endDate;
        }

        return date.toLocaleDateString('zh-TW');
      } catch (error) {
        console.warn('格式化結束日期時發生錯誤:', error);
        return activity.endDate || activity.end || activity.date || '未知';
      }
    };

    /**
     * 計算報名進度百分比
     */
    const calculateProgress = (current, total) => {
      if (!total || total <= 0) return 0;
      const currentNum = Number(current) || 0;
      const totalNum = Number(total) || 0;
      return Math.min(Math.round((currentNum / totalNum) * 100), 100);
    };

    /**
     * 取得報名日期資訊
     */
    const getRegistrationDateInfo = (activity) => {
      if (!activity) return '無報名資訊';

      if (activity.registrationStart && activity.registrationEnd) {
        const start = new Date(activity.registrationStart).toLocaleDateString('zh-TW');
        const end = new Date(activity.registrationEnd).toLocaleDateString('zh-TW');
        return `報名期間：${start} - ${end}`;
      } else if (activity.registrationEnd) {
        const end = new Date(activity.registrationEnd).toLocaleDateString('zh-TW');
        return `報名截止：${end}`;
      } else {
        return '開放報名中';
      }
    };

    /**
     * 重點註解：處理圖片載入錯誤，統一使用茶葉預設圖片
     * @param {Event} e - 圖片載入錯誤事件
     */
    const handleImageError = (e) => {
      console.warn('圖片載入失敗，使用預設茶葉圖片:', e.target.src);
      e.target.src = '/src/assets/images/tea.png';
    };

    /**
     * 重點註解：取得安全的圖片 URL，無圖片時使用茶葉預設圖片
     * @param {string} imageUrl - 原始圖片 URL
     * @returns {string} 安全的圖片 URL
     */
    const getSafeImageUrl = (imageUrl) => {
      // 重點註解：檢查是否為空值、空字串或預設值
      if (!imageUrl || 
          imageUrl.trim() === '' || 
          imageUrl === '無圖片' || 
          imageUrl === 'null' || 
          imageUrl === 'undefined') {
        return '/src/assets/images/tea.png';
      }
      
      // 重點註解：如果已經是預設圖片路徑，直接回傳
      if (imageUrl.includes('tea.png')) {
        return '/src/assets/images/tea.png';
      }
      
      return imageUrl;
    };

    /**
     * 重點註解：結束活動報名功能 - 使用 useConfirmDialog 呈現確認對話框
     * @param {Object} activity - 要結束報名的活動物件
     */
    const endRegistration = async (activity) => {
      if (!activity?.id) {
        showToast({
          title: '操作失敗',
          message: '無法識別活動資訊',
          type: 'error'
        });
        return;
      }

      try {
        // 重點註解：使用 useConfirmDialog 顯示確認對話框
        const confirmed = await showConfirmDialog({
          title: '結束活動報名',
          message: `確定要結束「${activity.name}」的報名嗎？\n\n此操作會將活動將移至「已結束活動」頁籤\n\n此操作無法復原！`,
          type: 'warning',
          confirmText: '確定結束',
          cancelText: '取消',
          confirmButtonClass: 'btn-warning'
        });

        if (!confirmed) return;

        console.log(`開始結束活動 ID: ${activity.id} 的報名...`);
        
        // 重點註解：呼叫 activityService 的 endRegistration 方法
        const result = await activityService.endRegistration(activity.id);

        if (result.success) {
          // 重點註解：API 呼叫成功，更新本地活動狀態
          const activityIndex = composableActivities.value.findIndex(a => a.id === activity.id);

          if (activityIndex !== -1) {
            // 重點註解：更新活動狀態為已結束
            composableActivities.value[activityIndex] = {
              ...composableActivities.value[activityIndex],
              status: false,           // 重點註解：設定 status = false 表示已結束
              isExpired: true,         // 標記為已過期
              statusText: "已結束",    // 顯示文字
              statusClass: "bg-secondary", // CSS 樣式類別
              statusDisplay: "已結束"  // 狀態顯示
            };

            console.log(`活動 ID: ${activity.id} 狀態已更新為已結束`);

            showToast({
              title: '報名已結束',
              message: `活動「${activity.name}」的報名已成功結束`,
              type: 'success'
            });

            // 重點註解：延遲切換到已結束頁籤，讓使用者看到成功訊息
            setTimeout(() => {
              activeTab.value = 'ended';
              console.log('已切換到已結束活動頁籤');
            }, 1500);

          } else {
            console.warn('在本地活動列表中找不到對應的活動記錄');
            // 重點註解：如果本地找不到，重新載入活動列表
            await refreshActivities();
            showToast({
              title: '報名已結束',
              message: result.message,
              type: 'success'
            });
          }

        } else {
          // 重點註解：API 回傳失敗結果
          throw new Error(result.message || '結束報名失敗');
        }

      } catch (error) {
        console.error('結束報名時發生錯誤:', error);
        showToast({
          title: '結束報名失敗',
          message: error.message || '發生未知錯誤，請稍後再試',
          type: 'error'
        });
      }
    };

    /**
     * 查看活動詳情 - 修復版本
     * @param {Object} activity - 要查看的活動
     */
    const viewActivity = async (activity) => {
      if (!activity || !activity.id) {
        showToast({
          title: '操作失敗',
          message: '無法識別活動資訊',
          type: 'error'
        });
        return;
      }

      try {
        detailLoading.value = true;
        console.log(`開始載入活動詳情，ID: ${activity.id}`);

        // 呼叫 API 獲取完整的活動詳情
        const activityDetail = await activityService.getActivityById(activity.id);

        if (activityDetail) {
          console.log('成功獲取活動詳情:', activityDetail);
          selectedActivity.value = activityDetail;

          // 開啟詳情 Modal
          const modalElement = document.getElementById('viewActivityModal');
          if (modalElement) {
            // 檢查 Bootstrap 是否已載入
            if (typeof bootstrap !== 'undefined' && bootstrap.Modal) {
              const modal = new bootstrap.Modal(modalElement, {
                backdrop: 'static', // 防止點擊背景關閉
                keyboard: true      // 允許按 ESC 鍵關閉
              });
              modal.show();
            } else {
              console.error('Bootstrap Modal 未載入');
              showToast({
                title: '系統錯誤',
                message: 'Modal 元件未正確載入，請重新整理頁面',
                type: 'error'
              });
            }
          } else {
            console.error('找不到 Modal 元素: viewActivityModal');
            showToast({
              title: '系統錯誤',
              message: '找不到詳情顯示元件',
              type: 'error'
            });
          }
        } else {
          throw new Error('無法獲取活動詳情');
        }

      } catch (error) {
        console.error('載入活動詳情失敗:', error);
        showToast({
          title: '載入失敗',
          message: error.message || '無法載入活動詳情，請稍後再試',
          type: 'error'
        });
        selectedActivity.value = null;
      } finally {
        detailLoading.value = false;
      }
    };

    /**
     * 從詳情頁面編輯活動
     */
    const editFromView = (activity) => {
      console.log('從詳情頁面編輯活動:', activity);

      // 關閉詳情 Modal
      const viewModal = document.getElementById('viewActivityModal');
      if (viewModal && typeof bootstrap !== 'undefined' && bootstrap.Modal) {
        const modal = bootstrap.Modal.getInstance(viewModal);
        if (modal) {
          modal.hide();
        }
      }

      // 等待 Modal 關閉動畫完成後開啟編輯 Modal
      setTimeout(() => {
        openEditModal(activity || selectedActivity.value);
      }, 300);
    };

    /**
     * 清除搜尋條件
     */
    const clearSearch = () => {
      searchText.value = '';
    };

    /**
     * 清除所有篩選條件
     */
    const clearAllFilters = () => {
      searchText.value = '';
      selectedCategory.value = '';
      selectedLocation.value = '';
      selectedInstructor.value = '';
    };

    /**
     * 設定類別篩選
     */
    const setCategory = (category) => {
      selectedCategory.value = category;
    };

    /**
     * 執行搜尋
     */
    const performSearch = () => {
      console.log('執行搜尋:', searchText.value);
    };

    /**
     * 開啟新增活動 Modal
     */
    const openAddModal = () => {
      isEditMode.value = false;
      selectedActivity.value = null;
      Object.assign(form, {
        name: '',
        category: '',
        date: '',
        endDate: '',
        time: '',
        location: '',
        instructor: '',
        description: '',
        image: '',
        limit: 0,
        current: 0,
        status: true,
        tags: []
      });
      // 重點註解：開啟活動表單 Modal
      const modalElement = document.getElementById('activityModal');
      if (modalElement && typeof bootstrap !== 'undefined' && bootstrap.Modal) {
        const modal = new bootstrap.Modal(modalElement);
        modal.show();
      }
    };

    /**
     * 開啟編輯活動 Modal
     */
    const openEditModal = (activity) => {
      isEditMode.value = true;
      selectedActivity.value = activity;

      Object.assign(form, {
        name: activity.name || '',
        category: activity.category || '',
        date: activity.date || '',
        endDate: activity.endDate || '',
        time: activity.time || '',
        location: activity.location || '',
        instructor: activity.instructor || '',
        description: activity.description || '',
        image: activity.image || '',
        limit: activity.capacity || 0,
        current: activity.currentCount || 0,
        status: activity.status ?? true,
        tags: activity.tags || []
      });

      const modalElement = document.getElementById('activityModal');
      if (modalElement && typeof bootstrap !== 'undefined' && bootstrap.Modal) {
        const modal = new bootstrap.Modal(modalElement);
        modal.show();
      }
    };

    /**
     * 重點註解：提交表單 - 會呼叫 activityService.addActivity 並刷新活動列表
     */
    const submitForm = async (formData) => {
      // 重點註解：現在這個方法主要處理「更新」邏輯
      // 新增邏輯已移至 ActivityForm.vue 內部處理
      if (!isEditMode.value || !selectedActivity.value) {
        console.warn('submitForm 被呼叫，但不是在編輯模式');
        return;
      }

      try {
        console.log(`開始更新活動 ID: ${selectedActivity.value.id}`);
        const result = await activityService.updateActivity(selectedActivity.value.id, formData);
        
        if (result.success) {
          showToast({
            title: '更新成功',
            message: result.message,
            type: 'success'
          });
          await refreshActivities();
          closeModal();
        } else {
          throw new Error(result.message || '更新活動失敗');
        }
      } catch (error) {
        console.error('更新活動失敗:', error);
        showToast({
          title: '更新失敗',
          message: error.message || '操作失敗，請稍後再試',
          type: 'error'
        });
      }
    };

    /**
     * 重點註解：處理新增成功後的事件
     * 當 ActivityForm 觸發 'add-success' 事件時，此方法會被呼叫
     * 由於 ActivityForm 會自己關閉 Modal，這裡只需要刷新列表
     */
    const handleActivityAdded = async () => {
      console.log('ActivityList - 收到新增成功事件，準備刷新列表');
      
      try {
        // 重點註解：重新載入活動列表
        await refreshActivities();
        console.log('活動列表已成功刷新');
      } catch (error) {
        console.error('刷新活動列表時發生錯誤:', error);
        showToast({
          title: '刷新失敗',
          message: '無法刷新活動列表，請手動重新整理頁面',
          type: 'error'
        });
      }
    };

    /**
     * 清除 Modal 狀態 - 用於編輯模式的清理
     */
    const closeModal = () => {
      isEditMode.value = false;
      selectedActivity.value = null;
      Object.assign(form, {
        name: '',
        category: '',
        date: '',
        end: '',
        time: '',
        location: '',
        instructor: '',
        description: '',
        image: '',
        limit: 30,
        current: 0,
        status: true,
        registrationStart: '',
        registrationEnd: '',
        latitude: null,
        longitude: null,
        tags: []
      });
    };

    /**
     * 重點註解：刪除活動功能 - 使用 useConfirmDialog 呈現確認對話框
     * @param {Object} activity - 要刪除的活動物件
     */
    const deleteActivity = async (activity) => {
      if (!activity?.id) {
        showToast({
          title: '操作失敗',
          message: '無法識別活動資訊',
          type: 'error'
        });
        return;
      }

      try {
        // 重點註解：使用 useConfirmDialog 顯示刪除確認對話框
        const confirmed = await showConfirmDialog({
          title: '刪除活動',
          message: `確定要刪除活動「${activity.name}」嗎？\n\n此操作將會永久移除活動資料\n無法復原\所有相關的報名資料也會被刪除\n\n請謹慎考慮！`,
          type: 'error',
          confirmText: '確定刪除',
          cancelText: '取消',
          confirmButtonClass: 'btn-danger'
        });

        if (!confirmed) return;

        console.log(`開始刪除活動 ID: ${activity.id}...`);

        const success = await removeActivity(activity.id);

        if (success) {
          showToast({
            title: '刪除成功',
            message: `活動「${activity.name}」已成功刪除`,
            type: 'success'
          });
        }

      } catch (error) {
        console.error('刪除活動時發生錯誤:', error);
        showToast({
          title: '刪除失敗',
          message: error.message || '刪除失敗，請稍後再試',
          type: 'error'
        });
      }
    };

    /**
     * 重點註解：新增標籤到表單
     * @param {string} tag - 要新增的標籤
     */
    const addTagToForm = (tag) => {
      if (tag && !form.tags.includes(tag)) {
        form.tags.push(tag);
      }
    };

    /**
     * 重點註解：從表單移除標籤
     * @param {number} index - 要移除的標籤索引
     */
    const removeTagFromForm = (index) => {
      if (index >= 0 && index < form.tags.length) {
        form.tags.splice(index, 1);
      }
    };

    // === 生命週期 ===
    onMounted(async () => {
      console.log('ActivityList 元件已掛載，開始載入活動資料...');
      await refreshActivities();
    });

    // === 回傳 ===
    return {
      // 狀態
      activities: composableActivities,
      loading: composableLoading,
      detailLoading,
      searchText,
      activeTab,
      isEditMode,
      selectedActivity,
      form,

      // 篩選相關
      selectedCategory,
      selectedLocation,
      selectedInstructor,
      categories,
      locations,
      instructors,
      isAnyFilterActive,

      // 計算屬性
      activeActivities,
      endedActivities,
      filteredActiveActivities,
      filteredEndedActivities,

      // 方法
      refreshActivities,
      formatActivityDate,
      formatEndDate,       // 重點註解：確保 formatEndDate 方法被回傳
      calculateProgress,
      getRegistrationDateInfo,
      handleImageError,
      getSafeImageUrl,
      endRegistration,
      clearSearch,
      clearAllFilters,
      setCategory,
      performSearch,
      openAddModal,
      openEditModal,
      viewActivity,        // 修復後的查看詳情方法
      editFromView,        // 從詳情編輯的方法
      submitForm,
      handleActivityAdded, // 重點註解：將新方法回傳給模板使用
      deleteActivity,
      addTagToForm,        // 重點註解：修正方法名稱
      removeTagFromForm    // 重點註解：修正方法名稱
    };
  }
}
</script>

<style scoped>
/* 重點註解：活動卡片基本樣式 */
.activity-card {
  transition: all 0.3s ease;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  border: none;
  display: flex;
  flex-direction: column;
}

.activity-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 15px rgba(0, 0, 0, 0.1);
}

/* 重點註解：確保卡片主體內容可以彈性調整 */
.card-body {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.activity-info {
  flex: 1;
}

/* 重點註解：結束活動的視覺樣式 */
.ended-activity {
  opacity: 0.85;
  background-color: #f8f9fa;
}

/* 重點註解：活動圖片容器樣式 */
.activity-image-container {
  position: relative;
  height: 200px;
  overflow: hidden;
  border-radius: 0.375rem 0.375rem 0 0;
  background-color: #f8f9fa;
}

.activity-image {
  object-fit: contain;
  object-position: center;
  height: 100%;
  width: 100%;
  background-color: #f8f9fa;
  transition: transform 0.3s ease;
}

.activity-card:hover .activity-image {
  transform: scale(1.05);
}

/* 重點註解：活動狀態標籤樣式 */
.activity-status-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  padding: 5px 10px;
  border-radius: 20px;
  font-size: 0.75rem;
  font-weight: 600;
  color: #fff;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.2);
  z-index: 1;
}

/* 重點註解：活動標題樣式 */
.activity-title {
  font-weight: 600;
  margin-bottom: 15px;
  height: 48px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
}

/* 重點註解：報名區塊樣式 */
.registration-section {
  margin-top: auto;
  padding-top: 12px;
  border-top: 1px solid #e9ecef;
}

.registration-period {
  background-color: #f8f9fa;
  padding: 8px 12px;
  border-radius: 6px;
  border-left: 3px solid #0d6efd;
}

.ended-activity .registration-period {
  border-left-color: #6c757d;
}

/* 重點註解：操作按鈕樣式優化 - 放大字體 */
.card-footer {
  padding: 5px;
  margin-top: auto;
}

.action-btn {
  height: 65px; /* 重點註解：增加按鈕高度以容納更大的字體 */
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  transition: all 0.2s ease;
  border-radius: 8px;
  font-weight: 500;
}

.action-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.action-btn i {
  font-size: 18px; /* 重點註解：圖示從 16px 放大到 18px */
  margin-bottom: 4px;
}

.action-btn small {
  font-size: 15px; /* 重點註解：文字從 11px 放大到 13px */
  font-weight: 600;
  line-height: 1.2; /* 重點註解：調整行高避免文字擠在一起 */
}

/* 重點註解：不同按鈕類型的 hover 效果 */
.btn-outline-warning:hover {
  background-color: #ffc107;
  border-color: #ffc107;
  color: #000;
}

.btn-outline-primary:hover {
  background-color: #0d6efd;
  border-color: #0d6efd;
  color: #fff;
}

.btn-outline-info:hover {
  background-color: #0dcaf0;
  border-color: #0dcaf0;
  color: #000;
}

.btn-outline-secondary:hover {
  background-color: #6c757d;
  border-color: #6c757d;
  color: #fff;
}

.btn-outline-danger:hover {
  background-color: #dc3545;
  border-color: #dc3545;
  color: #fff;
}

/* 重點註解：禁用狀態樣式 */
.action-btn:disabled {
  opacity: 0.6;
  transform: none;
  box-shadow: none;
}

/* 重點註解：響應式設計 - 調整各螢幕尺寸的字體大小 */
@media (max-width: 768px) {
  .activity-image-container {
    height: 150px;
  }
  
  .action-btn {
    height: 55px; /* 重點註解：中型螢幕稍微縮小高度 */
    padding: 4px;
  }
  
  .action-btn i {
    font-size: 16px; /* 重點註解：中型螢幕的圖示大小 */
    margin-bottom: 2px;
  }
  
  .action-btn small {
    font-size: 12px; /* 重點註解：中型螢幕的文字大小 */
  }
  
  .card-footer {
    padding: 12px;
  }
}

@media (max-width: 576px) {
  .activity-image-container {
    height: 120px;
  }
  
  .action-btn {
    height: 50px; /* 重點註解：小螢幕保持合適的高度 */
    padding: 3px;
  }
  
  .action-btn i {
    font-size: 14px; /* 重點註解：小螢幕的圖示大小 */
  }
  
  .action-btn small {
    font-size: 11px; /* 重點註解：小螢幕的文字大小 */
  }
  
  .card-footer {
    padding: 10px;
  }
}

/* 重點註解：超小螢幕額外優化 */
@media (max-width: 480px) {
  .action-btn {
    height: 48px;
  }
  
  .action-btn i {
    font-size: 13px;
    margin-bottom: 1px;
  }
  
  .action-btn small {
    font-size: 10px;
    font-weight: 700; /* 重點註解：小螢幕增加字體粗細確保可讀性 */
  }
}

/* 重點註解：進度條樣式優化 */
.progress {
  background-color: #e9ecef;
  border-radius: 3px;
}

.progress-bar {
  border-radius: 3px;
  transition: width 0.6s ease;
}
</style>
