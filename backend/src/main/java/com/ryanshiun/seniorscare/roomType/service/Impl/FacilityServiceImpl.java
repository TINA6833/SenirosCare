package com.ryanshiun.seniorscare.roomType.service.Impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.ryanshiun.seniorscare.roomType.dao.FacilityDao;
import com.ryanshiun.seniorscare.roomType.dto.FacilityForm;
import com.ryanshiun.seniorscare.roomType.model.Facility;
import com.ryanshiun.seniorscare.roomType.service.FacilityService;

@Service
public class FacilityServiceImpl implements FacilityService {

    private final FacilityDao dao;

    // 存檔實體路徑：{user.dir}/uploads/images/roomImg
    private final Path storageDir = Paths.get(
            System.getProperty("user.dir"), "uploads", "images", "roomImg"
    ).toAbsolutePath().normalize();

    // 存到 DB 的公開前綴（無前導斜線）
    private final String publicPrefix = "images/RoomImg";

    public FacilityServiceImpl(FacilityDao dao) {
        this.dao = dao;
    }

    // ===== C =====
    @Override
    public int addFacility(Facility facility) {
        return dao.addFacility(facility);
    }

    // ===== R =====
    @Override @Transactional(readOnly = true)
    public Facility getFacilityById(int id) {
        return dao.getFacilityById(id);
    }

    @Override @Transactional(readOnly = true)
    public List<Facility> getAllFacilities() {
        return dao.getAllFacilities();
    }

    @Override @Transactional(readOnly = true)
    public List<Facility> getAvailableFacilities() {
        return dao.getAvailableFacilities();
    }

    @Override @Transactional(readOnly = true)
    public List<Facility> searchFacilitiesByName(String keyword) {
        return dao.searchFacilitiesByName(keyword);
    }

    @Override @Transactional(readOnly = true)
    public List<Facility> getFacilitiesByAvailability(boolean isAvailable) {
        return dao.getFacilitiesByAvailability(isAvailable);
    }

    // ===== U =====
    @Override
    public boolean updateFacility(Facility facility) {
        return dao.updateFacility(facility);
    }

    // ===== Partial Update (Map / JSON) =====
    @Override
    public boolean partialUpdate(int id, Map<String, Object> updates) {
        Facility f = dao.getFacilityById(id);
        if (f == null || updates == null || updates.isEmpty()) return false;

        if (has(updates, "name"))        f.setName(getStr(updates, "name"));
        if (has(updates, "description")) f.setDescription(getStr(updates, "description"));

        if (hasAny(updates, "image_path", "imagePath"))
            f.setImagePath(getStrAny(updates, "image_path", "imagePath"));

        if (hasAny(updates, "is_available", "isAvailable"))
            f.setAvailable(getBoolAny(updates, "is_available", "isAvailable"));

        return dao.updateFacility(f);
    }

    // ===== Partial Update (multipart) =====
    @Override
    public boolean partialUpdate(int id, FacilityForm form) {
        Facility f = dao.getFacilityById(id);
        if (f == null) return false;

        if (form.getName() != null)        f.setName(form.getName());
        if (form.getDescription() != null) f.setDescription(form.getDescription());
        if (form.getIsAvailable() != null) f.setAvailable(form.getIsAvailable());

        // 圖片：優先使用上傳檔案；其次 imagePath；否則不變
        String path = resolveImagePathFromForm(form);
        if (path != null && !path.isBlank()) {
            f.setImagePath(path);
        }

        return dao.updateFacility(f);
    }

    // ===== Update Image only =====
    @Override
    public boolean updateImage(int id, MultipartFile image) {
        if (image == null || image.isEmpty()) return false;
        Facility f = dao.getFacilityById(id);
        if (f == null) return false;

        String path = saveImage(image);
        f.setImagePath(path);
        return dao.updateFacility(f);
    }

    @Override
    public boolean updateImagePath(int id, String imagePath) {
        Facility f = dao.getFacilityById(id);
        if (f == null) return false;
        f.setImagePath(imagePath);
        return dao.updateFacility(f);
    }

    // ===== D =====
    @Override
    public boolean deleteFacility(int id) {
        return dao.deleteFacility(id);
    }

    // ===== helpers =====
    private String resolveImagePathFromForm(FacilityForm form) {
        MultipartFile img = form.getImage();
        if (img != null && !img.isEmpty()) {
            return saveImage(img);
        }
        if (form.getImagePath() != null && !form.getImagePath().isBlank()) {
            return form.getImagePath().trim();
        }
        return null;
    }

    /**
     * 不使用 UUID：
     * - 檔案實體存到 {user.dir}/uploads/images/roomImg
     * - 同名檔以 -1, -2… 避免覆蓋
     * - 存到 DB 的字串為 "roomImg/檔名"（無前導斜線）
     */
    private String saveImage(MultipartFile file) {
        try {
            Files.createDirectories(storageDir);

            String original = Optional.ofNullable(file.getOriginalFilename()).orElse("image");
            String cleaned  = StringUtils.cleanPath(original)
                    .replaceAll("[\\\\/:*?\"<>|]", "_")
                    .trim();

            String name = cleaned;
            String ext  = "";
            int dot = cleaned.lastIndexOf('.');
            if (dot >= 0) {
                name = cleaned.substring(0, dot);
                ext  = cleaned.substring(dot); // 含 .
            }

            Path target = storageDir.resolve(name + ext);
            int idx = 1;
            while (Files.exists(target)) {
                target = storageDir.resolve(name + "-" + idx + ext);
                idx++;
            }

            file.transferTo(target.toFile());

            // 存到 DB 的值（前端顯示時，把 roomImg/ 轉成 /uploads/images/roomImg/ 再存取）
            return publicPrefix + "/" + target.getFileName().toString();

        } catch (IOException e) {
            throw new RuntimeException("Save facility image failed: " + e.getMessage(), e);
        }
    }

    // ------- small utils for Map -------
    private static boolean has(Map<String, Object> map, String key) {
        return map.containsKey(key) && map.get(key) != null;
    }
    private static boolean hasAny(Map<String, Object> map, String... keys) {
        for (String k : keys) if (has(map, k)) return true;
        return false;
    }
    private static String getStr(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v == null ? null : String.valueOf(v);
    }
    private static String getStrAny(Map<String, Object> map, String... keys) {
        for (String k : keys) if (has(map, k)) return getStr(map, k);
        return null;
    }
    private static boolean getBoolAny(Map<String, Object> map, String... keys) {
        for (String k : keys) if (has(map, k)) return getBool(map, k);
        return false;
    }
    private static boolean getBool(Map<String, Object> map, String key) {
        Object v = map.get(key);
        if (v == null) return false;
        if (v instanceof Boolean) return (Boolean) v;
        String s = String.valueOf(v).trim().toLowerCase();
        return "1".equals(s) || "true".equals(s) || "yes".equals(s);
    }
}