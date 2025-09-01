package com.ryanshiun.seniorscare.roomType.model;

import java.time.LocalDateTime;

import lombok.Data;


@Data
public class FavoriteRoom {
    private int memberId;
    private int roomTypeId;
    private LocalDateTime createdAt;
}