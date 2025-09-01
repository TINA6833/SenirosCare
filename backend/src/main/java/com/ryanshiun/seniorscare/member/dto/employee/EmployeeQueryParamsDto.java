package com.ryanshiun.seniorscare.member.dto.employee;

import lombok.Data;

// 定義前端回傳的客製化查詢內容
@Data
public class EmployeeQueryParamsDto {

    // 模糊查詢 (職等、停權名單)
    private String empName;
    // 停權名單 (1=normal, 0=ban)
    private Boolean isActive;
    // 限制回傳筆數
    private Integer limit;
    // 是否偏移做分頁用
    private Integer offset;
}
