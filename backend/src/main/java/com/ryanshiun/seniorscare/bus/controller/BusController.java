package com.ryanshiun.seniorscare.bus.controller;

import java.util.List;


import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ryanshiun.seniorscare.bus.dto.BusQueryParams;
import com.ryanshiun.seniorscare.bus.dto.BusRequest;
import com.ryanshiun.seniorscare.bus.model.Rehabus;
import com.ryanshiun.seniorscare.bus.service.BusService;

import jakarta.validation.Valid;


@Validated
@RestController
@RequestMapping("/api/rehabus")
@CrossOrigin(
		originPatterns = "http://localhost:517*",
				methods = {
				        RequestMethod.GET,
				        RequestMethod.POST,
				        RequestMethod.PUT,
				        RequestMethod.DELETE,
				        RequestMethod.OPTIONS,
				        RequestMethod.PATCH
				    },
		allowedHeaders = "*",
		allowCredentials = "true")
public class BusController {

	@Autowired
	private BusService busService;
	
	//查詢復康巴士
	@GetMapping("/findAll")
	public ResponseEntity<List<Rehabus>> listAll() {
		List<Rehabus> list = busService.findAllBus();
		return ResponseEntity.ok(list);
	}
	
	// 查詢復康巴士(根據ID)
	@GetMapping("/{busId}")
	public ResponseEntity<Rehabus> getById(@PathVariable("busId") int busId) {
	    Rehabus fBus = busService.findById(busId);
	    if (fBus == null) {
			return ResponseEntity.notFound().build();
		} else {
			return ResponseEntity.ok(fBus);
		}
	}
	
	//查詢座位數量所對應的復康巴士
	@GetMapping("/search")
	public ResponseEntity<List<Rehabus>> findByFilter(@RequestParam(name = "minSeats",required = false) Integer minSeats,
			@RequestParam(name = "minWheels" ,required = false) Integer minWheels) {
		
		BusQueryParams params = new BusQueryParams();
		params.setMinSeats(minSeats);
		params.setMinWheels(minWheels);
		List<Rehabus> result = busService.findByFilter(params);
		return ResponseEntity.ok(result);
	}
	
	
	//新增復康巴士
	@PostMapping
	public ResponseEntity<Rehabus> insertBus(@RequestBody @Valid BusRequest busRequest) {
		
		//取得新的id
		Integer busId = busService.insertBus(busRequest);
		
		//拿到新的物件
		Rehabus bus = busService.findById(busId);
	    return ResponseEntity.ok(bus);
	}
	
	//刪除巴士資料
	@DeleteMapping("/delete/{busId}")
	public ResponseEntity<Rehabus> deleteBus(@PathVariable("busId") int busId) {
		Integer dBus = busService.deleteBus(busId);
		if (dBus == null) {
			return ResponseEntity.notFound().build();
		} else {
			return ResponseEntity.noContent().build();
			//回傳一個 HTTP 204 No Content 的回應
		}
	}
	
	
	//更新巴士資料
	@PutMapping("/update/{busId}")
	public ResponseEntity<Rehabus> updateBus(@PathVariable("busId") int busId , @RequestBody @Valid BusRequest busRequest) {
		
		busRequest.setBusId(busId);
		Rehabus uBus = busService.updateBus(busRequest);
		
		if (uBus == null) {
			return ResponseEntity.notFound().build();
		} else {
			return ResponseEntity.ok(uBus);
		}
		
		
	}
	
	
	//匯入CSV檔案
	@PostMapping("/importcsv")
	public ResponseEntity<Integer> importFromCsv(@RequestParam("filePath") String csvFilePath) {
		//回傳值為int，讓人知道總共匯入了幾筆
		int count =  busService.importFromCsv(csvFilePath);
		return ResponseEntity.ok(count);
	}
	
	
	
	
}
