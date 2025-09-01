package com.ryanshiun.seniorscare.device.dao.Impl;

import com.ryanshiun.seniorscare.device.dao.DeviceCategoryDao;
import com.ryanshiun.seniorscare.device.model.DeviceCategory;
import com.ryanshiun.seniorscare.device.rowmapper.DeviceCategoryRowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.util.*;


@Component
public class DeviceCategoryDaoImpl implements DeviceCategoryDao {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    /**
     * 根據主鍵 ID 查詢分類資料，找不到則回傳 null
     */
    @Override
    public DeviceCategory findById(Integer id) {
        String sql = "SELECT * FROM devicecategory WHERE id = :id";
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        List<DeviceCategory> list = jdbcTemplate.query(sql, map, new DeviceCategoryRowMapper());
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * 查詢所有分類資料，預設依 category_id 升冪排序
     */
    @Override
    public List<DeviceCategory> findAll() {
        String sql = "SELECT * FROM devicecategory ORDER BY category_id";
        return jdbcTemplate.query(sql, new HashMap<>(), new DeviceCategoryRowMapper());
    }

    /**
     * 新增一筆分類資料，category 不需包含 ID，插入成功會回傳自動產生的主鍵 ID
     */
    @Override
    public Integer insert(DeviceCategory category) {
        String sql = "INSERT INTO devicecategory (name, category_id) VALUES (:name, :categoryId)";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("name", category.getName());
        params.addValue("categoryId", category.getCategoryId());

        // 使用 KeyHolder 取得新增後的主鍵 ID
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(sql, params, keyHolder);
        return Objects.requireNonNull(keyHolder.getKey()).intValue();
    }

    /**
     * 更新分類資料（根據 id 更新 name 與 category_id）
     * 成功更新則回傳 true
     */
    @Override
    public boolean update(DeviceCategory category) {
        String sql = "UPDATE devicecategory SET name = :name, category_id = :categoryId WHERE id = :id";
        Map<String, Object> map = new HashMap<>();
        map.put("id", category.getId());
        map.put("name", category.getName());
        map.put("categoryId", category.getCategoryId());
        return jdbcTemplate.update(sql, map) > 0;
    }

    /**
     * 根據主鍵 ID 刪除分類資料，成功則回傳 true
     */
    @Override
    public boolean deleteById(Integer id) {
        String sql = "DELETE FROM devicecategory WHERE id = :id";
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        return jdbcTemplate.update(sql, map) > 0;
    }

    /**
     * 檢查指定的分類 ID 是否存在
     */
    @Override
    public boolean existsById(Integer id) {
        String sql = "SELECT COUNT(*) FROM devicecategory WHERE id = :id";
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        Integer count = jdbcTemplate.queryForObject(sql, map, Integer.class);
        return count != null && count > 0;
    }

    /**
     * 模糊查詢名稱中包含關鍵字的分類清單（LIKE %keyword%）
     */
    @Override
    public List<DeviceCategory> searchByName(String keyword) {
        String sql = "SELECT * FROM devicecategory WHERE name LIKE :keyword";
        Map<String, Object> map = new HashMap<>();
        map.put("keyword", "%" + keyword + "%");
        return jdbcTemplate.query(sql, map, new DeviceCategoryRowMapper());
    }

    /**
     * 查詢目前 devicecategory 資料的總筆數
     */
    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM devicecategory";
        return jdbcTemplate.queryForObject(sql, new HashMap<>(), Integer.class);
    }
}
