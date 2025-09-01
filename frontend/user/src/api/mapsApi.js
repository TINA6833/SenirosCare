// 重點註解：所有 API 皆使用 axiosInstance，僅負責 HTTP，符合專案分層規範
import axiosInstance from './axiosInstance'

/**
 * 取得所有地圖標記點（前台地圖頁/房型地圖）
 */
export const fetchAllMapMarkers = () => {
  // GET /api/member/maps
  return axiosInstance.get('/member/maps')
}

/**
 * 依房型查詢地圖標記
 * @param {number} roomTypeId 房型ID
 */
export const fetchMapMarkersByRoomType = (roomTypeId) => {
  // GET /api/member/maps/by-roomtype?roomTypeId=1
  return axiosInstance.get('/member/maps/by-roomtype', {
    params: { roomTypeId }
  })
}

/**
 * 依地區查詢地圖標記
 * @param {string} area 地區名稱
 */
export const fetchMapMarkersByArea = (area) => {
  // GET /api/member/maps/by-area?area=台北市
  return axiosInstance.get('/member/maps/by-area', {
    params: { area }
  })
}

/**
 * 依關鍵字查詢地圖標記
 * @param {string} keyword 關鍵字
 */
export const fetchMapMarkersByKeyword = (keyword) => {
  // GET /api/member/maps/search?keyword=長照
  return axiosInstance.get('/member/maps/search', {
    params: { keyword }
  })
}

/**
 * 取得單一地圖標記詳細資訊
 * @param {number} id 標記ID
 */
export const fetchMapMarkerById = (id) => {
  // GET /api/member/maps/{id}
  return axiosInstance.get(`/member/maps/${id}`)
}

/**
 * 新增地圖標記（限管理員或有權限會員）
 * @param {Object} data 地圖標記資料
 */
export const createMapMarker = (data) => {
  // POST /api/member/maps
  return axiosInstance.post('/member/maps', data)
}

/**
 * 修改地圖標記（限管理員或有權限會員）
 * @param {number} id 標記ID
 * @param {Object} data 修改資料
 */
export const updateMapMarker = (id, data) => {
  // PUT /api/member/maps/{id}
  return axiosInstance.put(`/member/maps/${id}`, data)
}

/**
 * 刪除地圖標記（限管理員或有權限會員）
 * @param {number} id 標記ID
 */
export const deleteMapMarker = (id) => {
  // DELETE /api/member/maps/{id}
  return axiosInstance.delete(`/member/maps/${id}`)
}

/**
 * 取得機構地點資訊（前台地圖元件用）
 */
export const fetchPlaceInfo = () => {
  // GET /api/room-types/member/maps/place
  return axiosInstance.get('/room-types/member/maps/place')
}