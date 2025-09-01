package com.ryanshiun.seniorscare.member.model.employee;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EmpLog {
    private Long id;
    @JsonProperty("emp_name")
    private String empName;
    private String action;
    @JsonProperty("target_name")
    private String targetName;
    private LocalDateTime createdAt;
}
