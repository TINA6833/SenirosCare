<template>
  <div class="modal-backdrop" v-if="show" @click.self="close">
    <div class="modal-dialog modal-dialog-centered modal-lg">
      <div class="modal-content">
        <!-- 標題與關閉按鈕 -->
        <div class="modal-header">
          <h5 class="modal-title d-flex align-items-center">
            <iconify-icon icon="mingcute:wheelchair-line" class="me-2" width="22" height="22" />
            輔具詳情
          </h5>
          <button type="button" class="btn-close" @click="close"></button>
        </div>
        
        <!-- 內容區 -->
        <div class="modal-body">
          <div v-if="device" class="device-detail-content">
            <div class="row g-4">
              <!-- 左側圖片區 -->
              <div class="col-md-4 mb-3 mb-md-0">
                <div class="device-image-wrapper">
                  <div class="device-status-badge" :class="device.isOnline ? 'online' : 'offline'">
                    {{ device.isOnline ? '已上架' : '未上架' }}
                  </div>
                  <img 
                    :src="deviceImage" 
                    class="img-fluid" 
                    alt="輔具圖片" 
                    @error="handleImageError"
                  />
                </div>
                <h3 class="device-title mt-3">{{ device.name }}</h3>
                <div class="sku-badge">SKU: {{ device.sku }}</div>
              </div>

              <!-- 右側詳細資訊區 -->
              <div class="col-md-8">
                <div class="info-card">
                  <div class="info-card-header">
                    <h6>基本資訊</h6>
                  </div>
                  <div class="info-card-body">
                    <div class="info-row">
                      <div class="info-label">單價</div>
                      <div class="info-value price-value">${{ device.unitPrice }}</div>
                    </div>
                    <div class="info-row">
                      <div class="info-label">庫存數量</div>
                      <div class="info-value">{{ device.inventory }} 件</div>
                    </div>
                    <div class="info-row">
                      <div class="info-label">類別</div>
                      <div class="info-value category-value">
                        <template v-if="categoryName">
                          <span class="category-badge">{{ categoryName }}</span>
                        </template>
                        <template v-else-if="device.category">
                          <span class="category-badge">{{ device.category }}</span>
                        </template>
                        <template v-else>
                          <span class="text-muted">無分類</span>
                        </template>
                        <!-- 顯示 categoryId (需要時可以開啟) -->
                        <small v-if="device.categoryId" class="text-muted ms-2">(ID: {{ device.categoryId }})</small>
                      </div>
                    </div>
                    <div class="info-row description-row">
                      <div class="info-label">描述</div>
                      <div class="info-value description-value">{{ device.description || '無描述資料' }}</div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 載入中提示 -->
          <div v-else class="text-center py-5">
            <div class="spinner-container">
              <div class="spinner"></div>
            </div>
            <p class="mt-3 loading-text">載入輔具資料中...</p>
          </div>
        </div>
        
        <!-- 底部按鈕區，只保留關閉按鈕 -->
        <div class="modal-footer">
          <button @click="close" class="btn btn-light">
            關閉
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useDevice } from '@/composables/useDevice'
import { useDeviceCategory } from '@/composables/useDeviceCategory' // 引入類別相關 composable

// Props
const props = defineProps({
  show: Boolean,
  deviceId: [String, Number]
})

// Emits
const emit = defineEmits(['close'])

const { loadDevice } = useDevice()
// 使用 useDeviceCategory composable
const { categories, loadCategories } = useDeviceCategory()

const device = ref(null)
const categoriesLoaded = ref(false) // 標記類別資料是否已載入

// 取得類別名稱
const categoryName = computed(() => {
  if (!device.value || !device.value.categoryId || !categories.value || categories.value.length === 0) {
    return null
  }
  
  // 尋找匹配的類別
  const category = categories.value.find(c => String(c.id) === String(device.value.categoryId))
  
  // 若找到對應類別，返回其名稱
  if (category) {
    console.log(`找到對應類別: ${category.name} (ID: ${category.id})`)
    return category.name
  }
  
  // 若未找到對應類別但有 categoryId
  console.log(`未找到對應類別，categoryId: ${device.value.categoryId}`)
  return null
})

// 修正：圖片路徑處理
const baseImageUrl = 'http://localhost:8080'
const deviceImage = computed(() => {
  if (!device.value || !device.value.image) {
    console.log('輔具無圖片或資料尚未載入', device.value)
    return '/assets/images/default-device.jpg'
  }
  
  // 如果已經是完整 URL，直接返回
  if (device.value.image.startsWith('http')) {
    console.log('使用完整 URL 圖片:', device.value.image)
    return device.value.image
  }
  
  // 否則拼接基礎 URL
  const result = `${baseImageUrl}/${device.value.image}`
  console.log('拼接圖片路徑:', {
    原始路徑: device.value.image,
    基礎URL: baseImageUrl,
    最終URL: result
  })
  return result
})

// 載入所有類別資料
async function loadAllCategories() {
  if (categoriesLoaded.value) {
    return // 避免重複載入
  }
  
  try {
    console.log('開始載入輔具類別資料')
    await loadCategories()
    categoriesLoaded.value = true
    console.log('輔具類別資料載入完成:', categories.value)
  } catch (err) {
    console.error('載入輔具類別資料失敗:', err)
  }
}

// 監聽 deviceId 變化，載入資料
watch(() => props.deviceId, async (newId) => {
  if (newId && props.show) {
    await loadDeviceData(newId)
  }
}, { immediate: true })

watch(() => props.show, async (newShow) => {
  if (newShow && props.deviceId) {
    await loadAllCategories() // 確保類別資料已載入
    loadDeviceData(props.deviceId)
  } else if (!newShow) {
    device.value = null
  }
})

// 改進：載入輔具資料
async function loadDeviceData(id) {
  if (!id) return
  
  try {
    console.log('開始載入輔具資料，ID:', id)
    const data = await loadDevice(id)
    device.value = data
    
    // 若尚未載入類別資料，先載入
    if (!categoriesLoaded.value) {
      await loadAllCategories()
    }
    
    // 載入後檢查類別資訊
    console.log('輔具資料載入完成:', {
      id: data.id,
      name: data.name,
      categoryId: data.categoryId,
      category: data.category,
      對應類別名稱: categoryName.value
    })
  } catch (err) {
    console.error('載入輔具資料失敗:', err)
  }
}

// 組件掛載時載入類別資料
onMounted(async () => {
  await loadAllCategories()
})

function close() {
  emit('close')
}

// 處理圖片載入失敗
function handleImageError(e) {
  console.log('圖片載入失敗，使用預設圖片')
  e.target.src = '/assets/images/default-device.jpg'
}
</script>

<style scoped>
.modal-backdrop {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.6);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1050;
  backdrop-filter: blur(3px);
}

.modal-dialog {
  width: 100%;
  max-width: 800px;
  margin: 1.5rem;
}

.modal-content {
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
  border: none;
  overflow: hidden;
}

.modal-header {
  border-bottom: 1px solid #f0f0f0;
  padding: 1.25rem 1.5rem;
}

.modal-title {
  font-weight: 600;
  color: #1e2022;
  font-size: 1.1rem;
}

.modal-body {
  padding: 1.5rem;
}

.device-detail-content {
  position: relative;
}

.device-image-wrapper {
  position: relative;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
  height: 220px;
  background-color: #f8f9fa;
  display: flex;
  align-items: center;
  justify-content: center; /* 確保圖片置中 */
}

.device-image-wrapper img {
  width: 100%;
  height: 100%;
  object-fit: contain; /* 改為 contain 以確保完整顯示圖片 */
  max-width: 100%;
  max-height: 100%;
}

/* 加入圖片載入指示 */
.device-image-wrapper::before {
  content: "載入中...";
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: #6c757d;
  z-index: -1;
}

.device-status-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  padding: 5px 12px;
  border-radius: 20px;
  font-size: 0.75rem;
  font-weight: 600;
  z-index: 2;
}

.device-status-badge.online {
  background-color: rgba(52, 195, 143, 0.2);
  color: #34c38f;
}

.device-status-badge.offline {
  background-color: rgba(108, 117, 125, 0.2);
  color: #6c757d;
}

.device-title {
  font-size: 1.4rem;
  font-weight: 600;
  margin-top: 1rem;
  margin-bottom: 0.5rem;
  color: #1e2022;
}

.sku-badge {
  display: inline-block;
  background-color: #f3f4f6;
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 0.8rem;
  color: #666;
}

.info-card {
  background-color: #f9fafc;
  border-radius: 10px;
  overflow: hidden;
  height: 100%;
}

.info-card-header {
  background-color: #f3f4f6;
  padding: 0.75rem 1.25rem;
}

.info-card-header h6 {
  margin: 0;
  font-weight: 600;
  color: #495057;
}

.info-card-body {
  padding: 1.25rem;
}

.info-row {
  display: flex;
  border-bottom: 1px solid #eaecef;
  padding: 0.75rem 0;
}

.info-row:last-child {
  border-bottom: none;
}

.info-label {
  width: 30%;
  color: #6c757d;
  font-weight: 500;
}

.info-value {
  width: 70%;
  font-weight: 500;
  color: #1e2022;
}

.price-value {
  color: #4580FF;
  font-weight: 600;
}

.description-row {
  flex-direction: column;
}

.description-row .info-label {
  width: 100%;
  margin-bottom: 0.5rem;
}

.description-row .info-value {
  width: 100%;
}

.description-value {
  white-space: pre-line;
  background-color: #fff;
  padding: 0.75rem;
  border-radius: 6px;
  font-weight: 400;
  border: 1px solid #eaecef;
  min-height: 80px;
}

.modal-footer {
  border-top: 1px solid #f0f0f0;
  padding: 1.25rem 1.5rem;
  justify-content: flex-end;
}

/* 載入動畫 */
.spinner-container {
  display: flex;
  justify-content: center;
  margin-bottom: 1rem;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid rgba(69, 128, 255, 0.2);
  border-top-color: #4580FF;
  border-radius: 50%;
  animation: spin 1s ease infinite;
}

.loading-text {
  color: #6c757d;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* 新增類別樣式 */
.category-value {
  display: flex;
  align-items: center;
}

.category-badge {
  display: inline-block;
  background-color: rgba(69, 128, 255, 0.1);
  color: #4580FF;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 0.9rem;
  border: 1px solid rgba(69, 128, 255, 0.2);
}
</style>