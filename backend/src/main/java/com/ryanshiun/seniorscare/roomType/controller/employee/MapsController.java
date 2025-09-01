package com.ryanshiun.seniorscare.roomType.controller.employee;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/room-types/maps") //可以使用
public class MapsController {

    @Value("${app.place.name:SeniorsCare 養老院}")
    private String name;

    @Value("${app.place.address:台北市信義區市府路45號}")
    private String address;

    @Value("${app.place.lat:25.0375167}")
    private double lat;

    @Value("${app.place.lng:121.5637}")
    private double lng;

    @GetMapping(value = "/place", produces = "application/json; charset=UTF-8")
    public Map<String, Object> getPlace() {
        return Map.of("name", name, "address", address, "lat", lat, "lng", lng);
    }
}