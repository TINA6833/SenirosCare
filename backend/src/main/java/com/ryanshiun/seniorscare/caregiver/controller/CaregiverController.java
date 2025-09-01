package com.ryanshiun.seniorscare.caregiver.controller;

import com.ryanshiun.seniorscare.caregiver.dto.caregiver.CaregiverRequestDTO;
import com.ryanshiun.seniorscare.caregiver.dto.caregiver.CaregiverResponseDTO;
import com.ryanshiun.seniorscare.caregiver.service.caregiver.CaregiverService;
import com.ryanshiun.seniorscare.caregiver.service.caregiver.FileService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 照服員基本CRUD控制器
 * 路徑從 /api/caregivers 改為 /api/caregiver
 */
@RestController
@RequestMapping("/api/caregiver") // ✅ 修改：從 /api/caregivers 改為 /api/caregiver
@CrossOrigin(origins = "*") // 允許跨域請求
public class CaregiverController {

  @Autowired
  private CaregiverService caregiverService;

  @Autowired
  private FileService fileService;

  /**
   * 取得所有照服員
   * GET /api/caregiver
   */
  @GetMapping
  public ResponseEntity<Map<String, Object>> getAllCaregivers() {
    try {
      List<CaregiverResponseDTO> caregivers = caregiverService.getAllCaregivers();

      Map<String, Object> response = new HashMap<>();
      response.put("success", true);
      response.put("message", "查詢成功");
      response.put("data", caregivers);
      response.put("total", caregivers.size());

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "查詢失敗：" + e.getMessage());
    }
  }

  /**
   * 根據ID取得照服員
   * GET /api/caregiver/{id}
   */
  @GetMapping("/{id}")
  public ResponseEntity<Map<String, Object>> getCaregiverById(@PathVariable Integer id) {
    try {
      Optional<CaregiverResponseDTO> caregiverOpt = caregiverService.getCaregiverById(id);

      if (caregiverOpt.isEmpty()) {
        return createErrorResponse(HttpStatus.NOT_FOUND, "找不到指定的照服員");
      }

      Map<String, Object> response = new HashMap<>();
      response.put("success", true);
      response.put("message", "查詢成功");
      response.put("data", caregiverOpt.get());

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "查詢失敗：" + e.getMessage());
    }
  }

  /**
   * 根據姓名搜尋照服員
   * GET /api/caregiver/search?name={name}
   */
  @GetMapping("/search")
  public ResponseEntity<Map<String, Object>> searchCaregivers(@RequestParam String name) {
    try {
      List<CaregiverResponseDTO> caregivers = caregiverService.getCaregiversByName(name);

      Map<String, Object> response = new HashMap<>();
      response.put("success", true);
      response.put("message", "搜尋成功");
      response.put("data", caregivers);
      response.put("total", caregivers.size());

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "搜尋失敗：" + e.getMessage());
    }
  }

  /**
   * 根據狀態篩選照服員
   * GET /api/caregiver/status/{isActive}
   */
  @GetMapping("/status/{isActive}")
  public ResponseEntity<Map<String, Object>> getCaregiversByStatus(@PathVariable Boolean isActive) {
    try {
      List<CaregiverResponseDTO> caregivers = caregiverService.getCaregiversByStatus(isActive);

      Map<String, Object> response = new HashMap<>();
      response.put("success", true);
      response.put("message", "查詢成功");
      response.put("data", caregivers);
      response.put("total", caregivers.size());

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "查詢失敗：" + e.getMessage());
    }
  }

  /**
   * 根據服務區域搜尋照服員
   * GET /api/caregiver/service-area?area={area}
   */
  @GetMapping("/service-area")
  public ResponseEntity<Map<String, Object>> getCaregiversByServiceArea(@RequestParam String area) {
    try {
      List<CaregiverResponseDTO> caregivers = caregiverService.getCaregiversByServiceArea(area);

      Map<String, Object> response = new HashMap<>();
      response.put("success", true);
      response.put("message", "查詢成功");
      response.put("data", caregivers);
      response.put("total", caregivers.size());

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "查詢失敗：" + e.getMessage());
    }
  }

  /**
   * 新增照服員
   * POST /api/caregiver
   */
  @PostMapping
  public ResponseEntity<Map<String, Object>> createCaregiver(
      @Valid @RequestBody CaregiverRequestDTO requestDTO,
      BindingResult bindingResult) {

    if (bindingResult.hasErrors()) {
      return createValidationErrorResponse(bindingResult);
    }

    try {
      CaregiverResponseDTO createdCaregiver = caregiverService.createCaregiver(requestDTO);

      Map<String, Object> response = new HashMap<>();
      response.put("success", true);
      response.put("message", "照服員新增成功");
      response.put("data", createdCaregiver);

      return ResponseEntity.status(HttpStatus.CREATED).body(response);
    } catch (IllegalArgumentException e) {
      return createErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    } catch (Exception e) {
      return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "新增失敗：" + e.getMessage());
    }
  }

  /**
   * 更新照服員
   * PUT /api/caregiver/{id}
   */
  @PutMapping("/{id}")
  public ResponseEntity<Map<String, Object>> updateCaregiver(
      @PathVariable Integer id,
      @Valid @RequestBody CaregiverRequestDTO requestDTO,
      BindingResult bindingResult) {

    if (bindingResult.hasErrors()) {
      return createValidationErrorResponse(bindingResult);
    }

    try {
      CaregiverResponseDTO updatedCaregiver = caregiverService.updateCaregiver(id, requestDTO);

      Map<String, Object> response = new HashMap<>();
      response.put("success", true);
      response.put("message", "照服員更新成功");
      response.put("data", updatedCaregiver);

      return ResponseEntity.ok(response);
    } catch (IllegalArgumentException e) {
      return createErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    } catch (Exception e) {
      return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "更新失敗：" + e.getMessage());
    }
  }

  /**
   * 刪除照服員
   * DELETE /api/caregiver/{id}
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<Map<String, Object>> deleteCaregiver(@PathVariable Integer id) {
    try {
      boolean deleted = caregiverService.deleteCaregiver(id);

      Map<String, Object> response = new HashMap<>();
      response.put("success", deleted);
      response.put("message", deleted ? "照服員刪除成功" : "刪除失敗");

      return ResponseEntity.ok(response);
    } catch (IllegalArgumentException e) {
      return createErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
    } catch (Exception e) {
      return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "刪除失敗：" + e.getMessage());
    }
  }

  /**
   * 更新照服員評價
   * POST /api/caregiver/{id}/rating?rating={rating}
   */
  @PostMapping("/{id}/rating")
  public ResponseEntity<Map<String, Object>> updateCaregiverRating(
      @PathVariable Integer id,
      @RequestParam Integer rating) {

    if (rating < 1 || rating > 5) {
      return createErrorResponse(HttpStatus.BAD_REQUEST, "評價必須在1-5之間");
    }

    try {
      boolean updated = caregiverService.updateCaregiverRating(id, rating);

      Map<String, Object> response = new HashMap<>();
      response.put("success", updated);
      response.put("message", updated ? "評價更新成功" : "評價更新失敗");

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "評價更新失敗：" + e.getMessage());
    }
  }

  /**
   * 檢查電話是否存在
   * GET /api/caregiver/check-phone?phone={phone}&excludeId={excludeId}
   */
  @GetMapping("/check-phone")
  public ResponseEntity<Map<String, Object>> checkPhoneExists(
      @RequestParam String phone,
      @RequestParam(required = false) Integer excludeId) {

    try {
      boolean exists = caregiverService.isPhoneExists(phone, excludeId);

      Map<String, Object> response = new HashMap<>();
      response.put("success", true);
      response.put("exists", exists);
      response.put("message", exists ? "電話號碼已存在" : "電話號碼可以使用");

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "檢查失敗：" + e.getMessage());
    }
  }

  /**
   * 上傳照服員照片
   * POST /api/caregiver/{id}/photo
   */
  @PostMapping("/{id}/photo")
  public ResponseEntity<Map<String, Object>> uploadPhoto(
      @PathVariable Integer id,
      @RequestParam("file") MultipartFile file) {

    try {
      // 檢查照服員是否存在
      Optional<CaregiverResponseDTO> caregiverOpt = caregiverService.getCaregiverById(id);
      if (caregiverOpt.isEmpty()) {
        return createErrorResponse(HttpStatus.NOT_FOUND, "找不到指定的照服員");
      }

      // 驗證檔案
      if (file.isEmpty()) {
        return createErrorResponse(HttpStatus.BAD_REQUEST, "請選擇檔案");
      }

      // 檢查檔案類型
      String contentType = file.getContentType();
      if (contentType == null || !contentType.startsWith("image/")) {
        return createErrorResponse(HttpStatus.BAD_REQUEST, "只允許上傳圖片檔案");
      }

      // 使用您的路徑結構：caregiver/caregiver_photo
      String filePath = fileService.uploadFile(file, "caregiver/caregiver_photo");

      // 刪除舊照片
      CaregiverResponseDTO caregiver = caregiverOpt.get();
      if (caregiver.getPhoto() != null && !caregiver.getPhoto().isEmpty()) {
        fileService.deleteFile(caregiver.getPhoto());
      }

      // 更新照服員的照片路徑
      CaregiverRequestDTO updateDTO = new CaregiverRequestDTO();
      updateDTO.setChineseName(caregiver.getChineseName());
      updateDTO.setGender(caregiver.getGender());
      updateDTO.setPhone(caregiver.getPhone());
      updateDTO.setEmail(caregiver.getEmail());
      updateDTO.setExperienceYears(caregiver.getExperienceYears());
      updateDTO.setAddress(caregiver.getAddress());
      updateDTO.setServiceArea(caregiver.getServiceArea());
      updateDTO.setIsActive(caregiver.getIsActive());
      updateDTO.setPhoto(filePath);

      CaregiverResponseDTO updatedCaregiver = caregiverService.updateCaregiver(id, updateDTO);

      Map<String, Object> response = new HashMap<>();
      response.put("success", true);
      response.put("message", "照片上傳成功");
      response.put("data", updatedCaregiver);
      response.put("photo", filePath);

      return ResponseEntity.ok(response);
    } catch (IOException e) {
      System.err.println("檔案上傳 IOException: " + e.getMessage());
      e.printStackTrace();
      return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "檔案上傳失敗：" + e.getMessage());
    } catch (Exception e) {
      System.err.println("照片上傳 Exception: " + e.getMessage());
      e.printStackTrace();
      return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "更新失敗：" + e.getMessage());
    }
  }

  // 輔助方法
  private ResponseEntity<Map<String, Object>> createErrorResponse(HttpStatus status, String message) {
    Map<String, Object> response = new HashMap<>();
    response.put("success", false);
    response.put("message", message);
    return ResponseEntity.status(status).body(response);
  }

  private ResponseEntity<Map<String, Object>> createValidationErrorResponse(BindingResult bindingResult) {
    Map<String, Object> response = new HashMap<>();
    Map<String, String> errors = new HashMap<>();

    bindingResult.getFieldErrors().forEach(error -> {
      errors.put(error.getField(), error.getDefaultMessage());
    });

    response.put("success", false);
    response.put("message", "資料驗證失敗");
    response.put("errors", errors);

    return ResponseEntity.badRequest().body(response);
  }
}