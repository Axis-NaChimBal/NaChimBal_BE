package com.axis.nachimbal.domain.running.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

// [ 러닝 중 데이터 처리 응답 DTO ]
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionDataResponse {

    // 세션 ID
    private Long sessionId;

    // 페이스 판단 결과: OVER / NORMAL / UNDER
    private String label;

    // 이상적인 페이스 (min/km)
    private Double idealPace;

    // AI 판단 신뢰도 (0~1)
    private Double confidence;

    // 연속 동일 판단 횟수
    private Integer consecutiveCount;
}