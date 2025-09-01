package com.ryanshiun.seniorscare.device.rowmapper;

import com.ryanshiun.seniorscare.device.model.Cart;
import org.springframework.jdbc.core.RowMapper;
import java.sql.ResultSet;
import java.sql.SQLException;


// CartRowMapper - 把 ResultSet 轉成 Cart
public class CartRowMapper implements RowMapper<Cart> {
    @Override
    public Cart mapRow(ResultSet rs, int rowNum) throws SQLException {
        Cart c = new Cart();
        c.setCartId(rs.getInt("cart_id"));
        c.setMemberId((Integer)rs.getObject("member_id"));
        c.setGuestToken(rs.getString("guest_token"));
        c.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return c;
    }
}
