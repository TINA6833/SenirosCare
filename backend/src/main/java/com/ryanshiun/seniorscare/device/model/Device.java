package com.ryanshiun.seniorscare.device.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Device {
    private Integer id;                  // 商品主鍵
    private String name;                // 商品名稱
    private String sku;                 // 商品貨號
    private BigDecimal unitPrice;      // 單價
    private Integer inventory;          // 庫存數量
    private String description;         // 商品描述
    private String image;               // 圖片網址或檔名
    private Boolean isOnline;           // 是否上架
    private Integer categoryId;         // 所屬分類 ID
    private Integer createdByEmpId;     // 建立者員工 ID
}
