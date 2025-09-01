
package com.ryanshiun.seniorscare.device.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 後台更新訂單付款狀態請求 DTO
 *
 * 此物件用於後台 API，攜帶新的付款狀態及可選的支付平臺交易編號，
 * 便於系統記錄、對帳與客服查詢。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentUpdateRequest {

    private String paymentStatus;   // PENDING/PAID/FAILED/REFUNDED
    private String transactionNo;   // 可為 null
}
