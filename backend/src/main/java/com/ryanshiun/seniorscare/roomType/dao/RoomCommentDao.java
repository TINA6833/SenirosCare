package com.ryanshiun.seniorscare.roomType.dao;

import java.util.List;

import com.ryanshiun.seniorscare.roomType.model.RoomComment;

public interface RoomCommentDao {

    // C
    int addComment(RoomComment c); // 回傳新 id

    // U
    boolean approve(int id);                 // is_approved = 1
    boolean unapprove(int id);               // is_approved = 0
    boolean updateContent(int id, String content);
    boolean setAdminReply(int id, String reply);

    // D
    boolean deleteById(int id);

    // R
    List<RoomComment> findAll();
    RoomComment findById(int id);
    List<RoomComment> findByRoomType(int roomTypeId, Boolean approved); // approved=null 代表全部
    List<RoomComment> findByMember(int memberId, Boolean approved);
    List<RoomComment> getLatestApprovedByRoomType(int roomTypeId, int limit);

    // 分頁 + 計數（後台）
    List<RoomComment> pageByRoomType(int roomTypeId, Boolean approved, int offset, int pageSize);
    long countByRoomType(int roomTypeId, Boolean approved);
}