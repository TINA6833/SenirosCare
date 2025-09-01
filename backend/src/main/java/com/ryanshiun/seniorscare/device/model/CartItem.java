package com.ryanshiun.seniorscare.device.model;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * CartItem - 購物車明細
 */
@Data
public class CartItem {
    private Integer cartId;        // 購物車 ID (cart_id)
    private Integer deviceId;      // 商品 ID (device_id)
    private Integer quantity;      // 數量 (quantity)
    private LocalDateTime addedAt; // 加入時間 (added_at)
}
