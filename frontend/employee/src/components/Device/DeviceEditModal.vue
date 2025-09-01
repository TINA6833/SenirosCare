<template>
  <div v-if="show" class="modal-backdrop" @click.self="closeModal">
    <div class="modal-container">
      <!-- 彈窗標題 -->
      <div class="modal-header">
        <div class="d-flex align-items-center">
          <iconify-icon icon="lucide:edit" width="22" height="22" class="me-2" />
          <h5 class="modal-title mb-0">編輯輔具</h5>
        </div>
        <button type="button" class="btn-close" @click="closeModal"></button>
      </div>

      <!-- 彈窗內容 -->
      <div class="modal-body">
        <div v-if="loading" class="d-flex flex-column align-items-center justify-content-center py-5">
          <div class="spinner-border text-primary" role="status">
            <span class="visually-hidden">載入中...</span>
          </div>
          <p class="mt-3 text-muted">載入輔具資料中...</p>
        </div>

        <form v-else @submit.prevent="confirmSave" class="device-form">
          <div class="row g-4">
            <!-- 左側：圖片上傳區 -->
            <div class="col-md-4">
              <div class="image-upload-section">
                <label class="form-label fw-medium">輔具圖片</label>
                <div class="image-upload-container">
                  <!-- 顯示預覽圖或現有圖片 -->
                  <img 
                    :src="getImageUrl(previewImage || formData.image)" 
                    alt="輔具圖片"
                    class="device-image mb-3"
                    @error="handleImageError"
                  />
                  
                  <!-- 圖片上傳按鈕 -->
                  <input 
                    type="file" 
                    ref="imageInput" 
                    class="d-none" 
                    accept="image/*" 
                    @change="handleImageChange"
                  />
                  <button 
                    type="button" 
                    class="btn btn-outline-primary btn-sm w-100"
                    @click="triggerImageUpload"
                  >
                    <iconify-icon icon="majesticons:upload-line" class="me-1" />
                    上傳圖片
                  </button>
                </div>
              </div>
            </div>

            <!-- 右側：表單欄位 -->
            <div class="col-md-8">
              <!-- 輔具名稱 -->
              <div class="mb-3">
                <label for="deviceName" class="form-label">輔具名稱 <span class="text-danger">*</span></label>
                <input 
                  type="text" 
                  id="deviceName" 
                  v-model="formData.name" 
                  class="form-control"
                  :class="{ 'is-invalid': validationErrors.name }"
                  placeholder="請輸入輔具名稱"
                  required
                />
                <div v-if="validationErrors.name" class="invalid-feedback">
                  {{ validationErrors.name }}
                </div>
              </div>

              <!-- SKU -->
              <div class="mb-3">
                <label for="deviceSku" class="form-label">SKU <span class="text-danger">*</span></label>
                <input 
                  type="text" 
                  id="deviceSku" 
                  v-model="formData.sku" 
                  class="form-control"
                  :class="{ 'is-invalid': validationErrors.sku }"
                  placeholder="請輸入 SKU 編號"
                  required
                />
                <div v-if="validationErrors.sku" class="invalid-feedback">
                  {{ validationErrors.sku }}
                </div>
              </div>

              <!-- 單價與庫存 (並排) -->
              <div class="row mb-3">
                <div class="col-md-6">
                  <label for="devicePrice" class="form-label">單價 <span class="text-danger">*</span></label>
                  <input 
                    type="number" 
                    id="devicePrice" 
                    v-model.number="formData.unitPrice" 
                    class="form-control"
                    :class="{ 'is-invalid': validationErrors.unitPrice }"
                    min="0"
                    step="1"
                    required
                  />
                  <div v-if="validationErrors.unitPrice" class="invalid-feedback">
                    {{ validationErrors.unitPrice }}
                  </div>
                </div>
                <div class="col-md-6">
                  <label for="deviceInventory" class="form-label">庫存數量 <span class="text-danger">*</span></label>
                  <input 
                    type="number" 
                    id="deviceInventory" 
                    v-model.number="formData.inventory" 
                    class="form-control"
                    :class="{ 'is-invalid': validationErrors.inventory }"
                    min="0"
                    step="1"
                    required
                  />
                  <div v-if="validationErrors.inventory" class="invalid-feedback">
                    {{ validationErrors.inventory }}
                  </div>
                </div>
              </div>

              <!-- 類別 -->
              <div class="mb-3">
                <label for="deviceCategory" class="form-label">類別</label>
                <div v-if="categoriesLoading" class="d-flex align-items-center">
                  <div class="spinner-border spinner-border-sm text-secondary me-2" role="status"></div>
                  <small class="text-muted">載入類別中...</small>
                </div>
                <select
                  v-else
                  id="deviceCategory"
                  v-model="formData.categoryId"
                  class="form-select"
                  :class="{ 'is-invalid': validationErrors.categoryId }"
                >
                  <option value="" selected>請選擇輔具類別</option>
                  <option v-for="cat in categories" :key="cat.id" :value="cat.id">
                    {{ cat.name }} (ID: {{ cat.id }})
                  </option>
                </select>
                <div v-if="validationErrors.categoryId" class="invalid-feedback">
                  {{ validationErrors.categoryId }}
                </div>
                <!-- 新增：顯示偵錯資訊 -->
                <small v-if="categories && categories.length" class="text-muted mt-1">
                  目前選擇的分類ID: {{ formData.categoryId || '未選擇' }}
                </small>
              </div>

              <!-- 輔具狀態 -->
              <div class="mb-3">
                <label class="form-label d-block">狀態</label>
                <div class="form-check form-check-inline">
                  <input 
                    type="radio" 
                    id="statusOnline" 
                    v-model="formData.isOnline" 
                    :value="true" 
                    class="form-check-input"
                  />
                  <label for="statusOnline" class="form-check-label">已上架</label>
                </div>
                <div class="form-check form-check-inline">
                  <input 
                    type="radio" 
                    id="statusOffline" 
                    v-model="formData.isOnline" 
                    :value="false" 
                    class="form-check-input"
                  />
                  <label for="statusOffline" class="form-check-label">未上架</label>
                </div>
              </div>

              <!-- 描述 -->
              <div class="mb-3">
                <label for="deviceDescription" class="form-label">描述</label>
                <textarea 
                  id="deviceDescription" 
                  v-model="formData.description" 
                  class="form-control"
                  rows="4"
                  placeholder="請輸入輔具詳細描述"
                ></textarea>
              </div>
            </div>
          </div>
        </form>
      </div>

      <!-- 彈窗底部按鈕 - 加入 console.log -->
      <div class="modal-footer">
        <button 
          type="button" 
          class="btn btn-light" 
          @click="handleCancel"
        >
          取消
        </button>
        <button 
          type="button" 
          class="btn btn-primary" 
          @click="confirmSave"
          :disabled="submitting || loading"
        >
          <span v-if="submitting" class="spinner-border spinner-border-sm me-1" role="status" aria-hidden="true"></span>
          {{ submitting ? '儲存中...' : '儲存變更' }}
        </button>
      </div>
    </div>
  </div>

  <!-- 新增：確認對話框 -->
  <div v-if="showConfirmDialog" class="modal-backdrop" style="z-index: 1060;">
    <div class="confirm-dialog">
      <div class="confirm-header">
        <h5 class="mb-0">確認修改</h5>
      </div>
      <div class="confirm-body">
        <p>確定要修改「{{ formData.name }}」的資料嗎？</p>
      </div>
      <div class="confirm-footer">
        <button type="button" class="btn btn-light" @click="cancelSave">取消</button>
        <button type="button" class="btn btn-primary" @click="handleSave">確認修改</button>
      </div>
    </div>
  </div>

  <!-- 新增：成功提示 -->
  <div v-if="showSuccessToast" class="toast-container position-fixed top-0 end-0 p-3">
    <div class="toast show bg-success text-white" role="alert" aria-hidden="true">
      <div class="toast-header bg-success text-white">
        <iconify-icon icon="mdi:check-circle" class="me-2" />
        <strong class="me-auto">成功</strong>
        <button type="button" class="btn-close btn-close-white" @click="showSuccessToast = false"></button>
      </div>
      <div class="toast-body">
        輔具資料已成功更新！
      </div>
    </div>
  </div>

  <!-- 新增：失敗提示 -->
  <div v-if="showErrorToast" class="toast-container position-fixed top-0 end-0 p-3">
    <div class="toast show bg-danger text-white" role="alert" aria-hidden="true">
      <div class="toast-header bg-danger text-white">
        <iconify-icon icon="mdi:alert-circle" class="me-2" />
        <strong class="me-auto">錯誤</strong>
        <button type="button" class="btn-close btn-close-white" @click="showErrorToast = false"></button>
      </div>
      <div class="toast-body">
        {{ errorMessage }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted } from 'vue'
import { useDevice } from '@/composables/useDevice'
// 引入類別相關 composable
import { useDeviceCategory } from '@/composables/useDeviceCategory'

// Props - 從父元件接收
const props = defineProps({
  show: Boolean, // 控制彈窗顯示
  deviceId: {
    type: [Number, String],
    default: null
  }
})

// Emits - 事件向父元件發送
const emit = defineEmits(['close', 'updated'])

// 使用 useDevice composable 取得相關方法
const { loadDevice, modifyDevice, uploadImage } = useDevice()

// 使用 useDeviceCategory composable 取得分類相關方法
const { categories, loading: categoriesLoading, loadCategories } = useDeviceCategory()

// 狀態變數
const loading = ref(false) // 資料載入中
const submitting = ref(false) // 表單提交中
const previewImage = ref(null) // 圖片預覽
const imageFile = ref(null) // 上傳的圖片檔案
const imageInput = ref(null) // 圖片上傳 input 參考

// 新增：提示訊息和確認對話框狀態
const showConfirmDialog = ref(false) // 確認對話框
const showSuccessToast = ref(false) // 成功提示
const showErrorToast = ref(false) // 錯誤提示
const errorMessage = ref('更新輔具資料失敗，請稍後再試') // 錯誤訊息

// 表單資料
const formData = reactive({
  name: '',
  sku: '',
  unitPrice: 0,
  inventory: 0,
  category: '',     // 保留原有 category 欄位，用於顯示分類名稱
  categoryId: '',   // 新增 categoryId 欄位，用於儲存分類 ID
  isOnline: false,
  description: '',
  image: ''
})

// 表單驗證錯誤
const validationErrors = reactive({
  name: '',
  sku: '',
  unitPrice: '',
  inventory: '',
  categoryId: ''
})

// 監聽 deviceId 和 show 變化，載入資料
watch(
  () => [props.deviceId, props.show], 
  async ([newId, newShow]) => {
    if (newId && newShow) {
      await loadDeviceData(newId)
    }
  },
  { immediate: true }
)

// 圖片路徑處理
const baseImageUrl = 'http://localhost:8080'
function getImageUrl(imagePath) {
  if (!imagePath) {
    return '/assets/images/default-device.jpg'
  }
  
  // 如果是 Data URL (預覽圖片)
  if (imagePath.startsWith('data:')) {
    return imagePath
  }
  
  // 如果已經是完整 URL，直接返回
  if (imagePath.startsWith('http')) {
    return imagePath
  }
  
  // 否則拼接基礎 URL
  return `${baseImageUrl}/${imagePath}`
}

// 載入輔具資料
async function loadDeviceData(id) {
  if (!id) return
  
  try {
    loading.value = true
    
    // 重置表單錯誤
    Object.keys(validationErrors).forEach(key => validationErrors[key] = '')
    
    // 載入輔具資料
    const deviceData = await loadDevice(id)
    
    if (deviceData) {
      // 更新表單資料
      formData.name = deviceData.name || ''
      formData.sku = deviceData.sku || ''
      formData.unitPrice = deviceData.unitPrice || 0
      formData.inventory = deviceData.inventory || 0
      formData.category = deviceData.category || ''
      
      // 確保 categoryId 型別處理正確
      // 如果後端回傳的是字串但前端比對需要數字，或反之，進行轉換
      if (deviceData.categoryId) {
        console.log('後端回傳的 categoryId:', deviceData.categoryId, '型別:', typeof deviceData.categoryId)
        // 這裡根據實際情況決定是否需要轉型
        formData.categoryId = deviceData.categoryId
      } else {
        formData.categoryId = ''
      }
      
      formData.isOnline = deviceData.isOnline || false
      formData.description = deviceData.description || ''
      formData.image = deviceData.image || ''
      
      // 重置圖片預覽
      previewImage.value = null
      imageFile.value = null
    }
  } catch (err) {
    console.error('載入輔具資料失敗:', err)
    // 顯示錯誤提示
    errorMessage.value = '載入輔具資料失敗，請稍後再試'
    showErrorToast.value = true
  } finally {
    loading.value = false
  }
}

// 觸發圖片上傳
function triggerImageUpload() {
  // 使用 ref 來訪問 DOM 元素並觸發點擊事件
  imageInput.value.click()
}

// 處理圖片變更
function handleImageChange(event) {
  const file = event.target.files[0]
  if (!file) return
  
  // 檢查檔案類型
  if (!file.type.startsWith('image/')) {
    alert('請上傳圖片檔案')
    return
  }
  
  // 儲存檔案供後續上傳
  imageFile.value = file
  
  // 生成預覽圖片
  const reader = new FileReader()
  reader.onload = (e) => {
    previewImage.value = e.target.result
  }
  reader.readAsDataURL(file)
}

// 處理圖片載入錯誤
function handleImageError(e) {
  e.target.src = '/assets/images/default-device.jpg'
}

// 表單驗證
function validateForm() {
  let isValid = true
  
  // 重設錯誤訊息
  Object.keys(validationErrors).forEach(key => {
    validationErrors[key] = ''
  })
  
  // 驗證必填欄位
  if (!formData.name.trim()) {
    validationErrors.name = '請輸入輔具名稱'
    isValid = false
  }
  
  if (!formData.sku.trim()) {
    validationErrors.sku = '請輸入 SKU'
    isValid = false
  }
  
  if (formData.unitPrice < 0) {
    validationErrors.unitPrice = '單價不可為負數'
    isValid = false
  }
  
  if (formData.inventory < 0) {
    validationErrors.inventory = '庫存不可為負數'
    isValid = false
  }
  
  return isValid
}

// 新增：確認儲存（顯示確認對話框）
function confirmSave() {
  console.log('確認儲存被呼叫')
  
  // 先驗證表單
  if (!validateForm()) {
    console.log('表單驗證失敗')
    // 顯示表單驗證錯誤提示
    errorMessage.value = '請檢查表單資料是否正確'
    showErrorToast.value = true
    
    // 設定 3 秒後自動關閉錯誤提示
    setTimeout(() => {
      showErrorToast.value = false
    }, 1000)
    
    return
  }
  
  // 顯示確認對話框
  showConfirmDialog.value = true
}

// 新增：取消儲存（關閉確認對話框）
function cancelSave() {
  showConfirmDialog.value = false
}

// 儲存變更 - 不改動原有功能
async function saveChanges() {
  // 驗證表單（雖然前面已驗證，但保留原有邏輯）
  if (!validateForm()) {
    return
  }
  
  try {
    submitting.value = true
    
    // 先處理圖片上傳 (如果有新上傳的圖片)
    if (imageFile.value) {
      const imageUrl = await uploadImage(props.deviceId, imageFile.value)
      formData.image = imageUrl // 更新圖片路徑
    }
    
    // 根據選擇的類別 ID 更新類別名稱
    if (formData.categoryId) {
      console.log('處理類別資料，目前選擇的 categoryId:', formData.categoryId)
      const selectedCategory = categories.value.find(c => c.id == formData.categoryId)  // 使用 == 進行寬鬆比對
      if (selectedCategory) {
        formData.category = selectedCategory.name
        console.log('找到對應的類別名稱:', selectedCategory.name)
      } else {
        console.warn('找不到對應 ID 的類別:', formData.categoryId)
      }
    }
    
    // 更新輔具資料
    const updatedDevice = await modifyDevice(props.deviceId, {
      name: formData.name,
      sku: formData.sku,
      unitPrice: formData.unitPrice,
      inventory: formData.inventory,
      category: formData.category,
      categoryId: formData.categoryId,  // 新增類別 ID
      isOnline: formData.isOnline,
      description: formData.description,
      image: formData.image
    })
    
    // 顯示成功提示
    showSuccessToast.value = true
    
    // 設定 3 秒後自動關閉成功提示
    setTimeout(() => {
      showSuccessToast.value = false
    }, 1000)
    
    // 通知父元件更新成功
    emit('updated', updatedDevice)
    
    // 延遲關閉彈窗，讓使用者有時間看到成功提示
    setTimeout(() => {
      closeModal()
    }, 1000)
    
  } catch (err) {
    console.error('更新輔具失敗:', err)
    // 顯示錯誤提示，取代原有的 alert
    errorMessage.value = '更新輔具資料失敗，請稍後再試'
    showErrorToast.value = true
    
    // 設定 5 秒後自動關閉錯誤提示
    setTimeout(() => {
      showErrorToast.value = false
    }, 1000)
  } finally {
    submitting.value = false
  }
}

// 關閉彈窗 - 保留原有功能
function closeModal() {
  // 通知父元件關閉彈窗
  emit('close')
}

// 新增測試方法來檢查按鈕點擊 - 保留原有功能
function handleCancel() {
  console.log('取消按鈕被點擊')
  console.log('props:', props)
  console.log('emit 事件前')
  closeModal()
  console.log('emit 事件後')
}

// 儲存按鈕點擊處理 - 修改為先關閉確認對話框再儲存
function handleSave() {
  console.log('儲存變更按鈕被點擊')
  console.log('驗證表單前')
  // 關閉確認對話框
  showConfirmDialog.value = false
  // 呼叫原有的儲存功能
  saveChanges()
  console.log('saveChanges 執行後')
}

// 確保在元件掛載時載入類別資料
onMounted(async () => {
  console.log('載入類別資料中...')
  try {
    await loadCategories()
    console.log('類別資料載入完成:', categories.value)
  } catch (error) {
    console.error('載入類別資料失敗:', error)
    errorMessage.value = '載入輔具類別失敗，請稍後再試'
    showErrorToast.value = true
  }
})
</script>

<style scoped>
/* 彈窗背景遮罩 */
.modal-backdrop {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(33, 37, 41, 0.6);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1050;
  backdrop-filter: blur(3px);
}

/* 彈窗容器 */
.modal-container {
  width: 100%;
  max-width: 800px;
  background-color: #fff;
  border-radius: 0.5rem;
  box-shadow: 0 0.5rem 1rem rgba(0, 0, 0, 0.15);
  margin: 1.5rem;
  animation: modal-fade-in 0.3s ease;
}

/* 彈窗標題區塊 */
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.25rem;
  border-bottom: 1px solid rgba(0, 0, 0, 0.1);
}

.modal-title {
  font-size: 1.1rem;
  font-weight: 600;
  color: #1e2022;
}

/* 彈窗內容區塊 */
.modal-body {
  padding: 1.25rem;
  max-height: 70vh;
  overflow-y: auto;
}

/* 彈窗底部按鈕區塊 */
.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
  padding: 1rem 1.25rem;
  border-top: 1px solid rgba(0, 0, 0, 0.1);
}

/* 圖片上傳區域 */
.image-upload-container {
  border: 1px solid #ddd;
  border-radius: 0.375rem;
  padding: 1rem;
  text-align: center;
  background-color: #f8f9fa;
}

.device-image {
  width: 100%;
  height: 180px;
  object-fit: contain;
  border-radius: 0.25rem;
  background-color: white;
  border: 1px solid #eee;
}

/* 表單樣式 */
.device-form {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

/* 表單驗證錯誤樣式 */
.is-invalid {
  border-color: #f46a6a;
}

.invalid-feedback {
  display: block;
  width: 100%;
  margin-top: 0.25rem;
  font-size: 0.875em;
  color: #f46a6a;
}

/* 彈窗動畫 */
@keyframes modal-fade-in {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 新增：確認對話框樣式 */
.confirm-dialog {
  width: 100%;
  max-width: 450px;
  background-color: #fff;
  border-radius: 0.5rem;
  box-shadow: 0 0.5rem 1rem rgba(0, 0, 0, 0.15);
  animation: modal-fade-in 0.2s ease;
}

.confirm-header {
  padding: 1.25rem;
  border-bottom: 1px solid rgba(0, 0, 0, 0.1);
  font-weight: 600;
}

.confirm-body {
  padding: 1.25rem;
}

.confirm-footer {
  padding: 1rem 1.25rem;
  border-top: 1px solid rgba(0, 0, 0, 0.1);
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
}

/* 新增：提示訊息樣式 */
.toast-container {
  z-index: 1070;
}

.toast {
  min-width: 300px;
  box-shadow: 0 0.5rem 1rem rgba(0, 0, 0, 0.15);
}

.toast-header {
  border-bottom: none;
}
</style>