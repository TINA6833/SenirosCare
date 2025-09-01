package com.ryanshiun.seniorscare.roomType.dao.Impl;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.ryanshiun.seniorscare.roomType.dao.RoomCommentDao;
import com.ryanshiun.seniorscare.roomType.model.RoomComment;

@Repository
public class RoomCommentDaoImpl implements RoomCommentDao {

    private final NamedParameterJdbcTemplate jdbc;

    public RoomCommentDaoImpl(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final RowMapper<RoomComment> ROW_MAPPER = new RowMapper<>() {
        @Override
        public RoomComment mapRow(ResultSet rs, int rowNum) throws SQLException {
            RoomComment c = new RoomComment();
            c.setId(rs.getInt("id"));
            c.setMemberId(rs.getInt("member_id"));
            c.setRoomTypeId(rs.getInt("roomtype_id"));
            c.setContent(rs.getString("content"));
            c.setApproved(rs.getBoolean("is_approved"));
            c.setAdminReply(rs.getString("admin_reply"));
            c.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            return c;
        }
    };

    // ===== C =====
    @Override
    public int addComment(RoomComment c) {
        String sql = """
            INSERT INTO room_comment (member_id, roomtype_id, content, is_approved, admin_reply)
            VALUES (:member_id, :roomtype_id, :content, :is_approved, :admin_reply);
            SELECT CAST(SCOPE_IDENTITY() AS INT) AS id;
            """;
        MapSqlParameterSource p = new MapSqlParameterSource()
                .addValue("member_id", c.getMemberId())
                .addValue("roomtype_id", c.getRoomTypeId())
                .addValue("content", c.getContent())
                .addValue("is_approved", c.isApproved())
                .addValue("admin_reply", c.getAdminReply());
        return jdbc.queryForObject(sql, p, Integer.class);
    }

    // ===== U =====
    @Override
    public boolean approve(int id) {
        String sql = "UPDATE room_comment SET is_approved = 1 WHERE id = :id";
        return jdbc.update(sql, Collections.singletonMap("id", id)) > 0;
    }

    @Override
    public boolean unapprove(int id) {
        String sql = "UPDATE room_comment SET is_approved = 0 WHERE id = :id";
        return jdbc.update(sql, Collections.singletonMap("id", id)) > 0;
    }

    @Override
    public boolean updateContent(int id, String content) {
        String sql = "UPDATE room_comment SET content = :content WHERE id = :id";
        return jdbc.update(sql, Map.of("content", content, "id", id)) > 0;
    }

    @Override
    public boolean setAdminReply(int id, String reply) {
        String sql = "UPDATE room_comment SET admin_reply = :reply WHERE id = :id";
        return jdbc.update(sql, Map.of("reply", reply, "id", id)) > 0;
    }

    // ===== D =====
    @Override
    public boolean deleteById(int id) {
        String sql = "DELETE FROM room_comment WHERE id = :id";
        return jdbc.update(sql, Collections.singletonMap("id", id)) > 0;
    }

    // ===== R =====
    
    @Override
    public List<RoomComment> findAll() {
        String sql = "SELECT * FROM room_comment ORDER BY created_at DESC";
        return jdbc.query(sql, ROW_MAPPER);
    }
    @Override
    public RoomComment findById(int id) {
        String sql = "SELECT * FROM room_comment WHERE id = :id";
        try {
            return jdbc.queryForObject(sql, Collections.singletonMap("id", id), ROW_MAPPER);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public List<RoomComment> findByRoomType(int roomTypeId, Boolean approved) {
        String base = "SELECT * FROM room_comment WHERE roomtype_id = :rid";
        String order = " ORDER BY created_at DESC";
        if (approved == null) {
            return jdbc.query(base + order, Map.of("rid", roomTypeId), ROW_MAPPER);
        }
        String sql = base + " AND is_approved = :appr" + order;
        return jdbc.query(sql, Map.of("rid", roomTypeId, "appr", approved), ROW_MAPPER);
    }

    @Override
    public List<RoomComment> findByMember(int memberId, Boolean approved) {
        String base = "SELECT * FROM room_comment WHERE member_id = :mid";
        String order = " ORDER BY created_at DESC";
        if (approved == null) {
            return jdbc.query(base + order, Map.of("mid", memberId), ROW_MAPPER);
        }
        String sql = base + " AND is_approved = :appr" + order;
        return jdbc.query(sql, Map.of("mid", memberId, "appr", approved), ROW_MAPPER);
    }

    @Override
    public List<RoomComment> getLatestApprovedByRoomType(int roomTypeId, int limit) {
        String sql = """
            SELECT TOP (:limit) *
              FROM room_comment
             WHERE roomtype_id = :rid AND is_approved = 1
             ORDER BY created_at DESC
            """;
        return jdbc.query(sql, Map.of("rid", roomTypeId, "limit", limit), ROW_MAPPER);
    }

    // ===== 分頁 + 計數 =====
    @Override
    public List<RoomComment> pageByRoomType(int roomTypeId, Boolean approved, int offset, int pageSize) {
        String base = "SELECT * FROM room_comment WHERE roomtype_id = :rid";
        String cond = approved == null ? "" : " AND is_approved = :appr";
        String tail = " ORDER BY created_at DESC OFFSET :offset ROWS FETCH NEXT :size ROWS ONLY";
        String sql = base + cond + tail;

        MapSqlParameterSource p = new MapSqlParameterSource()
                .addValue("rid", roomTypeId)
                .addValue("offset", offset)
                .addValue("size", pageSize);
        if (approved != null) p.addValue("appr", approved);

        return jdbc.query(sql, p, ROW_MAPPER);
    }

    @Override
    public long countByRoomType(int roomTypeId, Boolean approved) {
        String base = "SELECT COUNT(1) FROM room_comment WHERE roomtype_id = :rid";
        String sql = approved == null ? base : base + " AND is_approved = :appr";
        MapSqlParameterSource p = new MapSqlParameterSource().addValue("rid", roomTypeId);
        if (approved != null) p.addValue("appr", approved);
        Long cnt = jdbc.queryForObject(sql, p, Long.class);
        return cnt == null ? 0 : cnt;
    }
}