package com.ryanshiun.seniorscare.roomType.controller.member;

import java.util.List;
import java.util.Map;
import java.util.Objects;

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

import com.ryanshiun.seniorscare.roomType.service.RoomTypeFeatureService;

@RestController
@RequestMapping("/api/member/room-type-features") 
public class MeRoomTypeFeatureController {

    private final RoomTypeFeatureService service;

    public MeRoomTypeFeatureController(RoomTypeFeatureService service) {
        this.service = service;
    }

    // ===== 建立關聯 =====

    // 新增單筆關聯：/api/room-type-features?roomTypeId=1&featureId=3
    @PostMapping // 測試成功
    public ResponseEntity<Integer> add(@RequestParam int roomTypeId,
                                       @RequestParam int featureId) {
        int res = service.add(roomTypeId, featureId);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

   

    // ===== 刪除關聯 =====

    // 刪除某房型的所有特徵
    @DeleteMapping("/{roomTypeId}")//可以使用
    public ResponseEntity<?> deleteByRoomType(@PathVariable int roomTypeId) {
        try {
            int affected = service.deleteByRoomType(roomTypeId);
            if (affected == 0) {
                // 沒有任何關聯可刪（可能本來就沒有或 roomTypeId 不存在）
                return ResponseEntity.noContent().build(); // 204
            }
            return ResponseEntity.ok(affected); // 200 + 被刪筆數
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // 若子表還被別的 FK 用到（罕見，但保險起見）
            return ResponseEntity.status(409).body(Map.of("message", "該資料使用中，無法刪除"));
        }
    }
    // 刪除單一房型-特徵關聯
    @DeleteMapping("/{roomTypeId}/{featureId}")// 測試成功
    public ResponseEntity<?> deleteOne(@PathVariable int roomTypeId,
                                       @PathVariable int featureId) {
        try {
            int affected = service.deleteByRoomTypeAndFeature(roomTypeId, featureId);
            if (affected == 0) {
                return ResponseEntity.noContent().build(); // 204 沒有找到要刪的關聯
            }
            return ResponseEntity.ok(affected); // 200 + 刪除筆數
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            return ResponseEntity.status(409).body(
                Map.of("message", "該資料使用中，無法刪除")
            );
        }
    }

    // ===== 查詢 =====

    // 查某房型擁有的特徵 IDs
    @GetMapping("/{roomTypeId}/features")//可以使用
    public List<Integer> findFeatureIdsByRoomType(@PathVariable int roomTypeId) {
        return service.findFeatureIdsByRoomType(roomTypeId);
    }

    // 查擁有某特徵的房型 IDs
    @GetMapping("/feature/{featureId}/room-types")//可以使用
    public List<Integer> findRoomTypesByFeature(@PathVariable int featureId) {
        return service.findRoomTypeIdsByFeature(featureId);
    }

    // ===== 計數 =====

    @GetMapping("/{roomTypeId}/count")//測試成功
    public ResponseEntity<Long> countFeaturesByRoomType(@PathVariable int roomTypeId) {
        return ResponseEntity.ok(service.countFeaturesByRoomType(roomTypeId));
    }

    @GetMapping("/feature/{featureId}/count")//測試成功
    public ResponseEntity<Long> countRoomTypesByFeature(@PathVariable int featureId) {
        return ResponseEntity.ok(service.countRoomTypesByFeature(featureId));
    }

    // ===== 覆蓋整包特徵（常用）=====
    // PUT /api/room-type-features/{roomTypeId}
    // body: [1,2,3] -> 會先清空再批量新增
    @PutMapping("/{roomTypeId}")//測試成功
    public ResponseEntity<?> replaceAll(@PathVariable int roomTypeId,
                                        @RequestBody List<Integer> featureIds) {
        if (featureIds == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "請傳入 JSON 陣列，例如：[1,2,3]"));
        }
        try {
            boolean ok = service.replaceAllFeaturesForRoomType(roomTypeId, sanitizeIds(featureIds));
            return ResponseEntity.ok(Map.of("success", ok));
        } catch (IllegalArgumentException e) {
            // 自行檢查不存在的 roomType 或 feature，主動丟這個例外
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "建立關聯失敗：可能是重複或參照資料不存在"));
        }
    }

    @PostMapping("/{roomTypeId}/batch")//測試成功
    public ResponseEntity<?> batchAdd(@PathVariable int roomTypeId,
                                      @RequestBody List<Integer> featureIds) {
        if (featureIds == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "請傳入 JSON 陣列，例如：[1,2,3]"));
        }
        try {
            int[] results = service.batchAdd(roomTypeId, sanitizeIds(featureIds));
            int inserted = 0; for (int r : results) if (r > 0) inserted++;
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "requested", featureIds.size(),
                    "inserted", inserted
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "建立關聯失敗：可能是重複或參照資料不存在"));
        }
    }

    private static List<Integer> sanitizeIds(List<Integer> ids) {
        return ids.stream()
                .filter(Objects::nonNull)
                .map(Integer::intValue)
                .filter(i -> i > 0)
                .distinct()
                .toList();
    }
}