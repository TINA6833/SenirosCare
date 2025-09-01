package com.ryanshiun.seniorscare.activity.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ActivityResponse {
    private Integer id;
    private String name;
    private String category;
    private Integer limit;
    private Integer current;

    private LocalDate date;
    private LocalDate end;

    private String time;
    private LocalDate registrationStart;
    private LocalDate registrationEnd;

    private String location;
    private BigDecimal latitude;
    private BigDecimal longitude;

    private String instructor;
    private Boolean status;
    private String description;
    private String image;
}
