package com.ryanshiun.seniorscare.device.rowmapper;

import com.ryanshiun.seniorscare.device.model.CartItem;
import org.springframework.jdbc.core.RowMapper;
import java.sql.ResultSet;
import java.sql.SQLException;


//CartItemRowMapper - 把 ResultSet 轉成 CartItem
public class CartItemRowMapper implements RowMapper<CartItem> {
    @Override
    public CartItem mapRow(ResultSet rs, int rowNum) throws SQLException {
        CartItem i = new CartItem();
        i.setCartId(rs.getInt("cart_id"));
        i.setDeviceId(rs.getInt("device_id"));
        i.setQuantity(rs.getInt("quantity"));
        i.setAddedAt(rs.getTimestamp("added_at").toLocalDateTime());
        return i;
    }
}
