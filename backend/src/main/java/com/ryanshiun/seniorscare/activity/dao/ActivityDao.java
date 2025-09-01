package com.ryanshiun.seniorscare.activity.dao;

import com.ryanshiun.seniorscare.activity.dto.ActivityQueryParams;
import com.ryanshiun.seniorscare.activity.model.Activity;

import java.util.List;

public interface ActivityDao {

    Activity getActivityById(Integer id);

    List<Activity> getAllActivities();

    void addActivity(Activity activity);

    void updateActivity(Integer id, Activity activity);

    void deleteActivityById(Integer id);
    
    List<Activity> getActivities(ActivityQueryParams activityqueryParams);

    int holdSeatsForRegistration(Integer activityId, int num, java.time.LocalDate today);

    int adjustSeats(Integer activityId, int delta, java.time.LocalDate today);


    void endRegistration(Integer id);
}