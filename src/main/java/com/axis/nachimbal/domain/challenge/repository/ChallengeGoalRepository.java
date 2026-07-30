package com.axis.nachimbal.domain.challenge.repository;

import com.axis.nachimbal.domain.challenge.entity.ChallengeGoal;
import com.axis.nachimbal.domain.challenge.enums.PeriodType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChallengeGoalRepository extends JpaRepository<ChallengeGoal, Long> {
    Optional<ChallengeGoal> findByUserIdAndPeriodType(Long userId, PeriodType periodType);
}