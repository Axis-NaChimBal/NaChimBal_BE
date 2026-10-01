package com.axis.nachimbal.domain.running.dto;

import com.axis.nachimbal.domain.running.entity.GoalType;
import lombok.Getter;
import lombok.NoArgsConstructor;

// [ 세션 시작 요청 DTO ]
@Getter
@NoArgsConstructor
public class SessionStartRequest {

    // 운동 목표 코드
    private GoalType goal;

    // TMAP API로 계산된 경로 거리 (km 단위)
    private Double routeDistance;

    //목표페이스메이커에서 사용
    private Double targetSpeedKmh;

    // 페이스 모드 — "AI" | "TARGET" | "NONE". AI 서버 세션 등록 여부 판단에 사용.
    private String paceMode;
}