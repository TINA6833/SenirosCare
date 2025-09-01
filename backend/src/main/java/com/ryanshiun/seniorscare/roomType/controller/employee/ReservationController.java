package com.ryanshiun.seniorscare.roomType.controller.employee;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ryanshiun.seniorscare.roomType.dto.ReservationDetail;
import com.ryanshiun.seniorscare.roomType.dto.ReservationStats;
import com.ryanshiun.seniorscare.roomType.dto.RoomTypeRanking;
import com.ryanshiun.seniorscare.roomType.model.Reservation;
import com.ryanshiun.seniorscare.roomType.service.ReservationService;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService service;

    public ReservationController(ReservationService service) {
        this.service = service;
    }

    // ===== CRUD =====

    @PostMapping//測試成功
    public ResponseEntity<Integer> create(@RequestBody Reservation r) {
        int id = service.addReservation(r);
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }

    @GetMapping("/{id}")//測試成功
    public ResponseEntity<Reservation> getById(@PathVariable int id) {
        Reservation r = service.getReservationById(id);
        return r == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(r);
    }

    @GetMapping//測試成功
    public List<Reservation> getAll() {
        return service.getAllReservations();
    }

    @PutMapping("/{id}")//測試成功
    public ResponseEntity<Boolean> update(@PathVariable int id, @RequestBody Reservation r) {
        r.setReservationId(id);
        return ResponseEntity.ok(service.updateReservation(r));
    }

    @DeleteMapping("/{id}")//測試成功
    public ResponseEntity<Boolean> delete(@PathVariable int id) {
        return ResponseEntity.ok(service.deleteReservation(id));
    }

    // ===== 查詢 =====

    // 會員的預約（memberId 可為 null → 代表非會員預約）
    @GetMapping("/by-member")//測試成功
    public List<Reservation> byMember(@RequestParam(required = false) Integer memberId) {
        return service.getByMember(memberId);
    }

    // 房型（roomTypeId 可為 null → 未指定房型）
    @GetMapping("/by-roomtype")//測試成功
    public List<Reservation> byRoomType(@RequestParam(required = false, name = "roomTypeId") Integer roomTypeId) {
        return service.getByRoomType(roomTypeId);
    }

    // 指定日期
    @GetMapping("/by-date")//測試成功
    public ResponseEntity<?> getByDate(@RequestParam("date") String dateStr) {
        String s = dateStr == null ? "" : dateStr.trim(); // 去掉前後空白/換行
        try {
            var date = java.time.LocalDate.parse(s, java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);
            return ResponseEntity.ok(service.getByPreferredDate(date));
        } catch (java.time.format.DateTimeParseException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of(
                "message", "date 參數格式錯誤，請用 yyyy-MM-dd，例如 2025-07-15",
                "given", s
            ));
        }
    }
    // 日期區間
    @GetMapping("/by-range")//測試成功
    public ResponseEntity<?> byRange(
            @RequestParam("from") String fromStr,
            @RequestParam("to") String toStr) {

        try {
            // 支援 yyyy-MM-dd 或 yyyy/MM/dd
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("[yyyy-MM-dd][yyyy/MM/dd]");
            LocalDate from = LocalDate.parse(fromStr.trim(), formatter);
            LocalDate to   = LocalDate.parse(toStr.trim(), formatter);

            // 檢查 from <= to
            if (from.isAfter(to)) {
                return ResponseEntity.badRequest().body(Map.of(
                        "message", "起始日期不可晚於結束日期",
                        "from", fromStr,
                        "to", toStr
                ));
            }

            return ResponseEntity.ok(service.getByDateRange(from, to));

        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "日期格式錯誤，請用 yyyy-MM-dd 或 yyyy/MM/dd，例如 2025-07-15",
                    "from", fromStr,
                    "to", toStr
            ));
        }
    }

    // 狀態：0待審,1已確認,2取消,3已完成,4未到
    @GetMapping("/by-status")//已測試
    public List<Reservation> byStatus(@RequestParam int status) {
        return service.getByStatus(status);
    }

    // ===== 統計 =====

    @GetMapping("/count/date")//已測試
    public ResponseEntity<Long> countOnDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(service.countOnDate(date));
    }

    @GetMapping("/count/roomtype")//已測試
    public ResponseEntity<Long> countByRoomType(
            @RequestParam(required = false, name = "roomTypeId") Integer roomTypeId) {
        return ResponseEntity.ok(service.countByRoomType(roomTypeId));
    }

    // ===== 趨勢與排行（給 Chart.js 很好用）=====

    @GetMapping("/analytics/daily")//已測試
    public List<ReservationStats> daily(@RequestParam(defaultValue = "30") int days) {
        return service.getDailyTrend(days);
    }

    @GetMapping("/analytics/monthly")//已測試
    public List<ReservationStats> monthly(@RequestParam(defaultValue = "12") int months) {
        return service.getMonthlyTrend(months);
    }

    @GetMapping("/analytics/ranking")//已測試
    public List<RoomTypeRanking> ranking(@RequestParam(defaultValue = "10") int topN) {
        return service.getRoomTypeRanking(topN);
    }
    
 // ReservationController.java
    @GetMapping("/{id}/detail")//已測試
    public ResponseEntity<ReservationDetail> detail(@PathVariable int id) {
        var d = service.getDetailById(id);
        return d == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(d);
    }
    
    // PATCH /api/reservations/{id}/status
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> patchReservationStatus(
            @PathVariable int id,
            @RequestBody Map<String, Object> payload
    ) {
        // 取得新狀態
        Object statusObj = payload.get("status");
        if (statusObj == null) {
            return ResponseEntity.badRequest().body(Map.of(
                "message", "缺少 status 欄位"
            ));
        }
        int status;
        try {
            status = Integer.parseInt(statusObj.toString());
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "message", "status 欄位格式錯誤"
            ));
        }

        // 可選：取得 note 欄位
        String note = payload.get("note") != null ? payload.get("note").toString() : null;

        // 呼叫 service 更新
        boolean ok = service.updateStatus(id, status, note);
        if (ok) {
            return ResponseEntity.ok(Map.of("success", true));
        } else {
            return ResponseEntity.status(404).body(Map.of(
                "message", "找不到預約資料"
            ));
        }
    }
}