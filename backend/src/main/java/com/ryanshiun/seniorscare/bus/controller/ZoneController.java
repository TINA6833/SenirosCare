package com.ryanshiun.seniorscare.bus.controller;

import java.util.List;

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
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ryanshiun.seniorscare.bus.dto.ZoneRequest;
import com.ryanshiun.seniorscare.bus.model.FareZone;
import com.ryanshiun.seniorscare.bus.service.ZoneService;

import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/farezone")
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
public class ZoneController {

	@Autowired
	private ZoneService zoneService;

	// 查詢所有行政區
	@GetMapping("/findAll")
	public ResponseEntity<List<FareZone>> listAll() {
		List<FareZone> list = zoneService.findAllZone();
		return ResponseEntity.ok(list);
	}

	// 查詢行政區(根據ID)
	@GetMapping("/{zoneId}")
	public ResponseEntity<FareZone> getById(@PathVariable("zoneId") int zoneId) {

		FareZone fZone = zoneService.findById(zoneId);
		if (fZone == null) {
			return ResponseEntity.notFound().build();

		} else {
			return ResponseEntity.ok(fZone);
		}

	}

	// 查詢行政區(名稱模糊查詢)
	@GetMapping("/search")
	public ResponseEntity<List<FareZone>> findByFilter(
			@RequestParam(name = "zoneName", required = false) String zoneName) {

		List<FareZone> result;
		if (zoneName == null || zoneName.isBlank()) {
			result = zoneService.findAllZone();
		} else {
			result = zoneService.findByFilter(zoneName);
		}
		return ResponseEntity.ok(result);
	}

	// 新增行政區
	@PostMapping
	public ResponseEntity<FareZone> insertZone(@RequestBody @Valid ZoneRequest zoneRequest) {

		// 取得新的id
		Integer zoneId = zoneService.insertZone(zoneRequest);

		// 拿到新的物件
		FareZone zone = zoneService.findById(zoneId);
		return ResponseEntity.ok(zone);

	}

	
	//刪除行政區
	@DeleteMapping("/delete/{zoneId}")
	public ResponseEntity<FareZone> deleteZone(@PathVariable("zoneId") int zoneId) {
		Integer dZone = zoneService.deleteZone(zoneId);
		
		if (dZone == null) {
			return ResponseEntity.notFound().build();
		} else {
            return ResponseEntity.noContent().build();
		}
	}
	
	
	// 更新行政區
	@PutMapping("/update/{zoneId}")
	public ResponseEntity<FareZone> updateZone(@PathVariable("zoneId") int zoneId,
			@RequestBody @Valid ZoneRequest zoneRequest) {

		zoneRequest.setZoneId(zoneId);
		FareZone uZone = zoneService.updateZone(zoneRequest);

		if (uZone == null) {
			return ResponseEntity.notFound().build();

		} else {
			return ResponseEntity.ok(uZone);
		}
	}
}
