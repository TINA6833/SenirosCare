package com.ryanshiun.seniorscare.activity.dao.Impl;

import java.util.List;
import java.util.Map;

import com.ryanshiun.seniorscare.activity.dao.ActivityCommentDao;
import com.ryanshiun.seniorscare.activity.model.ActivityComment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class ActivityCommentDaoImpl implements ActivityCommentDao {

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public Long insert(ActivityComment c) {
        String sql = """
            INSERT INTO activity_comment(activity_id, member_id, comment, rating, created_at)
            VALUES(:aid, :mid, :comment, :rating, SYSUTCDATETIME())
        """;
        MapSqlParameterSource p = new MapSqlParameterSource()
                .addValue("aid", c.getActivityId())
                .addValue("mid", c.getMemberId())
                .addValue("comment", c.getComment())
                .addValue("rating", c.getRating());
        KeyHolder kh = new GeneratedKeyHolder();
        namedParameterJdbcTemplate.update(sql, p, kh);
        return ((Number) kh.getKey()).longValue();
    }

    @Override
    public int update(Long id, Long memberId, String comment, Integer rating) {
        String sql = """
            UPDATE activity_comment
               SET comment = :comment, rating = :rating
             WHERE id = :id AND member_id = :mid
        """;
        return namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("comment", comment)
                .addValue("rating", rating)
                .addValue("id", id)
                .addValue("mid", memberId));
    }

    @Override
    public int delete(Long id, Long memberId) {
        String sql = "DELETE FROM activity_comment WHERE id=:id AND member_id=:mid";
        return namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource().addValue("id", id).addValue("mid", memberId));
    }

    @Override
    public List<ActivityComment> findByActivityId(Integer activityId) {
        String sql = "SELECT * FROM activity_comment WHERE activity_id=:aid ORDER BY created_at DESC";
        return namedParameterJdbcTemplate.query(sql, Map.of("aid", activityId),
                BeanPropertyRowMapper.newInstance(ActivityComment.class));
    }

    @Override
    public ActivityComment findByActivityAndMember(Integer activityId, Long memberId) {
        String sql = "SELECT * FROM activity_comment WHERE activity_id=:aid AND member_id=:mid";
        var list = namedParameterJdbcTemplate.query(sql,
                new MapSqlParameterSource().addValue("aid", activityId).addValue("mid", memberId),
                BeanPropertyRowMapper.newInstance(ActivityComment.class));
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public boolean hasConfirmedRegistration(Integer activityId, Long memberId) {
        String sql = """
            SELECT COUNT(*) FROM activity_registration
            WHERE activity_id=:aid AND member_id=:mid AND status IN ('CONFIRMED')
        """;
        Integer n = namedParameterJdbcTemplate.queryForObject(sql, Map.of("aid", activityId, "mid", memberId), Integer.class);
        return n != null && n > 0;
    }

    @Override
    public boolean activityEnded(Integer activityId) {
        String sql = "SELECT CASE WHEN [end] <= CONVERT(date, SYSUTCDATETIME()) THEN 1 ELSE 0 END FROM activity WHERE id=:aid";
        Integer v = namedParameterJdbcTemplate.queryForObject(sql, Map.of("aid", activityId), Integer.class);
        return v != null && v == 1;
    }
}
