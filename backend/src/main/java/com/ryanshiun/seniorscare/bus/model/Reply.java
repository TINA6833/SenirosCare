package com.ryanshiun.seniorscare.bus.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * "後台回覆評論表"，
 * 一個純資料物件（Plain Old Java Object，POJO），與 rehabus 資料表對應的一筆車輛資料
 * 用來封裝資料，可以是資料庫的一筆記錄，也可以是前端送來的請求參數
 */

@Data
@NoArgsConstructor
public class Reply {
	
	@JsonProperty("REPLY_ID")
	private int replyId;               //後台回覆編號
	
	@JsonProperty("COMMENT_ID")
	private int commentId;             //會員回覆編號
	
	@JsonProperty("MEMBER_ID")
	private int memberId;              //會員編號
	
	@JsonProperty("EMP_ID")
	private int empId;                //員工編號
	
	@JsonProperty("EMP_TEXT")
	private String empText;           //後台回覆內容
	
	@JsonProperty("REPLY_AT")
	private LocalDateTime replyAT;      //後台回覆時間

}
