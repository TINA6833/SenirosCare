package com.ryanshiun.seniorscare.member.dto.member;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BanReasonDto {

    private Integer memberId;
    @NotNull
    private Boolean isActive;
    @NotNull
    private String banReason;
}
