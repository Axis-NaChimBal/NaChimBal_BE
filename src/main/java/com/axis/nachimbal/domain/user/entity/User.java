package com.axis.nachimbal.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    // 나이
    @Column(name = "age", nullable = false)
    private Integer age;

    // 안정 심박수 (bpm) - 최초 측정 전 NULL
    @Column(name = "resting_hr")
    private Integer restingHr;

    // 페이스 조절 ON/OFF
    @Column(name = "pace_control_enabled", nullable = false)
    private Boolean paceControlEnabled = true;

    // 실측 보폭 (m)
    @Column(name = "stride_length")
    private Double strideLength;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public User(String email, String passwordHash, Integer age, Integer restingHr) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.age = age;
        this.restingHr = restingHr;
        this.paceControlEnabled = true;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // 안정 심박수 업데이트 (온보딩 최초 저장 + 마이페이지 재측정 모두 사용)
    public void updateRhr(int restingHr) {
        this.restingHr = restingHr;
    }

    public void updateAge(int age) {
        this.age = age;
    }

    public void updatePaceControl(boolean paceControlEnabled) {
        this.paceControlEnabled = paceControlEnabled;
    }

    // 보폭 업데이트
    public void updateStride(double strideLength) { this.strideLength = strideLength; }
}
