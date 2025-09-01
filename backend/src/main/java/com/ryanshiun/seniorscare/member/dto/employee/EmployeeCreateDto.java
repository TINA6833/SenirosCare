package com.ryanshiun.seniorscare.member.dto.employee;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EmployeeCreateDto {
    @NotNull
    private String empName;

    @NotNull
    private  String email;
    @NotNull
    private String password;
    // Default role ID for employee
    private int roleId = 3;
}
