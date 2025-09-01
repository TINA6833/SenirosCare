// 重點註解：此 service 層封裝房型留言業務邏輯，型別轉換、資料結構處理，串接 src/api/roomCommentApi.js

import {
  createRoomComment,
  fetchRoomCommentsByRoomType,
  fetchLatestRoomComments,
  fetchRoomCommentsByMember,
  reviewRoomComment,
  updateRoomComment,
  deleteRoomComment
} from '@/api/roomCommentApi'

/**
 * 新增留言（前台會員功能）
 * @param {Object} data 留言資料
 */
export async function addRoomComment(data) {
  const res = await createRoomComment(data)
  return mapRoomComment(res.data)
}

/**
 * 查詢房型留言（前台房型詳情）
 * @param {number} roomTypeId 房型ID
 */
export async function getRoomCommentsByRoomType(roomTypeId) {
  const res = await fetchRoomCommentsByRoomType(roomTypeId)
  return Array.isArray(res.data) ? res.data.map(mapRoomComment) : []
}

/**
 * 查詢最新通過留言（前台房型詳情）
 * @param {number} roomTypeId 房型ID
 * @param {number} limit 筆數限制
 */
export async function getLatestRoomComments(roomTypeId, limit = 5) {
  const res = await fetchLatestRoomComments(roomTypeId, limit)
  return Array.isArray(res.data) ? res.data.map(mapRoomComment) : []
}

/**
 * 查詢會員留言（前台會員功能）
 * @param {number} memberId 會員ID
 */
export async function getRoomCommentsByMember(memberId) {
  const res = await fetchRoomCommentsByMember(memberId)
  return Array.isArray(res.data) ? res.data.map(mapRoomComment) : []
}

/**
 * 審核留言（限管理員或本人）
 * @param {number} id 留言ID
 * @param {Object} data 審核資料（如 status）
 */
export async function reviewRoomCommentService(id, data) {
  const res = await reviewRoomComment(id, data)
  return mapRoomComment(res.data)
}

/**
 * 修改留言（限管理員或本人）
 * @param {number} id 留言ID
 * @param {Object} data 修改資料
 */
export async function editRoomComment(id, data) {
  const res = await updateRoomComment(id, data)
  return mapRoomComment(res.data)
}

/**
 * 刪除留言（限管理員或本人）
 * @param {number} id 留言ID
 */
export async function removeRoomComment(id) {
  const res = await deleteRoomComment(id)
  return res.data
}

/**
 * 型別轉換：後端留言資料 => 前端留言物件
 * @param {object} item 後端留言資料
 * @returns {object} 前端留言物件
 */
function mapRoomComment(item) {
  return {
    id: item.id,
    memberId: item.memberId,
    memberName: item.memberName,
    roomTypeId: item.roomTypeId,
    content: item.content,
    adminReply: item.adminReply,
    createdAt: item.createdAt,
    is_approved: item.approved === true || item.approved === 1 // ★重點註解：將 approved 轉成布林值
    // 其他欄位請依資料表補充
  }
}