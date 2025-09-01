package com.ryanshiun.seniorscare.bus.controller;


import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.ryanshiun.seniorscare.bus.dto.ResCreateRequest;
import com.ryanshiun.seniorscare.bus.dto.ResQueryParams;
import com.ryanshiun.seniorscare.bus.dto.ResRequest;
import com.ryanshiun.seniorscare.bus.model.BusReservation;
import com.ryanshiun.seniorscare.bus.service.ResService;

import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/reservation")
public class ResController {

	@Autowired
	private ResService resService;

	// 新增預約表單
	@PostMapping
	public ResponseEntity<BusReservation> insertRes(
			@RequestBody @Valid ResCreateRequest resCreateRequest,
			Authentication authentication) {
		int memberId = Integer.parseInt(authentication.getName());
		System.out.println("當前登入者 ID: " + memberId);
		resCreateRequest.setMemberId(memberId);
		// 取得新的id
		int id = resService.insertRes(resCreateRequest);

		// 拿到新的物件

		BusReservation busReservation = resService.findById(id);
		return ResponseEntity.ok(busReservation);
	}

	// 刪除預約表單
	@DeleteMapping("/delete/{id}")
	public ResponseEntity<BusReservation> deleteRes(@PathVariable("id") int id) {
		Integer dRes = resService.deleteRes(id);
		if (dRes == 0) {
			return ResponseEntity.notFound().build();
		} else {
			return ResponseEntity.noContent().build();
			// 回傳一個 HTTP 204 No Content 的回應
		}
	}

	// 修改預約表單
	@PutMapping("/update/{id}")
	public ResponseEntity<BusReservation> updateRes(@PathVariable("id") int id,

			@RequestBody @Valid ResRequest resRequest) {


		resRequest.setId(id);
		BusReservation uRes = resService.updateRes(resRequest);

		if (uRes == null) {
			return ResponseEntity.notFound().build();
		} else {
			return ResponseEntity.ok(uRes);
		}
	}

	// 查詢所有預約表單
	@GetMapping("/findAll")
	public ResponseEntity<List<BusReservation>> listAll() {
		List<BusReservation> list = resService.findAllRes();
		return ResponseEntity.ok(list);
	}

	// 查詢預約表單(根據ID)
	@GetMapping("/{id}")
	public ResponseEntity<BusReservation> getById(@PathVariable("id") int id) {
		BusReservation fRes = resService.findById(id);
		if (fRes == null) {
			return ResponseEntity.notFound().build();
		} else {
			return ResponseEntity.ok(fRes);
		}

	}

	// 查詢預約表單(根據指定地點、預約時間、會員ID)
	@GetMapping("/search")
	public ResponseEntity<List<BusReservation>> findByFilter(@ModelAttribute ResQueryParams resQueryParams) {

		List<BusReservation> list = resService.findByFilter(resQueryParams);
		if (list.isEmpty()) {
			return ResponseEntity.noContent().build();
		} else {
			return ResponseEntity.ok(list);
		}
	}

	// 已完乘，自動放入時間
	@PatchMapping("/{id}/complete")
	public ResponseEntity<Map<String, Object>> complete(@PathVariable int id) {

		Map<String, Object> view = resService.markCompleted(id);
		return ResponseEntity.ok(view);
	}

	@GetMapping("/view/{id}")
	public ResponseEntity<Map<String, Object>> view(@PathVariable("id") int id) {
		return ResponseEntity.ok(resService.findViewById(id));

	}

}
