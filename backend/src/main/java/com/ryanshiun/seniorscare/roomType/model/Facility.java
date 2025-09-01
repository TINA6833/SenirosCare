package com.ryanshiun.seniorscare.roomType.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class Facility {
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