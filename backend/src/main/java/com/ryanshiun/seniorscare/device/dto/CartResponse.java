package com.ryanshiun.seniorscare.device.dto;

import lombok.Data;
import java.util.List;


//CartResponse - 回傳購物車主檔與明細
@Data
public class CartResponse {
    private Integer cartId;               // 購物車 ID
    private Integer memberId;             // 會員 ID
    private String guestToken;            // 訪客 token
    private List<CartItemResponse> items; // 購物車明細列表
}
