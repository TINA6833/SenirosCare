package com.ryanshiun.seniorscare.activity.dto;

import lombok.Data;

@Data
public class ReservationQueryDto {

    private String CategoryName;
    private String activityName;
    private Integer memberId;
}
