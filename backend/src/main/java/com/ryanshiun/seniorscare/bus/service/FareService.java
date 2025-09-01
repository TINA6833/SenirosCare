package com.ryanshiun.seniorscare.bus.service;

import org.springframework.stereotype.Service;

import com.ryanshiun.seniorscare.bus.dto.FareQuote;

/**
* 試算票價的服務介面
* Data Access Object（資料存取物件），一個「專門負責與資料庫互動的方法集合」的類別 它的主要用途是： 操作資料庫（CRUD）、查資料 /
* 存資料、封裝 SQL、隱藏資料表結構，讓 Service 不用直接寫 SQL
*
*/

@Service
public interface FareService {

	/**
     * 以起點與終點（地址）呼叫 Google Distance Matrix 取得駕車距離，
     * 再依桃園市計程車計費規則計算車資，最後換算復康巴士費用（1/3 無條件進位）。
     *
     * @param origin 起點（例：桃園市政府）
     * @param destination 終點（例：中壢車站）
     * @return 試算結果。如果地址無效或 API 失敗，回傳 null。
     */
	 FareQuote quoteByAddresses(String origin, String destination);
	
	
	
}
