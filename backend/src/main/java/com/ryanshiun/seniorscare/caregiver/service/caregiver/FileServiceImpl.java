package com.ryanshiun.seniorscare.caregiver.service.caregiver;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    @Override
    public String uploadFile(MultipartFile file, String directory) throws IOException {
        try {
            // 參考 RoomType 的做法：直接使用 uploads/images
            // 您的路徑：uploads/images/caregiver/caregiver_photo/
            File uploadDir = new File("uploads/images/" + directory).getAbsoluteFile();

            // 確保目錄存在
            if (!uploadDir.exists()) {
                Files.createDirectories(uploadDir.toPath());
                System.out.println("建立目錄: " + uploadDir.getAbsolutePath());
            }

            // 驗證檔案
            if (file.isEmpty()) {
                throw new IOException("檔案為空");
            }

            // 產生唯一檔名
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.contains(".")) {
                throw new IOException("無效的檔案名稱");
            }

            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFilename = UUID.randomUUID().toString() + extension;

            // 儲存檔案
            Path filePath = uploadDir.toPath().resolve(newFilename);
            System.out.println("準備儲存檔案到: " + filePath.toAbsolutePath());

            Files.copy(file.getInputStream(), filePath);

            // 確認檔案已儲存
            if (!Files.exists(filePath)) {
                throw new IOException("檔案儲存失敗");
            }

            System.out.println("檔案儲存成功: " + filePath.toAbsolutePath());

            // 回傳 Web 可訪問的路徑格式（參考 RoomType）
            String webPath = "/images/" + directory + "/" + newFilename;
            System.out.println("回傳 Web 路徑: " + webPath);

            return webPath;
        } catch (Exception e) {
            System.err.println("uploadFile 發生錯誤: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("檔案上傳失敗: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteFile(String filePath) {
        try {
            // 檢查是否為外部 URL，如果是就不需要刪除
            if (filePath == null || filePath.trim().isEmpty()) {
                System.out.println("檔案路徑為空，跳過刪除");
                return true;
            }

            if (filePath.startsWith("http://") || filePath.startsWith("https://")) {
                System.out.println("外部 URL 照片，跳過刪除: " + filePath);
                return true; // 外部 URL 不需要刪除
            }

            if (filePath.startsWith("data:image")) {
                System.out.println("Base64 圖片，跳過刪除: " + filePath.substring(0, 50) + "...");
                return true; // Base64 圖片不需要刪除
            }

            // 移除 Web 路徑前綴，轉換為檔案系統路徑
            String relativePath = filePath.startsWith("/images/")

                    ? filePath.substring(8)
                    : filePath;


            Path fullPath = Paths.get("uploads/images", relativePath);
            System.out.println("準備刪除檔案: " + fullPath.toAbsolutePath());

            boolean deleted = Files.deleteIfExists(fullPath);
            System.out.println("檔案刪除結果: " + deleted);

            return deleted;
        } catch (Exception e) {
            System.err.println("刪除檔案時發生錯誤: " + e.getMessage());
            System.err.println("問題檔案路徑: " + filePath);
            e.printStackTrace();
            return false; // 刪除失敗但不影響整體流程
        }
    }
}