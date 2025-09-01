package com.ryanshiun.seniorscare.activity.controller;

import com.ryanshiun.seniorscare.activity.dto.ReservationCreateDto;
import com.ryanshiun.seniorscare.activity.dto.ReservationQueryDto;
import com.ryanshiun.seniorscare.activity.dto.ReservationResponse;
import com.ryanshiun.seniorscare.activity.dto.ReservationUpdateDto;
import com.ryanshiun.seniorscare.activity.model.ActivityRegistration;
import com.ryanshiun.seniorscare.activity.service.ActivityReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activities/reservation")
public class ActivityReservationController {

    @Autowired
    private ActivityReservationService activityReservationService;

    /** 查詢自己的預約
     * @return 自己的預約清單
     */
    @GetMapping("/me")
    public ResponseEntity<List<ActivityRegistration>> getMyReservations(Authentication authentication) {
        Integer memberId = Integer.parseInt(authentication.getName());
        List<ActivityRegistration> myReservation = activityReservationService.getReservationsByMemberId(memberId);
        if (myReservation != null) {
            return ResponseEntity.status(200).body(myReservation);
        } else {
            return ResponseEntity.status(404).build();
        }
    }

    /** 預約活動
        * @param registration 預約資料
     */
    @PostMapping
    public ResponseEntity<?> createReservation(
            @RequestBody ReservationCreateDto registration,
            Authentication authentication) {
        Integer memberId = Integer.parseInt(authentication.getName());
        registration.setMemberId(memberId);
        boolean success = activityReservationService.createReservation(registration);
        if (success) {
            return ResponseEntity.status(201).build();
        } else {
            return ResponseEntity.status(400).build();
        }
    }

    /** 取消預約
     * @param registrationId 預約ID
     */
    @DeleteMapping("/{registrationId}")
    public ResponseEntity<?> cancelReservation(
            @PathVariable Integer registrationId,
            Authentication authentication) {
        Integer memberId = Integer.parseInt(authentication.getName());
        boolean success = activityReservationService.cancelReservation(registrationId, memberId);
        if (success) {
            return ResponseEntity.status(200).build();
        } else {
            return ResponseEntity.status(400).build();
        }
    }

    /* =================================================
                    下面皆為後台員工使用
       ================================================= */


    /** 審核預約 (拒絕或允許報名)
     * @param reservationUpdateDto 預約更新資料
     */
    @PutMapping("/status")
    public ResponseEntity<?> reviewReservation(@RequestBody ReservationUpdateDto reservationUpdateDto) {
        boolean success = activityReservationService.reviewReservation(reservationUpdateDto);
        if (success) {
            return ResponseEntity.status(200).build();
        } else {
            return ResponseEntity.status(400).build();
        }
    }

    /** 客製化查詢活動預約
     * 不傳參數回傳全部預約
     *
     */
    @GetMapping
    public ResponseEntity<List<ReservationResponse>> getAllReservation(
            // 查詢條件
            @RequestParam(required = false) String CategoryName,
            @RequestParam(required = false) String activityName,
            @RequestParam(required = false) Integer memberId) {
        ReservationQueryDto queryDto = new ReservationQueryDto();
        queryDto.setCategoryName(CategoryName);
        queryDto.setActivityName(activityName);
        queryDto.setMemberId(memberId);
        List<ReservationResponse> reservations = activityReservationService.getAllReservations(queryDto);
        if (reservations != null) {
            return ResponseEntity.status(200).body(reservations);
        } else {
            return ResponseEntity.status(404).build();
        }
    }
}
