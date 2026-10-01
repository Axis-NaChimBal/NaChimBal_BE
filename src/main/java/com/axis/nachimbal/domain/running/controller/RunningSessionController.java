package com.axis.nachimbal.domain.running.controller;

import com.axis.nachimbal.domain.running.dto.SessionDataRequest;
import com.axis.nachimbal.domain.running.dto.SessionDataResponse;
import com.axis.nachimbal.domain.running.dto.SessionEndRequest;
import com.axis.nachimbal.domain.running.dto.SessionStartRequest;
import com.axis.nachimbal.domain.running.dto.SessionStartResponse;
import com.axis.nachimbal.domain.running.service.RunningSessionService;
import com.axis.nachimbal.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/running/session")
@RequiredArgsConstructor
public class RunningSessionController {

    private final RunningSessionService runningSessionService;

    // [ 세션 시작 API ]
    @PostMapping("/start")
    public ResponseEntity<ApiResponse<SessionStartResponse>> startSession(
            @AuthenticationPrincipal Long userId,
            @RequestBody SessionStartRequest request) {

        log.debug("[API] POST /api/running/session/start: userId={} goal={} routeDistance={}",
                userId, request.getGoal(), request.getRouteDistance());

        SessionStartResponse response = runningSessionService.startSession(userId, request);
        return ResponseEntity.ok(
                ApiResponse.success("SESSION_STARTED", "세션이 시작되었습니다.", response)
        );
    }

    // [ 러닝 중 데이터 처리 API ]
    // 주의: FE가 ApiResponse 래핑 없이 label/idealPace를 최상위에서 바로 파싱하므로
    //       SessionDataResponse를 그대로 반환 (ApiResponse로 감싸지 않음)
    @PostMapping("/data")
    public ResponseEntity<SessionDataResponse> processSessionData(
            @RequestBody SessionDataRequest request) {

        log.debug("[API] POST /api/running/session/data: sessionId={} currentElapsed={} windowSize={}",
                request.getSessionId(), request.getCurrentElapsed(), request.getWindow().size());

        SessionDataResponse response = runningSessionService.processSessionData(request);
        return ResponseEntity.ok(response);
    }

    // [ 세션 종료 API ]
    @PostMapping("/end")
    public ResponseEntity<ApiResponse<Void>> endSession(
            @RequestBody SessionEndRequest request) {

        log.debug("[API] POST /api/running/session/end: sessionId={} totalElapsed={}",
                request.getSessionId(), request.getTotalElapsed());

        runningSessionService.endSession(request);
        return ResponseEntity.ok(
                ApiResponse.success("SESSION_ENDED", "세션이 종료되었습니다.")
        );
    }
}