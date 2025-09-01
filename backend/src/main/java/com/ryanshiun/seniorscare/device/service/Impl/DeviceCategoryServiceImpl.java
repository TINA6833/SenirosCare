package com.ryanshiun.seniorscare.device.service.Impl;

import com.ryanshiun.seniorscare.device.dao.DeviceCategoryDao;
import com.ryanshiun.seniorscare.device.model.DeviceCategory;
import com.ryanshiun.seniorscare.device.service.DeviceCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class DeviceCategoryServiceImpl implements DeviceCategoryService {

    @Autowired
    private DeviceCategoryDao deviceCategoryDao;

    /**
     * 查詢所有分類資料（預設依 category_id 排序）
     */
    @Override
    public List<DeviceCategory> findAll() {
        return deviceCategoryDao.findAll();
    }

    /**
     * 根據主鍵 ID 查詢分類資料
     */
    @Override
    public DeviceCategory findById(Integer id) {
        return deviceCategoryDao.findById(id);
    }

    /**
     * 新增一筆分類資料，回傳自動生成的主鍵 ID
     */
    @Override
    public Integer insert(DeviceCategory category) {
        return deviceCategoryDao.insert(category);
    }

    /**
     * 根據 category.id 更新分類名稱與排序編號
     */
    @Override
    public boolean update(DeviceCategory category) {
        return deviceCategoryDao.update(category);
    }

    /**
     * 根據主鍵 ID 刪除分類資料
     */
    @Override
    public boolean deleteById(Integer id) {
        return deviceCategoryDao.deleteById(id);
    }

    /**
     * 檢查指定分類 ID 是否存在
     */
    @Override
    public boolean existsById(Integer id) {
        return deviceCategoryDao.existsById(id);
    }

    /**
     * 模糊查詢名稱包含關鍵字的分類清單
     */
    @Override
    public List<DeviceCategory> searchByName(String keyword) {
        return deviceCategoryDao.searchByName(keyword);
    }

    /**
     * 查詢分類總筆數
     */
    @Override
    public int count() {
        return deviceCategoryDao.count();
    }
}
