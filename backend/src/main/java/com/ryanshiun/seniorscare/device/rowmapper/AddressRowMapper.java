package com.ryanshiun.seniorscare.device.rowmapper;

import com.ryanshiun.seniorscare.device.model.Address;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

 //ResultSet 轉換為 Address 物件

public class AddressRowMapper implements RowMapper<Address> {

    @Override
    public Address mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Address.builder()
                .id(rs.getInt("id"))
                .memberId(rs.getInt("member_id"))
                .recipient(rs.getString("recipient"))
                .phone(rs.getString("phone"))
                .postalCode(rs.getString("postal_code"))
                .addressLine1(rs.getString("address_line1"))
                .addressLine2(rs.getString("address_line2"))
                .isDefault(rs.getBoolean("is_default"))
                .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                .build();
    }
}
