package com.ryanshiun.seniorscare.roomType.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.ryanshiun.seniorscare.roomType.dto.FacilityForm;
import com.ryanshiun.seniorscare.roomType.model.Facility;

public interface FacilityService {

    // ===== C =====
    int addFacility(Facility facility);

    // ===== R =====
    Facility getFacilityById(int id);
    List<Facility> getAllFacilities();
    List<Facility> getAvailableFacilities();
    List<Facility> searchFacilitiesByName(String keyword);
    List<Facility> getFacilitiesByAvailability(boolean isAvailable);

    // ===== U =====
    boolean updateFacility(Facility facility);

    // Partial Update（JSON / Map）
    boolean partialUpdate(int id, Map<String, Object> updates);

    // Partial Update（multipart / form）
    boolean partialUpdate(int id, FacilityForm form);

    // 只更新圖片
    boolean updateImage(int id, MultipartFile image);
    boolean updateImagePath(int id, String imagePath);

    // ===== D =====
    boolean deleteFacility(int id);
}
