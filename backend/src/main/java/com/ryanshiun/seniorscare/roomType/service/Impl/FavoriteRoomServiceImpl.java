package com.ryanshiun.seniorscare.roomType.service.Impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ryanshiun.seniorscare.member.dao.member.MemberDao;
import com.ryanshiun.seniorscare.roomType.dao.FavoriteRoomDao;
import com.ryanshiun.seniorscare.roomType.dao.RoomTypeDao;
import com.ryanshiun.seniorscare.roomType.dto.FavoriteRoomView;
import com.ryanshiun.seniorscare.roomType.service.FavoriteRoomService;

@Service
public class FavoriteRoomServiceImpl implements FavoriteRoomService {

    private final FavoriteRoomDao dao;
    private final NamedParameterJdbcTemplate tpl; // 直接用 JDBC 檢查存在性

    public FavoriteRoomServiceImpl(FavoriteRoomDao dao, NamedParameterJdbcTemplate tpl) {
        this.dao = dao;
        this.tpl = tpl;
    }

    @Override
    @Transactional
    public int addFavorite(int memberId, int roomTypeId) {
        // 1) 會員存在性
        if (!existsMember(memberId)) return 0;
        // 2) 房型存在性
        if (!existsRoomType(roomTypeId)) return 0;
        // 3) 去重（你已經有 dao.isFavorited）
        if (dao.isFavorited(memberId, roomTypeId)) return 0;

        // 4) 插入
        return dao.addFavorite(memberId, roomTypeId);
    }

    @Override
    @Transactional
    public int[] batchAddFavorite(int memberId, List<Integer> roomTypeIds) {
        if (roomTypeIds == null || roomTypeIds.isEmpty()) return new int[0];
        if (!existsMember(memberId)) return new int[roomTypeIds.size()]; // 全部失敗

        // 去 null、去重，保留原順序
        List<Integer> distinctOrdered = roomTypeIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        // 過濾：房型存在 + 尚未收藏
        List<Integer> validTargets = new ArrayList<>();
        for (Integer rtId : distinctOrdered) {
            if (existsRoomType(rtId) && !dao.isFavorited(memberId, rtId)) {
                validTargets.add(rtId);
            }
        }

        int[] inserted = validTargets.isEmpty()
                ? new int[0]
                : dao.batchAddFavorite(memberId, validTargets);

        // 對齊原輸入長度：成功標 1，其餘 0
        Map<Integer, Integer> success = new HashMap<>();
        for (int i = 0; i < validTargets.size(); i++) {
            if (i < inserted.length && inserted[i] > 0) success.put(validTargets.get(i), 1);
        }
        int[] result = new int[roomTypeIds.size()];
        for (int i = 0; i < roomTypeIds.size(); i++) {
            Integer rtId = roomTypeIds.get(i);
            result[i] = (rtId != null && success.getOrDefault(rtId, 0) == 1) ? 1 : 0;
        }
        return result;
    }

    @Override
    @Transactional
    public boolean removeFavorite(int memberId, int roomTypeId) {
        if (!existsMember(memberId) || !existsRoomType(roomTypeId)) return false;
        return dao.removeFavorite(memberId, roomTypeId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFavorited(int memberId, int roomTypeId) {
        return dao.isFavorited(memberId, roomTypeId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Integer> findRoomTypeIdsByMember(int memberId) {
        return dao.findRoomTypeIdsByMember(memberId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByRoomType(int roomTypeId) {
        return dao.countByRoomType(roomTypeId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByMember(int memberId) {
        return dao.countByMember(memberId);
    }

    // ====== private helpers (直接用 SQL 檢查存在性) ======
 
    public boolean existsMember(int memberId) {
        String sql = "SELECT 1 FROM member WHERE member_id = :id";
        return !tpl.queryForList(sql, Map.of("id", memberId)).isEmpty();
    }


    public boolean existsRoomType(int roomTypeId) {
        String sql = "SELECT 1 FROM roomtype WHERE id = :id";
        return !tpl.queryForList(sql, Map.of("id", roomTypeId)).isEmpty();
    }
    
    @Transactional(readOnly = true)
    public List<FavoriteRoomView> findFavoritesByMember(int memberId) {
        return dao.findFavoritesByMember(memberId);
    }
}