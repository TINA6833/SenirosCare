package com.ryanshiun.seniorscare.activity.dao.Impl;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import com.ryanshiun.seniorscare.activity.dao.ActivityOrganizerDao;
import com.ryanshiun.seniorscare.activity.model.ActivityOrganizer;

@Repository
public class ActivityOrganizerDaoImpl implements ActivityOrganizerDao {
    @Autowired
    private NamedParameterJdbcTemplate jdbc;

    @Override
    public List<ActivityOrganizer> findByActivityId(Integer activityId) {
        String sql = "SELECT * FROM activity_organizer WHERE activity_id = :aid";
        return jdbc.query(sql,
            Map.of("aid", activityId),
            new BeanPropertyRowMapper<>(ActivityOrganizer.class));
    }

    @Override
    public Integer insert(ActivityOrganizer org) {
        String sql = """
            INSERT INTO activity_organizer
              (activity_id, title, content, published_at)
            VALUES
              (:aid, :t, :c, :p)
        """;
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(sql, new MapSqlParameterSource()
            .addValue("aid", org.getActivityId())
            .addValue("t", org.getTitle())
            .addValue("c", org.getContent())
            .addValue("p", org.getPublishedAt()),
          kh);
        return kh.getKey().intValue();
    }
}
