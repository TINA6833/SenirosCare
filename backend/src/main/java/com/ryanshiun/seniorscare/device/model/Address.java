package com.ryanshiun.seniorscare.device.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class Address {
    private Integer id;            // 地址主鍵
    private Integer memberId;      // 會員 ID
    private String recipient;      // 收件人姓名
    private String phone;          // 收件人電話
    private String postalCode;     // 郵遞區號
    private String addressLine1;   // 地址第 1 行
    private String addressLine2;   // 地址第 2 行
    private Boolean isDefault;     // 是否為預設地址
    private LocalDateTime createdAt; // 建立時間
}
