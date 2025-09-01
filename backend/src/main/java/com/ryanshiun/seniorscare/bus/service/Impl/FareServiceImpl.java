package com.ryanshiun.seniorscare.bus.service.Impl;

import org.springframework.stereotype.Component;

import com.ryanshiun.seniorscare.bus.dto.FareQuote;
import com.ryanshiun.seniorscare.bus.fare.FareCalculator;
import com.ryanshiun.seniorscare.bus.service.FareService;
import com.ryanshiun.seniorscare.bus.utils.GoogleMapsClient;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FareServiceImpl implements FareService {

	private final GoogleMapsClient maps;

	public FareQuote quoteByAddresses(String origin, String destination) {

		Integer meters = maps.getDrivingDistanceMeters(origin, destination);
		if (meters == null)
			return null;
		// 取距離失敗（地址無效、API 錯誤等）

		int taxi = FareCalculator.calcTaxiFareByTYMeters(meters);
		// 依照桃園計程車計費算出費用(不含延滯計時)

		int rehabus = FareCalculator.calcRehabusFareFormTaxi(taxi);
		// 復康巴士費用 = 計程車資的 1/3

		FareQuote fareQuote = new FareQuote();
		fareQuote.setOrigin(origin);
		fareQuote.setDestination(destination);

		fareQuote.setDistanceMeters(meters);
		fareQuote.setDistanceKm(Math.round((meters / 1000.0) * 100.0) / 100.0);
		// 把公尺轉公里並四捨五入到小數點兩位

		fareQuote.setTaxiFare(taxi);
		fareQuote.setRehabusFare(rehabus);

		return fareQuote;
	}

}
