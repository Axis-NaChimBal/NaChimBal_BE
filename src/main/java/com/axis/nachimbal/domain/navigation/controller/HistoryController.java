package com.axis.nachimbal.domain.navigation.controller;


import com.axis.nachimbal.domain.navigation.dto.RecentPlaceDto;
import com.axis.nachimbal.domain.navigation.service.HistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
public class HistoryController {

    private final HistoryService historyService;

    /**
     * 최근 목적지 조회
     * GET /api/history/recent?limit=4
     *
     * 응답 예시:
     * [
     *   {
     *     "placeId": 1,
     *     "name": "덕성여자대학교",
     *     "address": "서울 도봉구 쌍문동",
     *     "lat": 37.6488,
     *     "lng": 127.0263,
     *     "searchedAt": "2026-03-06T12:00:00"
     *   },
     *   ...
     * ]
     */
    @GetMapping("/recent")
    public ResponseEntity<List<RecentPlaceDto>> getRecentPlaces(
            @RequestParam(name = "limit", defaultValue = "4") int limit
    ) {
        List<RecentPlaceDto> result = historyService.getRecentPlaces(limit);
        return ResponseEntity.ok(result);
    }
}