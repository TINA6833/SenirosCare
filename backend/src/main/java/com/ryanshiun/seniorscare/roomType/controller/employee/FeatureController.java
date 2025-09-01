package com.ryanshiun.seniorscare.roomType.controller.employee;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ryanshiun.seniorscare.roomType.model.Feature;
import com.ryanshiun.seniorscare.roomType.service.FeatureService;

@RestController
@RequestMapping("/api/room-types/features")
public class FeatureController {

    private final FeatureService service;

    public FeatureController(FeatureService service) {
        this.service = service;
    }

    // ===== Create =====
    @PostMapping //可以使用
    public ResponseEntity<?> create(@RequestBody Feature feature) {
        try {
            Integer id = service.add(feature);
            return ResponseEntity.created(URI.create("/api/room-types/features/" + id)).body(id);
        } catch (DataIntegrityViolationException e) {
            // 可能是 UNIQUE name 衝突
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "message", "名稱已存在，無法重複新增",
                            "name", feature.getName()
                    ));
        }
    }

    public static class BatchCreateResult {
        public String name;
        public Integer id;        // 成功才有
        public boolean success;
        public String message;
        BatchCreateResult(String name, Integer id, boolean success, String message) {
            this.name = name; this.id = id; this.success = success; this.message = message;
        }
    }

    @PostMapping("/batch")//可以使用
    public ResponseEntity<List<BatchCreateResult>> batchCreate(@RequestBody List<Feature> features) {
        List<BatchCreateResult> results = new ArrayList<>();
        boolean anySuccess = false;

        for (Feature f : features) {
            try {
                Integer id = service.add(f);
                anySuccess = true;
                results.add(new BatchCreateResult(f.getName(), id, true, "新增成功"));
            } catch (DataIntegrityViolationException e) {
                results.add(new BatchCreateResult(f.getName(), null, false, "名稱已存在"));
            } catch (Exception e) {
                results.add(new BatchCreateResult(f.getName(), null, false, "新增失敗"));
            }
        }

        // 有成功 → 201；全失敗 → 200（避免誤導）
        return ResponseEntity.status(anySuccess ? HttpStatus.CREATED : HttpStatus.OK).body(results);
    }

    // ===== Read =====
    @GetMapping("/{id}") //已測試
    public ResponseEntity<Feature> getById(@PathVariable int id) {
        Optional<Feature> opt = service.findById(id);
        return opt.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping //已測試
    public List<Feature> getAll() {
        return service.findAll();
    }

    @GetMapping("/search") //已測試
    public List<Feature> search(@RequestParam String keyword) {
        return service.findByNameLike(keyword);
    }

    @GetMapping("/exists") //已測試
    public ResponseEntity<Boolean> exists(@RequestParam String name) {
        return ResponseEntity.ok(service.existsByName(name));
    }

    @GetMapping("/count") //已測試
    public ResponseEntity<Long> count() {
        return ResponseEntity.ok(service.count());
    }

    // ===== Update =====
    @PutMapping("/{id}") //已測試
    public ResponseEntity<Boolean> update(@PathVariable int id, @RequestBody Feature feature) {
        feature.setId(id);
        boolean ok = service.update(feature);
        // 若想區分不存在，可先 findById(id) 檢查再決定回 404 或 200
        return ResponseEntity.ok(ok);
    }

    // ===== Delete =====
    @DeleteMapping("/{id}")//已測試
    public ResponseEntity<?> delete(@PathVariable int id) {
        // 先檢查是否被使用
        if (service.isFeatureUsed(id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("success", false, "message", "該特徵已被房型使用，無法刪除"));
        }
        boolean ok = service.deleteById(id);
        return ResponseEntity.ok(Map.of("success", ok));
    }
}