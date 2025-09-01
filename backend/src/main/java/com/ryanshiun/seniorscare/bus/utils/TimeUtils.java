package com.ryanshiun.seniorscare.bus.utils;

import java.time.LocalDateTime;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

public class TimeUtils {
	
	

	public TimeUtils() {
	}

	public static LocalDateTime taipeiNowMinute() {
		
		return ZonedDateTime.now(ZoneId.of("Asia/Taipei"))
			   .truncatedTo(ChronoUnit.MINUTES)
			   .toLocalDateTime();
	}
	
	// 將任意 LocalDateTime 截到「分鐘」（若為 null 直接回 null）
	public static LocalDateTime truncateToMinute(LocalDateTime datetime) {
		return datetime == null ? null: datetime.truncatedTo(ChronoUnit.MINUTES); 
	}
	
	
	
	/* 
     * ZoneId.of("Asia/Taipei")->取出 IANA 時區代碼為 Asia/Taipei 的時區物件（台灣時區）
     * 
	 * */
}
