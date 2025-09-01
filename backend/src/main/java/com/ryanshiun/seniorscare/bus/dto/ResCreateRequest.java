package com.ryanshiun.seniorscare.bus.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * 資料傳輸物件（DTO, Data Transfer Object） 用來承載從前端或 API 呼叫傳進來的參數，將它們統一封裝成一個物件，然後再傳給
 * Service/DAO 處理
 */

@Data
public class ResCreateRequest {

	private int memberId;

	@NotNull(message = "請提供巴士編號")
	@Positive
	private int busId;

	@Nullable
	private int startZone;

	@Nullable
	private int endZone;

	@NotNull(message = "請提供要預約的時間")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd['T'][' ']HH:mm[:ss]")
	private LocalDateTime scheduledAt;

	private String note;

	@NotBlank(message = "請提供出發地址")
	private String startAddress;

	@NotBlank(message = "請提供目的地址")
	private String endAddress;

	private BigDecimal startLat;

	private BigDecimal startLng;

	private BigDecimal endLat;

	private BigDecimal endLng;

}
