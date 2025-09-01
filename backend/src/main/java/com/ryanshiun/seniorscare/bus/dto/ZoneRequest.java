package com.ryanshiun.seniorscare.bus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 資料傳輸物件（DTO, Data Transfer Object）
 * 用來承載從前端或 API 呼叫傳進來的參數，將它們統一封裝成一個物件，然後再傳給 Service/DAO 處理
 */

@Data
public class ZoneRequest {
	
    @NotNull(message = "請提供行政區編號")	
    private int zoneId;
    
    @NotBlank(message = "請提供行政區名稱")
    private String zoneName;
    
    private String description;

}
