import { reservationApi } from '@/api/busReservationApi';

// ===== 工具：時間/VM 映射 =====
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

// 共用轉換：把 API 的 view/camel 資料 → 畫面可直接用
const normalizeReservation = (r) => {
  const pick = (U, c) => r?.[U] ?? r?.[c];
  const id = pick('ID','id');
  const distanceMeters = pick('DISTANCE_METERS','distanceMeters');
  const price = pick('PRICE','price') ?? 0;
  const rawStatus = String(pick('RESERVATION_STATUS','reservationStatus') || '').toLowerCase();

  const formattedDistance = distanceMeters != null ? `${(distanceMeters/1000).toFixed(1)} km` : '—';
  const formattedPrice = `$${price}`;
  const statusText = ({pending:'待處理',in_progress:'進行中',completed:'已完成',cancelled:'已取消'})[rawStatus] || '未知狀態';

  return {
    id,
    memberId: pick('MEMBER_ID','memberId'),
    busId: pick('BUS_ID','busId'),
    startAddress: pick('START_ADDRESS','startAddress'),
    endAddress: pick('END_ADDRESS','endAddress'),
    scheduledAt: pick('SCHEDULED_AT','scheduledAt'),
    completedAt: pick('COMPLETED_AT','completedAt'),
    note: pick('NOTE','note'),

    reservationStatus: rawStatus,
    distanceMeters,
    price,
    createdAt: pick('CREATED_AT','createdAt'),
    coords: {
      startLat: pick('START_LAT','startLat'),
      startLng: pick('START_LNG','startLng'),
      endLat: pick('END_LAT','endLat'),
      endLng: pick('END_LNG','endLng'),
    },

    formattedDistance,
    formattedPrice,
    display: { statusText, formattedDistance, formattedPrice },
  };
};




function mapReservationToVM(reservation) {

    // 處理距離格式化 - 確保即使沒有資料也有預設值
    const formattedDistance = reservation.DISTANCE_METERS != null
        ? `${(reservation.DISTANCE_METERS / 1000).toFixed(1)} km`
        : '—';

    // 處理價格格式化 - 確保即使沒有資料也有預設值
    const formattedPrice = reservation.PRICE != null
        ? `\$${reservation.PRICE}`
        : '—';

    const statusText = (s) => {
        if (!s) return '—';
        switch (s) {
            case 'pending': return '待確認';
            case 'in_progress': return '進行中';
            case 'completed': return '已完成';
            case 'cancelled': return '已取消';
            default: return '未知狀態';
        }
    };
    return {
        // 表單可編輯

        id: reservation.ID,
        memberId: reservation.MEMBER_ID,
        busId: reservation.BUS_ID,
        scheduledAt: reservation.SCHEDULED_AT,
        startAddress: reservation.START_ADDRESS || '未知起點',
        endAddress: reservation.END_ADDRESS || '未知終點',
        reservationStatus: reservation.RESERVATION_STATUS || 'pending',
        note: reservation.NOTE || '',

        // ★補：把區域 ID 與價格帶回（供 update 用）
        startZone: reservation.START_ZONE ?? reservation.START_ZONE_ID ?? reservation.startZone,
        endZone: reservation.END_ZONE ?? reservation.END_ZONE_ID ?? reservation.endZone,
        price: reservation.PRICE,

        // 地圖用
        coords: {
            startLat: reservation.START_LAT,
            startLng: reservation.START_LNG,
            endLat: reservation.END_LAT,
            endLng: reservation.END_LNG
        },

        // 系統/唯讀欄位（顯示用，不要送回去）
        system: {
            distanceMeters: reservation.DISTANCE_METERS,
            price: reservation.PRICE,
            createdAt: reservation.CREATED_AT,
            completedAt: reservation.COMPLETED_AT
        },

        // 顯示強化
        display: {
            statusText: statusText(reservation.RESERVATION_STATUS),
            formattedDistance,
            formattedPrice
        },

        // 直接在對象上也添加這些格式化的屬性，以便向後兼容
        formattedDistance,
        formattedPrice
    };
}

// 移除 undefined 欄位（避免多送）
function compact(obj) {
    Object.keys(obj).forEach(k => obj[k] === undefined && delete obj[k]);
    return obj;
}

// 在 calculateDistanceAndPrice 方法附近添加
function getDefaultFareQuote(origin, destination) {
    return {
        distanceMeters: 5000,
        rehabusFare: 95,        // 使用與後端一致的命名
        taxiFare: 190,
        origin: origin || '未知起點',
        destination: destination || '未知終點',
        estimated: true
    };
}

// 導出函數
export { toYMDHM };

/**
 * 預約服務模組 - 處理預約相關業務邏輯和資料轉換
 */
export const reservationService = {
    /**
     * 獲取所有預約列表並進行資料轉換
     * @returns {Promise<Array>} 轉換後的預約資料陣列
     */
    async getAllReservation() {
  try {
    const response = await reservationApi.getAllReservation();
    if (response.status === 200) {
      const rows = response.data ?? [];
      // 統一用 normalizeReservation，裡面已含 completedAt / 狀態小寫 / 顯示欄位
      return rows.map(normalizeReservation);
    }
    throw new Error(`伺服器回應錯誤：${response.status}`);
  } catch (error) {
    const errorMsg = error.response
      ? `錯誤 ${error.response.status}: ${error.response.data?.message || '未知錯誤'}`
      : error.message || '無法連接到伺服器';
    console.error('獲取預約資料失敗:', errorMsg);
    throw new Error(errorMsg);
  }
},

    /**
     * 根據 ID 獲取特定預約資料
     * @param {number} id - 預約 ID
     * @returns {Promise<Object>} 預約資料物件
     */
    async getReservationById(id) {
  try {
    // 先打 view（鍵名是大寫底線）
    const res = await reservationApi.getReservationById(id);
    if (res.status === 200) return normalizeReservation(res.data);
  } catch (e) {
    // 若是 404，回退打實體版（鍵名是 camelCase）
    const msg = String(e?.message || '');
    if (msg.includes('404')) {
      const res2 = await reservationApi.getReservationEntityById(id);
      if (res2.status === 200) return normalizeReservation(res2.data);
    }
    throw e;
  }
},


    /**
     * 新增預約
     * @param {Object} reservationData - 預約資料
     * @returns {Promise<Object>} 新增後的預約資料
     */
    async createReservation(reservationData) {
        try {
            // 轉換前端資料為 API 所需格式
            const ResCreateRequest = {
                memberId: reservationData.memberId,
                busId: reservationData.busId,
                startAddress: reservationData.startAddress,
                endAddress: reservationData.endAddress,
                scheduledAt: toYMDHM(reservationData.scheduledAt),
                note: reservationData.note || '',

                // 只有在找不到地址時才提供座標作為後備
                startLat: reservationData.coords?.startLat,
                startLng: reservationData.coords?.startLng,
                endLat: reservationData.coords?.endLat,
                endLng: reservationData.coords?.endLng
            };

            // 移除 undefined 欄位
            const cleanRequest = compact(ResCreateRequest);
            console.log('發送新增預約請求:', cleanRequest); // 偵錯日誌

            // 呼叫 API
            const response = await reservationApi.createReservation(cleanRequest);

            if (response.status === 200 || response.status === 201) {
                const reservation = response.data;

                // 新增：先算好展示用字串
                const formattedDistance = this._formatDistance(reservation.DISTANCE_METERS);
                const formattedPrice = `$${reservation.PRICE || 0}`;
                const statusText = this._formatStatus(reservation.RESERVATION_STATUS);

                return {
                    id: reservation.ID,
                    memberId: reservation.MEMBER_ID,
                    busId: reservation.BUS_ID,
                    startAddress: reservation.START_ADDRESS,
                    endAddress: reservation.END_ADDRESS,
                    startLat: reservation.START_LAT,
                    startLng: reservation.START_LNG,
                    endLat: reservation.END_LAT,
                    endLng: reservation.END_LNG,
                    distanceMeters: reservation.DISTANCE_METERS,
                    createdAt: reservation.CREATED_AT,
                    scheduledAt: reservation.SCHEDULED_AT,
                    price: reservation.PRICE,
                    reservationStatus: reservation.RESERVATION_STATUS,
                    note: reservation.NOTE,

                    // 顯示用（保留一份給列表直接讀）
                    formattedDistance,
                    formattedPrice,

                    // 列表用的 display 物件
                    display: {
                        statusText,
                        formattedDistance,
                        formattedPrice
                    }
                };
            }

            throw new Error(`伺服器回應錯誤：${response.status}`);
        } catch (error) {
            const errorMsg = error.response ?
                `錯誤 ${error.response.status}: ${error.response.data?.message || JSON.stringify(error.response.data) || '未知錯誤'}` :
                error.message || '無法連接到伺服器';

            console.error('新增預約失敗:', error);
            throw new Error(errorMsg);
        }
    },

    /**
     * 更新預約資料
     * @param {number} id - 預約 ID
     * @param {Object} reservationData - 預約更新資料
     * @returns {Promise<Object>} 更新後的預約資料
     */
    async updateReservation(id, reservationData) {
        console.log('更新預約 ID:', id, '資料:', reservationData);

        try {
            if (!id) throw new Error('預約 ID 不能為空');
            // 轉換前端資料為後端需要的格式

            const ResRequest = {
                id: parseInt(id),
                busId: reservationData.busId,
                startAddress: reservationData.startAddress,
                endAddress: reservationData.endAddress,
                scheduledAt: toYMDHM(reservationData.scheduledAt),
                reservationStatus: reservationData.reservationStatus || 'pending',
                note: reservationData.note || '',


                // 扁平化座標資料
                startLat: reservationData.startLat || reservationData.coords?.startLat || null,
                startLng: reservationData.startLng || reservationData.coords?.startLng || null,
                endLat: reservationData.endLat || reservationData.coords?.endLat || null,
                endLng: reservationData.endLng || reservationData.coords?.endLng || null,

                // 添加價格
                price: reservationData.price || reservationData.system?.price || 0
            };

            // 呼叫 API
            const response = await reservationApi.updateReservation(id, ResRequest);
            console.log('更新預約回應:', response);
            return response.data;
        } catch (error) {
            console.error('更新預約失敗:', error);
            throw new Error(`更新預約失敗: ${error.message}`);
        }
    },


    /**
     * 刪除預約
     * @param {number} id - 預約 ID
     * @returns {Promise<boolean>} 是否成功刪除
     */
    async deleteReservation(id) {
        try {
            const response = await reservationApi.deleteReservation(id);

            // 刪除成功回傳 204 No Content
            return response.status === 204;
        } catch (error) {
            const errorMsg = error.response ?
                `錯誤 ${error.response.status}: ${error.response.data?.message || '未知錯誤'}` :
                error.message || '無法連接到伺服器';

            console.error(`刪除預約 ID ${id} 失敗:`, errorMsg);
            throw new Error(errorMsg);
        }
    },

    /**
     * 搜尋預約（依會員、地址關鍵字、日期）
     * @param {Object} searchParams - 搜尋參數
     * @param {number} [searchParams.memberId] - 會員 ID
     * @param {string} [searchParams.startAddress] - 起點地址關鍵字
     * @param {string} [searchParams.endAddress] - 終點地址關鍵字
     * @param {Date|string} [searchParams.scheduledAt] - 預約日期
     * @param {Date|string} [searchParams.scheduledAtTime] - 精確預約時間
     * @returns {Promise<Array>} 搜尋結果陣列
     */
    // 直接貼上覆蓋原本的 searchReservations
async searchReservations(searchParams = {}) {
  try {
    // 1) 清理參數
    const clean = {};
    for (const [key, value] of Object.entries(searchParams)) {
      if (value === null || value === undefined) continue;
      const s = String(value).trim();
      if (!s || s === 'undefined') continue;

      // 時間參數統一到秒
      if (key === 'scheduledAt' || key === 'scheduledAtTime') {
        clean[key] = toYMDHM(value);
      } else if (key === 'scheduledDate') {
        // 只到天：yyyy-MM-dd
        clean[key] = s;
      } else {
        clean[key] = value;
      }
    }

    console.log('發送搜尋參數:', clean);

    // 2) 呼叫 API
    const { status, data } = await reservationApi.searchReservation(clean);

    // 3) 用「單一轉換器」處理回應（會帶出 completedAt、status 小寫、顯示欄位）
    if (status === 200) {
      const rows = data ?? [];
      return rows.map(normalizeReservation);
    }

    throw new Error(`伺服器回應錯誤：${status}`);
  } catch (error) {
    console.error('搜尋預約資料失敗:', error);
    // 錯誤就回空陣列給畫面
    return [];
  }
},

    /**
   * 標記預約為已完成
   * 200：回傳更新後的單筆資料（可直接用來更新該列）
   * 204：無內容（請呼叫端改用重載列表）
   */
    async completeReservation(id) {
    if (!id) throw new Error('預約單 ID 不能為空');

    try {
      const response = await reservationApi.completeReservation(id); // -> { status, data }

      // ---- 後端回 200 + JSON（建議）----
      if (response.status === 200 && response.data) {
        const reservation = response.data;

        // 可能是 UPPER_SNAKE 或 camelCase，做個兼容擷取
        const pick = (U, c) => reservation[U] ?? reservation[c];

        const distanceMeters = pick('DISTANCE_METERS', 'distanceMeters');
        const price = pick('PRICE', 'price') ?? 0;
        const rawStatus = String(pick('RESERVATION_STATUS', 'reservationStatus') || '').toLowerCase();

        // 展示用字串（若你服務已有 _formatDistance/_formatStatus 就用；否則用備援）
        const formattedDistance = this._formatDistance
          ? this._formatDistance(distanceMeters)
          : (distanceMeters != null ? `${(distanceMeters / 1000).toFixed(1)} km` : '—');

        const formattedPrice = `$${price}`;

        const statusText = this._formatStatus
          ? this._formatStatus(rawStatus)
          : ({
              pending: '待處理',
              in_progress: '進行中',
              completed: '已完成',
              cancelled: '已取消'
            }[rawStatus] || '未知狀態');

        return {
          id: pick('ID', 'id'),
          memberId: pick('MEMBER_ID', 'memberId'),
          busId: pick('BUS_ID', 'busId'),
          startAddress: pick('START_ADDRESS', 'startAddress'),
          endAddress: pick('END_ADDRESS', 'endAddress'),
          scheduledAt: pick('SCHEDULED_AT', 'scheduledAt'),
          completedAt: pick('COMPLETED_AT', 'completedAt'),
          note: pick('NOTE', 'note'),

          reservationStatus: rawStatus,
          distanceMeters,
          price,
          createdAt: pick('CREATED_AT', 'createdAt'),
          coords: {
            startLat: pick('START_LAT', 'startLat'),
            startLng: pick('START_LNG', 'startLng'),
            endLat: pick('END_LAT', 'endLat'),
            endLng: pick('END_LNG', 'endLng')
          },

          formattedDistance,
          formattedPrice,
          display: {
            statusText,
            formattedDistance,
            formattedPrice
          }
        };
      }

      // ---- 後端回 204（No Content）----
      if (response.status === 204) {
        return null; // 交給呼叫端決定是否整頁重載
      }

      throw new Error(`伺服器回應錯誤：${response.status}`);
    } catch (error) {
      // 注意：這裡沒有 axios 的 error.response
      const errorMsg = error?.message || '無法連接到伺服器';
      console.error(`標記預約 ID ${id} 為已完成失敗:`, errorMsg);
      throw new Error(errorMsg);
    }
  },

    /**
     * 格式化預約狀態為更友善的顯示文字
     * @param {string} status - 原始狀態代碼
     * @returns {string} 格式化後的狀態文字
     * @private
     */
    _formatStatus(status) {
        const statusMap = {
            'pending': '待處理',
            'in_progress': '進行中',
            'completed': '已完成',
            'cancelled': '已取消'
        };
        return statusMap[status] || status || '未知狀態';
    },

    /**
     * 格式化距離顯示
     * @param {number} meters - 距離（公尺）
     * @returns {string} 格式化後的距離文字
     * @private
     */
    _formatDistance(meters) {
        if (!meters && meters !== 0) return '未知距離';

        if (meters < 1000) {
            return `${meters}公尺`;
        } else {
            const km = (meters / 1000).toFixed(1);
            return `${km}公里`;
        }
    },

    // 用於調用 API 進行距離和價格計算
    async calculateDistanceAndPrice(startAddress, endAddress) {
        try {
            if (!startAddress || !endAddress) {
                throw new Error('起點和終點地址不能為空');
            }

            console.log('計算從', startAddress, '到', endAddress, '的距離與價格');

            try {
                // 嘗試呼叫 API 計算距離與價格
                const response = await reservationApi.calculateDistanceAndPrice(startAddress, endAddress);

                if (response.status === 200 && response.data) {
                    console.log('取得價格報價:', response.data);
                    return response.data;
                }
            } catch (apiError) {
                console.error('API 呼叫失敗:', apiError);
                // 使用預設計算
                console.log('使用預設計算方法');
            }

            // API 呼叫失敗時使用預設值
            return getDefaultFareQuote(startAddress, endAddress);

        } catch (error) {
            console.error('計算距離與價格失敗:', error);
            // 完全失敗時也使用預設值
            return getDefaultFareQuote(startAddress, endAddress);
        }
    }

};