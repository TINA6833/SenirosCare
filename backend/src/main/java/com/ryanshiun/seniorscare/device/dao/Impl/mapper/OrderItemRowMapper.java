
package com.ryanshiun.seniorscare.device.dao.Impl.mapper;

import com.ryanshiun.seniorscare.device.model.OrderItem;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 把 order_item 表的每行映射到 OrderItem 對象
 */
public class OrderItemRowMapper implements RowMapper<OrderItem> {

    /**
     * 將 ResultSet 當前行資料轉換為 OrderItem
     *
     * @param rs     查詢結果集
     * @param rowNum 當前行號
     * @return 對應的 OrderItem 物件
     * @throws SQLException 若讀取欄位時發生錯誤
     */
    @Override
    public OrderItem mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new OrderItem(
                rs.getInt("order_id"), // 取得 order_item 表的外鍵 order_id，對應 Order 的 ID
                rs.getInt("device_id"), // 取得商品 ID，對應 Device 的主鍵
                rs.getBigDecimal("unit_price"),  // 取得單價，使用 BigDecimal 精準表示金額
                rs.getInt("quantity"), // 取得數量
                rs.getString("name")
        );
    }
}
