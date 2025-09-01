package com.ryanshiun.seniorscare.device.dto;

import lombok.Data;

import java.math.BigDecimal;


 // DeviceRequest - 用於新增或更新設備的請求資料

@Data
public class DeviceRequest {
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
