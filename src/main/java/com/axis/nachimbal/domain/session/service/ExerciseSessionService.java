package com.axis.nachimbal.domain.session.service;

import com.axis.nachimbal.domain.session.dto.SessionSaveRequest;
import com.axis.nachimbal.domain.session.dto.SessionSaveResponse;
import com.axis.nachimbal.domain.session.entity.ExerciseSession;
import com.axis.nachimbal.domain.session.repository.ExerciseSessionRepository;
import com.axis.nachimbal.domain.streak.entity.RunningStreak;
import com.axis.nachimbal.domain.streak.repository.RunningStreakRepository;
import com.axis.nachimbal.domain.user.entity.User;
import com.axis.nachimbal.domain.user.repository.UserRepository;
import com.axis.nachimbal.global.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class ExerciseSessionService {

    private final ExerciseSessionRepository sessionRepository;
    private final RunningStreakRepository streakRepository;
    private final UserRepository userRepository;

    // 운동 데이터 저장
    public SessionSaveResponse saveSession(Long userId, SessionSaveRequest req) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("존재하지 않는 유저입니다."));

        LocalDateTime startedAt = LocalDateTime.parse(req.getStartedAt());
        LocalDateTime endedAt   = LocalDateTime.parse(req.getEndedAt());

        // 세션 저장
        ExerciseSession session = ExerciseSession.create(
                user,
                startedAt,
                endedAt,
                req.getDistance(),
                req.getDurationSec(),
                req.getAvgSpeed()
        );
        sessionRepository.save(session);

        // 달력 기록 여부 확인
        LocalDate exerciseDate = startedAt.toLocalDate();
        boolean alreadyRecorded = streakRepository
                .existsByUserIdAndExerciseDate(userId, exerciseDate);

        if (!alreadyRecorded) {
            RunningStreak streak = RunningStreak.create(user, exerciseDate, session);
            streakRepository.save(streak);
        }

        return new SessionSaveResponse(
                session.getId(),
                session.getStartedAt().toString(),
                session.getEndedAt().toString(),
                session.getDistance(),
                session.getDurationSec(),
                session.getAvgSpeed()
        );    }
}