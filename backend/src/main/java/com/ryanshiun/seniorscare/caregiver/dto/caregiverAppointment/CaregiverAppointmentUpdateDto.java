package com.ryanshiun.seniorscare.caregiver.dto.caregiverAppointment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CaregiverAppointmentUpdateDto {
    private LocalDateTime scheduledAt;
    private LocalDateTime endTime;
    private String status;
    private Integer serviceTypeId;
    private String serviceLocation;
    private BigDecimal totalAmount;
    private String notes;
    private String blockType;
}