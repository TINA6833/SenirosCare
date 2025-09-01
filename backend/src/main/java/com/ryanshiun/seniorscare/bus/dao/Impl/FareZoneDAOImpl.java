package com.ryanshiun.seniorscare.bus.dao.Impl;

import java.util.Collections;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.ryanshiun.seniorscare.bus.dao.FareZoneDAO;
import com.ryanshiun.seniorscare.bus.dto.ZoneRequest;
import com.ryanshiun.seniorscare.bus.model.FareZone;
import com.ryanshiun.seniorscare.bus.rowmapper.FareZoneRowMapper;

/**
 * FareZoneDaoImpl = DAO 的「具體做事者」 FareZoneDaoImpl（實作）：把介面上定義的方法，用實際的 SQL +
 * NamedParameterJdbcTemplate／RowMapper 等，具體實作出來
 */

@Repository
public class FareZoneDAOImpl implements FareZoneDAO {
	
	

	@Autowired
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
	// 取得 Spring 幫你配置好的 JDBC 工具

	// 新增行政區
	@Override
	public Integer insertZone(ZoneRequest zoneRequest) {
		final String sql = "INSERT INTO FareZone (zone_name, description) " + " VALUES (:zoneName, :description)";

		Map<String, Object> iZone = new HashMap<>();
		iZone.put("zoneName", zoneRequest.getZoneName());
		iZone.put("description", zoneRequest.getDescription());

		KeyHolder keyHolder = new GeneratedKeyHolder();
		// 呼叫它的建構子 new GeneratedKeyHolder()，就會得到一個空的 KeyHolder 實例
		namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource(iZone), keyHolder);

		Number key = keyHolder.getKey();
		if (key != null) {

			return key.intValue();
		} else {
			return null;
		}

	}

	// 刪除行政區
	@Override
	public Integer deleteZone(int zoneId) {
		final String sql = "DELETE FROM FareZone WHERE zone_id = :zoneId";
		Map<String, Object> dZone = new HashMap<>();
		dZone.put("zoneId", zoneId);

		return namedParameterJdbcTemplate.update(sql, dZone);
	}

	// 修改行政區
	@Override
	public FareZone updateZone(ZoneRequest zoneRequest) {
		final String sql = "UPDATE FareZone SET zone_name = :zoneName,"
				+ " description = :description WHERE zone_id = :zoneId";

		Map<String, Object> uZone = new HashMap<>();
		uZone.put("zoneId", zoneRequest.getZoneId());
		uZone.put("zoneName", zoneRequest.getZoneName());
		uZone.put("description", zoneRequest.getDescription());

		namedParameterJdbcTemplate.update(sql, uZone);

		final String sql1 = "SELECT * FROM FareZone WHERE zone_id = :zoneId ";
		return namedParameterJdbcTemplate.queryForObject(sql1, Map.of("zoneId", zoneRequest.getZoneId()),
				new FareZoneRowMapper());
	}

	// 查詢所有行政區
	@Override
	public List<FareZone> findAllZone() {
		final String sql = "SELECT * FROM FareZone ";
		Map<String, Object> faZone = Collections.emptyMap();

		return namedParameterJdbcTemplate.query(sql, faZone, new FareZoneRowMapper());
	}

	// 查詢行政區(根據ID)
	@Override
	public FareZone findById(int zoneId) {

		final String sql = "SELECT * FROM FareZone WHERE zone_id = :zoneId ";
		Map<String, Object> fiZone = new HashMap<>();
		fiZone.put("zoneId", zoneId);

		List<FareZone> zoneList = namedParameterJdbcTemplate.query(sql, fiZone, new FareZoneRowMapper());
		if (!zoneList.isEmpty()) {
			return zoneList.get(0);
			// 如果有資料就回傳第一筆，沒有則回傳 null
		} else {
			return null;
		}

	}

	// 查詢行政區(根據名稱模糊查詢)
	@Override
	public List<FareZone> findByFilter(String zoneName) {
		final String sql = "SELECT * FROM FareZone WHERE zone_name LIKE :zoneName ";
		Map<String, Object> fZone = new HashMap<>();
		fZone.put("zoneName", "%" + zoneName + "%");

		return namedParameterJdbcTemplate.query(sql, new MapSqlParameterSource(fZone), new FareZoneRowMapper());
	}

}
