package com.axis.nachimbal.domain.challenge.service;

import com.axis.nachimbal.domain.challenge.dto.GoalSaveRequest;
import com.axis.nachimbal.domain.challenge.dto.GoalSaveResponse;
import com.axis.nachimbal.domain.challenge.entity.ChallengeGoal;
import com.axis.nachimbal.domain.challenge.enums.PeriodType;
import com.axis.nachimbal.domain.challenge.repository.ChallengeGoalRepository;
import com.axis.nachimbal.domain.user.entity.User;
import com.axis.nachimbal.domain.user.repository.UserRepository;
import com.axis.nachimbal.global.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

@Service
@RequiredArgsConstructor
@Transactional
public class ChallengeGoalService {

    private final ChallengeGoalRepository challengeGoalRepository;
    private final UserRepository userRepository;

    // 주간/월간 목표 저장
    public GoalSaveResponse saveGoal(Long userId, GoalSaveRequest req) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("존재하지 않는 유저입니다."));

        // 이미 해당 기간 목표가 있으면 업데이트, 없으면 새로 생성
        ChallengeGoal goal = challengeGoalRepository
                .findByUserIdAndPeriodType(userId, req.getPeriodType())
                .map(existing -> {
                    existing.updateGoalDistance(req.getGoalDistance());
                    return existing;
                })
                .orElseGet(() -> {
                    LocalDate periodStart = LocalDate.now().withDayOfMonth(1);
                    LocalDate periodEnd   = periodStart.with(TemporalAdjusters.lastDayOfMonth());

                    if (PeriodType.WEEKLY.equals(req.getPeriodType())) {
                        periodStart = LocalDate.now().with(DayOfWeek.MONDAY);
                        periodEnd   = periodStart.plusDays(6);
                    }

                    return ChallengeGoal.create(user, req.getPeriodType(),
                            req.getGoalDistance(), periodStart, periodEnd);
                });

        challengeGoalRepository.save(goal);

        return new GoalSaveResponse(goal.getId(), goal.getPeriodType(), goal.getGoalDistance());
    }
}