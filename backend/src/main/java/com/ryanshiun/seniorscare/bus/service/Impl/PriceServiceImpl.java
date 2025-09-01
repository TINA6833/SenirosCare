package com.ryanshiun.seniorscare.bus.service.Impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.ryanshiun.seniorscare.bus.dao.FarePriceDAO;
import com.ryanshiun.seniorscare.bus.dto.PriceRequest;
import com.ryanshiun.seniorscare.bus.model.FarePrice;
import com.ryanshiun.seniorscare.bus.service.PriceService;

@Component
public class PriceServiceImpl implements PriceService {

	@Autowired
	private FarePriceDAO farePriceDAO;

	// 新增票價
	@Override
	public Integer insertPrice(PriceRequest priceRequest) {
		return farePriceDAO.insertPrice(priceRequest);
	}

	// 刪除票價
	@Override
	public Integer deletePrice(int priceId) {
		return farePriceDAO.deletePrice(priceId);
	}

	// 更新票價
	@Override
	public FarePrice updatePrice(PriceRequest priceRequest) {
		return farePriceDAO.updatePrice(priceRequest);
	}

	// 查詢全部票價
	@Override
	public List<FarePrice> findAll() {
		return farePriceDAO.findAll();
	}

	// 查詢票價(根據ID)
	@Override
	public FarePrice findById(int priceId) {
		return farePriceDAO.findById(priceId);
	}

	// 依起迄行政區查票價（票價試算用）
	public FarePrice getPriceByZone(int fromZone, int toZone) {
		return farePriceDAO.getPriceByZone(fromZone, toZone);
	}

	// 依起點查全部票價
	@Override
	public List<FarePrice> getPricesByFromZone(int fromZone) {
		return farePriceDAO.getPricesByFromZone(fromZone);
	}

	// 切換票價狀態
	@Override
	public Integer updatePriceStatus(int priceId, String priceStatus) {
		int update = farePriceDAO.updatePriceStatus(priceId, priceStatus);
		
		//回傳值 == 0：表示找不到對應的那筆 price_id，也就是沒有任何資料被修改到
		if (update == 0) {
			
			throw new ResponseStatusException(HttpStatus.NOT_FOUND,"找不到 priceId = " + priceId);
		} 
	       return update;
	}

}
