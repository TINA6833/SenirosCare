package com.ryanshiun.seniorscare.activity.service.Impl;

import com.ryanshiun.seniorscare.activity.dao.ActivityReservationDao;
import com.ryanshiun.seniorscare.activity.dto.ReservationCreateDto;
import com.ryanshiun.seniorscare.activity.dto.ReservationQueryDto;
import com.ryanshiun.seniorscare.activity.dto.ReservationResponse;
import com.ryanshiun.seniorscare.activity.dto.ReservationUpdateDto;
import com.ryanshiun.seniorscare.activity.model.ActivityRegistration;
import com.ryanshiun.seniorscare.activity.service.ActivityReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class ActivityReservationServiceImpl implements ActivityReservationService {

    @Autowired
    private ActivityReservationDao activityReservationDao;

    @Override
    public List<ActivityRegistration> getReservationsByMemberId(Integer memberId) {
        return activityReservationDao.getMyReservations(memberId);
    }

    @Override
    public boolean createReservation(ReservationCreateDto registration) {
       return activityReservationDao.createReservation(registration);
    }

    @Override
    public boolean cancelReservation(Integer registrationId, Integer memberId) {
        return activityReservationDao.cancelReservation(registrationId, memberId);
    }

    @Override
    public boolean reviewReservation(ReservationUpdateDto reservationUpdateDto) {
        return activityReservationDao.reviewReservation(reservationUpdateDto);
    }

    @Override
    public List<ReservationResponse> getAllReservations(ReservationQueryDto queryDto) {
        return activityReservationDao.getAllReservations(queryDto);
    }
}
