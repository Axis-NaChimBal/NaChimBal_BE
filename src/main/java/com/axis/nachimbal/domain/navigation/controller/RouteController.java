package com.axis.nachimbal.domain.navigation.controller;

import com.axis.nachimbal.domain.navigation.dto.RouteRequest;
import com.axis.nachimbal.domain.navigation.dto.RouteResponse;
import com.axis.nachimbal.domain.navigation.service.RouteService;
import com.axis.nachimbal.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/nav")
@RequiredArgsConstructor
@Slf4j
public class RouteController {

    private final RouteService tmapService;

    @PostMapping("/routes")
    public ResponseEntity<?> getPedestrianRoute(@RequestBody RouteRequest request) {
        RouteResponse result = tmapService.getPedestrianRoute(request);

        return ResponseEntity.ok(
                ApiResponse.success("SUCCESS_GET_TMAP_API", "경로 조회 성공", result)
        );
    }
}
