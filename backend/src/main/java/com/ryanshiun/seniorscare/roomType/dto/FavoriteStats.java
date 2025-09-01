package com.ryanshiun.seniorscare.roomType.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteStats {
    private int roomTypeId;
    private String roomTypeName;
    private long favorites;
}

