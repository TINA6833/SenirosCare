package com.ryanshiun.seniorscare.roomType.dao;

import java.util.List;

import com.ryanshiun.seniorscare.roomType.dto.FavoriteRoomView;
import com.ryanshiun.seniorscare.roomType.dto.FavoriteStats;

public interface FavoriteRoomDao {

    // 新增收藏（已存在則不重複新增，回傳 1 表示新增成功，0 表示已存在或失敗）
    int addFavorite(int memberId, int roomTypeId);

    // 批次新增收藏（忽略重複，回傳每筆是否成功 1/0）
    int[] batchAddFavorite(int memberId, List<Integer> roomTypeIds);

    // 取消收藏
    boolean removeFavorite(int memberId, int roomTypeId);

    // 是否已收藏
    boolean isFavorited(int memberId, int roomTypeId);

    // 某會員收藏了哪些房型（回傳 roomtype_id 清單，預設依 created_at DESC）
    List<Integer> findRoomTypeIdsByMember(int memberId);

    // 計算某房型被收藏的次數
    long countByRoomType(int roomTypeId);

    // 列出某會員收藏數量
    long countByMember(int memberId);
    
    /** 依房型彙總收藏數（可關鍵字過濾房型名），並排序（ASC/DESC），分頁 */
    List<FavoriteStats> findStats(String keyword, String order, int offset, int limit);

    /** 依同條件計總筆數（用於分頁） */
    int countStats(String keyword);

    /** 取 TopN 收藏數（可關鍵字過濾房型名），固定依收藏數 DESC */
    List<FavoriteStats> findTopFavorited(int limit, String keyword);
    
    List<FavoriteRoomView> findFavoritesByMember(int memberId);
    
}