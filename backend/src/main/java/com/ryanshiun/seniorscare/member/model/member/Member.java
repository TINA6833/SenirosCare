package com.ryanshiun.seniorscare.member.model.member;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
public class Member {
    @JsonProperty("member_id")
    private int memberId;

    @JsonProperty("line_user_id")
    private String lineUserId;

    @JsonProperty("member_name")
    private String memberName;

    @JsonProperty("main_phone")
    private String mainPhone;

    private Boolean gender;
    private Date birthday;
    private String address;
    private String email;
    @JsonProperty("image_path")
    private String imagePath;

    @JsonProperty("is_active")
    private Boolean active;

    @JsonProperty("ban_reason")
    private String banReason;

    @JsonProperty("created_at")
    private Date createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @JsonProperty("login_at")
    private LocalDateTime loginAt;
}
