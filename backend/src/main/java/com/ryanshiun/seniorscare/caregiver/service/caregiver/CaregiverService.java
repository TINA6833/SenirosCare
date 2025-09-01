package com.ryanshiun.seniorscare.caregiver.service.caregiver;

import com.ryanshiun.seniorscare.caregiver.dto.caregiver.CaregiverRequestDTO;
import com.ryanshiun.seniorscare.caregiver.dto.caregiver.CaregiverResponseDTO;
import java.util.List;
import java.util.Optional;

public interface CaregiverService {

    /**
     * 取得所有照服員
     */
    List<CaregiverResponseDTO> getAllCaregivers();

    /**
     * 根據 ID 取得照服員
     */
    Optional<CaregiverResponseDTO> getCaregiverById(Integer id);

    /**
     * 根據姓名模糊查詢照服員
     */
    List<CaregiverResponseDTO> getCaregiversByName(String chineseName);

    /**
     * 根據狀態查詢照服員
     */
    List<CaregiverResponseDTO> getCaregiversByStatus(Boolean isActive);

    /**
     * 根據服務區域查詢照服員
     */
    List<CaregiverResponseDTO> getCaregiversByServiceArea(String serviceArea);

    /**
     * 新增照服員
     */
    CaregiverResponseDTO createCaregiver(CaregiverRequestDTO requestDTO);

    /**
     * 更新照服員
     */
    CaregiverResponseDTO updateCaregiver(Integer id, CaregiverRequestDTO requestDTO);

    /**
     * 刪除照服員
     */
    boolean deleteCaregiver(Integer id);

    /**
     * 檢查電話是否已存在（排除指定ID）
     */
    boolean isPhoneExists(String phone, Integer excludeId);

    /**
     * 更新照服員評價
     */
    boolean updateCaregiverRating(Integer id, Integer rating);
}