package com.ryanshiun.seniorscare.device.dao.Impl;

import com.ryanshiun.seniorscare.device.dao.CartDao;
import com.ryanshiun.seniorscare.device.model.Cart;
import com.ryanshiun.seniorscare.device.model.CartItem;
import com.ryanshiun.seniorscare.device.rowmapper.CartRowMapper;
import com.ryanshiun.seniorscare.device.rowmapper.CartItemRowMapper;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Repository
public class CartDaoImpl implements CartDao {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    // 建立新購物車，回傳 cart_id
    @Override
    public Integer createCart(Integer memberId, String guestToken) {
        String sql = "INSERT INTO cart (member_id, guest_token) VALUES (:memberId, :guestToken)";
        KeyHolder kh = new GeneratedKeyHolder();
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("memberId", memberId)
                .addValue("guestToken", guestToken);
        jdbcTemplate.update(sql, params, kh);
        return kh.getKey().intValue();
    }

    // 依 cart_id 取得購物車
    @Override
    public Cart getCartById(Integer cartId) {
        String sql = "SELECT * FROM cart WHERE cart_id = :cartId";
        List<Cart> list = jdbcTemplate.query(sql, Map.of("cartId", cartId), new CartRowMapper());
        return list.isEmpty() ? null : list.get(0);
    }

    // 依會員或訪客取得購物車
    @Override
    public Cart getCartByMemberOrGuest(Integer memberId, String guestToken) {
        String sql;
        Map<String,Object> params = new HashMap<>();

        if (memberId != null) {
            // 會員購物車
            sql = "SELECT * FROM cart WHERE member_id = :memberId";
            params.put("memberId", memberId);
        } else if (guestToken != null) {
            // 訪客購物車
            sql = "SELECT * FROM cart WHERE guest_token = :guestToken";
            params.put("guestToken", guestToken);
        } else {
            return null;  // 既沒有 member 也沒有 guestToken
        }

        List<Cart> carts = jdbcTemplate.query(sql, params, new CartRowMapper());
        return carts.isEmpty() ? null : carts.get(0);
    }


    // 加入購物車項目
    @Override
    public Map<String, Integer> addCartItem(Integer cartId, Integer deviceId, Integer quantity) {
        String sql = "INSERT INTO cart_item (cart_id, device_id, quantity) "
                + "VALUES (:cartId, :deviceId, :quantity)";
        jdbcTemplate.update(sql, Map.of(
                "cartId", cartId,
                "deviceId", deviceId,
                "quantity", quantity
        ));
        Map<String, Integer> cartItem = new HashMap<>();
        cartItem.put("cartId", cartId);
        cartItem.put("deviceId", deviceId);

        return cartItem;
    }

    // 修改項目數量
    @Override
    public void updateCartItemQuantity(Integer cartId, Integer deviceId, Integer quantity) {
        String sql = "UPDATE cart_item SET quantity = :quantity "
                + "WHERE cart_id = :cartId AND device_id = :deviceId";
        jdbcTemplate.update(sql, Map.of(
                "cartId", cartId,
                "deviceId", deviceId,
                "quantity", quantity
        ));
    }

    // 移除購物車項目
    @Override
    public void removeCartItem(Integer cartId, Integer deviceId) {
        String sql = "DELETE FROM cart_item WHERE cart_id = :cartId AND device_id = :deviceId";
        jdbcTemplate.update(sql, Map.of("cartId", cartId, "deviceId", deviceId));
    }

    // 取得購物車所有明細
    @Override
    public List<CartItem> getCartItemsByCartId(Integer cartId) {
        String sql = "SELECT * FROM cart_item WHERE cart_id = :cartId";
        return jdbcTemplate.query(sql, Map.of("cartId", cartId), new CartItemRowMapper());
    }

    // 取得購物車內的指定商品
    @Override
    public CartItem getCartItem(Map<String, Integer> cartItem) {
        String sql = "SELECT * FROM cart_item WHERE cart_id = :cartId and device_id = :deviceId";
        return jdbcTemplate.queryForObject(sql,
                Map.of(
                        "cartId", cartItem.get("cartId"),
                        "deviceId", cartItem.get("deviceId")),
                new CartItemRowMapper());
    }

    //清空所有購物車(by cart_id)
    @Override
    public void clearCartItems(Integer cartId) {
        String sql = "DELETE FROM cart_item WHERE cart_id = :cartId";
        jdbcTemplate.update(sql, Map.of("cartId", cartId));
    }

    @Override
    public BigDecimal calcTotalAmount(Cart cart) {
        List<CartItem> items = getCartItemsByCartId(cart.getCartId());
        return items.stream()
                .map(ci -> {
                    // 1. 讀當下單價（DECIMAL）
                    BigDecimal price = jdbcTemplate.queryForObject(
                            "SELECT unit_price FROM device WHERE id = :id",
                            Map.of("id", ci.getDeviceId()),
                            BigDecimal.class
                    );
                    // 2. 價格 × 數量
                    return price.multiply(BigDecimal.valueOf(ci.getQuantity()));
                })
                // 3. 累加
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // 下完訂單後需移除購物車 member_id 關聯，下一次新增購物車才會是新的購物車
    @Override
    public void removeCartByMemberId(Integer memberId) {
        // 1. 先刪掉這個會員購物車裡所有的 cart_item
        String deleteItemsSql =
                "DELETE ci " +
                        "FROM cart_item ci " +
                        "JOIN cart c ON ci.cart_id = c.cart_id " +
                        "WHERE c.member_id = :memberId";
        jdbcTemplate.update(deleteItemsSql, Map.of("memberId", memberId));

        // 2. 再刪除 cart 本身
        String deleteCartSql =
                "DELETE FROM cart " +
                        "WHERE member_id = :memberId";
        jdbcTemplate.update(deleteCartSql, Map.of("memberId", memberId));
    }


}
