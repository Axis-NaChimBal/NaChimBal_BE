package com.axis.nachimbal.domain.gpsart.controller;

import com.axis.nachimbal.domain.gpsart.dto.GpsArtRequest;
import com.axis.nachimbal.domain.gpsart.dto.GpsArtResponse;
import com.axis.nachimbal.domain.gpsart.service.GpsArtService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 프론트 GpsArtService.js가 호출하는 주소: POST /api/routes/gps-art
@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
public class GpsArtController {

    private final GpsArtService gpsArtService;

    @PostMapping("/gps-art")
    public GpsArtResponse generateGpsArt(@RequestBody GpsArtRequest request) {
        return gpsArtService.generate(request);
    }
}