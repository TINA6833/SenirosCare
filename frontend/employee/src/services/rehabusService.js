import { busApi } from '@/api/rehabusApi';

/**
 * 復康巴士服務模組 - 處理業務邏輯和資料轉換
 */
export const rehabusService = {
  /**
   * 獲取所有復康巴士列表並進行資料轉換
   * @returns {Promise<Array>} 轉換後的復康巴士資料陣列
   */
  async getAllBuses() {
    try {
      // 呼叫 API 層取得資料
      const response = await busApi.getAllBuses();
      
      // 檢查回應是否成功
      if (response.status === 200) {
        // 轉換資料格式為前端所需的格式
        // 根據 Rehabus.java 的實際結構進行資料轉換
        return response.data.map(bus => ({
          id: bus.BUS_ID, // 對應 @JsonProperty("BUS_ID")
          carDealership: bus.CAR_DEALERSHIP || '未知車行', // 對應 @JsonProperty("CAR_DEALERSHIP")
          busBrand: bus.BUS_BRAND || '未知廠牌', // 對應 @JsonProperty("BUS_BRAND")
          busModel: bus.BUS_MODEL || '未知型號', // 對應 @JsonProperty("BUS_MODEL")
          seatCapacity: bus.SEAT_CAPACITY || 0, // 對應 @JsonProperty("SEAT_CAPACITY")
          wheelchairCapacity: bus.WHEELCHAIR_CAPACITY || 0, // 對應 @JsonProperty("WHEELCHAIR_CAPACITY")
          licensePlate: bus.LICENSE_PLATE || '未登記', // 對應 @JsonProperty("LICENSE_PLATE")
          status: bus.STATUS || 'available', // 對應 @JsonProperty("STATUS")
          // 增加前端顯示用的綜合欄位
          totalCapacity: (bus.SEAT_CAPACITY || 0) + (bus.WHEELCHAIR_CAPACITY || 0),
          displayName: `${bus.BUS_BRAND || '未知廠牌'} ${bus.BUS_MODEL || '未知型號'} (${bus.LICENSE_PLATE || '未登記'})`
        }));
      }
      
      // 如果回應不是 200，拋出錯誤
      throw new Error(`伺服器回應錯誤：${response.status}`);
    } catch (error) {
      // 整合錯誤訊息
      const errorMsg = error.response ? 
        `錯誤 ${error.response.status}: ${error.response.data.message || '未知錯誤'}` : 
        error.message || '無法連接到伺服器';
      
      console.error('獲取復康巴士資料失敗:', errorMsg);
      throw new Error(errorMsg);
    }
  },

  /**
   * 根據 ID 獲取特定復康巴士資料
   * @param {number} busId - 復康巴士 ID
   * @returns {Promise<Object>} 復康巴士資料物件
   */
  async getBusById(busId) {
    try {
      const response = await busApi.getBusById(busId);
      if (response.status === 200) {
        // 轉換單一復康巴士資料，根據 Rehabus.java 的實際結構
        const bus = response.data;
        return {
          id: bus.BUS_ID,
          carDealership: bus.CAR_DEALERSHIP || '未知車行',
          busBrand: bus.BUS_BRAND || '未知廠牌',
          busModel: bus.BUS_MODEL || '未知型號',
          seatCapacity: bus.SEAT_CAPACITY || 0,
          wheelchairCapacity: bus.WHEELCHAIR_CAPACITY || 0,
          licensePlate: bus.LICENSE_PLATE || '未登記',
          status: bus.STATUS || 'available',
          // 增加前端顯示用的綜合欄位
          totalCapacity: (bus.SEAT_CAPACITY || 0) + (bus.WHEELCHAIR_CAPACITY || 0),
          displayName: `${bus.BUS_BRAND || '未知廠牌'} ${bus.BUS_MODEL || '未知型號'} (${bus.LICENSE_PLATE || '未登記'})`
        };
      }
      throw new Error(`伺服器回應錯誤：${response.status}`);
    } catch (error) {
      const errorMsg = error.response ? 
        `錯誤 ${error.response.status}: ${error.response.data.message || '未知錯誤'}` : 
        error.message || '無法連接到伺服器';
      
      console.error(`獲取復康巴士 ID ${busId} 資料失敗:`, errorMsg);
      throw new Error(errorMsg);
    }
  },

  /**
   * 依據座位數量和輪椅數量篩選復康巴士
   * @param {Object} params - 查詢參數
   * @param {number|null} params.minSeats - 最小一般座位數量 (若為 null 則不限制)
   * @param {number|null} params.minWheels - 最小輪椅座位數量 (若為 null 則不限制)
   * @returns {Promise<Array>} 轉換後的復康巴士資料陣列
   */
  async searchBusesBySeats(params = { minSeats: null, minWheels: null }) {
    try {
      const response = await busApi.searchBuses(params);
      
      if (response.status === 200) {
        return response.data.map(bus => ({
          id: bus.BUS_ID,
          carDealership: bus.CAR_DEALERSHIP || '未知車行',
          busBrand: bus.BUS_BRAND || '未知廠牌',
          busModel: bus.BUS_MODEL || '未知型號',
          seatCapacity: bus.SEAT_CAPACITY || 0,
          wheelchairCapacity: bus.WHEELCHAIR_CAPACITY || 0,
          licensePlate: bus.LICENSE_PLATE || '未登記',
          status: bus.STATUS || '未知狀態',
          totalCapacity: (bus.SEAT_CAPACITY || 0) + (bus.WHEELCHAIR_CAPACITY || 0),
          displayName: `${bus.BUS_BRAND || '未知廠牌'} ${bus.BUS_MODEL || '未知型號'} (${bus.LICENSE_PLATE || '未登記'})`
        }));
      }
      
      throw new Error(`伺服器回應錯誤：${response.status}`);
    } catch (error) {
      const errorMsg = error.response ? 
        `錯誤 ${error.response.status}: ${error.response.data.message || '未知錯誤'}` : 
        error.message || '無法連接到伺服器';
      
      console.error('篩選復康巴士資料失敗:', errorMsg);
      throw new Error(errorMsg);
    }
  },

  /**
   * 新增復康巴士
   * @param {Object} busData - 復康巴士資料
   * @returns {Promise<Object>} 新增後的復康巴士資料
   */
  async addBus(busData) {
    try {
       // 確保狀態有值
      const busRequest = {
        carDealership: busData.carDealership,
        busBrand: busData.busBrand,
        busModel: busData.busModel,
        seatCapacity: busData.seatCapacity,
        wheelchairCapacity: busData.wheelchairCapacity,
        licensePlate: busData.licensePlate,
        status: busData.status || 'available'
      };

      console.log('發送新增巴士請求:', busRequest); // 偵錯日誌
      
      // 呼叫 API
      const response = await busApi.addBus(busRequest);
      
      if (response.status === 200 || response.status === 201) {
        const bus = response.data;
        return {
        id: bus.BUS_ID || bus.BUS_ID,
        carDealership: bus.CAR_DEALERSHIP,
        busBrand: bus.BUS_BRAND,
        busModel: bus.BUS_MODEL,
        seatCapacity: bus.SEAT_CAPACITY,
        wheelchairCapacity: bus.WHEELCHAIR_CAPACITY,
        licensePlate: bus.LICENSE_PLATE,
        // 確保狀態正確傳回，如果後端沒有傳回狀態，則使用請求時的狀態
        status: bus.STATUS || busRequest.STATUS,
        totalCapacity: (bus.SEAT_CAPACITY || 0) + (bus.WHEELCHAIR_CAPACITY || 0)
        };
      }
      
      throw new Error(`伺服器回應錯誤：${response.status}`);
    } catch (error) {
      // 提供更詳細的錯誤資訊
      const errorMsg = error.response ? 
        `錯誤 ${error.response.status}: ${error.response.data?.message || JSON.stringify(error.response.data) || '未知錯誤'}` :
        error.message || '無法連接到伺服器';
      
      console.error('新增復康巴士失敗:', error);
      throw new Error(errorMsg);
    }
  },

  /**
   * 更新復康巴士資料
   * @param {number} busId - 復康巴士 ID
   * @param {Object} busData - 復康巴士更新資料
   * @returns {Promise<Object>} 更新後的復康巴士資料
   */
  async updateBus(busId, busData) {
    try {
      // 轉換前端資料為後端需要的格式
      const busRequest = {
        busId: busId,
        carDealership: busData.carDealership,
        busBrand: busData.busBrand,
        busModel: busData.busModel,
        seatCapacity: busData.seatCapacity,
        wheelchairCapacity: busData.wheelchairCapacity,
        licensePlate: busData.licensePlate,
        status: busData.status
      };
      
      // 呼叫 API 進行更新
      const response = await busApi.updateBus(busId, busRequest);
      
      if (response.status === 200) {
      return response.data;
    }
    
    throw new Error(`伺服器回應錯誤：${response.status}`);
  } catch (error) {
    // 處理錯誤
    const errorMsg = error.response ? 
      `錯誤 ${error.response.status}: ${error.response.data?.message || '未知錯誤'}` : 
      error.message || '無法連接到伺服器';
    
    console.error('更新復康巴士資料失敗:', errorMsg);
    throw new Error(errorMsg);
  }
},

  /**
   * 刪除復康巴士
   * @param {number} busId - 復康巴士 ID
   * @returns {Promise<boolean>} 是否成功刪除
   */
  async deleteBus(busId) {
    try {
      const response = await busApi.deleteBus(busId);
      
      // 刪除成功回傳 204 No Content
      return response.status === 204;
    } catch (error) {
      const errorMsg = error.response ? 
        `錯誤 ${error.response.status}: ${error.response.data.message || '未知錯誤'}` : 
        error.message || '無法連接到伺服器';
      
      console.error(`刪除復康巴士 ID ${busId} 失敗:`, errorMsg);
      throw new Error(errorMsg);
    }
  },

  /**
   * 從 CSV 檔案匯入復康巴士資料
   * @param {string} filePath - CSV 檔案路徑
   * @returns {Promise<number>} 匯入的資料筆數
   */
  async importFromCsv(filePath) {
    try {
      const response = await busApi.importFromCsv(filePath);
      
      if (response.status === 200) {
        return response.data; // 回傳匯入的筆數
      }
      
      throw new Error(`伺服器回應錯誤：${response.status}`);
    } catch (error) {
      const errorMsg = error.response ? 
        `錯誤 ${error.response.status}: ${error.response.data.message || '未知錯誤'}` : 
        error.message || '無法連接到伺服器';
      
      console.error('從 CSV 匯入復康巴士資料失敗:', errorMsg);
      throw new Error(errorMsg);
    }
  }
};