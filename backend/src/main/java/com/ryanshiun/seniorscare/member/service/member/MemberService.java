package com.ryanshiun.seniorscare.member.service.member;

import com.ryanshiun.seniorscare.member.dto.member.*;

import java.util.List;

public interface MemberService {
    // 查詢會員資料 (前端用)
    MemberProfileDto getMemberById(Integer memberId);

    // 客製化查詢會員
    List<MemberProfileDto> getMembers(MemberQueryParamsDto memberQueryParams);

    // 修改個人資料
    void updateProfile(MemberUpdateDto memberUpdateDto);

    // 啟用 or 停權會員
    void toggleMemberStatus(BanReasonDto banReasonDto);

}
