package com.ryanshiun.seniorscare.device.model.enums;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/** 訂單狀態轉移規則（集中管理，杜絕 if/else 散落） */
public final class OrderStatusRule {

    // 定義「從某狀態」可以合法轉到的下一個狀態集合
    private static final Map<OrderStatus, Set<OrderStatus>> NEXT = Map.of(
            OrderStatus.PENDING,   EnumSet.of(OrderStatus.SHIPPED, OrderStatus.CANCELLED),
            OrderStatus.SHIPPED,   EnumSet.of(OrderStatus.COMPLETED, OrderStatus.RETURNED),
            OrderStatus.COMPLETED, EnumSet.noneOf(OrderStatus.class),
            OrderStatus.CANCELLED, EnumSet.noneOf(OrderStatus.class),
            OrderStatus.RETURNED,  EnumSet.noneOf(OrderStatus.class)
    );

    /** 檢查是否允許由 from → to */
    public static boolean canTransit(OrderStatus from, OrderStatus to) {
        return NEXT.getOrDefault(from, Set.of()).contains(to);
    }

    private OrderStatusRule() {}
}
