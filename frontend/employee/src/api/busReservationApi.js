import axiosInstance from './axiosInstance'; // **重點：引入我們建立的實例**
// **重點：共用時間轉換工具**
// yyyy-MM-dd
function toYMD(input) {
  if (input instanceof Date) {
    const p = n => String(n).padStart(2, '0');
    return `${input.getFullYear()}-${p(input.getMonth() + 1)}-${p(input.getDate())}`;
  }
  return String(input).replace(/T\d{2}:\d{2}$/, '');
}

// **重點：修正後的 toYMDHM 函數**
function toYMDHM(input) {
  const pad = n => String(n).padStart(2, '0');
  if (input instanceof Date) {
    return `${input.getFullYear()}-${pad(input.getMonth() + 1)}-${pad(input.getDate())}` +
      `T${pad(input.getHours())}:${pad(input.getMinutes())}:${pad(input.getSeconds())}`;
  }

  const s = String(input).trim();

  // yyyy-MM-ddTHH
  if (/^\d{4}-\d{2}-\d{2}T\d{2}$/.test(s)) {
    return `${s}:00:00`;
  }
  // yyyy-MM-ddTHH:mm
  if (/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}$/.test(s)) {
    return `${s}:00`;
  }
  // yyyy-MM-ddTHH:mm:ss 直接回傳
  if (/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}$/.test(s)) {
    return s;
  }

  // 其他格式（保底）：盡量補到秒
  const m = s.match(/^(\d{4}-\d{2}-\d{2}T\d{2})(?::(\d{2}))?(?::(\d{2}))?$/);
  if (m) {
    const hh = m[1];
    const mm = m[2] ?? '00';
    const ss = m[3] ?? '00';
    return `${hh}:${mm}:${ss}`;
  }
  return s; // 若完全不匹配，先原樣送出（方便定位其它來源）
}

// **重點：確保導出 toYMDHM 函數**
export { toYMDHM, toYMD };

/**
 * 預約表單 API 服務
 * 處理與預約表單相關的 HTTP 請求
 */
export const reservationApi = {
  /**
   * 獲取所有預約表單
   * @returns {Promise} 包含所有預約表單資料的 Promise
   */
  getAllReservation() {
    // **重點：使用 axiosInstance，它會自動帶上 Token**
    return axiosInstance.get('/reservation/findAll');
  },

  /**
   * 根據 ID 取得預約表
   * @param {number} id - 預約表單 ID
   * @returns {Promise} 包含單一預約表單資料的 Promise物件
   */
  getReservationById(id) {
    return axiosInstance.get(`/reservation/view/${id}`);
  },

  /**
   * 補一個實體版（非 view）查單
   * @param {number} id - 預約表單 ID
   * @returns {Promise} 包含單一預約表單資料的 Promise物件
   */
  getReservationEntityById(id) {
    return axiosInstance.get(`/reservation/${id}`);
  },

  /**
   * 新增預約表單
   * @param {Object} resCreateRequest - 預約表單資料
   * @returns {Promise} 包含創建的預約表單資料的 Promise物件
   */
  createReservation(resCreateRequest) {
    if (!resCreateRequest) throw new Error('預約資料不能為空');

    // **重點：最小必填驗證**
    const required = ['memberId', 'busId', 'scheduledAt', 'startAddress', 'endAddress'];
    for (const k of required) {
      const v = resCreateRequest[k];
      if (v === undefined || v === null || String(v).trim() === '') {
        throw new Error(`缺少必要欄位：${k}`);
      }
    }

    // **重點：確保資料符合後端API格式要求**
    const payload = {
      memberId: parseInt(resCreateRequest.memberId, 10),
      busId: parseInt(resCreateRequest.busId, 10),
      startAddress: resCreateRequest.startAddress?.trim(),
      endAddress: resCreateRequest.endAddress?.trim(),
      scheduledAt: toYMDHM(resCreateRequest.scheduledAt),
      note: resCreateRequest.note || 'available',

      // 扁平化座標資料 (與 updateReservation 保持一致)
      startLat: resCreateRequest.startLat || resCreateRequest.coords?.startLat || null,
      startLng: resCreateRequest.startLng || resCreateRequest.coords?.startLng || null,
      endLat: resCreateRequest.endLat || resCreateRequest.coords?.endLat || null,
      endLng: resCreateRequest.endLng || resCreateRequest.coords?.endLng || null,

      // 如果有距離和價格信息，也一併傳送
      price: resCreateRequest.price || resCreateRequest.system?.price || undefined,
      distanceMeters: resCreateRequest.distanceMeters || resCreateRequest.system?.distanceMeters || undefined
    };

    // **重點：移除 undefined 欄位**
    Object.keys(payload).forEach(k => payload[k] === undefined && delete payload[k]);

    return axiosInstance.post('/reservation', payload);
  },

  /**
   * 更新預約表單
   * @param {number} id - 預約表單 ID
   * @param {Object} resRequest - 更新的預約表單資料
   * @returns {Promise} 包含更新後的預約表單資料的 Promise物件
   */
  updateReservation(id, resRequest) {
    if (!id) throw new Error('預約單 ID 不能為空');
    if (!resRequest) throw new Error('預約單更新資料不能為空');

    // **重點：確保資料符合後端API格式要求**
    const payload = {
      id: parseInt(id, 10),
      busId: parseInt(resRequest.busId, 10),
      startAddress: resRequest.startAddress?.trim() || '',
      endAddress: resRequest.endAddress?.trim() || '',
      scheduledAt: toYMDHM(resRequest.scheduledAt),
      reservationStatus: resRequest.reservationStatus || 'pending',
      note: resRequest.note ?? '',

      // 扁平化座標資料
      startLat: resRequest.startLat || resRequest.coords?.startLat || null,
      startLng: resRequest.startLng || resRequest.coords?.startLng || null,
      endLat: resRequest.endLat || resRequest.coords?.endLat || null,
      endLng: resRequest.endLng || resRequest.coords?.endLng || null,

      // 添加價格資訊
      price: resRequest.price || resRequest.system?.price || 0
    };

    console.log('發送更新預約請求：', payload);
    return axiosInstance.put(`/reservation/update/${id}`, payload);
  },

  /**
   * 刪除預約表單
   * @param {number} id - 預約表單 ID
   * @returns {Promise} 包含刪除結果的 Promise
   */
  deleteReservation(id) {
    if (!id) throw new Error('預約表單 ID 不能為空');
    return axiosInstance.delete(`/reservation/delete/${id}`);
  },

  /**
   * 模糊查詢預約（依：會員、地址關鍵字、日期或日期時間）
   * @param {Object} resQueryParams - 搜尋參數
   * @returns {Promise} 包含搜尋結果的 Promise
   */
  searchReservation(resQueryParams = {}) {
    // **重點：清理參數，確保 "undefined" 字串不被發送**
    const params = {};

    // 只添加有實際值的參數
    if (resQueryParams.scheduledDate && resQueryParams.scheduledDate !== 'undefined') {
      params.scheduledDate = toYMD(resQueryParams.scheduledDate);
    }

    if (resQueryParams.scheduledAt && resQueryParams.scheduledAt !== 'undefined') {
      // 後端期望參數名是 scheduledAt（LocalDateTime），不是 scheduledAtTime
      params.scheduledAt = toYMDHM(resQueryParams.scheduledAt);
    }

    // **重點：處理其他查詢參數**
    if (resQueryParams.memberId) {
      params.memberId = resQueryParams.memberId;
    }
    if (resQueryParams.startAddress) {
      params.startAddress = resQueryParams.startAddress;
    }
    if (resQueryParams.endAddress) {
      params.endAddress = resQueryParams.endAddress;
    }

    // **重點：使用 axios 的 params 選項傳遞查詢參數**
    return axiosInstance.get('/reservation/search', { params });
  },

  /**
   * 已完成（完成訂單）
   * @param {number} id - 預約單 ID
   * @returns {Promise} 包含完成結果的 Promise
   */
  completeReservation(id) {
    if (!id) throw new Error('預約單 ID 不能為空');
    return axiosInstance.patch(`/reservation/${id}/complete`);
  },

  /**
   * 計算起點和終點之間的距離和價格
   * @param {string} startAddress - 起點地址
   * @param {string} endAddress - 終點地址
   * @returns {Promise} 包含距離和價格資料的 Promise 物件
   */
  calculateDistanceAndPrice(startAddress, endAddress) {
    if (!startAddress || !endAddress) {
      // **重點：返回預設值的 Promise**
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

    // **重點：輕度正規化：壓縮多餘空白，避免 API NOT_FOUND**
    const normalize = (addr) => addr.replace(/\s+/g, ' ').trim();

    const params = {
      origin: normalize(startAddress),
      destination: normalize(endAddress)
    };

    console.log('發送距離計算請求:', params);

    // **重點：使用 axios 處理請求，並提供錯誤處理**
    return axiosInstance.get('/fare/quote', { params })
      .catch(error => {
        console.warn(`距離計算 API 錯誤:`, error.message);
        // **重點：發生錯誤時返回預設值**
        return {
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
        };
      });
  }
};