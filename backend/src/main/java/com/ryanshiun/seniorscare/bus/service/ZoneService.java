package com.ryanshiun.seniorscare.bus.service;

import java.util.List;


import org.springframework.stereotype.Service;

import com.ryanshiun.seniorscare.bus.dto.ZoneRequest;
import com.ryanshiun.seniorscare.bus.model.FareZone;

/**
 * Data Access Object（資料存取物件），一個「專門負責與資料庫互動的方法集合」的類別 它的主要用途是： 操作資料庫（CRUD）、查資料 /
 * 存資料、封裝 SQL、隱藏資料表結構，讓 Service 不用直接寫 SQL
 *
 */

@Service
public interface ZoneService {

	// 新增行政區
	Integer insertZone(ZoneRequest zoneRequest);

	// 刪除行政區
	Integer deleteZone(int zoneId);

	// 修改行政區
	FareZone updateZone(ZoneRequest zoneRequest);

	// 查詢所有行政區
	List<FareZone> findAllZone();

	// 查詢行政區(根據ID)
	FareZone findById(int zoneId);

	// 查詢行政區(根據名稱模糊查詢)
	List<FareZone> findByFilter(String zoneName);

}
