package com.ryanshiun.seniorscare.roomType.service;

import java.util.List;

import com.ryanshiun.seniorscare.roomType.dto.FavoriteRoomView;

public interface FavoriteRoomService {

    // 新增收藏（已存在則不重複新增）
    int addFavorite(int memberId, int roomTypeId);

    // 批次新增收藏
    int[] batchAddFavorite(int memberId, List<Integer> roomTypeIds);

    // 取消收藏
    boolean removeFavorite(int memberId, int roomTypeId);

    // 是否已收藏
    boolean isFavorited(int memberId, int roomTypeId);

    // 取得會員收藏的房型 ID 清單
    List<Integer> findRoomTypeIdsByMember(int memberId);

    // 某房型被收藏的次數
    long countByRoomType(int roomTypeId);

    // 會員收藏的數量
    long countByMember(int memberId);
    
    boolean existsMember(int memberId);
    
    boolean existsRoomType(int roomTypeId);
    
    List<FavoriteRoomView> findFavoritesByMember(int memberId);
    
}