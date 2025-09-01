package com.ryanshiun.seniorscare.roomType.service;

import java.util.List;

public interface RoomTypeFeatureService {

    // 建立關聯
    int add(int roomTypeId, int featureId);

    // 批量建立關聯
    int[] batchAdd(int roomTypeId, List<Integer> featureIds);

    // 刪除某房型的所有關聯
    int deleteByRoomType(int roomTypeId);

    // 刪除指定房型與特徵的關聯
    int deleteByRoomTypeAndFeature(int roomTypeId, int featureId);

    // 查某房型擁有的特徵 ID
    List<Integer> findFeatureIdsByRoomType(int roomTypeId);

    // 查擁有某特徵的房型 ID
    List<Integer> findRoomTypeIdsByFeature(int featureId);

    // 計數
    long countFeaturesByRoomType(int roomTypeId);
    long countRoomTypesByFeature(int featureId);

    // 方便用法：用一組 featureIds 覆蓋該房型既有特徵
    boolean replaceAllFeaturesForRoomType(int roomTypeId, List<Integer> featureIds);
}