package com.ryanshiun.seniorscare.activity.dao.Impl;

import com.ryanshiun.seniorscare.activity.dao.ActivityReservationDao;
import com.ryanshiun.seniorscare.activity.dto.ReservationCreateDto;
import com.ryanshiun.seniorscare.activity.dto.ReservationQueryDto;
import com.ryanshiun.seniorscare.activity.dto.ReservationResponse;
import com.ryanshiun.seniorscare.activity.dto.ReservationUpdateDto;
import com.ryanshiun.seniorscare.activity.model.ActivityRegistration;
import com.ryanshiun.seniorscare.activity.rowmapper.ActivityRegistrationMapper;
import com.ryanshiun.seniorscare.activity.rowmapper.ActivityReservationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ActivityReservationDaoImpl implements ActivityReservationDao {
    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    LocalDateTime now = LocalDateTime.now();


    /**
     * 透過會員ID取得該會員的所有活動預約
     * @return 該會員所有活動預約
     */
    @Override
    public List<ActivityRegistration> getMyReservations(Integer memberId) {
        final String sql = "SELECT * FROM activity_registration WHERE member_id = :memberId";
        Map<String,Object> map = new HashMap<>();
        map.put("memberId",memberId);
        return namedParameterJdbcTemplate.query(sql, map, new ActivityRegistrationMapper());
    }

    /**
     * 建立活動預約
     *
     * @param registration 預約請求物件
     * @return 預約是否成功
     */
    @Override
    public boolean createReservation(ReservationCreateDto registration) {
        final String sql = """
                INSERT INTO activity_registration (activity_id, member_id,num, scheduled_at, status)
                VALUES (:activityId, :memberId, :num, :scheduledAt, :status)
                """;
        final String status = "預約審核中";
        Map<String,Object> map = new HashMap<>();
        map.put("activityId", registration.getActivityId());
        map.put("memberId", registration.getMemberId());
        map.put("num", registration.getNum());
        map.put("scheduledAt", now);
        map.put("status", status);
        return namedParameterJdbcTemplate.update(sql, map) > 0;
    }

    /**
     * 取消活動預約
     * 將活動狀態修改為"已取消"
     * @param registrationId 預約ID
     * @param memberId       會員ID
     * @return 取消是否成功
     */
    @Override
    public boolean cancelReservation(Integer registrationId, Integer memberId) {
        final String sql = """
                UPDATE activity_registration
                SET status = :status
                WHERE id = :registrationId AND member_id = :memberId
                """;
        final String status = "已取消";
        Map<String,Object> map = new HashMap<>();
        map.put("registrationId", registrationId);
        map.put("memberId", memberId);
        map.put("status", status);
        return namedParameterJdbcTemplate.update(sql, map) > 0;
    }

    /**
     * 審核活動預約
     * 將活動狀態修改為"已通過"或"已拒絕"
     *
     * @param reservationUpdateDto@return 審核是否成功
     */
    @Override
    public boolean reviewReservation(ReservationUpdateDto reservationUpdateDto) {
        final String sql = """
                UPDATE activity_registration
                SET status = :status
                WHERE activity_id = :registrationId AND member_id = :memberId
                """;
        Map<String ,Object> map = new HashMap<>();
        map.put("registrationId", reservationUpdateDto.getActivityId());
        map.put("memberId", reservationUpdateDto.getMemberId());
        map.put("status", reservationUpdateDto.getStatus());
        return namedParameterJdbcTemplate.update(sql, map) > 0;
    }

    /**
     * 根據查詢條件尋找活動報名
     *
     * @return 所有活動預約
     */
    @Override
    public List<ReservationResponse> getAllReservations(ReservationQueryDto queryDto) {
        StringBuilder sql = new StringBuilder("""
        SELECT
            r.activity_id                           AS activityId,        -- 活動ID
            r.member_id                             AS memberId,          -- 會員ID
            COALESCE(a.name,       N'未知活動')    AS activityName,      -- 活動資訊：名稱
            COALESCE(a.category,   N'未分類')      AS activityCategory,  -- 活動類型
            COALESCE(m.member_name,N'未知會員')    AS memberName,        -- 會員資訊：姓名
            COALESCE(m.main_phone, N'N/A')         AS memberPhone,       -- 會員資訊：電話
            r.num                                   AS people,            -- 報名人數
            r.scheduled_at                          AS scheduledAt,       -- 報名時間
            r.status                                AS status             -- 狀態
        FROM activity_registration r
        LEFT JOIN activity a ON a.id = r.activity_id
        LEFT JOIN member  m ON m.member_id = r.member_id
        WHERE 1 = 1
        """);

        Map<String, Object> params = new HashMap<>();

        if (queryDto.getMemberId() != null) {
            sql.append(" AND r.member_id = :memberId");
            params.put("memberId", queryDto.getMemberId());
        }

        if (StringUtils.hasText(queryDto.getActivityName())) {
            sql.append(" AND a.name LIKE N'%' + :activityName + '%'");
            params.put("activityName", queryDto.getActivityName().trim());
        }

        if (StringUtils.hasText(queryDto.getCategoryName())) {
            sql.append(" AND a.category = :category");
            params.put("category", queryDto.getCategoryName().trim());
        }

        sql.append(" ORDER BY r.scheduled_at DESC, r.id DESC");

        return namedParameterJdbcTemplate.query(sql.toString(), params, new ActivityReservationResponse());
    }
}




