package com.ryanshiun.seniorscare.activity.dao;

import java.util.List;

import com.ryanshiun.seniorscare.activity.dto.ReservationCreateDto;
import com.ryanshiun.seniorscare.activity.dto.ReservationQueryDto;
import com.ryanshiun.seniorscare.activity.dto.ReservationResponse;
import com.ryanshiun.seniorscare.activity.dto.ReservationUpdateDto;
import com.ryanshiun.seniorscare.activity.model.ActivityRegistration;

public interface ActivityReservationDao {

    // 透過會員ID取得該會員的所有活動預約
    List<ActivityRegistration> getMyReservations(Integer memberId);

    boolean createReservation(ReservationCreateDto registration);

    boolean cancelReservation(Integer registrationId, Integer memberId);

    boolean reviewReservation(ReservationUpdateDto reservationUpdateDto);

    List<ReservationResponse> getAllReservations(ReservationQueryDto queryDto);
}
