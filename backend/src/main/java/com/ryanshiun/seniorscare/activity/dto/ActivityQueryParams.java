 package com.ryanshiun.seniorscare.activity.dto;

import lombok.Data;

import java.util.Date;

@Data
public class ActivityQueryParams {
	
    private String category;
    private Date date;
    private String instructor;
    private String location;
    private Boolean status;
    
    //模糊查詢
    private String name;
}
