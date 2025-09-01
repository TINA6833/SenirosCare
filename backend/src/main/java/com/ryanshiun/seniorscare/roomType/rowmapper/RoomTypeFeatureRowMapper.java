package com.ryanshiun.seniorscare.roomType.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.ryanshiun.seniorscare.roomType.model.RoomTypeFeature;

public class RoomTypeFeatureRowMapper implements RowMapper<RoomTypeFeature> {
    @Override
    public RoomTypeFeature mapRow(ResultSet rs, int rowNum) throws SQLException {
        RoomTypeFeature rf = new RoomTypeFeature();
        rf.setRoomTypeId(rs.getInt("roomtype_id"));
        rf.setFeatureId(rs.getInt("feature_id"));
        return rf;
    }
}