package com.axis.nachimbal.domain.running.entity;

// [ 운동 세션 상태 ]
public enum SessionStatus {
    READY,   // 세션 생성됨 (아직 시작 전)
    ACTIVE,  // 러닝 진행 중
    ENDED    // 러닝 종료
}