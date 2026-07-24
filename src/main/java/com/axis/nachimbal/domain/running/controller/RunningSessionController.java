package com.axis.nachimbal.domain.running.controller;

import com.axis.nachimbal.domain.running.dto.SessionEndRequest;
import com.axis.nachimbal.domain.running.dto.SessionStartRequest;
import com.axis.nachimbal.domain.running.dto.SessionStartResponse;
import com.axis.nachimbal.domain.running.service.RunningSessionService;
import com.axis.nachimbal.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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
            @RequestBody SessionStartRequest request) {

        log.debug("[API] POST /api/running/session/start: userId={} goal={} routeDistance={}",
                request.getUserId(), request.getGoal(), request.getRouteDistance());

        SessionStartResponse response = runningSessionService.startSession(request);
        return ResponseEntity.ok(
                ApiResponse.success("SESSION_STARTED", "세션이 시작되었습니다.", response)
        );
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
