package com.ryanshiun.seniorscare.device.service.Impl;

import com.ryanshiun.seniorscare.device.dao.CartDao;
import com.ryanshiun.seniorscare.device.dao.DeviceDao;
import com.ryanshiun.seniorscare.device.dao.OrderDao;
import com.ryanshiun.seniorscare.device.dto.OrderRequest;
import com.ryanshiun.seniorscare.device.dto.PaymentUpdateRequest;
import com.ryanshiun.seniorscare.device.dto.StatusUpdateRequest;
import com.ryanshiun.seniorscare.device.model.Cart;
import com.ryanshiun.seniorscare.device.model.CartItem;
import com.ryanshiun.seniorscare.device.model.Device;
import com.ryanshiun.seniorscare.device.model.Order;
import com.ryanshiun.seniorscare.device.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.ryanshiun.seniorscare.device.model.enums.OrderStatus;
import com.ryanshiun.seniorscare.device.model.enums.OrderStatusRule;

import com.ryanshiun.seniorscare.device.dto.PaymentMethodUpdateRequest;
import com.ryanshiun.seniorscare.device.model.enums.PaymentMethod;

import com.ryanshiun.seniorscare.device.dto.GuestCheckoutRequest;
/**
 * 下單流程（唯一入口）：
 * 讀購物車 → 算總額 → 建主檔 → 逐筆建明細（用 device 當下單價）→ 扣庫存 → 清購物車 → 成功
 */
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderDao orderDao;   // DAO：只做 CRUD/小步驟
    private final CartDao cartDao;     // 取得購物車/明細、清購物車
    private final DeviceDao deviceDao; // 取得商品單價（若 CartItem 沒帶單價時）

    private boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }

    /** 建立新訂單（由購物車轉訂單） */
    @Override
    @Transactional
    public String createOrder(OrderRequest req) {
        // 1) 取購物車與明細
        Cart cart = cartDao.getCartById(req.getCartId());
        if (cart == null) return "購物車不存在";

        List<CartItem> items = cartDao.getCartItemsByCartId(cart.getCartId());
        if (items == null || items.isEmpty()) return "購物車為空";

        // 2) 計算總金額（直接用 device 表價格）
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem ci : items) {
            BigDecimal unitPrice = deviceDao.fetchUnitPrice(ci.getDeviceId()); // ← 新方法
            if (unitPrice == null) {
                return "商品不存在（ID=" + ci.getDeviceId() + "）";
            }
            total = total.add(unitPrice.multiply(BigDecimal.valueOf(ci.getQuantity())));
        }

        // 3) 插入 orders 主檔
        String orderNo = "ORD" + LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        Integer orderId = orderDao.insertOrder(
                orderNo,
                cart.getMemberId(),
                req.getAddressId(),
                total,
                req.getPaymentMethod()
        );

        // 4) 逐筆插入 order_item（用下單當下的 device 價格）
        for (CartItem ci : items) {
            BigDecimal unitPrice = deviceDao.fetchUnitPrice(ci.getDeviceId());
            orderDao.insertOrderItem(orderId, ci.getDeviceId(), unitPrice, ci.getQuantity());
        }

        // 5) 扣庫存（批次安全扣，失敗會丟例外 → 交易回滾）
        orderDao.updateInStock(orderId);

        // 6) 清購物車（以會員 ID 為準；或可改成 clearCart(cartId)）
        cartDao.removeCartByMemberId(cart.getMemberId());

        return "購買成功";
    }

    /** 根據訂單 ID 查單筆 */
    @Override
    public Order getOrderById(Integer id) {
        return orderDao.findById(id);
    }

    /** 查詢指定會員所有訂單（含 items） */
    @Override
    public List<Order> getOrdersByMember(Integer memberId) {
        return orderDao.findByMember(memberId);
    }

    /** 列出所有訂單（可帶 status 篩選；含 items） */
    @Override
    public List<Order> listAll(String status) {
        return orderDao.findAll(status);
    }

    // 更新訂單狀態（透過規則驗證轉換合法性）
    @Override
    public void updateStatus(Integer id, StatusUpdateRequest req) {
        var order = orderDao.findById(id);
        if (order == null) throw new IllegalArgumentException("訂單不存在");

        // 將 DB/請求的字串轉 enum
        OrderStatus from = OrderStatus.valueOf(order.getStatus());
        OrderStatus to   = OrderStatus.valueOf(req.getStatus());

        // 檢查是否允許由 from → to
        if (!OrderStatusRule.canTransit(from, to)) {
            throw new IllegalStateException("狀態不可由 " + from + " → " + to);
        }

        // 通過才更新
        orderDao.updateStatus(id, to.name());
    }

    // 更新付款狀態（透過規則驗證；PAID 時 DAO 端負責寫 paid_at）
    @Override
    public void updatePayment(Integer id, PaymentUpdateRequest req) {
        var order = orderDao.findById(id);
        if (order == null) throw new IllegalArgumentException("訂單不存在");

        // 規則：PENDING -> PAID/FAILED；PAID -> REFUNDED
        var from = com.ryanshiun.seniorscare.device.model.enums.PaymentStatus.valueOf(order.getPaymentStatus());
        var to   = com.ryanshiun.seniorscare.device.model.enums.PaymentStatus.valueOf(req.getPaymentStatus());
        if (!com.ryanshiun.seniorscare.device.model.enums.PaymentStatusRule.canTransit(from, to)) {
            throw new IllegalStateException("付款狀態不可由 " + from + " → " + to);
        }

        orderDao.updatePayment(id, to.name(), req.getTransactionNo()); // PAID 時會補 paid_at
    }

    @Override
    public void updatePaymentMethod(Integer id, PaymentMethodUpdateRequest req) {
        var order = orderDao.findById(id);
        if (order == null) throw new IllegalArgumentException("訂單不存在");

        // 只允許：訂單狀態 PENDING 且 付款狀態 PENDING
        if (!"PENDING".equals(order.getStatus())) {
            throw new IllegalStateException("僅在訂單狀態為 PENDING（待出貨）時允許變更付款方式");
        }
        if (!"PENDING".equals(order.getPaymentStatus())) {
            throw new IllegalStateException("僅在付款狀態為 PENDING（尚未付款）時允許變更付款方式");
        }

        // 轉成 enum 驗證值（再次保險）
        PaymentMethod.valueOf(req.getPaymentMethod());

        // 更新 DB
        orderDao.updatePaymentMethod(id, req.getPaymentMethod());
    }

    //刪除訂單
    @Override
    @org.springframework.transaction.annotation.Transactional
    public void delete(Integer id) {
        // 先刪明細再刪主檔，避免外鍵限制
        orderDao.deleteOrderItems(id);
        orderDao.deleteOrder(id);
    }



}
