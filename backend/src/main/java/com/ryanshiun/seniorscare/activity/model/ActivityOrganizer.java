package com.ryanshiun.seniorscare.activity.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ActivityOrganizer { // 主辦方更新內容表
    private Integer id;
    private Integer activityId;
    private String  title;
    private String  content;
    private LocalDateTime publishedAt;
}
