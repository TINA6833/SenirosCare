package com.ryanshiun.seniorscare.roomType.controller.employee;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ryanshiun.seniorscare.roomType.dto.FavoriteRoomView;
import com.ryanshiun.seniorscare.roomType.service.FavoriteRoomService;

@RestController
@RequestMapping("/api/room-types/favorites")
public class FavoriteRoomController {

    private final FavoriteRoomService service;

    public FavoriteRoomController(FavoriteRoomService service) {
        this.service = service;
    }

    // === 新增收藏 ===
    // 用 querystring 比較直覺：/api/favorites?memberId=1&roomTypeId=9
    @PostMapping  //測試成功
    public ResponseEntity<?> add(
            @RequestParam int memberId,
            @RequestParam int roomTypeId
    ) {
        // 檢查房間是否存在
        if (!service.existsRoomType(roomTypeId)) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("房間不存在");
        }

        // 檢查會員是否存在
        if (!service.existsMember(memberId)) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("會員不存在");
        }

        // 檢查是否已收藏
        if (service.isFavorited(memberId, roomTypeId)) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("已經收藏過這個房間");
        }

        // 嘗試新增收藏
        int result = service.addFavorite(memberId, roomTypeId);
        if (result == 1) {
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body("收藏成功");
        } else {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("收藏失敗，請稍後再試");
        }
    }

    // === 批次新增收藏 ===
    public static record BatchFavoriteRequest(int memberId, List<Integer> roomTypeIds) {}
    public static record BatchFavoriteResult(int roomTypeId, boolean success, String message) {}

    @PostMapping("/batch") //已測試
    public ResponseEntity<List<BatchFavoriteResult>> batchAdd(@RequestBody BatchFavoriteRequest body) {
        int[] results = service.batchAddFavorite(body.memberId(), body.roomTypeIds());

        List<BatchFavoriteResult> out = new java.util.ArrayList<>();
        boolean anySuccess = false;

        for (int i = 0; i < body.roomTypeIds().size(); i++) {
            int roomTypeId = body.roomTypeIds().get(i);
            boolean ok = (i < results.length) && results[i] > 0;
            if (ok) anySuccess = true;
            out.add(new BatchFavoriteResult(roomTypeId, ok, ok ? "收藏成功" : "收藏已收藏"));
        }

        return ResponseEntity.status(anySuccess ? HttpStatus.CREATED : HttpStatus.OK).body(out);
    }

    // === 取消收藏 ===
    // /api/favorites?memberId=1&roomTypeId=9
    public static record RemoveFavoriteResult(
            int memberId,
            int roomTypeId,
            boolean success,
            String message
    ) {}

    @DeleteMapping //已測試
    public ResponseEntity<RemoveFavoriteResult> remove(@RequestParam int memberId,
                                                       @RequestParam int roomTypeId) {
        boolean removed = service.removeFavorite(memberId, roomTypeId);

        String msg = removed ? "取消收藏成功" : "取消收藏失敗（可能不存在該收藏紀錄）";

        return ResponseEntity.ok(
            new RemoveFavoriteResult(memberId, roomTypeId, removed, msg)
        );
    }
    // === 某會員收藏清單 ===
    @GetMapping("/member/{memberId}") //已測試
    public ResponseEntity<List<FavoriteRoomView>> getFavoritesByMember(@PathVariable int memberId) {
        List<FavoriteRoomView> favorites = service.findFavoritesByMember(memberId);
        if (favorites.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(favorites);
    }
    // === 是否已收藏 ===
    // /api/favorites/exists?memberId=1&roomTypeId=9
    @GetMapping("/exists") //已測試
    public ResponseEntity<Boolean> exists(@RequestParam int memberId,
                                          @RequestParam int roomTypeId) {
        return ResponseEntity.ok(service.isFavorited(memberId, roomTypeId));
    }

    // === 收藏數（按房型）===
    @GetMapping("/count/roomtype/{roomTypeId}") // 已測試
    public ResponseEntity<Long> countByRoomType(@PathVariable int roomTypeId) {
        return ResponseEntity.ok(service.countByRoomType(roomTypeId));
    }

    // === 收藏數（按會員）===
    @GetMapping("/count/member/{memberId}") //已測試
    public ResponseEntity<Long> countByMember(@PathVariable int memberId) {
        return ResponseEntity.ok(service.countByMember(memberId));
    }
}
