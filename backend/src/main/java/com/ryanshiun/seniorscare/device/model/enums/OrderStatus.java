package com.ryanshiun.seniorscare.device.model.enums;

/** 訂單狀態（與 DB CHECK 值一致） */
public enum OrderStatus {
    PENDING,    // 待出貨
    SHIPPED,    // 已出貨
    CANCELLED,  // 已取消
    COMPLETED,  // 已完成
    RETURNED    // 已退貨
}
