package com.ryanshiun.seniorscare.member.dto.employee;

import com.ryanshiun.seniorscare.member.model.employee.EmpRole;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class EmployeeProfileDto {
    private int empId;
    private String empName;
    private  String email;
    private Boolean isActive;
    private List<EmpRole> roles = new ArrayList<>();
    private String imagePath;
    private String describe;
    private Date createdAt;
    private LocalDateTime updatedAt;
}
