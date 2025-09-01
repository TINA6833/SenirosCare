package com.ryanshiun.seniorscare.bus.model;

import java.math.BigDecimal;


import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * "試算票價表"，
 * 一個純資料物件（Plain Old Java Object，POJO），與 rehabus 資料表對應的一筆車輛資料
 * 用來封裝資料，可以是資料庫的一筆記錄，也可以是前端送來的請求參數
 */

@Data
@NoArgsConstructor
public class FarePrice {

	@JsonProperty("PRICE_ID")
	private int priceId;                     //價格對應編號
	
	@JsonProperty("FROM_ZONE_ID")
	private int fromZone;                    //起始地點
	
	@JsonProperty("TO_ZONE_ID")
	private int toZone;                      //目的地點
	
	@JsonProperty("PRICE")
	private int price;                       //車費
	
	@JsonProperty("ACTUAL_DISTANCE_KM")
	private BigDecimal actualDistanceKm;     //公里數(進階) 
	
	@JsonProperty("PRICE_STATUS")
	private String priceStatus;              //價格狀態
	
	
}
