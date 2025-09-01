import { featureApi } from '../api/featureApi';

export const featureService = {
  // 取得所有特徵
  async getAllFeatures() {
    try {
      const res = await featureApi.getAllFeatures();
      return res.data;
    } catch (e) {
      handleError(e, '取得特徵列表');
    }
  },

  // 取得單一特徵
  async getFeatureById(id) {
    try {
      const res = await featureApi.getFeatureById(id);
      return res.data;
    } catch (e) {
      handleError(e, '取得特徵');
    }
  },

  // 新增特徵
  async addFeature(feature) {
    try {
      const res = await featureApi.addFeature(feature);
      return res.data;
    } catch (e) {
      handleError(e, '新增特徵');
    }
  },

  // 批次新增
  async batchCreate(features) {
    try {
      const res = await featureApi.batchCreate(features);
      return res.data;
    } catch (e) {
      handleError(e, '批次新增特徵');
    }
  },

  // 修改特徵
  async updateFeature(id, feature) {
    try {
      const res = await featureApi.updateFeature(id, feature);
      return res.data;
    } catch (e) {
      handleError(e, '修改特徵');
    }
  },

  // 刪除特徵
  async deleteFeature(id) {
    try {
      const res = await featureApi.deleteFeature(id);
      return res.data;
    } catch (e) {
      handleError(e, '刪除特徵');
    }
  },

  // 搜尋
  async searchFeatures(keyword) {
    try {
      const res = await featureApi.searchFeatures(keyword);
      return res.data;
    } catch (e) {
      handleError(e, '搜尋特徵');
    }
  },

  // 檢查名稱是否存在
  async exists(name) {
    try {
      const res = await featureApi.exists(name);
      return res.data;
    } catch (e) {
      handleError(e, '檢查特徵名稱');
    }
  },

  // 計數
  async count() {
    try {
      const res = await featureApi.count();
      return res.data;
    } catch (e) {
      handleError(e, '取得特徵數量');
    }
  }
};

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


