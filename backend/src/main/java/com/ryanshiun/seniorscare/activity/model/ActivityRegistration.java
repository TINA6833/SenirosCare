package com.ryanshiun.seniorscare.activity.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ActivityRegistration { // 活動預約表
    private Integer id;
    private Integer activityId;
    private Integer memberId;
    private Integer num;
    private LocalDateTime scheduledAt;
    private String  status; // 預約狀態 (預約審核中、已取消、報名完成)
}
