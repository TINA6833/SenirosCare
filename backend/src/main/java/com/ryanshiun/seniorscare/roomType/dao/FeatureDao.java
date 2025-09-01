package com.ryanshiun.seniorscare.roomType.dao;

import java.util.List;
import java.util.Optional;

import com.ryanshiun.seniorscare.roomType.model.Feature;

public interface FeatureDao {

    Integer insert(Feature feature);

    int[] batchInsert(List<Feature> features);

    boolean update(Feature feature);

    boolean deleteById(int id);

    Optional<Feature> findById(int id);

    List<Feature> findAll();

    List<Feature> findByNameLike(String keyword);

    boolean existsByName(String name);

    long count();
    
    int countFeatureUsage(int featureId);
}
