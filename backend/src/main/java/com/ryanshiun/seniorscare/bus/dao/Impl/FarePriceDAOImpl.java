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

import com.ryanshiun.seniorscare.bus.dao.FarePriceDAO;
import com.ryanshiun.seniorscare.bus.dto.PriceRequest;
import com.ryanshiun.seniorscare.bus.model.FarePrice;
import com.ryanshiun.seniorscare.bus.rowmapper.FarePriceRowMapper;

/**
 * FarePriceDAOImpl = DAO 的「具體做事者」 FarePriceDAOImpl（實作）：把介面上定義的方法，用實際的 SQL +
 * NamedParameterJdbcTemplate／RowMapper 等，具體實作出來
 */

@Repository
public class FarePriceDAOImpl implements FarePriceDAO {

	@Autowired
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
	// 取得 Spring 幫你配置好的 JDBC 工具

	// 新增票價
	@Override
	public Integer insertPrice(PriceRequest priceRequest) {
		final String sql = "INSERT INTO FarePrice (from_zone_id, to_zone_id, price, "
				+ "actual_distance_km, price_status) VALUES (:fromZone, :toZone, "
				+ ":price, :actualDistanceKm, :priceStatus)";

		Map<String, Object> iPrice = new HashMap<>();
		iPrice.put("fromZone", priceRequest.getFromZone());
		iPrice.put("toZone", priceRequest.getToZone());
		iPrice.put("price", priceRequest.getPrice());
		iPrice.put("actualDistanceKm", priceRequest.getActualDistanceKm());
		iPrice.put("priceStatus", priceRequest.getPriceStatus());

		KeyHolder keyHolder = new GeneratedKeyHolder();
		// 呼叫它的建構子 new GeneratedKeyHolder()，就會得到一個空的 KeyHolder 實例
		namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource(iPrice), keyHolder);
		Number key = keyHolder.getKey();
		if (key != null) {

			return key.intValue();
		} else {
			return null;
		}

	}

	// 刪除票價
	@Override
	public Integer deletePrice(int priceId) {
		final String sql = "DELETE FROM FarePrice WHERE price_id = :priceId";
		Map<String, Object> dPrice = new HashMap<>();
		dPrice.put("priceId", priceId);

		return namedParameterJdbcTemplate.update(sql, dPrice);
	}

	// 更新票價
	@Override
	public FarePrice updatePrice(PriceRequest priceRequest) {
		final String sql = "UPDATE FarePrice SET from_zone_id = :fromZone, "
				+ "to_zone_id = :toZone, price = :price, actual_distance_km = :actualDistanceKm, "
				+ "price_status = :priceStatus WHERE price_id = :priceId" ;

		Map<String, Object> uPrice = new HashMap<>();
		uPrice.put("fromZone", priceRequest.getFromZone());
		uPrice.put("toZone", priceRequest.getToZone());
		uPrice.put("price", priceRequest.getPrice());
		uPrice.put("actualDistanceKm", priceRequest.getActualDistanceKm());
		uPrice.put("priceStatus", priceRequest.getPriceStatus());

		namedParameterJdbcTemplate.update(sql, uPrice);

		final String sql1 = "SELECT * FROM FarePrice WHERE price_id = :priceId ";
		return namedParameterJdbcTemplate.queryForObject(sql1, Map.of("priceId", priceRequest.getPriceId()),
				new FarePriceRowMapper());

	}

	// 查詢全部票價
	@Override
	public List<FarePrice> findAll() {
		final String sql = "SELECT * FROM FarePrice ";
		Map<String, Object> faPrice = Collections.emptyMap();

		return namedParameterJdbcTemplate.query(sql, faPrice, new FarePriceRowMapper());
	}

	// 查詢票價(根據ID)
	@Override
	public FarePrice findById(int priceId) {
		final String sql = "SELECT * FROM FarePrice WHERE price_id = :priceId ";
		Map<String, Object> fiPrice = new HashMap<>();
		fiPrice.put("priceId", priceId);

		List<FarePrice> priceList = namedParameterJdbcTemplate.query(sql, fiPrice, new FarePriceRowMapper());
		if (!priceList.isEmpty()) {
			return priceList.get(0);
			// 如果有資料就回傳第一筆，沒有則回傳 null
		} else {
			return null;
		}
	}

	// 依起迄行政區查票價（票價試算用）
	@Override
	public FarePrice getPriceByZone(int fromZone, int toZone) {
		final String sql = "SELECT * FROM FarePrice WHERE from_zone_id = :fromZone " + "AND to_zone_id = :toZone";
		Map<String, Object> params = new HashMap<>();
		params.put("fromZone", fromZone);
		params.put("toZone", toZone);

		List<FarePrice> priceList = namedParameterJdbcTemplate.query(sql, params, new FarePriceRowMapper());
		if (!priceList.isEmpty()) {
			return priceList.get(0);
			// 如果有資料就回傳第一筆，沒有則回傳 null
		} else {
			return null;
		}
	}

	// 依起點查全部票價
	@Override
	public List<FarePrice> getPricesByFromZone(int fromZone) {
		final String sql = "SELECT * FROM FarePrice WHERE from_zone_id = :fromZone ";
		Map<String, Object> findPrice = new HashMap<>();
		findPrice.put("fromZone", fromZone);

		return namedParameterJdbcTemplate.query(sql, findPrice, new FarePriceRowMapper());

	}
	
	//切換票價狀態
	@Override
	public Integer updatePriceStatus(int priceId, String priceStatus) {
		final String sql = "UPDATE FarePrice SET price_status = :priceStatus "
				+ "WHERE price_id = :priceId ";
		Map<String, Object> sPrice = new HashMap<>();
		sPrice.put("priceId", priceId);
		sPrice.put("priceStatus", priceStatus);
		
		return namedParameterJdbcTemplate.update(sql, sPrice);
	}

}
