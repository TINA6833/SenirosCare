import { facilityApi } from '../api/facilityApi'; // 改成設施 API

export const facilityService = {
  // 取得所有設施
  async getAllFacilities() {
    try {
      const res = await facilityApi.getAllFacilities();
      if (res.data && res.data.length > 0) {
        console.log('🔍 第一筆設施資料:', res.data[0]);
        console.log('🔍 設施所有 keys:', Object.keys(res.data[0]));
      }
      return res.data.map(transformFacility);
    } catch (e) {
      handleError(e, '取得設施列表');
    }
  },

  // 取得單一設施
  async getFacilityById(id) {
    const res = await facilityApi.getFacilityById(id);
    console.log('API回傳設施資料:', res.data);
    return transformFacility(res.data);
  },

  // 新增設施（含圖片）
  async addFacility(formData) {
    try {
      const res = await facilityApi.addFacility(formData);
      if (res.status === 200 || res.status === 201) {
        return res.data;
      }
      throw new Error(`伺服器回應錯誤：${res.status}`);
    } catch (e) {
      handleError(e, '新增設施');
      return null;
    }
  },

  // 更新設施（JSON）
  async updateFacility(facilityData) {
    try {
      const res = await facilityApi.updateFacility(facilityData.id, facilityData);
      if (res.status === 200) {
        return res.data;
      }
      throw new Error(`伺服器回應錯誤：${res.status}`);
    } catch (e) {
      handleError(e, '修改設施');
    }
  },

  // 部分更新（含圖片）
  async patchFacilityWithImage(id, formData) {
    return facilityApi.patchFacilityWithImage(id, formData);
  },

  // 刪除設施
  async deleteFacility(id) {
    try {
      const res = await facilityApi.deleteFacility(id);
      return res.data;
    } catch (e) {
      handleError(e, `刪除設施 ID ${id}`);
      return false;
    }
  },

  // 匯入 CSV
  async importCSV(file) {
    try {
      const formData = new FormData();
      formData.append('file', file);
      const res = await facilityApi.importCSV(formData);
      if (res.status === 200) {
        return res.data;
      }
      throw new Error(`伺服器回應錯誤：${res.status}`);
    } catch (e) {
      handleError(e, '匯入設施資料');
    }
  },

  // 匯出 CSV
  async exportCSV() {
    try {
      const res = await facilityApi.exportCSV();
      return res.data;
    } catch (e) {
      handleError(e, '匯出設施資料');
    }
  },

  // 更新設施上架狀態
  async updateAvailabilityStatus(id, isAvailable) {
    try {
      // 只更新 isAvailable 欄位
      const res = await facilityApi.patchFacility(id, { isAvailable });
      return res.status === 200;
    } catch (e) {
      handleError(e, '更新設施上架狀態');
      return false;
    }
  }
};

// 資料轉換
function transformFacility(facility) {
  let img = facility.image_path || '';
  if (img && !img.startsWith('http')) {
    if (!img.startsWith('/')) {
      img = '/' + img;
    }
    img = 'http://localhost:8080' + img;
  }

  return {
    id: facility.id,
    name: facility.name || '未命名',
    description: facility.description || '無描述',
    imagePath: img,
    isAvailable: facility.is_available === 1 || facility.is_available === true,
    createdAt: facility.created_at,
    updatedAt: facility.updated_at
  };
}

function handleError(error, action) {
  let errorMsg;
  console.error('完整錯誤物件:', error);

  if (error.response) {
    const status = error.response.status;
    const data = error.response.data;
    console.error(`API 回應錯誤: ${status}`, data);

    if (status === 415) {
      errorMsg = `${action} 失敗: 不支援的媒體類型 (415) - Content-Type 錯誤`;
    } else if (status === 404) {
      errorMsg = `${action} 失敗: 找不到端點 (404)`;
    } else {
      errorMsg = `${action} 失敗: HTTP ${status} - ${data?.message || '伺服器錯誤'}`;
    }
  } else if (error.request) {
    console.error('請求發出但無回應:', error.request);
    errorMsg = `${action} 失敗: 無法連接到伺服器 - 請檢查後端是否啟動`;
  } else {
    console.error('其他錯誤:', error.message);
    errorMsg = `${action} 失敗: ${error.message || '未知錯誤'}`;
  }

  console.error(errorMsg);
  throw new Error(errorMsg);
}


