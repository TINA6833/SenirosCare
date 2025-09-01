package com.ryanshiun.seniorscare.device.dto;

import lombok.Data;


 //前端傳入新增 / 修改地址資料的 DTO

@Data
public class AddressRequest {

    private Integer memberId;      // 會員 ID
    private String recipient;      // 收件人姓名
    private String phone;          // 收件人電話
    private String postalCode;     // 郵遞區號
    private String addressLine1;   // 地址第 1 行
    private String addressLine2;   // 地址第 2 行 (可為 null)
    private Boolean isDefault;     // 是否設為預設地址
}
