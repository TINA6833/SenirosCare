package com.ryanshiun.seniorscare.roomType.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import lombok.Data;

@Data //測試成功
	public class Reservation {
	    private int reservationId;
	    private Integer memberId; // 可為 null
	    private String applicantName;
	    private String mainPhone;
	    private Integer roomTypeId; // 可為 null
	    private int partySize;
	    private LocalDate preferredDate;
	    private LocalTime timeFrom;
	    private LocalTime timeTo;
	    private int status; // 0待審,1確認,2取消,3完成,4未到
	    private String note;
	    private LocalDateTime createdAt;
	    private LocalDateTime updatedAt;
	}

