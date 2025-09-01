package com.ryanshiun.seniorscare.activity.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReservationResponse {
    private Integer activityId;
    private Integer memberId;
    private String activityName;
    private String activityCategory;
    private String memberName;
    private String phone;
    private Integer people;
    private LocalDateTime scheduledAt;
    private String status;
}
