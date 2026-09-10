package com.axis.nachimbal.global.exception;

import com.axis.nachimbal.domain.chatbot.exception.OpenAiApiException;
import com.axis.nachimbal.global.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

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
