// src/composables/useDevice.js
// 這個檔案使用 Vue Composition API 以封裝共用的裝置邏輯
import { ref } from 'vue' // 引入 Vue 的 ref
import * as deviceService from '@/services/deviceService' // 引入 service 層的所有方法

export function useDevice() {
  const devices = ref([]) // 定義商品列表的 reactive 變數
  const device = ref(null) // 定義單一商品的 reactive 變數
  const totalCount = ref(0) // 商品總數 reactive 變數
  const loading = ref(false) // 載入中狀態 reactive 變數
  const error = ref(null) // 錯誤資訊 reactive 變數
  // 新增：通知訊息狀態
  const toast = ref({ show: false, message: '', type: 'info' }) 

  // 載入商品列表（可選分類）
  async function loadDevices(categoryId = null) {
    loading.value = true // 設為載入中
    error.value = null // 清空錯誤
    try {
      devices.value = await deviceService.fetchDevices(categoryId) // 從 service 取得資料並填入
    } catch (err) {
      error.value = err // 發生錯誤時記錄
    } finally {
      loading.value = false // 結束載入
    }
  }

  // 載入單一商品
  async function loadDevice(id) {
    loading.value = true // 載入中
    error.value = null // 清空錯誤
    try {
      device.value = await deviceService.fetchDevice(id) // 取得指定 id 商品
      return device.value // 回傳載入的商品資料
    } catch (err) {
      error.value = err // 記錄錯誤
      throw err // 丟出錯誤
    } finally {
      loading.value = false // 結束載入
    }
  }

  // 新增商品
  async function addDevice(data) {
    loading.value = true // 載入中
    try {
      const newId = await deviceService.createDevice(data) // 呼叫新增 API
      return newId // 回傳新 id
    } catch (err) {
      error.value = err // 記錄錯誤
      throw err // 丟出錯誤，讓呼叫者知道
    } finally {
      loading.value = false // 結束載入
    }
  }

  // 修改商品
  async function modifyDevice(id, data) {
    loading.value = true // 載入中
    try {
      return await deviceService.updateDevice(id, data) // 呼叫更新 API
    } catch (err) {
      error.value = err // 記錄錯誤
      throw err // 丟出給呼叫者處理
    } finally {
      loading.value = false // 結束載入
    }
  }

  // 刪除商品
  async function removeDevice(id) {
    loading.value = true // 載入中
    try {
      return await deviceService.deleteDevice(id) // 呼叫刪除 API
    } catch (err) {
      error.value = err // 記錄錯誤
      throw err // 丟出給呼叫者
    } finally {
      loading.value = false // 結束載入
    }
  }

  // 依關鍵字搜尋
  async function searchDevices(keyword) {
    loading.value = true // 載入中
    try {
      devices.value = await deviceService.searchDevices(keyword) // 更新列表
    } finally {
      loading.value = false // 結束載入
    }
  }

  // 取得商品總數
  async function fetchCount() {
    totalCount.value = await deviceService.countDevices() // 呼叫 count API
  }

  // 批次更新
  async function batchUpdate(requests) {
    return await deviceService.batchUpdateDevices(requests) // 傳回結果
  }

  // 批次刪除
  async function batchDelete(ids) {
    return await deviceService.batchDeleteDevices(ids) // 傳回結果
  }

  // 切換輔具上下架狀態
async function toggleDeviceStatus(id, isOnline) {
  loading.value = true // 設定載入中
  error.value = null // 清空錯誤
  
  try {
    console.log(`切換輔具 ID: ${id} 的狀態為: ${isOnline ? '上架' : '下架'}`)
    
    // 找出目標輔具
    let targetDevice = devices.value.find(d => d.id === id)
    
    // 處理可能的 ID 型別問題（數字型別與字串型別的比較）
    if (!targetDevice) {
      targetDevice = devices.value.find(d => String(d.id) === String(id))
    }
    
    // 如果在列表中找不到該輔具，嘗試單獨載入
    if (!targetDevice) {
      console.log(`在列表中找不到 ID: ${id} 的輔具，嘗試單獨載入`)
      targetDevice = await loadDevice(id)
    }
    
    if (!targetDevice) {
      throw new Error('找不到對應的輔具')
    }
    
    console.log('找到目標輔具:', targetDevice)
    
    // 克隆資料，避免直接修改 reactive 物件
    const updateData = JSON.parse(JSON.stringify(targetDevice))
    
    // 確保 id 欄位為數字型別
    updateData.id = typeof updateData.id === 'string' ? Number(updateData.id) : updateData.id
    
    // 更新狀態欄位
    updateData.isOnline = isOnline
    
    console.log('準備傳送的更新資料:', updateData)
    
    // 更新輔具狀態
    const updatedDevice = await deviceService.updateDevice(id, updateData)
    
    console.log('狀態更新成功，回傳資料:', updatedDevice)
    
    // 更新本地狀態，避免重新載入整個列表
    const index = devices.value.findIndex(d => 
      d.id === id || String(d.id) === String(id)
    )
    
    if (index !== -1) {
      devices.value[index].isOnline = isOnline
      console.log(`已更新本地資料，輔具 ${devices.value[index].name} (ID: ${id}) 狀態已變更為: ${isOnline ? '上架' : '下架'}`)
    }
    
    // 顯示成功訊息
    showToast(`輔具已成功${isOnline ? '上架' : '下架'}`, 'success')
    
    return updatedDevice
  } catch (err) {
    error.value = err // 設定錯誤
    console.error('切換輔具狀態失敗:', err)
    
    // 取得詳細的錯誤訊息
    const errorMessage = err.response?.data?.message || err.message || '請稍後再試'
    
    // 顯示錯誤訊息
    showToast(`狀態切換失敗: ${errorMessage}`, 'error')
    
    throw err // 丟出錯誤給呼叫方
  } finally {
    loading.value = false // 結束載入狀態
  }
}
  
  // 新增：顯示通知訊息
  function showToast(message, type = 'info') {
    toast.value = {
      show: true,
      message,
      type
    }
    
    // 3 秒後自動關閉
    setTimeout(() => {
      closeToast()
    }, 3000)
  }
  
  // 新增：關閉通知訊息
  function closeToast() {
    toast.value.show = false
  }

  // 上傳商品圖片並同步更新本地資料
  async function uploadImage(id, file) {
    loading.value = true
    error.value = null
    try {
      const url = await deviceService.uploadDeviceImage(id, file)

      // 如果列表中已有該商品，更新它的 image
      const idx = devices.value.findIndex(d => d.id === id)
      if (idx !== -1) {
        devices.value[idx].image = url
      }

      // 如果單一商品頁面也正在顯示此 id，亦更新
      if (device.value && device.value.id === id) {
        device.value.image = url
      }

      return url
    } catch (err) {
      error.value = err
      throw err
    } finally {
      loading.value = false
    }
  }

  // 匯出 CSV：呼叫 service 層，回傳 CSV 字串
  async function exportCsv() {
    try {
      // 呼叫 service 層取得 CSV 字串
      const csvData = await deviceService.exportDevices();
      return csvData;
    } catch (err) {
      error.value = err;
      showToast(`匯出失敗: ${err.message || '請稍後再試'}`, 'error');
      throw err;
    }
  }

  // 回傳所有 state 和方法
  return {
    devices,
    device,
    totalCount,
    loading,
    error,
    toast,
    loadDevices,
    loadDevice,
    addDevice,
    modifyDevice,
    removeDevice,
    searchDevices,
    fetchCount,
    batchUpdate,
    batchDelete,
    uploadImage,
    toggleDeviceStatus,
    showToast,
    closeToast,
    exportCsv // ← 加入這一行，讓元件可以呼叫
  }
}