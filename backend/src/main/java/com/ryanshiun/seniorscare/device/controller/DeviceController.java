package com.ryanshiun.seniorscare.device.controller;

import com.ryanshiun.seniorscare.device.dto.DeviceRequest;
import com.ryanshiun.seniorscare.device.dto.DeviceResponse;
import com.ryanshiun.seniorscare.device.model.Device;
import com.ryanshiun.seniorscare.device.service.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.util.StringUtils;
import java.nio.file.Path;
import java.nio.file.Files;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;


@CrossOrigin
@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    @Autowired
    private DeviceService deviceService;

    @Value("${file.upload-dir}")
    private String uploadDir;


    //取得全部商品，或依 categoryId 查詢
    @GetMapping
    public List<DeviceResponse> getDevices(@RequestParam(required = false) Integer categoryId) {
        if (categoryId != null) {
            // Service 已直接回傳 DTO
            return deviceService.getDevicesByCategoryId(categoryId);
        }
        return deviceService.getAllDevices();
    }

    // 取得單一商品
    @GetMapping("/{id}")
    public DeviceResponse getDevice(@PathVariable Integer id) {
        return deviceService.getDeviceById(id);
    }

    // 新增商品
    @PostMapping
    public Integer addDevice(@RequestBody DeviceRequest request) {
        // 前端送來的是 Request DTO，轉成 Entity
        Device device = toEntity(request);
        return deviceService.addDevice(device);
    }

    // 修改商品
    @PutMapping
    public boolean updateDevice(@RequestBody DeviceRequest request, @RequestParam Integer id) {
        Device device = toEntity(request);
        device.setId(id);
        System.out.println(request.getImage());
        try {
            return deviceService.updateDevice(device);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

    }

    // 刪除商品
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDevice(@PathVariable Integer id) {
        try {
            boolean success = deviceService.deleteDeviceById(id);
            if (success) {
                return ResponseEntity.ok("刪除成功");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("找不到該輔具");
            }
        } catch (DataIntegrityViolationException e) {
            //  關鍵改動：捕捉外鍵衝突
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("該輔具已有訂單紀錄，無法刪除，請改為下架");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("刪除失敗，請稍後再試");
        }
    }

    // 模糊搜尋
    @GetMapping("/search")
    public List<DeviceResponse> search(@RequestParam String keyword) {
        return deviceService.searchDevicesByName(keyword);
    }

    //查詢總數
    @GetMapping("/count")
    public int count() {
        return deviceService.countDevices();
    }

    // 排序查詢
    @GetMapping("/sort")
    public List<DeviceResponse> getSorted(@RequestParam String sortBy) {
        return deviceService.getAllDevicesSorted(sortBy);
    }

    //分頁查詢
    @GetMapping("/page")
    public List<DeviceResponse> getPaged(@RequestParam int offset, @RequestParam int limit) {
        return deviceService.getDevicesPaged(offset, limit);
    }

    //分頁 + 排序查詢
    @GetMapping("/page-sort")
    public List<DeviceResponse> getPagedSorted(@RequestParam int offset,
                                               @RequestParam int limit,
                                               @RequestParam String sortBy) {
        return deviceService.getDevicesPagedAndSorted(offset, limit, sortBy);
    }

    // 匯入 CSV
    @PostMapping("/import")
    public boolean importCsv(@RequestParam("file") MultipartFile file) {
        try {
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            return deviceService.importDevicesFromCsv(content);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 匯出 CSV
    @GetMapping("/export")
    public String exportDevicesToCsv() {
        return deviceService.exportDevicesToCsv();
    }

    // ─── 此兩個方法只為了處理 RequestDTO ↔ Entity ───
    private Device toEntity(DeviceRequest req) {
        return Device.builder()
                .id(req.getId())
                .name(req.getName())
                .sku(req.getSku())
                .unitPrice(req.getUnitPrice())
                .inventory(req.getInventory())
                .description(req.getDescription())
                .image(req.getImage())
                .isOnline(req.getIsOnline())
                .categoryId(req.getCategoryId())
                .createdByEmpId(req.getCreatedByEmpId())
                .build();
    }

    /**
     * 批次修改多筆輔具
     * @param requests 要更新的 DeviceRequest DTO 列表
     * @return 全部更新成功回 true，否則 false
     */
    @PutMapping("/batch")
    public boolean batchUpdateDevices(@RequestBody List<DeviceRequest> requests) {
        // 使用 stream 轉成 Entity list
        List<Device> devices = requests.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
        return deviceService.updateDevicesBatch(devices);
    }

    /**
     * 批次刪除多筆輔具
     * @param ids 要刪除的商品 ID 列表
     * @return 全部刪除成功回 true，否則 false
     */
    @DeleteMapping("/batch")
    public ResponseEntity<?> batchDeleteDevices(@RequestBody List<Integer> ids) {
        try {
            boolean success = deviceService.deleteDevicesBatch(ids);
            if (success) {
                return ResponseEntity.ok("批次刪除成功");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("部分或全部輔具不存在，刪除失敗");
            }
        } catch (DataIntegrityViolationException e) {
            // 如果其中一個 device 被訂單引用，會觸發這裡
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("部分輔具已有訂單紀錄，無法刪除，請改為下架");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("批次刪除失敗，請稍後再試");
        }
    }

    // 處理圖片上傳
    @PostMapping("/{id}/upload-image")
    public ResponseEntity<String> uploadDeviceImage(
            @PathVariable Integer id,
            @RequestParam("file") MultipartFile file) throws IOException {

        // 1. 空檔與類型檢查
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("請選擇檔案");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest().body("只支援圖片檔案");
        }

        // 2. 產生不會重覆的檔名
        String original = StringUtils.cleanPath(file.getOriginalFilename());
        String ext = StringUtils.getFilenameExtension(original);
        String filename = "device-" + id + "-" + System.currentTimeMillis() + "." + ext;

        // 3. 存到 uploads/images，不用 REPLACE_EXISTING
        Path target = Paths.get(uploadDir).resolve(filename);
        // 這裡不帶 REPLACE_EXISTING，若同名就會丟例外，你也可以 catch 丟例外改不同檔名
        Files.copy(file.getInputStream(), target);

        // 4. 更新資料庫 image 欄位
        deviceService.updateDeviceImage(id, filename);

        // 5. 組成完整 URL，回傳給前端
        String fullUrl = ServletUriComponentsBuilder
                .fromCurrentContextPath()          // 取得 "http://localhost:8080"
                .path("/images/")                  // 再加上 /images/
                .path(filename)                    // 再加上實際檔名
                .toUriString();                    // 組成字串

        return ResponseEntity.ok(fullUrl);
    }


}
