package com.ryanshiun.seniorscare.member.model.employee;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PwdReset {
    private Integer id;
    private Integer empId;
    private String code;
    private LocalDateTime expiresAt;
    private Boolean used;
}
