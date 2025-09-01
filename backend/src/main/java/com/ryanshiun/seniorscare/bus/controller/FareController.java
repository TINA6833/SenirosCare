package com.ryanshiun.seniorscare.bus.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ryanshiun.seniorscare.bus.dto.FareQuote;
import com.ryanshiun.seniorscare.bus.service.FareService;

import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequestMapping("/api/fare")
@RequiredArgsConstructor
@CrossOrigin(originPatterns = "http://localhost:517*", methods = { RequestMethod.GET, RequestMethod.POST,
		RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS,
		RequestMethod.PATCH }, allowedHeaders = "*", allowCredentials = "true")
public class FareController {

	private final FareService fareService;

	// 查尋點到點的車費(計程車原價和復康巴士費用)
	@GetMapping("/quote")
	public ResponseEntity<?> quote(@RequestParam("origin") String origin,
			@RequestParam("destination") String destination) {

		FareQuote fareQuote = fareService.quoteByAddresses(origin, destination);
		if (fareQuote == null) {

			// 回錯誤訊息
			return ResponseEntity.badRequest()
					.body(Map.of("error", "distance_lookup_failed", "origin", origin, "destination", destination));
		}
		return ResponseEntity.ok(fareQuote);

	}

}
