package com.ryanshiun.seniorscare.device.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceCategory {
    private Integer id;            // 自動編號主鍵
    private String name;           // 分類名稱
    private Integer categoryId;    // 分類排序用
}
