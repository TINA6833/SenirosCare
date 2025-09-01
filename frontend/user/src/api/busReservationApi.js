// frontend/user/src/api/busReservationApi.js
import axiosInstance from './axiosInstance';

// yyyy-MM-dd
function toYMD(input) {
  if (input instanceof Date) {
    const p = n => String(n).padStart(2, '0');
    return `${input.getFullYear()}-${p(input.getMonth() + 1)}-${p(input.getDate())}`;
  }
  return String(input).replace(/T\d{2}:\d{2}(?::\d{2})?$/, '');
}

function toYMDHM(input) {
  const pad = n => String(n).padStart(2, '0');
  if (input instanceof Date) {
    return `${input.getFullYear()}-${pad(input.getMonth() + 1)}-${pad(input.getDate())}` +
      `T${pad(input.getHours())}:${pad(input.getMinutes())}:${pad(input.getSeconds())}`;
  }
  const s = String(input).trim();
  if (/^\d{4}-\d{2}-\d{2}T\d{2}$/.test(s)) return `${s}:00:00`;
  if (/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}$/.test(s)) return `${s}:00`;
  if (/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}$/.test(s)) return s;
  const m = s.match(/^(\d{4}-\d{2}-\d{2}T\d{2})(?::(\d{2}))?(?::(\d{2}))?$/);
  if (m) {
    const hh = m[1];
    const mm = m[2] ?? '00';
    const ss = m[3] ?? '00';
    return `${hh}:${mm}:${ss}`;
  }
  return s;
}

export { toYMDHM, toYMD };

export const reservationApi = {
  getAllReservation() {
    return axiosInstance.get('/reservation/findAll');
  },
  getReservationById(id) {
    return axiosInstance.get(`/reservation/view/${id}`);
  },
  getReservationEntityById(id) {
    return axiosInstance.get(`/reservation/${id}`);
  },

  /**
   * 建立預約：處理 409 衝突（回拋結構化錯誤）
   */
  async createReservation(resCreateRequest) {
    if (!resCreateRequest) throw new Error('預約資料不能為空');
    const required = ['busId', 'scheduledAt', 'startAddress', 'endAddress'];
    for (const k of required) {
      const v = resCreateRequest[k];
      if (v === undefined || v === null || String(v).trim() === '') {
        throw new Error(`缺少必要欄位：${k}`);
      }
    }

    const payload = {
      busId: parseInt(resCreateRequest.busId, 10),
      startAddress: resCreateRequest.startAddress?.trim(),
      endAddress: resCreateRequest.endAddress?.trim(),
      scheduledAt: toYMDHM(resCreateRequest.scheduledAt),
      note: resCreateRequest.note || 'available',
      startLat: resCreateRequest.startLat || resCreateRequest.coords?.startLat || null,
      startLng: resCreateRequest.startLng || resCreateRequest.coords?.startLng || null,
      endLat: resCreateRequest.endLat || resCreateRequest.coords?.endLat || null,
      endLng: resCreateRequest.endLng || resCreateRequest.coords?.endLng || null,
      price: resCreateRequest.price || resCreateRequest.system?.price || undefined,
      distanceMeters: resCreateRequest.distanceMeters || resCreateRequest.system?.distanceMeters || undefined
    };
    Object.keys(payload).forEach(k => payload[k] === undefined && delete payload[k]);

    try {
      return await axiosInstance.post('/reservation', payload);
    } catch (err) {
      // ← 新增更穩健的維修判斷
      const status = err?.response?.status;
      const data = err?.response?.data || {};
      const msg =
        data?.message ||
        data?.error ||
        data?.msg ||
        err.message ||
        '';
      const code = (data?.code || data?.errorCode || data?.error_code || '').toString();

      // A. 車輛維修中：不管 400/409 都轉成 BUS_MAINTENANCE 結構化錯誤
      if (
        code.toUpperCase() === 'BUS_MAINTENANCE' ||
        /維修|maintenance|MAINTENANCE/i.test(msg)
      ) {
        const e = new Error('該車輛目前維修中，暫停預約');
        e.code = 'BUS_MAINTENANCE';
        e.original = err;
        throw e;
      }

      // B. 時段重疊：僅在 409 時拋出 RESERVATION_CONFLICT（保留你原本結構）
      if (status === 409) {
        const scheduledDate = toYMD(payload.scheduledAt);
        const conflictError = new Error('該時段已被預約');
        conflictError.code = 'RESERVATION_CONFLICT';
        conflictError.busId = payload.busId;
        conflictError.scheduledDate = scheduledDate;
        conflictError.original = err;
        throw conflictError;
      }

      // 其餘錯誤 → 照拋
      throw err;
    }

  },

  updateReservation(id, resRequest) {
    if (!id) throw new Error('預約單 ID 不能為空');
    if (!resRequest) throw new Error('預約單更新資料不能為空');
    const payload = {
      id: parseInt(id, 10),
      busId: parseInt(resRequest.busId, 10),
      startAddress: resRequest.startAddress?.trim() || '',
      endAddress: resRequest.endAddress?.trim() || '',
      scheduledAt: toYMDHM(resRequest.scheduledAt),
      reservationStatus: resRequest.reservationStatus || 'pending',
      note: resRequest.note ?? '',
      startLat: resRequest.startLat || resRequest.coords?.startLat || null,
      startLng: resRequest.startLng || resRequest.coords?.startLng || null,
      endLat: resRequest.endLat || resRequest.coords?.endLat || null,
      endLng: resRequest.endLng || resRequest.coords?.endLng || null,
      price: resRequest.price || resRequest.system?.price || 0
    };
    return axiosInstance.put(`/reservation/update/${id}`, payload);
  },

  deleteReservation(id) {
    if (!id) throw new Error('預約表單 ID 不能為空');
    return axiosInstance.delete(`/reservation/delete/${id}`);
  },

  searchReservation(resQueryParams = {}) {
    const params = {};
    if (resQueryParams.scheduledDate && resQueryParams.scheduledDate !== 'undefined') {
      params.scheduledDate = toYMD(resQueryParams.scheduledDate);
    }
    if (resQueryParams.scheduledAt && resQueryParams.scheduledAt !== 'undefined') {
      params.scheduledAt = toYMDHM(resQueryParams.scheduledAt);
    }
    if (resQueryParams.startAddress) params.startAddress = resQueryParams.startAddress;
    if (resQueryParams.endAddress) params.endAddress = resQueryParams.endAddress;
    return axiosInstance.get('/reservation/search', { params });
  },

  completeReservation(id) {
    if (!id) throw new Error('預約單 ID 不能為空');
    return axiosInstance.patch(`/reservation/${id}/complete`);
  },

  calculateDistanceAndPrice(startAddress, endAddress) {
    if (!startAddress || !endAddress) {
      return Promise.resolve({
        status: 200,
        data: {
          distanceMeters: 5000,
          rehabusFare: 95,
          taxiFare: 190,
          estimated: true
        }
      });
    }
    const normalize = (addr) => addr.replace(/\s+/g, ' ').trim();
    const params = { origin: normalize(startAddress), destination: normalize(endAddress) };
    return axiosInstance.get('/fare/quote', { params }).catch(() => ({
      status: 200,
      data: {
        distanceMeters: 5000,
        distanceKm: 5.0,
        rehabusFare: 95,
        taxiFare: 190,
        origin: startAddress,
        destination: endAddress,
        estimated: true
      }
    }));
  },

  /**
   * 新增的便利方法：用日期重抓當天的預約
   * 你在畫面收到 409 後：await reservationApi.getReservationsByDate(conflictError.scheduledDate)
   */
  getReservationsByDate(date) {
    return this.searchReservation({ scheduledDate: date });
  }
};
