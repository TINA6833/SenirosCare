
package com.ryanshiun.seniorscare.device.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 返回給前端的訂單明細資料
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {
    private Integer deviceId; //商品 ID，對應 Device 表的主鍵
    private String deviceName;   // 商品名稱，如需顯示可在 DAO 層進行 JOIN 查詢
    private BigDecimal unitPrice; //商品單價，以 BigDecimal 精準表示
    private Integer quantity;  //購買數量
    private BigDecimal subtotal; // 小計，等於 unitPrice × quantity
}
