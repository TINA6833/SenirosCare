package com.ryanshiun.seniorscare.activity.dao;

import java.util.List;
import com.ryanshiun.seniorscare.activity.model.ActivityCategory;

public interface ActivityCategoryDao {
    List<ActivityCategory> findAllActive();
    boolean existsByName(String name);
}
