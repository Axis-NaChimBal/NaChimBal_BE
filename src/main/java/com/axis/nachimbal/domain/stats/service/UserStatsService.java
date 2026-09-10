package com.axis.nachimbal.domain.stats.service;

import com.axis.nachimbal.domain.challenge.entity.ChallengeGoal;
import com.axis.nachimbal.domain.challenge.enums.PeriodType;
import com.axis.nachimbal.domain.challenge.repository.ChallengeGoalRepository;
import com.axis.nachimbal.domain.result.entity.ExerciseResult;
import com.axis.nachimbal.domain.result.repository.ExerciseResultRepository;
import com.axis.nachimbal.domain.stats.dto.UserStatsResponse;
import com.axis.nachimbal.domain.streak.entity.RunningStreak;
import com.axis.nachimbal.domain.streak.repository.RunningStreakRepository;
import com.axis.nachimbal.domain.user.entity.User;
import com.axis.nachimbal.domain.user.repository.UserRepository;
import com.axis.nachimbal.global.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserStatsService {

    private final ExerciseResultRepository exerciseResultRepository;
    private final UserRepository userRepository;
    private final RunningStreakRepository streakRepository;
    private final ChallengeGoalRepository challengeGoalRepository;

    // 운동 데이터 조회
    public UserStatsResponse getStats(Long userId, Integer year, Integer month) {

        List<ExerciseResult> results = exerciseResultRepository.findAllBySession_UserId(userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("존재하지 않는 유저입니다."));

        // 전체 누적 기준 값
        double totalDist = results.stream().mapToDouble(ExerciseResult::getDistanceKm).sum();

        // 조회 기준 연/월 결정
        YearMonth targetMonth = (year != null && month != null)
                ? YearMonth.of(year, month)
                : YearMonth.now();
        LocalDate monthStart = targetMonth.atDay(1);
        LocalDate monthEnd   = targetMonth.atEndOfMonth();

        // 해당 연/월 범위에 속하는 기록만 필터링
        List<ExerciseResult> monthlyResults = results.stream()
                .filter(r -> {
                    LocalDate d = r.getSession().getStartedAt().toLocalDate();
                    return !d.isBefore(monthStart) && !d.isAfter(monthEnd);
                })
                .toList();

        // 평균/최고/이번 달 거리 계산
        double avgDist    = monthlyResults.stream().mapToDouble(ExerciseResult::getDistanceKm).average().orElse(0);
        int    avgDur     = (int) monthlyResults.stream().mapToInt(ExerciseResult::getDurationSec).average().orElse(0);
        double avgSpeed   = monthlyResults.stream().mapToDouble(ExerciseResult::getAvgSpeedKmh).average().orElse(0);
        double maxRec     = monthlyResults.stream().mapToDouble(ExerciseResult::getDistanceKm).max().orElse(0);
        double monthlyDist = monthlyResults.stream().mapToDouble(ExerciseResult::getDistanceKm).sum();

        // 월간 목표 달성률
        Optional<ChallengeGoal> monthlyGoalOpt = challengeGoalRepository
                .findByUserIdAndPeriodType(userId, PeriodType.MONTHLY);

        double monthlyGoalPercent = monthlyGoalOpt
                .map(g -> Math.min((monthlyDist / g.getGoalDistance()) * 100, 100))
                .orElse(0.0);

        // 달력 기록
        List<RunningStreak> streakEntities = streakRepository.findAllByUserId(userId);
        List<LocalDate> exerciseDates = streakEntities.stream()
                .map(RunningStreak::getExerciseDate)
                .toList();
        List<String> streaks = exerciseDates.stream().map(LocalDate::toString).toList();

        // 실제 연속 스트릭 계산
        Set<LocalDate> dateSet = new HashSet<>(exerciseDates);
        int currentStreak = 0;
        LocalDate mostRecent = exerciseDates.stream().max(Comparator.naturalOrder()).orElse(null);
        if (mostRecent != null && !mostRecent.isBefore(LocalDate.now().minusDays(1))) {
            LocalDate cursor = mostRecent;
            while (dateSet.contains(cursor)) {
                currentStreak++;
                cursor = cursor.minusDays(1);
            }
        }

        // 목표값
        Double weeklyGoal  = challengeGoalRepository
                .findByUserIdAndPeriodType(userId, PeriodType.WEEKLY)
                .map(ChallengeGoal::getGoalDistance).orElse(null);
        Double monthlyGoal = monthlyGoalOpt.map(ChallengeGoal::getGoalDistance).orElse(null);

        // 주간 목표 달성 여부
        LocalDate today     = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
        LocalDate weekEnd   = weekStart.plusDays(6);

        double weeklyDist = results.stream()
                .filter(r -> {
                    LocalDate d = r.getSession().getStartedAt().toLocalDate();
                    return !d.isBefore(weekStart) && !d.isAfter(weekEnd);
                })
                .mapToDouble(ExerciseResult::getDistanceKm)
                .sum();

        boolean weeklyGoalAchieved = weeklyGoal != null && weeklyDist >= weeklyGoal;

        return UserStatsResponse.builder()
                .age(user.getAge())
                .paceControlEnabled(user.getPaceControlEnabled())
                .restingHr(user.getRestingHr())
                .monthlyGoalPercent(Math.round(monthlyGoalPercent * 10.0) / 10.0)
                .avgDistance(Math.round(avgDist * 10.0) / 10.0)
                .avgDurationSec(avgDur)
                .avgSpeed(Math.round(avgSpeed * 10.0) / 10.0)
                .maxRecord(Math.round(maxRec * 10.0) / 10.0)
                .totalDistance(Math.round(totalDist * 10.0) / 10.0)
                .streaks(streaks)
                .weeklyGoal(weeklyGoal)
                .monthlyGoal(monthlyGoal)
                .weeklyDistance(Math.round(weeklyDist * 10.0) / 10.0)
                .weeklyGoalAchieved(weeklyGoalAchieved)
                .streakDays(currentStreak)
                .build();
    }
}