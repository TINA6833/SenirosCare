package com.ryanshiun.seniorscare.device.dao.Impl.mapper;

import com.ryanshiun.seniorscare.device.model.Order;
import com.ryanshiun.seniorscare.device.model.OrderItem;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;

/**
 * 將 orders 表的一列映射為 Order 物件（不含 items，DAO 再補）
 */
public class OrderRowMapper implements RowMapper<Order> {
    @Override
    public Order mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Order(
                rs.getInt("id"),
                rs.getString("order_no"),
                rs.getInt("member_id"),
                rs.getInt("address_id"),
                rs.getString("status"),
                rs.getBigDecimal("total_amount"),
                rs.getString("payment_method"),
                rs.getString("payment_status"),
                rs.getString("transaction_no"),
                rs.getTimestamp("paid_at") == null
                        ? null
                        : rs.getTimestamp("paid_at").toLocalDateTime(),
                rs.getTimestamp("created_at").toLocalDateTime(),
                Collections.<OrderItem>emptyList()
        );
    }
}
