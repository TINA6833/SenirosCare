package com.ryanshiun.seniorscare.caregiver.rowmapper;

import com.ryanshiun.seniorscare.caregiver.model.CaregiverAppointment;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

@Component
public class CaregiverAppointmentRowMapper implements RowMapper<CaregiverAppointment> {

  @Override
  public CaregiverAppointment mapRow(ResultSet rs, int rowNum) throws SQLException {
    CaregiverAppointment appointment = new CaregiverAppointment();

    // 基本欄位 - 這些都存在於資料庫中
    appointment.setId(rs.getInt("id"));
    appointment.setMemberId(rs.getObject("member_id", Integer.class));
    appointment.setCaregiverId(rs.getInt("caregiver_id"));

    // 時間欄位處理
    Timestamp scheduledAt = rs.getTimestamp("scheduled_at");
    if (scheduledAt != null) {
      appointment.setScheduledAt(scheduledAt.toLocalDateTime());
    }

    Timestamp endTime = rs.getTimestamp("end_time");
    if (endTime != null) {
      appointment.setEndTime(endTime.toLocalDateTime());
    }

    // 狀態與服務相關欄位
    appointment.setStatus(rs.getString("status"));
    appointment.setIsBlocked(rs.getBoolean("is_blocked"));
    appointment.setServiceTypeId(rs.getObject("service_type_id", Integer.class));
    appointment.setServiceLocation(rs.getString("service_location"));
    appointment.setTotalAmount(rs.getBigDecimal("total_amount"));
    appointment.setNotes(rs.getString("notes"));
    appointment.setBlockType(rs.getString("block_type"));

    // 評分資訊
    appointment.setRatingScore(rs.getObject("rating_score", Integer.class));
    appointment.setRatingComment(rs.getString("rating_comment"));

    Timestamp ratedAt = rs.getTimestamp("rated_at");
    if (ratedAt != null) {
      appointment.setRatedAt(ratedAt.toLocalDateTime());
    }

    appointment.setIsRated(rs.getBoolean("is_rated"));

    // 系統時間戳記
    Timestamp createdAt = rs.getTimestamp("created_at");
    if (createdAt != null) {
      appointment.setCreatedAt(createdAt.toLocalDateTime());
    }

    Timestamp cancelledAt = rs.getTimestamp("cancelled_at");
    if (cancelledAt != null) {
      appointment.setCancelledAt(cancelledAt.toLocalDateTime());
    }

    return appointment;
  }
}