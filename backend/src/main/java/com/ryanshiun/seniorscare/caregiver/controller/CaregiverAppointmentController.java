package com.ryanshiun.seniorscare.caregiver.controller;

import com.ryanshiun.seniorscare.caregiver.dto.caregiverAppointment.CaregiverAppointmentQueryDto;
import com.ryanshiun.seniorscare.caregiver.dto.caregiverAppointment.CaregiverAppointmentUpdateDto;
import com.ryanshiun.seniorscare.caregiver.model.CaregiverAppointment;
import com.ryanshiun.seniorscare.caregiver.service.caregiverAppointment.CaregiverAppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 照服員專屬預約功能控制器
 * 重構後只保留照服員專屬的功能，移除與其他Controller重複的功能
 * 路徑: /api/caregiver
 */
@RestController
@RequestMapping("/api/caregiver")
public class CaregiverAppointmentController {

  @Autowired
  private CaregiverAppointmentService appointmentService;

  // ========== 照服員專屬功能（無重複） ==========

  /**
   * 取得照服員行程表
   * GET /api/caregiver/{caregiverId}/schedule
   *
   * 🎯 獨特功能：照服員專屬的行程查看
   */
  @GetMapping("/{caregiverId}/schedule")
  public ResponseEntity<Map<String, Object>> getCaregiverSchedule(
      @PathVariable Integer caregiverId,
      @RequestParam LocalDateTime startDate,
      @RequestParam LocalDateTime endDate) {

    Map<String, Object> response = new HashMap<>();

    try {
      List<CaregiverAppointment> schedule = appointmentService.getCaregiverSchedule(
          caregiverId, startDate, endDate);

      response.put("success", true);
      response.put("schedule", schedule);
      response.put("caregiverId", caregiverId);
      response.put("startDate", startDate);
      response.put("endDate", endDate);
      response.put("totalAppointments", schedule.size());

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      response.put("success", false);
      response.put("message", "查詢照服員行程失敗: " + e.getMessage());
      return ResponseEntity.badRequest().body(response);
    }
  }

  /**
   * 取得特定照服員的預約列表
   * GET /api/caregiver/{caregiverId}/appointments
   *
   * 🎯 獨特功能：照服員專屬的預約列表查看
   */
  @GetMapping("/{caregiverId}/appointments")
  public ResponseEntity<Map<String, Object>> getCaregiverAppointments(
      @PathVariable Integer caregiverId,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) Boolean isBlocked,
      @RequestParam(required = false) LocalDateTime startDate,
      @RequestParam(required = false) LocalDateTime endDate,
      @RequestParam(defaultValue = "20") Integer limit,
      @RequestParam(defaultValue = "0") Integer offset) {

    Map<String, Object> response = new HashMap<>();

    try {
      CaregiverAppointmentQueryDto queryDto = new CaregiverAppointmentQueryDto();
      queryDto.setCaregiverId(caregiverId);
      queryDto.setStatus(status);
      queryDto.setIsBlocked(isBlocked);
      queryDto.setStartDate(startDate);
      queryDto.setEndDate(endDate);
      queryDto.setLimit(limit);
      queryDto.setOffset(offset);

      Map<String, Object> result = appointmentService.getAppointments(queryDto);

      response.put("success", true);
      response.put("caregiverId", caregiverId);
      response.putAll(result);

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      response.put("success", false);
      response.put("message", "查詢照服員預約列表失敗: " + e.getMessage());
      return ResponseEntity.badRequest().body(response);
    }
  }

  /**
   * 檢查照服員時間衝突
   * GET /api/caregiver/{caregiverId}/check-conflict
   *
   * 🎯 獨特功能：照服員專屬的時間衝突檢查
   */
  @GetMapping("/{caregiverId}/check-conflict")
  public ResponseEntity<Map<String, Object>> checkTimeConflict(
      @PathVariable Integer caregiverId,
      @RequestParam LocalDateTime startTime,
      @RequestParam LocalDateTime endTime,
      @RequestParam(required = false) Integer excludeId) {

    Map<String, Object> response = new HashMap<>();

    try {
      boolean hasConflict = appointmentService.hasTimeConflict(caregiverId, startTime, endTime, excludeId);

      response.put("success", true);
      response.put("caregiverId", caregiverId);
      response.put("hasConflict", hasConflict);
      response.put("startTime", startTime);
      response.put("endTime", endTime);
      response.put("message", hasConflict ? "時間衝突" : "時間可用");

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      response.put("success", false);
      response.put("message", "檢查時間衝突失敗: " + e.getMessage());
      return ResponseEntity.badRequest().body(response);
    }
  }

  // ========== 通用功能（可能重複，但保留供特殊用途） ==========

  /**
   * 更新預約資訊
   * PUT /api/caregiver/appointment/{id}
   *
   * 🟡 可能重複：但提供通用的預約更新功能
   * 註：會員和管理員Controller中沒有此功能
   */
  @PutMapping("/appointment/{id}")
  public ResponseEntity<Map<String, Object>> updateAppointment(
      @PathVariable Integer id,
      @RequestBody CaregiverAppointmentUpdateDto updateDto) {

    Map<String, Object> response = new HashMap<>();

    try {
      boolean updated = appointmentService.updateAppointment(id, updateDto);

      response.put("success", updated);
      response.put("message", updated ? "預約更新成功" : "預約更新失敗");

      if (updated) {
        response.put("appointment", appointmentService.getAppointmentById(id));
      }

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      response.put("success", false);
      response.put("message", "更新預約失敗: " + e.getMessage());
      return ResponseEntity.badRequest().body(response);
    }
  }

  /**
   * 通用預約查詢
   * GET /api/caregiver/appointments
   *
   * 🟡 可能重複：但提供不需權限控制的通用查詢
   * 註：與管理員的查詢功能類似，但無權限限制
   */
  @GetMapping("/appointments")
  public ResponseEntity<Map<String, Object>> getAppointments(
      @RequestParam(required = false) Integer memberId,
      @RequestParam(required = false) Integer caregiverId,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) Boolean isBlocked,
      @RequestParam(required = false) LocalDateTime startDate,
      @RequestParam(required = false) LocalDateTime endDate,
      @RequestParam(required = false) Boolean isRated,
      @RequestParam(defaultValue = "20") Integer limit,
      @RequestParam(defaultValue = "0") Integer offset) {

    Map<String, Object> response = new HashMap<>();

    try {
      CaregiverAppointmentQueryDto queryDto = new CaregiverAppointmentQueryDto();
      queryDto.setMemberId(memberId);
      queryDto.setCaregiverId(caregiverId);
      queryDto.setStatus(status);
      queryDto.setIsBlocked(isBlocked);
      queryDto.setStartDate(startDate);
      queryDto.setEndDate(endDate);
      queryDto.setIsRated(isRated);
      queryDto.setLimit(limit);
      queryDto.setOffset(offset);

      Map<String, Object> result = appointmentService.getAppointments(queryDto);

      response.put("success", true);
      response.putAll(result);

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      response.put("success", false);
      response.put("message", "查詢預約失敗: " + e.getMessage());
      return ResponseEntity.badRequest().body(response);
    }
  }
}

