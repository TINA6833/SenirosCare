package com.ryanshiun.seniorscare.roomType.service;

import java.util.List;
import java.util.Optional;

import com.ryanshiun.seniorscare.roomType.model.Feature;

public interface FeatureService {

    // C
    Integer add(Feature feature);
    int[] batchAdd(List<Feature> features);

    // U
    boolean update(Feature feature);

    // D
    boolean deleteById(int id);

    // R
    Optional<Feature> findById(int id);
    List<Feature> findAll();
    List<Feature> findByNameLike(String keyword);

    // utils
    boolean existsByName(String name);
    long count();
    
    boolean isFeatureUsed(int featureId);
}
