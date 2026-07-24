package com.axis.nachimbal.domain.running.dto;

import com.axis.nachimbal.domain.running.entity.GoalType;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 현재 사용자 인증 미구현 단계이므로 user_id는 임시값 사용
 * 추후 JWT 인증 구현 시 user_id는 토큰에서 추출하도록 변경 필요
 */
// [ 세션 시작 요청 DTO ]
@Getter
@NoArgsConstructor
public class SessionStartRequest {

    // 임시: 인증 미구현 단계 - FE에서 전달하는 사용자 ID
    // 추후 JWT 인증 구현 시 제거 예정
    private Long userId;

    // 운동 목표 코드
    private GoalType goal;

    // TMAP API로 계산된 경로 거리 (km 단위)
    private Double routeDistance;
}
