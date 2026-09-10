package com.axis.nachimbal.domain.chatbot.dto;

import java.util.List;

public record ChatRequestDto(
        String question,
        List<ChatMessageDto> previousMessages   // 프론트에서 최근 10턴만 잘라서 보냄, 없으면 빈 리스트
) {
    public record ChatMessageDto(
            String role,      // "user" 또는 "assistant"
            String content
    ) {}
}