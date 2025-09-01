package com.ryanshiun.seniorscare.roomType.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.ryanshiun.seniorscare.roomType.model.Facility;

public class FacilityRowMapper implements RowMapper<Facility> {

    @Override
    public Facility mapRow(ResultSet rs, int rowNum) throws SQLException {
        Facility f = new Facility();
        f.setId(rs.getInt("id"));
        f.setName(rs.getString("name"));
        f.setDescription(rs.getString("description"));
        f.setImagePath(rs.getString("image_path"));
        f.setAvailable(rs.getBoolean("is_available")); // NULL -> false

        f.setCreatedAt(rs.getTimestamp("created_at")); // java.util.Date
        f.setUpdatedAt(rs.getTimestamp("updated_at"));

        return f;
    }
}