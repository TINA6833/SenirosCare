package com.ryanshiun.seniorscare.caregiver.dto.caregiverAppointment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CaregiverAppointmentQueryDto {
  private Integer memberId;
  private Integer caregiverId;
  private String status;
  private Boolean isBlocked;
  private LocalDateTime startDate;
  private LocalDateTime endDate;
  private Boolean isRated;
  private Integer limit = 20;
  private Integer offset = 0;
}