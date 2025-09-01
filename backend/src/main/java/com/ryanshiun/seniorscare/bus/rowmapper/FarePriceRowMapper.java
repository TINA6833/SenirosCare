package com.ryanshiun.seniorscare.bus.rowmapper;

import java.sql.ResultSet;

import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;

import com.ryanshiun.seniorscare.bus.model.FarePrice;

/**
 * 用來把「資料庫查詢結果的每一列」轉成 Java 的 FarePrice 物件
 */

public class FarePriceRowMapper implements RowMapper<FarePrice> {

	@Override
	public FarePrice mapRow(ResultSet rSet, int rowNum) throws SQLException {

		FarePrice farePrice = new FarePrice();
		farePrice.setPriceId(rSet.getInt("price_id"));
		farePrice.setFromZone(rSet.getInt("from_zone_id"));
		farePrice.setToZone(rSet.getInt("to_zone_id"));
		farePrice.setPrice(rSet.getInt("price"));
		farePrice.setActualDistanceKm(rSet.getBigDecimal("actual_distance_km"));
		farePrice.setPriceStatus(rSet.getString("price_status"));

		return farePrice;

	}
}