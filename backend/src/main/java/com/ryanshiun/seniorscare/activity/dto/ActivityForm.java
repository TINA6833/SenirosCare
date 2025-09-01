package com.ryanshiun.seniorscare.activity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 用於接收活動 (Activity) 新增/更新表單資料
 * 支援 multipart 上傳圖片 或 直接傳 imagePath
 * 模仿 RoomTypeForm 的設計
 */
@Data
public class ActivityForm {

    @NotNull(message = "活動名稱不能為空")
    private String name;

    @NotNull(message = "活動分類不能為空")
    private String category;

    @JsonProperty(defaultValue = "30")
    private Integer limit;

    @JsonProperty(defaultValue = "0")
    private Integer current;

    @NotNull(message = "活動開始日期不能為空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    @NotNull(message = "活動結束日期不能為空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate end;

    @NotNull(message = "活動時間不能為空")
    private String time;

    @NotNull(message = "報名開始日期不能為空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate registrationStart;

    @NotNull(message = "報名結束日期不能為空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate registrationEnd;

    @NotNull(message = "活動地點不能為空")
    private String location;

    // 經緯度欄位改為非必填，配合資料庫結構
    private BigDecimal latitude;
    private BigDecimal longitude;

    private String instructor;

    @JsonProperty(defaultValue = "true")
    private Boolean status;

    private String description;

    /** 圖片檔案（multipart 上傳） */
    private MultipartFile image;

    /** 圖片路徑（直接指定，不上傳檔案） */
    private String imagePath;

    /** 管理員備註 */
    private String adminNote;
}