package com.ryanshiun.seniorscare.activity.model;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityComment {
    private Integer id;
    private Integer activityId;
    private Long    memberId;
    private String  comment;      // 評論內容
    private Integer rating;       // 1~5，可為 null
    private LocalDateTime createdAt; // 建立時間
}
