package com.axis.nachimbal.domain.streak.repository;

import com.axis.nachimbal.domain.streak.entity.RunningStreak;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface RunningStreakRepository extends JpaRepository<RunningStreak, Long> {
    boolean existsByUserIdAndExerciseDate(Long userId, LocalDate exerciseDate);

    List<RunningStreak> findAllByUserId(Long userId);
}