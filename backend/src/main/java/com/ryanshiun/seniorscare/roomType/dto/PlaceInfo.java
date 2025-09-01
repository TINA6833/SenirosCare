package com.ryanshiun.seniorscare.roomType.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
public class PlaceInfo {
    private String name;
    private String address;
    private double lat;
    private double lng;
}