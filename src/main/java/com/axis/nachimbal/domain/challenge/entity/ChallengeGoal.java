package com.axis.nachimbal.domain.challenge.entity;

import com.axis.nachimbal.domain.challenge.enums.PeriodType;
import com.axis.nachimbal.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "challenge_goal")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChallengeGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "period_type", nullable = false)
    private PeriodType periodType;

    @Column(name = "goal_distance", nullable = false)
    private Double goalDistance;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    public static ChallengeGoal create(User user,
                                       PeriodType periodType,
                                       double goalDistance,
                                       LocalDate periodStart,
                                       LocalDate periodEnd) {
        ChallengeGoal g = new ChallengeGoal();
        g.user        = user;
        g.periodType  = periodType;
        g.goalDistance = goalDistance;
        g.periodStart = periodStart;
        g.periodEnd   = periodEnd;
        return g;
    }

    public void updateGoalDistance(double goalDistance) {
        this.goalDistance = goalDistance;
    }
}
