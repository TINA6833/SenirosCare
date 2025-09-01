import { memberApi } from "@/api/memberApi";

/**
 * **重點：格式化日期顯示為年月日**
 * @param {string|Date} dateString - 日期字串或日期物件
 * @returns {string} 格式化後的日期字串
 */
const formatDate = (dateString) => {
  if (!dateString) return "N/A";

  try {
    const date = new Date(dateString);
    // 只顯示年月日，不顯示時間
    return new Intl.DateTimeFormat("zh-TW", {
      year: "numeric",
      month: "2-digit",
      day: "2-digit",
    }).format(date);
  } catch (e) {
    console.error("日期格式化錯誤:", e);
    return dateString;
  }
};

/**
 * **重點：判斷會員是否活躍 (根據最後登入時間)**
 * @param {string|Date} loginAt - 最後登入時間
 * @returns {boolean} 是否活躍 (一個月內登入為活躍)
 */
const isActiveUser = (loginAt) => {
  if (!loginAt) return false;
  
  try {
    const lastLoginDate = new Date(loginAt);
    const currentDate = new Date();
    const oneMonthAgo = new Date();
    oneMonthAgo.setMonth(currentDate.getMonth() - 1);
    
    // 如果最後登入時間在一個月內，則為活躍
    return lastLoginDate >= oneMonthAgo;
  } catch (e) {
    console.error("判斷活躍度時發生錯誤:", e);
    return false;
  }
};

export const memberService = {
  /**
   * **重點：統一的資料轉換邏輯，對應 MemberProfileDto 結構 - 新增 banReason 欄位**
   * @param {Object} MemberProfileDto - 後端回傳的會員資料
   * @returns {Object} 轉換後的前端會員資料格式
   */
  __transformMemberData(MemberProfileDto) {
    return {
      memberId: MemberProfileDto.memberId,
      memberName: MemberProfileDto.memberName,
      mainPhone: MemberProfileDto.mainPhone,
      gender: MemberProfileDto.gender === null ? "未提供" : (MemberProfileDto.gender ? "男" : "女"),
      birthday: formatDate(MemberProfileDto.birthday),
      address: MemberProfileDto.address,
      email: MemberProfileDto.email,
      imagePath: MemberProfileDto.imagePath,
      active: MemberProfileDto.active, // Boolean true = 啟用, false = 停權
      banReason: MemberProfileDto.banReason || '', // **重點：新增停權原因欄位**
      isActiveUser: isActiveUser(MemberProfileDto.loginAt),
      createdAt: formatDate(MemberProfileDto.createdAt),
      updatedAt: formatDate(MemberProfileDto.updatedAt),
      loginAt: formatDate(MemberProfileDto.loginAt),
    };
  },

  /**
   * **重點：取得所有會員資料 - 支援查詢參數**
   * @param {Object} options - 查詢選項
   * @param {string} options.memberName - 會員姓名 (模糊搜尋)
   * @param {string} options.address - 地址 (模糊搜尋)
   * @param {boolean} options.gender - 性別 (true=男, false=女)
   * @param {boolean} options.isActive - 會員狀態 (true=啟用, false=停權)
   * @returns {Promise<Array>} 會員資料陣列
   */
  async getAllMembers(options = {}) {
    try {
      console.log('[memberService] 開始取得會員資料，查詢參數:', options);
      
      // **重點：建構查詢參數物件**
      const queryParams = {};
      
      // 會員姓名搜尋
      if (options.memberName) {
        queryParams.memberName = options.memberName;
      }
      
      // 地址搜尋
      if (options.address) {
        queryParams.address = options.address;
      }
      
      // 性別篩選 (轉換為後端期望的格式)
      if (options.gender !== undefined && options.gender !== null) {
        queryParams.gender = options.gender === '男' ? true : false;
      }
      
      // **重點：會員狀態篩選 (這是查詢停權會員的關鍵)**
      if (options.isActive !== undefined && options.isActive !== null) {
        queryParams.isActive = options.isActive;
      }
      
      console.log('[memberService] 處理後的查詢參數:', queryParams);
      
      // **重點：呼叫 API 並傳入查詢參數**
      const response = await memberApi.getAllMembers(queryParams);
      
      // **重點：轉換資料格式，包含新的 banReason 欄位**
      const transformedData = response.data.map((member) =>
        memberService.__transformMemberData(member)
      );
      
      console.log('[memberService] 成功取得會員資料:', {
        總數: transformedData.length,
        查詢條件: queryParams,
        啟用會員: transformedData.filter(m => m.active).length,
        停權會員: transformedData.filter(m => !m.active).length
      });
      
      return transformedData;
    } catch (error) {
      console.error('[memberService] 取得會員資料失敗:', {
        查詢參數: options,
        錯誤訊息: error.message
      });
      throw new Error(`取得會員資料失敗: ${error.message}`);
    }
  },

  /**
   * **重點：取得特定會員資料**
   * @param {string} memberId - 會員ID
   * @returns {Promise<Object} 會員資料
   */
  async getMemberById(memberId) {
    try {
      console.log('[memberService] 開始取得會員資料:', memberId);
      const response = await memberApi.getMemberById(memberId);
      
      const transformedData = memberService.__transformMemberData(response.data);
      console.log('[memberService] 成功取得會員資料:', transformedData);
      return transformedData;
    } catch (error) {
      console.error('[memberService] 取得特定會員資料失敗:', error);
      throw new Error(`取得特定會員資料失敗: ${error.message}`);
    }
  },

  /**
   * **重點：切換會員狀態 (停權/啟用) - 修正欄位名稱配合後端 DTO**
   * @param {string} memberId - 會員ID
   * @param {boolean} isActive - 新的啟用狀態 (true=啟用, false=停權)
   * @param {string} banReason - 停權原因 (停權時需要提供)
   * @returns {Promise<Object>} 更新後的會員資料
   */
  async toggleMemberStatus(memberId, isActive, banReason = '') {
    try {
      console.log('[memberService] 開始切換會員狀態:', {
        memberId,
        newStatus: isActive ? '啟用' : '停權',
        banReason: isActive ? '無' : banReason
      });

      // **重點：建構 banReasonDto 物件，配合後端 BanReasonDto 結構**
      const banReasonDto = {
        memberId: parseInt(memberId), // 後端需要 Integer 類型
        isActive: isActive,           // 修正：使用 isActive 而不是 banned
        banReason: isActive ? '' : (banReason || '管理員停權') // 停權時需要提供原因
      };

      console.log('[memberService] 發送 banReasonDto:', banReasonDto);

      // **重點：呼叫 API 更新會員狀態**
      const response = await memberApi.updateMemberStatus(memberId, banReasonDto);
      
      console.log('[memberService] 會員狀態切換成功:', {
        memberId,
        status: isActive ? '啟用' : '停權',
        response: response.data
      });

      // **重點：轉換並回傳更新後的會員資料**
      return memberService.__transformMemberData(response.data);
    } catch (error) {
      console.error('[memberService] 切換會員狀態失敗:', {
        memberId,
        targetStatus: isActive ? '啟用' : '停權',
        error: error.message
      });
      
      // **重點：根據錯誤類型提供更具體的錯誤訊息**
      if (error.response) {
        const status = error.response.status;
        const data = error.response.data;
        
        switch (status) {
          case 404:
            throw new Error('找不到指定的會員');
          case 403:
            throw new Error('沒有權限執行此操作');
          case 400:
            throw new Error(data.message || '請求參數錯誤');
          default:
            throw new Error(data.message || `伺服器錯誤 (${status})`);
        }
      } else if (error.request) {
        throw new Error('網路連線錯誤，請檢查網路狀態');
      } else {
        throw new Error(`停權或啟用會員失敗: ${error.message}`);
      }
    }
  }
};
