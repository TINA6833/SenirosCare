package com.ryanshiun.seniorscare.device.service;

import com.ryanshiun.seniorscare.device.model.DeviceCategory;

import java.util.List;

/**
 * DeviceCategoryService
 * 定義分類相關業務邏輯的操作
 */
public interface DeviceCategoryService {

    // 查詢所有分類資料
    List<DeviceCategory> findAll();

    // 根據 ID 查詢分類
    DeviceCategory findById(Integer id);

    // 新增分類資料
    Integer insert(DeviceCategory category);

    // 更新分類資料
    boolean update(DeviceCategory category);

    // 根據 ID 刪除分類資料
    boolean deleteById(Integer id);

    // 檢查分類是否存在
    boolean existsById(Integer id);

    // 模糊查詢分類名稱
    List<DeviceCategory> searchByName(String keyword);

    // 查詢分類總數
    int count();
}
