package com.ryanshiun.seniorscare.device.dto;

import lombok.Data;

/**
 * 用於新增／修改分類的請求物件
 */
@Data
public class DeviceCategoryRequest {
    private String name;
    private Integer categoryId;
}
