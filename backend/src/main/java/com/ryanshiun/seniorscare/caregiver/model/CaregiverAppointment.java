package com.ryanshiun.seniorscare.caregiver.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CaregiverAppointment {
  // 基本資訊 - 完全對應資料庫欄位
  private Integer id;                    // id
  private Integer memberId;              // member_id
  private Integer caregiverId;           // caregiver_id
  private LocalDateTime scheduledAt;     // scheduled_at
  private LocalDateTime endTime;         // end_time
  private String status;                 // status
  private Boolean isBlocked;             // is_blocked
  private Integer serviceTypeId;         // service_type_id
  private String serviceLocation;        // service_location
  private BigDecimal totalAmount;        // total_amount
  private String notes;                  // notes
  private String blockType;              // block_type

  // 評分相關欄位
  private Integer ratingScore;           // rating_score
  private String ratingComment;          // rating_comment
  private LocalDateTime ratedAt;         // rated_at
  private Boolean isRated;               // is_rated

  // 系統時間戳記
  private LocalDateTime createdAt;       // created_at
  private LocalDateTime cancelledAt;     // cancelled_at

}