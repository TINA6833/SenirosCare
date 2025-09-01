package com.ryanshiun.seniorscare.device.service.Impl;

import com.ryanshiun.seniorscare.device.dao.CartDao;
import com.ryanshiun.seniorscare.device.dto.CartRequest;
import com.ryanshiun.seniorscare.device.dto.CartResponse;
import com.ryanshiun.seniorscare.device.dto.CartItemResponse;
import com.ryanshiun.seniorscare.device.model.Cart;
import com.ryanshiun.seniorscare.device.model.CartItem;
import com.ryanshiun.seniorscare.device.service.CartService;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.stream.Collectors;
import java.util.List;
import java.util.Map;


@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartDao cartDao;

    // 建立或取得購物車
    @Override
    public CartResponse createOrGetCart(Integer memberId, String guestToken) {
        Cart cart = cartDao.getCartByMemberOrGuest(memberId, guestToken);
        if (cart == null) {
            Integer cartId = cartDao.createCart(memberId, guestToken);
            cart = cartDao.getCartById(cartId);
        }
        // build response
        CartResponse resp = new CartResponse();
        resp.setCartId(cart.getCartId());
        resp.setMemberId(cart.getMemberId());
        resp.setGuestToken(cart.getGuestToken());
        resp.setItems(cartDao.getCartItemsByCartId(cart.getCartId())
                .stream()
                .map(i -> {
                    CartItemResponse ir = new CartItemResponse();
                    ir.setDeviceId(i.getDeviceId());
                    ir.setQuantity(i.getQuantity());
                    return ir;
                })
                .collect(Collectors.toList()));
        return resp;
    }

    // 依 cartId 取得購物車
    @Override
    public CartResponse getCartById(Integer cartId) {
        Cart cart = cartDao.getCartById(cartId);
        if (cart == null) return null;
        CartResponse resp = new CartResponse();
        resp.setCartId(cart.getCartId());
        resp.setMemberId(cart.getMemberId());
        resp.setGuestToken(cart.getGuestToken());
        resp.setItems(cartDao.getCartItemsByCartId(cart.getCartId())
                .stream()
                .map(i -> {
                    CartItemResponse ir = new CartItemResponse();
                    ir.setDeviceId(i.getDeviceId());
                    ir.setQuantity(i.getQuantity());
                    return ir;
                })
                .collect(Collectors.toList()));
        return resp;
    }

    // 取得購物車所有商品
    @Override
    public List<CartItemResponse> getCartItems(Integer cartId) {
        return cartDao.getCartItemsByCartId(cartId)
                .stream()
                .map(i -> {
                    CartItemResponse ir = new CartItemResponse();
                    ir.setDeviceId(i.getDeviceId());
                    ir.setQuantity(i.getQuantity());
                    return ir;
                })
                .collect(Collectors.toList());
    }

    // 取得購物車內的指定商品
    @Override
    public CartItem getCartItem(Map<String, Integer> cartItem) {
        return cartDao.getCartItem(cartItem);
    }

    // 加入商品
    @Override
    public Map<String, Integer> addCartItem(Integer cartId, CartRequest req) {
        return cartDao.addCartItem(cartId, req.getDeviceId(), req.getQuantity());
    }

    // 更新商品數量
    @Override
    public void updateCartItemQuantity(Integer cartId, Integer deviceId, CartRequest req) {
        cartDao.updateCartItemQuantity(cartId, deviceId, req.getQuantity());
    }

    // 移除商品
    @Override
    public void removeCartItem(Integer cartId, Integer deviceId) {
        cartDao.removeCartItem(cartId, deviceId);
    }

    // 清空購物車
    @Override
    public void clearCart(Integer cartId) {
        cartDao.clearCartItems(cartId);
    }
}
