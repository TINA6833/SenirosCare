package com.ryanshiun.seniorscare.device.controller;

import com.ryanshiun.seniorscare.device.dto.DeviceCategoryRequest;
import com.ryanshiun.seniorscare.device.dto.DeviceCategoryResponse;
import com.ryanshiun.seniorscare.device.model.DeviceCategory;
import com.ryanshiun.seniorscare.device.service.DeviceCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * DeviceCategoryController
 * 提供後台 DeviceCategory 管理的 REST API
 */
@RestController // 允許所有前端網域呼叫此 Controller 的 API 可以改成 @CrossOrigin(origins = "https://your-frontend.com")  // 更安全
@RequestMapping("/api/device-categories")
@CrossOrigin // 開啟跨來源請求（前端可透過 JavaScript 呼叫）
public class DeviceCategoryController {

    @Autowired
    private DeviceCategoryService deviceCategoryService;

    /**
     * 查詢所有分類，回傳 Response DTO 清單
     */
    @GetMapping
    public List<DeviceCategoryResponse> getAllCategories() {
        return deviceCategoryService.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 根據 ID 查詢單筆分類，找不到則回傳 null
     */
    @GetMapping("/{id}")
    public DeviceCategoryResponse getCategoryById(@PathVariable Integer id) {
        DeviceCategory category = deviceCategoryService.findById(id);
        return (category != null) ? toResponse(category) : null;
    }

    /**
     * 新增分類，回傳新增後的 ID
     */
    @PostMapping
    public Integer createCategory(@RequestBody DeviceCategoryRequest request) {
        DeviceCategory category = new DeviceCategory();
        category.setName(request.getName());
        category.setCategoryId(request.getCategoryId());

        return deviceCategoryService.insert(category);
    }

    /**
     * 修改分類資料（根據路徑參數的 id 修改 name / category_id）
     */
    @PutMapping("/{id}")
    public boolean updateCategory(@PathVariable Integer id,
                                  @RequestBody DeviceCategoryRequest request) {
        DeviceCategory category = new DeviceCategory();
        category.setId(id);
        category.setName(request.getName());
        category.setCategoryId(request.getCategoryId());

        return deviceCategoryService.update(category);
    }

    /**
     * 刪除分類資料（僅當無商品使用此分類時允許刪除）
     */
    @DeleteMapping("/{id}")
    public boolean deleteCategory(@PathVariable Integer id) {
        return deviceCategoryService.deleteById(id);
    }

    /**
     * 檢查某個分類 ID 是否存在
     */
    @GetMapping("/exists/{id}")
    public boolean existsById(@PathVariable Integer id) {
        return deviceCategoryService.existsById(id);
    }

    /**
     * 模糊搜尋名稱包含關鍵字的分類清單
     */
    @GetMapping("/search")
    public List<DeviceCategoryResponse> searchByName(@RequestParam String keyword) {
        return deviceCategoryService.searchByName(keyword).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 查詢分類資料的總筆數
     */
    @GetMapping("/count")
    public int countCategories() {
        return deviceCategoryService.count();
    }

    // ========== Private method：Entity 轉換為 Response DTO ==========

    private DeviceCategoryResponse toResponse(DeviceCategory category) {
        DeviceCategoryResponse response = new DeviceCategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setCategoryId(category.getCategoryId());
        return response;
    }
}
