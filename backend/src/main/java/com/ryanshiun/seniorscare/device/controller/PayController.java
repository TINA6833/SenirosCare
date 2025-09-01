package com.ryanshiun.seniorscare.device.controller;

import com.ryanshiun.seniorscare.device.dto.LineRequestParam;
import com.ryanshiun.seniorscare.device.dto.LineRequestParam.Package;
import com.ryanshiun.seniorscare.device.dto.PaymentUpdateRequest;
import com.ryanshiun.seniorscare.device.model.Order;
import com.ryanshiun.seniorscare.device.service.OrderService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import jakarta.servlet.http.HttpServletResponse;  // ← 用這個
import org.springframework.web.bind.annotation.RequestParam; // ← 記得有
import java.nio.charset.StandardCharsets;
import java.util.*;

@RequestMapping("/api/pay")
@RestController
public class PayController {

    private final OrderService orderService;

    @Value("${CHANNEL_SECRET}")
    private String CHANNEL_SECRET;

    @Value("${CHANNEL_ID}")
    private String CHANNEL_ID;

    @Value("${API_PATH}")
    private String API_PATH;

    public PayController(OrderService orderService) {
        this.orderService = orderService;
    }

    /** 產生付款網址（回 JSON，前端自行導轉） */
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<Map<String, String>> getOrderPayUrlByOrderId(@PathVariable Integer orderId) throws Exception {
        Order order = orderService.getOrderById(orderId);
        if (order == null) return ResponseEntity.notFound().build();

        List<LineRequestParam.Prdocut> products = order.getItems().stream().map(d -> {
            LineRequestParam.Prdocut p = new LineRequestParam.Prdocut();
            p.setId(UUID.randomUUID().toString());
            p.setName(d.getName() + "x" + d.getQuantity());
            p.setPrice(d.getUnitPrice().intValue());
            p.setQuantity(d.getQuantity());
            return p;
        }).toList();

        int totalPrice = products.stream().mapToInt(p -> p.getPrice() * p.getQuantity()).sum();

        LineRequestParam param = new LineRequestParam();
        param.setAmount(totalPrice);
        // 如果你的 LineRequestParam 支援字串，建議改成 order.getOrderNo()
        param.setOrderId(order.getId());

        LineRequestParam.RedirectUrls redirectUrls = new LineRequestParam.RedirectUrls();
        redirectUrls.setConfirmUrl("http://localhost:8080/api/pay/line/callback?orderId=" + order.getId());
        redirectUrls.setCancelUrl("http://localhost:8080/api/pay/line/cancel?orderId=" + order.getId());
        param.setRedirectUrls(redirectUrls);

        Package pkg = new LineRequestParam.Package();
        pkg.setId("package1");
        pkg.setAmount(totalPrice);
        pkg.setName("商品組A");
        pkg.setProducts(products);
        param.setPackages(List.of(pkg));

        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(param);

        String nonce = UUID.randomUUID().toString();
        String message = CHANNEL_SECRET + API_PATH + body + nonce;

        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(CHANNEL_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        String signature = Base64.getEncoder().encodeToString(mac.doFinal(message.getBytes(StandardCharsets.UTF_8)));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-LINE-Authorization", signature);
        headers.set("X-LINE-Authorization-Nonce", nonce);
        headers.set("X-LINE-ChannelId", CHANNEL_ID);

        String url = "https://sandbox-api-pay.line.me" + API_PATH;
        var http = new org.springframework.web.client.RestTemplate();
        ResponseEntity<String> result = http.postForEntity(url, new HttpEntity<>(body, headers), String.class);

        JsonNode root = mapper.readTree(result.getBody());
        String paymentUrl = root.path("info").path("paymentUrl").path("web").asText();
        if (paymentUrl == null || paymentUrl.isBlank()) {
            return ResponseEntity.status(502).body(Map.of("error", result.getBody()));
        }
        return ResponseEntity.ok(Map.of("paymentUrl", paymentUrl));
    }

    /** LINE 付款成功導回（專題簡化版：直接標記 PAID，再導回前端） */
    @GetMapping("/line/callback")
    public void lineCallback(@RequestParam Integer orderId,
                             @RequestParam(required = false) String transactionId,
                             HttpServletResponse resp) throws java.io.IOException {
        String txn = (transactionId == null || transactionId.isBlank())
                ? "MOCK-" + UUID.randomUUID()
                : transactionId;

        orderService.updatePayment(orderId, new PaymentUpdateRequest("PAID", txn));

        resp.sendRedirect("http://localhost:5174/orders");
    }

    /** LINE 取消/失敗導回（標記 FAILED） */
    @GetMapping("/line/cancel")
    public void lineCancel(@RequestParam Integer orderId,
                           HttpServletResponse resp) throws java.io.IOException {
        orderService.updatePayment(orderId, new PaymentUpdateRequest("FAILED", null));
        resp.sendRedirect("http://localhost:5173/view/order?orderId=" + orderId);
    }
}
