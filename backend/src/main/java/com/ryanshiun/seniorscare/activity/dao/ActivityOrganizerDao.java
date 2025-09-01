package com.ryanshiun.seniorscare.activity.dao;

import java.util.List;
import com.ryanshiun.seniorscare.activity.model.ActivityOrganizer;

public interface ActivityOrganizerDao {
    List<ActivityOrganizer> findByActivityId(Integer activityId);
    Integer insert(ActivityOrganizer organizer);
}
