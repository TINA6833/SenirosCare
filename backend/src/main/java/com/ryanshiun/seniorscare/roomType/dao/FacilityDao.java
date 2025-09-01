package com.ryanshiun.seniorscare.roomType.dao;

import java.util.List;

import com.ryanshiun.seniorscare.roomType.model.Facility;

public interface FacilityDao {

    // 新增設施
    int addFacility(Facility facility);

    // 依ID查設施（查不到回傳 null）
    Facility getFacilityById(int id);

    // 查詢所有設施
    List<Facility> getAllFacilities();

    // 更新設施
    boolean updateFacility(Facility facility);

    // 刪除設施(依id)
    boolean deleteFacility(int id);

    // 查詢啟用中的設施 (is_available = 1)
    List<Facility> getAvailableFacilities();

    // 依名稱模糊查詢
    List<Facility> searchFacilitiesByName(String keyword);

    // 依可用狀態查詢
    List<Facility> getFacilitiesByAvailability(boolean isAvailable);
}