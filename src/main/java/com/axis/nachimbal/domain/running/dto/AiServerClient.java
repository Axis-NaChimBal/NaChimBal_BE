package com.axis.nachimbal.domain.running.client;

import com.axis.nachimbal.domain.running.dto.SessionDataRequest;
import com.axis.nachimbal.domain.running.dto.ai.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiServerClient {

    private final RestTemplate restTemplate;

    @Value("${ai.server.url}")
    private String aiServerUrl;

    // [ 세션 시작 시 AI 서버에 파라미터 등록 ]
    public void initSession(Long sessionId, double restingHr, int age, double targetDuration) {
        AiSessionStartRequest request = AiSessionStartRequest.builder()
                .sessionId(String.valueOf(sessionId))
                .restingHr(restingHr)
                .age(age)
                .targetDuration(targetDuration)
                .build();

        try {
            restTemplate.postForObject(aiServerUrl + "/session/start", request, Void.class);
            log.debug("[AI] 세션 등록 성공: sessionId={}", sessionId);
        } catch (Exception e) {
            log.warn("[AI] 세션 등록 실패: sessionId={} error={}", sessionId, e.getMessage());
        }
    }

    // [ 페이스 추론 요청 ]
    public AiPredictResponse predict(Long sessionId, List<SessionDataRequest.WindowItem> window) {
        List<AiWindowItem> aiWindow = window.stream()
                .map(w -> AiWindowItem.builder()
                        .heartRate(w.getHeartRate() != null ? w.getHeartRate().doubleValue() : 0.0)
                        .speed(w.getSpeed())
                        .altitude(w.getAltitude())
                        .elapsedTime(w.getT() != null ? w.getT().doubleValue() : 0.0)
                        .build())
                .collect(Collectors.toList());

        AiPredictRequest request = AiPredictRequest.builder()
                .sessionId(String.valueOf(sessionId))
                .window(aiWindow)
                .build();

        try {
            AiPredictResponse response = restTemplate.postForObject(
                    aiServerUrl + "/predict", request, AiPredictResponse.class
            );
            log.debug("[AI] 응답 수신: sessionId={} label={}", sessionId, response.getLabel());
            return response;
        } catch (Exception e) {
            log.warn("[AI] 호출 실패, 기본값 반환: sessionId={} error={}", sessionId, e.getMessage());
            return null; // Service에서 null 처리
        }
    }

    // [ 세션 종료 ]
    public void endSession(Long sessionId) {
        try {
            restTemplate.delete(aiServerUrl + "/session/" + sessionId);
            log.debug("[AI] 세션 삭제 성공: sessionId={}", sessionId);
        } catch (Exception e) {
            log.warn("[AI] 세션 삭제 실패: sessionId={} error={}", sessionId, e.getMessage());
        }
    }
}