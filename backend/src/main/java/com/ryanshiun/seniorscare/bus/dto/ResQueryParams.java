package com.ryanshiun.seniorscare.bus.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;

/** 定義模糊(區域)查詢的內容 */

@Data
public class ResQueryParams {

	// 根據會員ID查詢
	private Integer memberId;

	// 根據地區查詢(出發地區)
	private Integer startZone;

	// 根據地區查詢(到達地區)
	private Integer endZone;

	// 根據預約時間查詢(天+時間)
	@DateTimeFormat(pattern = "yyyy-MM-dd['T'][' ']HH:mm[:ss]")
	private LocalDateTime scheduledAt;
	
	// 根據預約時間查詢(天)
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate scheduledDate;


	// 起點地址
	private String startAddress;

	// 終點地址
	private String endAddress;
}
