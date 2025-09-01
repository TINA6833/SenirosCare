package com.ryanshiun.seniorscare.roomType.service.Impl;

import java.io.PrintWriter;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ryanshiun.seniorscare.roomType.dao.FavoriteRoomDao;
import com.ryanshiun.seniorscare.roomType.dto.FavoriteStats;
import com.ryanshiun.seniorscare.roomType.service.FavoriteStatsService;

@Service
@Transactional
public class FavoriteStatsServiceImpl implements FavoriteStatsService {

    private final FavoriteRoomDao dao;

    public FavoriteStatsServiceImpl(FavoriteRoomDao dao) {
        this.dao = dao;
    }

    @Override @Transactional(readOnly = true)
    public List<FavoriteStats> findStats(String keyword, String order, int page, int size) {
        int offset = Math.max(page - 1, 0) * Math.max(size, 1);
        return dao.findStats(keyword, order, offset, size);
    }

    @Override @Transactional(readOnly = true)
    public int countStats(String keyword) {
        return dao.countStats(keyword);
    }

    @Override @Transactional(readOnly = true)
    public List<FavoriteStats> findTopFavorited(int limit, String keyword) {
        return dao.findTopFavorited(limit, keyword);
    }

    @Override
    @Transactional(readOnly = true)
    public void exportStatsCsv(PrintWriter writer, String keyword, String order) {
        // 匯出全部（不分頁）：取 total 再一次性查
        int total = countStats(keyword);
        List<FavoriteStats> all = dao.findStats(keyword, order, 0, Math.max(total, 1));

        writer.println("roomTypeId,roomTypeName,favorites");
        for (FavoriteStats s : all) {
            writer.println(String.join(",",
                    csv(s.getRoomTypeId()),
                    csv(s.getRoomTypeName()),
                    csv(s.getFavorites())
            ));
        }
        writer.flush();
    }

    private static String csv(Object v) {
        String s = v == null ? "" : String.valueOf(v);
        if (s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r")) {
            s = s.replace("\"", "\"\"");
            return "\"" + s + "\"";
        }
        return s;
    }
}
