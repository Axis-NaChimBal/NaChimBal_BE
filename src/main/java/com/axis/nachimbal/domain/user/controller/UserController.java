package com.axis.nachimbal.domain.user.controller;

import com.axis.nachimbal.domain.user.dto.UpdateRhrRequest;
import com.axis.nachimbal.domain.user.dto.UpdateRhrResponse;
import com.axis.nachimbal.domain.user.service.UserService;
import com.axis.nachimbal.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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
}