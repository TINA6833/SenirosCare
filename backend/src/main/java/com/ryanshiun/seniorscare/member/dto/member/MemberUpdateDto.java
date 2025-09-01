package com.ryanshiun.seniorscare.member.dto.member;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;

@Data
public class MemberUpdateDto {
    private Integer memberId;
    @NotNull
    private String memberName;
    private String mainPhone;
    private boolean gender;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Date birthday;
    private String address;
    private String email;
}
