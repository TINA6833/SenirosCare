package com.ryanshiun.seniorscare.member.dto.employee;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EmployeeUpdateDto {
    @NotNull
    private String empName;
    private  String email;
    private String imagePath;
    private String describe;
}
