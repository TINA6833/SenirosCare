
package com.ryanshiun.seniorscare.device.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 後台更新訂單狀態請求 DTO
 *
 * 此物件用於後台 API，攜帶新的訂單狀態，
 * 便於系統記錄、訂單流程管理與客服查詢。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatusUpdateRequest {

    /**
     * 訂單狀態
     * 可選值：
     * <ul>
     *   <li>"PENDING"：待處理</li>
     *   <li>"SHIPPED"：已出貨</li>
     *   <li>"CANCELLED"：已取消</li>
     *   <li>"RETURNED"：已退貨</li>
     *   <li>"COMPLETED"：完成</li>
     * </ul>
     */
    private String status;
}
