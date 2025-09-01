package com.ryanshiun.seniorscare.roomType.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.ryanshiun.seniorscare.roomType.model.Feature;

public class FeatureRowMapper implements RowMapper<Feature> {
    @Override
    public Feature mapRow(ResultSet rs, int rowNum) throws SQLException {
        Feature f = new Feature();
        f.setId(rs.getInt("id"));
        f.setName(rs.getString("name"));
        return f;
    }
}
