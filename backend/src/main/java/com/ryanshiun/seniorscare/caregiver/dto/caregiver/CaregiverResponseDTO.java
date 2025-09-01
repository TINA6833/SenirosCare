package com.ryanshiun.seniorscare.caregiver.dto.caregiver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CaregiverResponseDTO {

    private Integer caregiverId;
    private String chineseName;
    private Boolean gender;
    private String phone;
    private String email;
    private Integer experienceYears;
    private String photo;
    private String address;
    private String serviceArea;
    private BigDecimal averageRating;
    private Integer totalRatings;
    private Integer totalPoints;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String selfIntroduction;
    // 性別顯示方法
    public String getGenderDisplay() {
        return gender != null ? (gender ? "男性" : "女性") : "未設定";
    }

    // 狀態顯示方法
    public String getStatusDisplay() {
        return isActive != null ? (isActive ? "在職" : "離職") : "未設定";
    }
}