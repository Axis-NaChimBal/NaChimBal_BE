package com.axis.nachimbal.domain.stats.service;

import com.axis.nachimbal.domain.challenge.entity.ChallengeGoal;
import com.axis.nachimbal.domain.challenge.enums.PeriodType;
import com.axis.nachimbal.domain.challenge.repository.ChallengeGoalRepository;
import com.axis.nachimbal.domain.session.entity.ExerciseSession;
import com.axis.nachimbal.domain.session.repository.ExerciseSessionRepository;
import com.axis.nachimbal.domain.stats.dto.UserStatsResponse;
import com.axis.nachimbal.domain.streak.repository.RunningStreakRepository;
import com.axis.nachimbal.domain.user.entity.User;
import com.axis.nachimbal.domain.user.repository.UserRepository;
import com.axis.nachimbal.global.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserStatsService {

    private final ExerciseSessionRepository exerciseSessionRepository;
    private final UserRepository userRepository;
    private final RunningStreakRepository streakRepository;
    private final ChallengeGoalRepository challengeGoalRepository;

    // 운동 데이터 조회
    public UserStatsResponse getStats(Long userId) {

        List<ExerciseSession> results = exerciseSessionRepository.findAllByUserId(userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("존재하지 않는 유저입니다."));

        // 평균/최고 계산
        double avgDist  = results.stream().mapToDouble(ExerciseSession::getDistance).average().orElse(0);
        int    avgDur   = (int) results.stream().mapToInt(ExerciseSession::getDurationSec).average().orElse(0);
        double avgSpeed = results.stream().mapToDouble(ExerciseSession::getAvgSpeed).average().orElse(0);
        double maxRec   = results.stream().mapToDouble(ExerciseSession::getDistance).max().orElse(0);
        double totalDist = results.stream().mapToDouble(ExerciseSession::getDistance).sum();

        // 이번 달 달린 거리
        YearMonth now        = YearMonth.now();
        LocalDate monthStart = now.atDay(1);
        LocalDate monthEnd   = now.atEndOfMonth();

        double monthlyDist = results.stream()
                .filter(r -> {
                    LocalDate d = r.getStartedAt().toLocalDate();
                    return !d.isBefore(monthStart) && !d.isAfter(monthEnd);
                })
                .mapToDouble(ExerciseSession::getDistance)
                .sum();

        // 월간 목표 달성률
        Optional<ChallengeGoal> monthlyGoalOpt = challengeGoalRepository
                .findByUserIdAndPeriodType(userId, PeriodType.MONTHLY);

        double monthlyGoalPercent = monthlyGoalOpt
                .map(g -> Math.min((monthlyDist / g.getGoalDistance()) * 100, 100))
                .orElse(0.0);

        // 달력 기록
        List<String> streaks = streakRepository.findAllByUserId(userId)
                .stream()
                .map(s -> s.getExerciseDate().toString())
                .toList();

        // 목표값
        Double weeklyGoal  = challengeGoalRepository
                .findByUserIdAndPeriodType(userId, PeriodType.WEEKLY)
                .map(ChallengeGoal::getGoalDistance).orElse(null);
        Double monthlyGoal = monthlyGoalOpt
                .map(ChallengeGoal::getGoalDistance).orElse(null);

        return UserStatsResponse.builder()
                .age(user.getAge())
                .paceControlEnabled(user.isPaceControlEnabled())
                .monthlyGoalPercent(Math.round(monthlyGoalPercent * 10.0) / 10.0)
                .avgDistance(Math.round(avgDist  * 10.0) / 10.0)
                .avgDurationSec(avgDur)
                .avgSpeed(Math.round(avgSpeed * 10.0) / 10.0)
                .maxRecord(Math.round(maxRec * 10.0) / 10.0)
                .totalDistance(Math.round(totalDist * 10.0) / 10.0)
                .streaks(streaks)
                .weeklyGoal(weeklyGoal)
                .monthlyGoal(monthlyGoal)
                .build();
    }
}