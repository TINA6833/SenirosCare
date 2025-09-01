package com.ryanshiun.seniorscare.device.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 更新付款方式的請求 DTO（只允許 COD 或 LINE_PAY） */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethodUpdateRequest {

    @NotBlank(message = "paymentMethod 不可為空")
    @Pattern(regexp = "COD|LINE_PAY", message = "paymentMethod 僅允許 COD 或 LINE_PAY")
    private String paymentMethod;
}
