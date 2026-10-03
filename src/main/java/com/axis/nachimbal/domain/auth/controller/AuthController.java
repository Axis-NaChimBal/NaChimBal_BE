package com.axis.nachimbal.domain.auth.controller;

import com.axis.nachimbal.domain.auth.dto.*;
import com.axis.nachimbal.domain.auth.service.AuthService;
import com.axis.nachimbal.domain.auth.service.EmailVerificationService;
import com.axis.nachimbal.domain.user.enums.AuthProvider;
import com.axis.nachimbal.global.dto.ApiResponse;
import com.axis.nachimbal.global.exception.UnsupportedProviderException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final EmailVerificationService emailVerificationService;

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignUpResponse>> signUp(@Valid @RequestBody SignUpRequest request) {

        SignUpResponse response = authService.signUp(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success("SIGNUP_SUCCESS", "회원가입이 완료되었습니다.", response)
        );
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest request) {

        TokenResponse response = authService.login(request);
        return ResponseEntity.ok(
                ApiResponse.success("LOGIN_SUCCESS", "로그인되었습니다.", response)
        );
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal Long userId) {
        log.debug("[API] POST /api/auth/logout: userId={}", userId);
        authService.logout(userId);
        return ResponseEntity.ok(
                ApiResponse.success("LOGOUT_SUCCESS", "로그아웃되었습니다.", null)
        );
    }

    // 탈퇴
    @DeleteMapping("/withdraw")
    public ResponseEntity<ApiResponse<Void>> withdraw(@AuthenticationPrincipal Long userId) {
        authService.withdraw(userId);
        return ResponseEntity.ok(
                ApiResponse.success("WITHDRAW_SUCCESS", "회원 탈퇴가 완료되었습니다.", null)
        );
    }

    // 토큰 재발급
    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<TokenResponse>> reissue(@Valid @RequestBody ReissueRequest request) {

        TokenResponse response = authService.reissue(request);
        return ResponseEntity.ok(
                ApiResponse.success("TOKEN_REISSUED", "토큰이 재발급되었습니다.", response)
        );
    }

    // 아이디 중복 확인
    @GetMapping("/check-login-id")
    public ResponseEntity<ApiResponse<Boolean>> checkLoginId(@RequestParam("loginId")  String loginId) {

        boolean available = authService.isLoginIdAvailable(loginId);
        return ResponseEntity.ok(
                ApiResponse.success("LOGIN_ID_CHECKED", "아이디 중복 확인이 완료되었습니다.", available)
        );
    }

    // 이메일 중복 확인
    @GetMapping("/check-email")
    public ResponseEntity<ApiResponse<Boolean>> checkEmail(@RequestParam("email")  String email) {

        boolean available = authService.isEmailAvailable(email);
        return ResponseEntity.ok(
                ApiResponse.success("EMAIL_CHECKED", "이메일 중복 확인이 완료되었습니다.", available)
        );
    }

    // 이메일 인증번호 발송
    @PostMapping("/email/send")
    public ResponseEntity<ApiResponse<Void>> sendEmailCode(@Valid @RequestBody EmailSendRequest request) {

        emailVerificationService.sendCode(request.getEmail());
        return ResponseEntity.ok(
                ApiResponse.success("EMAIL_CODE_SENT", "인증번호가 발송되었습니다.", null)
        );
    }

    // 이메일 인증번호 확인
    @PostMapping("/email/verify")
    public ResponseEntity<ApiResponse<Void>> verifyEmailCode(@Valid @RequestBody EmailVerifyRequest request) {

        emailVerificationService.verifyCode(request.getEmail(), request.getCode());
        return ResponseEntity.ok(
                ApiResponse.success("EMAIL_VERIFIED", "이메일 인증이 완료되었습니다.", null)
        );
    }

    // 소셜 로그인
    @PostMapping("/social/{provider}")
    public ResponseEntity<ApiResponse<TokenResponse>> socialLogin(
            @PathVariable String provider,
            @Valid @RequestBody SocialLoginRequest request) {

        AuthProvider authProvider = parseProvider(provider);

        log.debug("[API] POST /api/auth/social/{}", provider);

        TokenResponse response = authService.socialLogin(authProvider, request.getToken());
        return ResponseEntity.ok(
                ApiResponse.success("SOCIAL_LOGIN_SUCCESS", "소셜 로그인되었습니다.", response)
        );
    }

    private AuthProvider parseProvider(String provider) {
        try {
            return AuthProvider.valueOf(provider.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new UnsupportedProviderException("지원하지 않는 소셜 로그인입니다: " + provider);
        }
    }
}