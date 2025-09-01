package com.ryanshiun.seniorscare.caregiver.dto.caregiver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CaregiverRequestDTO {

    @NotBlank(message = "中文姓名不能為空")
    @Size(max = 100, message = "中文姓名長度不能超過100字元")
    private String chineseName;

    @NotNull(message = "性別不能為空")
    private Boolean gender;

    @NotBlank(message = "連絡電話不能為空")
    @Size(max = 20, message = "連絡電話長度不能超過20字元")
    private String phone;

    @NotBlank(message = "電子信箱不能為空")
    @Email(message = "電子信箱格式不正確")
    @Size(max = 100, message = "電子信箱長度不能超過100字元")
    private String email;

    @NotNull(message = "服務年資不能為空")
    @Min(value = 0, message = "服務年資不能為負數")
    private Integer experienceYears;

    @Size(max = 500, message = "照片路徑長度不能超過500字元")
    private String photo;

    @Size(max = 200, message = "居住地址長度不能超過200字元")
    private String address;

    @Size(max = 100, message = "服務區域長度不能超過100字元")
    private String serviceArea;

    @DecimalMin(value = "0.00", message = "評價不能為負數")
    @DecimalMax(value = "5.00", message = "評價不能超過5.00")
    private BigDecimal averageRating;

    @Min(value = 0, message = "總評價次數不能為負數")
    private Integer totalRatings;

    @Min(value = 0, message = "總得分不能為負數")
    private Integer totalPoints;

    private Boolean isActive = true;

  @Size(max = 1000, message = "自我介紹長度不能超過1000字元")
  private String selfIntroduction;
}