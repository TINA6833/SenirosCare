<template>
  <div class="modal fade" id="activityModal" tabindex="-1" aria-labelledby="activityModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-xl">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title" id="activityModalLabel">
            {{ isEditMode ? '編輯活動' : '新增活動' }}
          </h5>
          <!-- 重點註解：在標題旁邊添加測試按鈕，只在新增模式時顯示 -->
          <div class="d-flex gap-2">
            <button v-if="!isEditMode" type="button" class="btn btn-outline-warning btn-sm" @click="fillTestData">
              <i class="fas fa-flask me-1"></i>測試資料
            </button>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
        </div>
        <div class="modal-body">
          <!-- 活動表單 -->
          <form @submit.prevent="onSubmit">
            <div class="row">
              <!-- 基本資訊 -->
              <div class="col-md-6">
                <h6 class="text-primary mb-3">基本資訊</h6>

                <!-- 活動名稱 -->
                <div class="mb-3">
                  <label for="activityName" class="form-label">活動名稱 <span class="text-danger">*</span></label>
                  <input type="text" class="form-control" id="activityName" v-model="formData.name" required
                    placeholder="請輸入活動名稱">
                </div>

                <!-- 活動類別 -->
                <div class="mb-3">
                  <label for="activityCategory" class="form-label">活動類別 <span class="text-danger">*</span></label>
                  <select class="form-select" id="activityCategory" v-model="formData.category" required>
                    <option value="">請選擇類別</option>
                    <option v-for="cat in categories" :key="cat" :value="cat">{{ cat }}</option>
                  </select>
                </div>

                <!-- 報名人數限制 -->
                <div class="row">
                  <div class="col-6">
                    <div class="mb-3">
                      <label for="activityLimit" class="form-label">報名限制 <span class="text-danger">*</span></label>
                      <input type="number" class="form-control" id="activityLimit" v-model.number="formData.limit"
                        min="1" required placeholder="請輸入人數限制">
                    </div>
                  </div>
                  <div class="col-6">
                    <div class="mb-3">
                      <label for="activityCurrent" class="form-label">目前報名人數</label>
                      <input type="number" class="form-control" id="activityCurrent" v-model.number="formData.current"
                        min="0">
                    </div>
                  </div>
                </div>

                <!-- 講師 -->
                <div class="mb-3">
                  <label for="activityInstructor" class="form-label">講師</label>
                  <input type="text" class="form-control" id="activityInstructor" v-model="formData.instructor">
                </div>

                <!-- 活動狀態 -->
                <div class="mb-3">
                  <label class="form-label">活動狀態</label>
                  <div class="form-check form-switch">
                    <input class="form-check-input" type="checkbox" id="activityStatus" v-model="formData.status">
                    <label class="form-check-label" for="activityStatus">
                      {{ formData.status ? '報名中' : '過去活動' }}
                    </label>
                  </div>
                </div>
              </div>

              <!-- 時間地點資訊 -->
              <div class="col-md-6">
                <h6 class="text-primary mb-3">時間地點資訊</h6>

                <!-- 活動日期 -->
                <div class="row">
                  <div class="col-6">
                    <div class="mb-3">
                      <label for="activityDate" class="form-label">活動開始日期 <span class="text-danger">*</span></label>
                      <input type="date" class="form-control" id="activityDate" v-model="formData.date" required>
                    </div>
                  </div>
                  <div class="col-6">
                    <div class="mb-3">
                      <label for="activityEnd" class="form-label">活動結束日期</label>
                      <input type="date" class="form-control" id="activityEnd" v-model="formData.end">
                    </div>
                  </div>
                </div>

                <!-- 活動時間 -->
                <div class="mb-3">
                  <label for="activityTime" class="form-label">活動時間</label>
                  <input type="text" class="form-control" id="activityTime" v-model="formData.time"
                    placeholder="例：09:00-17:00">
                </div>

                <!-- 報名期間 -->
                <div class="row">
                  <div class="col-6">
                    <div class="mb-3">
                      <label for="registrationStart" class="form-label">報名開始日期</label>
                      <input type="date" class="form-control" id="registrationStart"
                        v-model="formData.registrationStart">
                    </div>
                  </div>
                  <div class="col-6">
                    <div class="mb-3">
                      <label for="registrationEnd" class="form-label">報名結束日期</label>
                      <input type="date" class="form-control" id="registrationEnd" v-model="formData.registrationEnd">
                    </div>
                  </div>
                </div>

                <!-- 活動地點 -->
                <div class="mb-3">
                  <label for="activityLocation" class="form-label">活動地點</label>
                  <input type="text" class="form-control" id="activityLocation" v-model="formData.location">
                </div>

                <!-- 座標 (可選) -->
                <div class="row">
                  <div class="col-6">
                    <div class="mb-3">
                      <label for="activityLatitude" class="form-label">緯度</label>
                      <input type="number" step="any" class="form-control" id="activityLatitude"
                        v-model.number="formData.latitude">
                    </div>
                  </div>
                  <div class="col-6">
                    <div class="mb-3">
                      <label for="activityLongitude" class="form-label">經度</label>
                      <input type="number" step="any" class="form-control" id="activityLongitude"
                        v-model.number="formData.longitude">
                    </div>
                  </div>
                </div>
              </div>

              <!-- 完整寬度區域 -->
              <div class="col-12">
                <!-- 活動描述 -->
                <div class="mb-3">
                  <label for="activityDescription" class="form-label">活動描述</label>
                  <textarea class="form-control" id="activityDescription" rows="4" v-model="formData.description"
                    placeholder="請輸入活動的詳細描述..."></textarea>
                </div>

                <!-- 使用圖片上傳元件 -->
                <ActivityImageUpload v-model:imageUrl="formData.image" />

                <!-- 標籤管理 -->
                <ActivityFormTags v-model:tags="formData.tags" @add-tag="addTag" @remove-tag="removeTag" />
              </div>
            </div>
          </form>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">取消</button>
          <!-- 重點註解：新增活動按鈕，會呼叫 onSubmit 並最終觸發 activityService.addActivity -->
          <button type="button" class="btn btn-primary" @click="onSubmit" :disabled="loading || isSubmitting">
            <span v-if="loading || isSubmitting" class="spinner-border spinner-border-sm me-2" role="status"></span>
            {{ isEditMode ? '更新活動' : '新增活動' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, watch } from 'vue';
// 重點註解：直接引入 activityService
import { activityService } from '@/services/activityService';
import { useToast } from '@/composables/useToast';
import ActivityFormTags from './ActivityFormTags.vue';
import ActivityImageUpload from './ActivityImageUpload.vue';

export default {
  name: 'ActivityForm',
  components: {
    ActivityFormTags,
    ActivityImageUpload
  },
  props: {
    // 是否處於編輯模式
    isEditMode: {
      type: Boolean,
      default: false
    },
    // 表單資料
    form: {
      type: Object,
      required: true
    },
    // 載入狀態
    loading: {
      type: Boolean,
      default: false
    },
    categories: {
      type: Array,
      default: () => []
    }
  },
  // 重點註解：更新 emits，新增 'add-success' 事件用於通知父元件
  emits: ['submit', 'add-tag', 'remove-tag', 'add-success'],
  setup(props, { emit }) {
    const isSubmitting = ref(false);
    // 重點註解：引入 useToast 以顯示成功或失敗訊息
    const { showToast } = useToast();

    const formData = reactive({
      ...props.form,
      tags: Array.isArray(props.form.tags) ? [...props.form.tags] : []
    });

    // 監聽外部 form 資料變化
    watch(() => props.form, (newForm) => {
      Object.keys(newForm).forEach(key => {
        if (key === 'tags') {
          // 確保標籤是陣列格式
          formData[key] = Array.isArray(newForm[key]) ? [...newForm[key]] : [];
        } else {
          formData[key] = newForm[key];
        }
      });
    }, { deep: true });

    /**
     * 重點註解：關閉 Modal 的通用方法
     */
    const closeModal = () => {
      const modalElement = document.getElementById('activityModal');
      if (modalElement && typeof bootstrap !== 'undefined' && bootstrap.Modal) {
        const modal = bootstrap.Modal.getInstance(modalElement);
        if (modal) {
          console.log('正在關閉活動表單 Modal...');
          modal.hide();
        }
      }
    };

    /**
     * 重點註解：清除表單資料的方法
     */
    const resetForm = () => {
      console.log('清除表單資料...');
      Object.assign(formData, {
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
     * 自動填入測試資料的函數
     */
    const fillTestData = () => {
      const now = new Date();
      const tomorrow = new Date(now);
      tomorrow.setDate(tomorrow.getDate() + 1);
      
      const nextWeek = new Date(now);
      nextWeek.setDate(nextWeek.getDate() + 7);

      const formatDate = (date) => {
          return date.toISOString().split('T')[0];
      };

      // **重點：確保所有 @NotNull 欄位都有值**
      Object.assign(formData, {
          // 必填欄位
          name: '教你泡出一手好茶',
          category: props.categories.length > 0 ? props.categories[0] : '課程',
          location: '台北市信義區松仁路100號', // **重點：必填**
          time: '09:00-17:00', // **重點：必填**
          date: formatDate(tomorrow), // **重點：必填**
          end: formatDate(tomorrow), // **重點：必填**
          registrationStart: formatDate(tomorrow), // **重點：必填**
          registrationEnd: formatDate(nextWeek), // **重點：必填**
          
          // 其他欄位
          limit: 30,
          current: 0,
          instructor: '李大帥',
          status: true,
          description: '一家泡茶萬家香。',
          image: '/src/assets/images/tea.png', // 重點註解：使用茶葉預設圖片
          latitude: 25.0330,
          longitude: 121.5654,
          tags: ['測試', '自動填入', '茶藝']
      });

      console.log('已自動填入測試資料:', formData);
    };

    /**
     * 提交表單前的驗證
     */
    const validateForm = () => {
      const errors = [];
      
      // **重點：檢查所有必填欄位**
      if (!formData.name?.trim()) {
          errors.push('請輸入活動名稱');
      }
      if (!formData.category) {
          errors.push('請選擇活動類別');
      }
      if (!formData.location?.trim()) {
          errors.push('請輸入活動地點');
      }
      if (!formData.date) {
          errors.push('請選擇活動開始日期');
      }
      if (!formData.end) {
          errors.push('請選擇活動結束日期');
      }
      if (!formData.registrationStart) {
          errors.push('請選擇報名開始日期');
      }
      if (!formData.registrationEnd) {
          errors.push('請選擇報名結束日期');
      }
      
      if (errors.length > 0) {
          showToast({
              title: '表單驗證失敗',
              message: errors.join('、'),
              type: 'error'
          });
          return false;
      }
      
      return true;
    };

    /**
     * 提交表單函數
     * 現在會根據 isEditMode 決定是觸發更新事件，還是直接呼叫新增活動的 service
     * 更新成功後會自動關閉 Modal
     */
    const onSubmit = async () => {
      if (isSubmitting.value) return;

      // **重點：提交前先驗證表單**
      if (!validateForm()) {
        return;
      }

      isSubmitting.value = true;

      try {
        // 確保資料格式正確
        const submitData = {
          ...formData,
          tags: Array.isArray(formData.tags) ? formData.tags : [],
          limit: Number(formData.limit) || 30,
          current: Number(formData.current) || 0,
          name: formData.name?.trim(),
          category: formData.category?.trim(),
          location: formData.location?.trim(),
          time: formData.time?.trim() || "09:00"
        };

        console.log('準備提交的資料:', submitData);

        if (props.isEditMode) {
          // 編輯模式，交給父元件處理
          emit('submit', submitData);


          setTimeout(() => {
            closeModal(); // 關閉 Modal
          }, 500); // 延遲 0.5 秒讓使用者看到成功提示
        } else {
          // 新增模式，直接呼叫 service
          const result = await activityService.addActivity(submitData);

          if (result.success) {
            showToast({
              title: '新增成功',
              message: result.message,
              type: 'success'
            });

            emit('add-success');

            setTimeout(() => {
              closeModal();
              setTimeout(() => {
                resetForm();
              }, 300);
            }, 1000);
          } else {
            throw new Error(result.message || '新增活動失敗');
          }
        }
      } catch (error) {
        console.error('提交表單時發生錯誤:', error);
        showToast({
          title: '操作失敗',
          message: error.message,
          type: 'error'
        });
      } finally {
        isSubmitting.value = false;
      }
    };

    // 標籤管理
    const addTag = (tag) => {
      console.log('ActivityForm - 收到新增標籤事件:', tag);
      emit('add-tag', tag);
    };

    const removeTag = (index) => {
      console.log('ActivityForm - 收到移除標籤事件，索引:', index);
      emit('remove-tag', index);
    };

    return {
      formData,
      isSubmitting,
      onSubmit,
      fillTestData,
      addTag,
      removeTag,
      closeModal, // 重點註解：暴露關閉方法供外部使用
      resetForm   // 重點註解：暴露重置方法供外部使用
    };
  }
}
</script>

<style scoped>
.modal-xl {
  max-width: 90%;
}

.form-label {
  font-weight: 500;
  margin-bottom: 0.25rem;
}

.text-danger {
  color: #dc3545 !important;
}

.text-primary {
  color: #0d6efd !important;
}

/* 重點註解：測試按鈕的樣式，使其在視覺上與主要功能區分 */
.btn-outline-warning {
  border-color: #ffc107;
  color: #ffc107;
}

.btn-outline-warning:hover {
  background-color: #ffc107;
  border-color: #ffc107;
  color: #000;
}
</style>
