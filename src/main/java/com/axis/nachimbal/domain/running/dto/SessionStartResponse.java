package com.axis.nachimbal.domain.running.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

// [ 세션 시작 응답 DTO ]
@Getter
@AllArgsConstructor
public class SessionStartResponse {

    // 세션 ID (DB의 exercise_sessions.id)
    private Long sessionId;

    // 예상 운동 시간 (초) - goal + route_distance 기반 계산값: FE에서 예상 도착 시간 화면 표시용으로 사용
    private Integer totalTime;

    private String status;
}
