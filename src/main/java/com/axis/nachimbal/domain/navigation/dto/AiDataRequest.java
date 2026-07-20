package com.axis.nachimbal.domain.navigation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiDataRequest {

    private String sessionId;         // 세션 구분용

    private Long reactionTime;        // 반응 시점 (ms 타임스탬프)
    private Long vibrationTime;       // 진동 발생 시점
    private Long directionChangeTime; // 실제 방향 전환 시점

    private Boolean isOffRoute;       // 경로 이탈 여부

    private Double avgSpeed;          // 평균 이동 속도 (km/h)

    private LocalDateTime recordedAt; // 기록 시각
}