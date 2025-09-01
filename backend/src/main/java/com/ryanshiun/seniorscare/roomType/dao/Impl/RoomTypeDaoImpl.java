package com.ryanshiun.seniorscare.roomType.dao.Impl;


import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.ryanshiun.seniorscare.roomType.dao.RoomTypeDao;
import com.ryanshiun.seniorscare.roomType.model.RoomComment;
import com.ryanshiun.seniorscare.roomType.model.RoomType;
import com.ryanshiun.seniorscare.roomType.rowmapper.RoomTypeRowMapper;


@Repository
public class RoomTypeDaoImpl implements RoomTypeDao {

	private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedJdbc;

    public RoomTypeDaoImpl(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedJdbc) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedJdbc = namedJdbc;
    }

    @Override
    public int insert(RoomType roomType) {
        String sql = "INSERT INTO roomtype (name, price, capacity, description, image_path, is_available, admin_note) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        return jdbcTemplate.update(sql,
                roomType.getName(),
                roomType.getPrice(),
                roomType.getCapacity(),
                roomType.getDescription(),
                roomType.getImagePath(),
                roomType.isAvailable(),
                roomType.getAdminNote());
    }

    @Override
    public List<RoomType> findAll() {
        String sql = "SELECT * FROM roomtype";
        return jdbcTemplate.query(sql, new RoomTypeRowMapper());
    }

    @Override
    public RoomType findById(int id) {
        String sql = "SELECT * FROM roomtype WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, new RoomTypeRowMapper(), id);
    }

    @Override
    public List<RoomType> findByPriceRange(int minPrice, int maxPrice) {
        String sql = "SELECT * FROM roomtype WHERE price BETWEEN ? AND ?";
        return jdbcTemplate.query(sql, new RoomTypeRowMapper(), minPrice, maxPrice);
    }

    @Override
    public List<RoomType> findByKeyword(String keyword) {
        String sql = "SELECT * FROM roomtype WHERE name LIKE ? OR description LIKE ?";
        String like = "%" + keyword + "%";
        return jdbcTemplate.query(sql, new RoomTypeRowMapper(), like, like);
    }

    @Override
    public List<RoomType> findByCapacity(int capacity) {
        String sql = "SELECT * FROM roomtype WHERE capacity = ?";
        return jdbcTemplate.query(sql, new RoomTypeRowMapper(), capacity);
    }

    @Override
    public boolean update(RoomType roomType) {
        String sql = "UPDATE roomtype SET name = ?, price = ?, capacity = ?, description = ?, image_path = ?, " +
                     "is_available = ?, admin_note = ?, updated_at = SYSUTCDATETIME() WHERE id = ?";
        return jdbcTemplate.update(sql,
                roomType.getName(),
                roomType.getPrice(),
                roomType.getCapacity(),
                roomType.getDescription(),
                roomType.getImagePath(),
                roomType.isAvailable(),
                roomType.getAdminNote(),
                roomType.getId()) > 0;
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM roomtype WHERE id = ?";
        return jdbcTemplate.update(sql, id) > 0;
    }

    @Override
    public List<RoomType> findPaged(int offset, int limit) {
        String sql = "SELECT * FROM roomtype ORDER BY id OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        return jdbcTemplate.query(sql, new RoomTypeRowMapper(), offset, limit);
    }

    @Override
    public int countAll() {
        String sql = "SELECT COUNT(*) FROM roomtype";
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }
    
    @Override
    public List<RoomType> findFilteredPaged(
            String keyword, Integer minPrice, Integer maxPrice, Integer capacity,
            List<Integer> featureIds, boolean matchAll,
            String sortBy, String order, int offset, int limit) {

        StringBuilder sql = new StringBuilder();
        MapSqlParameterSource params = new MapSqlParameterSource();

        sql.append("""
            SELECT rt.*
            FROM roomtype rt
        """);

        boolean hasFeatures = featureIds != null && !featureIds.isEmpty();
        if (hasFeatures && matchAll) {
            // matchAll=true：JOIN + GROUP BY + HAVING COUNT(DISTINCT)=size
            sql.append(" JOIN roomtype_feature rtf ON rtf.room_type_id = rt.id ");
        }

        // WHERE
        sql.append(" WHERE 1=1 ");

        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (rt.name LIKE :kw OR rt.description LIKE :kw) ");
            params.addValue("kw", "%" + keyword.trim() + "%");
        }
        if (minPrice != null) {
            sql.append(" AND rt.price >= :minPrice ");
            params.addValue("minPrice", minPrice);
        }
        if (maxPrice != null) {
            sql.append(" AND rt.price <= :maxPrice ");
            params.addValue("maxPrice", maxPrice);
        }
        if (capacity != null) {
            sql.append(" AND rt.capacity >= :capacity ");
            params.addValue("capacity", capacity);
        }

        if (hasFeatures) {
            if (matchAll) {
                sql.append(" AND rtf.feature_id IN (:featureIds) ");
                params.addValue("featureIds", featureIds);
            } else {
                // matchAny：EXISTS 子查詢
                sql.append("""
                    AND EXISTS (
                        SELECT 1 FROM roomtype_feature x
                        WHERE x.room_type_id = rt.id
                          AND x.feature_id IN (:featureIds)
                    )
                """);
                params.addValue("featureIds", featureIds);
            }
        }

        // GROUP BY / HAVING（僅 matchAll 需要）
        if (hasFeatures && matchAll) {
            sql.append("""
                GROUP BY rt.id, rt.name, rt.price, rt.capacity, rt.description,
                         rt.image_path, rt.is_available, rt.admin_note,
                         rt.created_at, rt.updated_at
                HAVING COUNT(DISTINCT rtf.feature_id) = :featSize
            """);
            params.addValue("featSize", featureIds.size());
        }

        // 排序白名單
        String sortCol = switch (Optional.ofNullable(sortBy).orElse("createdAt")) {
            case "price"     -> "rt.price";
            case "capacity"  -> "rt.capacity";
            case "name"      -> "rt.name";
            case "createdAt" -> "rt.created_at";
            default          -> "rt.created_at";
        };
        String sortOrder = "desc".equalsIgnoreCase(order) ? "DESC" : "ASC";

        sql.append(" ORDER BY ").append(sortCol).append(" ").append(sortOrder);
        sql.append(" OFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY ");
        params.addValue("offset", Math.max(offset, 0));
        params.addValue("limit",  Math.max(limit, 1));

        return namedJdbc.query(sql.toString(), params, new RoomTypeRowMapper());
    }

    @Override
    public int countFiltered(
            String keyword, Integer minPrice, Integer maxPrice, Integer capacity,
            List<Integer> featureIds, boolean matchAll) {

        MapSqlParameterSource params = new MapSqlParameterSource();
        boolean hasFeatures = featureIds != null && !featureIds.isEmpty();

        if (hasFeatures && matchAll) {
            // 需要跟查詢一致的 GROUP/HAVING，再外層 COUNT(*)
            StringBuilder inner = new StringBuilder();
            inner.append("""
                SELECT rt.id
                FROM roomtype rt
                JOIN roomtype_feature rtf ON rtf.room_type_id = rt.id
                WHERE 1=1
            """);

            if (keyword != null && !keyword.isBlank()) {
                inner.append(" AND (rt.name LIKE :kw OR rt.description LIKE :kw) ");
                params.addValue("kw", "%" + keyword.trim() + "%");
            }
            if (minPrice != null) { inner.append(" AND rt.price >= :minPrice "); params.addValue("minPrice", minPrice); }
            if (maxPrice != null) { inner.append(" AND rt.price <= :maxPrice "); params.addValue("maxPrice", maxPrice); }
            if (capacity != null) { inner.append(" AND rt.capacity >= :capacity "); params.addValue("capacity", capacity); }

            inner.append(" AND rtf.feature_id IN (:featureIds) ");
            params.addValue("featureIds", featureIds);

            inner.append("""
                GROUP BY rt.id
                HAVING COUNT(DISTINCT rtf.feature_id) = :featSize
            """);
            params.addValue("featSize", featureIds.size());

            String sql = "SELECT COUNT(*) FROM (" + inner + ") t";
            return namedJdbc.queryForObject(sql, params, Integer.class);
        } else {
            // 不用 HAVING 的情境（無特徵/任一特徵EXISTS）
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT COUNT(DISTINCT rt.id) FROM roomtype rt WHERE 1=1 ");

            if (keyword != null && !keyword.isBlank()) {
                sql.append(" AND (rt.name LIKE :kw OR rt.description LIKE :kw) ");
                params.addValue("kw", "%" + keyword.trim() + "%");
            }
            if (minPrice != null) { sql.append(" AND rt.price >= :minPrice "); params.addValue("minPrice", minPrice); }
            if (maxPrice != null) { sql.append(" AND rt.price <= :maxPrice "); params.addValue("maxPrice", maxPrice); }
            if (capacity != null) { sql.append(" AND rt.capacity >= :capacity "); params.addValue("capacity", capacity); }

            if (hasFeatures) {
                sql.append("""
                    AND EXISTS (
                        SELECT 1 FROM roomtype_feature x
                        WHERE x.room_type_id = rt.id
                          AND x.feature_id IN (:featureIds)
                    )
                """);
                params.addValue("featureIds", featureIds);
            }

            return namedJdbc.queryForObject(sql.toString(), params, Integer.class);
        }
    }
    
    
}
