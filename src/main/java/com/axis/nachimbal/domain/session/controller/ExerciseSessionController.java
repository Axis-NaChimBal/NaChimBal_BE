package com.axis.nachimbal.domain.session.controller;

import com.axis.nachimbal.domain.session.dto.SessionSaveRequest;
import com.axis.nachimbal.domain.session.dto.SessionSaveResponse;
import com.axis.nachimbal.domain.session.service.ExerciseSessionService;
import com.axis.nachimbal.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class ExerciseSessionController {

    private final ExerciseSessionService sessionService;

    // 운동 데이터 저장
    @PostMapping("/{userId}")
    public ResponseEntity<?> saveSession(
            @PathVariable Long userId,
            @RequestBody @Validated SessionSaveRequest req
    ) {
        SessionSaveResponse result = sessionService.saveSession(userId, req);

        return ResponseEntity.ok(
                ApiResponse.success("SUCCESS_SAVE_SESSION", "운동 데이터를 저장했습니다.", result)
        );
    }
}