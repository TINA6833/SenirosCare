package com.ryanshiun.seniorscare.member.controller.member;

import com.ryanshiun.seniorscare.member.dto.member.BanReasonDto;
import com.ryanshiun.seniorscare.member.dto.member.MemberProfileDto;
import com.ryanshiun.seniorscare.member.dto.member.MemberQueryParamsDto;
import com.ryanshiun.seniorscare.member.dto.member.MemberUpdateDto;
import com.ryanshiun.seniorscare.member.service.member.MemberService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/members")
@Validated
public class MemberController {
    @Autowired
    private MemberService memberService;

    /**
     * 查詢當前登入會員自己的資料
     * @param authentication Spring Security 自動注入的認證物件
     * @return 當前登入會員的資料
     */
    @GetMapping("/me")
    public ResponseEntity<MemberProfileDto> getMyProfile(Authentication authentication) {
        // 從 Authentication 物件中取得 memberId
        Integer memberId = Integer.parseInt(authentication.getName());
        MemberProfileDto member = memberService.getMemberById(memberId);
        if (member != null) {
            return ResponseEntity.status(HttpStatus.OK).body(member);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /** 修改自己會員資料
     * @param memberUpdateDto 會員更新資料
     * @return 更新成功回傳最新會員資料
     */
    @PutMapping("/me")
    public ResponseEntity<MemberProfileDto> updateProfile(
            @RequestBody @Valid MemberUpdateDto memberUpdateDto,
            Authentication authentication
    ) {
        Integer memberId = Integer.parseInt(authentication.getName());
        MemberProfileDto member = memberService.getMemberById(memberId);
        if (member == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        memberUpdateDto.setMemberId(memberId);
        memberService.updateProfile(memberUpdateDto);
        MemberProfileDto updateMember = memberService.getMemberById(memberId);
        return ResponseEntity.status(HttpStatus.OK).body(updateMember);
    }

    // =================================================
    // 下面皆為後台員工使用 (Employee-Only Endpoints)
    // =================================================

    /** 查詢全部會員
     * @param isActive 會員狀態
     *
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<MemberProfileDto>> getMembers(
            // 查詢條件
            @RequestParam(required = false) String memberName,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) Boolean gender,
            @RequestParam(defaultValue = "true") Boolean isActive
    ) {
        // 創建一個 class (dto) 用於接收前端傳來的值
        MemberQueryParamsDto memberQueryParams = new MemberQueryParamsDto();
        memberQueryParams.setMemberName(memberName);
        memberQueryParams.setAddress(address);
        memberQueryParams.setGender(gender);
        memberQueryParams.setIsActive(isActive);
        List<MemberProfileDto> members = memberService.getMembers(memberQueryParams);
        return ResponseEntity.status(HttpStatus.OK).body(members);
    }

    /** 查詢特定會員資料
     * @param memberId 會員 ID
     * @return 會員資料，若不存在則回傳 404
     */
    @GetMapping("/{memberId}")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'ADMIN')")
    public ResponseEntity<MemberProfileDto> getMemberById(@PathVariable Integer memberId) {
        MemberProfileDto member = memberService.getMemberById(memberId);
        if (member != null) {
            return ResponseEntity.status(HttpStatus.OK).body(member);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /** 啟用 or 停權會員
     * @param memberId 會員 ID
     * @param banReasonDto 停權原因
     * @return 成功回傳 200，失敗回傳 400
     */
    @PutMapping("/{memberId}/status")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<MemberProfileDto> updateMemberStatus(
            @PathVariable Integer memberId,
            @RequestBody @Valid BanReasonDto banReasonDto
            ) {
        MemberProfileDto member = memberService.getMemberById(memberId);
        if (member == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        banReasonDto.setMemberId(memberId);

        memberService.toggleMemberStatus(banReasonDto);
        MemberProfileDto banMember = memberService.getMemberById(memberId);
        return ResponseEntity.status(HttpStatus.OK).body(banMember);
    }
}
