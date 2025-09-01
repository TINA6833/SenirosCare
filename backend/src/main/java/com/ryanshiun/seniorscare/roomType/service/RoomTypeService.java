package com.ryanshiun.seniorscare.roomType.service;

import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ryanshiun.seniorscare.roomType.dto.RoomTypeForm;
import com.ryanshiun.seniorscare.roomType.model.RoomType;

public interface RoomTypeService {


	    // ====== Create ======
	    Integer addRoomType(RoomType room);
	    Integer addRoomWithImage(RoomTypeForm form);

	    // ====== Read ======
	    List<RoomType> getAllRoomTypes();
	    RoomType getRoomTypeById(int id);

	    // ====== Update / Delete ======
	    boolean updateRoomType(RoomType room);
	    boolean deleteRoomType(int id);

	    // ====== Queries ======
	    List<RoomType> getRoomTypesByPriceRange(int min, int max);
	    List<RoomType> getRoomTypesByDescriptionKeyword(String keyword);
	    List<RoomType> getRoomTypesByCapacity(int capacity);

	    // ====== Pagination ======
	    List<RoomType> getRoomTypesPaged(int page, int size);
	    int countAllRoomTypes();

	    // ====== CSV (Path) ======
	    void importRoomTypesFromCSV(String csvPath);

	    // ====== CSV (Multipart) ======
	    void exportToCSV(PrintWriter writer);
	    void importFromCSV(MultipartFile file);

	    // ====== Partial Update ======
	    boolean partialUpdate(int id, Map<String, Object> updates);
	    boolean partialUpdate(int id, RoomTypeForm form);
	    
	 // RoomTypeService.java（介面擴充）
	    List<RoomType> searchFiltered(
	            String keyword, Integer minPrice, Integer maxPrice, Integer capacity,
	            List<Integer> featureIds, boolean matchAll,
	            String sortBy, String order, int page, int size);

	    int countFiltered(
	            String keyword, Integer minPrice, Integer maxPrice, Integer capacity,
	            List<Integer> featureIds, boolean matchAll);
	    //圖片處理
	    void updateImage(int id, MultipartFile img);
	    void updateImagePath(int id, String imagePath);
		/** 新增：用表單建（不含圖片處理） */
		Integer addRoomType(RoomTypeForm form);
	}