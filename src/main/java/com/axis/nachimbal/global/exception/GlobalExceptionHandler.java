package com.axis.nachimbal.global.exception;

import com.axis.nachimbal.domain.chatbot.exception.OpenAiApiException;
import com.axis.nachimbal.global.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // [ 아이디 중복 (409) ] - 이미 존재하는 loginId로 가입 시도할 때 발생
    @ExceptionHandler(DuplicateLoginIdException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateLoginId(
            DuplicateLoginIdException e, HttpServletRequest request) {
        log.warn("[Exception] DuplicateLoginIdException: {}", e.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of(
                409,
                request.getRequestURI(),
                "DUPLICATE_LOGIN_ID",
                e.getMessage()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    // [ 이메일 중복 (409) ] - 이미 존재하는 email로 가입 시도할 때 발생
    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateEmail(
            DuplicateEmailException e, HttpServletRequest request) {
        log.warn("[Exception] DuplicateEmailException: {}", e.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of(
                409,
                request.getRequestURI(),
                "DUPLICATE_EMAIL",
                e.getMessage()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    // [ 이메일 인증번호 불일치/만료 (400) ] - 불일치하거나 만료됐을 때 발생
    @ExceptionHandler(InvalidVerificationCodeException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidVerificationCode(
            InvalidVerificationCodeException e, HttpServletRequest request) {
        log.warn("[Exception] InvalidVerificationCodeException: {}", e.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of(
                400,
                request.getRequestURI(),
                "INVALID_VERIFICATION_CODE",
                e.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // [ 이메일 발송 실패 (502) ] - 메일 서버 연결/발송 실패 시 발생
    @ExceptionHandler(EmailSendFailedException.class)
    public ResponseEntity<ApiErrorResponse> handleEmailSendFailed(
            EmailSendFailedException e, HttpServletRequest request) {
        log.warn("[Exception] EmailSendFailedException: {}", e.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of(
                502,
                request.getRequestURI(),
                "EMAIL_SEND_FAILED",
                e.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(response);
    }

    // [ 인증 실패 (401) ] - 아이디·비밀번호 불일치 또는 토큰 검증 실패 시 발생
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidCredentials(
            InvalidCredentialsException e, HttpServletRequest request) {
        log.warn("[Exception] InvalidCredentialsException: {}", e.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of(
                401,
                request.getRequestURI(),
                "INVALID_CREDENTIALS",
                e.getMessage()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    // [ 이메일 인증 미완료 (400) ] - 이메일 인증 절차를 거치지 않고 가입 시도할 때 발생
    @ExceptionHandler(EmailNotVerifiedException.class)
    public ResponseEntity<ApiErrorResponse> handleEmailNotVerified(
            EmailNotVerifiedException e, HttpServletRequest request) {
        log.warn("[Exception] EmailNotVerifiedException: {}", e.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of(
                400,
                request.getRequestURI(),
                "EMAIL_NOT_VERIFIED",
                e.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // [ 요청 값 검증 실패 (400) ] - @Valid가 붙은 DTO에서 검증 조건을 만족 못 할 때 발생(첫 번째 필드 에러 메시지만 반환)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException e, HttpServletRequest request) {

        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse("요청 값이 올바르지 않습니다.");

        log.warn("[Exception] MethodArgumentNotValidException: {}", message);

        ApiErrorResponse response = ApiErrorResponse.of(
                400,
                request.getRequestURI(),
                "INVALID_REQUEST",
                message
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // [ 지원하지 않는 소셜 로그인 provider (400) ] - 유효하지 않은 provider 문자열이 들어올 때 발생
    @ExceptionHandler(UnsupportedProviderException.class)
    public ResponseEntity<ApiErrorResponse> handleUnsupportedProvider(
            UnsupportedProviderException e, HttpServletRequest request) {
        log.warn("[Exception] UnsupportedProviderException: {}", e.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of(
                400,
                request.getRequestURI(),
                "UNSUPPORTED_PROVIDER",
                e.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // [ 소셜 계정 비밀번호 변경 시도 (400) ]
    @ExceptionHandler(SocialAccountException.class)
    public ResponseEntity<ApiErrorResponse> handleSocialAccount(
            SocialAccountException e, HttpServletRequest request) {
        log.warn("[Exception] SocialAccountException: {}", e.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of(
                400,
                request.getRequestURI(),
                "SOCIAL_ACCOUNT_NO_PASSWORD",
                e.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // [ 가입되지 않은 이메일 (404) ] - 아이디 찾기 인증번호 발송/확인 시 발생
    @ExceptionHandler(EmailNotRegisteredException.class)
    public ResponseEntity<ApiErrorResponse> handleEmailNotRegistered(
            EmailNotRegisteredException e, HttpServletRequest request) {
        log.warn("[Exception] EmailNotRegisteredException: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ApiErrorResponse.of(404, request.getRequestURI(), "EMAIL_NOT_REGISTERED", e.getMessage()));
    }

    // [ 아이디·이메일 불일치 (404) ] - 비밀번호 재설정 본인 확인 실패
    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleAccountNotFound(
            AccountNotFoundException e, HttpServletRequest request) {
        log.warn("[Exception] AccountNotFoundException: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ApiErrorResponse.of(404, request.getRequestURI(), "ACCOUNT_NOT_FOUND", e.getMessage()));
    }

    // [ 재설정 토큰 만료/무효 (400) ]
    @ExceptionHandler(InvalidResetTokenException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidResetToken(
            InvalidResetTokenException e, HttpServletRequest request) {
        log.warn("[Exception] InvalidResetTokenException: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiErrorResponse.of(400, request.getRequestURI(), "INVALID_RESET_TOKEN", e.getMessage()));
    }

    // [ 기존과 동일한 비밀번호 (400) ]
    @ExceptionHandler(SamePasswordException.class)
    public ResponseEntity<ApiErrorResponse> handleSamePassword(
            SamePasswordException e, HttpServletRequest request) {
        log.warn("[Exception] SamePasswordException: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiErrorResponse.of(400, request.getRequestURI(), "SAME_PASSWORD", e.getMessage()));
    }

    // [ 인증번호 재요청 제한 (429) ] - 60초 이내 재발송 시도 시 발생
    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<ApiErrorResponse> handleTooManyRequests(
            TooManyRequestsException e, HttpServletRequest request) {
        log.warn("[Exception] TooManyRequestsException: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(
                ApiErrorResponse.of(429, request.getRequestURI(), "TOO_MANY_REQUESTS", e.getMessage()));
    }

    // [ 지원하지 않는 HTTP 메서드 (405) ]
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        log.warn("[Exception] {} {} : {}", request.getMethod(), request.getRequestURI(), e.getMessage());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(
                ApiErrorResponse.of(405, request.getRequestURI(), "METHOD_NOT_ALLOWED", e.getMessage()));
    }

    // [ AI 챗봇 API 호출 오류 (502) ]
    @ExceptionHandler(OpenAiApiException.class)
    public ResponseEntity<ApiErrorResponse> handleOpenAiApiException(OpenAiApiException e, HttpServletRequest request) {
        ApiErrorResponse response = ApiErrorResponse.of(
                502,
                request.getRequestURI(),
                "CHATBOT_UPSTREAM_ERROR",
                "AI 코치 응답을 가져오는 데 실패했습니다. 잠시 후 다시 시도해주세요."
        );
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(response);
    }

    // [ 사용자/세션 조회 실패 (404) ]
    // RunningSessionService에서 userId 또는 sessionId를 찾지 못할 때 발생
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(
            IllegalArgumentException e, HttpServletRequest request) {
        log.warn("[Exception] IllegalArgumentException: {}", e.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of(
                404,
                request.getRequestURI(),
                "NOT_FOUND",
                e.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    // [ 서버 오류 (500) ]
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleException(Exception e, HttpServletRequest request) {
        ApiErrorResponse response = ApiErrorResponse.of(
                500,
                request.getRequestURI(),
                "SERVER_ERROR",
                "서버에 오류가 발생했습니다. 다시 시도해주세요."
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleUserNotFound(UserNotFoundException e, HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiErrorResponse.of(404, request.getRequestURI(), "USER_NOT_FOUND", e.getMessage()));
    }
}
