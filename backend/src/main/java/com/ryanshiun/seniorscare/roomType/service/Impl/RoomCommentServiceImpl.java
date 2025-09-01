package com.ryanshiun.seniorscare.roomType.service.Impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ryanshiun.seniorscare.roomType.dao.RoomCommentDao;
import com.ryanshiun.seniorscare.roomType.model.RoomComment;
import com.ryanshiun.seniorscare.roomType.service.RoomCommentService;

@Service
@Transactional
public class RoomCommentServiceImpl implements RoomCommentService {

    private final RoomCommentDao dao;

    public RoomCommentServiceImpl(RoomCommentDao dao) {
        this.dao = dao;
    }

    // ===== C =====
    @Override
    public int addComment(RoomComment c) {
        return dao.addComment(c);
    }

    // ===== U =====
    @Override
    public boolean approve(int id) {
        return dao.approve(id);
    }

    @Override
    public boolean unapprove(int id) {
        return dao.unapprove(id);
    }

    @Override
    public boolean updateContent(int id, String content) {
        return dao.updateContent(id, content);
    }

    @Override
    public boolean setAdminReply(int id, String reply) {
        return dao.setAdminReply(id, reply);
    }

    // ===== D =====
    @Override
    public boolean deleteById(int id) {
        return dao.deleteById(id);
    }

    // ===== R =====
    
    @Override
    @Transactional(readOnly = true)
    public List<RoomComment> findAll() {
        return dao.findAll();
    }
    
    @Override
    @Transactional(readOnly = true)
    public RoomComment findById(int id) {
        return dao.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomComment> findByRoomType(int roomTypeId, Boolean approved) {
        return dao.findByRoomType(roomTypeId, approved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomComment> findByMember(int memberId, Boolean approved) {
        return dao.findByMember(memberId, approved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomComment> getLatestApprovedByRoomType(int roomTypeId, int limit) {
        return dao.getLatestApprovedByRoomType(roomTypeId, limit);
    }

    // ===== 分頁 + 計數 =====
    @Override
    @Transactional(readOnly = true)
    public List<RoomComment> pageByRoomType(int roomTypeId, Boolean approved, int offset, int pageSize) {
        return dao.pageByRoomType(roomTypeId, approved, offset, pageSize);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByRoomType(int roomTypeId, Boolean approved) {
        return dao.countByRoomType(roomTypeId, approved);
    }
}
