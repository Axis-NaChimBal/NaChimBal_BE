package com.axis.nachimbal.domain.result.controller;

import com.axis.nachimbal.domain.result.dto.ResultSaveRequest;
import com.axis.nachimbal.domain.result.dto.ResultSaveResponse;
import com.axis.nachimbal.domain.result.service.ExerciseResultService;
import com.axis.nachimbal.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

// 팀원의 러닝 세션 엔드포인트(/api/running/session) 하위로 편입
@RestController
@RequestMapping("/api/running/session")
@RequiredArgsConstructor
public class ExerciseResultController {

    private final ExerciseResultService resultService;

    // 운동 결과 저장: userId 대신 sessionId를 경로로 받음
    @PostMapping("/{sessionId}/result")
    public ResponseEntity<?> saveResult(
            @PathVariable Long sessionId,
            @RequestBody @Validated ResultSaveRequest req
    ) {
        ResultSaveResponse result = resultService.saveResult(sessionId, req);

        return ResponseEntity.ok(
                ApiResponse.success("SUCCESS_SAVE_RESULT", "운동 결과를 저장했습니다.", result)
        );
    }
}