package com.ryanshiun.seniorscare.device.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Cart {
    private Integer cartId;        // 購物車主鍵 (cart_id)
    private Integer memberId;      // 會員 ID，可為 null (member_id)
    private String guestToken;     // 訪客 token，可為 null (guest_token)
    private LocalDateTime createdAt; // 建立時間 (created_at)
}
