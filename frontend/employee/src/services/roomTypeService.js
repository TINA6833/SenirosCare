import { roomTypeApi } from '../api/roomTypeApi'; // ✅ 只從這裡 import

export const roomTypeService = {
  // 取得所有房型
  async getAllRoomTypes() {
    try {
      console.log('📡 呼叫 roomTypeApi.getAllRoomTypes()...');
      const res = await roomTypeApi.getAllRoomTypes();

      console.log('🏠 後端 API 完整回應:', res);
      console.log('🏠 後端回應的 data:', res.data);

      // 🔍 檢查第一筆資料的所有欄位
      if (res.data && res.data.length > 0) {
        console.log('🔍 第一筆原始資料完整結構:', res.data[0]);
        console.log('🔍 第一筆資料的所有 keys:', Object.keys(res.data[0]));
        console.log('🔍 is_available 相關欄位檢查:', {
          'is_available': res.data[0].is_available,
          'isAvailable': res.data[0].isAvailable,
          'available': res.data[0].available,
          'Is_Available': res.data[0].Is_Available,
          'IS_AVAILABLE': res.data[0].IS_AVAILABLE
        });
      }

      const transformedRooms = res.data.map(transformRoom);
      return transformedRooms;
    } catch (e) {
      console.error('❌ roomTypeService.getAllRoomTypes 錯誤:', e);
      handleError(e, '取得房型列表');
    }
  },

  async getRoomTypeById(id) {
    try {
      console.log('📡 呼叫 getRoomTypeById, ID:', id);
      const res = await roomTypeApi.getRoomTypeById(id);
      if (res.status === 200) {
        const transformedRoom = transformRoom(res.data);
        console.log('✅ getRoomTypeById 成功:', transformedRoom);
        return transformedRoom;
      }
      throw new Error(`伺服器回應錯誤：${res.status}`);
    } catch (e) {
      console.error('❌ getRoomTypeById 失敗:', e);
      handleError(e, `獲取房型 ID ${id}`);
    }
  },

  async getRoomTypesByDescription(keyword) {
    try {
      const res = await roomTypeApi.getByDescription(keyword);
      if (res.status === 200) {
        return res.data.map(transformRoom);
      }
      throw new Error(`伺服器回應錯誤：${res.status}`);
    } catch (e) {
      handleError(e, `描述模糊查詢 (${keyword})`);
    }
  },

  async getRoomTypesByPriceRange(min, max) {
    try {
      const res = await roomTypeApi.getByPriceRange(min, max);
      if (res.status === 200) {
        return res.data.map(transformRoom);
      }
      throw new Error(`伺服器回應錯誤：${res.status}`);
    } catch (e) {
      handleError(e, `價格區間查詢 (${min}~${max})`);
    }
  },

  async getRoomTypesByCapacity(capacity) {
    try {
      const res = await roomTypeApi.getByCapacity(capacity);
      if (res.status === 200) {
        return res.data.map(transformRoom);
      }
      throw new Error(`伺服器回應錯誤：${res.status}`);
    } catch (e) {
      handleError(e, `容量查詢 (${capacity})`);
    }
  },

  async addRoomType(formData) {
    try {
      console.log('=== Debug FormData ===');
      for (let [key, value] of formData.entries()) {
        if (value instanceof File) {
          console.log(`${key}: File(${value.name}, ${value.size} bytes, ${value.type})`);
        } else {
          console.log(`${key}: ${value}`);
        }
      }
      console.log('======================');

      const res = await roomTypeApi.addRoomType(formData);
      if (res.status === 200 || res.status === 201) {
        return res.data; // 回傳新增的 ID
      }
      throw new Error(`伺服器回應錯誤：${res.status}`);
    } catch (e) {
      handleError(e, '新增房型');
    }
  },

  async updateRoomType(roomData) {
    try {
      const res = await roomTypeApi.updateRoomType(roomData);
      if (res.status === 200) {
        return res.data;
      }
      throw new Error(`伺服器回應錯誤：${res.status}`);
    } catch (e) {
      handleError(e, '修改房型');
    }
  },

  async deleteRoomType(id) {
    try {
      const res = await roomTypeApi.deleteRoomType(id);
      return res.data;
    } catch (e) {
      handleError(e, `刪除房型 ID ${id}`);
      return false;
    }
  },

  async patchRoomType(id, updates) {
    return roomTypeApi.patchRoomType(id, updates);
  },

  async patchRoomTypeWithImage(id, formData) {
    return roomTypeApi.patchRoomTypeWithImage(id, formData);
  },

  // 🆕 專門更新上架狀態的方法
  async updateAvailabilityStatus(roomId, isAvailable) {
    try {
      // 轉換 boolean 為數字 (1/0) - SQL 需要的格式
      const availableValue = isAvailable ? 1 : 0;

      console.log(`📡 更新房型 ${roomId} 上架狀態: ${availableValue}`);

      // 使用 PATCH 方法只更新 is_available 欄位
      const res = await roomTypeApi.patchRoomType(roomId, {
        is_available: availableValue  // 直接使用資料庫欄位名稱
      });

      if (res && (res.status === 200 || res.data)) {
        console.log(`✅ 房型 ${roomId} 上架狀態已更新為: ${availableValue}`);
        return true;
      }

      throw new Error('伺服器回應異常');
    } catch (e) {
      console.error('❌ 更新上架狀態失敗:', e);
      throw e; // 重新拋出錯誤，讓 UI 可以處理
    }
  },

  // 新增：用 FormData 更新房型（含圖片或純資料）
  async updateRoomTypeWithFormData(roomData) {
    const formData = new FormData();
    formData.append('name', this.form.name);
    formData.append('price', this.form.price);
    formData.append('capacity', this.form.capacity);
    formData.append('description', this.form.description || '');
    formData.append('imagePath', this.form.imagePath || '');
    formData.append('isAvailable', this.form.isAvailable ? 1 : 0);
    formData.append('adminNote', this.form.adminNote || '');
    if (this.form.imageFile) {
      formData.append('image', this.form.imageFile);
    }
    return roomTypeApi.patchRoomTypeWithImage(roomData.id, formData);
  },
};

function transformRoom(room) {
  console.log('🔄 Transform Room - 完整原始資料:', room);
  console.log('🔄 Transform Room - 所有 keys:', Object.keys(room));

  // 處理圖片路徑
  let img = room.image_path || '';
  if (img && !img.startsWith('http')) {
    if (!img.startsWith('/')) {
      img = '/' + img;
    }
    img = 'http://localhost:8080' + img;
  }

  // 🔧 處理 is_available 欄位 (確保正確轉換)
  let isAvailable = false;
  if (room.hasOwnProperty('is_available')) {
    // 處理數字 (1/0) 和布林值
    isAvailable = room.is_available === 1 || room.is_available === true;
    console.log(`🔧 is_available: ${room.is_available} → 轉換為: ${isAvailable}`);
  } else if (room.hasOwnProperty('isAvailable')) {
    isAvailable = room.isAvailable === 1 || room.isAvailable === true;
    console.log(`🔧 isAvailable: ${room.isAvailable} → 轉換為: ${isAvailable}`);
  }

  const transformedRoom = {
    id: room.id,
    name: room.name || '未命名',
    price: room.price,
    capacity: room.capacity,
    description: room.description || '無描述',
    special_features: room.special_features || '',
    imagePath: img,
    isAvailable: isAvailable,  // 前端統一使用 isAvailable

    // 加上這三個欄位
    createdAt: room.created_at,
    updatedAt: room.updated_at,
    adminNote: room.admin_note,
  };

  return transformedRoom;
}

function handleError(error, action) {
  let errorMsg;

  console.error('完整錯誤物件:', error); // ✅ 加上詳細 debug

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


