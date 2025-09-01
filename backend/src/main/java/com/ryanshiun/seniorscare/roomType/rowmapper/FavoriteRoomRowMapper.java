package com.ryanshiun.seniorscare.roomType.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

import org.springframework.jdbc.core.RowMapper;

import com.ryanshiun.seniorscare.roomType.model.FavoriteRoom;

public class FavoriteRoomRowMapper implements RowMapper<FavoriteRoom> {
    @Override
    public FavoriteRoom mapRow(ResultSet rs, int rowNum) throws SQLException {
        FavoriteRoom f = new FavoriteRoom();
        f.setMemberId(rs.getInt("member_id"));
        f.setRoomTypeId(rs.getInt("roomtype_id"));
        f.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        return f;
    }
}