package com.ryanshiun.seniorscare.device.rowmapper;

import com.ryanshiun.seniorscare.device.model.Order;
import com.ryanshiun.seniorscare.device.model.OrderItem;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;

/**
 * 訂單映射器  將 JDBC 查詢結果中的欄位對應並封裝為 Order 實體物件
 */
public class OrderRowMapper implements RowMapper<Order> {

    /**
     * 將當前行的 ResultSet 轉換為 Order 物件
     *
     * @param rs     包含查詢結果的 ResultSet
     * @param rowNum 當前行號（從 0 開始）
     * @return 封裝了對應欄位值的 Order 物件
     * @throws SQLException 若讀取欄位失敗則拋出
     */
    @Override
    public Order mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Order(
                rs.getInt("id"),    // 訂單主鍵 ID
                rs.getString("order_no"),  // 訂單編號
                rs.getInt("member_id"),  // 下訂會員 ID
                rs.getInt("address_id"),  // 收件地址 ID
                rs.getString("status"),   // 訂單狀態
                rs.getBigDecimal("total_amount"),  // 訂單總金額
                rs.getString("payment_method"),  // 支付方式
                rs.getString("payment_status"),   // 付款狀態
                rs.getString("transaction_no"), // 金流交易編號

                // 付款時間：若資料庫該欄為 null，則轉為 null；否則轉為 LocalDateTime
                rs.getTimestamp("paid_at") == null
                        ? null
                        : rs.getTimestamp("paid_at").toLocalDateTime(),

                // 建立時間：預期不為 null，直接轉為 LocalDateTime
                rs.getTimestamp("created_at").toLocalDateTime(),
                // 訂單明細清單（OrderItem），由 Service/DAO 層後續查出後填充，此處先用空清單佔位
                Collections.<OrderItem>emptyList()
        );
    }
}
