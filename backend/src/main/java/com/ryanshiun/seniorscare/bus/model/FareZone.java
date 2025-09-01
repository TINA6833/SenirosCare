package com.ryanshiun.seniorscare.bus.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *"行政區表"，
 * 一個純資料物件（Plain Old Java Object，POJO），與 "rehabus" 資料表對應的一筆車輛資料
 * 用來封裝資料，可以是資料庫的一筆記錄，也可以是前端送來的請求參數
 */



@Data
@NoArgsConstructor
public class FareZone {
	
	@JsonProperty("ZONE_ID")
    private int zoneId;                //區域編號
	
	@JsonProperty("ZONE_NAME")
	private String zoneName;           //區域名稱
	
	@JsonProperty("DESCRIPTION")
	private String description;        //補充說明 
	
}
