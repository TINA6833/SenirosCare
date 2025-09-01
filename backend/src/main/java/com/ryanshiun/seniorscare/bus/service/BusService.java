package com.ryanshiun.seniorscare.bus.service;

import java.util.List;

import com.ryanshiun.seniorscare.bus.dto.BusQueryParams;
import com.ryanshiun.seniorscare.bus.dto.BusRequest;
import com.ryanshiun.seniorscare.bus.model.Rehabus;



public interface BusService {

	// 新增復康巴士
	Integer insertBus(BusRequest busRequest);

	// 刪除復康巴士
	Integer deleteBus(int busId);

	// 修改復康巴士
	Rehabus updateBus(BusRequest busRequest);

	// 查詢座位數量(區域查詢)
	List<Rehabus> findByFilter(BusQueryParams busQueryParams);

	// 查詢復康巴士(根據ID)
	Rehabus findById(int busId);

	// 查詢所有巴士
	List<Rehabus> findAllBus();

	// 放入復康巴士資料
	int importFromCsv(String csvFilePath);
	

}
