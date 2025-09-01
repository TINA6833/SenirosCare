package com.ryanshiun.seniorscare.caregiver.controller;

import com.ryanshiun.seniorscare.caregiver.dto.caregiverAppointment.AppointmentRatingDto;
import com.ryanshiun.seniorscare.caregiver.dto.caregiverAppointment.CaregiverAppointmentCreateDto;
import com.ryanshiun.seniorscare.caregiver.dto.caregiverAppointment.CaregiverAppointmentQueryDto;
import com.ryanshiun.seniorscare.caregiver.model.CaregiverAppointment;
import com.ryanshiun.seniorscare.caregiver.model.ServiceType;
import com.ryanshiun.seniorscare.caregiver.service.caregiverAppointment.CaregiverAppointmentService;
import com.ryanshiun.seniorscare.caregiver.service.serviceType.ServiceTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * 會員預約控制器
 * 路徑：/api/caregiver/member
 *
 * 功能包含：
 * 1. 會員建立預約（包含時間檢查）
 * 2. 時間可用性檢查
 * 3. 可用時間段查詢
 * 4. 預約管理（查看、取消、評分）
 */
@RestController
@RequestMapping("/api/caregiver/member")
public class MemberAppointmentController {

  @Autowired
  private ServiceTypeService serviceTypeService;
  @Autowired
  private CaregiverAppointmentService appointmentService;

  /**
   * 預約價格計算預覽
   * GET /api/caregiver/member/calculate-price
   *
   * @param serviceTypeId 服務類型ID
   * @param startTime 開始時間
   * @param endTime 結束時間
   * @return 價格計算結果
   */
  @GetMapping("/calculate-price")
  public ResponseEntity<Map<String, Object>> calculateAppointmentPrice(
      @RequestParam Integer serviceTypeId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
      Authentication authentication) {

    Map<String, Object> response = new HashMap<>();

    try {
       Integer memberId = Integer.parseInt(authentication.getName());

      if (memberId == null) {
        response.put("success", false);
        response.put("message", "請先登入");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
      }

      // 🔧 使用 appointmentService 的計算方法
      Map<String, Object> priceInfo = appointmentService.calculateAppointmentPrice(
          serviceTypeId, startTime, endTime);

      // 加入會員資訊
      priceInfo.put("memberId", memberId);

      return ResponseEntity.ok(priceInfo);

    } catch (Exception e) {
      response.put("success", false);
      response.put("message", "計算價格失敗：" + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  /**
   * 取得所有可用的服務類型（供會員選擇）
   * GET /api/caregiver/member/service-types
   */
  @GetMapping("/service-types")
  public ResponseEntity<Map<String, Object>> getAvailableServiceTypes(Authentication authentication) {
    Map<String, Object> response = new HashMap<>();

    try {
       Integer memberId = Integer.parseInt(authentication.getName());

      if (memberId == null) {
        response.put("success", false);
        response.put("message", "請先登入");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
      }

      List<ServiceType> serviceTypes = serviceTypeService.getAllActiveServiceTypes();

      response.put("success", true);
      response.put("serviceTypes", serviceTypes);
      response.put("total", serviceTypes.size());
      response.put("message", "查詢成功");

      return ResponseEntity.ok(response);

    } catch (Exception e) {
      response.put("success", false);
      response.put("message", "查詢服務類型失敗：" + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }
  // ========== 時間檢查相關功能 ==========

  /**
   * 檢查預約時間是否可用
   * GET /api/caregiver/member/check-availability
   *
   * @param caregiverId 照服員ID
   * @param startTime 開始時間
   * @param endTime 結束時間
   * @return 時間可用性檢查結果
   */
  @GetMapping("/check-availability")
  public ResponseEntity<Map<String, Object>> checkTimeAvailability(
      @RequestParam Integer caregiverId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {

    Map<String, Object> response = new HashMap<>();

    try {
      // 檢查時間可用性
      Map<String, Object> availabilityResult = appointmentService.preCheckTimeAvailability(
          caregiverId, startTime, endTime);

      response.put("success", true);
      response.put("caregiverId", caregiverId);
      response.put("requestedStartTime", startTime);
      response.put("requestedEndTime", endTime);
      response.putAll(availabilityResult);

      return ResponseEntity.ok(response);

    } catch (Exception e) {
      response.put("success", false);
      response.put("available", false);
      response.put("message", "檢查時間可用性失敗：" + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  /**
   * 取得照服員指定日期的可用時間段
   * GET /api/caregiver/member/available-slots
   *
   * @param caregiverId 照服員ID
   * @param date 查詢日期
   * @return 可用時間段列表
   */
  @GetMapping("/available-slots")
  public ResponseEntity<Map<String, Object>> getAvailableTimeSlots(
      @RequestParam Integer caregiverId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

    Map<String, Object> response = new HashMap<>();

    try {
      // 檢查日期是否為過去
      if (date.isBefore(LocalDate.now())) {
        response.put("success", false);
        response.put("message", "不能查詢過去的日期");
        return ResponseEntity.badRequest().body(response);
      }

      // 檢查日期是否超出允許範圍（例如：只能預約未來30天內）
      if (date.isAfter(LocalDate.now().plusDays(30))) {
        response.put("success", false);
        response.put("message", "只能預約未來30天內的時間");
        return ResponseEntity.badRequest().body(response);
      }

      List<Map<String, Object>> availableSlots = appointmentService.getAvailableTimeSlots(caregiverId, date);

      response.put("success", true);
      response.put("caregiverId", caregiverId);
      response.put("date", date);
      response.put("availableSlots", availableSlots);
      response.put("totalSlots", availableSlots.size());

      return ResponseEntity.ok(response);

    } catch (Exception e) {
      response.put("success", false);
      response.put("message", "取得可用時間段失敗：" + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  // ========== 預約管理功能 ==========

  /**
   * 會員建立新預約（包含時間檢查）
   * POST /api/caregiver/member/appointment
   *
   * @param createDto 預約建立資料
   * @return 建立結果
   */
  @PostMapping("/appointment")
  public ResponseEntity<Map<String, Object>> createAppointment(
      @RequestBody CaregiverAppointmentCreateDto createDto,
      Authentication authentication) {


    Map<String, Object> response = new HashMap<>();

    try {
      Integer memberId = Integer.parseInt(authentication.getName());


      // 🆕 驗證服務類型是否有效
      if (createDto.getServiceTypeId() != null) {
        if (!serviceTypeService.isValidServiceType(createDto.getServiceTypeId())) {
          response.put("success", false);
          response.put("message", "選擇的服務類型無效或已停用");
          return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
      }

      // 🔧 新增：預約前先檢查時間可用性
      Map<String, Object> availabilityCheck = appointmentService.preCheckTimeAvailability(
          createDto.getCaregiverId(),
          createDto.getScheduledAt(),
          createDto.getEndTime());

      Boolean isAvailable = (Boolean) availabilityCheck.get("available");
      if (!Boolean.TRUE.equals(isAvailable)) {
        response.put("success", false);
        response.put("message", "預約失敗：" + availabilityCheck.get("message"));
        response.put("availabilityCheck", availabilityCheck);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
      }

      // 設定會員建立預約的參數
      createDto.setMemberId(memberId);           // 設定會員ID
      createDto.setIsBlocked(false);             // 會員預約：is_blocked = false
      createDto.setStatus("pending");            // 會員預約：status = pending
      createDto.setBlockType(null);              // 會員預約不需要 block_type

      // 在 notes 中記錄建立者資訊
      String originalNotes = createDto.getNotes() != null ? createDto.getNotes() : "";
      String memberInfo = "會員預約 (ID: " + memberId + ")";
      createDto.setNotes(originalNotes.isEmpty() ? memberInfo : originalNotes + " | " + memberInfo);

      // 🆕 如果有服務類型，顯示價格資訊給用戶確認
      if (createDto.getServiceTypeId() != null) {
        Map<String, Object> priceInfo = appointmentService.calculateAppointmentPrice(
            createDto.getServiceTypeId(),
            createDto.getScheduledAt(),
            createDto.getEndTime()
        );
        response.put("priceInfo", priceInfo);
      }

      Integer appointmentId = appointmentService.createAppointment(createDto);

      response.put("success", true);
      response.put("message", "預約申請已送出，請等待審核結果");
      response.put("appointmentId", appointmentId);
      response.put("availabilityCheck", availabilityCheck);

      return ResponseEntity.status(HttpStatus.CREATED).body(response);

    } catch (Exception e) {
      response.put("success", false);
      response.put("message", "建立預約失敗：" + e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
  }

  /**
   * 查看會員的預約記錄
   * GET /api/caregiver/member/appointments
   *
   * @param status 預約狀態篩選
   * @param page 頁碼
   * @param size 每頁數量
   * @return 預約記錄列表
   */
  @GetMapping("/appointments")
  public ResponseEntity<Map<String, Object>> getMemberAppointments(
      @RequestParam(required = false) String status,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      Authentication authentication) {


    Map<String, Object> response = new HashMap<>();

    try {
      Integer memberId = Integer.parseInt(authentication.getName());

      if (memberId == null) {
        response.put("success", false);
        response.put("message", "請先登入");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
      }

      // 建立查詢條件（只查詢該會員的預約）
      CaregiverAppointmentQueryDto queryDto = new CaregiverAppointmentQueryDto();
      queryDto.setMemberId(memberId);
      queryDto.setStatus(status);
      queryDto.setIsBlocked(false); // 只查詢會員建立的預約
      queryDto.setOffset(page * size);
      queryDto.setLimit(size);

      Map<String, Object> result = appointmentService.getAppointments(queryDto);

      response.put("success", true);
      response.put("memberId", memberId);
      response.putAll(result);
      response.put("currentPage", page);

      return ResponseEntity.ok(response);

    } catch (Exception e) {
      response.put("success", false);
      response.put("message", "查詢預約記錄失敗：" + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  /**
   * 查看單一預約詳情
   * GET /api/caregiver/member/appointment/{id}
   *
   * @param id 預約ID
   * @return 預約詳細資訊
   */
  @GetMapping("/appointment/{id}")

  public ResponseEntity<Map<String, Object>> getAppointmentDetail(@PathVariable Integer id, Authentication authentication) {
    Map<String, Object> response = new HashMap<>();

    try {
       Integer memberId = Integer.parseInt(authentication.getName());

      if (memberId == null) {
        response.put("success", false);
        response.put("message", "請先登入");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
      }

      // 檢查權限
      if (!appointmentService.checkMemberPermission(id, memberId)) {
        response.put("success", false);
        response.put("message", "無權限查看此預約");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
      }

      CaregiverAppointment appointment = appointmentService.getAppointmentById(id);

      if (appointment == null) {
        response.put("success", false);
        response.put("message", "預約不存在");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
      }

      response.put("success", true);
      response.put("appointment", appointment);
      response.put("availableActions", appointmentService.getAvailableActionsForMember(id, memberId));

      return ResponseEntity.ok(response);

    } catch (Exception e) {
      response.put("success", false);
      response.put("message", "查詢預約詳情失敗：" + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  /**
   * 取消預約（會員只能取消自己的預約）
   * POST /api/caregiver/member/appointment/{id}/cancel
   *
   * @param id 預約ID
   * @param request 取消原因
   * @return 取消結果
   */
  @PostMapping("/appointment/{id}/cancel")
  public ResponseEntity<Map<String, Object>> cancelAppointment(
      @PathVariable Integer id,

      @RequestBody(required = false) Map<String, String> request,
      Authentication authentication) {

    Map<String, Object> response = new HashMap<>();

    try {
       Integer memberId = Integer.parseInt(authentication.getName());

      if (memberId == null) {
        response.put("success", false);
        response.put("message", "請先登入");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
      }

      // 檢查權限
      if (!appointmentService.checkMemberPermission(id, memberId)) {
        response.put("success", false);
        response.put("message", "無權限取消此預約");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
      }

      CaregiverAppointment appointment = appointmentService.getAppointmentById(id);

      if (appointment == null) {
        response.put("success", false);
        response.put("message", "預約不存在");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
      }

      // 檢查是否可以取消
      if (!canMemberCancelAppointment(appointment)) {
        response.put("success", false);
        response.put("message", "當前狀態不允許取消預約，或距離預約時間不足24小時");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
      }

      boolean cancelled = appointmentService.cancelAppointment(id);

      // 記錄取消原因（如果有）
      if (cancelled && request != null && request.containsKey("reason")) {
        String reason = request.get("reason");
        String cancelNote = "會員取消原因：" + reason;
        appointmentService.updateAppointmentNotes(id, cancelNote);
      }

      if (cancelled) {
        response.put("success", true);
        response.put("message", "預約已取消，您可以重新預約新的時間");
        response.put("appointment", appointmentService.getAppointmentById(id));
      } else {
        response.put("success", false);
        response.put("message", "取消失敗，請稍後再試");
      }

      return ResponseEntity.ok(response);

    } catch (Exception e) {
      response.put("success", false);
      response.put("message", "取消預約失敗：" + e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
  }

  /**
   * 新增評分（已完成的預約）
   * POST /api/caregiver/member/appointment/{id}/rating
   *
   * @param id 預約ID
   * @param ratingDto 評分資料
   * @return 評分結果
   */
  @PostMapping("/appointment/{id}/rating")
  public ResponseEntity<Map<String, Object>> addRating(
      @PathVariable Integer id,

      @RequestBody AppointmentRatingDto ratingDto,
      Authentication authentication) {

    Map<String, Object> response = new HashMap<>();

    try {
       Integer memberId = Integer.parseInt(authentication.getName());

      if (memberId == null) {
        response.put("success", false);
        response.put("message", "請先登入");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
      }

      // 檢查權限
      if (!appointmentService.checkMemberPermission(id, memberId)) {
        response.put("success", false);
        response.put("message", "無權限評分此預約");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
      }

      // 驗證評分資料
      if (ratingDto.getRatingScore() == null ||
          ratingDto.getRatingScore() < 1 ||
          ratingDto.getRatingScore() > 5) {
        response.put("success", false);
        response.put("message", "評分必須在1-5分之間");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
      }

      boolean rated = appointmentService.addRating(id, ratingDto);

      if (rated) {
        response.put("success", true);
        response.put("message", "評分成功，感謝您的反饋");
        response.put("appointment", appointmentService.getAppointmentById(id));
      } else {
        response.put("success", false);
        response.put("message", "評分失敗，請檢查預約狀態");
      }

      return ResponseEntity.ok(response);

    } catch (Exception e) {
      response.put("success", false);
      response.put("message", "評分失敗：" + e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
  }

  /**
   * 取得待評分預約
   * GET /api/caregiver/member/appointments/pending-ratings
   *
   * @return 待評分的預約列表
   */
  @GetMapping("/appointments/pending-ratings")
  public ResponseEntity<Map<String, Object>> getPendingRatings(Authentication authentication) {
    Map<String, Object> response = new HashMap<>();

    try {
       Integer memberId = Integer.parseInt(authentication.getName());

      if (memberId == null) {
        response.put("success", false);
        response.put("message", "請先登入");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
      }

      List<CaregiverAppointment> pendingRatings = appointmentService.getPendingRatings(memberId);

      response.put("success", true);
      response.put("appointments", pendingRatings);
      response.put("count", pendingRatings.size());
      response.put("message", pendingRatings.isEmpty() ? "目前沒有待評分的預約" : "找到 " + pendingRatings.size() + " 筆待評分預約");

      return ResponseEntity.ok(response);

    } catch (Exception e) {
      response.put("success", false);
      response.put("message", "查詢待評分預約失敗：" + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  /**
   * 取得會員預約統計
   * GET /api/caregiver/member/appointments/statistics
   *
   * @return 預約統計資料
   */
  @GetMapping("/appointments/statistics")
  public ResponseEntity<Map<String, Object>> getMemberAppointmentStatistics(Authentication authentication) {
    Map<String, Object> response = new HashMap<>();

    try {
       Integer memberId = Integer.parseInt(authentication.getName());

      if (memberId == null) {
        response.put("success", false);
        response.put("message", "請先登入");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
      }

      // 查詢各狀態的預約數量
      CaregiverAppointmentQueryDto baseQuery = new CaregiverAppointmentQueryDto();
      baseQuery.setMemberId(memberId);
      baseQuery.setIsBlocked(false); // 只統計會員預約

      Map<String, Integer> statistics = new HashMap<>();

      // 各狀態統計
      String[] statuses = {"pending", "approved", "completed", "cancelled", "rejected"};
      for (String status : statuses) {
        CaregiverAppointmentQueryDto statusQuery = new CaregiverAppointmentQueryDto();
        statusQuery.setMemberId(memberId);
        statusQuery.setIsBlocked(false);
        statusQuery.setStatus(status);

        Integer count = appointmentService.getAppointmentCount(statusQuery);
        statistics.put(status + "Count", count);
      }

      // 總數統計
      CaregiverAppointmentQueryDto totalQuery = new CaregiverAppointmentQueryDto();
      totalQuery.setMemberId(memberId);
      totalQuery.setIsBlocked(false);

      Integer totalCount = appointmentService.getAppointmentCount(totalQuery);
      statistics.put("totalCount", totalCount);

      // 待評分數量
      List<CaregiverAppointment> pendingRatings = appointmentService.getPendingRatings(memberId);
      statistics.put("pendingRatingsCount", pendingRatings.size());

      response.put("success", true);
      response.put("statistics", statistics);
      response.put("memberId", memberId);

      return ResponseEntity.ok(response);

    } catch (Exception e) {
      response.put("success", false);
      response.put("message", "查詢統計資料失敗：" + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  // ========== 私有輔助方法 ==========

  /**
   * 取得當前會員ID（需要根據您的認證機制實作）
   *
   * @return 會員ID
   */
  private Integer getCurrentMemberId() {
    // TODO: 實作從 session 或 JWT token 中取得會員ID
    // 暫時返回測試用的ID（實際開發時請替換）
    return 1;
  }

  /**
   * 檢查會員是否可以取消預約
   *
   * @param appointment 預約資料
   * @return 是否可以取消
   */
  private boolean canMemberCancelAppointment(CaregiverAppointment appointment) {
    List<String> cancellableStatuses = Arrays.asList("pending", "approved");

    if (!cancellableStatuses.contains(appointment.getStatus())) {
      return false;
    }

    // 檢查是否在允許取消的時間範圍內（預約時間前24小時）
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime scheduledTime = appointment.getScheduledAt();
    long hoursUntilAppointment = ChronoUnit.HOURS.between(now, scheduledTime);

    return hoursUntilAppointment >= 24;
  }

  /**
   * 驗證預約時間是否合理
   *
   * @param startTime 開始時間
   * @param endTime 結束時間
   * @return 驗證結果
   */
  private Map<String, Object> validateAppointmentTime(LocalDateTime startTime, LocalDateTime endTime) {
    Map<String, Object> result = new HashMap<>();

    if (startTime == null || endTime == null) {
      result.put("valid", false);
      result.put("message", "開始時間和結束時間不能為空");
      return result;
    }

    if (startTime.isAfter(endTime) || startTime.isEqual(endTime)) {
      result.put("valid", false);
      result.put("message", "開始時間必須早於結束時間");
      return result;
    }

    if (startTime.isBefore(LocalDateTime.now())) {
      result.put("valid", false);
      result.put("message", "預約時間不能是過去時間");
      return result;
    }

    // 檢查預約時長（最少1小時，最多8小時）
    long duration = ChronoUnit.HOURS.between(startTime, endTime);
    if (duration < 1) {
      result.put("valid", false);
      result.put("message", "預約時間最少1小時");
      return result;
    }

    if (duration > 8) {
      result.put("valid", false);
      result.put("message", "單次預約時間最多8小時");
      return result;
    }

    result.put("valid", true);
    result.put("duration", duration);
    return result;
  }
}