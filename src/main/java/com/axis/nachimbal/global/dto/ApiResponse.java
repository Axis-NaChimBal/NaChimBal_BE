package com.axis.nachimbal.global.dto;

import lombok.*;

@Getter
@AllArgsConstructor
@Builder

public class ApiResponse<T> {
    private int status;
    private String code;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(String code, String message, T data) {
        return ApiResponse.<T>builder()
                .status(200)
                .code(code)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(String message) {
        return success("SUCCESS", message, null);
    }

    public static <T> ApiResponse<T> success(String code, String message) {
        return success(code, message, null);
    }

    public static <T> ApiResponse<T> error(String code, String message, int status) {
        return ApiResponse.<T>builder()
                .status(status)
                .code(code)
                .message(message)
                .data(null)
                .build();
    }
}
