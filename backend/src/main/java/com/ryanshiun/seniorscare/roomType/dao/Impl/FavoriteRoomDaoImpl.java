package com.ryanshiun.seniorscare.roomType.dao.Impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import com.ryanshiun.seniorscare.roomType.dao.FavoriteRoomDao;
import com.ryanshiun.seniorscare.roomType.dto.FavoriteRoomView;
import com.ryanshiun.seniorscare.roomType.dto.FavoriteStats;

@Repository
public class FavoriteRoomDaoImpl implements FavoriteRoomDao {

    private final NamedParameterJdbcTemplate jdbc;

    public FavoriteRoomDaoImpl(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }
    


    @Override
    public int addFavorite(int memberId, int roomTypeId) {
        String sql = """
            INSERT INTO favorite_room (member_id, roomtype_id)
            VALUES (:member_id, :roomtype_id)
            """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("member_id", memberId)
                .addValue("roomtype_id", roomTypeId);
        try {
            return jdbc.update(sql, params); // 1 表新增成功
        } catch (DuplicateKeyException ignore) {
            return 0; // 已存在則忽略
        }
    }

    @Override
    public int[] batchAddFavorite(int memberId, List<Integer> roomTypeIds) {
        String sql = """
            INSERT INTO favorite_room (member_id, roomtype_id)
            VALUES (:member_id, :roomtype_id)
            """;
        SqlParameterSource[] batch = roomTypeIds.stream()
                .map(id -> new MapSqlParameterSource()
                        .addValue("member_id", memberId)
                        .addValue("roomtype_id", id))
                .toArray(SqlParameterSource[]::new);

        try {
            return jdbc.batchUpdate(sql, batch);
        } catch (DuplicateKeyException e) {
            // SQL Server 的 batch 對重複鍵會整批失敗；保守作法：逐筆插入、忽略重複
            return roomTypeIds.stream().mapToInt(id -> addFavorite(memberId, id)).toArray();
        }
    }

    @Override
    public boolean removeFavorite(int memberId, int roomTypeId) {
        String sql = """
            DELETE FROM favorite_room
             WHERE member_id = :member_id AND roomtype_id = :roomtype_id
            """;
        Map<String, Object> params = Map.of("member_id", memberId, "roomtype_id", roomTypeId);
        return jdbc.update(sql, params) > 0;
    }

    @Override
    public boolean isFavorited(int memberId, int roomTypeId) {
        String sql = """
            SELECT COUNT(1) FROM favorite_room
             WHERE member_id = :member_id AND roomtype_id = :roomtype_id
            """;
        Long cnt = jdbc.queryForObject(sql, Map.of("member_id", memberId, "roomtype_id", roomTypeId), Long.class);
        return cnt != null && cnt > 0;
    }

    @Override
    public List<Integer> findRoomTypeIdsByMember(int memberId) {
        String sql = """
            SELECT roomtype_id
              FROM favorite_room
             WHERE member_id = :member_id
             ORDER BY created_at DESC
            """;
        return jdbc.queryForList(sql, Collections.singletonMap("member_id", memberId), Integer.class);
    }

    @Override
    public long countByRoomType(int roomTypeId) {
        String sql = "SELECT COUNT(1) FROM favorite_room WHERE roomtype_id = :roomtype_id";
        Long cnt = jdbc.queryForObject(sql, Collections.singletonMap("roomtype_id", roomTypeId), Long.class);
        return cnt == null ? 0 : cnt;
    }

    @Override
    public long countByMember(int memberId) {
        String sql = "SELECT COUNT(1) FROM favorite_room WHERE member_id = :member_id";
        Long cnt = jdbc.queryForObject(sql, Collections.singletonMap("member_id", memberId), Long.class);
        return cnt == null ? 0 : cnt;
    }
    
    private static final RowMapper<FavoriteStats> STATS_MAPPER = (rs, i) ->
    new FavoriteStats(
        rs.getInt("roomTypeId"),
        rs.getString("roomTypeName"),
        rs.getLong("favorites")
    );

public List<FavoriteStats> findStats(String keyword, String order, int offset, int limit) {
    boolean hasKw = keyword != null && !keyword.isBlank();
    String sortOrder = "asc".equalsIgnoreCase(order) ? "ASC" : "DESC";

    StringBuilder sql = new StringBuilder();
    sql.append("""
        SELECT
            fr.roomtype_id AS roomTypeId,
            rt.name        AS roomTypeName,
            COUNT(*)       AS favorites
        FROM dbo.favorite_room fr
        JOIN dbo.roomtype rt ON rt.id = fr.roomtype_id
        """);
    if (hasKw) {
        sql.append(" WHERE rt.name LIKE :kw ");
    }
    // 這行不要用文字區塊拼接，直接用一般字串避免少空白
    sql.append(" GROUP BY fr.roomtype_id, rt.name ");
    sql.append(" ORDER BY favorites ").append(sortOrder);
    sql.append(" OFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY ");

    MapSqlParameterSource p = new MapSqlParameterSource()
        .addValue("offset", Math.max(offset, 0))
        .addValue("limit", Math.max(limit, 1));
    if (hasKw) {
        p.addValue("kw", "%" + keyword.trim() + "%");
    }

    return jdbc.query(sql.toString(), p, STATS_MAPPER);
}

    //計總筆數（給分頁用）
    @Override
    public int countStats(String keyword) {
    	boolean hasKw = keyword != null && !keyword.isBlank();

    	StringBuilder sql = new StringBuilder();
    	sql.append("""
    			SELECT COUNT(*) AS total
    			FROM (
    			SELECT fr.roomtype_id
    			FROM favorite_room fr
    			JOIN roomtype rt ON rt.id = fr.roomtype_id
    			""");
    	if (hasKw) sql.append(" WHERE rt.name LIKE :kw ");
    	sql.append("""
    			GROUP BY fr.roomtype_id
    			) t
    			""");

    	MapSqlParameterSource p = new MapSqlParameterSource();
    	if (hasKw) p.addValue("kw", "%" + keyword.trim() + "%");

    	return jdbc.queryForObject(sql.toString(), p, Integer.class);
    }

    //TopN（固定依收藏數 DESC；用 OFFSET/FETCH 取前 N 筆，避免 TOP 參數化的兼容問題）
    @Override
    public List<FavoriteStats> findTopFavorited(int limit, String keyword) {
        int safe = Math.max(1, Math.min(limit, 100)); // 1~100 限制
        String where = "";
        Map<String, Object> params = new HashMap<>();

        if (keyword != null && !keyword.isBlank()) {
            where = "WHERE rt.name LIKE '%' + :kw + '%'";
            params.put("kw", keyword.trim());
        }

        String sql = """
            SELECT TOP %d
                   rt.id         AS roomTypeId,
                   rt.name       AS roomTypeName,
                   COUNT(*)      AS favorites
            FROM dbo.favorite_room fr
            JOIN dbo.roomtype rt ON rt.id = fr.roomtype_id
            %s
            GROUP BY rt.id, rt.name
            ORDER BY favorites DESC, rt.id ASC
            """.formatted(safe, where);

        return jdbc.query(sql, params, STATS_MAPPER);
    }
    
    @Override
    public List<FavoriteRoomView> findFavoritesByMember(int memberId) {
        String sql = """
            SELECT rt.id, rt.name, rt.image_path, rt.price
            FROM favorite_room fr
            JOIN roomtype rt ON fr.roomtype_id = rt.id
            WHERE fr.member_id = :memberId
        """;

        Map<String, Object> params = Map.of("memberId", memberId);
        return jdbc.query(sql, params, (rs, rowNum) -> {
            FavoriteRoomView view = new FavoriteRoomView();
            view.setId(rs.getInt("id"));
            view.setName(rs.getString("name"));
            view.setImagePath(rs.getString("image_path"));
            view.setPrice(rs.getInt("price"));
            return view;
        });
    }


}