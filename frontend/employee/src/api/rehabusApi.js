import axiosInstance from './axiosInstance'; // 引入 axios 實例

/**
 * 復康巴士 API 模組 - 使用 axios 處理 HTTP 請求和回應
 * 根據 BusController.java 中的端點實作對應的方法
 */
export const busApi = {
  /**
   * 取得所有復康巴士列表
   * @returns {Promise} 包含所有復康巴士資料的 Promise 物件
   */
  getAllBuses() {
    return axiosInstance.get('/rehabus/findAll');
  },

  /**
   * 根據 ID 取得單一復康巴士
   * @param {number} busId - 復康巴士 ID
   * @returns {Promise} 包含復康巴士資料的 Promise 物件
   */
  getBusById(busId) {
    return axiosInstance.get(`/rehabus/${busId}`);
  },

  /**
   * 搜尋復康巴士
   * @param {Object} params - 搜尋參數
   * @returns {Promise} 包含搜尋結果的 Promise 物件
   */
  searchBuses(params) {
    // 構建查詢參數
    const queryParams = {};

    if (params.minSeats !== null && params.minSeats !== undefined) {
      queryParams.minSeats = params.minSeats;
    }

    if (params.minWheels !== null && params.minWheels !== undefined) {
      queryParams.minWheels = params.minWheels;
    }

    return axiosInstance.get('/rehabus/search', { params: queryParams });
  },

  /**
   * 新增復康巴士
   * @param {Object} busRequest - 復康巴士資料
   * @returns {Promise} 包含新增後復康巴士資料的 Promise 物件
   */
  addBus(busRequest) {
    return axiosInstance.post('/rehabus', busRequest);
  },

  /**
   * 更新復康巴士資料
   * @param {number} busId - 復康巴士 ID
   * @param {Object} busRequest - 復康巴士更新資料
   * @returns {Promise} 包含更新後復康巴士資料的 Promise 物件
   */
  updateBus(busId, busRequest) {
    if (!busId) throw new Error('復康巴士 ID 不能為空');
    if (!busRequest) throw new Error('復康巴士更新資料不能為空');

    // 確保資料符合後端API格式要求
    const payload = {
      carDealership: busRequest.carDealership,
      busBrand: busRequest.busBrand,
      busModel: busRequest.busModel,
      seatCapacity: parseInt(busRequest.seatCapacity) || 0,
      wheelchairCapacity: parseInt(busRequest.wheelchairCapacity) || 0,
      licensePlate: busRequest.licensePlate,
      status: busRequest.status || 'available'
    };

    console.log('API 層 - 準備更新復康巴士資料:', busId, payload);

    // 使用 axiosInstance.put 方法發送請求 
    return axiosInstance.put(`/rehabus/update/${busId}`, payload);
  },

  /**
   * 刪除復康巴士
   * @param {number} busId - 復康巴士 ID
   * @returns {Promise} 包含刪除結果的 Promise 物件
   */
  deleteBus(busId) {
    return axiosInstance.delete(`/rehabus/delete/${busId}`);
  },

  /**
   * 從 CSV 檔案匯入復康巴士資料
   * @param {string} filePath - CSV 檔案路徑
   * @returns {Promise} 包含匯入筆數的 Promise 物件
   */
  importFromCsv(filePath) {
    return axiosInstance.post('/rehabus/importcsv', null, { 
      params: { filePath } 
    });
  }
};