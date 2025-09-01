package com.ryanshiun.seniorscare.roomType.service.Impl;

import com.ryanshiun.seniorscare.roomType.dao.RoomTypeDao;
import com.ryanshiun.seniorscare.roomType.dto.RoomTypeForm;
import com.ryanshiun.seniorscare.roomType.model.RoomType;
import com.ryanshiun.seniorscare.roomType.service.RoomTypeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

@Service
public class RoomTypeServiceImpl implements RoomTypeService {

    private final RoomTypeDao dao;

    // 統一圖片目錄與公開路徑（與前端一致）
    private static final File FS_UPLOAD_DIR = new File("uploads/images/RoomImg").getAbsoluteFile();
    private static final String PUBLIC_IMG_PREFIX = "images/RoomImg/"; // 存 DB 的相對路徑

    public RoomTypeServiceImpl(RoomTypeDao dao) {
        this.dao = dao;
    }

    // ====== C ======
    @Override
    public Integer addRoomType(RoomType room) {
        return dao.insert(room);
    }

    /** 新增：用表單建（不含圖片處理） */
    @Override
    public Integer addRoomType(RoomTypeForm form) {
        RoomType r = new RoomType();
        r.setName(form.getName());
        r.setPrice(form.getPrice() == null ? 0 : form.getPrice());
        r.setCapacity(form.getCapacity() == null ? 0 : form.getCapacity());
        r.setDescription(form.getDescription());
        r.setAvailable(Boolean.TRUE.equals(form.getIsAvailable()));
        r.setAdminNote(form.getAdminNote());
        return dao.insert(r);
    }

    @Override
    public Integer addRoomWithImage(RoomTypeForm form) {
        RoomType r = new RoomType();
        r.setName(form.getName());
        r.setPrice(form.getPrice() == null ? 0 : form.getPrice());
        r.setCapacity(form.getCapacity() == null ? 0 : form.getCapacity());
        r.setDescription(form.getDescription());
        r.setAvailable(Boolean.TRUE.equals(form.getIsAvailable()));
        r.setAdminNote(form.getAdminNote());

        String imagePath = resolveImagePathFromForm(form);
        if (imagePath != null && !imagePath.isBlank()) {
            r.setImagePath(imagePath);
        }
        return dao.insert(r);
    }

    // ====== R ======
    @Override
    @Transactional(readOnly = true)
    public List<RoomType> getAllRoomTypes() {
        return dao.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public RoomType getRoomTypeById(int id) {
        return dao.findById(id);
    }

    // ====== U / D ======
    @Override
    public boolean updateRoomType(RoomType room) {
        return dao.update(room);
    }

    @Override
    public boolean deleteRoomType(int id) {
        return dao.delete(id);
    }

    // ====== Queries ======
    @Override
    @Transactional(readOnly = true)
    public List<RoomType> getRoomTypesByPriceRange(int min, int max) {
        return dao.findByPriceRange(min, max);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomType> getRoomTypesByDescriptionKeyword(String keyword) {
        return dao.findByKeyword(keyword);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomType> getRoomTypesByCapacity(int capacity) {
        return dao.findByCapacity(capacity);
    }

    // ====== Pagination ======
    @Override
    @Transactional(readOnly = true)
    public List<RoomType> getRoomTypesPaged(int page, int size) {
        int offset = Math.max((page - 1), 0) * Math.max(size, 1);
        return dao.findPaged(offset, size);
    }

    @Override
    @Transactional(readOnly = true)
    public int countAllRoomTypes() {
        return dao.countAll();
    }

    // ====== CSV Import (path) ======
    @Override
    @Transactional
    public void importRoomTypesFromCSV(String csvPath) {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(csvPath), StandardCharsets.UTF_8))) {
            importCsvBuffered(br);
        } catch (IOException e) {
            throw new RuntimeException("Import CSV failed: " + e.getMessage(), e);
        }
    }

    // ====== CSV Export（與匯入順序一致，7 欄，snake_case）======
    @Override
    @Transactional(readOnly = true)
    public void exportToCSV(PrintWriter writer) {
        writer.println("name,price,capacity,description,image_path,is_available,admin_note");
        for (RoomType r : dao.findAll()) {
            writer.println(String.join(",",
                    csv(r.getName()),
                    csv(r.getPrice()),
                    csv(r.getCapacity()),
                    csv(r.getDescription()),
                    csv(r.getImagePath()),
                    // SQL Server BIT 最穩用 1/0
                    csv(r.isAvailable() ? 1 : 0),
                    csv(r.getAdminNote())
            ));
        }
        writer.flush();
    }

    // ====== CSV Import (multipart) ======
    @Override
    @Transactional
    public void importFromCSV(MultipartFile file) {
        if (file == null || file.isEmpty()) return;
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            importCsvBuffered(br);
        } catch (IOException e) {
            throw new RuntimeException("Import CSV(multipart) failed: " + e.getMessage(), e);
        }
    }

    // ====== Partial Update (Map) ======
    @Override
    public boolean partialUpdate(int id, Map<String, Object> updates) {
        RoomType room = dao.findById(id);
        if (room == null || updates == null || updates.isEmpty()) return false;

        if (has(updates, "name"))                room.setName(getStr(updates, "name"));
        if (has(updates, "description"))         room.setDescription(getStr(updates, "description"));
        if (hasAny(updates, "admin_note", "adminNote"))
            room.setAdminNote(getStrAny(updates, "admin_note", "adminNote"));

        if (has(updates, "price"))               room.setPrice(getInt(updates, "price"));
        if (has(updates, "capacity"))            room.setCapacity(getInt(updates, "capacity"));

        if (hasAny(updates, "is_available", "isAvailable"))
            room.setAvailable(getBoolAny(updates, "is_available", "isAvailable"));

        if (hasAny(updates, "image_path", "imagePath"))
            room.setImagePath(getStrAny(updates, "image_path", "imagePath"));

        return dao.update(room);
    }

    private int getInt(Map<String, Object> updates, String string) {
		// TODO Auto-generated method stub
		return 0;
	}

	private boolean getBoolAny(Map<String, Object> updates, String string, String string2) {
		// TODO Auto-generated method stub
		return false;
	}

	// ====== Partial Update (Form) ======
    @Override
    public boolean partialUpdate(int id, RoomTypeForm form) {
        RoomType room = dao.findById(id);
        if (room == null) return false;

        if (form.getName() != null)            room.setName(form.getName());
        if (form.getDescription() != null)     room.setDescription(form.getDescription());
        if (form.getPrice() != null)           room.setPrice(form.getPrice());
        if (form.getCapacity() != null)        room.setCapacity(form.getCapacity());
        if (form.getIsAvailable() != null)     room.setAvailable(form.getIsAvailable());
        if (form.getAdminNote() != null)       room.setAdminNote(form.getAdminNote());

        String imagePath = resolveImagePathFromForm(form);
        if (imagePath != null && !imagePath.isBlank()) {
            room.setImagePath(imagePath);
        }
        return dao.update(room);
    }

    // ====== Image helpers ======

    /** 決定 imagePath 來源：優先檔案，其次字串路徑（與前端一致的公開路徑） */
    private String resolveImagePathFromForm(RoomTypeForm form) {
        MultipartFile file = form.getImage();
        if (file != null && !file.isEmpty()) {
            try {
                return saveImage(file); // 回傳 images/RoomImg/{filename}
            } catch (IOException e) {
                throw new RuntimeException("Save image failed: " + e.getMessage(), e);
            }
        }
        if (form.getImagePath() != null && !form.getImagePath().isBlank()) {
            return form.getImagePath().trim();
        }
        return null;
    }

    /** 寫入 uploads/images/RoomImg；DB 存 images/RoomImg/{filename} */
    private String saveImage(MultipartFile file) throws IOException {
        if (!FS_UPLOAD_DIR.exists()) Files.createDirectories(FS_UPLOAD_DIR.toPath());

        String original = Optional.ofNullable(file.getOriginalFilename()).orElse("upload");
        original = java.nio.file.Paths.get(original).getFileName().toString();

        String ext = "";
        int dot = original.lastIndexOf('.');
        if (dot >= 0 && dot < original.length() - 1) {
            ext = original.substring(dot).toLowerCase(); // .png
            original = original.substring(0, dot);
        }

        String base = original
                .replaceAll("[\\s]+", "_")
                .replaceAll("[^0-9A-Za-z\\u4e00-\\u9fa5._-]", "_")
                .trim();
        if (base.isBlank()) base = "file";
        if (base.length() > 200) base = base.substring(0, 200);

        // 防同名覆蓋
        String filename = base + ext;
        File dest = new File(FS_UPLOAD_DIR, filename);
        int i = 1;
        while (dest.exists()) {
            filename = base + "-" + i + ext;
            dest = new File(FS_UPLOAD_DIR, filename);
            i++;
        }

        file.transferTo(dest);
        return PUBLIC_IMG_PREFIX + filename; // e.g. images/RoomImg/room1.png
    }

    // ====== CSV helpers（重點：支援 header 對映 + 無 header 回退順序）======

    private void importCsvBuffered(BufferedReader br) throws IOException {
        // 讀第一行（可能是 header，也可能是資料列或空行）
        String firstLine = readNextNonEmptyLine(br);
        if (firstLine == null) return;

        firstLine = stripBOM(firstLine);
        String[] firstCols = splitCsv(firstLine);

        boolean hasHeader = looksLikeHeader(firstCols);

        Map<String, Integer> idx = new HashMap<>();
        if (hasHeader) {
            for (int i = 0; i < firstCols.length; i++) {
                idx.put(norm(stripBOM(firstCols[i])), i);
            }
        } else {
            // 沒有 header 的話，firstLine 也是資料列，要先處理它
            processDataRow(firstCols, false, idx);
        }

        String line;
        while ((line = br.readLine()) != null) {
            line = stripBOM(line);
            if (line == null || line.isBlank()) continue; // 跳過空行
            String[] cols = splitCsv(line);
            if (cols.length == 0) continue;               // 跳過空欄
            processDataRow(cols, hasHeader, idx);
        }
    }

    private void processDataRow(String[] cols, boolean hasHeader, Map<String, Integer> idx) {
        RoomType r = new RoomType();

        if (hasHeader) {
            // 欄名導向（大小寫不敏感，容錯常見別名）
            r.setName(getStr(cols, idx, "name"));
            r.setPrice(parseIntSafe(getStr(cols, idx, "price")));
            r.setCapacity(parseIntSafe(getStr(cols, idx, "capacity")));
            r.setDescription(getStr(cols, idx, "description"));

            String img = getStr(cols, idx, "image_path", "imagepath", "image_url", "imageurl");
            r.setImagePath(img);

            Boolean avail = parseBoolObj(getStr(cols, idx, "is_available", "available", "isavailable"));
            r.setAvailable(avail != null ? avail : true);

            r.setAdminNote(getStr(cols, idx, "admin_note", "adminnote", "note"));
        } else {
            // 無 header：固定順序 name, price, capacity, description, image_path, is_available, admin_note
            int i = 0;
            r.setName(ns(getByIdx(cols, i++)));
            r.setPrice(parseIntSafe(getByIdx(cols, i++)));
            r.setCapacity(parseIntSafe(getByIdx(cols, i++)));
            r.setDescription(ns(getByIdx(cols, i++)));
            r.setImagePath(ns(getByIdx(cols, i++)));
            r.setAvailable(parseBoolSafe(getByIdx(cols, i++)));
            r.setAdminNote(ns(getByIdx(cols, i++)));
        }

        // 匯入策略：全 INSERT（最單純不干擾既有資料）
        dao.insert(r);
    }

    // 讀下一行非空白（也會去掉 BOM）
    private String readNextNonEmptyLine(BufferedReader br) throws IOException {
        String s;
        while ((s = br.readLine()) != null) {
            s = stripBOM(s);
            if (s != null && !s.isBlank()) return s;
        }
        return null;
    }

    // 安全 CSV 分割：永不回傳 null（空行回傳空陣列）
    private static String[] splitCsv(String line) {
        if (line == null) return new String[0];
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') { cur.append('"'); i++; }
                else { inQuotes = !inQuotes; }
            } else if (ch == ',' && !inQuotes) { out.add(cur.toString()); cur.setLength(0); }
            else { cur.append(ch); }
        }
        out.add(cur.toString());
        return out.toArray(new String[0]);
    }

    private static String getByIdx(String[] arr, int i) {
        if (arr == null) return null; // 防 NPE
        return (i >= 0 && i < arr.length) ? arr[i] : null;
    }

    private static String stripBOM(String s) {
        if (s == null) return null;
        return s.startsWith("\uFEFF") ? s.substring(1) : s;
    }

    private static boolean looksLikeHeader(String[] cols) {
        if (cols == null || cols.length == 0) return false;
        String c0 = norm(stripBOM(cols[0]));
        if (c0.isEmpty()) return false;
        // 只要第一欄像欄名（常見欄或含字母）就視為 header
        return c0.equals("name") || c0.equals("price") || c0.equals("description") || c0.matches(".*[a-z].*");
    }

    private static String norm(String s) {
        if (s == null) return "";
        return s.trim().toLowerCase().replaceAll("[^a-z0-9_]", "");
    }

    private static String csv(Object v) {
        String s = v == null ? "" : String.valueOf(v);
        if (s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r")) {
            s = s.replace("\"", "\"\"");
            return "\"" + s + "\"";
        }
        return s;
    }

    private static int parseIntSafe(String s) {
        try { return (s == null || s.isBlank()) ? 0 : Integer.parseInt(s.trim()); }
        catch (Exception e) { return 0; }
    }

    private static boolean parseBoolSafe(String s) {
        Boolean b = parseBoolObj(s);
        return b != null && b;
    }

    private static Boolean parseBoolObj(String s) {
        if (s == null) return null;
        String t = s.trim().toLowerCase();
        switch (t) {
            case "1", "true", "yes", "y", "是": return true;
            case "0", "false", "no", "n", "否": return false;
            default: return null;
        }
    }

    private static String ns(String s) { return (s == null) ? "" : s.trim(); }

    private static boolean has(Map<String, Object> map, String key) { return map.containsKey(key) && map.get(key) != null; }
    private static boolean hasAny(Map<String, Object> map, String... keys) { for (String k : keys) if (has(map, k)) return true; return false; }
    private static String getStr(Map<String, Object> map, String key) { Object v = map.get(key); return v == null ? null : String.valueOf(v); }
    private static String getStrAny(Map<String, Object> map, String... keys) { for (String k : keys) if (has(map, k)) return getStr(map, k); return null; }

    private static String getStr(String[] cols, Map<String, Integer> idx, String... keys) {
        for (String k : keys) {
            Integer i = idx.get(norm(k));
            if (i != null && i >= 0 && i < cols.length) {
                String val = cols[i];
                return (val == null || val.isBlank()) ? null : val.trim();
            }
        }
        return null;
    }

    // ====== Image APIs exposed to controller ======
    @Override
    public void updateImagePath(int id, String imagePath) {
        if (imagePath == null) return;
        RoomType room = dao.findById(id);
        if (room == null) return;
        room.setImagePath(imagePath.trim());
        dao.update(room);
    }

    @Override
    public void updateImage(int id, MultipartFile img) {
        if (img == null || img.isEmpty()) return;
        try {
            String path = saveImage(img); // images/RoomImg/{filename}
            updateImagePath(id, path);
        } catch (IOException e) {
            throw new RuntimeException("Save image failed: " + e.getMessage(), e);
        }
    }

    // ====== Filtered Search ======
    @Override
    @Transactional(readOnly = true)
    public List<RoomType> searchFiltered(String keyword, Integer minPrice, Integer maxPrice, Integer capacity,
                                         List<Integer> featureIds, boolean matchAll,
                                         String sortBy, String order, int page, int size) {
        int offset = Math.max(page - 1, 0) * Math.max(size, 1);
        return dao.findFilteredPaged(keyword, minPrice, maxPrice, capacity,
                featureIds, matchAll, sortBy, order, offset, size);
    }

    @Override
    @Transactional(readOnly = true)
    public int countFiltered(String keyword, Integer minPrice, Integer maxPrice, Integer capacity,
                             List<Integer> featureIds, boolean matchAll) {
        return dao.countFiltered(keyword, minPrice, maxPrice, capacity, featureIds, matchAll);
    }
}
