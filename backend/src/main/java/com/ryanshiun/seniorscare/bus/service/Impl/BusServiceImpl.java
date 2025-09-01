package com.ryanshiun.seniorscare.bus.service.Impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ryanshiun.seniorscare.bus.dao.BusDAO;
import com.ryanshiun.seniorscare.bus.dto.BusQueryParams;
import com.ryanshiun.seniorscare.bus.dto.BusRequest;
import com.ryanshiun.seniorscare.bus.model.Rehabus;
import com.ryanshiun.seniorscare.bus.service.BusService;

@Component
public class BusServiceImpl implements BusService {

	@Autowired
	private BusDAO busDAO;

	// 新增復康巴士
	@Override
	public Integer insertBus(BusRequest busRequest) {
		return busDAO.insertBus(busRequest);
	}

	// 刪除復康巴士
	@Override
	public Integer deleteBus(int busId) {
		return busDAO.deleteBus(busId);
	}

	// 修改復康巴士
	@Override
	public Rehabus updateBus(BusRequest busRequest) {
		return busDAO.updateBus(busRequest);
	}

	// 查詢座位數量(區域查詢)
	@Override
	public List<Rehabus> findByFilter(BusQueryParams busQueryParams) {
		return busDAO.findByFilter(busQueryParams);
	}

	// 查詢復康巴士(根據ID)
	@Override
	public Rehabus findById(int busId) {
		return busDAO.findById(busId);
	}

	// 查詢所有巴士
	@Override
	public List<Rehabus> findAllBus() {
		return busDAO.findAllBus();
	}

	// 放入復康巴士資料
	@Override
	public int importFromCsv(String csvFilePath) {
		return busDAO.importFromCsv(csvFilePath);
	}

}
