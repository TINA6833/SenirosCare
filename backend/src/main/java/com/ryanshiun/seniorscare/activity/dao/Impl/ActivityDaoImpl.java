package com.ryanshiun.seniorscare.activity.dao.Impl;

import com.ryanshiun.seniorscare.activity.dao.ActivityDao;
import com.ryanshiun.seniorscare.activity.dto.ActivityQueryParams;
import com.ryanshiun.seniorscare.activity.model.Activity;
import com.ryanshiun.seniorscare.activity.rowmapper.ActivityRowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ActivityDaoImpl implements ActivityDao {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    // 查詢單筆活動 by ID
    @Override
    public Activity getActivityById(Integer id) {
        String sql = "SELECT * FROM Activity WHERE id = :id";

        Map<String, Object> map = new HashMap<>();
        map.put("id", id);

        List<Activity> list = jdbcTemplate.query(sql, map, new ActivityRowMapper());
        return list.isEmpty() ? null : list.get(0);
    }

    // 查詢全部活動
    @Override
    public List<Activity> getAllActivities() {
        String sql = "SELECT * FROM Activity";
        return jdbcTemplate.query(sql, new ActivityRowMapper());
    }

    // 依條件查詢活動+模糊查詢
    @Override
    public List<Activity> getActivities(ActivityQueryParams activityparams) {
        StringBuilder sql = new StringBuilder("SELECT * FROM Activity WHERE 1=1");
        Map<String, Object> map = new HashMap<>();

        if (activityparams.getName() != null && !activityparams.getName().isEmpty()) {
            sql.append(" AND name LIKE :name");
            map.put("name", "%" + activityparams.getName() + "%");  // 模糊查詢
        }

        if (activityparams.getCategory() != null) {
            sql.append(" AND category = :category");
            map.put("category", activityparams.getCategory());
        }

        if (activityparams.getDate() != null) {
            sql.append(" AND date = :date");
            map.put("date", activityparams.getDate());
        }

        if (activityparams.getInstructor() != null) {
            sql.append(" AND instructor = :instructor");
            map.put("instructor", activityparams.getInstructor());
        }

        if (activityparams.getLocation() != null) {
            sql.append(" AND location = :location");
            map.put("location", activityparams.getLocation());
        }

        if (activityparams.getStatus() != null) {
            sql.append(" AND status = :status");
            map.put("status", activityparams.getStatus());
        }

        return jdbcTemplate.query(sql.toString(), map, new ActivityRowMapper());
    }


    // 新增活動
    @Override
    public void addActivity(Activity activity) {
    	String sql = "INSERT INTO Activity(" +
                "name, category, limit, [current], date, [end], time, " +
                "registration_start, registration_end, location, " +
                "latitude, longitude, instructor, status, description, image) " +
                "VALUES(:name, :category, :limit, :current, :date, :end, :time, " +
                ":registrationStart, :registrationEnd, :location, " +
                ":latitude, :longitude, :instructor, :status, :description, :image)";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("name", activity.getName());
        params.addValue("category", activity.getCategory());
        params.addValue("limit", activity.getLimit());
        params.addValue("current", activity.getCurrent());
        params.addValue("date", activity.getDate());
        params.addValue("end", activity.getEnd());
        params.addValue("time", activity.getTime());
        params.addValue("registrationStart", activity.getRegistrationStart());
        params.addValue("registrationEnd", activity.getRegistrationEnd());
        params.addValue("location", activity.getLocation());
        params.addValue("latitude", activity.getLatitude());
        params.addValue("longitude", activity.getLongitude());
        params.addValue("instructor", activity.getInstructor());
        params.addValue("status", activity.getStatus());
        params.addValue("description", activity.getDescription());
        params.addValue("image", activity.getImage());

        jdbcTemplate.update(sql, params);
    }

    // 修改活動
    @Override
    public void updateActivity(Integer id, Activity activity) {
        String sql = "UPDATE Activity SET " +
                "name = :name, " +
                "category = :category, " +
                "limit = :limit, " +
                "[current] = :current, " +
                "date = :date, " +
                "[end] = :end, " +
                "time = :time, " +
                "registration_start = :registrationStart, " +
                "registration_end = :registrationEnd, " +
                "location = :location, " +
                "latitude = :latitude, " +
                "longitude = :longitude, " +
                "instructor = :instructor, " +
                "status = :status, " +
                "description = :description, " +
                "image = :image " +
                "WHERE id = :id";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", id);
        params.addValue("name", activity.getName());
        params.addValue("category", activity.getCategory());
        params.addValue("limit", activity.getLimit());
        params.addValue("current", activity.getCurrent());
        params.addValue("date", activity.getDate());
        params.addValue("end", activity.getEnd());
        params.addValue("time", activity.getTime());
        params.addValue("registrationStart", activity.getRegistrationStart());
        params.addValue("registrationEnd", activity.getRegistrationEnd());
        params.addValue("location", activity.getLocation());
        params.addValue("latitude", activity.getLatitude());
        params.addValue("longitude", activity.getLongitude());
        params.addValue("instructor", activity.getInstructor());
        params.addValue("status", activity.getStatus());
        params.addValue("description", activity.getDescription());
        params.addValue("image", activity.getImage());

        jdbcTemplate.update(sql, params);
    }

    // 刪除活動
    @Override
    public void deleteActivityById(Integer id) {
        String sql = "DELETE FROM Activity WHERE id = :id";

        Map<String, Object> map = new HashMap<>();
        map.put("id", id);

        jdbcTemplate.update(sql, map);
    }

    @Override
    public int holdSeatsForRegistration(Integer activityId, int num, java.time.LocalDate today) {
        String sql = """
        UPDATE activity
        SET [current] = [current] + :num
        WHERE id = :aid
          AND status = 1
          AND :today BETWEEN registration_start AND registration_end
          AND [current] + :num <= [limit]
        """;
        var p = new org.springframework.jdbc.core.namedparam.MapSqlParameterSource()
                .addValue("aid", activityId)
                .addValue("num", num)
                .addValue("today", java.sql.Date.valueOf(today));
        return jdbcTemplate.update(sql, p);
    }

    @Override
    public int adjustSeats(Integer activityId, int delta, java.time.LocalDate today) {
        String sql = """
        UPDATE activity
        SET [current] = [current] + :delta
        WHERE id = :aid
          AND [current] + :delta BETWEEN 0 AND [limit]
          AND ( :delta <= 0
                OR (status = 1 AND :today BETWEEN registration_start AND registration_end) )
        """;
        var p = new org.springframework.jdbc.core.namedparam.MapSqlParameterSource()
                .addValue("aid", activityId)
                .addValue("delta", delta)
                .addValue("today", java.sql.Date.valueOf(today));
        return jdbcTemplate.update(sql, p);
    }

    @Override
    public void endRegistration(Integer id) {
        final String sql = "UPDATE activity SET status = 0 WHERE id = :id";
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        jdbcTemplate.update(sql, map);
    }

}
