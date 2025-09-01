package com.ryanshiun.seniorscare.roomType.dao;

import java.time.LocalDate;
import java.util.List;

import com.ryanshiun.seniorscare.roomType.dto.ReservationDetail;
import com.ryanshiun.seniorscare.roomType.dto.ReservationStats;
import com.ryanshiun.seniorscare.roomType.dto.RoomTypeRanking;
import com.ryanshiun.seniorscare.roomType.model.Reservation;

public interface ReservationDao {

    // 基本 CRUD
    int addReservation(Reservation r);
    boolean updateReservation(Reservation r);
    boolean deleteReservation(int reservationId);
    Reservation getReservationById(int reservationId);
    List<Reservation> getAllReservations();

    // 條件查詢
    List<Reservation> getByMember(Integer memberId);            // memberId 可為 null
    List<Reservation> getByRoomType(Integer roomTypeId);        // roomTypeId 可為 null
    List<Reservation> getByPreferredDate(LocalDate date);
    List<Reservation> getByDateRange(LocalDate from, LocalDate to);
    List<Reservation> getByStatus(int status);

    // 統計
    long countOnDate(LocalDate date);
    long countByRoomType(Integer roomTypeId);

    // 趨勢（以 preferred_date 為準）
    List<ReservationStats> getDailyTrend(int days);
    List<ReservationStats> getMonthlyTrend(int months);

    // 房型排行（忽略 roomtype_id 為 NULL）
    List<RoomTypeRanking> getRoomTypeRanking(int topN);
    ReservationDetail getDetailById(int reservationId);
}