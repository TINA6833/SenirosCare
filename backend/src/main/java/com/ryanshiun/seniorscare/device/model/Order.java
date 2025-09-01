// src/main/java/com/ryanshiun/seniorscare/device/model/Order.java
package com.ryanshiun.seniorscare.device.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class Order {
    private Integer id;            // 訂單主鍵 ID（自動遞增）
    private String orderNo;        //訂單編號（顯示給使用者的唯一代碼）
    private Integer memberId;      //訂會員的 ID（對應 member 表的外鍵）
    private Integer addressId;     //收件地址 ID（對應 address 表的外鍵）
    private String status;        //訂單狀態  PENDING：待出貨 SHIPPED：已出貨 CANCELLED：已取消 COMPLETED：已完成 RETURNED：已退貨
    private BigDecimal totalAmount;   //訂單總金額（所有商品金額加總）
    private String paymentMethod;   // 支付方式
    private String paymentStatus;   //付款狀態（PENDING：待付款 PAID：已付款 FAILED：付款失敗 REFUNDED：已退款
    private String transactionNo;   //金流交易編號（由金流廠商回傳，可選）
    private LocalDateTime paidAt;   //實際付款時間（成功付款後由金流系統回傳）
    private LocalDateTime createdAt;
    // 訂單明細清單
    private List<OrderItem> items;
}
