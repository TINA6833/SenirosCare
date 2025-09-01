package com.ryanshiun.seniorscare.roomType.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

import org.springframework.jdbc.core.RowMapper;

import com.ryanshiun.seniorscare.roomType.model.RoomComment;
public class RoomCommentRowMapper implements RowMapper<RoomComment> {
    @Override
    public RoomComment mapRow(ResultSet rs, int rowNum) throws SQLException {
        RoomComment c = new RoomComment();
        c.setId(rs.getInt("id"));
        c.setMemberId(rs.getInt("member_id"));
        c.setRoomTypeId(rs.getInt("roomtype_id"));
        c.setContent(rs.getString("content"));
        c.setApproved(rs.getBoolean("is_approved"));
        c.setAdminReply(rs.getString("admin_reply"));
        c.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        return c;
    }
}