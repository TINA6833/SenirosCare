package com.ryanshiun.seniorscare.device.service;

import com.ryanshiun.seniorscare.device.dto.DeviceResponse;
import com.ryanshiun.seniorscare.device.model.Device;

import java.net.URISyntaxException;
import java.util.List;

public interface DeviceService {

    // 根據 ID 取得單一輔具
    DeviceResponse getDeviceById(Integer id);

    // 取得所有輔具
    List<DeviceResponse> getAllDevices();

    // 依分類 ID 取得所有輔具
    List<DeviceResponse> getDevicesByCategoryId(Integer categoryId);

    // 依名稱模糊搜尋輔具
    List<DeviceResponse> searchDevicesByName(String keyword);

    // 新增輔具
    Integer addDevice(Device device);

    // 更新輔具（依 id）
    boolean updateDevice(Device device) throws URISyntaxException;

    // 刪除輔具
    boolean deleteDeviceById(Integer id);

    // 檢查設備是否存在
    boolean existsById(Integer id);

    // 查詢總筆數
    int countDevices();

    // 依指定欄位排序取得所有輔具（名稱、價格、庫存）
    List<DeviceResponse> getAllDevicesSorted(String sortBy);

    // 分頁查詢輔具
    List<DeviceResponse> getDevicesPaged(int offset, int limit);

    // 分頁 + 排序查詢輔具
    List<DeviceResponse> getDevicesPagedAndSorted(int offset, int limit, String sortBy);

    // 批次功能
    boolean updateDevices(List<Device> devices);
    boolean deleteDevicesByIds(List<Integer> ids);

    // 匯入 / 匯出 CSV
    String exportDevicesToCsv(); // 可改為直接回傳字串
    boolean importDevicesFromCsv(String csvContent);

    // 查詢指定商品是否有庫存
    boolean checkInStock(Integer deviceId, Integer qty);

    //批次修改多筆輔具
    boolean updateDevicesBatch(List<Device> devices);

    //批次刪除多筆輔具
    boolean deleteDevicesBatch(List<Integer> ids);

    /**
     * 更新裝置的圖片檔名
     * @param id 裝置 ID
     * @param filename 儲存在伺服器上的檔名
     * @return 更新是否成功
     */
    boolean updateDeviceImage(Integer id, String filename);
}
