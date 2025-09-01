package com.ryanshiun.seniorscare.device.model.enums;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/** 付款狀態轉移規則（集中管理） */
public final class PaymentStatusRule {

    // PENDING -> PAID | FAILED
    // PAID -> REFUNDED
    private static final Map<PaymentStatus, Set<PaymentStatus>> NEXT = Map.of(
            PaymentStatus.PENDING, EnumSet.of(PaymentStatus.PAID, PaymentStatus.FAILED),
            PaymentStatus.PAID,    EnumSet.of(PaymentStatus.REFUNDED),
            PaymentStatus.FAILED,  EnumSet.noneOf(PaymentStatus.class),
            PaymentStatus.REFUNDED,EnumSet.noneOf(PaymentStatus.class)
    );

    public static boolean canTransit(PaymentStatus from, PaymentStatus to) {
        return NEXT.getOrDefault(from, Set.of()).contains(to);
    }

    private PaymentStatusRule() {}
}
