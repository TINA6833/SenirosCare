package com.ryanshiun.seniorscare.bus.dao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.ryanshiun.seniorscare.bus.dto.ResCreateRequest;
import com.ryanshiun.seniorscare.bus.dto.ResRequest;
import com.ryanshiun.seniorscare.bus.model.BusReservation;

/**
 * Data Access Object（資料存取物件），一個「專門負責與資料庫互動的方法集合」的類別 它的主要用途是： 操作資料庫（CRUD）、查資料 /
 * 存資料、封裝 SQL、隱藏資料表結構，讓 Service 不用直接寫 SQL
 *
 */
public interface ReservationDAO {

	// 新增預約表單
	Integer insertRes(ResCreateRequest resCreateRequest, LocalDateTime createdAt, LocalDateTime scheduledAt,
			String reservationStatus, Integer distanceMeters, Integer price);

	// 刪除預約表單
	Integer deleteRes(int id);

	// 修改預約表單
	BusReservation updateRes(ResRequest resRequest, Integer distanceMeters, Integer price);

	// 查詢所有預約表單
	List<BusReservation> findAllRes();

	// 查詢預約表單(根據ID)
	BusReservation findById(int id);

	// 查詢預約表單(根據指定地點、預約時間、會員ID)
	List<BusReservation> findByFilter(Integer memberId, String startAddress, String endAddress, LocalDateTime from,

			LocalDateTime to);


	// 已完乘，自動放入時間
	int markCompleted(int id, LocalDateTime completedAt);

	Map<String, Object> findViewById(int id);

	// 查詢是否有「同一台車、同一日的時段重疊」的紀錄
	boolean hasConflict(Integer busId, LocalDateTime scheduledAt, int slotMinutes);

}
