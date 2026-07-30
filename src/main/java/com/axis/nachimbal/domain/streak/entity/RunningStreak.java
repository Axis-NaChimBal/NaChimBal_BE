package com.axis.nachimbal.domain.streak.entity;

import com.axis.nachimbal.domain.session.entity.ExerciseSession;
import com.axis.nachimbal.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "running_streaks",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "exercise_date"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RunningStreak {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "exercise_date", nullable = false)
    private LocalDate exerciseDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ExerciseSession session;

    public static RunningStreak create(User user,
                                       LocalDate exerciseDate,
                                       ExerciseSession session) {
        RunningStreak s = new RunningStreak();
        s.user         = user;
        s.exerciseDate = exerciseDate;
        s.session      = session;
        return s;
    }
}