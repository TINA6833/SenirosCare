
package com.ryanshiun.seniorscare.caregiver.service.caregiver;

import com.ryanshiun.seniorscare.caregiver.dao.caregiver.CaregiverDAO;
import com.ryanshiun.seniorscare.caregiver.dto.caregiver.CaregiverRequestDTO;
import com.ryanshiun.seniorscare.caregiver.dto.caregiver.CaregiverResponseDTO;
import com.ryanshiun.seniorscare.caregiver.model.Caregiver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class CaregiverServiceImpl implements CaregiverService {

    @Autowired
    private CaregiverDAO caregiverDAO;

    @Override
    @Transactional(readOnly = true)
    public List<CaregiverResponseDTO> getAllCaregivers() {
        return caregiverDAO.findAll().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CaregiverResponseDTO> getCaregiverById(Integer id) {
        return caregiverDAO.findById(id)
                .map(this::convertToResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CaregiverResponseDTO> getCaregiversByName(String chineseName) {
        return caregiverDAO.findByChineseNameContaining(chineseName).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CaregiverResponseDTO> getCaregiversByStatus(Boolean isActive) {
        return caregiverDAO.findByIsActive(isActive).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CaregiverResponseDTO> getCaregiversByServiceArea(String serviceArea) {
        return caregiverDAO.findByServiceAreaContaining(serviceArea).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CaregiverResponseDTO createCaregiver(CaregiverRequestDTO requestDTO) {
        // 檢查電話是否已存在
        if (caregiverDAO.findByPhone(requestDTO.getPhone()).isPresent()) {
            throw new IllegalArgumentException("電話號碼已存在");
        }

        Caregiver caregiver = convertToEntity(requestDTO);
        Caregiver savedCaregiver = caregiverDAO.save(caregiver);
        return convertToResponseDTO(savedCaregiver);
    }

    @Override
    public CaregiverResponseDTO updateCaregiver(Integer id, CaregiverRequestDTO requestDTO) {
        Optional<Caregiver> existingCaregiverOpt = caregiverDAO.findById(id);
        if (existingCaregiverOpt.isEmpty()) {
            throw new IllegalArgumentException("找不到指定的照服員");
        }

        // 檢查電話是否已被其他人使用
        if (isPhoneExists(requestDTO.getPhone(), id)) {
            throw new IllegalArgumentException("電話號碼已被其他照服員使用");
        }

        Caregiver existingCaregiver = existingCaregiverOpt.get();
        updateCaregiverFromDTO(existingCaregiver, requestDTO);

        Caregiver updatedCaregiver = caregiverDAO.update(existingCaregiver);
        return convertToResponseDTO(updatedCaregiver);
    }

    @Override
    public boolean deleteCaregiver(Integer id) {
        if (caregiverDAO.findById(id).isEmpty()) {
            throw new IllegalArgumentException("找不到指定的照服員");
        }
        return caregiverDAO.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isPhoneExists(String phone, Integer excludeId) {
        if (excludeId == null) {
            return caregiverDAO.findByPhone(phone).isPresent();
        }
        return caregiverDAO.existsByPhoneAndCaregiverIdNot(phone, excludeId);
    }

    @Override
    public boolean updateCaregiverRating(Integer id, Integer rating) {
        Optional<Caregiver> caregiverOpt = caregiverDAO.findById(id);
        if (caregiverOpt.isEmpty()) {
            return false;
        }

        Caregiver caregiver = caregiverOpt.get();

        // 更新評價統計
        int newTotalRatings = caregiver.getTotalRatings() + 1;
        int newTotalPoints = caregiver.getTotalPoints() + rating;
        BigDecimal newAverageRating = BigDecimal.valueOf((double) newTotalPoints / newTotalRatings)
                .setScale(2, RoundingMode.HALF_UP);

        caregiver.setTotalRatings(newTotalRatings);
        caregiver.setTotalPoints(newTotalPoints);
        caregiver.setAverageRating(newAverageRating);

        caregiverDAO.update(caregiver);
        return true;
    }

    // 轉換方法
    private CaregiverResponseDTO convertToResponseDTO(Caregiver caregiver) {
        CaregiverResponseDTO responseDTO = new CaregiverResponseDTO();
        responseDTO.setCaregiverId(caregiver.getCaregiverId());
        responseDTO.setChineseName(caregiver.getChineseName());
        responseDTO.setGender(caregiver.getGender());
        responseDTO.setPhone(caregiver.getPhone());
        responseDTO.setEmail(caregiver.getEmail());
        responseDTO.setExperienceYears(caregiver.getExperienceYears());
        responseDTO.setPhoto(caregiver.getPhoto());
        responseDTO.setAddress(caregiver.getAddress());
        responseDTO.setServiceArea(caregiver.getServiceArea());
        responseDTO.setAverageRating(caregiver.getAverageRating());
        responseDTO.setTotalRatings(caregiver.getTotalRatings());
        responseDTO.setTotalPoints(caregiver.getTotalPoints());
        responseDTO.setIsActive(caregiver.getIsActive());
        responseDTO.setCreatedAt(caregiver.getCreatedAt());
        responseDTO.setUpdatedAt(caregiver.getUpdatedAt());
        responseDTO.setSelfIntroduction(caregiver.getSelfIntroduction()); // 修正：新增自我介紹欄位
        return responseDTO;
    }

    private Caregiver convertToEntity(CaregiverRequestDTO requestDTO) {
        Caregiver caregiver = new Caregiver();
        caregiver.setChineseName(requestDTO.getChineseName());
        caregiver.setGender(requestDTO.getGender());
        caregiver.setPhone(requestDTO.getPhone());
        caregiver.setEmail(requestDTO.getEmail());
        caregiver.setExperienceYears(requestDTO.getExperienceYears());
        caregiver.setPhoto(requestDTO.getPhoto());
        caregiver.setAddress(requestDTO.getAddress());
        caregiver.setServiceArea(requestDTO.getServiceArea());
        caregiver.setSelfIntroduction(requestDTO.getSelfIntroduction()); // 修正：新增自我介紹欄位
        caregiver.setAverageRating(
                requestDTO.getAverageRating() != null ? requestDTO.getAverageRating() : BigDecimal.ZERO);
        caregiver.setTotalRatings(requestDTO.getTotalRatings() != null ? requestDTO.getTotalRatings() : 0);
        caregiver.setTotalPoints(requestDTO.getTotalPoints() != null ? requestDTO.getTotalPoints() : 0);
        caregiver.setIsActive(requestDTO.getIsActive() != null ? requestDTO.getIsActive() : true);
        return caregiver;
    }

    private void updateCaregiverFromDTO(Caregiver caregiver, CaregiverRequestDTO requestDTO) {
        caregiver.setChineseName(requestDTO.getChineseName());
        caregiver.setGender(requestDTO.getGender());
        caregiver.setPhone(requestDTO.getPhone());
        caregiver.setEmail(requestDTO.getEmail());
        caregiver.setExperienceYears(requestDTO.getExperienceYears());
        caregiver.setPhoto(requestDTO.getPhoto());
        caregiver.setAddress(requestDTO.getAddress());
        caregiver.setServiceArea(requestDTO.getServiceArea());
        caregiver.setSelfIntroduction(requestDTO.getSelfIntroduction());
        if (requestDTO.getAverageRating() != null) {
            caregiver.setAverageRating(requestDTO.getAverageRating());
        }
        if (requestDTO.getTotalRatings() != null) {
            caregiver.setTotalRatings(requestDTO.getTotalRatings());
        }
        if (requestDTO.getTotalPoints() != null) {
            caregiver.setTotalPoints(requestDTO.getTotalPoints());
        }
        if (requestDTO.getIsActive() != null) {
            caregiver.setIsActive(requestDTO.getIsActive());
        }
    }
}