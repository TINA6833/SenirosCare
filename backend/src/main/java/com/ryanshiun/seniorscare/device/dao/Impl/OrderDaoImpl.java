package com.ryanshiun.seniorscare.device.dao.Impl;

import com.ryanshiun.seniorscare.device.dao.OrderDao;
import com.ryanshiun.seniorscare.device.dao.Impl.mapper.OrderItemRowMapper;
import com.ryanshiun.seniorscare.device.dao.Impl.mapper.OrderRowMapper;
import com.ryanshiun.seniorscare.device.model.Order;
import com.ryanshiun.seniorscare.device.model.OrderItem;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

/**
 * 訂單 DAO 實作：僅進行 SQL 存取，不做流程控制
 */
@Repository
@RequiredArgsConstructor
public class OrderDaoImpl implements OrderDao {

    private final NamedParameterJdbcTemplate jdbc;

    /** 插入 orders 主檔並回傳自增 ID */
    @Override
    public Integer insertOrder(String orderNo, Integer memberId, Integer addressId,
                               BigDecimal totalAmount, String paymentMethod) {
        String sql = """
            INSERT INTO orders
              (order_no, member_id, address_id, status, total_amount,
               payment_method, payment_status, created_at)
            VALUES
              (:orderNo, :memberId, :addressId, 'PENDING', :totalAmount,
               :paymentMethod, 'PENDING', :createdAt)
            """;
        MapSqlParameterSource p = new MapSqlParameterSource()
                .addValue("orderNo", orderNo)
                .addValue("memberId", memberId)
                .addValue("addressId", addressId)
                .addValue("totalAmount", totalAmount)
                .addValue("paymentMethod", paymentMethod)
                .addValue("createdAt", LocalDateTime.now());
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(sql, p, kh);
        return kh.getKey().intValue();
    }

    /** 插入 order_item 明細 */
    @Override
    public int insertOrderItem(Integer orderId, Integer deviceId,
                               BigDecimal unitPrice, Integer quantity) {
        String sql = """
            INSERT INTO order_item (order_id, device_id, unit_price, quantity)
            VALUES (:orderId, :deviceId, :unitPrice, :quantity)
            """;
        return jdbc.update(sql, new MapSqlParameterSource()
                .addValue("orderId", orderId)
                .addValue("deviceId", deviceId)
                .addValue("unitPrice", unitPrice)
                .addValue("quantity", quantity));
    }

    /** 查單筆（含 items） */
    @Override
    public Order findById(Integer id) {
        try {
            String sql = "SELECT * FROM orders WHERE id = :id";
            Order o = jdbc.queryForObject(sql, Map.of("id", id), new OrderRowMapper());
            o.setItems(findItems(o.getId()));
            return o;
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /** 查會員全部（含 items） */
    @Override
    public List<Order> findByMember(Integer memberId) {
        String sql = """
            SELECT * FROM orders
            WHERE member_id = :m
            ORDER BY created_at DESC
            """;
        List<Order> orders = jdbc.query(sql, Map.of("m", memberId), new OrderRowMapper());
        for (Order o : orders) {
            o.setItems(findItems(o.getId()));
        }
        return orders;
    }

    /** 查全部或指定狀態（含 items） */
    @Override
    public List<Order> findAll(String status) {
        String sqlAll = "SELECT * FROM orders ORDER BY created_at DESC";
        String sqlByStatus = "SELECT * FROM orders WHERE status = :s ORDER BY created_at DESC";

        List<Order> orders = (status == null)
                ? jdbc.query(sqlAll, Map.of(), new OrderRowMapper())
                : jdbc.query(sqlByStatus, Map.of("s", status), new OrderRowMapper());

        for (Order o : orders) {
            o.setItems(findItems(o.getId()));
        }
        return orders;
    }

    /** 更新訂單狀態 */
    @Override
    public void updateStatus(Integer id, String status) {
        String sql = "UPDATE orders SET status = :s WHERE id = :id";
        jdbc.update(sql, Map.of("s", status, "id", id));
    }

    /** 更新付款狀態與交易編號 */
    @Override
    public void updatePayment(Integer id, String paymentStatus, String txnNo) {
        String sql = """
        UPDATE orders
           SET payment_status = :ps,
               transaction_no = :txn,
               paid_at = CASE WHEN :ps = 'PAID' THEN SYSDATETIME() ELSE paid_at END
         WHERE id = :id
    """;
        jdbc.update(sql, java.util.Map.of("ps", paymentStatus, "txn", txnNo, "id", id));
    }

    /** 查訂單明細 */
    @Override
    public List<OrderItem> findItems(Integer orderId) {
        String sql = """
                SELECT *
                FROM order_item AS oi
                LEFT JOIN device AS d ON d.id = oi.device_id
                WHERE oi.order_id = :oid
                """;
        return jdbc.query(sql, Map.of("oid", orderId), new OrderItemRowMapper());
    }

    /**
     * 由訂單批次扣庫存（避免負庫存）
     * 流程：
     * 1) 先檢查是否有任何一項會變成負庫存，若是則丟例外（Service 交易回滾）
     * 2) 用 JOIN 一次批次扣（WITH (UPDLOCK, ROWLOCK) 降低並發超賣）
     * 3) 驗證受影響筆數是否等於明細筆數
     */
    @Override
    public void updateInStock(Integer orderId) {
        // 1) 檢查是否有庫存不足
        Integer bad = jdbc.queryForObject("""
            SELECT COUNT(*) 
            FROM device d 
            JOIN order_item oi ON oi.device_id = d.id
            WHERE oi.order_id = :oid AND d.inventory < oi.quantity
        """, Map.of("oid", orderId), Integer.class);
        if (bad != null && bad > 0) {
            throw new IllegalStateException("庫存不足，無法扣庫存");
        }

        // 2) 批次安全扣庫存
        int updated = jdbc.update("""
            UPDATE d
            SET d.inventory = d.inventory - oi.quantity
            FROM device d WITH (UPDLOCK, ROWLOCK)
            JOIN order_item oi ON oi.device_id = d.id
            WHERE oi.order_id = :oid AND d.inventory >= oi.quantity
        """, Map.of("oid", orderId));

        // 3) 驗證筆數
        Integer expected = jdbc.queryForObject(
                "SELECT COUNT(*) FROM order_item WHERE order_id = :oid",
                Map.of("oid", orderId),
                Integer.class
        );
        if (expected == null) expected = 0;
        if (updated != expected) {
            throw new IllegalStateException("扣庫存筆數不一致，已回滾");
        }
    }

    @Override
    public void updatePaymentMethod(Integer id, String paymentMethod) {
        String sql = "UPDATE orders SET payment_method = :m WHERE id = :id AND is_deleted = 0";
        jdbc.update(sql, java.util.Map.of("m", paymentMethod, "id", id));
    }

    // 刪掉這筆訂單的所有明細
    @Override
    public void deleteOrderItems(Integer orderId) {
        String sql = "DELETE FROM order_item WHERE order_id = :id";
        jdbc.update(sql, java.util.Map.of("id", orderId));
    }

    // 刪掉訂單主檔
    @Override
    public int deleteOrder(Integer orderId) {
        String sql = "DELETE FROM orders WHERE id = :id";
        return jdbc.update(sql, java.util.Map.of("id", orderId));
    }


}
