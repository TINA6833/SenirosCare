package com.ryanshiun.seniorscare.member.dto.member;

import lombok.Data;

@Data
public class MemberQueryParamsDto {

    private String memberName; // 模糊查詢會員名稱
    private String address;
    private Boolean gender;
    private Boolean isActive; // 停權名單 (1=normal, 0=ban)
}
