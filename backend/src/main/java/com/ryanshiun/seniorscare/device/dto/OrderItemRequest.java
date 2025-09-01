
package com.ryanshiun.seniorscare.device.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 前端傳來一筆訂單明細項：哪件商品、多少數量
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemRequest {
    private Integer deviceId;  // 商品 ID
    private Integer quantity;  // 数量
}
