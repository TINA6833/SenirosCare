package com.ryanshiun.seniorscare.bus.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * "復康巴士資料表"，
 * 一個純資料物件（Plain Old Java Object，POJO），與 rehabus 資料表對應的一筆車輛資料
 * 用來封裝資料，可以是資料庫的一筆記錄，也可以是前端送來的請求參數
 */

@Data
@NoArgsConstructor
public class Rehabus {
	
	@JsonProperty("BUS_ID")
	private int busId;                    //車輛流水號
	
	@JsonProperty("CAR_DEALERSHIP")
	private String carDealership;         //車行
	
	@JsonProperty("BUS_BRAND")
	private String busBrand;              //汽車廠牌
	
	@JsonProperty("BUS_MODEL")
	private String busModel;              //型號
	
	@JsonProperty("SEAT_CAPACITY")
	private int seatCapacity;             //一般座位
	
	@JsonProperty("WHEELCHAIR_CAPACITY")
	private int wheelchairCapacity;       //輪椅座位
	
	@JsonProperty("LICENSE_PLATE")
	private String licensePlate;          //車牌號碼
	
	@JsonProperty("STATUS")
	private String status;                //派遣狀態
	

}
