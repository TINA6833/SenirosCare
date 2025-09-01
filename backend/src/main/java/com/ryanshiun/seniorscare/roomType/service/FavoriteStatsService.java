package com.ryanshiun.seniorscare.roomType.service;

import java.io.PrintWriter;
import java.util.List;

import com.ryanshiun.seniorscare.roomType.dto.FavoriteStats;

public interface FavoriteStatsService {

    // 查詢 + 分頁
    List<FavoriteStats> findStats(String keyword, String order, int page, int size);
    int countStats(String keyword);

    // TopN
    List<FavoriteStats> findTopFavorited(int limit, String keyword);

    // 匯出 CSV（依同條件）
    void exportStatsCsv(PrintWriter writer, String keyword, String order);
}