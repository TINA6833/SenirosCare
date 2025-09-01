package com.ryanshiun.seniorscare.member.dto.member;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
public class MemberProfileDto {
    private int memberId;
    private String memberName;
    private String mainPhone;
    private Boolean gender;
    private Date birthday;
    private String address;
    private String email;
    private String imagePath;
    private Boolean active;
    private String banReason;
    private Date createdAt;
    private LocalDateTime updatedAt;
    private  LocalDateTime loginAt;
}
