package com.ryanshiun.seniorscare.member.model.employee;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
public class Employee {
    @JsonProperty("emp_id")
    private Integer empId;
    @JsonProperty("emp_name")
    private String empName;
    private String password;
    private String email;
    @JsonProperty("is_active")
    private boolean isActive;
    @JsonProperty("image_path")
    private String imagePath;
    private String describe;
    @JsonProperty("created_at")
    private Date createdAt;
    @JsonProperty("updated_at")
    private  LocalDateTime updatedAt;
}
