package com.ryanshiun.seniorscare.activity.service.Impl;

import com.ryanshiun.seniorscare.activity.dao.ActTagDAO;
import com.ryanshiun.seniorscare.activity.dao.ActivityCategoryDao;
import com.ryanshiun.seniorscare.activity.dao.ActivityDao;
import com.ryanshiun.seniorscare.activity.dao.ActivityTagDAO;
import com.ryanshiun.seniorscare.activity.dto.ActivityRequest;
import com.ryanshiun.seniorscare.activity.dto.ActivityForm;
import com.ryanshiun.seniorscare.activity.model.Activity;
import com.ryanshiun.seniorscare.activity.model.ActivityTag;
import com.ryanshiun.seniorscare.activity.service.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ActivityServiceImpl implements ActivityService {

    @Autowired
    private ActivityDao activityDao;
    @Autowired
    private ActivityCategoryDao activityCategoryDao;
    @Autowired
    private ActivityTagDAO activityTagDAO;
    @Autowired
    private ActTagDAO actTagDAO;

    // ===== 圖片上傳相關設定 (依照現有專案配置) =====
    // 統一圖片目錄與公開路徑（與前端一致）
    private static final File FS_UPLOAD_DIR = new File("uploads/images").getAbsoluteFile();
    private static final String PUBLIC_IMG_PREFIX = "images/"; // 存 DB 的相對路徑，配合現有的 /images/** 映射

    // ===== 原有的 CRUD 方法 =====
    @Override
    public Activity getActivityById(Integer id) {
        return activityDao.getActivityById(id);
    }

    @Override
    public List<Activity> getAllActivities() {
        return activityDao.getAllActivities();
    }

    @Override
    public void addActivity(ActivityRequest activityRequest) {
        String category = activityRequest.getCategory();
        if (category == null || category.isBlank() || !activityCategoryDao.existsByName(category)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "category 無效：請用 /api/activity-categories 回傳的其中一個"
            );
        }

        Activity activity = new Activity();
        activity.setName(activityRequest.getName());
        activity.setCategory(category);
        activity.setLimit(activityRequest.getLimit());
        activity.setCurrent(activityRequest.getCurrent());
        activity.setDate(activityRequest.getDate());
        activity.setEnd(activityRequest.getEnd());
        activity.setTime(activityRequest.getTime());
        activity.setRegistrationStart(activityRequest.getRegistrationStart());
        activity.setRegistrationEnd(activityRequest.getRegistrationEnd());
        activity.setLocation(activityRequest.getLocation());
        activity.setLatitude(activityRequest.getLatitude());
        activity.setLongitude(activityRequest.getLongitude());
        activity.setInstructor(activityRequest.getInstructor());
        activity.setStatus(activityRequest.getStatus());
        activity.setDescription(activityRequest.getDescription());
        activity.setImage(activityRequest.getImage());

        activityDao.addActivity(activity);
    }

    // ===== 新增：支援圖片上傳的活動新增方法 =====
    /**
     * 新增活動：用表單建構（支援圖片處理）
     * 模仿 RoomTypeServiceImpl.addRoomWithImage
     */
    @Override
    public void addActivityWithImage(ActivityForm form) {
        // 驗證活動分類
        String category = form.getCategory();
        if (category == null || category.isBlank() || !activityCategoryDao.existsByName(category)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "category 無效：請用 /api/activity-categories 回傳的其中一個"
            );
        }

        Activity activity = new Activity();
        activity.setName(form.getName());
        activity.setCategory(category);
        activity.setLimit(form.getLimit() == null ? 30 : form.getLimit());
        activity.setCurrent(form.getCurrent() == null ? 0 : form.getCurrent());
        activity.setDate(form.getDate());
        activity.setEnd(form.getEnd());
        activity.setTime(form.getTime());
        activity.setRegistrationStart(form.getRegistrationStart());
        activity.setRegistrationEnd(form.getRegistrationEnd());
        activity.setLocation(form.getLocation());
        activity.setLatitude(form.getLatitude());
        activity.setLongitude(form.getLongitude());
        activity.setInstructor(form.getInstructor());
        activity.setStatus(Boolean.TRUE.equals(form.getStatus()));
        activity.setDescription(form.getDescription());

        // 處理圖片上傳
        String imagePath = resolveImagePathFromForm(form);
        if (imagePath != null && !imagePath.isBlank()) {
            activity.setImage(imagePath);
        }

        activityDao.addActivity(activity);
    }

    @Override
    public void updateActivity(Integer id, Activity activity) {
        String category = activity.getCategory();
        if (category == null || category.isBlank() || !activityCategoryDao.existsByName(category)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "category 無效：請用 /api/activity-categories 回傳的其中一個"
            );
        }
        activityDao.updateActivity(id, activity);
    }

    // ===== 新增：支援圖片上傳的活動更新方法 =====
    /**
     * 部分更新活動（支援圖片處理）
     * 模仿 RoomTypeServiceImpl.partialUpdate
     */
    @Override
    public boolean partialUpdateActivity(Integer id, ActivityForm form) {
        Activity activity = activityDao.getActivityById(id);
        if (activity == null) return false;

        // 更新各個欄位（非 null 才更新）
        if (form.getName() != null) activity.setName(form.getName());
        if (form.getCategory() != null) {
            // 驗證分類是否有效
            if (!activityCategoryDao.existsByName(form.getCategory())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "category 無效：請用 /api/activity-categories 回傳的其中一個"
                );
            }
            activity.setCategory(form.getCategory());
        }
        if (form.getLimit() != null) activity.setLimit(form.getLimit());
        if (form.getCurrent() != null) activity.setCurrent(form.getCurrent());
        if (form.getDate() != null) activity.setDate(form.getDate());
        if (form.getEnd() != null) activity.setEnd(form.getEnd());
        if (form.getTime() != null) activity.setTime(form.getTime());
        if (form.getRegistrationStart() != null) activity.setRegistrationStart(form.getRegistrationStart());
        if (form.getRegistrationEnd() != null) activity.setRegistrationEnd(form.getRegistrationEnd());
        if (form.getLocation() != null) activity.setLocation(form.getLocation());
        if (form.getLatitude() != null) activity.setLatitude(form.getLatitude());
        if (form.getLongitude() != null) activity.setLongitude(form.getLongitude());
        if (form.getInstructor() != null) activity.setInstructor(form.getInstructor());
        if (form.getStatus() != null) activity.setStatus(form.getStatus());
        if (form.getDescription() != null) activity.setDescription(form.getDescription());

        // 處理圖片上傳
        String imagePath = resolveImagePathFromForm(form);
        if (imagePath != null && !imagePath.isBlank()) {
            activity.setImage(imagePath);
        }

        activityDao.updateActivity(id, activity);
        return true;
    }

    @Override
    public void deleteActivityById(Integer id) {
        activityDao.deleteActivityById(id);
    }

    // ===== 標籤查詢 =====
    @Override
    public List<Activity> getActivitiesByTag(String tagName) {
        return actTagDAO.findActivitiesBySingleTag(tagName);
    }

    // 取得/建立標籤 id
    private Integer ensureTagId(String raw) {
        if (raw == null) return null;
        String name = raw.trim();
        if (name.isEmpty()) return null;
        return activityTagDAO.findByName(name)
                .map(ActivityTag::getId)
                .orElseGet(() -> activityTagDAO.insertTag(name));
    }

    // 覆蓋某活動的所有標籤
    @Override
    @Transactional
    public void addTagsToActivity(int activityId, List<String> tagNames) {
        actTagDAO.deleteByActivityId(activityId);
        if (tagNames == null) return;

        LinkedHashSet<String> cleaned = tagNames.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        for (String t : cleaned) {
            Integer tagId = ensureTagId(t);
            if (tagId != null) {
                actTagDAO.insert(activityId, tagId);
            }
        }
    }

    // 結束報名，將 status 設為 false
    @Override
    public void endRegistration(Integer id) {
        activityDao.endRegistration(id);
    }

    // ====== 圖片處理相關方法 (模仿 RoomTypeServiceImpl) ======

    /**
     * 決定 imagePath 來源：優先檔案，其次字串路徑（與前端一致的公開路徑）
     */
    private String resolveImagePathFromForm(ActivityForm form) {
        MultipartFile file = form.getImage();
        if (file != null && !file.isEmpty()) {
            try {
                return saveImage(file); // 回傳 images/{filename}
            } catch (IOException e) {
                throw new RuntimeException("Save image failed: " + e.getMessage(), e);
            }
        }
        if (form.getImagePath() != null && !form.getImagePath().isBlank()) {
            return form.getImagePath().trim();
        }
        return null;
    }

    /**
     * 寫入 uploads/images；DB 存 images/{filename}
     * 依照現有專案的配置：所有圖片都存在 uploads/images 根目錄
     */
    private String saveImage(MultipartFile file) throws IOException {
        // 建立目錄（如不存在）
        if (!FS_UPLOAD_DIR.exists()) Files.createDirectories(FS_UPLOAD_DIR.toPath());

        // 取得原始檔名並清理
        String original = Optional.ofNullable(file.getOriginalFilename()).orElse("upload");
        original = java.nio.file.Paths.get(original).getFileName().toString();

        // 分離副檔名
        String ext = "";
        int dot = original.lastIndexOf('.');
        if (dot >= 0 && dot < original.length() - 1) {
            ext = original.substring(dot).toLowerCase(); // .png
            original = original.substring(0, dot);
        }

        // 清理檔名：支援中文，移除特殊字符，並加上 activity 前綴以區分用途
        String base = "activity_" + original
                .replaceAll("[\\s]+", "_")
                .replaceAll("[^0-9A-Za-z\\u4e00-\\u9fa5._-]", "_")
                .trim();
        if (base.equals("activity_")) base = "activity_file";
        if (base.length() > 200) base = base.substring(0, 200);

        // 防止檔名重複覆蓋
        String filename = base + ext;
        File dest = new File(FS_UPLOAD_DIR, filename);
        int i = 1;
        while (dest.exists()) {
            filename = base + "-" + i + ext;
            dest = new File(FS_UPLOAD_DIR, filename);
            i++;
        }

        // 儲存檔案
        file.transferTo(dest);

        // 回傳 DB 儲存的相對路徑，配合現有的 /images/** 靜態資源映射
        return PUBLIC_IMG_PREFIX + filename; // 例如: images/activity_photo1.png
    }
}