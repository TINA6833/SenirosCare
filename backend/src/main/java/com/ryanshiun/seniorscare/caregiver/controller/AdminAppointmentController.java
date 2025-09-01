package com.ryanshiun.seniorscare.caregiver.controller;

import com.ryanshiun.seniorscare.caregiver.dto.caregiverAppointment.CaregiverAppointmentCreateDto;
import com.ryanshiun.seniorscare.caregiver.model.CaregiverAppointment;
import com.ryanshiun.seniorscare.caregiver.service.caregiverAppointment.CaregiverAppointmentService;
import com.ryanshiun.seniorscare.caregiver.dto.caregiverAppointment.CaregiverAppointmentQueryDto;
import com.ryanshiun.seniorscare.caregiver.util.AppointmentHelper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.*;

/**
 * 管理員預約控制器
 * 路徑從 /api/admin/appointments 改為 /api/caregiver/admin
 */
@RestController
@RequestMapping("/api/caregiver/admin") // ✅ 修改：從 /api/admin/appointments 改為 /api/caregiver/admin
public class AdminAppointmentController {

  @Autowired
  private CaregiverAppointmentService appointmentService;

  // ========== 員工建立預約功能 ==========

  /**
   * 員工建立預約（虛擬預約/照服員時間封鎖）
   * POST /api/caregiver/admin/appointment/employee-create
   */
  @PostMapping("/appointment/employee-create") // ✅ 修改：從 /employee-create 改為 /appointment/employee-create
  public ResponseEntity<Map<String, Object>> createEmployeeAppointment(
      @Valid @RequestBody CaregiverAppointmentCreateDto createDto) {

    Map<String, Object> response = new HashMap<>();

    try {
      // TODO: 驗證員工權限
      // Integer employeeId = getCurrentEmployeeId();
      // if (!hasPermission(employeeId, "CREATE_APPOINTMENT")) {
      //     return unauthorized();
      // }

      // 根據設計要則設定員工建立預約的參數
      createDto.setMemberId(null);               // 員工預約：member_id = null
      createDto.setIsBlocked(true);              // 員工預約：is_blocked = true
      createDto.setStatus("approved");           // 員工預約：status = approved

      // 員工建立預約必須指定原因類型
      if (createDto.getBlockType() == null || createDto.getBlockType().trim().isEmpty()) {
        createDto.setBlockType("unavailable"); // 預設原因
      }

      // 在 notes 中記錄建立者資訊
      String originalNotes = createDto.getNotes() != null ? createDto.getNotes() : "";
      String employeeInfo = "員工建立的預約";
      createDto.setNotes(originalNotes.isEmpty() ? employeeInfo : originalNotes + " | " + employeeInfo);

      Integer appointmentId = appointmentService.createAppointment(createDto);

      response.put("success", true);
      response.put("message", "員工預約建立成功");
      response.put("appointmentId", appointmentId);
      response.put("appointment", appointmentService.getAppointmentById(appointmentId));

      return ResponseEntity.status(HttpStatus.CREATED).body(response);

    } catch (Exception e) {
      response.put("success", false);
      response.put("message", "建立員工預約失敗: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
  }

  // ========== 原有的預約管理功能 ==========

  /**
   * 1. 查看所有訂單（支援分頁和篩選）
   * GET /api/caregiver/admin/appointments
   */
  @GetMapping("/appointments") // ✅ 修改：明確指定 /appointments 路徑
  public ResponseEntity<Map<String, Object>> getAppointments(
      @RequestParam(required = false) String status,
      @RequestParam(required = false) Integer caregiverId,
      @RequestParam(required = false) Integer memberId,
      @RequestParam(required = false) LocalDate startDate,
      @RequestParam(required = false) LocalDate endDate,
      @RequestParam(required = false) Boolean isBlocked, // 新增：可以篩選員工/會員預約
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {

    try {
      CaregiverAppointmentQueryDto queryDto = new CaregiverAppointmentQueryDto();
      queryDto.setStatus(status);
      queryDto.setCaregiverId(caregiverId);
      queryDto.setMemberId(memberId);
      queryDto.setIsBlocked(isBlocked); // 支援篩選預約類型

      if (startDate != null) {
        queryDto.setStartDate(startDate.atStartOfDay());
      }
      if (endDate != null) {
        queryDto.setEndDate(endDate.plusDays(1).atStartOfDay());
      }

      queryDto.setOffset(page * size);
      queryDto.setLimit(size);

      Map<String, Object> result = appointmentService.getAppointments(queryDto);
      Integer totalCount = (Integer) result.get("totalCount");

      // 為每個預約加入建立者資訊
      @SuppressWarnings("unchecked")
      List<CaregiverAppointment> appointments = (List<CaregiverAppointment>) result.get("appointments");

      List<Map<String, Object>> enhancedAppointments = new ArrayList<>();
      for (CaregiverAppointment appointment : appointments) {
        Map<String, Object> appointmentData = new HashMap<>();
        appointmentData.put("appointment", appointment);
        // 使用 AppointmentHelper
        appointmentData.put("creatorType", AppointmentHelper.getCreatorType(appointment));
        appointmentData.put("creatorDisplayName", AppointmentHelper.getCreatorDisplayName(appointment));
        appointmentData.put("isMemberAppointment", AppointmentHelper.isMemberAppointment(appointment));
        appointmentData.put("isEmployeeAppointment", AppointmentHelper.isEmployeeAppointment(appointment));
        enhancedAppointments.add(appointmentData);
      }

      Map<String, Object> response = new HashMap<>();
      response.put("success", true);
      response.put("appointments", enhancedAppointments);
      response.put("totalCount", totalCount);
      response.put("currentPage", page);
      response.put("hasNext", (page + 1) * size < totalCount);

      return ResponseEntity.ok(response);

    } catch (Exception e) {
      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put("success", false);
      errorResponse.put("message", "查詢訂單失敗: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
  }

  /**
   * 2. 查看待審核訂單（只包含會員建立的預約）
   * GET /api/caregiver/admin/appointments/pending
   */
  @GetMapping("/appointments/pending") // ✅ 修改：從 /pending 改為 /appointments/pending
  public ResponseEntity<Map<String, Object>> getPendingAppointments(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {

    try {
      CaregiverAppointmentQueryDto queryDto = new CaregiverAppointmentQueryDto();
      queryDto.setStatus("pending");
      queryDto.setIsBlocked(false); // 只查詢會員建立的預約
      queryDto.setOffset(page * size);
      queryDto.setLimit(size);

      Map<String, Object> result = appointmentService.getAppointments(queryDto);

      Map<String, Object> response = new HashMap<>();
      response.put("success", true);
      response.putAll(result);
      response.put("currentPage", page);

      return ResponseEntity.ok(response);

    } catch (Exception e) {
      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put("success", false);
      errorResponse.put("message", "查詢待審核訂單失敗: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
  }

  /**
   * 3. 審核訂單（通過）
   * PUT /api/caregiver/admin/appointment/{id}/approve
   */
  @PutMapping("/appointment/{id}/approve") // ✅ 修改：從 /{id}/approve 改為 /appointment/{id}/approve
  public ResponseEntity<Map<String, Object>> approveAppointment(@PathVariable Integer id) {
    Map<String, Object> response = new HashMap<>();

    try {
      CaregiverAppointment appointment = appointmentService.getAppointmentById(id);
      if (appointment == null) {
        response.put("success", false);
        response.put("message", "訂單不存在");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
      }

      if (!"pending".equals(appointment.getStatus())) {
        response.put("success", false);
        response.put("message", "訂單狀態不允許審核");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
      }

      // 使用 AppointmentHelper 檢查是否為會員預約
      if (!AppointmentHelper.isMemberAppointment(appointment)) {
        response.put("success", false);
        response.put("message", "只能審核會員建立的預約");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
      }

      // 檢查照服員時間衝突
      boolean hasConflict = appointmentService.hasTimeConflict(
          appointment.getCaregiverId(),
          appointment.getScheduledAt(),
          appointment.getEndTime(),
          id
      );

      if (hasConflict) {
        response.put("success", false);
        response.put("message", "照服員時間衝突，無法通過審核");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
      }

      boolean result = appointmentService.updateAppointmentStatus(id, "approved");

      if (result) {
        response.put("success", true);
        response.put("message", "訂單審核通過");
        response.put("appointment", appointmentService.getAppointmentById(id));
        return ResponseEntity.ok(response);
      } else {
        response.put("success", false);
        response.put("message", "審核失敗");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
      }

    } catch (Exception e) {
      response.put("success", false);
      response.put("message", "審核訂單失敗: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  /**
   * 4. 審核訂單（拒絕）
   * PUT /api/caregiver/admin/appointment/{id}/reject
   */
  @PutMapping("/appointment/{id}/reject") // ✅ 修改：從 /{id}/reject 改為 /appointment/{id}/reject
  public ResponseEntity<Map<String, Object>> rejectAppointment(
      @PathVariable Integer id,
      @RequestBody(required = false) Map<String, String> request) {

    Map<String, Object> response = new HashMap<>();

    try {
      CaregiverAppointment appointment = appointmentService.getAppointmentById(id);
      if (appointment == null) {
        response.put("success", false);
        response.put("message", "訂單不存在");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
      }

      if (!"pending".equals(appointment.getStatus())) {
        response.put("success", false);
        response.put("message", "訂單狀態不允許審核");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
      }

      // 使用 AppointmentHelper 檢查是否為會員預約
      if (!AppointmentHelper.isMemberAppointment(appointment)) {
        response.put("success", false);
        response.put("message", "只能審核會員建立的預約");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
      }

      boolean result = appointmentService.updateAppointmentStatus(id, "rejected");

      // 如果有拒絕原因，更新到 notes 欄位
      if (request != null && request.containsKey("reason")) {
        String rejectReason = request.get("reason");
        appointmentService.updateAppointmentNotes(id, "拒絕原因: " + rejectReason);
      }

      if (result) {
        response.put("success", true);
        response.put("message", "訂單已拒絕");
        response.put("appointment", appointmentService.getAppointmentById(id));
        return ResponseEntity.ok(response);
      } else {
        response.put("success", false);
        response.put("message", "拒絕失敗");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
      }

    } catch (Exception e) {
      response.put("success", false);
      response.put("message", "拒絕訂單失敗: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  /**
   * 5. 取消訂單
   * PUT /api/caregiver/admin/appointment/{id}/cancel
   */
  @PutMapping("/appointment/{id}/cancel") // ✅ 修改：從 /{id}/cancel 改為 /appointment/{id}/cancel
  public ResponseEntity<Map<String, Object>> cancelAppointment(
      @PathVariable Integer id,
      @RequestBody(required = false) Map<String, String> request) {

    Map<String, Object> response = new HashMap<>();

    try {
      CaregiverAppointment appointment = appointmentService.getAppointmentById(id);
      if (appointment == null) {
        response.put("success", false);
        response.put("message", "訂單不存在");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
      }

      List<String> cancellableStatuses = Arrays.asList("pending", "approved");
      if (!cancellableStatuses.contains(appointment.getStatus())) {
        response.put("success", false);
        response.put("message", "訂單狀態不允許取消");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
      }

      boolean result = appointmentService.cancelAppointment(id);

      // 如果有取消原因，更新到 notes 欄位
      if (request != null && request.containsKey("reason")) {
        String cancelReason = request.get("reason");
        appointmentService.updateAppointmentNotes(id, "管理員取消原因: " + cancelReason);
      }

      if (result) {
        response.put("success", true);
        response.put("message", "訂單已取消");
        response.put("appointment", appointmentService.getAppointmentById(id));
        return ResponseEntity.ok(response);
      } else {
        response.put("success", false);
        response.put("message", "取消失敗");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
      }

    } catch (Exception e) {
      response.put("success", false);
      response.put("message", "取消訂單失敗: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  /**
   * 6. 查看訂單詳細資訊
   * GET /api/caregiver/admin/appointment/{id}
   */
  @GetMapping("/appointment/{id}") // ✅ 修改：從 /{id} 改為 /appointment/{id}
  public ResponseEntity<Map<String, Object>> getAppointmentDetail(@PathVariable Integer id) {
    Map<String, Object> response = new HashMap<>();

    try {
      CaregiverAppointment appointment = appointmentService.getAppointmentById(id);

      if (appointment != null) {
        response.put("success", true);
        response.put("appointment", appointment);

        // 使用 AppointmentHelper 加入建立者資訊
        response.put("creatorType", AppointmentHelper.getCreatorType(appointment));
        response.put("creatorDisplayName", AppointmentHelper.getCreatorDisplayName(appointment));
        response.put("isMemberAppointment", AppointmentHelper.isMemberAppointment(appointment));
        response.put("isEmployeeAppointment", AppointmentHelper.isEmployeeAppointment(appointment));
        response.put("canBeRated", AppointmentHelper.canBeRated(appointment));
        response.put("isCancellable", AppointmentHelper.isCancellable(appointment));
        response.put("statusDisplayName", AppointmentHelper.getStatusDisplayName(appointment.getStatus()));
        response.put("blockTypeDisplayName", AppointmentHelper.getBlockTypeDisplayName(appointment.getBlockType()));

        return ResponseEntity.ok(response);
      } else {
        response.put("success", false);
        response.put("message", "訂單不存在");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
      }

    } catch (Exception e) {
      response.put("success", false);
      response.put("message", "查詢訂單詳情失敗: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  /**
   * 7. 統計資料
   * GET /api/caregiver/admin/appointments/statistics
   */
  @GetMapping("/appointments/statistics") // ✅ 修改：從 /statistics 改為 /appointments/statistics
  public ResponseEntity<Map<String, Object>> getAppointmentStatistics() {
    Map<String, Object> response = new HashMap<>();

    try {
      Map<String, Object> statistics = appointmentService.getAppointmentStatistics();

      response.put("success", true);
      response.putAll(statistics);

      return ResponseEntity.ok(response);

    } catch (Exception e) {
      response.put("success", false);
      response.put("message", "查詢統計資料失敗: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  /**
   * 8. 批量操作訂單狀態
   * PUT /api/caregiver/admin/appointments/batch-update
   */
  @PutMapping("/appointments/batch-update") // ✅ 修改：從 /batch-update 改為 /appointments/batch-update
  public ResponseEntity<Map<String, Object>> batchUpdateStatus(
      @RequestBody Map<String, Object> request) {

    Map<String, Object> response = new HashMap<>();

    try {
      @SuppressWarnings("unchecked")
      List<Integer> ids = (List<Integer>) request.get("ids");
      String status = (String) request.get("status");

      if (ids == null || ids.isEmpty()) {
        response.put("success", false);
        response.put("message", "請選擇要操作的訂單");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
      }

      if (status == null || status.trim().isEmpty()) {
        response.put("success", false);
        response.put("message", "請指定要更新的狀態");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
      }

      Integer updatedCount = appointmentService.batchUpdateAppointmentStatus(ids, status);

      response.put("success", true);
      response.put("message", String.format("成功更新 %d 筆訂單", updatedCount));
      response.put("updatedCount", updatedCount);
      response.put("totalRequested", ids.size());

      return ResponseEntity.ok(response);

    } catch (Exception e) {
      response.put("success", false);
      response.put("message", "批量更新失敗: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  /**
   * 9. 查看員工建立的預約（虛擬預約）
   * GET /api/caregiver/admin/appointments/employee-appointments
   */
  @GetMapping("/appointments/employee-appointments") // ✅ 修改：從 /employee-appointments 改為 /appointments/employee-appointments
  public ResponseEntity<Map<String, Object>> getEmployeeAppointments(
      @RequestParam(required = false) Integer caregiverId,
      @RequestParam(required = false) String blockType,
      @RequestParam(required = false) LocalDate startDate,
      @RequestParam(required = false) LocalDate endDate,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {

    try {
      CaregiverAppointmentQueryDto queryDto = new CaregiverAppointmentQueryDto();
      queryDto.setIsBlocked(true); // 只查詢員工建立的預約
      queryDto.setCaregiverId(caregiverId);

      if (startDate != null) {
        queryDto.setStartDate(startDate.atStartOfDay());
      }
      if (endDate != null) {
        queryDto.setEndDate(endDate.plusDays(1).atStartOfDay());
      }

      queryDto.setOffset(page * size);
      queryDto.setLimit(size);

      Map<String, Object> result = appointmentService.getAppointments(queryDto);

      // 如果有指定 blockType，在結果中進行過濾
      if (blockType != null && !blockType.trim().isEmpty()) {
        @SuppressWarnings("unchecked")
        List<CaregiverAppointment> appointments = (List<CaregiverAppointment>) result.get("appointments");
        appointments = appointments.stream()
            .filter(apt -> blockType.equals(apt.getBlockType()))
            .toList();
        result.put("appointments", appointments);
      }

      Map<String, Object> response = new HashMap<>();
      response.put("success", true);
      response.putAll(result);
      response.put("currentPage", page);

      return ResponseEntity.ok(response);

    } catch (Exception e) {
      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put("success", false);
      errorResponse.put("message", "查詢員工預約失敗: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
  }

  /**
   * 10. 完成預約
   * PUT /api/caregiver/admin/appointment/{id}/complete
   */
  @PutMapping("/appointment/{id}/complete") // ✅ 修改：從 /{id}/complete 改為 /appointment/{id}/complete
  public ResponseEntity<Map<String, Object>> completeAppointment(@PathVariable Integer id) {
    Map<String, Object> response = new HashMap<>();

    try {
      boolean result = appointmentService.completeAppointment(id);

      if (result) {
        response.put("success", true);
        response.put("message", "預約已標記為完成");
        response.put("appointment", appointmentService.getAppointmentById(id));
        return ResponseEntity.ok(response);
      } else {
        response.put("success", false);
        response.put("message", "完成預約失敗");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
      }

    } catch (Exception e) {
      response.put("success", false);
      response.put("message", "完成預約失敗: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
  }

  // ========== 私有輔助方法 ==========

  /**
   * TODO: 取得當前員工ID（需要根據您的認證機制實作）
   */
  private Integer getCurrentEmployeeId() {
    // 從 session 或 JWT token 中取得員工ID
    return 1; // 暫時回傳測試用的ID
  }

  /**
   * TODO: 檢查員工權限（需要根據您的權限機制實作）
   */
  private boolean hasPermission(Integer employeeId, String permission) {
    // 實作權限檢查邏輯
    return true; // 暫時回傳 true
  }
}