package com.axis.nachimbal.domain.session.entity;

import com.axis.nachimbal.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "exercise_sessions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExerciseSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "distance", nullable = false)
    private Double distance;

    @Column(name = "duration_sec", nullable = false)
    private Integer durationSec;

    @Column(name = "avg_speed", nullable = false)
    private Double avgSpeed;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static ExerciseSession create(User user,
                                         LocalDateTime startedAt,
                                         LocalDateTime endedAt,
                                         double distance,
                                         int durationSec,
                                         double avgSpeed) {
        ExerciseSession s = new ExerciseSession();
        s.user        = user;
        s.startedAt   = startedAt;
        s.endedAt     = endedAt;
        s.distance  = distance;
        s.durationSec = durationSec;
        s.avgSpeed = avgSpeed;
        return s;
    }
}