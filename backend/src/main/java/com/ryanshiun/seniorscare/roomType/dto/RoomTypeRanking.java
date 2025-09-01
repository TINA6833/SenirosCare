package com.ryanshiun.seniorscare.roomType.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
public class RoomTypeRanking {
    private int roomTypeId;
    private String roomTypeName;
    private long count;
}