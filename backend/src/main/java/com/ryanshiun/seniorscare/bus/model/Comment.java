package com.ryanshiun.seniorscare.bus.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * "會員評論表"，
 * 一個純資料物件（Plain Old Java Object，POJO），與 Comment 資料表對應的一筆車輛資料
 * 用來封裝資料，可以是資料庫的一筆記錄，也可以是前端送來的請求參數
 */

@Data
@NoArgsConstructor
public class Comment {
	
	@JsonProperty("COMMENT_ID")
	private int commentId;              //回覆編號
	
	@JsonProperty("RESERVATION_ID")
	private int reservationId;          //預約單號
	
	@JsonProperty("MEMBER_ID")
	private int memberId;               //會員編號 
	
	@JsonProperty("RATING")
	private int rating;                  //評分，1-5
	
	@JsonProperty("COMMENT_TEXT")
	private String commentText;          //評論內容
	
	@JsonProperty("CREATE_AT")
	private LocalDateTime createAt;      //評論建立時間
	
}
