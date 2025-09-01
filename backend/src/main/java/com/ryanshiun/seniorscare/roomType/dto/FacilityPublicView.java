package com.ryanshiun.seniorscare.roomType.dto;


import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FacilityPublicView {
	private int id;
    private String name;
    private String description;

    @JsonProperty("image_path")
    private String imagePath;

    @JsonProperty("is_available")
    private boolean isAvailable;

    @JsonProperty("created_at")
    private Date createdAt;

    @JsonProperty("updated_at")
    private Date updatedAt;
}