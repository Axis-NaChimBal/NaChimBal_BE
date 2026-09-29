package com.axis.nachimbal.domain.user.entity;

import com.axis.nachimbal.domain.user.enums.AuthProvider;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import com.axis.nachimbal.domain.running.entity.GoalType;

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

    @Column(name = "login_id", nullable = false, unique = true, length = 20)
    private String loginId;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash") // 소셜 로그인 유저는 null
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 20)
    private AuthProvider provider = AuthProvider.LOCAL;

    @Column(name = "provider_id")
    private String providerId; // 소셜 로그인 시 발급받는 고유 식별자, 로컬 가입 시 null

    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified = false;

    // 나이
    @Column(name = "age")
    private Integer age;

    // 안정 심박수 (bpm) - 최초 측정 전 NULL
    @Column(name = "resting_hr")
    private Integer restingHr;

    // 페이스 조절 ON/OFF
    @Column(name = "pace_control_enabled", nullable = false)
    private Boolean paceControlEnabled = true;

    // 운동 목표 (설정 화면에서 미리 선택, AI 페이스 조절 모드에 사용)
    @Enumerated(EnumType.STRING)
    @Column(name = "exercise_goal", length = 30)
    private GoalType exerciseGoal;

    // 실측 보폭 (m)
    @Column(name = "stride_length")
    private Double strideLength;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public User(String loginId, String email, String passwordHash, AuthProvider provider,
                String providerId) {
        this.loginId = loginId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.provider = provider != null ? provider : AuthProvider.LOCAL;
        this.providerId = providerId;
        this.emailVerified = false;
        this.paceControlEnabled = true;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // 이메일 인증 완료 처리
    public void verifyEmail() {
        this.emailVerified = true;
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

    // 운동목표 업데이트
    public void updateExerciseGoal(GoalType exerciseGoal) { this.exerciseGoal = exerciseGoal; }

    // 보폭 업데이트
    public void updateStride(double strideLength) { this.strideLength = strideLength; }

    public void updatePasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
}