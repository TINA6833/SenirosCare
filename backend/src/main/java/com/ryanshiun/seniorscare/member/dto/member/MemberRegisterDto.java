package com.ryanshiun.seniorscare.member.dto.member;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MemberRegisterDto {

    @NotNull
    private String lineUserId;
    @NotNull
    private String memberName;

    private String email;
    private String imagePath;
}
