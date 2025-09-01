package com.ryanshiun.seniorscare.caregiver.controller;

import com.ryanshiun.seniorscare.caregiver.model.ServiceType;
import com.ryanshiun.seniorscare.caregiver.service.caregiverAppointment.CaregiverAppointmentService;
import com.ryanshiun.seniorscare.caregiver.service.serviceType.ServiceTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 服務類型控制器
 * 路徑：/api/caregiver/service-types
 */
@RestController
@RequestMapping("/api/caregiver/service-types")
public class ServiceTypeController {

  @Autowired
  private ServiceTypeService serviceTypeService;

  @Autowired
  private CaregiverAppointmentService appointmentService;

  /**
   * 取得所有啟用的服務類型
   * GET /api/caregiver/service-types
   */
  @GetMapping
  public ResponseEntity<Map<String, Object>> getAllActiveServiceTypes() {
    try {
      List<ServiceType> serviceTypes = serviceTypeService.getAllActiveServiceTypes();

      Map<String, Object> response = new HashMap<>();
      response.put("success", true);
      response.put("message", "查詢成功");
      response.put("data", serviceTypes);
      response.put("total", serviceTypes.size());

      return ResponseEntity.ok(response);

    } catch (Exception e) {
      return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "查詢服務類型失敗：" + e.getMessage());
    }
  }

  /**
   * 根據ID取得服務類型詳情
   * GET /api/caregiver/service-types/{id}
   */
  @GetMapping("/{id}")
  public ResponseEntity<Map<String, Object>> getServiceTypeById(@PathVariable Integer id) {
    try {
      var serviceTypeOpt = serviceTypeService.getServiceTypeById(id);

      if (serviceTypeOpt.isEmpty()) {
        return createErrorResponse(HttpStatus.NOT_FOUND, "找不到指定的服務類型");
      }

      Map<String, Object> response = new HashMap<>();
      response.put("success", true);
      response.put("message", "查詢成功");
      response.put("data", serviceTypeOpt.get());

      return ResponseEntity.ok(response);

    } catch (Exception e) {
      return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "查詢服務類型失敗：" + e.getMessage());
    }
  }

  /**
   * 計算預約價格預覽
   * GET /api/caregiver/service-types/{serviceTypeId}/calculate-price
   *
   * @param serviceTypeId 服務類型ID
   * @param startTime 開始時間 (格式: yyyy-MM-ddTHH:mm:ss)
   * @param endTime 結束時間 (格式: yyyy-MM-ddTHH:mm:ss)
   */
  @GetMapping("/{serviceTypeId}/calculate-price")
  public ResponseEntity<Map<String, Object>> calculatePrice(
      @PathVariable Integer serviceTypeId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {

    try {
      Map<String, Object> priceInfo = appointmentService.calculateAppointmentPrice(serviceTypeId, startTime, endTime);

      if (Boolean.TRUE.equals(priceInfo.get("success"))) {
        return ResponseEntity.ok(priceInfo);
      } else {
        return ResponseEntity.badRequest().body(priceInfo);
      }

    } catch (Exception e) {
      return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "計算價格失敗：" + e.getMessage());
    }
  }

  /**
   * 快速取得所有服務類型的基本資訊（用於下拉選單）
   * GET /api/caregiver/service-types/dropdown
   */
  @GetMapping("/dropdown")
  public ResponseEntity<Map<String, Object>> getServiceTypesForDropdown() {
    try {
      List<ServiceType> serviceTypes = serviceTypeService.getAllActiveServiceTypes();

      // 只回傳必要的欄位
      List<Map<String, Object>> dropdownData = serviceTypes.stream()
          .map(st -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", st.getServiceTypeId());
            item.put("name", st.getServiceName());
            item.put("hourlyRate", st.getHourlyRate());
            item.put("description", st.getDescription());
            return item;
          })
          .toList();

      Map<String, Object> response = new HashMap<>();
      response.put("success", true);
      response.put("message", "查詢成功");
      response.put("data", dropdownData);
      response.put("total", dropdownData.size());

      return ResponseEntity.ok(response);

    } catch (Exception e) {
      return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "查詢服務類型失敗：" + e.getMessage());
    }
  }

  // 輔助方法：建立錯誤回應
  private ResponseEntity<Map<String, Object>> createErrorResponse(HttpStatus status, String message) {
    Map<String, Object> response = new HashMap<>();
    response.put("success", false);
    response.put("message", message);
    return ResponseEntity.status(status).body(response);
  }
}