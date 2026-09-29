package com.axis.nachimbal.domain.auth.controller;

import com.axis.nachimbal.domain.auth.dto.*;
import com.axis.nachimbal.domain.auth.service.AccountRecoveryService;
import com.axis.nachimbal.global.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AccountRecoveryController {

    private final AccountRecoveryService accountRecoveryService;

    // 아이디 찾기 - 인증번호 발송
    @PostMapping("/find-id/send")
    public ResponseEntity<ApiResponse<Void>> sendFindIdCode(@Valid @RequestBody EmailSendRequest request) {
        accountRecoveryService.sendFindIdCode(request.getEmail());
        return ResponseEntity.ok(
                ApiResponse.success("FIND_ID_CODE_SENT", "인증번호가 발송되었습니다.", null));
    }

    // 아이디 찾기 - 인증번호 확인 + 결과 반환
    @PostMapping("/find-id/verify")
    public ResponseEntity<ApiResponse<FindIdResponse>> verifyFindId(@Valid @RequestBody EmailVerifyRequest request) {
        FindIdResponse response = accountRecoveryService.verifyFindId(request.getEmail(), request.getCode());
        return ResponseEntity.ok(
                ApiResponse.success("FIND_ID_SUCCESS", "인증이 완료되었습니다.", response));
    }

    // 비밀번호 재설정 - 인증번호 발송
    @PostMapping("/password-reset/send")
    public ResponseEntity<ApiResponse<Void>> sendPasswordResetCode(@Valid @RequestBody PasswordResetSendRequest request) {
        accountRecoveryService.sendPasswordResetCode(request.getLoginId(), request.getEmail());
        return ResponseEntity.ok(
                ApiResponse.success("PASSWORD_RESET_CODE_SENT", "인증번호가 발송되었습니다.", null));
    }

    // 비밀번호 재설정 - 인증번호 확인 + resetToken 발급
    @PostMapping("/password-reset/verify")
    public ResponseEntity<ApiResponse<PasswordResetVerifyResponse>> verifyPasswordReset(
            @Valid @RequestBody PasswordResetVerifyRequest request) {
        PasswordResetVerifyResponse response = accountRecoveryService.verifyPasswordReset(
                request.getLoginId(), request.getEmail(), request.getCode());
        return ResponseEntity.ok(
                ApiResponse.success("PASSWORD_RESET_VERIFIED", "인증이 완료되었습니다.", response));
    }

    // 비밀번호 재설정 - 새 비밀번호 확정
    @PostMapping("/password-reset/confirm")
    public ResponseEntity<ApiResponse<Void>> confirmPasswordReset(
            @Valid @RequestBody PasswordResetConfirmRequest request) {
        accountRecoveryService.confirmPasswordReset(request.getResetToken(), request.getNewPassword());
        return ResponseEntity.ok(
                ApiResponse.success("PASSWORD_RESET_SUCCESS", "비밀번호가 변경되었습니다.", null));
    }
}