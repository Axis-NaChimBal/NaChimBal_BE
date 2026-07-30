package com.axis.nachimbal.domain.result.entity;

import com.axis.nachimbal.domain.running.entity.ExerciseSession; // 팀원이 만든 세션 엔티티를 그대로 참조
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

// [ 운동 결과 Entity ] : ERD 6번 exercise_results, ExerciseSession과 1:1
@Entity
@Table(name = "exercise_results")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExerciseResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false, unique = true)
    private ExerciseSession session;

    @Column(name = "distance_km", nullable = false)
    private Double distanceKm;

    @Column(name = "duration_sec", nullable = false)
    private Integer durationSec;

    @Column(name = "avg_speed_kmh", nullable = false)
    private Double avgSpeedKmh;

    // 심박수/칼로리/페이스조절횟수/GPS트랙은 값 출처 확정 전이라 아직 미포함
    // (avg_heart_rate, max_heart_rate, min_heart_rate, calories_kcal, pace_adjust_count, gps_track)

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static ExerciseResult create(ExerciseSession session,
                                        Double distanceKm,
                                        Integer durationSec,
                                        Double avgSpeedKmh) {
        ExerciseResult r = new ExerciseResult();
        r.session     = session;
        r.distanceKm  = distanceKm;
        r.durationSec = durationSec;
        r.avgSpeedKmh = avgSpeedKmh;
        return r;
    }
}