package com.ryanshiun.seniorscare.roomType.dao.Impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import com.ryanshiun.seniorscare.roomType.dao.FacilityDao;
import com.ryanshiun.seniorscare.roomType.model.Facility;

@Repository
public class FacilityDaoImpl implements FacilityDao {

    private final NamedParameterJdbcTemplate jdbc;

    public FacilityDaoImpl(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final RowMapper<Facility> FACILITY_ROW_MAPPER = new RowMapper<>() {
        @Override
        public Facility mapRow(ResultSet rs, int rowNum) throws SQLException {
            Facility f = new Facility();
            f.setId(rs.getInt("id"));
            f.setName(rs.getString("name"));
            f.setDescription(rs.getString("description"));
            f.setImagePath(rs.getString("image_path"));
            f.setAvailable(rs.getBoolean("is_available"));
            // 若你的 model 沒有 createdAt / updatedAt，下面兩行移除即可
            try { f.setCreatedAt(rs.getTimestamp("created_at")); } catch (SQLException ignore) {}
            try { f.setUpdatedAt(rs.getTimestamp("updated_at")); } catch (SQLException ignore) {}
            return f;
        }
    };

    // ===== C =====
    @Override
    public int addFacility(Facility facility) {
        String sql = """
            INSERT INTO facility (name, description, image_path, is_available)
            VALUES (:name, :description, :image_path, :is_available)
            """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", facility.getName())
                .addValue("description", facility.getDescription())
                .addValue("image_path", facility.getImagePath())
                .addValue("is_available", facility.isAvailable());

        KeyHolder kh = new GeneratedKeyHolder();
        int affected = jdbc.update(sql, params, kh, new String[]{"id"});
        if (affected <= 0) return 0;
        Number key = kh.getKey();
        return key == null ? 0 : key.intValue();
    }

    // ===== R =====
    @Override
    public Facility getFacilityById(int id) {
        String sql = """
            SELECT id, name, description, image_path, is_available, created_at, updated_at
              FROM facility
             WHERE id = :id
            """;
        try {
            return jdbc.queryForObject(sql, Collections.singletonMap("id", id), FACILITY_ROW_MAPPER);
        } catch (EmptyResultDataAccessException e) {
            return null; // 依你的介面回傳型別，查不到就回 null
        }
    }

    @Override
    public List<Facility> getAllFacilities() {
        String sql = """
            SELECT id, name, description, image_path, is_available, created_at, updated_at
              FROM facility
             ORDER BY id DESC
            """;
        return jdbc.query(sql, FACILITY_ROW_MAPPER);
    }

    @Override
    public List<Facility> getAvailableFacilities() {
        String sql = """
            SELECT id, name, description, image_path, is_available, created_at, updated_at
              FROM facility
             WHERE is_available = 1
             ORDER BY name ASC
            """;
        return jdbc.query(sql, FACILITY_ROW_MAPPER);
    }

    @Override
    public List<Facility> searchFacilitiesByName(String keyword) {
        String sql = """
            SELECT id, name, description, image_path, is_available, created_at, updated_at
              FROM facility
             WHERE name LIKE :kw
             ORDER BY name ASC
            """;
        return jdbc.query(sql, Map.of("kw", "%" + keyword + "%"), FACILITY_ROW_MAPPER);
    }

    @Override
    public List<Facility> getFacilitiesByAvailability(boolean isAvailable) {
        String sql = """
            SELECT id, name, description, image_path, is_available, created_at, updated_at
              FROM facility
             WHERE is_available = :is_available
             ORDER BY name ASC
            """;
        return jdbc.query(sql, Map.of("is_available", isAvailable), FACILITY_ROW_MAPPER);
    }

    // ===== U =====
    @Override
    public boolean updateFacility(Facility facility) {
        String sql = """
            UPDATE facility
               SET name = :name,
                   description = :description,
                   image_path = :image_path,
                   is_available = :is_available,
                   updated_at = SYSUTCDATETIME()
             WHERE id = :id
            """;
        Map<String, Object> params = Map.of(
                "name", facility.getName(),
                "description", facility.getDescription(),
                "image_path", facility.getImagePath(),
                "is_available", facility.isAvailable(),
                "id", facility.getId()
        );
        return jdbc.update(sql, params) > 0;
    }

    // ===== D =====
    @Override
    public boolean deleteFacility(int id) {
        String sql = "DELETE FROM facility WHERE id = :id";
        return jdbc.update(sql, Collections.singletonMap("id", id)) > 0;
    }
}