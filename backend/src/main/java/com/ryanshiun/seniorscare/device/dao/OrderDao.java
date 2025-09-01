package com.ryanshiun.seniorscare.device.dao;

import com.ryanshiun.seniorscare.device.model.Order;
import com.ryanshiun.seniorscare.device.model.OrderItem;

import java.math.BigDecimal;
import java.util.List;

/**
 * 訂單 DAO：只負責資料庫 CRUD/小步驟，不負責流程編排
 */
public interface OrderDao {

    /** 插入一筆訂單主檔，回傳自動產生的 ID */
    Integer insertOrder(
            String orderNo,
            Integer memberId,
            Integer addressId,
            BigDecimal totalAmount,
            String paymentMethod
    );

    /** 插入一筆訂單明細，回傳影響筆數（應為 1） */
    int insertOrderItem(
            Integer orderId,
            Integer deviceId,
            BigDecimal unitPrice,
            Integer quantity
    );

    /** 根據訂單 ID 查詢單筆（含 items） */
    Order findById(Integer id);

    /** 查詢指定會員的全部訂單（含 items） */
    List<Order> findByMember(Integer memberId);

    /** 查詢全部或指定狀態的訂單（含 items） */
    List<Order> findAll(String status);

    /** 更新訂單狀態 */
    void updateStatus(Integer id, String status);

    /** 更新付款狀態及交易編號 */
    void updatePayment(Integer id, String paymentStatus, String txnNo);

    /** 查詢指定訂單的全部明細 */
    List<OrderItem> findItems(Integer orderId);

    /** 由訂單批次扣庫存（避免負庫存） */
    void updateInStock(Integer orderId);

    /** 更新付款方式（COD / LINE_PAY） */
    void updatePaymentMethod(Integer id, String paymentMethod);

    // 刪掉這筆訂單的所有明細
    void deleteOrderItems(Integer orderId);

    // 刪掉訂單主檔
    int deleteOrder(Integer orderId);

}
