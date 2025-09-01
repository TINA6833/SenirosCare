package com.ryanshiun.seniorscare.roomType.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.jdbc.core.RowMapper;

import com.ryanshiun.seniorscare.roomType.model.Reservation;

public class ReservationRowMapper implements RowMapper<Reservation> {
    @Override
    public Reservation mapRow(ResultSet rs, int rowNum) throws SQLException {
        Reservation r = new Reservation();
        r.setReservationId(rs.getInt("reservation_id"));
        int memberId = rs.getInt("member_id");
        r.setMemberId(rs.wasNull() ? null : memberId);  // 可為 NULL
        r.setApplicantName(rs.getString("applicant_name"));
        r.setMainPhone(rs.getString("main_phone"));
        int roomTypeId = rs.getInt("roomtype_id");
        r.setRoomTypeId(rs.wasNull() ? null : roomTypeId); // 可為 NULL
        r.setPartySize(rs.getInt("party_size"));
        r.setPreferredDate(rs.getObject("preferred_date", LocalDate.class));
        r.setTimeFrom(rs.getObject("time_from", LocalTime.class));
        r.setTimeTo(rs.getObject("time_to", LocalTime.class));
        r.setStatus(rs.getInt("status"));
        r.setNote(rs.getString("note"));
        r.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        r.setUpdatedAt(rs.getObject("updated_at", LocalDateTime.class));
        return r;
    }
}
