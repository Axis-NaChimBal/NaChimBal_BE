package com.axis.nachimbal.domain.stats.controller;

import com.axis.nachimbal.domain.stats.dto.UserStatsResponse;
import com.axis.nachimbal.domain.stats.service.UserStatsService;
import com.axis.nachimbal.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class UserStatsController {

    private final UserStatsService statsService;

    // 운동 데이터 조회
    @GetMapping("/{userId}")
    public ResponseEntity<?> getStats(@PathVariable Long userId) {
        UserStatsResponse result = statsService.getStats(userId);

        return ResponseEntity.ok(
                ApiResponse.success("SUCCESS_GET_STATS", "운동 데이터를 조회했습니다.", result)
        );
    }
}