package com.ryanshiun.seniorscare.activity.rowmapper;


import com.ryanshiun.seniorscare.activity.model.ActivityRegistration;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ActivityRegistrationMapper implements RowMapper<ActivityRegistration> {
    @Override
    public ActivityRegistration mapRow(ResultSet rs, int rowNum) throws SQLException {
        ActivityRegistration activityRegistration = new ActivityRegistration();

        activityRegistration.setActivityId(rs.getInt("activity_id"));
        activityRegistration.setMemberId(rs.getInt("member_id"));
        activityRegistration.setNum(rs.getInt("num"));
        activityRegistration.setScheduledAt(rs.getTimestamp("scheduled_at").toLocalDateTime());
        activityRegistration.setStatus(rs.getString("status"));
        return activityRegistration;
    }
}
