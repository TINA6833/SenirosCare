package com.ryanshiun.seniorscare.caregiver.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Caregiver {
    private Integer caregiverId;
    private String chineseName;
    private Boolean gender; // 0=女性, 1=男性
    private String phone;
    private String email;
    private Integer experienceYears;
    private String photo;
    private String address;
    private String serviceArea;
    private BigDecimal averageRating = BigDecimal.ZERO;
    private Integer totalRatings = 0;
    private Integer totalPoints = 0;
    private Boolean isActive = true;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String selfIntroduction;

  public String getSelfIntroduction() {
    return selfIntroduction;
  }

  public void setSelfIntroduction(String selfIntroduction) {
    this.selfIntroduction = selfIntroduction;
  }
}