package com.ryanshiun.seniorscare.roomType.rowmapper;

import com.ryanshiun.seniorscare.roomType.model.RoomType;

import org.springframework.jdbc.core.RowMapper;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class RoomTypeRowMapper implements RowMapper<RoomType> {
    @Override
    public RoomType mapRow(ResultSet rs, int rowNum) throws SQLException {
        RoomType r = new RoomType();
        r.setId(rs.getInt("id"));
        r.setName(rs.getString("name"));
        r.setPrice(rs.getInt("price"));
        r.setCapacity(rs.getInt("capacity"));
        r.setDescription(rs.getString("description"));
        r.setImagePath(rs.getString("image_path"));
        r.setAvailable(rs.getBoolean("is_available")); // BIT -> boolean
        r.setAdminNote(rs.getString("admin_note"));
        r.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        r.setUpdatedAt(rs.getObject("updated_at", LocalDateTime.class));
        return r;
    }
}

//RoomTypeRowMapper 是使用Spring JDBC所需要的元件之一

//用來把ResultSet的資料行，轉換成Java物件