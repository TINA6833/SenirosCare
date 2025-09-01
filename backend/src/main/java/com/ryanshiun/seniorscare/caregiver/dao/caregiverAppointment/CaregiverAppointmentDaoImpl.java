package com.ryanshiun.seniorscare.caregiver.dao.caregiverAppointment;

import com.ryanshiun.seniorscare.caregiver.dao.caregiverAppointment.CaregiverAppointmentDao;
import com.ryanshiun.seniorscare.caregiver.dto.caregiverAppointment.AppointmentRatingDto;
import com.ryanshiun.seniorscare.caregiver.dto.caregiverAppointment.CaregiverAppointmentCreateDto;
import com.ryanshiun.seniorscare.caregiver.dto.caregiverAppointment.CaregiverAppointmentQueryDto;
import com.ryanshiun.seniorscare.caregiver.dto.caregiverAppointment.CaregiverAppointmentUpdateDto;
import com.ryanshiun.seniorscare.caregiver.model.CaregiverAppointment;
import com.ryanshiun.seniorscare.caregiver.rowmapper.CaregiverAppointmentRowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class CaregiverAppointmentDaoImpl implements CaregiverAppointmentDao {

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    private CaregiverAppointmentRowMapper appointmentRowMapper;

  @Override
  public Integer createAppointment(CaregiverAppointmentCreateDto createDto) {
    String sql = """
        INSERT INTO caregiver_appointment (
            member_id, caregiver_id, scheduled_at, end_time, status, is_blocked,
            service_type_id, service_location, total_amount, notes, block_type,
            created_at
        ) VALUES (
            :memberId, :caregiverId, :scheduledAt, :endTime, :status, :isBlocked,
            :serviceTypeId, :serviceLocation, :totalAmount, :notes, :blockType,
            SYSDATETIME()
        )
        """;

    Map<String, Object> params = new HashMap<>();
    params.put("memberId", createDto.getMemberId());
    params.put("caregiverId", createDto.getCaregiverId());
    params.put("scheduledAt", createDto.getScheduledAt());
    params.put("endTime", createDto.getEndTime());
    params.put("status", createDto.getStatus());
    params.put("isBlocked", createDto.getIsBlocked());
    params.put("serviceTypeId", createDto.getServiceTypeId());
    params.put("serviceLocation", createDto.getServiceLocation());
    params.put("totalAmount", createDto.getTotalAmount());
    params.put("notes", createDto.getNotes());
    params.put("blockType", createDto.getBlockType());

    KeyHolder keyHolder = new GeneratedKeyHolder();
    namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource(params), keyHolder);
    return keyHolder.getKey().intValue();
  }


    @Override
    public CaregiverAppointment getAppointmentById(Integer id) {
        String sql = "SELECT * FROM caregiver_appointment WHERE id = :id";

        Map<String, Object> params = new HashMap<>();
        params.put("id", id);

        List<CaregiverAppointment> results = namedParameterJdbcTemplate.query(sql, params, appointmentRowMapper);
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public List<CaregiverAppointment> getAppointments(CaregiverAppointmentQueryDto queryDto) {
        StringBuilder sql = new StringBuilder("SELECT * FROM caregiver_appointment WHERE 1=1");
        Map<String, Object> params = new HashMap<>();

        buildWhereClause(sql, params, queryDto);

        sql.append(" ORDER BY created_at DESC");

        if (queryDto.getLimit() != null && queryDto.getLimit() > 0) {
            sql.append(" OFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY");
            params.put("limit", queryDto.getLimit());
            params.put("offset", queryDto.getOffset() != null ? queryDto.getOffset() : 0);
        }

        return namedParameterJdbcTemplate.query(sql.toString(), params, appointmentRowMapper);
    }

    @Override
    public Integer getAppointmentCount(CaregiverAppointmentQueryDto queryDto) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM caregiver_appointment WHERE 1=1");
        Map<String, Object> params = new HashMap<>();

        buildWhereClause(sql, params, queryDto);

        return namedParameterJdbcTemplate.queryForObject(sql.toString(), params, Integer.class);
    }

    @Override
    public Integer updateAppointment(Integer id, CaregiverAppointmentUpdateDto updateDto) {
        StringBuilder sql = new StringBuilder("UPDATE caregiver_appointment SET ");
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);

        boolean hasUpdate = false;

        if (updateDto.getScheduledAt() != null) {
            sql.append("scheduled_at = :scheduledAt, ");
            params.put("scheduledAt", updateDto.getScheduledAt());
            hasUpdate = true;
        }

        if (updateDto.getEndTime() != null) {
            sql.append("end_time = :endTime, ");
            params.put("endTime", updateDto.getEndTime());
            hasUpdate = true;
        }

        if (updateDto.getStatus() != null) {
            sql.append("status = :status, ");
            params.put("status", updateDto.getStatus());
            hasUpdate = true;
        }

        if (updateDto.getServiceTypeId() != null) {
            sql.append("service_type_id = :serviceTypeId, ");
            params.put("serviceTypeId", updateDto.getServiceTypeId());
            hasUpdate = true;
        }

        if (updateDto.getServiceLocation() != null) {
            sql.append("service_location = :serviceLocation, ");
            params.put("serviceLocation", updateDto.getServiceLocation());
            hasUpdate = true;
        }

        if (updateDto.getTotalAmount() != null) {
            sql.append("total_amount = :totalAmount, ");
            params.put("totalAmount", updateDto.getTotalAmount());
            hasUpdate = true;
        }

        if (updateDto.getNotes() != null) {
            sql.append("notes = :notes, ");
            params.put("notes", updateDto.getNotes());
            hasUpdate = true;
        }

        if (updateDto.getBlockType() != null) {
            sql.append("block_type = :blockType, ");
            params.put("blockType", updateDto.getBlockType());
            hasUpdate = true;
        }

        if (!hasUpdate) {
            return 0;
        }

        // 移除最後的逗號和空格，添加 WHERE 子句
        sql.setLength(sql.length() - 2);
        sql.append(" WHERE id = :id");

        return namedParameterJdbcTemplate.update(sql.toString(), params);
    }

    @Override
    public Integer cancelAppointment(Integer id) {
        String sql = """
            UPDATE caregiver_appointment 
            SET status = 'cancelled', cancelled_at = SYSDATETIME() 
            WHERE id = :id
            """;

        Map<String, Object> params = new HashMap<>();
        params.put("id", id);

        return namedParameterJdbcTemplate.update(sql, params);
    }

    @Override
    public Integer addRating(Integer id, AppointmentRatingDto ratingDto) {
        String sql = """
            UPDATE caregiver_appointment 
            SET rating_score = :ratingScore, rating_comment = :ratingComment, 
                rated_at = SYSDATETIME(), is_rated = 1
            WHERE id = :id AND status = 'completed'
            """;

        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("ratingScore", ratingDto.getRatingScore());
        params.put("ratingComment", ratingDto.getRatingComment());

        return namedParameterJdbcTemplate.update(sql, params);
    }

    @Override
    public List<CaregiverAppointment> checkTimeConflict(Integer caregiverId, LocalDateTime startTime, LocalDateTime endTime, Integer excludeId) {
        StringBuilder sql = new StringBuilder("""
            SELECT * FROM caregiver_appointment 
            WHERE caregiver_id = :caregiverId 
            AND status != 'cancelled'
            AND (
                (scheduled_at <= :startTime AND end_time > :startTime) OR
                (scheduled_at < :endTime AND end_time >= :endTime) OR
                (scheduled_at >= :startTime AND end_time <= :endTime)
            )
            """);

        Map<String, Object> params = new HashMap<>();
        params.put("caregiverId", caregiverId);
        params.put("startTime", startTime);
        params.put("endTime", endTime);

        if (excludeId != null) {
            sql.append(" AND id != :excludeId");
            params.put("excludeId", excludeId);
        }

        return namedParameterJdbcTemplate.query(sql.toString(), params, appointmentRowMapper);
    }

    @Override
    public List<CaregiverAppointment> getCaregiverAppointments(Integer caregiverId, LocalDateTime startDate, LocalDateTime endDate) {
        String sql = """
            SELECT * FROM caregiver_appointment 
            WHERE caregiver_id = :caregiverId
            AND scheduled_at >= :startDate 
            AND scheduled_at <= :endDate
            ORDER BY scheduled_at ASC
            """;

        Map<String, Object> params = new HashMap<>();
        params.put("caregiverId", caregiverId);
        params.put("startDate", startDate);
        params.put("endDate", endDate);

        return namedParameterJdbcTemplate.query(sql, params, appointmentRowMapper);
    }

    @Override
    public List<CaregiverAppointment> getMemberAppointments(Integer memberId, String status) {
        StringBuilder sql = new StringBuilder("""
            SELECT * FROM caregiver_appointment 
            WHERE member_id = :memberId
            """);

        Map<String, Object> params = new HashMap<>();
        params.put("memberId", memberId);

        if (status != null) {
            sql.append(" AND status = :status");
            params.put("status", status);
        }

        sql.append(" ORDER BY scheduled_at DESC");

        return namedParameterJdbcTemplate.query(sql.toString(), params, appointmentRowMapper);
    }

    @Override
    public List<CaregiverAppointment> getUnratedCompletedAppointments(Integer memberId) {
        String sql = """
            SELECT * FROM caregiver_appointment 
            WHERE member_id = :memberId
            AND status = 'completed'
            AND is_rated = 0
            ORDER BY scheduled_at DESC
            """;

        Map<String, Object> params = new HashMap<>();
        params.put("memberId", memberId);

        return namedParameterJdbcTemplate.query(sql, params, appointmentRowMapper);
    }

    @Override
    public Integer updateAppointmentStatus(Integer id, String status) {
        String sql = "UPDATE caregiver_appointment SET status = :status WHERE id = :id";

        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("status", status);

        return namedParameterJdbcTemplate.update(sql, params);
    }

    // 私有方法：建立 WHERE 條件
    private void buildWhereClause(StringBuilder sql, Map<String, Object> params, CaregiverAppointmentQueryDto queryDto) {
      if (queryDto.getMemberId() != null) {
        sql.append(" AND member_id = :memberId");
        params.put("memberId", queryDto.getMemberId());
      }

      if (queryDto.getCaregiverId() != null) {
        sql.append(" AND caregiver_id = :caregiverId");
        params.put("caregiverId", queryDto.getCaregiverId());
      }

      if (queryDto.getStatus() != null) {
        sql.append(" AND status = :status");
        params.put("status", queryDto.getStatus());
      }

      if (queryDto.getIsBlocked() != null) {
        sql.append(" AND is_blocked = :isBlocked");
        params.put("isBlocked", queryDto.getIsBlocked());
      }

      if (queryDto.getStartDate() != null) {
        sql.append(" AND scheduled_at >= :startDate");
        params.put("startDate", queryDto.getStartDate());
      }

      if (queryDto.getEndDate() != null) {
        sql.append(" AND scheduled_at <= :endDate");
        params.put("endDate", queryDto.getEndDate());
      }

      if (queryDto.getIsRated() != null) {
        sql.append(" AND is_rated = :isRated");
        params.put("isRated", queryDto.getIsRated());
      }
    }
  @Override
  public Integer getCountByStatus(String status) {
    String sql;
    Map<String, Object> params = new HashMap<>();

    if (status == null) {
      sql = "SELECT COUNT(*) FROM caregiver_appointment";
    } else {
      sql = "SELECT COUNT(*) FROM caregiver_appointment WHERE status = :status";
      params.put("status", status);
    }

    Integer count = namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
    return count != null ? count : 0;
  }

  @Override
  public Integer getCountByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
    String sql = """
            SELECT COUNT(*) FROM caregiver_appointment 
            WHERE created_at >= :startDate AND created_at < :endDate
            """;

    Map<String, Object> params = new HashMap<>();
    params.put("startDate", startDate);
    params.put("endDate", endDate);

    Integer count = namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
    return count != null ? count : 0;
  }

  @Override
  public List<CaregiverAppointment> checkTimeConflictForConfirmedAppointments(
      Integer caregiverId, LocalDateTime startTime, LocalDateTime endTime, Integer excludeId) {

    StringBuilder sql = new StringBuilder("""
        SELECT * FROM caregiver_appointment 
        WHERE caregiver_id = :caregiverId 
        AND status IN ('approved', 'completed') 
        AND (
            (scheduled_at <= :startTime AND end_time > :startTime) OR
            (scheduled_at < :endTime AND end_time >= :endTime) OR
            (scheduled_at >= :startTime AND end_time <= :endTime)
        )
        """);

    Map<String, Object> params = new HashMap<>();
    params.put("caregiverId", caregiverId);
    params.put("startTime", startTime);
    params.put("endTime", endTime);

    if (excludeId != null) {
      sql.append(" AND id != :excludeId");
      params.put("excludeId", excludeId);
    }

    return namedParameterJdbcTemplate.query(sql.toString(), params, appointmentRowMapper);
  }
}