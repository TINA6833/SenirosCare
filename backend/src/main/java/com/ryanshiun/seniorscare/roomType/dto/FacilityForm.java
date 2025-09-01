package com.ryanshiun.seniorscare.roomType.dto;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

/**
 * 用於接收設施(Facility) 新增/更新表單資料
 * 支援 multipart 上傳圖片 或 直接傳 imagePath
 */
@Data
public class FacilityForm {

    /** 設施名稱 */
    private String name;

    /** 設施描述 */
    private String description;

    /** 圖片檔案（multipart 上傳） */
    private MultipartFile image;

    /** 圖片路徑（直接指定，不上傳檔案） */
    private String imagePath;

    /** 是否啟用 */
    private Boolean isAvailable;
}
