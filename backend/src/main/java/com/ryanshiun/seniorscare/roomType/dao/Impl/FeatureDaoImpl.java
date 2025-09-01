package com.ryanshiun.seniorscare.roomType.dao.Impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.ryanshiun.seniorscare.roomType.dao.FeatureDao;
import com.ryanshiun.seniorscare.roomType.model.Feature;

@Repository
public class FeatureDaoImpl implements FeatureDao {

    private final NamedParameterJdbcTemplate jdbc;

    public FeatureDaoImpl(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final RowMapper<Feature> FEATURE_ROW_MAPPER = new RowMapper<Feature>() {
        @Override
        public Feature mapRow(ResultSet rs, int rowNum) throws SQLException {
            Feature f = new Feature();
            f.setId(rs.getInt("id"));
            f.setName(rs.getString("name"));
            return f;
        }
    };

    @Override
    public Integer insert(Feature feature) {
        String sql = "INSERT INTO room_features (name) VALUES (:name)";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", feature.getName());
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(sql, params, kh, new String[]{"id"});
        Number key = kh.getKey();
        return key == null ? null : key.intValue();
    }

    @Override
    public int[] batchInsert(List<Feature> features) {
        String sql = "INSERT INTO room_features (name) VALUES (:name)";
        SqlParameterSource[] batch = features.stream()
                .map(f -> new MapSqlParameterSource().addValue("name", f.getName()))
                .toArray(SqlParameterSource[]::new);
        return jdbc.batchUpdate(sql, batch);
    }

    @Override
    public boolean update(Feature feature) {
        String sql = "UPDATE room_features SET name = :name WHERE id = :id";
        Map<String, Object> params = Map.of(
                "name", feature.getName(),
                "id", feature.getId()
        );
        return jdbc.update(sql, params) > 0;
    }

    @Override
    public boolean deleteById(int id) {
        String sql = "DELETE FROM room_features WHERE id = :id";
        return jdbc.update(sql, Collections.singletonMap("id", id)) > 0;
    }

    @Override
    public Optional<Feature> findById(int id) {
        String sql = "SELECT id, name FROM room_features WHERE id = :id";
        try {
            Feature f = jdbc.queryForObject(sql, Collections.singletonMap("id", id), FEATURE_ROW_MAPPER);
            return Optional.ofNullable(f);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Feature> findAll() {
        String sql = "SELECT id, name FROM room_features ORDER BY id DESC";
        return jdbc.query(sql, FEATURE_ROW_MAPPER);
    }

    @Override
    public List<Feature> findByNameLike(String keyword) {
        String sql = "SELECT id, name FROM room_features WHERE name LIKE :kw ORDER BY name ASC";
        return jdbc.query(sql, Collections.singletonMap("kw", "%" + keyword + "%"), FEATURE_ROW_MAPPER);
    }

    @Override
    public boolean existsByName(String name) {
        String sql = "SELECT COUNT(1) FROM room_features WHERE name = :name";
        Long cnt = jdbc.queryForObject(sql, Collections.singletonMap("name", name), Long.class);
        return cnt != null && cnt > 0;
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(1) FROM room_features";
        Long cnt = jdbc.queryForObject(sql, new MapSqlParameterSource(), Long.class);
        return cnt == null ? 0 : cnt;
    }
    public int countFeatureUsage(int featureId) {
        String sql = "SELECT COUNT(*) FROM dbo.roomtype_feature WHERE feature_id = :id";
        return jdbc.queryForObject(sql, Map.of("id", featureId), Integer.class);
    }


}