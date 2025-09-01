package com.ryanshiun.seniorscare.roomType.service;

import java.time.LocalDate;
import java.util.List;

import com.ryanshiun.seniorscare.roomType.dto.ReservationDetail;
import com.ryanshiun.seniorscare.roomType.dto.ReservationStats;
import com.ryanshiun.seniorscare.roomType.dto.RoomTypeRanking;
import com.ryanshiun.seniorscare.roomType.model.Reservation;
public interface ReservationService {

    // ===== CRUD =====
    int addReservation(Reservation r);
    boolean updateReservation(Reservation r);
    boolean deleteReservation(int reservationId);
    Reservation getReservationById(int reservationId);
    List<Reservation> getAllReservations();

    // ===== 查詢 =====
    List<Reservation> getByMember(Integer memberId);
    List<Reservation> getByRoomType(Integer roomTypeId);
    List<Reservation> getByPreferredDate(LocalDate date);
    List<Reservation> getByDateRange(LocalDate from, LocalDate to);
    List<Reservation> getByStatus(int status);

    // ===== 統計 =====
    long countOnDate(LocalDate date);
    long countByRoomType(Integer roomTypeId);

    // ===== 趨勢 =====
    List<ReservationStats> getDailyTrend(int days);
    List<ReservationStats> getMonthlyTrend(int months);

    // ===== 房型排行 =====
    List<RoomTypeRanking> getRoomTypeRanking(int topN);
    
    ReservationDetail getDetailById(int reservationId);
    
    boolean updateStatus(int reservationId, int status, String note);
}