package com.ryanshiun.seniorscare.device.model.enums;

public enum PaymentStatus {
    PENDING,   // 尚未付款 / 未確認
    PAID,      // 已付款
    FAILED,    // 付款失敗
    REFUNDED   // 已退款
}
