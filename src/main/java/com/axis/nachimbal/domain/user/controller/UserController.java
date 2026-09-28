package com.axis.nachimbal.domain.user.controller;

import com.axis.nachimbal.domain.user.dto.UpdateAgeRequest;
import com.axis.nachimbal.domain.user.dto.UpdatePaceControlRequest;
import com.axis.nachimbal.domain.user.dto.UpdateExerciseGoalRequest;
import com.axis.nachimbal.domain.user.dto.UpdateRhrRequest;
import com.axis.nachimbal.domain.user.dto.UpdateRhrResponse;
import com.axis.nachimbal.domain.user.dto.UpdateStrideRequest;
import com.axis.nachimbal.domain.user.service.UserService;
import com.axis.nachimbal.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // [ 안정 심박수 업데이트 API ]
    @PutMapping("/rhr")
    public ResponseEntity<ApiResponse<UpdateRhrResponse>> updateRhr(
            @RequestBody UpdateRhrRequest request) {

        log.debug("[API] PUT /api/user/rhr: userId={} restingHr={}",
                request.getUserId(), request.getRestingHr());

        UpdateRhrResponse response = userService.updateRhr(request);
        return ResponseEntity.ok(
                ApiResponse.success("RHR_UPDATED", "안정 심박수가 저장되었습니다.", response)
        );
    }

    // 나이 업데이트
    @PatchMapping("/{userId}/age")
    public ResponseEntity<?> updateAge(
            @PathVariable Long userId,
            @RequestBody @Validated UpdateAgeRequest req
    ) {
        Integer result = userService.updateAge(userId, req);

        return ResponseEntity.ok(
                ApiResponse.success("SUCCESS_SAVE_AGE", "나이를 저장했습니다.", "age = " + result)
        );
    }

    // 페이스 조절 업데이트
    @PatchMapping("/{userId}/paceControl")
    public ResponseEntity<?> updatePaceControl(
            @PathVariable Long userId,
            @RequestBody @Validated UpdatePaceControlRequest req
    ) {
        Boolean result = userService.updatePaceControl(userId, req);

        return ResponseEntity.ok(
                ApiResponse.success("SUCCESS_SAVE_PACE_CONTROL", "페이스 조절 여부를 저장했습니다.", "페이스 조절 = " + result)
        );
    }

    // 운동목표 업데이트
     @PatchMapping("/{userId}/exerciseGoal")
    public ResponseEntity<?> updateExerciseGoal(
            @PathVariable("userId") Long userId,
            @RequestBody @Validated UpdateExerciseGoalRequest req
    ) { String result = userService.updateExerciseGoal(userId, req);
        return ResponseEntity.ok(ApiResponse.success("SUCCESS_SAVE_EXERCISE_GOAL", "운동목표를 저장했습니다.", "운동목표 = " + result)
        );
    }

    // 보폭 업데이트 API
    @PutMapping("/stride")
    public ResponseEntity<ApiResponse<Void>> updateStride(
            @RequestBody UpdateStrideRequest request) {
        log.debug("[API] PUT /api/user/stride: userId={} strideLength={}",
                request.getUserId(), request.getStrideLength());
        userService.updateStride(request);
        return ResponseEntity.ok(
                ApiResponse.success("STRIDE_UPDATED", "보폭이 저장되었습니다.")
        );
    }
}