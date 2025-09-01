package com.ryanshiun.seniorscare.bus.dao;

import java.util.List;

import com.ryanshiun.seniorscare.bus.dto.PriceRequest;
import com.ryanshiun.seniorscare.bus.model.FarePrice;

/**
	 * Data Access Object（資料存取物件），一個「專門負責與資料庫互動的方法集合」的類別
	 * 它的主要用途是：
	 * 操作資料庫（CRUD）、查資料 / 存資料、封裝 SQL、隱藏資料表結構，讓 Service 不用直接寫 SQL
	 *
	 */
	
public interface FarePriceDAO {
	
	//新增票價
	Integer insertPrice(PriceRequest priceRequest);
	
	//刪除票價
	Integer deletePrice(int priceId);
	
	//更新票價
	FarePrice updatePrice(PriceRequest priceRequest);
	
	//查詢全部票價
	List<FarePrice> findAll();
	
	//查詢票價(根據ID)
	FarePrice findById(int priceId);
	
	//依起迄行政區查票價（票價試算用）
	FarePrice getPriceByZone(int fromZone, int toZone);
	
	//依起點查全部票價
	List<FarePrice> getPricesByFromZone(int fromZone);
	
	//切換票價狀態
	Integer updatePriceStatus(int priceId, String priceStatus);
	
	

}
