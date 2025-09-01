package com.ryanshiun.seniorscare.activity.model;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

@Data
public class Activity { // 主表
    private Integer id;
    private String name;
    private String category;
    private Integer limit;
    private Integer current;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate end;

    public LocalDate getEnd() {
        return end;
    }

    public void setEnd(LocalDate end) {
        this.end = end;
    }

    
    private String time;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate registrationStart;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate registrationEnd;
    private String location;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String instructor;
    private Boolean status;
    private String description;
    private String image;

}

