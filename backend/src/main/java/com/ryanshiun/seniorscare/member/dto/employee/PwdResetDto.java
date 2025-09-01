package com.ryanshiun.seniorscare.member.dto.employee;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PwdResetDto {
    @NotBlank
    String code;

    @NotBlank
    String newPwd;
}
