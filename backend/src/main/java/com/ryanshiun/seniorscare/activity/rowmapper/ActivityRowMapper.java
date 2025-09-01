package com.ryanshiun.seniorscare.activity.rowmapper;

import com.ryanshiun.seniorscare.activity.model.Activity;
import org.springframework.jdbc.core.RowMapper;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class ActivityRowMapper implements RowMapper<Activity> {

    @Override
    public Activity mapRow(ResultSet rs, int rowNum) throws SQLException {
        Activity activity = new Activity();
        activity.setId(rs.getInt("id"));
        activity.setName(rs.getString("name"));
        activity.setCategory(rs.getString("category"));
        activity.setLimit(rs.getInt("limit"));
        activity.setCurrent(rs.getInt("current"));
        
        activity.setDate(rs.getDate("date").toLocalDate());
        activity.setEnd(rs.getDate("end").toLocalDate());

        activity.setTime(rs.getString("time"));
        activity.setRegistrationStart(rs.getDate("registration_start").toLocalDate());
        activity.setRegistrationEnd(rs.getDate("registration_end").toLocalDate());

        activity.setLocation(rs.getString("location"));
        activity.setLatitude(rs.getBigDecimal("latitude"));
        activity.setLongitude(rs.getBigDecimal("longitude"));
        activity.setInstructor(rs.getString("instructor"));
        activity.setStatus(rs.getBoolean("status"));
        activity.setDescription(rs.getString("description"));
        activity.setImage(rs.getString("image"));

        return activity;
    }
}
