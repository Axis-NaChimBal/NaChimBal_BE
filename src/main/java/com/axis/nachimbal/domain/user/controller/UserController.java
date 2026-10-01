package com.axis.nachimbal.domain.user.controller;

import com.axis.nachimbal.domain.user.dto.*;
import com.axis.nachimbal.domain.user.service.UserService;
import com.axis.nachimbal.global.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
            @AuthenticationPrincipal Long userId,
            @RequestBody UpdateRhrRequest request) {

        log.debug("[API] PUT /api/user/rhr: userId={} restingHr={}",
                userId, request.getRestingHr());

        UpdateRhrResponse response = userService.updateRhr(userId, request);
        return ResponseEntity.ok(
                ApiResponse.success("RHR_UPDATED", "안정 심박수가 저장되었습니다.", response)
        );
    }

    // 나이 업데이트
    @PatchMapping("/age")
    public ResponseEntity<?> updateAge(
            @AuthenticationPrincipal Long userId,
            @RequestBody @Validated UpdateAgeRequest req
    ) {
        Integer result = userService.updateAge(userId, req);

        return ResponseEntity.ok(
                ApiResponse.success("SUCCESS_SAVE_AGE", "나이를 저장했습니다.", "age = " + result)
        );
    }

    // 페이스 조절 업데이트
    @PatchMapping("/paceControl")
    public ResponseEntity<?> updatePaceControl(
            @AuthenticationPrincipal Long userId,
            @RequestBody @Validated UpdatePaceControlRequest req
    ) {
        Boolean result = userService.updatePaceControl(userId, req);

        return ResponseEntity.ok(
                ApiResponse.success("SUCCESS_SAVE_PACE_CONTROL", "페이스 조절 여부를 저장했습니다.", "페이스 조절 = " + result)
        );
    }

    // 운동목표 업데이트
    @PatchMapping("/exerciseGoal")
    public ResponseEntity<?> updateExerciseGoal(
            @AuthenticationPrincipal Long userId,
            @RequestBody @Validated UpdateExerciseGoalRequest req
    ) {
        String result = userService.updateExerciseGoal(userId, req);
        return ResponseEntity.ok(
                ApiResponse.success("SUCCESS_SAVE_EXERCISE_GOAL", "운동목표를 저장했습니다.", "운동목표 = " + result)
        );
    }

    // 보폭 업데이트 API
    @PutMapping("/stride")
    public ResponseEntity<ApiResponse<Void>> updateStride(
            @AuthenticationPrincipal Long userId,
            @RequestBody UpdateStrideRequest request) {
        log.debug("[API] PUT /api/user/stride: userId={} strideLength={}",
                userId, request.getStrideLength());
        userService.updateStride(userId, request);
        return ResponseEntity.ok(
                ApiResponse.success("STRIDE_UPDATED", "보폭이 저장되었습니다.")
        );
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MyInfoResponse>> getMyInfo(@AuthenticationPrincipal Long userId) {
        MyInfoResponse response = userService.getMyInfo(userId);

        return ResponseEntity.ok(
                ApiResponse.success("MY_INFO_FETCHED", "내 정보 조회가 완료되었습니다.", response)
        );
    }

    @PutMapping("/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(userId, request.getNewPassword());

        return ResponseEntity.ok(
                ApiResponse.success("PASSWORD_CHANGED", "비밀번호가 변경되었습니다.", null)
        );
    }
}