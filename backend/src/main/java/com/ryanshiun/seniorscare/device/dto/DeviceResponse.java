package com.ryanshiun.seniorscare.device.dto;

import lombok.Data;

import java.math.BigDecimal;


 //DeviceResponse - 回傳設備資料給前端的格式

@Data
public class DeviceResponse {
    private Integer id;
    private String name;
    private String sku;
    private BigDecimal unitPrice;
    private Integer inventory;
    private String description;
    private String image;
    private Boolean isOnline;
    private Integer categoryId;
    private Integer createdByEmpId;
}
