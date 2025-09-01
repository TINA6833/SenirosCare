package com.ryanshiun.seniorscare.roomType.model;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class RoomComment {
    private int id;
    private int memberId;
    private int roomTypeId;
    private String content;
    private boolean isApproved;
    private String adminReply;
    private LocalDateTime createdAt;
}
