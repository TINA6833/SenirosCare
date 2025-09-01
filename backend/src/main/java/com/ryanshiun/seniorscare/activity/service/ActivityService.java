package com.ryanshiun.seniorscare.activity.service;

import com.ryanshiun.seniorscare.activity.dto.ActivityRequest;
import com.ryanshiun.seniorscare.activity.dto.ActivityForm;
import com.ryanshiun.seniorscare.activity.model.Activity;

import java.util.List;

/**
 * 活動服務介面
 * 新增支援圖片上傳的方法
 */
public interface ActivityService {

    // ===== 原有的 CRUD 方法 =====
    Activity getActivityById(Integer id);
    List<Activity> getAllActivities();
    void addActivity(ActivityRequest activityRequest);
    void updateActivity(Integer id, Activity activity);
    void deleteActivityById(Integer id);

    // ===== 新增：支援圖片上傳的方法 =====
    /**
     * 新增活動（支援圖片上傳）
     * @param form 活動表單資料（含圖片檔案或路徑）
     */
    void addActivityWithImage(ActivityForm form);

    /**
     * 部分更新活動（支援圖片上傳）
     * @param id 活動 ID
     * @param form 活動表單資料（含圖片檔案或路徑）
     * @return 是否更新成功
     */
    boolean partialUpdateActivity(Integer id, ActivityForm form);

    // ===== 標籤相關方法 =====
    List<Activity> getActivitiesByTag(String tagName);
    void addTagsToActivity(int activityId, List<String> tagNames);

    // ===== 其他方法 =====
    void endRegistration(Integer id);
}