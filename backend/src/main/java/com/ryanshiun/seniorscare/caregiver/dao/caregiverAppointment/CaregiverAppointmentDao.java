package com.ryanshiun.seniorscare.caregiver.dao.caregiverAppointment;

import com.ryanshiun.seniorscare.caregiver.dto.caregiverAppointment.AppointmentRatingDto;
import com.ryanshiun.seniorscare.caregiver.dto.caregiverAppointment.CaregiverAppointmentCreateDto;
import com.ryanshiun.seniorscare.caregiver.dto.caregiverAppointment.CaregiverAppointmentQueryDto;
import com.ryanshiun.seniorscare.caregiver.dto.caregiverAppointment.CaregiverAppointmentUpdateDto;
import com.ryanshiun.seniorscare.caregiver.model.CaregiverAppointment;

import java.time.LocalDateTime;
import java.util.List;

public interface CaregiverAppointmentDao {

  // 建立預約
  Integer createAppointment(CaregiverAppointmentCreateDto createDto);

  // 根據ID查詢預約
  CaregiverAppointment getAppointmentById(Integer id);

  // 條件查詢預約列表
  List<CaregiverAppointment> getAppointments(CaregiverAppointmentQueryDto queryDto);

  // 取得預約總數 (用於分頁)
  Integer getAppointmentCount(CaregiverAppointmentQueryDto queryDto);

  // 更新預約資訊
  Integer updateAppointment(Integer id, CaregiverAppointmentUpdateDto updateDto);

  // 取消預約
  Integer cancelAppointment(Integer id);

  // 新增評分
  Integer addRating(Integer id, AppointmentRatingDto ratingDto);

  // 檢查時間衝突
  List<CaregiverAppointment> checkTimeConflict(
      Integer caregiverId, LocalDateTime startTime, LocalDateTime endTime, Integer excludeId);

  // 取得照服員特定時間範圍的預約
  List<CaregiverAppointment> getCaregiverAppointments(
      Integer caregiverId, LocalDateTime startDate, LocalDateTime endDate);

  // 取得會員的預約記錄
  List<CaregiverAppointment> getMemberAppointments(Integer memberId, String status);

  // 取得未評分的完成預約
  List<CaregiverAppointment> getUnratedCompletedAppointments(Integer memberId);

  // 更新預約状態
  Integer updateAppointmentStatus(Integer id, String status);

  // 根據狀態取得訂單數量

  Integer getCountByStatus(String status);

  // 根據日期範圍取得訂單數量
  Integer getCountByDateRange(LocalDateTime startDate, LocalDateTime endDate);

  // 檢查已確認預約的時間衝突（不包含 pending、rejected、cancelled 狀態）
  List<CaregiverAppointment> checkTimeConflictForConfirmedAppointments(
      Integer caregiverId, LocalDateTime startTime, LocalDateTime endTime, Integer excludeId);
}
