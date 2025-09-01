package com.ryanshiun.seniorscare.activity.dao.Impl;

import com.ryanshiun.seniorscare.activity.dao.ActTagDAO;
import com.ryanshiun.seniorscare.activity.model.Activity;
import com.ryanshiun.seniorscare.activity.rowmapper.ActivityRowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class ActTagDAOImpl implements ActTagDAO {

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public void deleteByActivityId(int activityId) {
        namedParameterJdbcTemplate.update("DELETE FROM act_tag WHERE activity_id=:id", Map.of("id", activityId));
    }

    @Override
    public void insert(int activityId, int tagId) {
        namedParameterJdbcTemplate.update("INSERT INTO act_tag(activity_id, tag_id) VALUES(:aid,:tid)",
                Map.of("aid", activityId, "tid", tagId));
    }

    @Override
    public List<Activity> findActivitiesBySingleTag(String tag) {
        String sql = """
          SELECT a.*
          FROM activity a
          JOIN act_tag at ON a.id = at.activity_id
          JOIN activity_tag t ON t.id = at.tag_id
          WHERE t.name = :tag
          ORDER BY a.id DESC
        """;
        return namedParameterJdbcTemplate.query(sql, Map.of("tag", tag), new ActivityRowMapper());
    }

    @Override
    public List<Activity> findActivitiesByAnyTags(List<String> tags) {
        String sql = """
          SELECT DISTINCT a.*
          FROM activity a
          JOIN act_tag at ON a.id = at.activity_id
          JOIN activity_tag t ON t.id = at.tag_id
          WHERE t.name IN (:tags)
          ORDER BY a.id DESC
        """;
        return namedParameterJdbcTemplate.query(sql, Map.of("tags", tags), new ActivityRowMapper());
    }

    @Override
    public List<Activity> findActivitiesByAllTags(List<String> tags) {
        String sql = """
          SELECT a.*
          FROM activity a
          JOIN act_tag at ON a.id = at.activity_id
          JOIN activity_tag t ON t.id = at.tag_id
          WHERE t.name IN (:tags)
          GROUP BY a.id, a.name, a.category, a.[limit], a.[current], a.date, a.[end],
                   a.time, a.registration_start, a.registration_end, a.location,
                   a.latitude, a.longitude, a.instructor, a.status, a.[description], a.image
          HAVING COUNT(DISTINCT t.name) = :n
          ORDER BY a.id DESC
        """;
        return namedParameterJdbcTemplate.query(sql, Map.of("tags", tags, "n", tags.size()), new ActivityRowMapper());
    }


}
