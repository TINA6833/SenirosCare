package com.ryanshiun.seniorscare.device.dto;

import lombok.Data;


// CartItemResponse - 回傳購物車明細
@Data
public class CartItemResponse {
    private Integer deviceId; // 商品 ID
    private Integer quantity; // 數量
}
