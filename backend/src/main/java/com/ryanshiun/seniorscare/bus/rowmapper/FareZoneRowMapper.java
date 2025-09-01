package com.ryanshiun.seniorscare.bus.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.ryanshiun.seniorscare.bus.model.FareZone;

/**
 * 用來把「資料庫查詢結果的每一列」轉成 Java 的 FareZone 物件
 */

public class FareZoneRowMapper implements RowMapper<FareZone> {

	@Override
	public FareZone mapRow(ResultSet rSet, int rowNum) throws SQLException {

		FareZone fareZone = new FareZone();
		fareZone.setZoneId(rSet.getInt("zone_id"));
		fareZone.setZoneName(rSet.getString("zone_name"));
		fareZone.setDescription(rSet.getString("description"));
		
		return fareZone;
		
	}

}
