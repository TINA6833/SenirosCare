// [重點] 修正匯入方式，使用具名匯入而非物件解構
import { getMyProfile, updateProfile } from '@/api/memberApi';

/**
 * [重點] 格式化日期顯示為年月日
 * @param {string|Date} dateString - 日期字串或日期物件
 * @returns {string} 格式化後的日期字串
 */
const formatDate = (dateString) => {
  if (!dateString) return 'N/A';
  
  try {
    const date = new Date(dateString);
    // [重點] 只顯示年月日，不顯示時間
    return new Intl.DateTimeFormat('zh-TW', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit'
    }).format(date);
  } catch (e) {
    console.error('日期格式化錯誤:', e);
    return dateString;
  }
};

/**
 * [重點] 轉換前端日期格式為後端接受的格式
 * @param {string} dateString - 前端日期格式 (YYYY-MM-DD)
 * @returns {string|null} 後端日期格式 (YYYY-MM-DDTHH:mm:ss.sssZ) 或 null
 */
const convertDateForBackend = (dateString) => {
  if (!dateString || dateString.trim() === '') {
    return null;
  }
  
  try {
    // [重點] 將 YYYY-MM-DD 轉換為完整的 ISO 8601 格式
    const date = new Date(dateString + 'T00:00:00.000Z');
    
    // [重點] 檢查日期是否有效
    if (isNaN(date.getTime())) {
      console.error('無效的日期格式:', dateString);
      return null;
    }
    
    // [重點] 返回 ISO 8601 格式的日期字串
    return date.toISOString();
  } catch (e) {
    console.error('日期轉換錯誤:', e);
    return null;
  }
};

/**
 * [重點] 轉換後端日期格式為前端輸入框格式
 * @param {string} dateString - 後端日期格式
 * @returns {string} 前端輸入框格式 (YYYY-MM-DD)
 */
const convertDateForInput = (dateString) => {
  if (!dateString || dateString === 'N/A') return '';
  
  try {
    const date = new Date(dateString);
    
    // [重點] 檢查日期是否有效
    if (isNaN(date.getTime())) {
      console.error('無效的日期格式:', dateString);
      return '';
    }
    
    // [重點] 返回 YYYY-MM-DD 格式
    return date.toISOString().split('T')[0];
  } catch (e) {
    console.error('日期轉換錯誤:', e);
    return '';
  }
};

export const memberService = {
  /**
   * [重點] 統一的資料轉換邏輯，對應 MemberProfileDto 結構   
   * @param {Object} MemberProfileDto - 後端回傳的會員資料
   * @returns {Object} 轉換後的前端使用格式
   */
  __transformMemberData(MemberProfileDto) {
    return {
      memberId: MemberProfileDto.memberId,
      memberName: MemberProfileDto.memberName,
      mainPhone: MemberProfileDto.mainPhone,
      // [重點] 修復性別轉換邏輯，處理 null/undefined 情況
      gender: MemberProfileDto.gender === true ? '男' : MemberProfileDto.gender === false ? '女' : null,
      birthday: formatDate(MemberProfileDto.birthday),
      // [重點] 新增原始生日資料，供編輯表單使用
      birthdayRaw: MemberProfileDto.birthday,
      address: MemberProfileDto.address,
      // [重點] 確保 email 正確傳遞，不要強制為空
      email: MemberProfileDto.email,
      // [重點] 確保 imagePath（Line 頭像 URL）正確傳遞
      imagePath: MemberProfileDto.imagePath,
      createdAt: formatDate(MemberProfileDto.createdAt),
      updatedAt: formatDate(MemberProfileDto.updatedAt),
      loginAt: formatDate(MemberProfileDto.loginAt)
    };
  },

  /**
   * [重點] 取得當前登入的會員資料
   * @returns {Promise<Object>} 轉換後的會員資料
   */
  async getCurrentMember() {
    try {
      // [重點] 直接使用匯入的函數
      const response = await getMyProfile();
      const transformedData = this.__transformMemberData(response.data);
      
      // [重點] 確保 Line 頭像 URL 有效性檢查
      if (transformedData.imagePath) {
        console.log('取得使用者 Line 頭像 URL:', transformedData.imagePath);
      } else {
        console.log('使用者尚未設定 Line 頭像');
      }
      
      return transformedData;
    } catch (error) {
      console.error('取得當前登入會員資料失敗:', error);
      throw error;
    }
  },

  /**
   * [重點] 修改自己會員資料
   * @param {Object} memberUpdate - 要更新的會員資料
   * @returns {Promise<Object>} 更新後的會員資料
   */
  async updateCurrentMember(memberUpdate) {
    try {
      // [重點] 驗證必填欄位
      if (!memberUpdate.memberName) {
        throw new Error('會員姓名為必填');
      }

      // [重點] 組成更新用的 DTO，特別處理日期和性別格式
      const memberUpdateDto = {
        memberName: memberUpdate.memberName,
        mainPhone: memberUpdate.mainPhone,
        // [重點] 轉換性別格式：字串 -> 布林值
        gender: memberUpdate.gender === 'true' ? true : memberUpdate.gender === 'false' ? false : null,
        // [重點] 轉換日期格式：YYYY-MM-DD -> ISO 8601
        birthday: convertDateForBackend(memberUpdate.birthday),
        address: memberUpdate.address,
        email: memberUpdate.email
        // [重點] imagePath 不允許前端更新，由後端 Line API 取得
      };

      console.log('準備送出的會員更新資料:', memberUpdateDto);

      // [重點] 直接使用匯入的函數
      const response = await updateProfile(memberUpdateDto);

      if (response.status === 200) {
        // [重點] 回傳轉換後的資料
        return this.__transformMemberData(response.data);
      }
      
      throw new Error('更新會員資料失敗');
    } catch (error) {
      console.error('更新會員資料時發生錯誤:', error);
      throw error;
    }
  },

  // [重點] 提供給外部使用的格式化函數
  formatDate,
  convertDateForInput,
  convertDateForBackend
};