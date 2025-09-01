package com.ryanshiun.seniorscare.device.dto;

import lombok.Data;

/**
 * 回傳給前端的分類資料物件
 */
@Data
public class DeviceCategoryResponse {
    private Integer id;
    private String name;
    private Integer categoryId;
}
