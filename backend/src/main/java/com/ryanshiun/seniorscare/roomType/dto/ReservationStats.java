package com.ryanshiun.seniorscare.roomType.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
public class ReservationStats {
    private LocalDate date;  // 日或月第一天
    private long count;
}

