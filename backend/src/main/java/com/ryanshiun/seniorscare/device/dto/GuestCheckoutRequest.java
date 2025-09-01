package com.ryanshiun.seniorscare.device.dto;

import lombok.Data;

/** 訪客由購物車結帳請求：不帶總金額，後端會重算 */
@Data
public class GuestCheckoutRequest {
    private String recipient;     // 收件人
    private String phone;         // 電話
    private String addressLine1;  // 完整地址（精簡做法：全部塞這裡）
    private String paymentMethod; // 例如 LINEPAY / COD
}
