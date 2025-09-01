package com.ryanshiun.seniorscare.caregiver.service.serviceType;

import com.ryanshiun.seniorscare.caregiver.dao.ServiceTypeDAO;
import com.ryanshiun.seniorscare.caregiver.model.ServiceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * 服務類型服務實作類
 */
@Service
@Transactional(readOnly = true)
public class ServiceTypeServiceImpl implements ServiceTypeService {

  @Autowired
  private ServiceTypeDAO serviceTypeDAO;

  @Override
  public List<ServiceType> getAllActiveServiceTypes() {
    return serviceTypeDAO.findAllActive();
  }

  @Override
  public Optional<ServiceType> getServiceTypeById(Integer id) {
    if (id == null || id <= 0) {
      return Optional.empty();
    }
    return serviceTypeDAO.findById(id);
  }

  @Override
  public BigDecimal calculateTotalAmount(Integer serviceTypeId, LocalDateTime startTime, LocalDateTime endTime) {
    // 驗證參數
    if (serviceTypeId == null || startTime == null || endTime == null) {
      return null;
    }

    if (startTime.isAfter(endTime) || startTime.isEqual(endTime)) {
      throw new IllegalArgumentException("開始時間必須早於結束時間");
    }

    // 取得每小時費率
    Integer hourlyRate = serviceTypeDAO.getHourlyRateById(serviceTypeId);
    if (hourlyRate == null) {
      return null; // 服務類型不存在或已停用
    }

    // 計算服務時數
    BigDecimal serviceHours = calculateServiceHours(startTime, endTime);

    // 計算總金額 = 每小時費率 × 服務時數
    BigDecimal totalAmount = BigDecimal.valueOf(hourlyRate).multiply(serviceHours);

    // 四捨五入到小數點後2位
    return totalAmount.setScale(2, RoundingMode.HALF_UP);
  }

  @Override
  public BigDecimal calculateServiceHours(LocalDateTime startTime, LocalDateTime endTime) {
    if (startTime == null || endTime == null) {
      return BigDecimal.ZERO;
    }

    if (startTime.isAfter(endTime) || startTime.isEqual(endTime)) {
      return BigDecimal.ZERO;
    }

    // 計算分鐘數差異
    long minutesBetween = ChronoUnit.MINUTES.between(startTime, endTime);

    // 轉換為小時（支援小數）
    BigDecimal hours = BigDecimal.valueOf(minutesBetween).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);

    return hours;
  }

  @Override
  public Integer getHourlyRate(Integer serviceTypeId) {
    if (serviceTypeId == null || serviceTypeId <= 0) {
      return null;
    }
    return serviceTypeDAO.getHourlyRateById(serviceTypeId);
  }

  @Override
  public boolean isValidServiceType(Integer serviceTypeId) {
    if (serviceTypeId == null || serviceTypeId <= 0) {
      return false;
    }

    Optional<ServiceType> serviceType = serviceTypeDAO.findById(serviceTypeId);
    return serviceType.isPresent() && Boolean.TRUE.equals(serviceType.get().getIsActive());
  }
}