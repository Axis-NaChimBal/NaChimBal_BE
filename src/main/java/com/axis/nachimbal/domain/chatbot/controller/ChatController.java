package com.axis.nachimbal.domain.chatbot.controller;

import com.axis.nachimbal.domain.chatbot.dto.ChatRequestDto;
import com.axis.nachimbal.domain.chatbot.dto.ChatResponseDto;
import com.axis.nachimbal.domain.chatbot.service.ChatService;
import com.axis.nachimbal.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/{userId}")
    public ResponseEntity<?> chat(
            @PathVariable("userId") Long userId,
            @RequestBody ChatRequestDto request
    ) {
        ChatResponseDto result = chatService.chat(userId, request);

        return ResponseEntity.ok(
                ApiResponse.success("SUCCESS_CHAT", "챗봇 응답을 생성했습니다.", result)
        );
    }
}