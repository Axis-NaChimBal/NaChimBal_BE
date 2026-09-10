package com.axis.nachimbal.domain.chatbot.client;

import com.axis.nachimbal.domain.chatbot.exception.OpenAiApiException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OpenAiClient {

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper = new ObjectMapper(); // 빈 주입 대신 직접 생성

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.model}")
    private String model;

    @Value("${openai.base-url}")
    private String baseUrl;

    // messages: [{role: "system"/"user"/"assistant", content: "..."}] 형태
    public String getChatCompletion(List<Map<String, String>> messages) {
        WebClient client = webClientBuilder.build();

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", messages
        );

        try {
            String rawResponse = client.post()
                    .uri(baseUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(status -> status.value() >= 400, response ->
                            response.bodyToMono(String.class).map(body ->
                                    new OpenAiApiException("OpenAI API 에러 응답: " + body)
                            )
                    )
                    .bodyToMono(String.class)
                    .block();

            JsonNode root = objectMapper.readTree(rawResponse);
            return root.path("choices").get(0).path("message").path("content").asText();

        } catch (OpenAiApiException e) {
            throw e; // 이미 우리 예외 타입이면 그대로 던짐
        } catch (Exception e) {
            throw new OpenAiApiException("OpenAI API 호출 실패: " + e.getMessage(), e);
        }
    }
}