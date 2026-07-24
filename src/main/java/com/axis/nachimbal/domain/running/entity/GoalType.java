package com.axis.nachimbal.domain.running.entity;

// [ 운동 목표 코드 ] : FE의 GoalSelectModal에서 전달되는 code 값과 일치해야 함
public enum GoalType {
    INJURY_RECOVERY,  // 부상 회복
    BEGINNER,         // 초보
    DIET,             // 다이어트
    ENDURANCE,        // 지구력
    SPEED_UP,         // 속도 향상
    INTERVAL,         // 인터벌
    MARATHON_PACE,    // 마라톤 페이스
    SPRINT            // 단거리 스프린트
}
