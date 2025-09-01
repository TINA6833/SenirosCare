package com.ryanshiun.seniorscare.caregiver.rowmapper;

import com.ryanshiun.seniorscare.caregiver.model.ServiceType;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * 服務類型 RowMapper
 * 負責將資料庫查詢結果映射為 ServiceType 物件
 */
@Component
public class ServiceTypeRowMapper implements RowMapper<ServiceType> {

  @Override
  public ServiceType mapRow(ResultSet rs, int rowNum) throws SQLException {
    ServiceType serviceType = new ServiceType();

    // 基本資訊
    serviceType.setServiceTypeId(rs.getInt("service_type_id"));
    serviceType.setServiceName(rs.getString("service_name"));
    serviceType.setHourlyRate(rs.getInt("hourly_rate"));
    serviceType.setDescription(rs.getString("description"));
    serviceType.setIsActive(rs.getBoolean("is_active"));

    // 時間戳記
    Timestamp createdAt = rs.getTimestamp("created_at");
    if (createdAt != null) {
      serviceType.setCreatedAt(createdAt.toLocalDateTime());
    }

    Timestamp updatedAt = rs.getTimestamp("updated_at");
    if (updatedAt != null) {
      serviceType.setUpdatedAt(updatedAt.toLocalDateTime());
    }

    return serviceType;
  }
}