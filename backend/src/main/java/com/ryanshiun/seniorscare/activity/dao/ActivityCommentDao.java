package com.ryanshiun.seniorscare.activity.dao;

import java.util.List;
import com.ryanshiun.seniorscare.activity.model.ActivityComment;

public interface ActivityCommentDao {
    Long insert(ActivityComment c);
    int update(Long id, Long memberId, String comment, Integer rating);
    int delete(Long id, Long memberId);

    List<ActivityComment> findByActivityId(Integer activityId);
    ActivityComment findByActivityAndMember(Integer activityId, Long memberId);

    boolean hasConfirmedRegistration(Integer activityId, Long memberId);
    boolean activityEnded(Integer activityId);
}
