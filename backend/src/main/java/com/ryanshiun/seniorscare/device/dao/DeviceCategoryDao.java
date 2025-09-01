package com.ryanshiun.seniorscare.device.dao;

import com.ryanshiun.seniorscare.device.model.DeviceCategory;

import java.util.List;


public interface DeviceCategoryDao {

    // 根據主鍵 ID 查詢分類
    DeviceCategory findById(Integer id);

    // 查詢所有分類資料（預設依 category_id 排序）
    List<DeviceCategory> findAll();

    // 新增分類資料，回傳自動生成的主鍵 ID
    Integer insert(DeviceCategory category);

    // 更新分類資料（根據 id 更新 name、category_id）
    boolean update(DeviceCategory category);

    // 根據主鍵 ID 刪除分類資料
    boolean deleteById(Integer id);

    // 檢查指定分類 ID 是否存在
    boolean existsById(Integer id);

    // 模糊查詢名稱包含關鍵字的分類清單
    List<DeviceCategory> searchByName(String keyword);

    // 查詢目前分類資料的總筆數
    int count();
}
