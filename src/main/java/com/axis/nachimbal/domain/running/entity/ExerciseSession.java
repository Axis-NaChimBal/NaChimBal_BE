package com.axis.nachimbal.domain.running.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// [ 운동 세션 Entity ] : 러닝 세션의 시작~종료 관리
@Entity
@Table(name = "exercise_sessions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExerciseSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 사용자 ID (users 테이블 참조)
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // 사용한 경로 ID (아카이빙 경로 선택 시 연결, 새로 생성한 경로면 null)
    @Column(name = "route_id")
    private Long routeId;

    // 운동 목표
    @Enumerated(EnumType.STRING)
    @Column(name = "exercise_goal", length = 30)
    private GoalType exerciseGoal;

    // 경로 거리 (km) - total_time 계산 및 AI 전달용
    @Column(name = "route_distance_km")
    private Double routeDistanceKm;

    // 예상 운동 시간 (초) - goal + route_distance 기반 계산
    @Column(name = "total_time_sec")
    private Integer totalTimeSec;

    // 목표 속도 (km/h) - TARGET 모드일 때만 값 존재
    @Column(name = "target_speed_kmh")
    private Double targetSpeedKmh;

    // 세션 상태: READY → ACTIVE → ENDED
    @Enumerated(EnumType.STRING)
    @Column(name = "session_status", nullable = false, length = 10)
    private SessionStatus sessionStatus;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Builder
    public ExerciseSession(Long userId, Long routeId, GoalType exerciseGoal,
                           Double routeDistanceKm, Integer totalTimeSec, Double targetSpeedKmh) {
        this.userId = userId;
        this.routeId = routeId;
        this.exerciseGoal = exerciseGoal;
        this.routeDistanceKm = routeDistanceKm;
        this.totalTimeSec = totalTimeSec;
        this.targetSpeedKmh = targetSpeedKmh;
        this.sessionStatus = SessionStatus.ACTIVE;
    }

    @PrePersist
    protected void onCreate() {
        this.startedAt = LocalDateTime.now();
    }

    // 세션 종료 처리
    public void end() {
        this.sessionStatus = SessionStatus.ENDED;
        this.endedAt = LocalDateTime.now();
    }
}
