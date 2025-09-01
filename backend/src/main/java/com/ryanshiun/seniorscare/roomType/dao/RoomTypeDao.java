package com.ryanshiun.seniorscare.roomType.dao;

import com.ryanshiun.seniorscare.roomType.model.RoomType;

import java.util.List;
public interface RoomTypeDao {

    // 新增房型
    int insert(RoomType roomType);

    // 查全部房型
    List<RoomType> findAll();

    // 依 ID 查房型
    RoomType findById(int id);

    // 依價格區間查房型
    List<RoomType> findByPriceRange(int minPrice, int maxPrice);

    // 依關鍵字（描述或名稱）查房型
    List<RoomType> findByKeyword(String keyword);

    // 依人數查房型
    List<RoomType> findByCapacity(int capacity);

    // 更新房型
    boolean update(RoomType roomType);

    // 刪除房型
    boolean delete(int id);

    // 分頁查詢
    List<RoomType> findPaged(int offset, int limit);

    // 查詢總筆數（分頁用）
    int countAll();
    
    List<RoomType> findFilteredPaged(
            String keyword,
            Integer minPrice,
            Integer maxPrice,
            Integer capacity,
            List<Integer> featureIds,
            boolean matchAll,
            String sortBy,
            String order,
            int offset,
            int limit
    );

    int countFiltered(
            String keyword,
            Integer minPrice,
            Integer maxPrice,
            Integer capacity,
            List<Integer> featureIds,
            boolean matchAll
    );
}
