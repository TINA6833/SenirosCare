package com.ryanshiun.seniorscare.roomType.controller.employee;

import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ryanshiun.seniorscare.roomType.dto.FavoriteStats;
import com.ryanshiun.seniorscare.roomType.service.FavoriteStatsService;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/room-types/favorites")
public class FavoriteStatsController {

    private final FavoriteStatsService service;

    public FavoriteStatsController(FavoriteStatsService service) {
        this.service = service;
    }

    // === TopN（固定依收藏數 DESC）===
    @GetMapping("/stats/top") // 已測試
    public List<FavoriteStats> top(@RequestParam(defaultValue = "10") int limit,
                                   @RequestParam(required = false) String keyword) {
        return service.findTopFavorited(limit, keyword);
    }

    // === 搜尋 + 排序 + 分頁 ===
    // order: asc | desc（依 favorites 排序）
    @GetMapping("/stats") // 已測試
    public ResponseEntity<Map<String, Object>> stats(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "desc") String order,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        // 防呆
        String ord = "asc".equalsIgnoreCase(order) ? "asc" : "desc";
        int pg   = Math.max(1, page);
        int lim  = Math.min(Math.max(1, size), 100); // 1~100
        int offset = (pg - 1) * lim;
        String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();

        try {
            int total = service.countStats(kw);
            var data  = service.findStats(kw, ord, offset, lim); // 注意：這裡傳 offset, limit

            return ResponseEntity.ok(Map.of(
                    "data",  data,
                    "total", total,
                    "page",  pg,
                    "size",  lim
            ));
        } catch (Exception e) {
            // 方便你看到實際錯誤（或改成 logger）
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "message", "查詢失敗",
                    "error",   e.getClass().getSimpleName(),
                    "detail",  e.getMessage()
            ));
        }
    }

    // === 匯出 CSV（依相同條件，輸出全部，不分頁）===
    @GetMapping("/export") //可以使用
    public void export(HttpServletResponse response,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(defaultValue = "desc") String order) {
        try {
            String filename = URLEncoder.encode("favorite_stats.csv", StandardCharsets.UTF_8);
            response.setContentType("text/csv; charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + filename);

            OutputStream out = response.getOutputStream();
            // UTF-8 BOM（Excel 防亂碼）
            out.write(new byte[]{(byte)0xEF, (byte)0xBB, (byte)0xBF});
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8), true);

            service.exportStatsCsv(writer, keyword, order);
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
