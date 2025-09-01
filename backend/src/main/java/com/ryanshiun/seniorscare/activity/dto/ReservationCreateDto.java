package com.ryanshiun.seniorscare.activity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReservationCreateDto {

    @NotNull
    private Integer activityId;
    private Integer memberId;

    @JsonProperty(defaultValue = "1")
    private Integer num;
}
