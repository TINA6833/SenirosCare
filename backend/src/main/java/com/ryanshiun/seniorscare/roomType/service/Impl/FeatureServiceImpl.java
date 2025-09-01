package com.ryanshiun.seniorscare.roomType.service.Impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ryanshiun.seniorscare.roomType.dao.FeatureDao;
import com.ryanshiun.seniorscare.roomType.model.Feature;
import com.ryanshiun.seniorscare.roomType.service.FeatureService;

@Service
@Transactional
public class FeatureServiceImpl implements FeatureService {

    private final FeatureDao dao;

    public FeatureServiceImpl(FeatureDao dao) {
        this.dao = dao;
    }

    // ===== C =====
    @Override
    public Integer add(Feature feature) {
        // 如需避免重複名稱，可以在這裡先檢查：
        // if (dao.existsByName(feature.getName())) throw new IllegalArgumentException("Feature name exists");
        return dao.insert(feature);
    }

    @Override
    public int[] batchAdd(List<Feature> features) {
        if (features == null || features.isEmpty()) return new int[0];
        return dao.batchInsert(features);
    }

    // ===== U =====
    @Override
    public boolean update(Feature feature) {
        return dao.update(feature);
    }

    // ===== D =====
    @Override
    public boolean deleteById(int id) {
        return dao.deleteById(id);
    }

    // ===== R =====
    @Override
    @Transactional(readOnly = true)
    public Optional<Feature> findById(int id) {
        return dao.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Feature> findAll() {
        return dao.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Feature> findByNameLike(String keyword) {
        return dao.findByNameLike(keyword);
    }

    // ===== utils =====
    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return dao.existsByName(name);
    }

    @Override
    @Transactional(readOnly = true)
    public long count() {
        return dao.count();
    }
    public boolean isFeatureUsed(int featureId) {
        return dao.countFeatureUsage(featureId) > 0;
    }


}