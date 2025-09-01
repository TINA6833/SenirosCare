package com.ryanshiun.seniorscare.bus.service.Impl;

import java.time.LocalDate;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import com.ryanshiun.seniorscare.bus.utils.GoogleMapsClient;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;

import com.ryanshiun.seniorscare.bus.fare.FareCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.ryanshiun.seniorscare.bus.dao.ReservationDAO;
import com.ryanshiun.seniorscare.bus.dto.ResCreateRequest;
import com.ryanshiun.seniorscare.bus.dto.ResQueryParams;
import com.ryanshiun.seniorscare.bus.dto.ResRequest;
import com.ryanshiun.seniorscare.bus.model.BusReservation;

import com.ryanshiun.seniorscare.bus.model.Rehabus;
import com.ryanshiun.seniorscare.bus.service.BusService;
import com.ryanshiun.seniorscare.bus.service.ResService;
import com.ryanshiun.seniorscare.bus.utils.TimeUtils;

@Component
public class ResServiceImpl implements ResService {

	@Autowired
	private ReservationDAO reservationDAO;
	@Autowired
	private GoogleMapsClient googleMapsClient;
	@Autowired
	private BusService busService;

	// 小工具：狀態標準化（去空白、小寫）與雙語對應
	private String norm(String word) {
		return word == null ? "" : word.trim().toLowerCase();
	}

	private boolean isMaintenance(String status) {
		String word = norm(status);
		return "維修中".equals(word) || "maintenance".equals(word);
	}


	public ResServiceImpl(ReservationDAO reservationDAO, BusService busService) {
		this.reservationDAO = reservationDAO;
		this.busService = busService;
	}

	// 新增預約表單（加入「時段重疊」檢查）
	@Override
	public Integer insertRes(ResCreateRequest resCreateRequest) {

		// 基本檢查：起訖地址必填
		if (resCreateRequest.getStartAddress() == null || resCreateRequest.getStartAddress().isBlank()
				|| resCreateRequest.getEndAddress() == null || resCreateRequest.getEndAddress().isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "請提供有效的起點與終點地址");
		}

		// 取 busId，查詢巴士狀態，只在「維修中/maintenance」時擋下
		int busId = resCreateRequest.getBusId();
		Rehabus bus = busService.findById(busId);
		if (bus == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "找不到指定的巴士編號（id=" + busId + "）");
		}

		String status = bus.getStatus();
		if (isMaintenance(status)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "該巴士目前狀態為「維修中」，無法預約");
		}

		// 計算距離（呼叫 Google Maps API）
		Integer distanceMeters = googleMapsClient.getDrivingDistanceMeters(resCreateRequest.getStartAddress(),
				resCreateRequest.getEndAddress());

		if (distanceMeters == null) {
			throw new IllegalStateException("無法計算距離，請檢查地址是否正確");
		}

		// 計算價格
		int taxiFare = FareCalculator.calcTaxiFareByTYMeters(distanceMeters);
		int price = FareCalculator.calcRehabusFareFormTaxi(taxiFare);

		LocalDateTime createdAt = TimeUtils.taipeiNowMinute();
		LocalDateTime scheduledAt = TimeUtils.truncateToMinute(resCreateRequest.getScheduledAt());
		String reservationStatus = "Active";

		// 送入前做「時段重疊」檢查（固定時間片長，依你的需求調整）
		int slotMinutes = 120; // 例：每單固定 2 小時
		if (reservationDAO.hasConflict(resCreateRequest.getBusId(), scheduledAt, slotMinutes)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "該時段已被預約");
		}

		return reservationDAO.insertRes(resCreateRequest, createdAt, scheduledAt, reservationStatus, distanceMeters,
				price);

	}

	// 刪除預約表單
	@Override
	public Integer deleteRes(int id) {
		return reservationDAO.deleteRes(id);
	}

	// 修改預約表單
	@Override
	public BusReservation updateRes(ResRequest resRequest) {

		// 先取舊資料用來比對是否真的改地址
		BusReservation old = reservationDAO.findById(resRequest.getId());
		if (old == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "找不到要更新的預約單");
		}

		boolean startChanged = resRequest.getStartAddress() != null && !resRequest.getStartAddress().isBlank()
				&& !resRequest.getStartAddress().equals(old.getStartAddress());

		boolean endChanged = resRequest.getEndAddress() != null && !resRequest.getEndAddress().isBlank()
				&& !resRequest.getEndAddress().equals(old.getEndAddress());

		boolean addressChanged = startChanged || endChanged;

		Integer distanceMetersForUpdate = null;
		Integer priceForUpdate = null;

		if (addressChanged) {
			// 用本次要更新的「最終起訖」來算距離（沒帶的新地址就沿用舊值）
			String origin = startChanged ? resRequest.getStartAddress() : old.getStartAddress();
			String destination = endChanged ? resRequest.getEndAddress() : old.getEndAddress();

			Integer dist = googleMapsClient.getDrivingDistanceMeters(origin, destination);
			if (dist == null) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "無法計算距離，請確認新地址是否正確");
			}
			distanceMetersForUpdate = dist;

			int taxiFare = FareCalculator.calcTaxiFareByTYMeters(dist);
			priceForUpdate = FareCalculator.calcRehabusFareFormTaxi(taxiFare);

		}

		return reservationDAO.updateRes(resRequest, distanceMetersForUpdate, priceForUpdate);
	}

	// 查詢所有預約表單
	@Override
	public List<BusReservation> findAllRes() {
		return reservationDAO.findAllRes();
	}

	// 查詢預約表單(根據ID)
	@Override
	public BusReservation findById(int id) {
		return reservationDAO.findById(id);
	}

	// 查詢預約表單(根據指定地點、預約時間、會員ID)
	@Override
	public List<BusReservation> findByFilter(ResQueryParams resQueryParams) {

		Integer memberId = resQueryParams.getMemberId();
		String startAddress = resQueryParams.getStartAddress();
		String endAddress = resQueryParams.getEndAddress();

		// 這兩個是方法內的「區域變數」，不是 DTO 欄位
		LocalDateTime from = null;
		LocalDateTime to = null;

		// 只有 scheduledAt（到秒）：用「該分鐘」作查詢窗
		// e.g. 2025-08-20T02:30:15 -> [2025-08-20T02:30:00 , 2025-08-20T02:31:00)

		if (resQueryParams.getScheduledAt() != null) {
			LocalDateTime ts = resQueryParams.getScheduledAt();
			from = ts.truncatedTo(ChronoUnit.MINUTES);
			to = from.plusMinutes(1);
		} else if (resQueryParams.getScheduledDate() != null) {
			LocalDate date = resQueryParams.getScheduledDate();
			from = date.atStartOfDay();
			to = date.plusDays(1).atStartOfDay();
		}

		return reservationDAO.findByFilter(memberId, startAddress, endAddress, from, to);
	}

	// 已完乘，自動放入時間
	@Override
	@Transactional
	public Map<String, Object> markCompleted(int id) {

		LocalDateTime now = LocalDateTime.now();
		int rows = reservationDAO.markCompleted(id, now);
		if (rows == 0) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found: id=" + id);
		}
		// 更新後查一次，把完整資料回傳
		return reservationDAO.findViewById(id);
	}

	@Override
	public Map<String, Object> findViewById(int id) {
		try {
			return reservationDAO.findViewById(id);
		} catch (EmptyResultDataAccessException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found: id=" + id);

		}

	}

}
