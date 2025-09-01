package com.ryanshiun.seniorscare.device.rowmapper;

import com.ryanshiun.seniorscare.device.model.Device;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DeviceRowMapper
 * 用來將資料庫查詢結果的每一筆資料（ResultSet）轉換為 Device 物件
 */
public class DeviceRowMapper implements RowMapper<Device> {

    @Override
    public Device mapRow(ResultSet rs, int rowNum) throws SQLException {
        // 使用 Builder 模式創建 Device 物件
        return Device.builder()
                .id(rs.getInt("id"))
                .name(rs.getString("name"))
                .sku(rs.getString("sku"))
                .unitPrice(rs.getBigDecimal("unit_price"))
                .inventory(rs.getInt("inventory"))
                .description(rs.getString("description"))
                .image(rs.getString("image"))
                .isOnline(rs.getBoolean("is_online"))
                .categoryId(rs.getInt("category_id"))
                .createdByEmpId(rs.getInt("created_by_emp_id"))
                .build();
    }
}
