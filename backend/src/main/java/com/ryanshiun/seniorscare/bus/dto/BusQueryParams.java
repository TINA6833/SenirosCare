package com.ryanshiun.seniorscare.bus.dto;

import lombok.Data;

/**定義模糊(區域)查詢的內容*/

@Data
public class BusQueryParams {
	
	//區域查詢一般座位數量(最少)
	private Integer minSeats;
	
	//區域查詢輪椅座位數量
	private Integer minWheels;



}
