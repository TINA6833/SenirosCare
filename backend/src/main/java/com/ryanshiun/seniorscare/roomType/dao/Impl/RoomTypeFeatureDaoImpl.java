package com.ryanshiun.seniorscare.roomType.dao.Impl;

import java.util.Collections;
import java.util.List;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.ryanshiun.seniorscare.roomType.dao.RoomTypeFeatureDao;

@Repository
public class RoomTypeFeatureDaoImpl implements RoomTypeFeatureDao {

    private final NamedParameterJdbcTemplate jdbc;

    public RoomTypeFeatureDaoImpl(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public int insert(int roomTypeId, int featureId) {
        String sql = """
            INSERT INTO roomtype_feature (roomtype_id, feature_id)
            VALUES (:roomtype_id, :feature_id)
            """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("roomtype_id", roomTypeId)
                .addValue("feature_id", featureId);
        return jdbc.update(sql, params);
    }

    @Override
    public int[] batchInsert(int roomTypeId, List<Integer> featureIds) {
        String sql = """
            INSERT INTO roomtype_feature (roomtype_id, feature_id)
            VALUES (:roomtype_id, :feature_id)
            """;
        return jdbc.batchUpdate(sql, featureIds.stream()
                .map(fid -> new MapSqlParameterSource()
                        .addValue("roomtype_id", roomTypeId)
                        .addValue("feature_id", fid))
                .toArray(MapSqlParameterSource[]::new));
    }

    @Override
    public int deleteByRoomTypeId(int roomTypeId) {
        String sql = "DELETE FROM roomtype_feature WHERE roomtype_id = :roomtype_id";
        return jdbc.update(sql, Collections.singletonMap("roomtype_id", roomTypeId));
    }

    @Override
    public int deleteByRoomTypeAndFeature(int roomTypeId, int featureId) {
        String sql = """
            DELETE FROM roomtype_feature
            WHERE roomtype_id = :roomtype_id AND feature_id = :feature_id
            """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("roomtype_id", roomTypeId)
                .addValue("feature_id", featureId);
        return jdbc.update(sql, params);
    }

    @Override
    public List<Integer> findFeatureIdsByRoomTypeId(int roomTypeId) {
        String sql = "SELECT feature_id FROM roomtype_feature WHERE roomtype_id = :roomtype_id";
        return jdbc.queryForList(sql, Collections.singletonMap("roomtype_id", roomTypeId), Integer.class);
    }

    @Override
    public List<Integer> findRoomTypeIdsByFeatureId(int featureId) {
        String sql = "SELECT roomtype_id FROM roomtype_feature WHERE feature_id = :feature_id";
        return jdbc.queryForList(sql, Collections.singletonMap("feature_id", featureId), Integer.class);
    }

    @Override
    public long countFeaturesByRoomTypeId(int roomTypeId) {
        String sql = "SELECT COUNT(1) FROM roomtype_feature WHERE roomtype_id = :roomtype_id";
        Long cnt = jdbc.queryForObject(sql, Collections.singletonMap("roomtype_id", roomTypeId), Long.class);
        return cnt == null ? 0 : cnt;
    }

    @Override
    public long countRoomTypesByFeatureId(int featureId) {
        String sql = "SELECT COUNT(1) FROM roomtype_feature WHERE feature_id = :feature_id";
        Long cnt = jdbc.queryForObject(sql, Collections.singletonMap("feature_id", featureId), Long.class);
        return cnt == null ? 0 : cnt;
    }
}
