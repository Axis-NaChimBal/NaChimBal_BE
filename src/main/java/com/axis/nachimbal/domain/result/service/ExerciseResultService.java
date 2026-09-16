package com.axis.nachimbal.domain.result.service;

import com.axis.nachimbal.domain.result.dto.ResultSaveRequest;
import com.axis.nachimbal.domain.result.dto.ResultSaveResponse;
import com.axis.nachimbal.domain.result.entity.ExerciseResult;
import com.axis.nachimbal.domain.result.repository.ExerciseResultRepository;
import com.axis.nachimbal.domain.running.entity.ExerciseSession;
import com.axis.nachimbal.domain.running.repository.ExerciseSessionRepository;
import com.axis.nachimbal.domain.streak.entity.RunningStreak;
import com.axis.nachimbal.domain.streak.repository.RunningStreakRepository;
import com.axis.nachimbal.domain.user.entity.User;
import com.axis.nachimbal.domain.user.repository.UserRepository;
import com.axis.nachimbal.global.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional
public class ExerciseResultService {

    private final ExerciseResultRepository resultRepository;
    private final ExerciseSessionRepository sessionRepository; // 세션 신규 생성이 아니라 "조회" 용도로만 사용
    private final RunningStreakRepository streakRepository;
    private final UserRepository userRepository;

    // 운동 결과 저장 (userId 대신 sessionId를 받음)
    public ResultSaveResponse saveResult(Long sessionId, ResultSaveRequest req) {

        // ① userId로 유저 찾아서 세션 새로 만들던 것 → sessionId로 "이미 존재하는" 세션 조회
        ExerciseSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "세션을 찾을 수 없습니다. sessionId=" + sessionId));

        // ② 새 세션 생성이 아니라, 조회한 세션에 결과만 연결
        ExerciseResult result = ExerciseResult.create(
                session,
                req.getDistance(),
                req.getDurationSec(),
                req.getAvgSpeed()
        );

        // 심박수 / 칼로리 / 페이스 조절 횟수 반영
        result.updateVitals(
                req.getAvgHeartRate(),
                req.getMaxHeartRate(),
                req.getMinHeartRate(),
                req.getCaloriesKcal()
        );
        result.updatePaceAdjustCount(req.getPaceAdjustCount());

        resultRepository.save(result);

        // ③ 스트릭 기록: userId/exerciseDate를 요청값이 아니라 session에서 그대로 꺼내 씀
        Long userId = session.getUserId();
        LocalDate exerciseDate = session.getStartedAt().toLocalDate();

        boolean alreadyRecorded = streakRepository
                .existsByUserIdAndExerciseDate(userId, exerciseDate);

        if (!alreadyRecorded) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("존재하지 않는 유저입니다."));
            // 주의: RunningStreak.create()의 session 파라미터 타입이 아직
            //       domain.session.entity.ExerciseSession으로 되어 있다면
            //       domain.running.entity.ExerciseSession으로 import 수정 필요
            RunningStreak streak = RunningStreak.create(user, exerciseDate, session);
            streakRepository.save(streak);
        }

        return new ResultSaveResponse(
                sessionId,
                result.getDistanceKm(),
                result.getDurationSec(),
                result.getAvgSpeedKmh(),
                result.getAvgHeartRate(),
                result.getMaxHeartRate(),
                result.getMinHeartRate(),
                result.getCaloriesKcal(),
                result.getPaceAdjustCount()
        );
    }
}