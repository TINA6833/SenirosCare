package com.ryanshiun.seniorscare.bus.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ryanshiun.seniorscare.bus.dto.PriceRequest;
import com.ryanshiun.seniorscare.bus.model.FarePrice;
import com.ryanshiun.seniorscare.bus.service.PriceService;

import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/fareprice")
public class PriceController {

	@Autowired
	private PriceService priceService;

	// 新增票價
	@PostMapping
	public ResponseEntity<FarePrice> insertPrice(@RequestBody @Valid PriceRequest priceRequest) {

		// 取得新的id
		Integer priceId = priceService.insertPrice(priceRequest);

		// 拿到新的物件
		FarePrice price = priceService.findById(priceId);
		return ResponseEntity.ok(price);
	}

	// 刪除票價
	@DeleteMapping("/delete/{priceId}")
	public ResponseEntity<Void> deletePrice(@PathVariable("priceId") int priceId) {
		Integer dPrice = priceService.deletePrice(priceId);

		if (dPrice == 0) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.noContent().build();
		// 回傳一個 HTTP 204 No Content 的回應
	}

	// 更新票價
	@PutMapping("/update/{priceId}")
	public ResponseEntity<FarePrice> updatePrice(@PathVariable("priceId") Integer priceId,
			@RequestBody @Valid PriceRequest priceRequest) {

		priceRequest.setPriceId(priceId);
		FarePrice uPrice = priceService.updatePrice(priceRequest);

		if (uPrice == null) {
			return ResponseEntity.notFound().build();
		} else {
			return ResponseEntity.ok(uPrice);
		}
	}

	// 查詢全部票價
	@GetMapping("/findAll")
	public ResponseEntity<List<FarePrice>> listAll() {
		List<FarePrice> list = priceService.findAll();
		return ResponseEntity.ok(list);
	}

	// 查詢票價(根據ID)
	@GetMapping("/{priceId}")
	public ResponseEntity<FarePrice> findById(@PathVariable("priceId") int priceId) {
		FarePrice fPrice = priceService.findById(priceId);

		if (fPrice == null) {
			return ResponseEntity.notFound().build();
		} else {
			return ResponseEntity.ok(fPrice);
		}

	}

	// 依起迄行政區查票價（票價試算用）
	@GetMapping("/zone/{fromZone}/to/{toZone}")
	public ResponseEntity<FarePrice> getPriceByZone(@PathVariable("fromZone") int fromZone,
			@PathVariable("toZone") int toZone) {
		FarePrice tPrice = priceService.getPriceByZone(fromZone, toZone);

		if (tPrice == null) {
			return ResponseEntity.notFound().build();

		} else {
			return ResponseEntity.ok(tPrice);
		}
	}

	// 依起點查全部票價
	@GetMapping("/from/{fromZone}")
	public ResponseEntity<List<FarePrice>> getPricesByFromZone(@PathVariable("fromZone") int fromZone) {
		List<FarePrice> list = priceService.getPricesByFromZone(fromZone);

		// 無資料也會回 200 + 空陣列
		return ResponseEntity.ok(list);
	}

	// 切換票價狀態(上架/下架)
	@PatchMapping("/{priceId}/status")
	public ResponseEntity<Void> changeStatus(@PathVariable("priceId") int priceId,
			@RequestParam("priceStatus") String priceStatus) {

		// 這裡如果 priceId 不存在，Service 會拋例外，可讓 Spring 自動處理成 404
		priceService.updatePriceStatus(priceId, priceStatus);

		// 只回 204 No Content
		return ResponseEntity.noContent().build();
	}

}
