package com.ryanshiun.seniorscare.device.dao;

import com.ryanshiun.seniorscare.device.model.Device;

import java.math.BigDecimal;
import java.util.List;


public interface DeviceDao {

    // 新增商品，回傳自動生成的 ID
    Integer insert(Device device);

    // 更新指定商品
    boolean update(Device device);

    // 根據 ID 刪除商品
    boolean deleteById(Integer id);

    // 根據 ID 查詢商品（含分類資訊可用 JOIN 實作）
    Device findById(Integer id);

    // 查詢所有商品（預設依名稱排序）
    List<Device> findAll();

    // 查詢特定分類下的所有商品
    List<Device> findByCategoryId(Integer categoryId);

    // 模糊查詢商品名稱（LIKE %keyword%）
    List<Device> searchByName(String keyword);

    // 查詢商品總筆數
    int count();

    // 查詢是否存在指定商品 ID
    boolean existsById(Integer id);

    // 查詢所有商品並依指定欄位排序（如 unit_price, inventory）
    List<Device> findAllSorted(String sortBy);

    // 分頁查詢商品（offset 起始筆數, limit 筆數）
    List<Device> findPage(int offset, int limit);

    //分頁並依指定欄位排序查詢商品資料
    List<Device> findPagedAndSorted(int offset, int limit, String sortBy);

    //CSV 匯入／匯出
    boolean importDevicesFromCsv(String csvContent);
    String exportDevicesToCsv();

    //批次修改多筆輔具
    int updateDevicesBatch(List<Device> devices);

    //批次刪除多筆輔具
    int deleteDevicesBatch(List<Integer> ids);

    /**
     * 取得某商品當前單價
     * @param deviceId 商品 ID
     * @return 該商品的 unit_price (BigDecimal)
     */
    BigDecimal fetchUnitPrice(Integer deviceId);

    /**
     * 扣減某商品庫存
     * @param deviceId 商品 ID
     * @param qty      要扣除的數量
     * @return 更新影響的筆數（=1 表示成功）
     */
    int decreaseInventory(Integer deviceId, Integer qty);

    /**
     * 更新裝置資料表的 image 欄位
     * @param id 裝置 ID
     * @param filename 圖片檔名
     * @return 更新是否成功
     */
    boolean updateImage(Integer id, String filename);



}
