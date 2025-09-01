package com.ryanshiun.seniorscare.roomType.controller.member;


import com.ryanshiun.seniorscare.roomType.model.RoomComment;
import com.ryanshiun.seniorscare.roomType.service.RoomCommentService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/member/room-comments")
public class MeRoomCommentController {

    private final RoomCommentService service;

    public MeRoomCommentController(RoomCommentService service) {
        this.service = service;
    }

    // ===== C =====
    @PostMapping //測試成功
    public ResponseEntity<Integer> create(@RequestBody RoomComment c) {
        int id = service.addComment(c);
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }

    // ===== R =====
 // RoomCommentController.java
    @GetMapping // /api/room-comments
    public List<RoomComment> getAll() {
        return service.findAll();
    }
    @GetMapping("/{id}")//測試成功
    public ResponseEntity<RoomComment> getById(@PathVariable int id) {
        RoomComment c = service.findById(id);
        return c == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(c);
    }

    // 依房型查（approved 可為 true/false，不給則查全部）
    @GetMapping("/by-roomtype/{roomTypeId}") //測試成功
    public List<RoomComment> byRoomType(@PathVariable int roomTypeId,
                                        @RequestParam(required = false) Boolean approved) {
        return service.findByRoomType(roomTypeId, approved);
    }

    // 依會員查（approved 可為 true/false，不給則查全部）
    @GetMapping("/by-member/{memberId}") //測試成功
    public List<RoomComment> byMember(@PathVariable int memberId,
                                      @RequestParam(required = false) Boolean approved) {
        return service.findByMember(memberId, approved);
    }

    // 取某房型最新通過留言
    @GetMapping("/latest") //測試成功
    public List<RoomComment> latestApproved(@RequestParam int roomTypeId,
                                            @RequestParam(defaultValue = "5") int limit) {
        return service.getLatestApprovedByRoomType(roomTypeId, limit);
    }

    // 分頁（後台）
    @GetMapping("/page") //測試成功
    public List<RoomComment> page(@RequestParam int roomTypeId,
                                  @RequestParam(required = false) Boolean approved,
                                  @RequestParam(defaultValue = "1") int page,
                                  @RequestParam(defaultValue = "10") int size) {
        int offset = Math.max(page - 1, 0) * Math.max(size, 1);
        return service.pageByRoomType(roomTypeId, approved, offset, size);
    }

    // 計數（後台）
    @GetMapping("/count") //測試成功
    public ResponseEntity<Long> count(@RequestParam int roomTypeId,
                                      @RequestParam(required = false) Boolean approved) {
        return ResponseEntity.ok(service.countByRoomType(roomTypeId, approved));
    }

    // ===== U =====

    // 審核通過
    @PatchMapping("/{id}/approve") //測試成功
    public ResponseEntity<Boolean> approve(@PathVariable int id) {
        return ResponseEntity.ok(service.approve(id));
    }

    // 退回/不通過
    @PatchMapping("/{id}/unapprove") //測試成功
    public ResponseEntity<Boolean> unapprove(@PathVariable int id) {
        return ResponseEntity.ok(service.unapprove(id));
    }

    // 修改內容
    public static record UpdateContentRequest(String content) {}
    @PatchMapping("/{id}/content") //測試成功
    public ResponseEntity<Boolean> updateContent(@PathVariable int id,
                                                 @RequestBody UpdateContentRequest body) {
        return ResponseEntity.ok(service.updateContent(id, body.content()));
    }

    // 設定管理員回覆
    public static record AdminReplyRequest(String reply) {}
    @PatchMapping("/{id}/reply") // 測試成功
    public ResponseEntity<Boolean> setReply(
            @PathVariable int id,
            @RequestBody Map<String, String> body) {

        String reply = body.get("adminReply");   // 可能不存在或為 null
        if (reply != null) reply = reply.trim();
        if (reply != null && reply.isEmpty()) reply = null; // 空字串視為清空

        boolean ok = service.setAdminReply(id, reply);
        return ok ? ResponseEntity.ok(true) : ResponseEntity.notFound().build();
    }
    // ===== D =====
    @DeleteMapping("/{id}") //測試成功
    public ResponseEntity<Boolean> delete(@PathVariable int id) {
        return ResponseEntity.ok(service.deleteById(id));
    }
}