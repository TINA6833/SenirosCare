package com.ryanshiun.seniorscare.roomType.controller.employee;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ryanshiun.seniorscare.roomType.dto.FacilityForm;
import com.ryanshiun.seniorscare.roomType.dto.FacilityPublicView;
import com.ryanshiun.seniorscare.roomType.model.Facility;
import com.ryanshiun.seniorscare.roomType.service.FacilityService;

@RestController
@RequestMapping("/api/room-types/facilities") 
public class FacilityController {

    private final FacilityService service;

    public FacilityController(FacilityService service) {
        this.service = service;
    }

    // ===== Create =====
    // 建議用 multipart 表單來新增（支援同時上傳圖片或直接給 imagePath） //測試通過
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Integer> create(@ModelAttribute FacilityForm form) {
        Facility f = new Facility();
        f.setName(form.getName());
        f.setDescription(form.getDescription());
        f.setAvailable(Boolean.TRUE.equals(form.getIsAvailable()));
        // 先建立（不處理圖片）
        int newId = service.addFacility(f);

        // 有檔案就上傳，否則如果有路徑就直接寫入
        MultipartFile img = form.getImage();
        if (img != null && !img.isEmpty()) {
            service.updateImage(newId, img);
        } else if (form.getImagePath() != null && !form.getImagePath().isBlank()) {
            service.updateImagePath(newId, form.getImagePath().trim());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(newId);
    }

    // ===== Read =====
    @GetMapping // 測試成功
    public List<Facility> getAll() {
        return service.getAllFacilities();
    }

    @GetMapping("/{id}") //測試成功
    public ResponseEntity<Facility> getById(@PathVariable int id) {
        Facility f = service.getFacilityById(id);
        return f == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(f);
    }

    @GetMapping("/available") //測試成功
    public List<Facility> getAvailable() {
        return service.getAvailableFacilities();
    }

    @GetMapping("/search") //測試成功
    public List<Facility> searchByName(@RequestParam String keyword) {
        return service.searchFacilitiesByName(keyword);
    }

    @GetMapping("/by-availability") //測試成功
    public List<Facility> byAvailability(@RequestParam boolean isAvailable) {
        return service.getFacilitiesByAvailability(isAvailable);
    }

    // ===== Update =====
    // 全量更新（JSON）
    @PutMapping("/{id}") //可行，但只能傳json 不能上傳圖片
    public ResponseEntity<Boolean> update(@PathVariable int id, @RequestBody Facility facility) {
        facility.setId(id);
        return ResponseEntity.ok(service.updateFacility(facility));
    }

    // 部分更新（JSON）
    @PatchMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)//可用 但應該也沒必要部屬
    public ResponseEntity<Boolean> patchJson(@PathVariable int id, @RequestBody Map<String, Object> updates) {
        return ResponseEntity.ok(service.partialUpdate(id, updates));
    }

    // 部分更新（multipart，可同時改文字＋圖片）
    @PatchMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)//可以用，這個需要部屬
    public ResponseEntity<Boolean> patchForm(@PathVariable int id, @ModelAttribute FacilityForm form) {
        return ResponseEntity.ok(service.partialUpdate(id, form));
    }

    // 單獨更新圖片
    @PatchMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE) //可以用，這個不需部屬
    public ResponseEntity<Boolean> updateImage(@PathVariable int id, @RequestParam("image") MultipartFile image) {
        return ResponseEntity.ok(service.updateImage(id, image));
    }

    // ===== Delete =====
    @DeleteMapping("/{id}")//可以使用
    public ResponseEntity<Boolean> delete(@PathVariable int id) {
        return ResponseEntity.ok(service.deleteFacility(id));
    }
    
    @GetMapping("/public")//可以使用
    public List<FacilityPublicView> listPublic(
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String order
    ) {
        List<Facility> list = service.getAvailableFacilities();

        Comparator<Facility> comparator;
        if ("name".equalsIgnoreCase(sort)) {
            comparator = Comparator.comparing(f -> String.valueOf(f.getName()), String.CASE_INSENSITIVE_ORDER);
        } else {
            comparator = Comparator.comparing(Facility::getCreatedAt, Comparator.nullsLast(Date::compareTo));
        }
        if ("desc".equalsIgnoreCase(order)) {
            comparator = comparator.reversed();
        }
        list.sort(comparator);

        return list.stream()
                .map(f -> {
                    FacilityPublicView dto = new FacilityPublicView();
                    dto.setId(f.getId());
                    dto.setName(f.getName());
                    dto.setDescription(f.getDescription());
                    dto.setImagePath(f.getImagePath());
                    dto.setAvailable(f.isAvailable());
                    dto.setCreatedAt(f.getCreatedAt());
                    dto.setUpdatedAt(f.getUpdatedAt());
                    return dto;
                })
                .toList();
    }
}