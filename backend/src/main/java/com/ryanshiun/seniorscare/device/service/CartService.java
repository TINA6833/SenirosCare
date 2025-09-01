package com.ryanshiun.seniorscare.device.service;

import com.ryanshiun.seniorscare.device.dto.CartRequest;
import com.ryanshiun.seniorscare.device.dto.CartResponse;
import com.ryanshiun.seniorscare.device.model.CartItem;
import com.ryanshiun.seniorscare.device.dto.CartItemResponse;
import java.util.List;
import java.util.Map;


public interface CartService {
    // 建立或取得購物車
    CartResponse createOrGetCart(Integer memberId, String guestToken);
    // 依 cartId 取得購物車
    CartResponse getCartById(Integer cartId);
    // 取得購物車所有商品
    List<CartItemResponse> getCartItems(Integer cartId);
    // 取得購物車內的指定商品
    CartItem getCartItem(Map<String, Integer> cartItem);
    // 加入商品
    Map<String, Integer> addCartItem(Integer cartId, CartRequest req);
    // 更新商品數量
    void updateCartItemQuantity(Integer cartId, Integer deviceId, CartRequest req);
    // 移除商品
    void removeCartItem(Integer cartId, Integer deviceId);
    //清空購物車
    void clearCart(Integer cartId);
}
