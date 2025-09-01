package com.ryanshiun.seniorscare.bus.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FareQuote {

	@NotBlank(message = "請提供地址")
	private String origin;              //出發地點

	@NotBlank(message = "請提供地址")
	private String destination;         //到達地點

	private int distanceMeters;         //總公尺數

	private double distanceKm;          //總公里數

	private int taxiFare;               //計程車車費(原價)

	private int rehabusFare;            //復康巴士車費(原價*1/3) 

}
