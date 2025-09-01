package com.ryanshiun.seniorscare.member.dto.employee;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EmployeeLoginDto {
    @NotNull
    private Integer empId;

    @NotBlank
    private String password;
}
