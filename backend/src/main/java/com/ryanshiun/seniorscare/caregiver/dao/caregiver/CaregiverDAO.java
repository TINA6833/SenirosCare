package com.ryanshiun.seniorscare.caregiver.dao.caregiver;

import com.ryanshiun.seniorscare.caregiver.model.Caregiver;
import java.util.List;
import java.util.Optional;

public interface CaregiverDAO {

    /**查詢全部照服員*/
    List<Caregiver> findAll();

    /**根據 ID 查詢照服員*/
    Optional<Caregiver> findById(Integer id);

    /**根據姓名模糊查詢照服員*/
    List<Caregiver> findByChineseNameContaining(String chineseName);

    /**根據電話查詢照服員*/
    Optional<Caregiver> findByPhone(String phone);

    /**根據狀態查詢照服員*/
    List<Caregiver> findByIsActive(Boolean isActive);

    /**新增照服員*/
    Caregiver save(Caregiver caregiver);

    /**更新照服員*/
    Caregiver update(Caregiver caregiver);

    /**刪除照服員*/
    boolean deleteById(Integer id);

    /** 檢查電話是否存在（排除指定ID）*/
    boolean existsByPhoneAndCaregiverIdNot(String phone, Integer caregiverId);

    /** 根據服務區域查詢照服員*/
    List<Caregiver> findByServiceAreaContaining(String serviceArea);

    /** 根據評價範圍查詢照服員*/
    List<Caregiver> findByAverageRatingBetween(Double minRating, Double maxRating);
}