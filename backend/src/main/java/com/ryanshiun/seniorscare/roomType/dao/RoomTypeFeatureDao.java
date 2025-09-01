package com.ryanshiun.seniorscare.roomType.dao;

import java.util.List;

public interface RoomTypeFeatureDao {

    // 新增房型與特徵關聯
    int insert(int roomTypeId, int featureId);

    // 批量新增房型與特徵關聯
    int[] batchInsert(int roomTypeId, List<Integer> featureIds);

    // 刪除指定房型的所有特徵關聯
    int deleteByRoomTypeId(int roomTypeId);

    // 刪除指定房型與特徵的關聯
    int deleteByRoomTypeAndFeature(int roomTypeId, int featureId);

    // 查詢某房型擁有的特徵 ID 清單
    List<Integer> findFeatureIdsByRoomTypeId(int roomTypeId);

    // 查詢擁有某特徵的房型 ID 清單
    List<Integer> findRoomTypeIdsByFeatureId(int featureId);

    // 計算房型的特徵數量
    long countFeaturesByRoomTypeId(int roomTypeId);

    // 計算擁有該特徵的房型數量
    long countRoomTypesByFeatureId(int featureId);
}