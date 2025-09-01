package com.ryanshiun.seniorscare.roomType.service.Impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ryanshiun.seniorscare.roomType.dao.RoomTypeFeatureDao;
import com.ryanshiun.seniorscare.roomType.service.RoomTypeFeatureService;

@Service
@Transactional
public class RoomTypeFeatureServiceImpl implements RoomTypeFeatureService {

    private final RoomTypeFeatureDao dao;

    public RoomTypeFeatureServiceImpl(RoomTypeFeatureDao dao) {
        this.dao = dao;
    }

    @Override
    public int add(int roomTypeId, int featureId) {
        return dao.insert(roomTypeId, featureId);
    }

    @Override
    public int[] batchAdd(int roomTypeId, List<Integer> featureIds) {
        if (featureIds == null || featureIds.isEmpty()) return new int[0];
        return dao.batchInsert(roomTypeId, featureIds);
    }
    
    

    @Override
    public int deleteByRoomType(int roomTypeId) {
        return dao.deleteByRoomTypeId(roomTypeId);
    }

    @Override
    public int deleteByRoomTypeAndFeature(int roomTypeId, int featureId) {
        return dao.deleteByRoomTypeAndFeature(roomTypeId, featureId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Integer> findFeatureIdsByRoomType(int roomTypeId) {
        return dao.findFeatureIdsByRoomTypeId(roomTypeId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Integer> findRoomTypeIdsByFeature(int featureId) {
        return dao.findRoomTypeIdsByFeatureId(featureId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countFeaturesByRoomType(int roomTypeId) {
        return dao.countFeaturesByRoomTypeId(roomTypeId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countRoomTypesByFeature(int featureId) {
        return dao.countRoomTypesByFeatureId(featureId);
    }

    @Override
    public boolean replaceAllFeaturesForRoomType(int roomTypeId, List<Integer> featureIds) {
        // 先清空，再批量新增；視需求也可先比對差異做最小變更
        dao.deleteByRoomTypeId(roomTypeId);
        if (featureIds == null || featureIds.isEmpty()) return true;
        dao.batchInsert(roomTypeId, featureIds);
        return true;
    }
}