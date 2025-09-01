package com.ryanshiun.seniorscare.roomType.model;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class RoomTypeStatistics {
    private int roomtypeId;
    private int viewCount;
    private int favoriteCount;
    private int commentCount;
    private double averageRating;
    private int hotScore;
    private LocalDateTime latestFavoriteAt;
    private LocalDateTime latestCommentAt;
    private LocalDateTime lastUpdated;
}
