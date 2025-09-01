
package com.ryanshiun.seniorscare.device.controller;

import com.ryanshiun.seniorscare.device.dto.PaymentUpdateRequest;
import com.ryanshiun.seniorscare.device.dto.StatusUpdateRequest;
import com.ryanshiun.seniorscare.device.model.Order;
import com.ryanshiun.seniorscare.device.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台訂單管理接口
 */
@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {
    private final OrderService svc;

    /** 4. 列表，可按 status 筛 */
    @GetMapping
    public ResponseEntity<List<Order>> list(@RequestParam(required=false) String status) {
        return ResponseEntity.ok(svc.listAll(status));
    }

    /** 5. 更新訂單狀態 */
    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable Integer id,
            @RequestBody StatusUpdateRequest req
    ) {
        svc.updateStatus(id, req);
        return ResponseEntity.noContent().build();
    }

    /** 6. 更新付款狀態*/
    @PatchMapping("/{id}/payment")
    public ResponseEntity<Void> updatePayment(
            @PathVariable Integer id,
            @RequestBody PaymentUpdateRequest req
    ) {
        svc.updatePayment(id, req);
        return ResponseEntity.noContent().build();
    }
}
