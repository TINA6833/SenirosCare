package com.ryanshiun.seniorscare.device.dto;

import lombok.Data;


//CartRequest - 新增/修改購物車項目請求
@Data
public class CartRequest {
    private Integer deviceId;   // 要加入/修改的商品 ID
    private Integer quantity;   // 數量
}
