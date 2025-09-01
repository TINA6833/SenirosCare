package com.ryanshiun.seniorscare.device.controller;

import com.ryanshiun.seniorscare.device.dto.OrderRequest;
import com.ryanshiun.seniorscare.device.dto.PaymentUpdateRequest;
import com.ryanshiun.seniorscare.device.model.Order;
import com.ryanshiun.seniorscare.device.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.ryanshiun.seniorscare.device.dto.StatusUpdateRequest;

import com.ryanshiun.seniorscare.device.dto.GuestCheckoutRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


/**
 * 訂單 API
 * - GET /api/orders?memberId=1   查某會員全部訂單
 * - GET /api/orders               查全部（可帶 ?status=PAID）
 * - GET /api/orders/{id}          查單筆
 * - POST /api/orders              建立訂單（由購物車轉訂單）
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@CrossOrigin
public class OrderController {

    private final OrderService svc;

    /** 建立訂單（由購物車轉訂單） */
    @PostMapping
    public ResponseEntity<String> create(@RequestBody OrderRequest req) {
        String message = svc.createOrder(req);
        // 這裡直接回 200/400，亦可視需求回 201 或回傳 OrderResponse
        if ("購買成功".equals(message)) {
            return ResponseEntity.ok(message);
        }
        return ResponseEntity.badRequest().body(message);
    }

    /** 有 memberId 才會匹配：GET /api/orders?memberId=1 */
    @GetMapping
    public ResponseEntity<List<Order>> listByMember(Authentication authentication) {
        Integer memberId = Integer.parseInt(authentication.getName());
        return ResponseEntity.ok(svc.getOrdersByMember(memberId));
    }

    /** 沒有 memberId 才會匹配：GET /api/orders 或 /api/orders?status=PAID */
    @GetMapping(params = "!memberId")
    public ResponseEntity<List<Order>> listAll(
            @RequestParam(value = "status", required = false) String status) {
        String normalized = (status == null || status.isBlank()) ? null : status.trim();
        return ResponseEntity.ok(svc.listAll(normalized));
    }

    /** 查單筆：GET /api/orders/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOne(@PathVariable Integer id) {
        Order o = svc.getOrderById(id);
        return o == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(o);
    }

    /**
     * 更新訂單狀態（只更新 status 一個欄位）
     * Body 例：{ "status": "CANCELLED" }
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateOrderStatus(
            @PathVariable Integer id,
            @Valid @RequestBody StatusUpdateRequest req) {
        svc.updateStatus(id, req); // 內部已用規則檢查合法性
        return ResponseEntity.noContent().build();   // 204 No Content
    }

    //刪除訂單
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Integer id) {
        svc.delete(id);                 // 你的 service 欄位名若不是 svc，改成實際的
        return ResponseEntity.noContent().build(); // 204
    }


    /** 更新付款狀態（PENDING→PAID/FAILED, PAID→REFUNDED） */
    @PatchMapping("/{id}/payment-status")
    public ResponseEntity<Void> updatePaymentStatus(
            @PathVariable Integer id,
            @Valid @RequestBody PaymentUpdateRequest req) {
        svc.updatePayment(id, req);
        return ResponseEntity.noContent().build(); // 204
    }


}
