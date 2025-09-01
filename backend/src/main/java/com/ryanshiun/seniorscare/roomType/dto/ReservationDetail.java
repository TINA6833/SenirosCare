package com.ryanshiun.seniorscare.roomType.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.Data;

@Data
public class ReservationDetail {
    private int reservationId;

    // 申請人/會員
    private Integer memberId;        // 可能為 null
    private String memberName;       // 來自 member.name（或你實際欄位）
    private String applicantName;    // 非會員可填
    private String mainPhone;

    // 房型
    private Integer roomTypeId;      // 可能為 null
    private String roomTypeName;

    // 預約需求
    private int partySize;
    private LocalDate preferredDate;
    private LocalTime timeFrom;
    private LocalTime timeTo;
    private int status;
    private String note;

    // 系統欄
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;
}
