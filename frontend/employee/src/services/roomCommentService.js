import { roomCommentApi } from '@/api/roomCommentApi';

export const roomCommentService = {
  // 新增留言
  async create(comment) {
    try {
      const res = await roomCommentApi.create(comment);
      return res.data;
    } catch (e) {
      handleError(e, '新增留言');
    }
  },

  // 取得全部留言
  async getAll() {
    try {
      const res = await roomCommentApi.getAll();
      return res.data;
    } catch (e) {
      handleError(e, '取得全部留言');
    }
  },

  // 取得單筆留言
  async getById(id) {
    try {
      const res = await roomCommentApi.getById(id);
      return res.data;
    } catch (e) {
      handleError(e, '取得單筆留言');
    }
  },

  // 依房型查詢留言（可選 approved）
  async getByRoomType(roomTypeId, approved) {
    try {
      const res = await roomCommentApi.getByRoomType(roomTypeId, approved);
      return res.data;
    } catch (e) {
      handleError(e, '依房型查詢留言');
    }
  },

  // 依會員查詢留言（可選 approved）
  async getByMember(memberId, approved) {
    try {
      const res = await roomCommentApi.getByMember(memberId, approved);
      return res.data;
    } catch (e) {
      handleError(e, '依會員查詢留言');
    }
  },

  // 取得某房型最新通過留言
  async getLatestApproved(roomTypeId, limit = 5) {
    try {
      const res = await roomCommentApi.getLatestApproved(roomTypeId, limit);
      return res.data;
    } catch (e) {
      handleError(e, '取得最新通過留言');
    }
  },

  // 分頁查詢（後台）
  async getPage(roomTypeId, approved, page = 1, size = 10) {
    try {
      const res = await roomCommentApi.getPage(roomTypeId, approved, page, size);
      return res.data;
    } catch (e) {
      handleError(e, '分頁查詢留言');
    }
  },

  // 計數（後台）
  async getCount(roomTypeId, approved) {
    try {
      const res = await roomCommentApi.getCount(roomTypeId, approved);
      return res.data;
    } catch (e) {
      handleError(e, '計算留言數量');
    }
  },

  // 審核通過
  async approve(id) {
    try {
      const res = await roomCommentApi.approve(id);
      return res.data;
    } catch (e) {
      handleError(e, '審核通過留言');
    }
  },

  // 退回/不通過
  async unapprove(id) {
    try {
      const res = await roomCommentApi.unapprove(id);
      return res.data;
    } catch (e) {
      handleError(e, '退回留言');
    }
  },

  // 修改內容
  async updateContent(id, content) {
    try {
      const res = await roomCommentApi.updateContent(id, content);
      return res.data;
    } catch (e) {
      handleError(e, '修改留言內容');
    }
  },

  // 管理員回覆
  async setReply(id, adminReply) {
    try {
      const res = await roomCommentApi.setReply(id, adminReply);
      return res.data;
    } catch (e) {
      handleError(e, '管理員回覆留言');
    }
  },

  // 刪除留言
  async delete(id) {
    try {
      const res = await roomCommentApi.delete(id);
      return res.data;
    } catch (e) {
      handleError(e, '刪除留言');
    }
  }
};

// handleError 函數保留即可
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


