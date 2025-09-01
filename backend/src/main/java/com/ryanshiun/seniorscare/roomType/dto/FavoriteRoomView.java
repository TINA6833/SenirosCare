package com.ryanshiun.seniorscare.roomType.dto;

import lombok.Data;

@Data
public class FavoriteRoomView {
    private int id;
    private String name;
    private String imagePath;
    private int price;
}