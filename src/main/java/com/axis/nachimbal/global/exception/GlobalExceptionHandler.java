package com.axis.nachimbal.global.exception;

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
}
