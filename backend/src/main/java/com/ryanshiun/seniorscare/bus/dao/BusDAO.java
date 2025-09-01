package com.ryanshiun.seniorscare.bus.dao;

import java.util.List;


import com.ryanshiun.seniorscare.bus.dto.BusQueryParams;
import com.ryanshiun.seniorscare.bus.dto.BusRequest;
import com.ryanshiun.seniorscare.bus.model.Rehabus;


/**
 * Data Access Object（資料存取物件），一個「專門負責與資料庫互動的方法集合」的類別
 * 它的主要用途是：
 * 操作資料庫（CRUD）、查資料 / 存資料、封裝 SQL、隱藏資料表結構，讓 Service 不用直接寫 SQL
 *
 */
public interface BusDAO {

	//新增復康巴士
	Integer insertBus(BusRequest busRequest);
	
	//刪除復康巴士
	Integer deleteBus(int busId);
	
	//修改復康巴士
	Rehabus updateBus(BusRequest busRequest);
	
	//查詢座位數量(區域查詢)
	List<Rehabus> findByFilter(BusQueryParams busQueryParams);
	
	//查詢復康巴士(根據ID)
	Rehabus findById(int busId);
	
	//查詢所有巴士
	List<Rehabus> findAllBus();
	
	//放入復康巴士資料
	int importFromCsv(String csvFilePath);

	
		
}
