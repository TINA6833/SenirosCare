package com.ryanshiun.seniorscare.bus.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 資料傳輸物件（DTO, Data Transfer Object）
 * 用來承載從前端或 API 呼叫傳進來的參數，將它們統一封裝成一個物件，然後再傳給 Service/DAO 處理
 */

@Data
public class BusRequest {

	@NotNull(message = "請提供巴士編號")
	private int busId;
	
	@NotBlank(message = "請提供車行名稱")
	private String carDealership; 
	
	@NotBlank(message = "請提供廠牌")
	private String busBrand;
	
	@NotBlank(message = "請提供型號")
	private String busModel;              //型號
	
	@NotNull(message = "請提供一般座位的數量")
	private int seatCapacity;             //一般座位
	
	@NotNull(message = "請提供輪椅座位的數量")
	private int wheelchairCapacity;       //輪椅座位
	
	@NotBlank(message = "請提供車牌號碼")
	private String licensePlate;          //車牌

	@NotBlank(message = "請提供車輛狀態")
	private String status;                //狀態		
	
}
