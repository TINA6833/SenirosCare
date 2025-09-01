package com.ryanshiun.seniorscare.bus.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * "預約表"， 一個純資料物件（Plain Old Java Object，POJO），與 BusReservation 資料表對應的一筆車輛資料*/


@Data
@NoArgsConstructor
public class BusReservation {

	@JsonProperty("ID")
	private Integer id; // 預約單號

	@JsonProperty("MEMBER_ID")
	private Integer memberId; // 會員標號

	@JsonProperty("BUS_ID")
	private Integer busId; // 巴士編號

	@JsonProperty("START_ZONE_ID")
	private Integer startZone; // 出發位置

	@JsonProperty("END_ZONE_ID")
	private Integer endZone; // 到達位置

	@JsonProperty("CREATED_AT")
	private LocalDateTime createAt; // 訂單建立時間

	@JsonProperty("SCHEDULED_AT")
	private LocalDateTime scheduledAt; // 預約搭車時間

	@JsonProperty("COMPLETED_AT")
	private LocalDateTime completedAt; // 訂單完乘時間

	@JsonProperty("PRICE")
	private int price; // 車費

	@JsonProperty("RESERVATION_STATUS")
	private String reservationStatus; // 訂單狀態

	@JsonProperty("NOTE")
	private String note; // 訂單備註

	@JsonProperty("START_ADDRESS")
	private String startAddress; // 起點地址

	@JsonProperty("END_ADDRESS")
	private String endAddress; // 終點地址

	@JsonProperty("START_LAT")
	private BigDecimal startLat; // 起點緯度

	@JsonProperty("START_LNG")
	private BigDecimal startLng; // 起點經度

	@JsonProperty("END_LAT")
	private BigDecimal endLat; // 終點緯度

	@JsonProperty("END_LNG")
	private BigDecimal endLng; // 終點經度

	@JsonProperty("DISTANCE_METERS")
	private Integer distanceMeters; // 距離(公尺)

}
