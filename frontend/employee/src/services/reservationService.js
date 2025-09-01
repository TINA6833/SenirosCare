import { reservationApi } from '@/api/reservationApi';

export const reservationService = {
  // 取得所有預約
  async getAllReservations() {
    try {
      const res = await reservationApi.getAll();
      return res.data;
    } catch (e) {
      handleError(e, '取得預約列表');
    }
  },

  // 取得單一預約
  async getReservationById(id) {
    const res = await reservationApi.getById(id);
    return res.data;
  },

  // 新增預約
  async createReservation(data) {
    // 對應 @PostMapping
    const res = await reservationApi.create(data);
    return res.data;
  },

  // 修改預約
  async updateReservation(id, data) {
    try {
      const res = await reservationApi.update(id, data);
      return res.data;
    } catch (e) {
      handleError(e, `修改預約 ID ${id}`);
    }
  },

  // 刪除預約
  async deleteReservation(id) {
    const res = await reservationApi.delete(id);
    return res.status === 200 || res.status === 204;
  },

  // 更新預約狀態
  async patchReservationStatus(id, data) {
    try {
      const res = await reservationApi.patchStatus(id, data);
      return res.data;
    } catch (e) {
      handleError(e, `更新預約 ID ${id} 狀態`);
    }
  },

  // 假設 reservationApi 有 patch 方法
  async patchReservation(id, data) {
    // 改用 PUT 方法（對應 @PutMapping）
    const res = await reservationApi.update(id, data);
    return res.data;
  },

  // 透過時間範圍取得預約
  async getByRange(from, to) {
    const res = await fetch(`/api/reservations/by-range?from=${from}&to=${to}`);
    if (!res.ok) throw new Error('API 回傳錯誤');
    return await res.json();
  }
};

function handleError(error, action) {
  let errorMsg;
  if (error.response) {
    const status = error.response.status;
    const data = error.response.data;
    if (status === 415) {
      errorMsg = `${action} 失敗: 不支援的媒體類型 (415)`;
    } else if (status === 404) {
      errorMsg = `${action} 失敗: 找不到端點 (404)`;
    } else {
      errorMsg = `${action} 失敗: HTTP ${status} - ${data?.message || '伺服器錯誤'}`;
    }
  } else if (error.request) {
    errorMsg = `${action} 失敗: 無法連接到伺服器`;
  } else {
    errorMsg = `${action} 失敗: ${error.message || '未知錯誤'}`;
  }
  throw new Error(errorMsg);
}