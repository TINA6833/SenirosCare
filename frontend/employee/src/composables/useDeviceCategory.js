// src/composables/useDeviceCategory.js
// 這個檔案使用 Vue Composition API 以封裝共用的分類邏輯
import { ref } from 'vue'
import * as deviceCategoryService from '@/services/deviceCategoryService'

export function useDeviceCategory() {
  const categories = ref([])               // 分類列表
  const category = ref(null)               // 單一分類
  const totalCount = ref(0)               // 分類總數
  const loading = ref(false)              // 載入中狀態
  const error = ref(null)                 // 錯誤資訊
  const toast = ref({ show: false, message: '', type: 'info' }) // 通知訊息狀態

  // 載入所有分類
  async function loadCategories() {
    loading.value = true
    error.value = null
    try {
      categories.value = await deviceCategoryService.fetchCategories() // 取得資料
    } catch (err) {
      error.value = err
      showToast(`載入分類失敗：${err.message}`, 'error')
    } finally {
      loading.value = false
    }
  }

  // 載入單一分類
  async function loadCategory(id) {
    loading.value = true
    error.value = null
    try {
      category.value = await deviceCategoryService.fetchCategory(id)
      return category.value
    } catch (err) {
      error.value = err
      throw err
    } finally {
      loading.value = false
    }
  }

  // 新增分類
  async function addCategory(data) {
    loading.value = true
    try {
      const newId = await deviceCategoryService.createCategory(data)
      showToast('分類新增成功', 'success')
      return newId
    } catch (err) {
      error.value = err
      showToast(`新增分類失敗：${err.message}`, 'error')
      throw err
    } finally {
      loading.value = false
    }
  }

  // 修改分類
  async function modifyCategory(id, data) {
    loading.value = true
    try {
      const result = await deviceCategoryService.updateCategory(id, data)
      showToast('分類更新成功', 'success')
      return result
    } catch (err) {
      error.value = err
      showToast(`更新分類失敗：${err.message}`, 'error')
      throw err
    } finally {
      loading.value = false
    }
  }

  // 刪除分類
  async function removeCategory(id) {
    loading.value = true
    try {
      const result = await deviceCategoryService.deleteCategory(id)
      showToast('分類刪除成功', 'success')
      return result
    } catch (err) {
      error.value = err
      showToast(`刪除分類失敗：${err.message}`, 'error')
      throw err
    } finally {
      loading.value = false
    }
  }

  // 模糊搜尋分類
  async function searchCategories(keyword) {
    loading.value = true
    try {
      categories.value = await deviceCategoryService.searchCategories(keyword)
    } catch (err) {
      error.value = err
    } finally {
      loading.value = false
    }
  }

  // 取得分類總數
  async function fetchCount() {
    totalCount.value = await deviceCategoryService.countCategories()
  }

  // 顯示通知訊息
  function showToast(message, type = 'info') {
    toast.value = { show: true, message, type }
    setTimeout(() => { toast.value.show = false }, 3000)
  }

  return {
    categories,
    category,
    totalCount,
    loading,
    error,
    toast,
    loadCategories,
    loadCategory,
    addCategory,
    modifyCategory,
    removeCategory,
    searchCategories,
    fetchCount,
    showToast
  }
}
