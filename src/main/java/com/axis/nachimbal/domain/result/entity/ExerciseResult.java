package com.axis.nachimbal.domain.result.entity;

import com.axis.nachimbal.domain.running.entity.ExerciseSession;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

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

    @Column(name = "avg_heart_rate", nullable = false)
    private Integer avgHeartRate;

    @Column(name = "max_heart_rate", nullable = false)
    private Integer maxHeartRate;

    @Column(name = "min_heart_rate", nullable = false)
    private Integer minHeartRate;

    @Column(name = "calories_kcal", nullable = false)
    private Double caloriesKcal;

    @Column(name = "pace_adjust_count", nullable = false)
    private Integer paceAdjustCount;

    @Column(name = "gps_track", columnDefinition = "TEXT")
    private String gpsTrack;

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

    // 심박수/칼로리 등 나중에 채워넣을 때 쓸 setter 메서드
    public void updateVitals(Integer avgHeartRate, Integer maxHeartRate,
                             Integer minHeartRate, Double caloriesKcal) {
        this.avgHeartRate = avgHeartRate;
        this.maxHeartRate = maxHeartRate;
        this.minHeartRate = minHeartRate;
        this.caloriesKcal = caloriesKcal;
    }

    // 페이스 조절 횟수 저장용 setter
    public void updatePaceAdjustCount(Integer paceAdjustCount) {
        this.paceAdjustCount = paceAdjustCount;
    }
}