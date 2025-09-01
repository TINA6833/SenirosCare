package com.ryanshiun.seniorscare.bus.utils;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * 這是一個 Spring 的 @Component，專門跟 Google 的兩個 API 溝通：
 * Distance Matrix API：算「起點 → 終點」的駕車距離。
 * Geocoding API（備援）：當文字地址讓 Distance Matrix 認不得時，先把地址轉成 lat,lng 經緯度，再用經緯度去算距離。
 * 成功回傳「公尺」距離（Integer）；失敗回 null，讓上層決定要不要回 400 或自訂錯誤。
 */

@Component
public class GoogleMapsClient {

	/*
	 * Spring 的屬性佔位符 (Property Placeholder) 語法 ${...} 裡面放的是「屬性鍵名」，Spring 會在啟動時從它的
	 * Environment （含 application.properties / .yml、環境變數、系統參數、命令列參數…） 去解析並注入對應的值
	 */

	//Logger：之後用 log.info 印出 API 狀態，第一時間看出錯在哪（例如 REQUEST_DENIED、NOT_FOUND）
	private static final Logger log = LoggerFactory.getLogger(GoogleMapsClient.class);

	@Value("${GOOGLE_MAPS_API_KEY}")
	private String apiKey;

	private final RestTemplate rest;

	// 設定基本逾時（毫秒）
	public GoogleMapsClient() {
		SimpleClientHttpRequestFactory schrFactory = new SimpleClientHttpRequestFactory();
		//連線建立的時間上限:5s
		schrFactory.setConnectTimeout(5000);
		schrFactory.setReadTimeout(10000);
		//連線成功後，等待回應資料的時間上限:10s
		this.rest = new RestTemplate(schrFactory);
	}

	//文字正規化（只處理空白):避免地址裡有奇怪的空格導致geocoding 失敗
	private String normalize(String text) {
		if (text == null)
			return null;

		//全形空白(U+3000) → 半形
		text = text.replace("\u3000", " ");
		
		//去頭尾空白（含 Unicode）
		text = text.strip();
		
		//連續空白壓成單一空白
		text = text.replaceAll("\\s", " ");
		
		return text;
	}

	//呼叫 Distance Matrix（origin/destination 可以是地址或經緯度 "lat,lng"）
	private ResponseEntity<Map> callDistanceMatrix(String origin, String destination) {
		
		//UriComponentsBuilder:自動處理編碼
		URI uri = UriComponentsBuilder
				.fromUriString("https://maps.googleapis.com/maps/api/distancematrix/json")
				.queryParam("origins", origin)
				.queryParam("destinations", destination)
				.queryParam("mode", "driving")
				.queryParam("language", "zh-TW")
				.queryParam("region", "tw") 
				// 只用參數提示區域，不改原字串
				.encode(StandardCharsets.UTF_8)
				.queryParam("key", apiKey)
				.build()
				.toUri();

		//回傳 ResponseEntity<Map>：讓 Jackson 把 JSON 直接轉成 Java 結構
		return rest.getForEntity(uri, Map.class);
	}

	// 將地址 geocode 成經緯度"lat,lng"；需要在 GCP 啟用 Geocoding API（失敗回 null）
	public String geocodeToLatLng(String address) {
		try {
			URI uri = UriComponentsBuilder
					.fromUriString("https://maps.googleapis.com/maps/api/geocode/json")
					.queryParam("address", address)
					.queryParam("region", "tw")
					.queryParam("key", apiKey)
					.encode(StandardCharsets.UTF_8)
					.build()
					.toUri();

			//發送 GET，把回來的 JSON 直接反序列化成 Map
			ResponseEntity<Map> respEntity = rest.getForEntity(uri, Map.class);
			Map<?, ?> body = respEntity.getBody();
			if (body == null)
				return null;

			//檢查頂層狀態:Geocoding API 會回 status 欄位
			Object status = body.get("status");
			//使用 instanceof 確認是否為字串
			if (!(status instanceof String) || !"OK".equals(status)) {
				log.info("[GEOCODE] status={}", status);
				return null;
			}

			//Geocoding 回的主要內容在 results 陣列
			Object resultsObj = body.get("results");
			if (!(resultsObj instanceof List))
				return null;
			List<?> results = (List<?>) resultsObj;
			if (results.isEmpty())
				return null;

			//取第一筆結果（最匹配的那一筆）
			Object r0 = results.get(0);
			if (!(r0 instanceof Map))
				return null;
			Map<?, ?> res = (Map<?, ?>) r0;

			
			//依照 Geocoding API 的結構一路往下判斷:results[0] → geometry → location → lat/lng
			//每一步都用 instanceof 做型別守護，避免「Unchecked cast 警告」與執行期轉型錯誤
			Object geometryObj = res.get("geometry");
			if (!(geometryObj instanceof Map))
				return null;
			Map<?, ?> geometry = (Map<?, ?>) geometryObj;

			Object locObj = geometry.get("location");
			if (!(locObj instanceof Map))
				return null;
			Map<?, ?> loc = (Map<?, ?>) locObj;

			Object latObj = loc.get("lat");
			Object lngObj = loc.get("lng");
			if (!(latObj instanceof Number) || !(lngObj instanceof Number))
				return null;

			//轉成double 之後，用「逗號分隔」組成 "lat,lng" 字串回傳
			double lat = ((Number) latObj).doubleValue();
			double lng = ((Number) lngObj).doubleValue();
			return lat + "," + lng; 

		} catch (Exception e) {

			return null;

		}
	}

	/**
	 * 回傳駕車距離（公尺）；失敗回 null。 先以地址直接呼叫 Distance Matrix；若元素狀態非 OK（NOT_FOUND /
	 * ZERO_RESULTS 等）， 則兩端地址先 geocode 成座標，再重打一次 Distance Matrix。
	 */
	public Integer getDrivingDistanceMeters(String origin, String destination) {
		try {
			String o1 = normalize(origin);
			String d1 = normalize(destination);

			// 第一次：文字地址直接查
			ResponseEntity<Map> resp1 = callDistanceMatrix(o1, d1);
			Map<?, ?> body1 = resp1.getBody();
			if (body1 == null) {
				log.warn("[GMAPS] empty body");
				return null;
			}

			Object topStatus = body1.get("status");
			Object errMsg = body1.get("error_message");
			log.info("[GMAPS] top status={}, err={}", topStatus, errMsg);
			if (topStatus instanceof String && !"OK".equals(topStatus))
				return null;

			Object rowsObj = body1.get("rows");
			if (!(rowsObj instanceof List))
				return null;
			List<?> rows = (List<?>) rowsObj;
			if (rows.isEmpty())
				return null;

			Object row0 = rows.get(0);
			if (!(row0 instanceof Map))
				return null;
			Map<?, ?> rowMap = (Map<?, ?>) row0;

			Object elementsObj = rowMap.get("elements");
			if (!(elementsObj instanceof List))
				return null;
			List<?> elements = (List<?>) elementsObj;
			if (elements.isEmpty())
				return null;

			Object ele0Obj = elements.get(0);
			if (!(ele0Obj instanceof Map))
				return null;
			Map<?, ?> ele0 = (Map<?, ?>) ele0Obj;

			Object eleStatus = ele0.get("status");
			log.info("[GMAPS] element status={}", eleStatus);

			if (eleStatus instanceof String && "OK".equals(eleStatus)) {
				Object distObj = ele0.get("distance");
				if (!(distObj instanceof Map))
					return null;
				Object value = ((Map<?, ?>) distObj).get("value");
				if (!(value instanceof Number))
					return null;
				return ((Number) value).intValue();
			}

			// 第二次：備援（先 geocode → 用座標重試）
			String oLL = geocodeToLatLng(o1);
			String dLL = geocodeToLatLng(d1);
			if (oLL == null || dLL == null) {
				log.info("[GMAPS] fallback geocode failed. oLL={}, dLL={}", oLL, dLL);
				return null;
			}

			ResponseEntity<Map> resp2 = callDistanceMatrix(oLL, dLL);
			Map<?, ?> body2 = resp2.getBody();
			if (body2 == null)
				return null;

			Object rows2 = body2.get("rows");
			if (!(rows2 instanceof List))
				return null;
			List<?> r2 = (List<?>) rows2;
			if (r2.isEmpty())
				return null;

			Object ele2 = ((Map<?, ?>) r2.get(0)).get("elements");
			if (!(ele2 instanceof List) || ((List<?>) ele2).isEmpty())
				return null;

			Map<?, ?> ele2Map = (Map<?, ?>) ((List<?>) ele2).get(0);
			Object eleStatus2 = ele2Map.get("status");
			log.info("[GMAPS] element status (fallback)={}", eleStatus2);
			if (!(eleStatus2 instanceof String) || !"OK".equals(eleStatus2))
				return null;

			Object dist2 = ele2Map.get("distance");
			if (!(dist2 instanceof Map))
				return null;
			Object val2 = ((Map<?, ?>) dist2).get("value");
			if (!(val2 instanceof Number))
				return null;

			return ((Number) val2).intValue();
		} catch (Exception e) {
			log.warn("[GMAPS] exception: {}", e.toString());
			return null;
		}
	}

}
