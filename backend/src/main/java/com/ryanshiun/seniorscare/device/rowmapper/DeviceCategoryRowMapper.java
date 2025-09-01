package com.ryanshiun.seniorscare.device.rowmapper;

import com.ryanshiun.seniorscare.device.model.DeviceCategory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DeviceCategoryRowMapper implements RowMapper<DeviceCategory> {
    @Override
    public DeviceCategory mapRow(ResultSet rs, int rowNum) throws SQLException {
        DeviceCategory category = new DeviceCategory();
        category.setId(rs.getInt("id"));
        category.setName(rs.getString("name"));
        category.setCategoryId(rs.getInt("category_id"));
        return category;
    }
}
