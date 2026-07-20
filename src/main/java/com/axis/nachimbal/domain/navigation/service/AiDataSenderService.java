package com.axis.nachimbal.domain.navigation.service;

import com.axis.nachimbal.domain.navigation.dto.AiDataRequest;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiDataSenderService {

    private final RestTemplate restTemplate; // HTTP 요청 도구

    @Value("${ai.server.url}")  // application.properties에서 값 읽어옴
    private String aiServerUrl;

    public void sendNavigationData(AiDataRequest dto) {

        String endpoint = aiServerUrl + "/analyze";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<AiDataRequest> request = new HttpEntity<>(dto, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    endpoint, request, String.class
            );
            log.info("AI 서버 응답: {}", response.getBody()); // 응답 로그
        } catch (Exception e) {
            log.error("AI 서버 전송 실패: {}", e.getMessage()); // 실패 로그
        }
    }
}