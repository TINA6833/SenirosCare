package com.ryanshiun.seniorscare.roomType.dao.Impl;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.ryanshiun.seniorscare.roomType.dao.ReservationDao;
import com.ryanshiun.seniorscare.roomType.dto.ReservationDetail;
import com.ryanshiun.seniorscare.roomType.dto.ReservationStats;
import com.ryanshiun.seniorscare.roomType.dto.RoomTypeRanking;
import com.ryanshiun.seniorscare.roomType.model.Reservation;

@Repository
public class ReservationDaoImpl implements ReservationDao {

    private final NamedParameterJdbcTemplate jdbc;

    public ReservationDaoImpl(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final RowMapper<Reservation> ROW_MAPPER = new RowMapper<>() {
        @Override
        public Reservation mapRow(ResultSet rs, int rowNum) throws SQLException {
            Reservation r = new Reservation();
            r.setReservationId(rs.getInt("reservation_id"));
            int mId = rs.getInt("member_id");
            r.setMemberId(rs.wasNull() ? null : mId);
            r.setApplicantName(rs.getString("applicant_name"));
            r.setMainPhone(rs.getString("main_phone"));
            int rtId = rs.getInt("roomtype_id");
            r.setRoomTypeId(rs.wasNull() ? null : rtId);
            r.setPartySize(rs.getInt("party_size"));
            r.setPreferredDate(rs.getDate("preferred_date").toLocalDate());
            r.setTimeFrom(rs.getTime("time_from").toLocalTime());
            r.setTimeTo(rs.getTime("time_to").toLocalTime());
            r.setStatus(rs.getInt("status"));
            r.setNote(rs.getString("note"));
            r.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            r.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
            return r;
        }
    };

    // === CRUD ===
    @Override
    public int addReservation(Reservation r) {
        String sql = """
            INSERT INTO reservation
            (member_id, applicant_name, main_phone, roomtype_id, party_size,
             preferred_date, time_from, time_to, status, note)
            VALUES (:member_id, :applicant_name, :main_phone, :roomtype_id, :party_size,
                    :preferred_date, :time_from, :time_to, :status, :note)
            """;
        MapSqlParameterSource p = new MapSqlParameterSource()
                .addValue("member_id", r.getMemberId())
                .addValue("applicant_name", r.getApplicantName())
                .addValue("main_phone", r.getMainPhone())
                .addValue("roomtype_id", r.getRoomTypeId())
                .addValue("party_size", r.getPartySize())
                .addValue("preferred_date", r.getPreferredDate())
                .addValue("time_from", r.getTimeFrom())
                .addValue("time_to", r.getTimeTo())
                .addValue("status", r.getStatus())
                .addValue("note", r.getNote());

        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(sql, p, kh, new String[]{"reservation_id"});
        Number key = kh.getKey();
        return key == null ? 0 : key.intValue();
    }

    @Override
    public boolean updateReservation(Reservation r) {
        String sql = """
            UPDATE reservation
               SET member_id = :member_id,
                   applicant_name = :applicant_name,
                   main_phone = :main_phone,
                   roomtype_id = :roomtype_id,
                   party_size = :party_size,
                   preferred_date = :preferred_date,
                   time_from = :time_from,
                   time_to = :time_to,
                   status = :status,
                   note = :note,
                   updated_at = SYSUTCDATETIME()
             WHERE reservation_id = :reservation_id
            """;
        MapSqlParameterSource p = new MapSqlParameterSource()
                .addValue("member_id", r.getMemberId())
                .addValue("applicant_name", r.getApplicantName())
                .addValue("main_phone", r.getMainPhone())
                .addValue("roomtype_id", r.getRoomTypeId())
                .addValue("party_size", r.getPartySize())
                .addValue("preferred_date", r.getPreferredDate())
                .addValue("time_from", r.getTimeFrom())
                .addValue("time_to", r.getTimeTo())
                .addValue("status", r.getStatus())
                .addValue("note", r.getNote())
                .addValue("reservation_id", r.getReservationId());
        return jdbc.update(sql, p) > 0;
    }

    @Override
    public boolean deleteReservation(int reservationId) {
        String sql = "DELETE FROM reservation WHERE reservation_id = :id";
        return jdbc.update(sql, Collections.singletonMap("id", reservationId)) > 0;
    }

    @Override
    public Reservation getReservationById(int reservationId) {
        String sql = "SELECT * FROM reservation WHERE reservation_id = :id";
        try {
            return jdbc.queryForObject(sql, Collections.singletonMap("id", reservationId), ROW_MAPPER);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public List<Reservation> getAllReservations() {
        String sql = "SELECT * FROM reservation ORDER BY created_at DESC";
        return jdbc.query(sql, ROW_MAPPER);
    }

    // === 條件查詢 ===
    @Override
    public List<Reservation> getByMember(Integer memberId) {
        String sql = "SELECT * FROM reservation WHERE member_id " +
                (memberId == null ? "IS NULL" : "= :member_id") +
                " ORDER BY created_at DESC";
        if (memberId == null) {
            return jdbc.query(sql, ROW_MAPPER);
        }
        return jdbc.query(sql, Map.of("member_id", memberId), ROW_MAPPER);
    }

    @Override
    public List<Reservation> getByRoomType(Integer roomTypeId) {
        String sql = "SELECT * FROM reservation WHERE roomtype_id " +
                (roomTypeId == null ? "IS NULL" : "= :roomtype_id") +
                " ORDER BY created_at DESC";
        if (roomTypeId == null) {
            return jdbc.query(sql, ROW_MAPPER);
        }
        return jdbc.query(sql, Map.of("roomtype_id", roomTypeId), ROW_MAPPER);
    }

    @Override
    public List<Reservation> getByPreferredDate(LocalDate date) {
        String sql = "SELECT * FROM reservation WHERE preferred_date = :d ORDER BY time_from ASC";
        return jdbc.query(sql, Map.of("d", date), ROW_MAPPER);
    }

    @Override
    public List<Reservation> getByDateRange(LocalDate from, LocalDate to) {
        String sql = """
            SELECT * FROM reservation
             WHERE preferred_date BETWEEN :from AND :to
             ORDER BY preferred_date, time_from
            """;
        return jdbc.query(sql, Map.of("from", from, "to", to), ROW_MAPPER);
    }

    @Override
    public List<Reservation> getByStatus(int status) {
        String sql = "SELECT * FROM reservation WHERE status = :s ORDER BY created_at DESC";
        return jdbc.query(sql, Map.of("s", status), ROW_MAPPER);
    }

    // === 統計 ===
    @Override
    public long countOnDate(LocalDate date) {
        String sql = "SELECT COUNT(1) FROM reservation WHERE preferred_date = :d";
        Long cnt = jdbc.queryForObject(sql, Map.of("d", date), Long.class);
        return cnt == null ? 0 : cnt;
    }

    @Override
    public long countByRoomType(Integer roomTypeId) {
        String sql = "SELECT COUNT(1) FROM reservation WHERE roomtype_id " +
                (roomTypeId == null ? "IS NULL" : "= :roomtype_id");
        Long cnt = roomTypeId == null
                ? jdbc.queryForObject(sql, new MapSqlParameterSource(), Long.class)
                : jdbc.queryForObject(sql, Map.of("roomtype_id", roomTypeId), Long.class);
        return cnt == null ? 0 : cnt;
    }

    // === 趨勢（用 preferred_date） ===
    @Override
    public List<ReservationStats> getDailyTrend(int days) {
        String sql = """
            SELECT CAST(preferred_date AS DATE) AS day, COUNT(*) AS cnt
              FROM reservation
             WHERE preferred_date >= DATEADD(DAY, -:days, CAST(GETUTCDATE() AS DATE))
             GROUP BY CAST(preferred_date AS DATE)
             ORDER BY day
            """;
        return jdbc.query(sql, Map.of("days", days),
                (rs, i) -> new ReservationStats(rs.getDate("day").toLocalDate(), rs.getLong("cnt")));
    }

    @Override
    public List<ReservationStats> getMonthlyTrend(int months) {
        String sql = """
            SELECT CAST(DATEFROMPARTS(YEAR(preferred_date), MONTH(preferred_date), 1) AS DATE) AS month_start,
                   COUNT(*) AS cnt
              FROM reservation
             WHERE preferred_date >= DATEADD(MONTH, -:months, CAST(GETUTCDATE() AS DATE))
             GROUP BY DATEFROMPARTS(YEAR(preferred_date), MONTH(preferred_date), 1)
             ORDER BY month_start
            """;
        return jdbc.query(sql, Map.of("months", months),
                (rs, i) -> new ReservationStats(rs.getDate("month_start").toLocalDate(), rs.getLong("cnt")));
    }

    @Override
    public List<RoomTypeRanking> getRoomTypeRanking(int topN) {
        String sql = """
            SELECT r.roomtype_id, rt.name AS roomtype_name, COUNT(*) AS cnt
              FROM reservation r
              JOIN roomtype rt ON r.roomtype_id = rt.id
             WHERE r.roomtype_id IS NOT NULL
             GROUP BY r.roomtype_id, rt.name
             ORDER BY cnt DESC
             OFFSET 0 ROWS FETCH NEXT :topN ROWS ONLY
            """;
        return jdbc.query(sql, Map.of("topN", topN),
                (rs, i) -> new RoomTypeRanking(
                        rs.getInt("roomtype_id"),
                        rs.getString("roomtype_name"),
                        rs.getLong("cnt")
                ));
    }
    
    private static final RowMapper<ReservationDetail> DETAIL_MAPPER = (rs, i) -> {
        ReservationDetail d = new ReservationDetail();
        d.setReservationId(rs.getInt("reservation_id"));
        d.setMemberId((Integer) rs.getObject("member_id"));
        d.setMemberName(rs.getString("member_name"));
        d.setApplicantName(rs.getString("applicant_name"));
        d.setMainPhone(rs.getString("main_phone"));
        d.setRoomTypeId((Integer) rs.getObject("roomtype_id"));
        d.setRoomTypeName(rs.getString("roomtype_name"));
        d.setPartySize(rs.getInt("party_size"));
        d.setPreferredDate(rs.getDate("preferred_date").toLocalDate());
        d.setTimeFrom(rs.getTime("time_from").toLocalTime());
        d.setTimeTo(rs.getTime("time_to").toLocalTime());
        d.setStatus(rs.getInt("status"));
        d.setNote(rs.getString("note"));
        java.sql.Timestamp cts = rs.getTimestamp("created_at");
        java.sql.Timestamp uts = rs.getTimestamp("updated_at");
        d.setCreatedAt(cts == null ? null : cts.toLocalDateTime());
        d.setUpdatedAt(uts == null ? null : uts.toLocalDateTime());
        return d;
    };

 // 建議放在 DAO 類別的欄位（改成你資料表真正的欄位名）
    private static final String MEMBER_NAME_COL   = "member_name"; // 例如 member 表叫 member_name
    private static final String ROOMTYPE_NAME_COL = "name";        // 若 roomtype 真的叫 name 就保留；否則改成 roomtype_name

    public ReservationDetail getDetailById(int reservationId) {
        String sql = ("""
            SELECT
                r.reservation_id, r.member_id,
                m.%s AS member_name,
                r.applicant_name, r.main_phone,
                r.roomtype_id,
                rt.%s AS roomtype_name,
                r.party_size, r.preferred_date, r.time_from, r.time_to,
                r.status, r.note, r.created_at, r.updated_at
            FROM dbo.reservation r
            LEFT JOIN dbo.member   m  ON m.member_id = r.member_id
            LEFT JOIN dbo.roomtype rt ON rt.id       = r.roomtype_id
            WHERE r.reservation_id = :id
            """).formatted(MEMBER_NAME_COL, ROOMTYPE_NAME_COL);

        Map<String, Object> params = Map.of("id", reservationId);
        List<ReservationDetail> list = jdbc.query(sql, params, DETAIL_MAPPER);
        return list.isEmpty() ? null : list.get(0);
    }
    
    private static final RowMapper<ReservationDetail> DETAIL_MAPPER1 = (rs, i) -> {
        ReservationDetail d = new ReservationDetail();
        d.setReservationId(rs.getInt("reservation_id"));
        d.setMemberId((Integer) rs.getObject("member_id"));
        d.setApplicantName(rs.getString("applicant_name"));
        d.setMainPhone(rs.getString("main_phone"));
        d.setRoomTypeId((Integer) rs.getObject("roomtype_id"));
        d.setPartySize(rs.getInt("party_size"));
        var pd = rs.getDate("preferred_date");   d.setPreferredDate(pd == null ? null : pd.toLocalDate());
        var tf = rs.getTime("time_from");        d.setTimeFrom(tf == null ? null : tf.toLocalTime());
        var tt = rs.getTime("time_to");          d.setTimeTo(tt == null ? null : tt.toLocalTime());
        d.setStatus(rs.getInt("status"));
        d.setNote(rs.getString("note"));
        var ca = rs.getTimestamp("created_at");  d.setCreatedAt(ca == null ? null : ca.toLocalDateTime());
        var ua = rs.getTimestamp("updated_at");  d.setUpdatedAt(ua == null ? null : ua.toLocalDateTime());
        d.setMemberName(rs.getString("member_name"));     // 讀別名
        d.setRoomTypeName(rs.getString("roomtype_name")); // 讀別名
        return d;
    };

}