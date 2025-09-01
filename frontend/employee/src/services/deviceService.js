import {deviceApi} from '@/api/deviceApi';


// 封裝呼叫，並直接回傳 data
export async function fetchDevices(categoryId) {
  const res = await deviceApi.getDevices(categoryId)
  return res.data
}

export async function fetchDevice(id) {
  const res = await deviceApi.getDevice(id)
  return res.data
}

export async function createDevice(data) {
  const res = await deviceApi.createDevice(data)
  return res.data
}

export async function updateDevice(id, data) {
  const res = await deviceApi.updateDevice(id, data)
  return res.data
}

export async function deleteDevice(id) {
  const res = await deviceApi.deleteDevice(id)
  return res.data
}

export async function searchDevices(keyword) {
  const res = await deviceApi.searchDevices(keyword)
  return res.data
}

export async function countDevices() {
  const res = await deviceApi.countDevices()
  return res.data
}

export async function sortDevices(sortBy) {
  const res = await deviceApi.sortDevices(sortBy)
  return res.data
}

export async function getPagedDevices(offset, limit) {
  const res = await deviceApi.getPagedDevices(offset, limit)
  return res.data
}

export async function getPagedSortedDevices(offset, limit, sortBy) {
  const res = await deviceApi.getPagedSortedDevices(offset, limit, sortBy)
  return res.data
}

export async function importDevices(file) {
  const res = await deviceApi.importDevices(file)
  return res.data
}

export async function exportDevices() {
  const res = await deviceApi.exportDevices()
  return res.data
}

export async function batchUpdateDevices(requests) {
  const res = await deviceApi.batchUpdateDevices(requests)
  return res.data
}

export async function batchDeleteDevices(ids) {
  const res = await deviceApi.batchDeleteDevices(ids)
  return res.data
}

// 上傳單一商品圖片，並回傳完整可直接使用的 URL
export async function uploadDeviceImage(id, file) {
  const res = await deviceApi.uploadDeviceImage(id, file)  // 後端回傳可能是完整 URL 或只是檔名
  const result = res.data

  // 如果只是檔名，就幫它補上前綴
  return result

}
