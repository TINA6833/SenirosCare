package com.ryanshiun.seniorscare.activity.dao;
import java.util.Optional;
import java.util.List;
import com.ryanshiun.seniorscare.activity.model.ActivityTag;
public interface ActivityTagDAO {
    Optional<ActivityTag> findByName(String name);

    Integer insertTag(String name);

    List<ActivityTag> findByActivityId(int activityId);
}