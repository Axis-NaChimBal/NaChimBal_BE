package com.axis.nachimbal.domain.running.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

// [ 러닝 중 데이터 처리 요청 DTO ]
@Getter
@NoArgsConstructor
public class SessionDataRequest {

    // 세션 ID (exercise_sessions.id)
    private Long sessionId;

    // 현재 경과 시간 (초)
    private Integer currentElapsed;

    // 최근 120초치 데이터 배열 (최대 24개, 5초 간격)
    private List<WindowItem> window;

    // [ 슬라이딩 윈도우 개별 데이터 ]
    @Getter
    @NoArgsConstructor
    public static class WindowItem {

        // 경과 시간 (초, 러닝 시작 기준)
        private Integer t;

        // 심박수 (BPM, 이동평균값)
        @JsonProperty("heart_rate")
        private Integer heartRate;

        // 속도 (m/s, GPS)
        private Double speed;

        // 고도 (m, GPS)
        private Double altitude;
    }
}