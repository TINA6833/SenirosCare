package com.ryanshiun.seniorscare.caregiver.dao;

import com.ryanshiun.seniorscare.caregiver.model.ServiceType;
import java.util.List;
import java.util.Optional;

/**
 * 服務類型資料存取介面
 */
public interface ServiceTypeDAO {

  /**
   * 查詢所有啟用的服務類型
   * @return 服務類型列表
   */
  List<ServiceType> findAllActive();

  /**
   * 根據ID查詢服務類型
   * @param id 服務類型ID
   * @return 服務類型物件
   */
  Optional<ServiceType> findById(Integer id);

  /**
   * 根據服務名稱查詢
   * @param serviceName 服務名稱
   * @return 服務類型物件
   */
  Optional<ServiceType> findByServiceName(String serviceName);

  /**
   * 查詢所有服務類型（包含停用）
   * @return 服務類型列表
   */
  List<ServiceType> findAll();

  /**
   * 根據ID獲取每小時費率
   * @param serviceTypeId 服務類型ID
   * @return 每小時費率，找不到返回null
   */
  Integer getHourlyRateById(Integer serviceTypeId);
}