package com.ryanshiun.seniorscare.activity.dao;

import com.ryanshiun.seniorscare.activity.model.Activity;
import java.util.List;

public interface ActTagDAO {
    void deleteByActivityId(int activityId);              // 先清舊關聯
    void insert(int activityId, int tagId);               // 新增關聯
    List<Activity> findActivitiesBySingleTag(String tag); // 單一標籤
    List<Activity> findActivitiesByAnyTags(List<String> tags); // 任一符合
    List<Activity> findActivitiesByAllTags(List<String> tags); // 全部都要
}
