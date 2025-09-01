package com.ryanshiun.seniorscare.caregiver.dao.caregiver;

import com.ryanshiun.seniorscare.caregiver.model.Caregiver;
import com.ryanshiun.seniorscare.caregiver.rowmapper.CaregiverRowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class CaregiverDAOImpl implements CaregiverDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private CaregiverRowMapper caregiverRowMapper;

    @Override
    public List<Caregiver> findAll() {
        String sql = "SELECT * FROM caregiver ORDER BY caregiver_id";
        return jdbcTemplate.query(sql, caregiverRowMapper);
    }

    @Override
    public Optional<Caregiver> findById(Integer id) {
        String sql = "SELECT * FROM caregiver WHERE caregiver_id = ?";
        try {
            Caregiver caregiver = jdbcTemplate.queryForObject(sql, caregiverRowMapper, id);
            return Optional.ofNullable(caregiver);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Caregiver> findByChineseNameContaining(String chineseName) {
        String sql = "SELECT * FROM caregiver WHERE chinese_name LIKE ? ORDER BY caregiver_id";
        return jdbcTemplate.query(sql, caregiverRowMapper, "%" + chineseName + "%");
    }

    @Override
    public Optional<Caregiver> findByPhone(String phone) {
        String sql = "SELECT * FROM caregiver WHERE phone = ?";
        try {
            Caregiver caregiver = jdbcTemplate.queryForObject(sql, caregiverRowMapper, phone);
            return Optional.ofNullable(caregiver);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Caregiver> findByIsActive(Boolean isActive) {
        String sql = "SELECT * FROM caregiver WHERE is_active = ? ORDER BY caregiver_id";
        return jdbcTemplate.query(sql, caregiverRowMapper, isActive);
    }

    @Override
    public Caregiver save(Caregiver caregiver) {
        String sql = "INSERT INTO caregiver (chinese_name, gender, phone, email, experience_years, " +
                "photo, address, service_area, average_rating, total_ratings, total_points, " +
                "is_active, created_at, updated_at, self_introduction) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        LocalDateTime now = LocalDateTime.now();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, caregiver.getChineseName());
            ps.setBoolean(2, caregiver.getGender());
            ps.setString(3, caregiver.getPhone());
            ps.setString(4, caregiver.getEmail());
            ps.setInt(5, caregiver.getExperienceYears());
            ps.setString(6, caregiver.getPhoto());
            ps.setString(7, caregiver.getAddress());
            ps.setString(8, caregiver.getServiceArea());
            ps.setBigDecimal(9, caregiver.getAverageRating());
            ps.setInt(10, caregiver.getTotalRatings());
            ps.setInt(11, caregiver.getTotalPoints());
            ps.setBoolean(12, caregiver.getIsActive());
            ps.setTimestamp(13, Timestamp.valueOf(now));
            ps.setTimestamp(14, Timestamp.valueOf(now));
          ps.setString(15, caregiver.getSelfIntroduction());
          return ps;
        }, keyHolder);

        caregiver.setCaregiverId(keyHolder.getKey().intValue());
        caregiver.setCreatedAt(now);
        caregiver.setUpdatedAt(now);

        return caregiver;
    }

    @Override
    public Caregiver update(Caregiver caregiver) {
        String sql = "UPDATE caregiver SET chinese_name = ?, gender = ?, phone = ?, email = ?, " +
                "experience_years = ?, photo = ?, address = ?, service_area = ?, " +
                "average_rating = ?, total_ratings = ?, total_points = ?, is_active = ?, " +
                "updated_at = ?, self_introduction = ? WHERE caregiver_id = ?";

        LocalDateTime now = LocalDateTime.now();

      jdbcTemplate.update(sql,
          caregiver.getChineseName(),          // 1
          caregiver.getGender(),               // 2
          caregiver.getPhone(),                // 3
          caregiver.getEmail(),                // 4
          caregiver.getExperienceYears(),      // 5
          caregiver.getPhoto(),                // 6
          caregiver.getAddress(),              // 7
          caregiver.getServiceArea(),          // 8
          caregiver.getAverageRating(),        // 9
          caregiver.getTotalRatings(),         // 10
          caregiver.getTotalPoints(),          // 11
          caregiver.getIsActive(),             // 12
          Timestamp.valueOf(now),              // 13 (updated_at)
          caregiver.getSelfIntroduction(),     // 14 (self_introduction)
          caregiver.getCaregiverId()           // 15 (WHERE 條件)
      );
        caregiver.setUpdatedAt(now);
        return caregiver;
    }

    @Override
    public boolean deleteById(Integer id) {
        String sql = "DELETE FROM caregiver WHERE caregiver_id = ?";
        int rowsAffected = jdbcTemplate.update(sql, id);
        return rowsAffected > 0;
    }

    @Override
    public boolean existsByPhoneAndCaregiverIdNot(String phone, Integer caregiverId) {
        String sql = "SELECT COUNT(*) FROM caregiver WHERE phone = ? AND caregiver_id != ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, phone, caregiverId);
        return count != null && count > 0;
    }

    @Override
    public List<Caregiver> findByServiceAreaContaining(String serviceArea) {
        String sql = "SELECT * FROM caregiver WHERE service_area LIKE ? ORDER BY caregiver_id";
        return jdbcTemplate.query(sql, caregiverRowMapper, "%" + serviceArea + "%");
    }

    @Override
    public List<Caregiver> findByAverageRatingBetween(Double minRating, Double maxRating) {
        String sql = "SELECT * FROM caregiver WHERE average_rating BETWEEN ? AND ? ORDER BY average_rating DESC";
        return jdbcTemplate.query(sql, caregiverRowMapper, minRating, maxRating);
    }
}