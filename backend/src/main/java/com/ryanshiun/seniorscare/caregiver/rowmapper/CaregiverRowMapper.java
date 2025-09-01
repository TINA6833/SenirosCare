package com.ryanshiun.seniorscare.caregiver.rowmapper;

import com.ryanshiun.seniorscare.caregiver.model.Caregiver;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

@Component
public class CaregiverRowMapper implements RowMapper<Caregiver> {

    @Override
    public Caregiver mapRow(ResultSet rs, int rowNum) throws SQLException {
        Caregiver caregiver = new Caregiver();

        caregiver.setCaregiverId(rs.getInt("caregiver_id"));
        caregiver.setChineseName(rs.getString("chinese_name"));
        caregiver.setGender(rs.getBoolean("gender"));
        caregiver.setPhone(rs.getString("phone"));
        caregiver.setEmail(rs.getString("email"));
        caregiver.setExperienceYears(rs.getInt("experience_years"));
        caregiver.setPhoto(rs.getString("photo"));
        caregiver.setAddress(rs.getString("address"));
        caregiver.setServiceArea(rs.getString("service_area"));
        caregiver.setAverageRating(rs.getBigDecimal("average_rating"));
        caregiver.setTotalRatings(rs.getInt("total_ratings"));
        caregiver.setTotalPoints(rs.getInt("total_points"));
        caregiver.setIsActive(rs.getBoolean("is_active"));
        caregiver.setSelfIntroduction(rs.getString("self_introduction"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            caregiver.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            caregiver.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return caregiver;
    }
}