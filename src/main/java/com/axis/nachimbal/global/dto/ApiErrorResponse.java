package com.axis.nachimbal.global.dto;

import lombok.*;
import java.time.Instant;

@Getter
@Builder

public class ApiErrorResponse {
    private String timestamp;
    private int status;
    private String error;
    private String path;
    private String code;
    private String message;

    public static ApiErrorResponse of(int status, String path, String code, String message) {
        return ApiErrorResponse.builder()
                .timestamp(Instant.now().toString())
                .status(status)
                .error(getErrorName(status))
                .path(path)
                .code(code)
                .message(message)
                .build();
    }

    private static String getErrorName(int status) {
        return switch (status) {
            case 400 -> "Bad Request";
            case 401 -> "Unauthorized";
            case 403 -> "Forbidden";
            case 404 -> "Not Found";
            case 500 -> "Internal Server Error";
            default -> "Unknown Error";
        };
    }
}
