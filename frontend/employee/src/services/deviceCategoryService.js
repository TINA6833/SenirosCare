import deviceCategoryApi from '@/api/deviceCategoryApi';

// 封裝呼叫，並直接回傳 data
export async function fetchCategories() {
  const res = await deviceCategoryApi.getCategories()
  return res.data
}

export async function fetchCategory(id) {
  const res = await deviceCategoryApi.getCategory(id)
  return res.data
}

export async function createCategory(data) {
  const res = await deviceCategoryApi.createCategory(data)
  return res.data
}

export async function updateCategory(id, data) {
  const res = await deviceCategoryApi.updateCategory(id, data)
  return res.data
}

export async function deleteCategory(id) {
  const res = await deviceCategoryApi.deleteCategory(id)
  return res.data
}

export async function existsCategory(id) {
  const res = await deviceCategoryApi.existsCategory(id)
  return res.data
}

export async function searchCategories(keyword) {
  const res = await deviceCategoryApi.searchCategories(keyword)
  return res.data
}

export async function countCategories() {
  const res = await deviceCategoryApi.countCategories()
  return res.data
}