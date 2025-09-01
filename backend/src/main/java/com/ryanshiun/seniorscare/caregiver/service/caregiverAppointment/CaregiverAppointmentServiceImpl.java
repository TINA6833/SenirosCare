package com.ryanshiun.seniorscare.caregiver.service.caregiverAppointment;

import com.ryanshiun.seniorscare.caregiver.dao.caregiverAppointment.CaregiverAppointmentDao;
import com.ryanshiun.seniorscare.caregiver.dto.caregiverAppointment.AppointmentRatingDto;
import com.ryanshiun.seniorscare.caregiver.dto.caregiverAppointment.CaregiverAppointmentCreateDto;
import com.ryanshiun.seniorscare.caregiver.dto.caregiverAppointment.CaregiverAppointmentQueryDto;
import com.ryanshiun.seniorscare.caregiver.dto.caregiverAppointment.CaregiverAppointmentUpdateDto;
import com.ryanshiun.seniorscare.caregiver.model.CaregiverAppointment;
import com.ryanshiun.seniorscare.caregiver.model.ServiceType;
import com.ryanshiun.seniorscare.caregiver.service.caregiver.CaregiverService;
import com.ryanshiun.seniorscare.caregiver.service.serviceType.ServiceTypeService;
import java.math.BigDecimal;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CaregiverAppointmentServiceImpl implements CaregiverAppointmentService {

  @Autowired
  private ServiceTypeService serviceTypeService;

  @Autowired
  private CaregiverAppointmentDao appointmentDao;

  // 注入 CaregiverService 用於更新照服員評分統計
  @Autowired
  private CaregiverService caregiverService;

  // ========== 基本預約操作 ==========

  @Override
  @Transactional
  public Integer createAppointment(CaregiverAppointmentCreateDto createDto) {
    validateAppointment(createDto);

    // 🆕 新增：自動計算總金額
    if (createDto.getServiceTypeId() != null) {
      BigDecimal calculatedAmount = serviceTypeService.calculateTotalAmount(
          createDto.getServiceTypeId(),
          createDto.getScheduledAt(),
          createDto.getEndTime());

      if (calculatedAmount != null) {
        createDto.setTotalAmount(calculatedAmount);
        System.out.println("✅ 自動計算總金額成功：" + calculatedAmount + " 元");
      } else {
        throw new IllegalArgumentException("無法計算總金額，請檢查服務類型是否有效");
      }
    }

    // 根據設計規則決定初始狀態
    String initialStatus = determineInitialStatus(createDto);
    createDto.setStatus(initialStatus);

    // 只有已確認的預約才檢查時間衝突（approved 狀態）
    if ("approved".equals(initialStatus)) {
      List<CaregiverAppointment> conflicts = appointmentDao.checkTimeConflictForConfirmedAppointments(
          createDto.getCaregiverId(),
          createDto.getScheduledAt(),
          createDto.getEndTime(),
          null);

      if (!conflicts.isEmpty()) {
        throw new RuntimeException("該時段已有其他預約，請選擇其他時間");
      }
    }

    return appointmentDao.createAppointment(createDto);
  }

  @Override
  public CaregiverAppointment getAppointmentById(Integer id) {
    if (id == null || id <= 0) {
      throw new IllegalArgumentException("預約ID不能為空或小於等於0");
    }

    CaregiverAppointment appointment = appointmentDao.getAppointmentById(id);
    if (appointment == null) {
      throw new RuntimeException("找不到指定的預約記錄：" + id);
    }

    return appointment;
  }

  @Override
  public Map<String, Object> getAppointments(CaregiverAppointmentQueryDto queryDto) {
    if (queryDto == null) {
      queryDto = new CaregiverAppointmentQueryDto();
    }

    List<CaregiverAppointment> appointments = appointmentDao.getAppointments(queryDto);
    Integer totalCount = appointmentDao.getAppointmentCount(queryDto);

    Map<String, Object> result = new HashMap<>();
    result.put("appointments", appointments);
    result.put("totalCount", totalCount);
    result.put("currentPage", (queryDto.getOffset() / queryDto.getLimit()) + 1);
    result.put("totalPages", (int) Math.ceil((double) totalCount / queryDto.getLimit()));

    return result;
  }

  @Override
  public Integer getAppointmentCount(CaregiverAppointmentQueryDto queryDto) {
    try {
      return appointmentDao.getAppointmentCount(queryDto);
    } catch (Exception e) {
      System.err.println("取得預約數量時發生錯誤：" + e.getMessage());
      return 0;
    }
  }

  @Override
  @Transactional
  public boolean updateAppointment(Integer id, CaregiverAppointmentUpdateDto updateDto) {
    if (id == null || id <= 0) {
      throw new IllegalArgumentException("預約ID不能為空或小於等於0");
    }

    if (updateDto == null) {
      throw new IllegalArgumentException("更新資料不能為空");
    }

    CaregiverAppointment existingAppointment = getAppointmentById(id);

    // 如果更新時間，需要檢查衝突
    if (updateDto.getScheduledAt() != null || updateDto.getEndTime() != null) {
      LocalDateTime startTime = updateDto.getScheduledAt() != null ? updateDto.getScheduledAt()
          : existingAppointment.getScheduledAt();
      LocalDateTime endTime = updateDto.getEndTime() != null ? updateDto.getEndTime()
          : existingAppointment.getEndTime();

      if (hasTimeConflict(existingAppointment.getCaregiverId(), startTime, endTime, id)) {
        throw new RuntimeException("更新失敗：預約時間衝突");
      }
    }

    Integer result = appointmentDao.updateAppointment(id, updateDto);
    return result > 0;
  }

  @Override
  @Transactional
  public boolean cancelAppointment(Integer id) {
    if (id == null || id <= 0) {
      throw new IllegalArgumentException("預約ID不能為空或小於等於0");
    }

    CaregiverAppointment appointment = getAppointmentById(id);

    if ("cancelled".equals(appointment.getStatus())) {
      throw new RuntimeException("預約已經被取消");
    }

    if ("completed".equals(appointment.getStatus())) {
      throw new RuntimeException("已完成的預約無法取消");
    }

    Integer result = appointmentDao.cancelAppointment(id);
    return result > 0;
  }

  // ========== 評分功能 ==========

  @Override
  @Transactional
  public boolean addRating(Integer id, AppointmentRatingDto ratingDto) {
    if (id == null || id <= 0) {
      throw new IllegalArgumentException("預約ID不能為空或小於等於0");
    }

    if (ratingDto == null || ratingDto.getRatingScore() == null) {
      throw new IllegalArgumentException("評分資料不完整");
    }

    if (ratingDto.getRatingScore() < 1 || ratingDto.getRatingScore() > 5) {
      throw new IllegalArgumentException("評分必須在1-5分之間");
    }

    CaregiverAppointment appointment = getAppointmentById(id);

    if (!"completed".equals(appointment.getStatus())) {
      throw new RuntimeException("只能對已完成的預約進行評分");
    }

    if (Boolean.TRUE.equals(appointment.getIsRated())) {
      throw new RuntimeException("此預約已經評分過了");
    }

    // 1. 更新預約評分
    Integer result = appointmentDao.addRating(id, ratingDto);

    if (result > 0) {
      // 2. 同步更新照服員評分統計
      try {
        updateCaregiverRatingStatistics(appointment.getCaregiverId(), ratingDto.getRatingScore());
        return true;
      } catch (Exception e) {
        // 如果照服員評分統計更新失敗，整個事務回滾
        throw new RuntimeException("更新照服員評分統計失敗: " + e.getMessage(), e);
      }
    }

    return false;
  }

  /**
   * 更新照服員評分統計
   * 當預約被評分後，同步更新照服員的總評分次數、總得分、平均評分
   */
  private void updateCaregiverRatingStatistics(Integer caregiverId, Integer newRating) {
    try {
      boolean updateSuccess = caregiverService.updateCaregiverRating(caregiverId, newRating);
      if (!updateSuccess) {
        throw new RuntimeException("照服員評分統計更新失敗，照服員ID: " + caregiverId);
      }

      // 記錄成功日誌
      System.out.println("✅ 照服員評分統計更新成功 - 照服員ID: " + caregiverId + ", 新評分: " + newRating);

    } catch (Exception e) {
      // 記錄詳細錯誤信息
      System.err.println("❌ 更新照服員評分統計時發生錯誤:");
      System.err.println("   照服員ID: " + caregiverId);
      System.err.println("   評分: " + newRating);
      System.err.println("   錯誤訊息: " + e.getMessage());
      e.printStackTrace();

      // 拋出異常，讓事務回滾
      throw new RuntimeException("更新照服員評分統計失敗", e);
    }
  }

  // ========== 時間檢查功能 ==========

  @Override
  public boolean hasTimeConflict(Integer caregiverId, LocalDateTime startTime, LocalDateTime endTime,
      Integer excludeId) {
    if (caregiverId == null || startTime == null || endTime == null) {
      return false;
    }

    if (startTime.isAfter(endTime) || startTime.isEqual(endTime)) {
      throw new IllegalArgumentException("開始時間必須早於結束時間");
    }

    List<CaregiverAppointment> conflicts = appointmentDao.checkTimeConflictForConfirmedAppointments(
        caregiverId, startTime, endTime, excludeId);

    return !conflicts.isEmpty();
  }

  @Override
  public Map<String, Object> preCheckTimeAvailability(Integer caregiverId, LocalDateTime startTime,
      LocalDateTime endTime) {
    Map<String, Object> result = new HashMap<>();

    try {
      // 1. 基本驗證
      if (caregiverId == null || startTime == null || endTime == null) {
        result.put("available", false);
        result.put("message", "參數不完整");
        return result;
      }

      if (startTime.isAfter(endTime) || startTime.isEqual(endTime)) {
        result.put("available", false);
        result.put("message", "開始時間必須早於結束時間");
        return result;
      }

      if (startTime.isBefore(LocalDateTime.now())) {
        result.put("available", false);
        result.put("message", "預約時間不能早於現在時間");
        return result;
      }

      // 2. 檢查是否為營業時間（可根據需求調整）
      int startHour = startTime.getHour();
      int endHour = endTime.getHour();
      if (startHour < 6 || endHour > 23) {
        result.put("available", false);
        result.put("message", "預約時間必須在營業時間內（06:00-23:00）");
        return result;
      }

      // 3. 檢查預約時長
      long duration = ChronoUnit.HOURS.between(startTime, endTime);
      if (duration < 1) {
        result.put("available", false);
        result.put("message", "預約時間最少1小時");
        return result;
      }

      if (duration > 8) {
        result.put("available", false);
        result.put("message", "單次預約時間最多8小時");
        return result;
      }

      // 4. 檢查時間衝突（只檢查已確認的預約）
      List<CaregiverAppointment> conflicts = appointmentDao.checkTimeConflictForConfirmedAppointments(
          caregiverId, startTime, endTime, null);

      if (!conflicts.isEmpty()) {
        result.put("available", false);
        result.put("message", "該時段已有其他預約");
        result.put("conflictAppointments", conflicts);
        return result;
      }

      // 5. 時間可用
      result.put("available", true);
      result.put("message", "時間可用");

      return result;

    } catch (Exception e) {
      result.put("available", false);
      result.put("message", "檢查時間可用性時發生錯誤：" + e.getMessage());
      return result;
    }
  }

  @Override
  public List<Map<String, Object>> getAvailableTimeSlots(Integer caregiverId, LocalDate date) {
    List<Map<String, Object>> availableSlots = new ArrayList<>();

    try {
      // 取得當天的所有已確認預約
      LocalDateTime dayStart = date.atStartOfDay();
      LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();

      List<CaregiverAppointment> dayAppointments = appointmentDao.getCaregiverAppointments(
          caregiverId, dayStart, dayEnd);

      // 過濾出已確認的預約
      List<CaregiverAppointment> confirmedAppointments = dayAppointments.stream()
          .filter(apt -> "approved".equals(apt.getStatus()) || "completed".equals(apt.getStatus()))
          .sorted((a, b) -> a.getScheduledAt().compareTo(b.getScheduledAt()))
          .toList();

      // 營業時間：8:00 - 20:00
      LocalDateTime workStart = date.atTime(8, 0);
      LocalDateTime workEnd = date.atTime(20, 0);

      // 如果是今天，從當前時間開始
      if (date.equals(LocalDate.now())) {
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(workStart)) {
          workStart = now.plusHours(1).withMinute(0).withSecond(0).withNano(0); // 至少提前1小時預約
        }
      }

      // 產生可用時間段
      LocalDateTime currentTime = workStart;

      for (CaregiverAppointment appointment : confirmedAppointments) {
        // 如果當前時間與預約開始時間之間有空檔
        if (currentTime.isBefore(appointment.getScheduledAt())) {
          // 只有空檔大於等於1小時才加入
          long gap = ChronoUnit.MINUTES.between(currentTime, appointment.getScheduledAt());
          if (gap >= 60) {
            availableSlots.add(createTimeSlot(currentTime, appointment.getScheduledAt()));
          }
        }
        currentTime = appointment.getEndTime();
      }

      // 最後一個預約後到下班時間的空檔
      if (currentTime.isBefore(workEnd)) {
        long gap = ChronoUnit.MINUTES.between(currentTime, workEnd);
        if (gap >= 60) {
          availableSlots.add(createTimeSlot(currentTime, workEnd));
        }
      }

    } catch (Exception e) {
      System.err.println("取得可用時間段時發生錯誤：" + e.getMessage());
      e.printStackTrace();
    }

    return availableSlots;
  }

  /**
   * 輔助方法：建立時間段物件
   */
  private Map<String, Object> createTimeSlot(LocalDateTime start, LocalDateTime end) {
    Map<String, Object> slot = new HashMap<>();
    slot.put("startTime", start);
    slot.put("endTime", end);
    slot.put("duration", ChronoUnit.MINUTES.between(start, end));

    // 加入格式化的時間字串
    slot.put("startTimeFormatted", start.format(DateTimeFormatter.ofPattern("HH:mm")));
    slot.put("endTimeFormatted", end.format(DateTimeFormatter.ofPattern("HH:mm")));
    slot.put("dateFormatted", start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

    return slot;
  }

  // ========== 照服員和會員相關功能 ==========

  @Override
  public List<CaregiverAppointment> getCaregiverSchedule(Integer caregiverId, LocalDateTime startDate,
      LocalDateTime endDate) {
    if (caregiverId == null || caregiverId <= 0) {
      throw new IllegalArgumentException("照服員ID不能為空或小於等於0");
    }

    if (startDate == null || endDate == null) {
      throw new IllegalArgumentException("查詢時間範圍不能為空");
    }

    if (startDate.isAfter(endDate)) {
      throw new IllegalArgumentException("開始時間不能晚於結束時間");
    }

    return appointmentDao.getCaregiverAppointments(caregiverId, startDate, endDate);
  }

  @Override
  public List<CaregiverAppointment> getMemberAppointments(Integer memberId, String status) {
    if (memberId == null || memberId <= 0) {
      throw new IllegalArgumentException("會員ID不能為空或小於等於0");
    }

    return appointmentDao.getMemberAppointments(memberId, status);
  }

  @Override
  public List<CaregiverAppointment> getPendingRatings(Integer memberId) {
    if (memberId == null || memberId <= 0) {
      throw new IllegalArgumentException("會員ID不能為空或小於等於0");
    }

    return appointmentDao.getUnratedCompletedAppointments(memberId);
  }

  // ========== 狀態管理功能 ==========

  @Override
  @Transactional
  public boolean completeAppointment(Integer id) {
    if (id == null || id <= 0) {
      throw new IllegalArgumentException("預約ID不能為空或小於等於0");
    }

    CaregiverAppointment appointment = getAppointmentById(id);

    if ("completed".equals(appointment.getStatus())) {
      throw new RuntimeException("預約已經完成");
    }

    if ("cancelled".equals(appointment.getStatus())) {
      throw new RuntimeException("已取消的預約無法標記為完成");
    }

    Integer result = appointmentDao.updateAppointmentStatus(id, "completed");
    return result > 0;
  }

  @Override
  public boolean updateAppointmentStatus(Integer id, String status) {
    Integer result = appointmentDao.updateAppointmentStatus(id, status);
    return result > 0;
  }

  @Override
  public Integer updateAppointmentNotes(Integer id, String notes) {
    CaregiverAppointmentUpdateDto updateDto = new CaregiverAppointmentUpdateDto();
    updateDto.setNotes(notes);
    return appointmentDao.updateAppointment(id, updateDto);
  }

  // ========== 統計和批量操作功能 ==========

  @Override
  public Map<String, Object> getAppointmentStatistics() {
    Map<String, Object> statistics = new HashMap<>();

    // 基本狀態統計
    statistics.put("pendingCount", appointmentDao.getCountByStatus("pending"));
    statistics.put("approvedCount", appointmentDao.getCountByStatus("approved"));
    statistics.put("rejectedCount", appointmentDao.getCountByStatus("rejected"));
    statistics.put("completedCount", appointmentDao.getCountByStatus("completed"));
    statistics.put("cancelledCount", appointmentDao.getCountByStatus("cancelled"));

    Integer totalCount = appointmentDao.getCountByStatus(null);
    statistics.put("totalCount", totalCount);

    // 時間範圍統計
    LocalDateTime todayStart = LocalDate.now().atStartOfDay();
    LocalDateTime todayEnd = LocalDate.now().plusDays(1).atStartOfDay();
    statistics.put("todayCount", appointmentDao.getCountByDateRange(todayStart, todayEnd));

    LocalDateTime weekStart = LocalDate.now().with(DayOfWeek.MONDAY).atStartOfDay();
    statistics.put("weekCount", appointmentDao.getCountByDateRange(weekStart, LocalDateTime.now()));

    LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
    statistics.put("monthCount", appointmentDao.getCountByDateRange(monthStart, LocalDateTime.now()));

    return statistics;
  }

  @Override
  @Transactional
  public Integer batchUpdateAppointmentStatus(List<Integer> ids, String status) {
    int totalUpdated = 0;

    for (Integer appointmentId : ids) {
      CaregiverAppointment appointment = appointmentDao.getAppointmentById(appointmentId);
      if (appointment != null && canUpdateStatus(appointment.getStatus(), status)) {
        Integer result = appointmentDao.updateAppointmentStatus(appointmentId, status);
        totalUpdated += result;
      }
    }

    return totalUpdated;
  }

  // ========== 權限和驗證功能 ==========

  @Override
  public boolean checkMemberPermission(Integer appointmentId, Integer memberId) {
    if (appointmentId == null || memberId == null) {
      return false;
    }

    CaregiverAppointment appointment = appointmentDao.getAppointmentById(appointmentId);

    if (appointment == null) {
      return false;
    }

    return memberId.equals(appointment.getMemberId());
  }

  @Override
  public List<String> getAvailableActionsForMember(Integer appointmentId, Integer memberId) {
    List<String> actions = new ArrayList<>();

    if (appointmentId == null || memberId == null) {
      return actions;
    }

    CaregiverAppointment appointment = appointmentDao.getAppointmentById(appointmentId);
    if (appointment == null || !memberId.equals(appointment.getMemberId())) {
      return actions;
    }

    switch (appointment.getStatus()) {
      case "pending":
        if (canCancelAppointment(appointment)) {
          actions.add("cancel");
        }
        break;
      case "approved":
        if (canCancelAppointment(appointment)) {
          actions.add("cancel");
        }
        break;
      case "completed":
        if (!Boolean.TRUE.equals(appointment.getIsRated())) {
          actions.add("rate");
        }
        actions.add("view");
        break;
      case "rejected":
      case "cancelled":
        actions.add("rebook");
        actions.add("view");
        break;
      default:
        actions.add("view");
        break;
    }

    return actions;
  }

  @Override
  public void validateAppointment(CaregiverAppointmentCreateDto createDto) {
    if (createDto == null) {
      throw new IllegalArgumentException("預約資料不能為空");
    }

    if (createDto.getCaregiverId() == null || createDto.getCaregiverId() <= 0) {
      throw new IllegalArgumentException("照服員ID不能為空或無效");
    }

    if (createDto.getScheduledAt() == null) {
      throw new IllegalArgumentException("預約開始時間不能為空");
    }

    if (createDto.getEndTime() == null) {
      throw new IllegalArgumentException("預約結束時間不能為空");
    }

    if (createDto.getScheduledAt().isAfter(createDto.getEndTime()) ||
        createDto.getScheduledAt().isEqual(createDto.getEndTime())) {
      throw new IllegalArgumentException("開始時間必須早於結束時間");
    }

    if (createDto.getScheduledAt().isBefore(LocalDateTime.now())) {
      throw new IllegalArgumentException("預約時間不能早於現在時間");
    }

    // 🆕 新增：服務類型驗證
    if (createDto.getServiceTypeId() != null) {
      if (!serviceTypeService.isValidServiceType(createDto.getServiceTypeId())) {
        throw new IllegalArgumentException("服務類型無效或已停用");
      }
    }

    // 根據設計規則驗證建立者邏輯
    if (Boolean.TRUE.equals(createDto.getIsBlocked())) {
      // 員工建立：is_blocked = true, member_id 應該為 null
      if (createDto.getMemberId() != null) {
        throw new IllegalArgumentException("員工建立的預約不應該有會員ID");
      }
      if (createDto.getBlockType() == null) {
        throw new IllegalArgumentException("員工建立的預約必須指定原因類型");
      }
    } else {
      // 會員建立：is_blocked = false, member_id 必須有值
      if (createDto.getMemberId() == null) {
        throw new IllegalArgumentException("會員建立的預約必須指定會員ID");
      }
    }
  }

  /**
   * 計算預約價格預覽（不建立實際預約）
   * 
   * @param serviceTypeId 服務類型ID
   * @param startTime     開始時間
   * @param endTime       結束時間
   * @return 包含價格資訊的Map
   */
  public Map<String, Object> calculateAppointmentPrice(Integer serviceTypeId, LocalDateTime startTime,
      LocalDateTime endTime) {
    Map<String, Object> priceInfo = new HashMap<>();

    try {
      // 驗證參數
      if (serviceTypeId == null || startTime == null || endTime == null) {
        priceInfo.put("success", false);
        priceInfo.put("message", "參數不完整");
        return priceInfo;
      }

      if (startTime.isAfter(endTime) || startTime.isEqual(endTime)) {
        priceInfo.put("success", false);
        priceInfo.put("message", "開始時間必須早於結束時間");
        return priceInfo;
      }

      // 取得服務類型資訊
      Optional<ServiceType> serviceTypeOpt = serviceTypeService.getServiceTypeById(serviceTypeId);
      if (serviceTypeOpt.isEmpty()) {
        priceInfo.put("success", false);
        priceInfo.put("message", "服務類型不存在");
        return priceInfo;
      }

      ServiceType serviceType = serviceTypeOpt.get();
      if (!Boolean.TRUE.equals(serviceType.getIsActive())) {
        priceInfo.put("success", false);
        priceInfo.put("message", "服務類型已停用");
        return priceInfo;
      }

      // 計算服務時數
      BigDecimal serviceHours = serviceTypeService.calculateServiceHours(startTime, endTime);

      // 計算總金額
      BigDecimal totalAmount = serviceTypeService.calculateTotalAmount(serviceTypeId, startTime, endTime);

      // 組裝回傳資料
      priceInfo.put("success", true);
      priceInfo.put("serviceType", serviceType);
      priceInfo.put("serviceHours", serviceHours);
      priceInfo.put("hourlyRate", serviceType.getHourlyRate());
      priceInfo.put("totalAmount", totalAmount);
      priceInfo.put("message", "價格計算成功");

      return priceInfo;

    } catch (Exception e) {
      priceInfo.put("success", false);
      priceInfo.put("message", "計算價格時發生錯誤：" + e.getMessage());
      return priceInfo;
    }
  }
  // ========== 私有輔助方法 ==========

  /**
   * 根據設計規則決定初始狀態
   * 會員建立：status = "pending", member_id = 有值, is_blocked = false
   * 員工建立：status = "approved", member_id = null, is_blocked = true
   */
  private String determineInitialStatus(CaregiverAppointmentCreateDto createDto) {
    // 員工建立：is_blocked = true, member_id = null, status = "approved"
    if (Boolean.TRUE.equals(createDto.getIsBlocked()) && createDto.getMemberId() == null) {
      return "approved"; // 員工建立直接通過
    }

    // 會員建立：is_blocked = false, member_id = 有值, status = "pending"
    if (Boolean.FALSE.equals(createDto.getIsBlocked()) && createDto.getMemberId() != null) {
      return "pending"; // 會員建立需要審核
    }

    // 預設情況（不應該發生，因為 validateAppointment 會檢查）
    return "pending";
  }

  /**
   * 檢查狀態轉換是否合法
   */
  private boolean canUpdateStatus(String currentStatus, String newStatus) {
    Map<String, List<String>> allowedTransitions = new HashMap<>();
    allowedTransitions.put("pending", Arrays.asList("approved", "rejected", "cancelled"));
    allowedTransitions.put("approved", Arrays.asList("completed", "cancelled"));
    allowedTransitions.put("rejected", Arrays.asList());
    allowedTransitions.put("completed", Arrays.asList());
    allowedTransitions.put("cancelled", Arrays.asList());

    List<String> allowedStatuses = allowedTransitions.get(currentStatus);
    return allowedStatuses != null && allowedStatuses.contains(newStatus);
  }

  /**
   * 檢查是否可以取消預約（24小時前）
   */
  private boolean canCancelAppointment(CaregiverAppointment appointment) {
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

  // ========== 公用輔助方法（供其他地方使用） ==========

  /**
   * 透過現有欄位判斷建立者類型
   */
  public String getCreatorType(CaregiverAppointment appointment) {
    // 員工建立的特徵：is_blocked = true, member_id = null
    if (Boolean.TRUE.equals(appointment.getIsBlocked()) && appointment.getMemberId() == null) {
      return "employee";
    }

    // 會員建立的特徵：is_blocked = false, member_id 有值
    if (Boolean.FALSE.equals(appointment.getIsBlocked()) && appointment.getMemberId() != null) {
      return "member";
    }

    return "unknown";
  }

  /**
   * 取得建立者顯示名稱
   */
  public String getCreatorDisplayName(CaregiverAppointment appointment) {
    String creatorType = getCreatorType(appointment);
    switch (creatorType) {
      case "member":
        return "會員預約 (ID: " + appointment.getMemberId() + ")";
      case "employee":
        return "員工預約 (" + appointment.getBlockType() + ")";
      default:
        return "未知建立者";
    }
  }

  /**
   * 檢查是否為會員預約
   */
  public boolean isMemberAppointment(CaregiverAppointment appointment) {
    return Boolean.FALSE.equals(appointment.getIsBlocked()) && appointment.getMemberId() != null;
  }

  /**
   * 檢查是否為員工預約
   */
  public boolean isEmployeeAppointment(CaregiverAppointment appointment) {
    return Boolean.TRUE.equals(appointment.getIsBlocked()) && appointment.getMemberId() == null;
  }
}