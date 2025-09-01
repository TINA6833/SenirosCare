package com.ryanshiun.seniorscare.member.service.member.Impl;

import com.ryanshiun.seniorscare.member.dao.member.MemberDao;
import com.ryanshiun.seniorscare.member.dto.member.BanReasonDto;
import com.ryanshiun.seniorscare.member.dto.member.MemberProfileDto;
import com.ryanshiun.seniorscare.member.dto.member.MemberQueryParamsDto;
import com.ryanshiun.seniorscare.member.dto.member.MemberUpdateDto;
import com.ryanshiun.seniorscare.member.service.member.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MemberServiceImpl implements MemberService {

    @Autowired
    private MemberDao memberDao;

    /** 查詢會員資料 (前端用)
     * @param memberId 員工 ID
     * @return 會員詳細資料
     */
    @Override
    public MemberProfileDto getMemberById(Integer memberId) {
        return memberDao.getMemberById(memberId);
    }


    /** 客製化查詢會員
     * @param memberQueryParams 查詢參數
     * @return 會員列表
     */
    @Override
    public List<MemberProfileDto> getMembers(MemberQueryParamsDto memberQueryParams) {
        return memberDao.getMembers(memberQueryParams);
    }

    /** 修改個人資料
     * @param memberUpdateDto 會員資料
     */
    @Override
    public void updateProfile(MemberUpdateDto memberUpdateDto) {
        memberDao.updateProfile(memberUpdateDto);
    }

    /** 啟用 or 停權會員
     * @param banReasonDto 停權原因
     */
    @Override
    public void toggleMemberStatus(BanReasonDto banReasonDto) {
        memberDao.toggleMemberStatus(banReasonDto);
    }
}
