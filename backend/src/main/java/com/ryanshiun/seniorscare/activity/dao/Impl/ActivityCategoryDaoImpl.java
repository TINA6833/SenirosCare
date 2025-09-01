package com.ryanshiun.seniorscare.activity.dao.Impl;

import com.ryanshiun.seniorscare.activity.dao.ActivityCategoryDao;
import com.ryanshiun.seniorscare.activity.model.ActivityCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class ActivityCategoryDaoImpl implements ActivityCategoryDao {

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;


    @Override
    public List<ActivityCategory> findAllActive() {
        String sql= """
                SELECT * From activity_category
                Where is_active=1
                ORDER BY id
                """;
        return namedParameterJdbcTemplate.query(sql,new BeanPropertyRowMapper<>(ActivityCategory.class));
    }

    @Override
    public boolean existsByName(String name) {
        String sql = """
            SELECT COUNT(1)
            FROM activity_category
            WHERE is_active = 1 AND name = :name
        """;
        Integer n = namedParameterJdbcTemplate.queryForObject(sql, Map.of("name", name), Integer.class);
        return n != null && n > 0;
    }
}
