<template>
  <div class="mb-3">
    <label class="form-label">活動圖片</label>
    
    <!-- 圖片預覽 -->
    <div v-if="imagePreview" class="mb-3">
      <div class="image-preview-container">
        <img 
          :src="imagePreview" 
          class="img-thumbnail image-preview" 
          alt="圖片預覽"
          @error="handlePreviewError"
        >
        <button 
          type="button" 
          class="btn btn-danger btn-sm position-absolute top-0 end-0 m-2"
          @click="clearImage"
          title="移除圖片"
        >
          <i class="fas fa-times"></i>
        </button>
      </div>
    </div>
    
    <!-- URL 輸入 -->
    <div class="input-group mb-2">
      <input 
        type="url" 
        class="form-control" 
        placeholder="請輸入圖片網址 (http://... 或 https://...)"
        :value="imageUrl"
        @input="handleImageUrlInput"
        @blur="validateAndSetImage"
      >
      <button 
        type="button" 
        class="btn btn-outline-primary" 
        @click="validateAndSetImage"
        :disabled="!localImageUrl"
      >
        <i class="fas fa-eye me-1"></i>預覽
      </button>
    </div>
    
    <!-- 檔案上傳 (如果有實作後端上傳 API) -->
    <div class="input-group">
      <input 
        type="file" 
        class="form-control" 
        accept="image/*"
        @change="handleFileUpload"
        ref="fileInput"
      >
      <button 
        type="button" 
        class="btn btn-outline-secondary" 
        @click="clearFileInput"
        title="清除檔案"
      >
        <i class="fas fa-times"></i>
      </button>
    </div>
    
    <!-- 提示文字 -->
    <div class="form-text">
      支援格式：JPG, PNG, GIF, WebP。建議尺寸：400x250px 或相近比例。
    </div>
    
    <!-- 錯誤訊息 -->
    <div v-if="errorMessage" class="alert alert-danger mt-2">
      {{ errorMessage }}
    </div>
  </div>
</template>

<script>
import { ref, watch, computed } from 'vue';

export default {
  name: 'ActivityImageUpload',
  props: {
    // 使用 v-model:imageUrl 綁定圖片網址
    imageUrl: {
      type: String,
      default: ''
    }
  },
  emits: ['update:imageUrl'],
  setup(props, { emit }) {
    const imagePreview = ref('');
    const errorMessage = ref('');
    const fileInput = ref(null);
    const localImageUrl = ref(props.imageUrl || '');
    
    // 處理圖片 URL 輸入
    const handleImageUrlInput = (event) => {
      localImageUrl.value = event.target.value;
      emit('update:imageUrl', event.target.value);
    };
    
    // 驗證並設定圖片
    const validateAndSetImage = async () => {
      const urlToValidate = localImageUrl.value || props.imageUrl;
      
      if (!urlToValidate) {
        clearImage();
        return;
      }
      
      try {
        // 驗證 URL 格式
        new URL(urlToValidate);
        
        // 檢查圖片是否能正確載入
        const img = new Image();
        img.onload = () => {
          imagePreview.value = urlToValidate;
          errorMessage.value = '';
          localImageUrl.value = urlToValidate;
          emit('update:imageUrl', urlToValidate);
        };
        img.onerror = () => {
          errorMessage.value = '無法載入此圖片，請檢查網址是否正確';
          imagePreview.value = '';
        };
        img.src = urlToValidate;
        
      } catch (error) {
      }
    };
    
    // 處理檔案上傳
    const handleFileUpload = (event) => {
      const file = event.target.files[0];
      if (!file) return;
      
      // 檢查檔案類型
      if (!file.type.startsWith('image/')) {
        errorMessage.value = '請選擇圖片檔案';
        return;
      }
      
      // 檢查檔案大小 (限制 5MB)
      if (file.size > 5 * 1024 * 1024) {
        errorMessage.value = '檔案大小不能超過 5MB';
        return;
      }
      
      // 創建本地預覽
      const reader = new FileReader();
      reader.onload = (e) => {
        imagePreview.value = e.target.result;
        errorMessage.value = '';
        localImageUrl.value = e.target.result;
        
        // TODO: 這裡需要實作上傳到後端的邏輯
        // 暫時使用本地預覽 URL
        emit('update:imageUrl', e.target.result);
      };
      reader.readAsDataURL(file);
    };
    
    // 處理預覽圖片載入錯誤
    const handlePreviewError = () => {
      errorMessage.value = '圖片載入失敗';
      imagePreview.value = '';
    };
    
    // 清除圖片
    const clearImage = () => {
      imagePreview.value = '';
      errorMessage.value = '';
      localImageUrl.value = '';
      emit('update:imageUrl', '');
    };
    
    // 清除檔案輸入
    const clearFileInput = () => {
      if (fileInput.value) {
        fileInput.value.value = '';
      }
    };
    
    // 監聽外部 imageUrl 變化
    watch(() => props.imageUrl, (newUrl) => {
      localImageUrl.value = newUrl || '';
      if (newUrl && newUrl !== imagePreview.value) {
        validateAndSetImage();
      } else if (!newUrl) {
        imagePreview.value = '';
        errorMessage.value = '';
      }
    }, { immediate: true });
    
    return {
      imagePreview,
      errorMessage,
      fileInput,
      localImageUrl,
      handleImageUrlInput,
      validateAndSetImage,
      handleFileUpload,
      handlePreviewError,
      clearImage,
      clearFileInput
    };
  }
}
</script>

<style scoped>
.image-preview-container {
  position: relative;
  display: inline-block;
  max-width: 300px;
}

.image-preview {
  max-width: 100%;
  height: auto;
  max-height: 200px;
  border-radius: 8px;
}

.form-control:invalid {
  border-color: #dc3545;
}

.alert {
  font-size: 0.875rem;
  padding: 0.5rem 0.75rem;
  border-radius: 6px;
}

/* 改善圖片預覽容器的樣式 */
.image-preview-container {
  border: 2px dashed #dee2e6;
  border-radius: 8px;
  padding: 1rem;
  background-color: #f8f9fa;
  transition: all 0.3s ease;
}

.image-preview-container:hover {
  border-color: #0d6efd;
  background-color: #f0f7ff;
}

/* 按鈕樣式改進 */
.btn-outline-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>