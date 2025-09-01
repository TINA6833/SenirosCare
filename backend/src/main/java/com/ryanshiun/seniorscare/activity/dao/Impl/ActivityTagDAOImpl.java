package com.ryanshiun.seniorscare.activity.dao.Impl;

import com.ryanshiun.seniorscare.activity.dao.ActivityTagDAO;
import com.ryanshiun.seniorscare.activity.model.ActivityTag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class ActivityTagDAOImpl implements ActivityTagDAO {

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public Optional<ActivityTag> findByName(String name) {
        String sql = "SELECT id, name FROM activity_tag WHERE name = :name";
        List<ActivityTag> list = namedParameterJdbcTemplate.query(sql, Map.of("name", name),
                new BeanPropertyRowMapper<>(ActivityTag.class));
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Override
    public Integer insertTag(String name) {
        try {
            String sql = "INSERT INTO activity_tag(name) VALUES(:name)";
            var kh = new GeneratedKeyHolder();
            namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource("name", name), kh);
            return kh.getKey().intValue();
        } catch (DuplicateKeyException e) {
            // 競爭時重查
            return namedParameterJdbcTemplate.queryForObject(
                    "SELECT id FROM activity_tag WHERE name=:name", Map.of("name", name), Integer.class);
        }
    }

    @Override
    public List<ActivityTag> findByActivityId(int activityId) {
        String sql = """
          SELECT t.id, t.name
          FROM act_tag at
          JOIN activity_tag t ON t.id = at.tag_id
          WHERE at.activity_id = :id
        """;
        return namedParameterJdbcTemplate.query(sql, Map.of("id", activityId),
                new BeanPropertyRowMapper<>(ActivityTag.class));
    }
}
