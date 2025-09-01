package com.ryanshiun.seniorscare.device.dao.Impl;

import com.ryanshiun.seniorscare.device.dao.DeviceDao;
import com.ryanshiun.seniorscare.device.model.Device;
import com.ryanshiun.seniorscare.device.rowmapper.DeviceRowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.core.namedparam.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import org.springframework.stereotype.Component;


@Component
public class DeviceDaoImpl implements DeviceDao {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    // 建立 RowMapper 實例
    private RowMapper<Device> rowMapper = new DeviceRowMapper();

    // 新增設備，並回傳自動產生的主鍵 ID
    @Override
    public Integer insert(Device device) {
        String sql = "INSERT INTO device (name, sku, unit_price, inventory, description, image, is_online, category_id, created_by_emp_id) " +
                "VALUES (:name, :sku, :unitPrice, :inventory, :description, :image, :isOnline, :categoryId, :createdByEmpId)";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("name", device.getName());
        params.addValue("sku", device.getSku());
        params.addValue("unitPrice", device.getUnitPrice());
        params.addValue("inventory", device.getInventory());
        params.addValue("description", device.getDescription());
        params.addValue("image", device.getImage());
        params.addValue("isOnline", device.getIsOnline());
        params.addValue("categoryId", device.getCategoryId());
        params.addValue("createdByEmpId", device.getCreatedByEmpId());

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(sql, params, keyHolder);
        return keyHolder.getKey().intValue();
    }

    // 更新指定設備資料
    @Override
    public boolean update(Device device) {
        String sql = "UPDATE device SET name = :name, sku = :sku, unit_price = :unitPrice, inventory = :inventory, " +
                "description = :description, image = :image, is_online = :isOnline, category_id = :categoryId, " +
                "created_by_emp_id = :createdByEmpId " +  "WHERE id = :id";

        Map<String, Object> params = new HashMap<>();
        params.put("id", device.getId());
        params.put("name", device.getName());
        params.put("sku", device.getSku());
        params.put("unitPrice", device.getUnitPrice());
        params.put("inventory", device.getInventory());
        params.put("description", device.getDescription());
        params.put("image", device.getImage());
        params.put("isOnline", device.getIsOnline());
        params.put("categoryId", device.getCategoryId());
        params.put("createdByEmpId", device.getCreatedByEmpId());

        return jdbcTemplate.update(sql, params) > 0;
    }

    // 根據 ID 刪除設備
    @Override
    public boolean deleteById(Integer id) {
        String sql = "DELETE FROM device WHERE id = :id";
        Map<String, Object> params = Map.of("id", id);
        return jdbcTemplate.update(sql, params) > 0;
    }

    // 根據 ID 查詢設備資料（單筆）
    @Override
    public Device findById(Integer id) {
        String sql = "SELECT * FROM device WHERE id = :id";
        List<Device> list = jdbcTemplate.query(sql, Map.of("id", id), new DeviceRowMapper());
        return list.isEmpty() ? null : list.get(0);
    }

    // 查詢所有設備（預設依名稱排序）
    @Override
    public List<Device> findAll() {
        String sql = "SELECT * FROM device ORDER BY name ASC";
        return jdbcTemplate.query(sql, new DeviceRowMapper());
    }

    // 根據分類 ID 查詢所有設備
    @Override
    public List<Device> findByCategoryId(Integer categoryId) {
        String sql = "SELECT * FROM device WHERE category_id = :categoryId";
        return jdbcTemplate.query(sql, Map.of("categoryId", categoryId), new DeviceRowMapper());
    }



    // 依設備名稱模糊搜尋
    @Override
    public List<Device> searchByName(String keyword) {
        String sql = "SELECT * FROM device WHERE name LIKE :keyword";
        return jdbcTemplate.query(sql, Map.of("keyword", "%" + keyword + "%"), new DeviceRowMapper());
    }

    // 查詢設備總數
    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM device";
        return jdbcTemplate.queryForObject(sql, new HashMap<>(), Integer.class);
    }

    // 檢查某設備 ID 是否存在
    @Override
    public boolean existsById(Integer id) {
        String sql = "SELECT COUNT(*) FROM device WHERE id = :id";
        Integer count = jdbcTemplate.queryForObject(sql, Map.of("id", id), Integer.class);
        return count != null && count > 0;
    }

    // 查詢所有設備，並依指定欄位排序（unit_price、inventory、name 等）
    @Override
    public List<Device> findAllSorted(String sortBy) {
        String sql = "SELECT * FROM device ORDER BY " + sortBy;
        return jdbcTemplate.query(sql, new DeviceRowMapper());
    }

    // 分頁查詢設備資料
    @Override
    public List<Device> findPage(int offset, int limit) {
        String sql = "SELECT * FROM device ORDER BY id OFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY";
        Map<String, Object> map = new HashMap<>();
        map.put("offset", offset);
        map.put("limit", limit);
        return jdbcTemplate.query(sql, map, rowMapper);
    }

    //分頁並依指定欄位排序查詢商品資料
    @Override
    public List<Device> findPagedAndSorted(int offset, int limit, String sortBy) {
        List<String> allowedFields = List.of("id", "name", "unit_price", "inventory");
        if (!allowedFields.contains(sortBy)) {
            sortBy = "id"; // fallback 預設排序欄位
        }

        String sql = "SELECT * FROM device ORDER BY " + sortBy + " OFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY";
        Map<String, Object> map = new HashMap<>();
        map.put("offset", offset);
        map.put("limit", limit);
        return jdbcTemplate.query(sql, map, rowMapper);
    }


    // 匯入 CSV：第一行為 header，之後每行依序：name,sku,unitPrice,inventory,description,image,isOnline,categoryId,createdByEmpId
    @Override
    public boolean importDevicesFromCsv(String csvContent) {
        try (BufferedReader reader = new BufferedReader(new StringReader(csvContent))) {
            // 跳過表頭
            String header = reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] cols = line.split(",");
                // 欄位依序對應上面 header
                Device device = Device.builder()
                        .name(cols[0])
                        .sku(cols[1])
                        .unitPrice(new BigDecimal(cols[2]))
                        .inventory(Integer.parseInt(cols[3]))
                        .description(cols[4])
                        .image(cols[5])
                        .isOnline(Boolean.parseBoolean(cols[6]))
                        .categoryId(Integer.parseInt(cols[7]))
                        .createdByEmpId(Integer.parseInt(cols[8]))
                        .build();

                // 利用已有的 insert 方法逐筆寫入
                insert(device);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    //匯出 CSV：輸出同樣的欄位順序，第一行表頭
    @Override
    public String exportDevicesToCsv() {
        List<Device> devices = findAll();  // 或 jdbcTemplate 詢問所有資料
        StringBuilder sb = new StringBuilder();

        // 加上表頭
        sb.append("name,sku,unitPrice,inventory,description,image,isOnline,categoryId,createdByEmpId\n");

        // 逐筆 append
        for (Device d : devices) {
            sb.append(d.getName()).append(",")
                    .append(d.getSku()).append(",")
                    .append(d.getUnitPrice()).append(",")
                    .append(d.getInventory()).append(",")
                    .append(d.getDescription()).append(",")
                    .append(d.getImage()).append(",")
                    .append(d.getIsOnline()).append(",")
                    .append(d.getCategoryId()).append(",")
                    .append(d.getCreatedByEmpId())
                    .append("\n");
        }
        return sb.toString();
    }

    /**
     * 批次更新多筆 Device 資料
     */
    @Override
    public int updateDevicesBatch(List<Device> devices) {
        String sql = """
            UPDATE device
               SET name = :name,
                   sku = :sku,
                   unit_price = :unitPrice,
                   inventory = :inventory,
                   description = :description,
                   image = :image,
                   is_online = :isOnline,
                   category_id = :categoryId,
                   created_by_emp_id = :createdByEmpId
             WHERE id = :id
            """;

        SqlParameterSource[] batchParams = devices.stream()
                .map(d -> new MapSqlParameterSource()
                        .addValue("id", d.getId())
                        .addValue("name", d.getName())
                        .addValue("sku", d.getSku())
                        .addValue("unitPrice", d.getUnitPrice())
                        .addValue("inventory", d.getInventory())
                        .addValue("description", d.getDescription())
                        .addValue("image", d.getImage())
                        .addValue("isOnline", d.getIsOnline())
                        .addValue("categoryId", d.getCategoryId())
                        .addValue("createdByEmpId", d.getCreatedByEmpId())
                )
                .toArray(SqlParameterSource[]::new);

        int[] counts = jdbcTemplate.batchUpdate(sql, batchParams);
        return counts.length;
    }

    /**
     * 批次刪除多筆 Device
     */
    @Override
    public int deleteDevicesBatch(List<Integer> ids) {
        String sql = "DELETE FROM device WHERE id IN (:ids)";
        MapSqlParameterSource params = new MapSqlParameterSource("ids", ids);
        return jdbcTemplate.update(sql, params);
    }

    /**
     * 取得某商品當前單價
     */
    @Override
    public BigDecimal fetchUnitPrice(Integer deviceId) {
        String sql = "SELECT unit_price FROM device WHERE id = :id";
        return jdbcTemplate.queryForObject(
                sql,
                Map.of("id", deviceId),
                BigDecimal.class
        );
    }

    /**
     * 扣減某商品庫存
     */
    @Override
    public int decreaseInventory(Integer deviceId, Integer qty) {
        String sql = """
            UPDATE device
               SET inventory = inventory - :qty
             WHERE id = :id
               AND inventory >= :qty
            """;
        return jdbcTemplate.update(
                sql,
                Map.of("id", deviceId, "qty", qty)
        );
    }

    /**
     * 透過 SQL 更新 device.image 欄位
     */
    @Override
    public boolean updateImage(Integer id, String filename) {
        String sql = "UPDATE device SET image = :image WHERE id = :id";
        Map<String, Object> params = Map.of(
                "image", filename,
                "id", id
        );
        // update 回傳影響 row 數，等於 1 表示成功
        return jdbcTemplate.update(sql, params) == 1;
    }


}
