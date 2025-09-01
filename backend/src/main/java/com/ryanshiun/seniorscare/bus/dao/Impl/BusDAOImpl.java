package com.ryanshiun.seniorscare.bus.dao.Impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.ryanshiun.seniorscare.bus.dao.BusDAO;
import com.ryanshiun.seniorscare.bus.dto.BusQueryParams;
import com.ryanshiun.seniorscare.bus.dto.BusRequest;
import com.ryanshiun.seniorscare.bus.model.Rehabus;
import com.ryanshiun.seniorscare.bus.rowmapper.RehabusRowMapper;


/**
 * BusDaoImpl = DAO 的「具體做事者」
 * BusDaoImpl（實作）：把介面上定義的方法，用實際的 SQL + NamedParameterJdbcTemplate／RowMapper 等，具體實作出來
 */

@Repository
public class BusDAOImpl implements BusDAO {

	@Autowired
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    //取得 Spring 幫你配置好的 JDBC 工具
	
	//新增復康巴士
	@Override
	public Integer insertBus(BusRequest busRequest) {
		final String sql = "INSERT INTO rehabus (car_dealership, bus_brand, bus_model, seat_capacity, wheelchair_capacity, license_plate)"
				+ " VALUES (:carDealership, :busBrand, :busModel, :seatCapacity, :wheelchairCapacity, :licensePlate)";
		Map<String, Object> iBus = new HashMap<>();

		iBus.put("carDealership", busRequest.getCarDealership());
		iBus.put("busBrand", busRequest.getBusBrand());
		iBus.put("busModel", busRequest.getBusModel());
		iBus.put("seatCapacity", busRequest.getSeatCapacity());
		iBus.put("wheelchairCapacity", busRequest.getWheelchairCapacity());
		iBus.put("licensePlate", busRequest.getLicensePlate());

		KeyHolder keyHolder = new GeneratedKeyHolder();
		namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource(iBus), keyHolder);

		return Objects.requireNonNull(keyHolder.getKey()).intValue();
	}

	//刪除巴士資料
	@Override
	public Integer deleteBus(int busId) {

		final String sql = "DELETE FROM rehabus WHERE bus_id = :busId";
		Map<String, Object> dBus = new HashMap<>();
		dBus.put("busId", busId);

		return namedParameterJdbcTemplate.update(sql, dBus);
	}

	//更新巴士資料
	@Override
	public Rehabus updateBus(BusRequest busRequest) {
		final String sql = "UPDATE rehabus SET "
				+ "car_dealership = :carDealership, bus_brand = :busBrand, bus_model = :busModel, "
				+ "seat_capacity = :seatCapacity, wheelchair_capacity = :wheelchairCapacity, license_plate = :licensePlate, "
				+ "status = :status WHERE bus_id = :busId";
		
		Map<String, Object> uBus = new HashMap<>();
		uBus.put("busId", busRequest.getBusId());
		uBus.put("carDealership", busRequest.getCarDealership());
		uBus.put("busBrand", busRequest.getBusBrand());
		uBus.put("busModel", busRequest.getBusModel());
		uBus.put("seatCapacity", busRequest.getSeatCapacity());
		uBus.put("wheelchairCapacity", busRequest.getWheelchairCapacity());
		uBus.put("licensePlate", busRequest.getLicensePlate());
		uBus.put("status", busRequest.getStatus());

		namedParameterJdbcTemplate.update(sql, uBus);

		final String sql1 = "SELECT * FROM rehabus WHERE bus_id = :busId ";
		return namedParameterJdbcTemplate.queryForObject(sql1, Map.of("busId", busRequest.getBusId()), new RehabusRowMapper());
	}
	

	//查詢座位數量所對應的復康巴士
	@Override
	public List<Rehabus> findByFilter(BusQueryParams busQueryParams) {

		StringBuilder sql = new StringBuilder("SELECT * FROM rehabus WHERE 1=1");
		Map<String, Object> fBus = new HashMap<>();

		if (busQueryParams.getMinSeats() != null) {
			sql.append(" AND seat_capacity  >= :minSeats");
			// 表示「最小座位數」查詢條件
			fBus.put("minSeats", busQueryParams.getMinSeats());

		}

		if (busQueryParams.getMinWheels() != null) {
			sql.append(" AND wheelchair_capacity >= :minWheels ");
			// 表示「最小輪椅座位數」查詢條件
			fBus.put("minWheels", busQueryParams.getMinWheels());
		}

		return namedParameterJdbcTemplate.query(sql.toString(), fBus, new RehabusRowMapper());
	}

	
	//查詢復康巴士(by id)
	@Override
	public Rehabus findById(int busId) {

		final String sql = "SELECT * FROM rehabus WHERE bus_id = :busId ";
		Map<String, Object> fiBus = new HashMap<>();
		fiBus.put("busId", busId);

		List<Rehabus> busList = namedParameterJdbcTemplate.query(sql, fiBus, new RehabusRowMapper());
		if (!busList.isEmpty()) {
			return busList.get(0);
			// 如果有資料就回傳第一筆，沒有則回傳 null
		} else {
			return null;
		}
	}

	
	//查詢全部的復康巴士資料
	@Override
	public List<Rehabus> findAllBus() {

		final String sql = "SELECT * FROM rehabus ";
		Map<String, Object> faBus = Collections.emptyMap();

		return namedParameterJdbcTemplate.query(sql, faBus, new RehabusRowMapper());
	}

	
	//匯入CSV檔案
	@Override
	public int importFromCsv(String csvFilePath) {

		final String sql = "INSERT INTO rehabus "
				+ "(car_dealership, bus_brand, bus_model, seat_capacity, wheelchair_capacity, license_plate) "
				+ "VALUES "
				+ "(:carDealership, :busBrand, :busModel, :seatCapacity, :wheelchairCapacity, :licensePlate)";

	 // 收集每一列的 SqlParameterSource
	    List<SqlParameterSource> batchParams = new ArrayList<>();
        
     // 用 try-with-resources 讀 CSV 檔
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(csvFilePath))) {
        	String line = reader.readLine();
        	
        while ((line = reader.readLine()) != null) {
     // 切欄位 (假設 CSV 欄位順序剛好是這些)   	
        	String[] col = line.split(",",-1);
        	
        if (col.length < 6) {
        	System.err.println("CSV 欄位數不足，跳過此行: " + line);
            continue;	
		}	
        
     // 每列對應一個 MapSqlParameterSource
        MapSqlParameterSource params = new MapSqlParameterSource()
        	.addValue("carDealership", col[0].trim())
        	.addValue("busBrand", col[1].trim())
        	.addValue("busModel", col[2].trim())
        	.addValue("seatCapacity", col[3].trim())
        	.addValue("wheelchairCapacity", col[4].trim())
        	.addValue("licensePlate", col[5].trim());
        
        batchParams.add(params);
        }
        
        
        } catch (IOException e) {
		  throw new RuntimeException("CSV 匯入失敗: " + e.getMessage(), e);
		  
        }
      // 一次批次執行所有 INSERT
        int[] results = namedParameterJdbcTemplate.batchUpdate(
        		sql, batchParams.toArray(new SqlParameterSource[0]));
        
		return Arrays.stream(results).sum();
                      	
		}


}
