package com.axis.nachimbal.domain.challenge.controller;

import com.axis.nachimbal.domain.challenge.dto.GoalSaveRequest;
import com.axis.nachimbal.domain.challenge.dto.GoalSaveResponse;
import com.axis.nachimbal.domain.challenge.service.ChallengeGoalService;
import com.axis.nachimbal.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
public class ChallengeGoalController {

    private final ChallengeGoalService challengeGoalService;

    // 주간/월간 목표 저장
    @PostMapping
    public ResponseEntity<?> saveGoal(
            @AuthenticationPrincipal Long userId,
            @RequestBody @Validated GoalSaveRequest req
    ) {
        GoalSaveResponse result = challengeGoalService.saveGoal(userId, req);

        return ResponseEntity.ok(
                ApiResponse.success("SUCCESS_SAVE_GOAL", "목표를 저장했습니다.", result)
        );
    }
}