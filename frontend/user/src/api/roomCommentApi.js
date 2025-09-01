// 重點註解：所有 API 皆使用 axiosInstance，僅負責 HTTP，符合專案分層規範
import axiosInstance from './axiosInstance'

/**
 * 新增留言（前台會員功能）
 * @param {Object} data 留言資料（memberId, roomTypeId, content, ...）
 */
export const createRoomComment = (data) => {
  // POST /api/member/room-comments
  return axiosInstance.post('/member/room-comments', data)
}

/**
 * 查詢房型留言（前台房型詳情）
 * @param {number} roomTypeId 房型ID
 */
export const fetchRoomCommentsByRoomType = (roomTypeId) => {
  // GET /api/member/room-comments/by-roomtype/{roomTypeId}
  return axiosInstance.get(`/member/room-comments/by-roomtype/${roomTypeId}`)
}

/**
 * 查詢最新通過留言（前台房型詳情）
 * @param {number} roomTypeId 房型ID
 * @param {number} limit 筆數限制
 */
export const fetchLatestRoomComments = (roomTypeId, limit = 5) => {
  // GET /api/member/room-comments/latest?roomTypeId=1&limit=5
  return axiosInstance.get('/member/room-comments/latest', {
    params: { roomTypeId, limit }
  })
}

/**
 * 查詢會員留言（前台會員功能）
 * @param {number} memberId 會員ID
 */
export const fetchRoomCommentsByMember = (memberId) => {
  // GET /api/member/room-comments/by-member/{memberId}
  return axiosInstance.get(`/member/room-comments/by-member/${memberId}`)
}

/**
 * 審核留言（限管理員或本人）
 * @param {number} id 留言ID
 * @param {Object} data 審核資料（如 status）
 */
export const reviewRoomComment = (id, data) => {
  // PATCH /api/member/room-comments/{id}
  return axiosInstance.patch(`/member/room-comments/${id}`, data)
}

/**
 * 修改留言（限管理員或本人）
 * @param {number} id 留言ID
 * @param {Object} data 修改資料
 */
export const updateRoomComment = (id, data) => {
  // PUT /api/member/room-comments/{id}
  return axiosInstance.put(`/member/room-comments/${id}`, data)
}

/**
 * 刪除留言（限管理員或本人）
 * @param {number} id 留言ID
 */
export const deleteRoomComment = (id) => {
  // DELETE /api/member/room-comments/{id}
  return axiosInstance.delete(`/member/room-comments/${id}`)
}