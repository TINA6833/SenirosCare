package com.ryanshiun.seniorscare.activity.dto;

import lombok.Data;

@Data
public class ReservationUpdateDto {

    private Integer activityId;
    private Integer memberId;
    private  String status; // "已取消" or "報名成功"
}
