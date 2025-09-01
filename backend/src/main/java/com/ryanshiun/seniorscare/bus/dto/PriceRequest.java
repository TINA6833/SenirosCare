package com.ryanshiun.seniorscare.bus.dto;


import java.math.BigDecimal;



import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 資料傳輸物件（DTO, Data Transfer Object）
 * 用來承載從前端或 API 呼叫傳進來的參數，將它們統一封裝成一個物件，然後再傳給 Service/DAO 處理
 */


@Data
public class PriceRequest {

	@NotNull(message = "請提供價格編號")
	private int priceId;

	@NotNull(message = "請提供起始地點")
	private int fromZone;

	@NotNull(message = "請提供到達地點")
	private int toZone;
	
	@NotNull(message = "請輸入金額") 
	private Integer price; 
	
	private BigDecimal actualDistanceKm;     

	private String priceStatus;               // 預設 "Active"

}
