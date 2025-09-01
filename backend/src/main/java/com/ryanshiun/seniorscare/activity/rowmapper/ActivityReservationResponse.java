package com.ryanshiun.seniorscare.activity.rowmapper;

import com.ryanshiun.seniorscare.activity.dto.ReservationResponse;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;


public class ActivityReservationResponse implements RowMapper<ReservationResponse> {

    @Override
    public ReservationResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
        ReservationResponse activityReservationResponse = new ReservationResponse();
        activityReservationResponse.setActivityId(rs.getInt("activityId"));
        activityReservationResponse.setMemberId(rs.getInt("memberId"));
        activityReservationResponse.setActivityName(rs.getString("activityName"));
        activityReservationResponse.setActivityCategory(rs.getString("activityCategory"));
        activityReservationResponse.setMemberName(rs.getString("memberName"));
        activityReservationResponse.setPhone(rs.getString("memberPhone"));
        activityReservationResponse.setPeople(rs.getInt("people"));
        activityReservationResponse.setScheduledAt(rs.getTimestamp("scheduledAt").toLocalDateTime());
        activityReservationResponse.setStatus(rs.getString("status"));
        return activityReservationResponse;
    }
}
