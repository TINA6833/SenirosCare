<template>
  <!-- 新增輔具彈窗 -->
  <div
    class="modal fade"
    :class="{ show: show }"
    tabindex="-1"
    :style="{ display: show ? 'block' : 'none' }"
  >
    <div class="modal-dialog modal-dialog-centered modal-lg">
      <div class="modal-content">
        <!-- 彈窗標題 -->
        <div class="modal-header">
          <h5 class="modal-title">新增輔具</h5>
          <button
            type="button"
            class="btn-close"
            @click="handleClose"
          ></button>
        </div>

        <!-- 彈窗內容：表單 -->
        <div class="modal-body">
          <form @submit.prevent="submitForm">
            <!-- 表單欄位 -->
            <div class="row mb-3">
              <!-- 輔具名稱 -->
              <div class="col-md-6">
                <label for="name" class="form-label">輔具名稱 <span class="text-danger">*</span></label>
                <input 
                  type="text" 
                  class="form-control" 
                  id="name" 
                  v-model="formData.name" 
                  placeholder="請輸入輔具名稱"
                  required
                >
                <div class="form-text text-danger" v-if="errors.name">{{ errors.name }}</div>
              </div>

              <!-- SKU -->
              <div class="col-md-6">
                <label for="sku" class="form-label">SKU <span class="text-danger">*</span></label>
                <input 
                  type="text" 
                  class="form-control" 
                  id="sku" 
                  v-model="formData.sku" 
                  placeholder="請輸入唯一識別碼"
                  required
                >
                <div class="form-text text-danger" v-if="errors.sku">{{ errors.sku }}</div>
              </div>
            </div>

            <div class="row mb-3">
              <!-- 單價 -->
              <div class="col-md-6">
                <label for="unitPrice" class="form-label">單價 <span class="text-danger">*</span></label>
                <div class="input-group">
                  <span class="input-group-text">NT$</span>
                  <input 
                    type="number" 
                    class="form-control" 
                    id="unitPrice" 
                    v-model.number="formData.unitPrice"
                    min="0" 
                    placeholder="請輸入單價"
                    required
                  >
                </div>
                <div class="form-text text-danger" v-if="errors.unitPrice">{{ errors.unitPrice }}</div>
              </div>

              <!-- 庫存量 -->
              <div class="col-md-6">
                <label for="inventory" class="form-label">庫存量 <span class="text-danger">*</span></label>
                <input 
                  type="number" 
                  class="form-control" 
                  id="inventory" 
                  v-model.number="formData.inventory"
                  min="0"
                  placeholder="請輸入庫存數量" 
                  required
                >
                <div class="form-text text-danger" v-if="errors.inventory">{{ errors.inventory }}</div>
              </div>
            </div>

            <div class="row mb-3">
              <!-- 輔具分類選擇 -->
              <div class="col-md-6">
                <label for="categoryId" class="form-label">輔具分類 <span class="text-danger">*</span></label>
                <div class="input-group">
                  <select 
                    class="form-select" 
                    id="categoryId" 
                    v-model="formData.categoryId"
                    required
                  >
                    <option value="" disabled selected>請選擇輔具分類</option>
                    <!-- 使用 v-for 渲染分類選項 -->
                    <option 
                      v-for="category in categories" 
                      :key="category.id" 
                      :value="category.categoryId"
                    >
                      {{ category.name }}
                    </option>
                  </select>
                  <button 
                    type="button"
                    class="btn btn-outline-secondary"
                    @click="refreshCategories"
                    :disabled="categoriesLoading"
                  >
                    <i class="ri-refresh-line" :class="{'fa-spin': categoriesLoading}"></i>
                  </button>
                </div>
                <div class="form-text text-danger" v-if="errors.categoryId">{{ errors.categoryId }}</div>
              </div>

              <!-- 修改：負責員工改為直接輸入ID -->
              <div class="col-md-6">
                <label for="createdByEmpId" class="form-label">負責員工 ID <span class="text-danger">*</span></label>
                <input 
                  type="number" 
                  class="form-control" 
                  id="createdByEmpId" 
                  v-model.number="formData.createdByEmpId" 
                  placeholder="請輸入員工ID"
                  required
                  min="1"
                >
                <div class="form-text text-danger" v-if="errors.createdByEmpId">{{ errors.createdByEmpId }}</div>
              </div>
            </div>

            <!-- 詳細描述 -->
            <div class="mb-3">
              <label for="description" class="form-label">詳細描述</label>
              <textarea 
                class="form-control" 
                id="description" 
                v-model="formData.description" 
                rows="3"
                placeholder="請輸入輔具詳細資訊"
              ></textarea>
            </div>

            <!-- 輔具圖片 -->
            <div class="mb-3">
              <label for="image" class="form-label">輔具圖片</label>
              <input 
                type="file" 
                class="form-control" 
                id="image" 
                @change="handleImageSelect"
                accept="image/*"
              >
              <!-- 圖片預覽 -->
              <div v-if="imagePreview" class="mt-2 text-center">
                <img 
                  :src="imagePreview" 
                  alt="預覽圖" 
                  style="max-height: 150px; max-width: 100%;"
                  class="img-thumbnail"
                >
              </div>
            </div>

            <!-- 立即上架開關 -->
            <div class="mb-3 form-check form-switch">
              <input 
                type="checkbox" 
                class="form-check-input" 
                id="isOnline" 
                v-model="formData.isOnline"
              >
              <label class="form-check-label" for="isOnline">立即上架</label>
              <div class="form-text">勾選後，輔具將立即對客戶顯示</div>
            </div>

            <!-- 表單欄位下方新增一鍵填入按鈕 -->
            <div class="mb-3 text-end">
              <button type="button" class="btn btn-outline-info btn-sm" @click="fillDemoData">
                一鍵填入測試資料
              </button>
            </div>
          </form>
        </div>

        <!-- 彈窗底部：按鈕 -->
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" @click="handleClose">
            取消
          </button>
          <button 
            type="button" 
            class="btn btn-primary" 
            @click="submitForm" 
            :disabled="loading || categoriesLoading"
          >
            <span v-if="loading" class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
            儲存
          </button>
        </div>
      </div>
    </div>
  </div>

  <!-- 彈窗背景遮罩 -->
  <div 
    v-if="show" 
    class="modal-backdrop fade show"
    @click="handleClose"
  ></div>

  <!-- 新增成功提示視窗（z-index 提高，置中固定） -->
  <div
    v-if="showSuccessModal"
    class="success-modal-mask"
  >
    <div class="modal-dialog modal-dialog-centered modal-sm success-modal-dialog">
      <div class="modal-content text-center">
        <div class="modal-body p-4">
          <div class="mb-3">
            <i class="ri-checkbox-circle-fill text-success" style="font-size: 3rem;"></i>
          </div>
          <h5 class="mb-2" style="color: #34c38f;">新增成功</h5>
          <p class="mb-3">輔具已成功新增！</p>
          <button type="button" class="btn btn-success" @click="handleSuccessModalClose">確定</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
// 引入必要的函式和組件
import { ref, reactive, watch, onMounted } from 'vue';
import { useDevice } from '@/composables/useDevice';
import { useDeviceCategory } from '@/composables/useDeviceCategory';

// 定義組件接收的 props
const props = defineProps({
  show: {
    type: Boolean,
    default: false
  }
});

// 定義組件對外事件
const emit = defineEmits(['close', 'created']);

// 引入輔具相關 composable
const { addDevice, uploadImage, loading } = useDevice();

// 引入分類相關 composable
const { categories, loading: categoriesLoading, loadCategories } = useDeviceCategory();

// 表單資料
const formData = reactive({
  name: '',            // 輔具名稱
  sku: '',             // SKU 唯一識別碼
  unitPrice: 0,        // 單價
  inventory: 0,        // 庫存
  description: '',     // 詳細描述
  image: '',           // 圖片檔名
  isOnline: false,     // 是否上架
  categoryId: '',      // 分類ID欄位
  createdByEmpId: 1,   // 負責員工ID，預設為1
});

// 錯誤訊息
const errors = reactive({
  name: '',
  sku: '',
  unitPrice: '',
  inventory: '',
  categoryId: '',
  createdByEmpId: '',  // 員工錯誤訊息
});

// 圖片相關
const imageFile = ref(null);
const imagePreview = ref('');

// 新增：控制成功提示視窗顯示
const showSuccessModal = ref(false);

// 元件掛載時載入分類資料
onMounted(async () => {
  if (categories.value.length === 0) {
    await loadCategories();
  }
  
  // 初始化時嘗試設定當前登入使用者為負責人
  const currentUser = getCurrentUser();
  if (currentUser && currentUser.empId) {
    formData.createdByEmpId = currentUser.empId;
  }
});

// 當彈窗顯示時，載入資料
watch(() => props.show, async (newVal) => {
  if (newVal === true) {
    // 載入分類資料
    if (categories.value.length === 0) {
      await loadCategories();
    }
    
    // 嘗試設定當前登入使用者為負責人
    const currentUser = getCurrentUser();
    if (currentUser && currentUser.empId) {
      formData.createdByEmpId = currentUser.empId;
    }
  }
  
  if (newVal === false) {
    resetForm();
  }
});

// 處理圖片選擇
function handleImageSelect(event) {
  const file = event.target.files[0];
  if (!file) return;
  
  // 保存檔案引用
  imageFile.value = file;
  
  // 生成預覽圖
  const reader = new FileReader();
  reader.onload = (e) => {
    imagePreview.value = e.target.result;
  };
  reader.readAsDataURL(file);
}

// 表單驗證
function validateForm() {
  let isValid = true;
  
  // 重置錯誤訊息
  errors.name = '';
  errors.sku = '';
  errors.unitPrice = '';
  errors.inventory = '';
  errors.categoryId = '';
  errors.createdByEmpId = '';
  
  // 驗證輔具名稱
  if (!formData.name.trim()) {
    errors.name = '請輸入輔具名稱';
    isValid = false;
  }
  
  // 驗證 SKU
  if (!formData.sku.trim()) {
    errors.sku = '請輸入 SKU';
    isValid = false;
  }
  
  // 驗證單價
  if (formData.unitPrice <= 0) {
    errors.unitPrice = '單價必須大於 0';
    isValid = false;
  }
  
  // 驗證庫存
  if (formData.inventory < 0) {
    errors.inventory = '庫存量不可為負數';
    isValid = false;
  }
  
  // 驗證分類
  if (!formData.categoryId) {
    errors.categoryId = '請選擇輔具分類';
    isValid = false;
  }
  
  // 驗證負責員工 ID
  if (!formData.createdByEmpId || formData.createdByEmpId <= 0) {
    errors.createdByEmpId = '請輸入有效的員工 ID';
    isValid = false;
  }
  
  return isValid;
}

// 重新載入分類資料
async function refreshCategories() {
  await loadCategories();
}

// 獲取當前登入使用者資訊
function getCurrentUser() {
  // 從 localStorage 中獲取當前使用者資訊
  const userJson = localStorage.getItem('currentUser');
  if (userJson) {
    try {
      return JSON.parse(userJson);
    } catch (e) {
      console.error('解析使用者資料失敗:', e);
      return null;
    }
  }
  return null;
}

// 提交表單
async function submitForm() {
  // 先驗證表單
  if (!validateForm()) return;
  
  try {
    // 構建要傳送至後端的 JSON 資料
    // 確保 categoryId 和 createdByEmpId 為數字類型
    const dataToSubmit = { 
      name: formData.name,
      sku: formData.sku,
      unitPrice: Number(formData.unitPrice),
      inventory: Number(formData.inventory),
      description: formData.description,
      isOnline: formData.isOnline,
      categoryId: Number(formData.categoryId),
      createdByEmpId: Number(formData.createdByEmpId)
    };
    
    // 圖片處理 - 只傳送檔案名稱，實際檔案會在創建成功後上傳
    if (imageFile.value) {
      // 先從檔案中取出檔案名稱，後端會處理儲存路徑
      dataToSubmit.image = imageFile.value.name;
    }
    
    console.log('準備傳送至後端的資料:', JSON.stringify(dataToSubmit, null, 2));
    
    // 新增輔具基本資料
    const newDeviceId = await addDevice(dataToSubmit);
    console.log('輔具新增成功，ID:', newDeviceId);
    
    // 如果有選擇圖片，上傳圖片
    if (imageFile.value) {
      await uploadImage(newDeviceId, imageFile.value);
      console.log('輔具圖片上傳成功');
    }
    
    // 顯示成功提示視窗
    showSuccessModal.value = true;
    
    // 不要立即 emit('created') 與 handleClose()
  } catch (err) {
    console.error('新增輔具失敗:', err);
    
    // 處理可能的錯誤，例如 SKU 已存在
    if (err.response?.data?.message?.includes('SKU already exists')) {
      errors.sku = 'SKU 已存在，請使用其他 SKU';
    } else if (err.response?.data?.message?.includes('Category not found')) {
      errors.categoryId = '選擇的分類不存在';
    } else if (err.response?.data?.message?.includes('Employee not found')) {
      errors.createdByEmpId = '員工 ID 不存在，請確認後重試';
    } else if (err.response?.data?.message) {
      // 顯示後端回傳的錯誤訊息
      alert(`新增失敗: ${err.response.data.message}`);
    } else {
      // 顯示一般錯誤訊息
      alert('新增輔具失敗，請稍後再試');
    }
  }
}

// 新增：成功提示視窗關閉時，才真正 emit created 並關閉新增視窗
function handleSuccessModalClose() {
  showSuccessModal.value = false;
  emit('created');
  handleClose();
}

// 一鍵填入測試資料
function fillDemoData() {
  // 重點：這裡可依實際需求調整資料內容
  formData.name = '測試輔具';
  formData.sku = 'SKU' + Math.floor(Math.random() * 10000);
  formData.unitPrice = 999;
  formData.inventory = 10;
  formData.description = '這是測試用的輔具說明。';
  formData.isOnline = true;
  // 若有分類資料，預設選第一個分類
  if (categories.value.length > 0) {
    formData.categoryId = categories.value[0].categoryId;
  }
  // 預設員工ID
  formData.createdByEmpId = 1;
}

// 重置表單
function resetForm() {
  // 重置基本資料
  Object.keys(formData).forEach(key => {
    if (key === 'isOnline') {
      formData[key] = false;
    } else if (key === 'createdByEmpId') {
      // 保留預設的員工 ID 為 1
      formData[key] = 1;
    } else if (typeof formData[key] === 'number') {
      formData[key] = 0;
    } else {
      formData[key] = '';
    }
  });
  
  // 嘗試自動填入當前登入員工
  const currentUser = getCurrentUser();
  if (currentUser && currentUser.empId) {
    formData.createdByEmpId = currentUser.empId;
  }
  
  // 重置錯誤訊息
  Object.keys(errors).forEach(key => {
    errors[key] = '';
  });
  
  // 重置圖片
  imageFile.value = null;
  imagePreview.value = '';
}

// 關閉彈窗
function handleClose() {
  emit('close');
}
</script>

<style scoped>
/* 彈窗樣式 */
.modal {
  background-color: rgba(0, 0, 0, 0.4);
}

/* 必填標記 */
.text-danger {
  color: #f46a6a;
}

/* 表單標籤 */
.form-label {
  font-weight: 500;
}

/* 開關樣式 */
.form-switch .form-check-input:checked {
  background-color: #34c38f;
  border-color: #34c38f;
}

/* 預覽圖樣式 */
.img-thumbnail {
  border: 1px dashed #ccc;
}

/* 分類選擇器樣式 */
.form-select {
  background-color: #fff;
}

/* 旋轉圖示動畫 */
.fa-spin {
  animation: spin 1s infinite linear;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

/* 新增成功提示視窗遮罩，確保最高層且全螢幕覆蓋 */
.success-modal-mask {
  position: fixed;
  z-index: 20000;
  left: 0;
  top: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0,0,0,0.35);
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 讓成功提示視窗本體也有高 z-index與更寬的視覺 */
.success-modal-dialog {
  z-index: 21000;
  min-width: 340px;   /* 調整寬度，可依需求加大 */
  max-width: 95vw;
}

/* 讓內容圓角、陰影更明顯 */
.modal-content.text-center {
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.18);
  padding: 16px 0 24px 0;
}
.ri-checkbox-circle-fill.text-success {
  color: #34c38f;
  font-size: 3.5rem;
}
.modal-content.text-center h5 {
  font-size: 2rem;
  font-weight: bold;
}
.modal-content.text-center p {
  font-size: 1.1rem;
  margin-bottom: 1.5rem;
}
.btn.btn-success {
  min-width: 90px;
  font-size: 1.1rem;
  padding: 0.5rem 1.5rem;
}
</style>