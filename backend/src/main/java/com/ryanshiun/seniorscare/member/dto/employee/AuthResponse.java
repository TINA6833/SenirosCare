package com.ryanshiun.seniorscare.member.dto.employee;

import lombok.Data;

@Data
public class AuthResponse {
    private String token;
    private EmployeeProfileDto employeeProfileDto;

    public AuthResponse(String token, EmployeeProfileDto employeeProfileDto) {
        this.token = token;
        this.employeeProfileDto = employeeProfileDto;
    }
}
