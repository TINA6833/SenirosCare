package com.ryanshiun.seniorscare.caregiver.dao;

import com.ryanshiun.seniorscare.caregiver.model.ServiceType;
import com.ryanshiun.seniorscare.caregiver.rowmapper.ServiceTypeRowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 服務類型資料存取實作類
 */
@Repository
public class ServiceTypeDAOImpl implements ServiceTypeDAO {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Autowired
  private ServiceTypeRowMapper serviceTypeRowMapper;

  @Override
  public List<ServiceType> findAllActive() {
    String sql = """
            SELECT service_type_id, service_name, hourly_rate, description, 
                   is_active, created_at, updated_at 
            FROM service_type 
            WHERE is_active = 1 
            ORDER BY service_type_id
            """;

    return jdbcTemplate.query(sql, serviceTypeRowMapper);
  }

  @Override
  public Optional<ServiceType> findById(Integer id) {
    String sql = """
            SELECT service_type_id, service_name, hourly_rate, description, 
                   is_active, created_at, updated_at 
            FROM service_type 
            WHERE service_type_id = ?
            """;

    try {
      ServiceType serviceType = jdbcTemplate.queryForObject(sql, serviceTypeRowMapper, id);
      return Optional.ofNullable(serviceType);
    } catch (EmptyResultDataAccessException e) {
      return Optional.empty();
    }
  }

  @Override
  public Optional<ServiceType> findByServiceName(String serviceName) {
    String sql = """
            SELECT service_type_id, service_name, hourly_rate, description, 
                   is_active, created_at, updated_at 
            FROM service_type 
            WHERE service_name = ?
            """;

    try {
      ServiceType serviceType = jdbcTemplate.queryForObject(sql, serviceTypeRowMapper, serviceName);
      return Optional.ofNullable(serviceType);
    } catch (EmptyResultDataAccessException e) {
      return Optional.empty();
    }
  }

  @Override
  public List<ServiceType> findAll() {
    String sql = """
            SELECT service_type_id, service_name, hourly_rate, description, 
                   is_active, created_at, updated_at 
            FROM service_type 
            ORDER BY service_type_id
            """;

    return jdbcTemplate.query(sql, serviceTypeRowMapper);
  }

  @Override
  public Integer getHourlyRateById(Integer serviceTypeId) {
    String sql = "SELECT hourly_rate FROM service_type WHERE service_type_id = ? AND is_active = 1";

    try {
      return jdbcTemplate.queryForObject(sql, Integer.class, serviceTypeId);
    } catch (EmptyResultDataAccessException e) {
      return null;
    }
  }
}