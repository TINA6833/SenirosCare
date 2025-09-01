
package com.ryanshiun.seniorscare.device.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * OrderResponse - 返回給前端的完整訂單資料
 * 包含訂單主鍵、訂單編號、會員及地址資訊、狀態、金流狀態與交易編號、付款及建立時間，
 * 以及訂單明細列表，便於前端頁面顯示訂單詳情。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Integer id;                //訂單主鍵 ID，自動生成
    private String orderNo;            // 訂單編號，供前端顯示及唯一識別
    private Integer memberId;         //下訂會員 ID，對應會員表的主鍵
    private Integer addressId;        //收件地址 ID，對應地址表的主鍵
    private String status;            // 訂單狀態，例如 PENDING（待出貨）、SHIPPED（已出貨）、COMPLETED（已完成）等
    private BigDecimal totalAmount;   // 訂單總金額
    private String paymentMethod;     //付款方式，如 CreditCard、LINEPAY、ATM、CASH 等
    private String paymentStatus;    // 付款狀態，例如 PENDING（待付款）、PAID（已付款）等
    private String transactionNo;    // 金流系統回傳的交易編號，用於對帳
    private LocalDateTime paidAt;    //實際付款時間，可為 null
    private LocalDateTime createdAt;  // 訂單建立時間
    private List<OrderItemResponse> items; //訂單明細列表，包含每項商品的單價、數量及小計
}
