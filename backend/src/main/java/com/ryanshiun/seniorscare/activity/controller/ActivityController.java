package com.ryanshiun.seniorscare.activity.controller;

import com.ryanshiun.seniorscare.activity.dto.ActivityRequest;
import com.ryanshiun.seniorscare.activity.dto.ActivityForm;
import com.ryanshiun.seniorscare.activity.model.Activity;
import com.ryanshiun.seniorscare.activity.service.ActivityService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activities")
@CrossOrigin
public class ActivityController {

    @Autowired
    private ActivityService activityService;

    // ===== Read 操作 =====

    /** 查詢所有活動 */
    @GetMapping
    public List<Activity> getAllActivities() {
        return activityService.getAllActivities();
    }

    /** 查詢單筆活動 */
    @GetMapping("/{id}")
    public ResponseEntity<Activity> getActivityById(@PathVariable Integer id) {
        Activity activity = activityService.getActivityById(id);
        if (activity != null) {
            return ResponseEntity.ok(activity);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // ===== Create 操作 =====

    /**
     * 新增活動（原有方法，不含圖片上傳）
     */
    @PostMapping
    public ResponseEntity<String> addActivity(@RequestBody @Valid ActivityRequest activityRequest) {
        try {
            activityService.addActivity(activityRequest);
            System.out.println("已新增活動");
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            System.err.println("新增活動失敗: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("錯誤: " + e.getMessage());
        }
    }


    /**
     * 全量更新活動（原有方法）
     */
    @PutMapping("/{id}")
    public ResponseEntity<String> updateActivity(@PathVariable Integer id,
                                                 @Valid @RequestBody ActivityRequest activityrequest) {
        Activity existing = activityService.getActivityById(id);
        if (existing != null) {
            Activity updated = new Activity();
            updated.setId(id); // 保留原 ID

            updated.setName(activityrequest.getName());
            updated.setCategory(activityrequest.getCategory());
            updated.setLimit(activityrequest.getLimit());
            updated.setCurrent(activityrequest.getCurrent());
            updated.setDate(activityrequest.getDate());
            updated.setEnd(activityrequest.getEnd());
            updated.setTime(activityrequest.getTime());
            updated.setRegistrationStart(activityrequest.getRegistrationStart());
            updated.setRegistrationEnd(activityrequest.getRegistrationEnd());
            updated.setLocation(activityrequest.getLocation());
            updated.setLatitude(activityrequest.getLatitude());
            updated.setLongitude(activityrequest.getLongitude());
            updated.setInstructor(activityrequest.getInstructor());
            updated.setStatus(activityrequest.getStatus());
            updated.setDescription(activityrequest.getDescription());
            updated.setImage(activityrequest.getImage());

            activityService.updateActivity(id, updated);
            return ResponseEntity.ok("更新成功");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 部分更新活動（支援圖片上傳，模仿 RoomTypeController 的 patchRoomWithImage）
     */
    @PatchMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> patchActivityWithImage(
            @PathVariable Integer id,
            @ModelAttribute ActivityForm form) {
        try {
            boolean success = activityService.partialUpdateActivity(id, form);
            if (success) {
                return ResponseEntity.ok("活動更新成功");
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println("更新活動失敗: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("錯誤: " + e.getMessage());
        }
    }

    // ===== Delete 操作 =====

    /** 刪除活動 */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteActivity(@PathVariable Integer id) {
        Activity existing = activityService.getActivityById(id);
        if (existing != null) {
            activityService.deleteActivityById(id);
            return ResponseEntity.ok("刪除成功");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // ===== 標籤相關操作 =====

    /** 新增/覆蓋某活動的標籤 */
    @PostMapping("/{id}/tags")
    public ResponseEntity<Void> saveTags(@PathVariable int id, @RequestBody List<String> tags) {
        activityService.addTagsToActivity(id, tags);
        return ResponseEntity.ok().build();
    }

    /** 依單一標籤查活動 */
    @GetMapping("/tags/{tag}")
    public List<Activity> byTag(@PathVariable String tag) {
        return activityService.getActivitiesByTag(tag);
    }

    // ===== 其他操作 =====

    /** 結束活動報名 */
    @PostMapping("/{id}/end-registration")
    public ResponseEntity<String> endRegistration(@PathVariable Integer id) {
        Activity existing = activityService.getActivityById(id);
        if (existing != null) {
            activityService.endRegistration(id);
            return ResponseEntity.ok("活動報名已結束");
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}