package com.ryanshiun.seniorscare.member.model.employee;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class EmpRole {
    @JsonProperty("role_id")
    private Integer roleId;
    @JsonProperty("role_name")
    private String roleName;
}
