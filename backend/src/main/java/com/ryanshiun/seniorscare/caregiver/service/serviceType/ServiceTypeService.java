package com.ryanshiun.seniorscare.caregiver.service.serviceType;

import com.ryanshiun.seniorscare.caregiver.model.ServiceType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 服務類型服務介面
 */
public interface ServiceTypeService {

  /**
   * 取得所有啟用的服務類型
   * @return 服務類型列表
   */
  List<ServiceType> getAllActiveServiceTypes();

  /**
   * 根據ID取得服務類型
   * @param id 服務類型ID
   * @return 服務類型物件
   */
  Optional<ServiceType> getServiceTypeById(Integer id);

  /**
   * 根據服務類型ID計算總金額
   * @param serviceTypeId 服務類型ID
   * @param startTime 開始時間
   * @param endTime 結束時間
   * @return 總金額，如果服務類型不存在返回null
   */
  BigDecimal calculateTotalAmount(Integer serviceTypeId, LocalDateTime startTime, LocalDateTime endTime);

  /**
   * 計算服務時數（小時）
   * @param startTime 開始時間
   * @param endTime 結束時間
   * @return 服務時數（支援小數）
   */
  BigDecimal calculateServiceHours(LocalDateTime startTime, LocalDateTime endTime);

  /**
   * 根據ID取得每小時費率
   * @param serviceTypeId 服務類型ID
   * @return 每小時費率
   */
  Integer getHourlyRate(Integer serviceTypeId);

  /**
   * 驗證服務類型是否存在且啟用
   * @param serviceTypeId 服務類型ID
   * @return true if 存在且啟用
   */
  boolean isValidServiceType(Integer serviceTypeId);
}