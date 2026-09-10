package com.axis.nachimbal.domain.chatbot.exception;

// OpenAI API 호출 관련 예외 (429, 타임아웃, 응답 파싱 실패 등 전부 포함)
public class OpenAiApiException extends RuntimeException {

    public OpenAiApiException(String message) {
        super(message);
    }

    public OpenAiApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
