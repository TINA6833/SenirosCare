package com.ryanshiun.seniorscare.device.dao;

import com.ryanshiun.seniorscare.device.model.Cart;
import com.ryanshiun.seniorscare.device.model.CartItem;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;


public interface CartDao {
    // 建立新購物車，回傳 cart_id
    Integer createCart(Integer memberId, String guestToken);
    // 依 cart_id 取得購物車
    Cart getCartById(Integer cartId);
    // 依會員或訪客取得購物車
    Cart getCartByMemberOrGuest(Integer memberId, String guestToken);
    // 加入購物車項目
    Map<String, Integer> addCartItem(Integer cartId, Integer deviceId, Integer quantity);
    // 修改項目數量
    void updateCartItemQuantity(Integer cartId, Integer deviceId, Integer quantity);
    // 移除購物車項目
    void removeCartItem(Integer cartId, Integer deviceId);
    // 取得購物車所有明細
    List<CartItem> getCartItemsByCartId(Integer cartId);
    // 取得購物車內的指定商品
    CartItem getCartItem(Map<String, Integer> cartItem);
    //清空購物車所有項目
    void clearCartItems(Integer cartId);
    // 計算購物車總金額
    BigDecimal calcTotalAmount(Cart cart);
    // 下完訂單後需移除購物車 member_id 關聯，下一次新增購物車才會是新的購物車
    void removeCartByMemberId(Integer memberId);



}
