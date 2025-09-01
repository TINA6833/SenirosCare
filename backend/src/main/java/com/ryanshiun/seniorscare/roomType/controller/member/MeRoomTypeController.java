package com.ryanshiun.seniorscare.roomType.controller.member;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.ryanshiun.seniorscare.roomType.dto.RoomTypeForm;
import com.ryanshiun.seniorscare.roomType.model.RoomType;
import com.ryanshiun.seniorscare.roomType.service.RoomTypeService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.io.OutputStream;

@RestController
@RequestMapping("/api/member/room-types")
public class MeRoomTypeController {

    private final RoomTypeService roomTypeService;

    public MeRoomTypeController(RoomTypeService roomTypeService) {
        this.roomTypeService = roomTypeService;
    }
    
    

    // ===== Read =====
    @GetMapping //測試成功 後端邏輯
    public List<RoomType> getAllRoomTypes() {
        return roomTypeService.getAllRoomTypes();
    }

    @GetMapping("/{id}") //測試成功 //後台邏輯
    public RoomType getRoomTypeById(@PathVariable int id) {
        return roomTypeService.getRoomTypeById(id);
    }

    // ===== Create =====
    // 舊路徑保留：/RoomAdd（multipart）
    @PostMapping(value = "/RoomAdd", consumes = MediaType.MULTIPART_FORM_DATA_VALUE) //測試結束
    public Integer uploadRoom(@ModelAttribute RoomTypeForm form) {
        return roomTypeService.addRoomWithImage(form);
    }
    
    
    
    // ===== Update =====
    // 全量更新
    @PutMapping("/{id}") //測試成功 //後台邏輯
    public boolean updateRoomType(@PathVariable int id, @RequestBody RoomType room) {
        room.setId(id);
        return roomTypeService.updateRoomType(room);
    }

    // 部分更新（multipart：可含圖片）//測試成功
    @PatchMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> patchRoomWithImage(
            @PathVariable int id,
            @Valid @ModelAttribute RoomTypeForm form,
            BindingResult br
    ) {
        if (br.hasErrors()) {
            var errors = br.getFieldErrors().stream()
                .map(fe -> Map.of(
                    "field", fe.getField(),
                    "rejected", String.valueOf(fe.getRejectedValue()),
                    "message", fe.getDefaultMessage()))
                .toList();
            return ResponseEntity.badRequest().body(Map.of(
                "status", 400,
                "error", "Bad Request",
                "message", "Validation/Binding failed",
                "details", errors
            ));
        }

        boolean ok = roomTypeService.partialUpdate(id, form);
        return ResponseEntity.ok(Map.of("success", ok));
    }

    // 部分更新（JSON：只改欄位）//測試成功
    @PatchMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public boolean patchRoomJson(@PathVariable int id, @RequestBody Map<String, Object> updates) {
        return roomTypeService.partialUpdate(id, updates);
    }

    // ===== Delete =====
    @DeleteMapping("/{id}")//測試成功 //後台邏輯
    public ResponseEntity<?> deleteRoomType(@PathVariable int id) {
        try {
            boolean ok = roomTypeService.deleteRoomType(id);
            if (ok) {
                return ResponseEntity.noContent().build(); // 204
            } else {
                return ResponseEntity.notFound().build();  // 404
            }
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            // 例如被 roomtype_feature 參照，無法刪除
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "該資料使用中，無法刪除"));
        }
    }

    // ===== Queries =====
    @GetMapping("/price-range") //測試成功
    public List<RoomType> getByPriceRange(@RequestParam int min, @RequestParam int max) {
        return roomTypeService.getRoomTypesByPriceRange(min, max);
    }

 // 舊的：只用 keyword 模糊查
    @GetMapping("/search/keyword") //測試成功
    public List<RoomType> getByDescription(@RequestParam String keyword) {
        return roomTypeService.getRoomTypesByDescriptionKeyword(keyword);
    }

    @GetMapping("/capacity")// 測試成功
    public List<RoomType> getByCapacity(@RequestParam int capacity) {
        return roomTypeService.getRoomTypesByCapacity(capacity);
    }

    // ===== Pagination & Count =====
    @GetMapping("/page")// 測試成功
    public List<RoomType> page(@RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "10") int size) {
        return roomTypeService.getRoomTypesPaged(page, size);
    }

    @GetMapping("/count")// 測試成功
    public int countAll() {
        return roomTypeService.countAllRoomTypes();
    }

    // ===== CSV =====
    @GetMapping("/export")// 測試成功 //後台邏輯
    public void exportCSV(HttpServletResponse response) {
        try {
            String filename = URLEncoder.encode("room_types.csv", StandardCharsets.UTF_8);
            response.setContentType("text/csv; charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + filename);

            OutputStream out = response.getOutputStream();
            // UTF-8 BOM（Excel 防亂碼）
            out.write(new byte[]{(byte)0xEF, (byte)0xBB, (byte)0xBF});
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8), true);

            roomTypeService.exportToCSV(writer);
        } catch (IOException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/import")// 測試成功 //後台邏輯
    public ResponseEntity<String> importCSV(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("請選擇要上傳的 CSV 檔案");
        }
        try {
            roomTypeService.importFromCSV(file);
            return ResponseEntity.ok("匯入成功！");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("匯入失敗：" + e.getMessage());
        }
    }
    
    @GetMapping("/search") //測試成功 // 後台邏輯
    public Map<String, Object> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer min,
            @RequestParam(required = false) Integer max,
            @RequestParam(required = false) Integer capacity,
            @RequestParam(required = false) String featureIds,
            @RequestParam(defaultValue = "any") String match,         // any | all
            @RequestParam(defaultValue = "createdAt") String sort,    // price|capacity|name|createdAt
            @RequestParam(defaultValue = "desc") String order,        // asc|desc
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        List<Integer> feats = parseIds(featureIds);
        boolean matchAll = "all".equalsIgnoreCase(match);

        List<RoomType> list = roomTypeService.searchFiltered(
                keyword, min, max, capacity, feats, matchAll, sort, order, page, size);
        int total = roomTypeService.countFiltered(
                keyword, min, max, capacity, feats, matchAll);

        return Map.of(
                "data", list,
                "total", total,
                "page", page,
                "size", size
        );
    }

    private static List<Integer> parseIds(String csv) {
        if (csv == null || csv.isBlank()) return List.of();
        String[] tokens = csv.split(",");
        List<Integer> ids = new java.util.ArrayList<>();
        for (String t : tokens) {
            try {
                ids.add(Integer.parseInt(t.trim()));
            } catch (NumberFormatException ignore) {}
        }
        return ids;
    }
}