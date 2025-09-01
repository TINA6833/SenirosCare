package com.ryanshiun.seniorscare.device.controller;

import com.ryanshiun.seniorscare.device.dto.CartRequest;
import com.ryanshiun.seniorscare.device.dto.CartResponse;
import com.ryanshiun.seniorscare.device.model.CartItem;
import com.ryanshiun.seniorscare.device.dto.CartItemResponse;
import com.ryanshiun.seniorscare.device.service.CartService;
import com.ryanshiun.seniorscare.device.service.DeviceService;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/carts")
@CrossOrigin
public class CartController {

    @Autowired
    private CartService cartService;
    @Autowired
    private DeviceService deviceService;

    /** 建立或取得購物車 */
    @PostMapping
    public CartResponse createOrGetCart(
            @RequestParam(required = false) String guestToken,
            Authentication authentication) {
        Integer memberId = Integer.parseInt(authentication.getName());
        return cartService.createOrGetCart(memberId, guestToken);
    }

    /** 依 cartId 取得購物車 */
    @GetMapping("/{cartId}")
    public CartResponse getCart(@PathVariable Integer cartId) {
        return cartService.getCartById(cartId);
    }

    /** 取得購物車所有商品 */
    @GetMapping("/{cartId}/items")
    public List<CartItemResponse> getItems(@PathVariable Integer cartId) {
        return cartService.getCartItems(cartId);
    }

    /** 加入購物車商品 */
    @PostMapping("/{cartId}/items")
    public ResponseEntity<?> addItem(@PathVariable Integer cartId,
                                     @RequestBody CartRequest req) {
        // 判斷商品是否有庫存
        Map<String, Object> response = new HashMap<>();
        boolean inStock = deviceService.checkInStock(req.getDeviceId(), req.getQuantity());
        if(!inStock) {
            response.put("message", "庫存不足。");
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        Map<String, Integer> cartItem = cartService.addCartItem(cartId, req);
        CartItem item = cartService.getCartItem(cartItem);
        // 回傳訊息&加入商品的資料
        response.put("message", "加入購物車成功。");
        response.put("data", item);
        return ResponseEntity.ok(response);
    }

    /** 修改購物車明細數量 */
    @PutMapping("/{cartId}/items/{deviceId}")
    public ResponseEntity<?> updateItem(@PathVariable Integer cartId,
                                        @PathVariable Integer deviceId,
                                        @RequestBody CartRequest req) {
        // 判斷商品是否有庫存
        Map<String, Object> response = new HashMap<>();
        boolean inStock = deviceService.checkInStock(req.getDeviceId(), req.getQuantity());
        if(!inStock) {
            response.put("message", "庫存不足。");
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        cartService.updateCartItemQuantity(cartId, deviceId, req);
        return ResponseEntity.ok("更新購物車成功。");
    }

    /** 刪除單個購物車商品 */
    @DeleteMapping("/{cartId}/items/{deviceId}")
    public ResponseEntity<?> removeItem(@PathVariable Integer cartId,
                                        @PathVariable Integer deviceId) {
        cartService.removeCartItem(cartId, deviceId);
        return ResponseEntity.ok("刪除購物車商品成功。");
    }

    /** 清空整個購物車裡的所有商品 */
    @DeleteMapping("/{cartId}/items")
    public ResponseEntity<?> clearCart(@PathVariable Integer cartId) {
        cartService.clearCart(cartId);
        return ResponseEntity.ok("刪除購物車商品成功。");
    }
}
