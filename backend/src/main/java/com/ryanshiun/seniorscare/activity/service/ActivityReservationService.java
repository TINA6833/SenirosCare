package com.ryanshiun.seniorscare.activity.service;

import com.ryanshiun.seniorscare.activity.dto.ReservationCreateDto;
import com.ryanshiun.seniorscare.activity.dto.ReservationQueryDto;
import com.ryanshiun.seniorscare.activity.dto.ReservationResponse;
import com.ryanshiun.seniorscare.activity.dto.ReservationUpdateDto;
import com.ryanshiun.seniorscare.activity.model.ActivityRegistration;

import java.util.List;

public interface ActivityReservationService {

    List<ActivityRegistration> getReservationsByMemberId(Integer memberId);

    boolean createReservation(ReservationCreateDto registration);

    boolean cancelReservation(Integer registrationId, Integer memberId);

    boolean reviewReservation(ReservationUpdateDto reservationUpdateDto);

    List<ReservationResponse> getAllReservations(ReservationQueryDto queryDto);
}
