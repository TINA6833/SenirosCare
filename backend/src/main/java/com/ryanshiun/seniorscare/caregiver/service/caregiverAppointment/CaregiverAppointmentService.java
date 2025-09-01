package com.ryanshiun.seniorscare.caregiver.service.caregiverAppointment;

import com.ryanshiun.seniorscare.caregiver.dto.caregiverAppointment.AppointmentRatingDto;
import com.ryanshiun.seniorscare.caregiver.dto.caregiverAppointment.CaregiverAppointmentCreateDto;
import com.ryanshiun.seniorscare.caregiver.dto.caregiverAppointment.CaregiverAppointmentQueryDto;
import com.ryanshiun.seniorscare.caregiver.dto.caregiverAppointment.CaregiverAppointmentUpdateDto;
import com.ryanshiun.seniorscare.caregiver.model.CaregiverAppointment;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface CaregiverAppointmentService {

  // 建立預約
  Integer createAppointment(CaregiverAppointmentCreateDto createDto);

  // 取得預約詳情
  CaregiverAppointment getAppointmentById(Integer id);

  // 查詢預約列表 (支援分頁)
  Map<String, Object> getAppointments(CaregiverAppointmentQueryDto queryDto);

  // 更新預約
  boolean updateAppointment(Integer id, CaregiverAppointmentUpdateDto updateDto);

  // 取消預約
  boolean cancelAppointment(Integer id);

  // 新增評分
  boolean addRating(Integer id, AppointmentRatingDto ratingDto);

  // 檢查時間衝突
  boolean hasTimeConflict(
      Integer caregiverId, LocalDateTime startTime, LocalDateTime endTime, Integer excludeId);

  // 取得照服員行程
  List<CaregiverAppointment> getCaregiverSchedule(
      Integer caregiverId, LocalDateTime startDate, LocalDateTime endDate);

  // 取得會員預約記錄
  List<CaregiverAppointment> getMemberAppointments(Integer memberId, String status);

  // 取得待評分預約
  List<CaregiverAppointment> getPendingRatings(Integer memberId);

  // 完成預約
  boolean completeAppointment(Integer id);

  // 驗證預約資料
  void validateAppointment(CaregiverAppointmentCreateDto createDto);

  // 更新預約狀態
  boolean updateAppointmentStatus(Integer id, String status);

  // 更新訂單備註
  Integer updateAppointmentNotes(Integer id, String notes);

  // 取得訂單統計資料
  Map<String, Object> getAppointmentStatistics();

  // 批量操作訂單狀態
  Integer batchUpdateAppointmentStatus(List<Integer> ids, String status);

  // 檢查會員是否有權限操作預約
  boolean checkMemberPermission(Integer appointmentId, Integer memberId);

  // 取得會員可執行的操作
  List<String> getAvailableActionsForMember(Integer appointmentId, Integer memberId);

  // ========== 🔧 新增方法 ==========

  /**
   * 預檢查時間是否衝突（用於會員預約前的驗證）
   * @param caregiverId 照服員ID
   * @param startTime 開始時間
   * @param endTime 結束時間
   * @return 時間衝突檢查結果
   */
  Map<String, Object> preCheckTimeAvailability(Integer caregiverId, LocalDateTime startTime, LocalDateTime endTime);

  /**
   * 取得照服員的可用時間段
   * @param caregiverId 照服員ID
   * @param date 查詢日期
   * @return 可用時間段列表
   */
  List<Map<String, Object>> getAvailableTimeSlots(Integer caregiverId, LocalDate date);

  /**
   * 取得預約數量（根據查詢條件）
   * @param queryDto 查詢條件
   * @return 預約數量
   */
  Integer getAppointmentCount(CaregiverAppointmentQueryDto queryDto);

  /**
   * 🆕 計算預約價格預覽（不建立實際預約）
   * @param serviceTypeId 服務類型ID
   * @param startTime 開始時間
   * @param endTime 結束時間
   * @return 包含價格資訊的Map
   */
  Map<String, Object> calculateAppointmentPrice(Integer serviceTypeId, LocalDateTime startTime, LocalDateTime endTime);
}