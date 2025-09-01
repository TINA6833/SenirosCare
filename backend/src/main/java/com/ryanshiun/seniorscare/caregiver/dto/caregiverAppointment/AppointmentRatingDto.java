package com.ryanshiun.seniorscare.caregiver.dto.caregiverAppointment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentRatingDto {
    private Integer ratingScore;
    private String ratingComment;
}
