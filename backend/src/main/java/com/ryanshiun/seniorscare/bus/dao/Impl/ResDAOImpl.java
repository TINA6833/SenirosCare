package com.ryanshiun.seniorscare.bus.dao.Impl;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.ryanshiun.seniorscare.bus.dao.ReservationDAO;
import com.ryanshiun.seniorscare.bus.dto.ResCreateRequest;
import com.ryanshiun.seniorscare.bus.dto.ResRequest;
import com.ryanshiun.seniorscare.bus.model.BusReservation;
import com.ryanshiun.seniorscare.bus.rowmapper.ReservationRowMapper;

/**
 * ResDAOImpl = DAO 的「具體做事者」 ResDAOImpl（實作）：把介面上定義的方法，用實際的 SQL +
 * NamedParameterJdbcTemplate／RowMapper 等，具體實作出來
 */

@Repository
public class ResDAOImpl implements ReservationDAO {

	@Autowired
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
	// 取得 Spring 幫你配置好的 JDBC 工具

	// 新增預約表單
	@Override
	public Integer insertRes(ResCreateRequest resCreateRequest, LocalDateTime createdAt, LocalDateTime scheduledAt,
			String reservationStatus, Integer distanceMeters, Integer price) {
		final String sql = "INSERT INTO BusReservation (member_id, bus_id, "
				+ "start_address, end_address, start_lat, start_lng, end_lat, "
				+ "end_lng, distance_meters, created_at, scheduled_at, price, "
				+ "reservation_status, note) VALUES (:memberId, :busId, "
				+ ":startAddress, :endAddress, :startLat, :startLng, :endLat, :endLng, "
				+ ":distanceMeters, :createAt, :scheduledAt, :price, :reservationStatus, :note)";

		Map<String, Object> iRes = new HashMap<>();
		iRes.put("memberId", resCreateRequest.getMemberId());
		iRes.put("busId", resCreateRequest.getBusId());
		iRes.put("startAddress", resCreateRequest.getStartAddress());
		iRes.put("endAddress", resCreateRequest.getEndAddress());
		iRes.put("startLat", resCreateRequest.getStartLat());
		iRes.put("startLng", resCreateRequest.getStartLng());
		iRes.put("endLat", resCreateRequest.getEndLat());
		iRes.put("endLng", resCreateRequest.getEndLng());
		iRes.put("distanceMeters", distanceMeters);
		iRes.put("createAt", createdAt);
		iRes.put("scheduledAt", scheduledAt);
		iRes.put("price", price);
		iRes.put("reservationStatus", reservationStatus);
		iRes.put("note", resCreateRequest.getNote());

		KeyHolder keyHolder = new GeneratedKeyHolder();
		// 呼叫它的建構子 new GeneratedKeyHolder()，就會得到一個空的 KeyHolder 實例
		namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource(iRes), keyHolder);

		Number key = keyHolder.getKey();
		if (key != null) {
			return key.intValue();
		} else {
			return null;
		}

	}

	// 刪除預約表單
	@Override
	public Integer deleteRes(int id) {
		final String sql = "DELETE FROM BusReservation WHERE id = :id";
		Map<String, Object> dRes = new HashMap<>();
		dRes.put("id", id);

		return namedParameterJdbcTemplate.update(sql, dRes);
	}

	// 修改預約表單
	@Override
	public BusReservation updateRes(ResRequest resRequest, Integer distanceMeters, Integer price) {

		final String sql = "UPDATE BusReservation SET  " + "bus_id = :busId, "

				+ "start_address = :startAddress, end_address = :endAddress, start_lat = :startLat, "
				+ "start_lng = :startLng, end_lat = :endLat, end_lng = :endLng, distance_meters = COALESCE(:distanceMeters, distance_meters), "
				+ "scheduled_at = :scheduledAt, price = COALESCE(:price, price), "
				+ "reservation_status = :reservationStatus, note = :note WHERE id = :id";

		Map<String, Object> uRes = new HashMap<>();
		uRes.put("id", resRequest.getId());
		uRes.put("busId", resRequest.getBusId());
		uRes.put("startAddress", resRequest.getStartAddress());
		uRes.put("endAddress", resRequest.getEndAddress());
		uRes.put("startLat", resRequest.getStartLat());
		uRes.put("startLng", resRequest.getStartLng());
		uRes.put("endLat", resRequest.getEndLat());
		uRes.put("endLng", resRequest.getEndLng());
		uRes.put("distanceMeters", distanceMeters);
		uRes.put("scheduledAt", resRequest.getScheduledAt());
		uRes.put("price", price);
		uRes.put("reservationStatus", resRequest.getReservationStatus());
		uRes.put("note", resRequest.getNote());

		namedParameterJdbcTemplate.update(sql, uRes);

		final String sql1 = "SELECT * FROM BusReservation WHERE id = :id";
		return namedParameterJdbcTemplate.queryForObject(sql1, Map.of("id", resRequest.getId()),
				new ReservationRowMapper());

	}

	// 查詢所有預約表單
	@Override
	public List<BusReservation> findAllRes() {

		final String sql = "SELECT * FROM BusReservation ";

		Map<String, Object> faRes = Collections.emptyMap();

		return namedParameterJdbcTemplate.query(sql, faRes, new ReservationRowMapper());
	}

	// 查詢預約表單(根據ID)
	@Override
	public BusReservation findById(int id) {

		final String sql = "SELECT * FROM BusReservation WHERE id = :id";


		Map<String, Object> fiRes = new HashMap<>();
		fiRes.put("id", id);

		List<BusReservation> resList = namedParameterJdbcTemplate.query(sql, fiRes, new ReservationRowMapper());
		if (!resList.isEmpty()) {
			return resList.get(0);
			// 如果有資料就回傳第一筆，沒有則回傳 null
		} else {
			return null;
		}
	}

	// 查詢預約表單(根據指定地點、預約時間、會員ID)
	@Override
	public List<BusReservation> findByFilter(Integer memberId, String startAddress, String endAddress, LocalDateTime from,

			LocalDateTime to) {


		StringBuilder sql = new StringBuilder("SELECT * FROM BusReservation WHERE 1=1 ");
		MapSqlParameterSource mspSource = new MapSqlParameterSource();

		if (memberId != null) {
			sql.append(" AND member_id = :memberId");
			mspSource.addValue("memberId", memberId);
		}

		// 起點地址關鍵字
		if (startAddress != null && !startAddress.isBlank()) {
			sql.append(" AND start_address LIKE :startAddress");
			mspSource.addValue("startAddress", "%" + startAddress.trim() + "%");
		}

		// 起點地址關鍵字
		if (endAddress != null && !endAddress.isBlank()) {
			sql.append(" AND end_address LIKE :endAddress");
			mspSource.addValue("endAddress", "%" + endAddress.trim() + "%");
		}

		if (from != null) {
			sql.append(" AND scheduled_at >= :from");
			mspSource.addValue("from", from);
		}

		if (to != null) {
			sql.append(" AND scheduled_at < :to");
			mspSource.addValue("to", to);
		}

		return namedParameterJdbcTemplate.query(sql.toString(), mspSource, new ReservationRowMapper());

	}

	// 已完乘，自動放入時間
	@Override
	public int markCompleted(int id, LocalDateTime completedAt) {
		final String sql = "UPDATE BusReservation SET completed_at = CASE "
				+ " WHEN :completedAt < scheduled_at THEN scheduled_at " + " ELSE :completedAt END, "
				+ " reservation_status =  N'completed' WHERE id = :id";

		return namedParameterJdbcTemplate.update(sql, Map.of("id", id, "completedAt", completedAt));

	}

	@Override
	public Map<String, Object> findViewById(int id) {
		final String sql = "SELECT " + "r.id AS ID, " + "r.member_id AS MEMBER_ID, " + "r.bus_id AS BUS_ID, "
				+ "r.start_address AS START_ADDRESS, " + "r.end_address AS END_ADDRESS, "
				+ "r.scheduled_at AS SCHEDULED_AT, " + "r.completed_at AS COMPLETED_AT, "
				+ "r.distance_meters AS DISTANCE_METERS, " + "r.price AS PRICE, "
				+ "LOWER(r.reservation_status) AS RESERVATION_STATUS, " + "r.note AS NOTE, "
				+ "r.start_lat AS START_LAT, " + "r.start_lng AS START_LNG, " + "r.end_lat AS END_LAT, "
				+ "r.end_lng AS END_LNG, " + "r.created_at AS CREATED_AT " + "FROM BusReservation r WHERE r.id = :id";

		return namedParameterJdbcTemplate.queryForObject(sql, Map.of("id", id), (rs, rowNum) -> {
			Map<String, Object> map = new LinkedHashMap<>();

			map.put("ID", rs.getInt("ID"));
			map.put("MEMBER_ID", rs.getInt("MEMBER_ID"));
			map.put("BUS_ID", rs.getInt("BUS_ID"));
			map.put("START_ADDRESS", rs.getString("START_ADDRESS"));
			map.put("END_ADDRESS", rs.getString("END_ADDRESS"));
			map.put("SCHEDULED_AT", rs.getObject("SCHEDULED_AT", java.time.LocalDateTime.class));
			map.put("COMPLETED_AT", rs.getObject("COMPLETED_AT", java.time.LocalDateTime.class));
			map.put("DISTANCE_METERS", (Object) rs.getObject("DISTANCE_METERS")); // 可能為 null
			map.put("PRICE", (Object) rs.getObject("PRICE"));
			map.put("RESERVATION_STATUS", rs.getString("RESERVATION_STATUS"));
			map.put("NOTE", rs.getString("NOTE"));
			map.put("START_LAT", (Object) rs.getObject("START_LAT"));
			map.put("START_LNG", (Object) rs.getObject("START_LNG"));
			map.put("END_LAT", (Object) rs.getObject("END_LAT"));
			map.put("END_LNG", (Object) rs.getObject("END_LNG"));
			map.put("CREATED_AT", rs.getObject("CREATED_AT", java.time.LocalDateTime.class));
			return map;
		});
	}

	// 查詢是否有「同一台車、同一日的時段重疊」的紀錄
	@Override
	public boolean hasConflict(Integer busId, LocalDateTime scheduledAt, int slotMinutes) {
		String sql = "SELECT COUNT(*) FROM BusReservation r " + "WHERE r.bus_id = :busId "
				+ "AND r.reservation_status = N'Active' "
				+ "AND r.scheduled_at < DATEADD(MINUTE, :slotMinutes, :scheduledAt) "
				+ "AND DATEADD(MINUTE, :slotMinutes, r.scheduled_at) > :scheduledAt ";

		MapSqlParameterSource qRes = new MapSqlParameterSource().addValue("busId", busId)
				.addValue("scheduledAt", scheduledAt).addValue("slotMinutes", slotMinutes);

		Integer count = namedParameterJdbcTemplate.queryForObject(sql, qRes, Integer.class);
		return count != null && count > 0;
	}
}