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
import com.ryanshiun.seniorscare.activity.dao.ActivityPhotoDao;
import com.ryanshiun.seniorscare.activity.model.ActivityPhoto;

@Repository
public class ActivityPhotoDaoImpl implements ActivityPhotoDao {
    @Autowired
    private NamedParameterJdbcTemplate jdbc;

    @Override
    public Integer insert(ActivityPhoto photo) {
        String sql = """
            INSERT INTO activity_photo
              (activity_id, photo_url, uploaded)
            VALUES
              (:aid, :url, :upd)
        """;
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(sql, new MapSqlParameterSource()
            .addValue("aid", photo.getActivityId())
            .addValue("url", photo.getPhotoUrl())
            .addValue("upd", photo.getUploaded()),
          kh);
        return kh.getKey().intValue();
    }
    @Override
    public List<ActivityPhoto> findByActivityId(Integer activityId) {
        String sql = "SELECT * FROM activity_photo WHERE activity_id = :aid ORDER BY uploaded DESC";
        return jdbc.query(sql,
            Map.of("aid", activityId),
            new BeanPropertyRowMapper<>(ActivityPhoto.class));
    }
}
