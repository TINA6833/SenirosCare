package com.ryanshiun.seniorscare.device.service.Impl;

import com.ryanshiun.seniorscare.device.dao.DeviceDao;
import com.ryanshiun.seniorscare.device.dto.DeviceResponse;
import com.ryanshiun.seniorscare.device.model.Device;
import com.ryanshiun.seniorscare.device.service.DeviceService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;


@Service // 標記為 Spring 服務層元件
public class DeviceServiceImpl implements DeviceService {

    @Autowired // 自動注入 DeviceDao 實例
    private DeviceDao deviceDao;

    /**
     * 取得所有輔具，並轉換成 DTO 回傳
     */
    @Override
    public List<DeviceResponse> getAllDevices() {
        return deviceDao.findAll() // 從 DAO 取得所有 Entity
                .stream()
                .map(this::toResponse) // Entity → DTO
                .collect(Collectors.toList()); // 收集成 List 返回
    }

    /**
     * 根據 ID 取得單一輔具，若不存在回傳 null
     */
    @Override
    public DeviceResponse getDeviceById(Integer id) {
        Device d = deviceDao.findById(id); // DAO 查詢
        return d == null ? null : toResponse(d); // 空值檢查 + 轉 DTO
    }

    /**
     * 新增輔具，回傳新生成的 ID
     */
    @Override
    public Integer addDevice(Device device) {
        return deviceDao.insert(device); // 呼叫 DAO 新增
    }

    /**
     * 更新輔具資料，回傳是否成功
     */
    @Override
    public boolean updateDevice(Device device) throws URISyntaxException {
        String oldUrl = device.getImage();

        // 去除 http
        String newUrl = Paths.get(new URI(oldUrl).getPath()).getFileName().toString();

        device.setImage(newUrl);
        return deviceDao.update(device); // 呼叫 DAO 更新操作
    }

    /**
     * 刪除指定 ID 的輔具，回傳是否成功
     */
    @Override
    public boolean deleteDeviceById(Integer id) {
        return deviceDao.deleteById(id); // 呼叫 DAO 刪除
    }

    /**
     * 檢查指定 ID 的輔具是否存在
     */
    @Override
    public boolean existsById(Integer id) {
        return deviceDao.existsById(id); // DAO 回傳存在狀態
    }

    /**
     * 根據分類 ID 取得該分類底下的所有輔具
     */
    @Override
    public List<DeviceResponse> getDevicesByCategoryId(Integer categoryId) {
        return deviceDao.findByCategoryId(categoryId) // DAO 查詢
                .stream()
                .map(this::toResponse) // Entity → DTO
                .collect(Collectors.toList());
    }

    /**
     * 依名稱模糊搜尋輔具
     */
    @Override
    public List<DeviceResponse> searchDevicesByName(String keyword) {
        return deviceDao.searchByName(keyword) // DAO 搜尋
                .stream()
                .map(this::toResponse) // 轉 DTO
                .collect(Collectors.toList());
    }

    /**
     * 取得輔具總數
     */
    @Override
    public int countDevices() {
        return deviceDao.count(); // DAO 回傳 count
    }

    /**
     * 依指定欄位排序並取得所有輔具
     */
    @Override
    public List<DeviceResponse> getAllDevicesSorted(String sortBy) {
        return deviceDao.findAllSorted(sortBy) // DAO 排序查詢
                .stream()
                .map(this::toResponse) // 轉 DTO
                .collect(Collectors.toList());
    }

    /**
     * 分頁查詢輔具
     */
    @Override
    public List<DeviceResponse> getDevicesPaged(int offset, int limit) {
        return deviceDao.findPage(offset, limit) // DAO 分頁查詢
                .stream()
                .map(this::toResponse) // 轉 DTO
                .collect(Collectors.toList());
    }

    /**
     * 分頁 + 排序查詢輔具
     */
    @Override
    public List<DeviceResponse> getDevicesPagedAndSorted(int offset, int limit, String sortBy) {
        return deviceDao.findPagedAndSorted(offset, limit, sortBy) // DAO 分頁排序
                .stream()
                .map(this::toResponse) // 轉 DTO
                .collect(Collectors.toList());
    }

    /**
     * 批次更新多筆輔具資料
     */
    @Override
    public boolean updateDevices(List<Device> devices) {
        // 全部更新成功才回傳 true
        return devices.stream().allMatch(deviceDao::update);
    }

    /**
     * 批次刪除多筆輔具資料
     */
    @Override
    public boolean deleteDevicesByIds(List<Integer> ids) {
        // 全部刪除成功才回傳 true
        return ids.stream().allMatch(deviceDao::deleteById);
    }

    /**
     * 匯入 CSV 字串，呼叫 DAO 執行解析與儲存
     */
    @Override
    public boolean importDevicesFromCsv(String csvContent) {
        return deviceDao.importDevicesFromCsv(csvContent); // 直接轉給 DAO
    }

    /**
     * 匯出所有輔具成 CSV 格式字串
     */
    @Override
    public String exportDevicesToCsv() {
        return deviceDao.exportDevicesToCsv(); // 直接轉給 DAO
    }

    /**
     * 私有工具：將 Entity 轉換成 DTO
     */
    private DeviceResponse toResponse(Device device) {
        DeviceResponse dto = new DeviceResponse();
        BeanUtils.copyProperties(device, dto); // 複製同名屬性
        return dto;
    }

    // 查詢指定商品是否有庫存
    public boolean checkInStock(Integer deviceId, Integer qty) {
        Device device = deviceDao.findById(deviceId);
        Integer inStock = device.getInventory();
        return (inStock - qty) >= 0;
    }

    /**
     * 批次更新多筆 Device，整個流程在同一事務中
     */
    @Override
    @Transactional
    public boolean updateDevicesBatch(List<Device> devices) {
        int updated = deviceDao.updateDevicesBatch(devices);
        // 確保所有筆數都更新成功
        return updated == devices.size();
    }

    /**
     * 批次刪除多筆 Device，整個流程在同一事務中
     */
    @Override
    @Transactional
    public boolean deleteDevicesBatch(List<Integer> ids) {
        int deleted = deviceDao.deleteDevicesBatch(ids);
        // 確保所有筆數都刪除成功
        return deleted == ids.size();
    }

    /**
     * 實作更新圖片的服務
     */
    @Override
    public boolean updateDeviceImage(Integer id, String filename) {
        // 呼叫 DAO 更新 image 欄位
        return deviceDao.updateImage(id, filename);
    }

}
