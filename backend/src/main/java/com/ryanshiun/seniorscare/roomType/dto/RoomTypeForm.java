package com.ryanshiun.seniorscare.roomType.dto;

import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
@Data
public class RoomTypeForm {
    private String name;
    private Integer price;
    private Integer capacity;
    private String description;
    private MultipartFile image; // 上傳檔案
    private String imagePath;    // 若前端直接傳路徑
    private Boolean isAvailable; // 是否上架
    private String adminNote;    // 管理員備註
}