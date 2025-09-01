package com.ryanshiun.seniorscare.bus.rowmapper;

import java.sql.ResultSet;

import java.sql.SQLException;
import java.time.LocalDateTime;

import org.springframework.jdbc.core.RowMapper;

import com.ryanshiun.seniorscare.bus.model.BusReservation;

/**
 * 用來把「資料庫查詢結果的每一列」轉成 Java 的 Reservation 物件
 */

public class ReservationRowMapper implements RowMapper<BusReservation> {

	@Override
	public BusReservation mapRow(ResultSet rSet, int rowNum) throws SQLException {

		BusReservation reservation = new BusReservation();
		reservation.setId(rSet.getInt("id"));
		reservation.setMemberId(rSet.getInt("member_id"));
		reservation.setBusId(rSet.getInt("bus_id"));
		reservation.setStartZone(rSet.getObject("start_zone_id", Integer.class));
		reservation.setEndZone(rSet.getObject("end_zone_id", Integer.class));
		reservation.setCreateAt(rSet.getObject("created_at", LocalDateTime.class));
		reservation.setScheduledAt(rSet.getObject("scheduled_at", LocalDateTime.class));

		LocalDateTime completedAt = rSet.getObject("completed_at", LocalDateTime.class);
		reservation.setCompletedAt(completedAt);

		reservation.setPrice(rSet.getInt("price"));
		reservation.setReservationStatus(rSet.getString("reservation_status"));
		reservation.setNote(rSet.getString("note"));

		reservation.setStartAddress(rSet.getString("start_address"));
		reservation.setEndAddress(rSet.getString("end_address"));
		reservation.setStartLat(rSet.getBigDecimal("start_lat"));
		reservation.setStartLng(rSet.getBigDecimal("start_lng"));
		reservation.setEndLat(rSet.getBigDecimal("end_lat"));
		reservation.setEndLng(rSet.getBigDecimal("end_lng"));
		reservation.setDistanceMeters((Integer) rSet.getObject("distance_meters"));

		return reservation;

	}
}
