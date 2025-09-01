package com.ryanshiun.seniorscare.bus.service.Impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ryanshiun.seniorscare.bus.dao.FareZoneDAO;
import com.ryanshiun.seniorscare.bus.dto.ZoneRequest;
import com.ryanshiun.seniorscare.bus.model.FareZone;
import com.ryanshiun.seniorscare.bus.service.ZoneService;

@Service
public class ZoneServiceImpl implements ZoneService {

	@Autowired
	private FareZoneDAO zoneDAO;
	
	//新增行政區
	public Integer insertZone(ZoneRequest zoneRequest) {
		return zoneDAO.insertZone(zoneRequest);
	}
		
		//刪除行政區
		public Integer deleteZone(int zoneId) {
			return zoneDAO.deleteZone(zoneId);
		}
		
		//修改行政區
		public FareZone updateZone(ZoneRequest zoneRequest) {
			return zoneDAO.updateZone(zoneRequest);
		}
		
		//查詢所有行政區
		public List<FareZone> findAllZone() {
			return zoneDAO.findAllZone();
		}
		
		//查詢行政區(根據ID)
		public FareZone findById(int zoneId) {
			return zoneDAO.findById(zoneId);
		}
		
		//查詢行政區(根據名稱模糊查詢)
		public List<FareZone> findByFilter(String zoneName) {
			return zoneDAO.findByFilter(zoneName);
		}
		
}
