
package com.ryanshiun.seniorscare.device.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 前端創建訂單請求
 * totalAmount 不由前端傳，而是後端根據 unit_price × quantity 重新計算
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {
    private Integer cartId;             // 購物車 id
    private Integer addressId;          // 收件地址 id
    private String paymentMethod;        // 付款方式
}
