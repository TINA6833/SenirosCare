
package com.ryanshiun.seniorscare.device.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    private Integer orderId; //所屬訂單 ID（對應 orders 表的外鍵）
    private Integer deviceId;   //商品（輔具）ID（對應 device 表的外鍵）
    private BigDecimal unitPrice;  //單價（訂購時的商品價格）
    private Integer quantity;  //購買數量
    private String name;
}
