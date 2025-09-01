import api from "./axiosInstance";

/**
 * 復康巴士 API
 * 注意：此檔的路徑需與後端 BusController 對應
 * 後端 base path: /api/rehabus
 * 你的 axiosInstance 通常會把 baseURL 設成 /api，因此這裡用 /rehabus 開頭即可
 */
export const busApi = {
  /** 取得所有復康巴士（對應：GET /api/rehabus/findAll） */
  getAllBuses() {
    return api.get("/rehabus/findAll");
  },

  /** 依 ID 取得單一巴士（對應：GET /api/rehabus/{busId}） */
  getBusById(busId) {
    return api.get(`/rehabus/${busId}`);
  },

  /**
   * 依座位條件查詢巴士（對應：GET /api/rehabus/search?minSeats=&minWheels=）
   * @param {{ minSeats?: number, minWheels?: number }} params
   */
  searchBuses(params = {}) {
    const { minSeats, minWheels } = params;
    return api.get("/rehabus/search", {
      params: {
        // 後端參數名固定為 minSeats、minWheels，未填則不帶
        ...(minSeats != null ? { minSeats } : {}),
        ...(minWheels != null ? { minWheels } : {}),
      },
    });
  },

  /**
   * 新增復康巴士（對應：POST /api/rehabus）
   * 建議在 service 層把前端欄位轉成後端需要的 BusRequest 格式再丟進來
   * BusRequest 典型欄位：
   * { carDealership, busBrand, busModel, seatCapacity, wheelchairCapacity, licensePlate }
   */
  addBus(busRequest) {
    return api.post("/rehabus", busRequest);
  },

  /**
   * 更新復康巴士（對應：PUT /api/rehabus/update/{busId}）
   * 後端會從 path 取得 busId，body 仍是 BusRequest
   */
  updateBus(busId, busRequest) {
    return api.put(`/rehabus/update/${busId}`, busRequest);
  },

  /**
   * 刪除復康巴士（對應：DELETE /api/rehabus/delete/{busId}）
   * 後端成功會回 204 No Content，前端可只檢查 status 是否為 204
   */
  deleteBus(busId) {
    return api.delete(`/rehabus/delete/${busId}`);
  },

  /**
   * 匯入 CSV（對應：POST /api/rehabus/importcsv?filePath=...）
   * 後端是用 @RequestParam("filePath")，這裡以 query param 方式傳
   * 回傳為數字（匯入的筆數）
   */
  importFromCsv(filePath) {
    return api.post("/rehabus/importcsv", null, {
      params: { filePath },
    });
  },
};