package com.ryanshiun.seniorscare.member.dao.member;

import com.ryanshiun.seniorscare.member.dto.member.*;
import com.ryanshiun.seniorscare.member.model.member.Member;

import java.util.List;

public interface MemberDao {

    // 查詢會員資料 (前端用)
    MemberProfileDto getMemberById(Integer memberId);

    // 查詢會員資料 (line登入用) - 透過 id
    Member passMemberInfoByLineId(String lineUserId);

    // 查詢 email 是否已經被註冊
    Integer isEmailExists(String email);

    // 客製化查詢會員
    List<MemberProfileDto> getMembers(MemberQueryParamsDto memberQueryParams);

    // 修改個人資料
    void updateProfile(MemberUpdateDto memberUpdateDto);

    // 啟用 or 停權會員
    void toggleMemberStatus(BanReasonDto banReasonDto);

    /* ------------------------- 登入相關 -------------------------- */

    // 更新登入時間
    void updateLastLogin(String lineUserId);

    // line登入 (首次登入)
    Integer register(MemberRegisterDto memberRegisterDto);

}
