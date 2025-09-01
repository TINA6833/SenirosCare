package com.ryanshiun.seniorscare.activity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ActivityRequest {

	@NotNull
    private String name;

    @NotNull
    private String category;
    
    @JsonProperty(defaultValue = "30")
    private Integer limit;

    @JsonProperty(defaultValue = "0")
    private Integer current;

    @NotNull
    private LocalDate date;

    @NotNull
    private LocalDate end;

    @NotNull
    private String time;

    @NotNull
    private LocalDate registrationStart;

    @NotNull
    private LocalDate registrationEnd;

    @NotNull
    private String location;

    private BigDecimal latitude;
    private BigDecimal longitude;

    private String instructor;
    @JsonProperty(defaultValue = "1")
    private Boolean status;
    private String description;
    private String image;
}
