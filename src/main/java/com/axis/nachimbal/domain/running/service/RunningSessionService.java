package com.axis.nachimbal.domain.running.service;

import com.axis.nachimbal.domain.running.dto.SessionEndRequest;
import com.axis.nachimbal.domain.running.dto.SessionStartRequest;
import com.axis.nachimbal.domain.running.dto.SessionStartResponse;
import com.axis.nachimbal.domain.running.entity.ExerciseSession;
import com.axis.nachimbal.domain.running.entity.GoalType;
import com.axis.nachimbal.domain.running.repository.ExerciseSessionRepository;
import com.axis.nachimbal.domain.user.entity.User;
import com.axis.nachimbal.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RunningSessionService {

    private final UserRepository userRepository;
    private final ExerciseSessionRepository exerciseSessionRepository;

    // [ 목표별 기준 페이스 (sec/km) ]
    // total_time(초) = route_distance(km) × pace(sec/km)
    private static double getSpeedMps(GoalType goal) {
        return switch (goal) {
            case INJURY_RECOVERY -> 2.08; // Zone 1, 약 8:00 min/km
            case BEGINNER        -> 2.38; // Zone 2, 약 7:00 min/km
            case DIET            -> 2.56; // Zone 2, 약 6:30 min/km
            case ENDURANCE       -> 2.78; // Zone 3, 약 6:00 min/km
            case MARATHON_PACE   -> 3.03; // Zone 3, 약 5:30 min/km
            case SPEED_UP        -> 3.33; // Zone 4, 약 5:00 min/km
            case INTERVAL        -> 3.70; // Zone 4, 약 4:30 min/km
            case SPRINT          -> 4.76; // Zone 5, 약 3:30 min/km
        };
    }

    // [ 세션 시작 ]
    @Transactional
    public SessionStartResponse startSession(SessionStartRequest request) {

        // 1. 사용자 조회 (hr_rest, age 필요)
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "사용자를 찾을 수 없습니다. userId=" + request.getUserId()));

        // 2. total_time(초) = (route_distance_km × 1000) / speed_mps
        double speedMps  = getSpeedMps(request.getGoal());
        int totalTimeSec = (int) Math.round(
                (request.getRouteDistance() * 1000) / speedMps
        );

        int hrRest = user.getRestingHr() != null ? user.getRestingHr() : 60;
        log.debug("[Session] userId={} age={} hrRest={} goal={} routeDistance={}km " +
                        "speedMps={} totalTimeSec={}",
                user.getId(), user.getAge(), hrRest,
                request.getGoal(), request.getRouteDistance(),
                speedMps, totalTimeSec);

        // 3. 세션 생성 및 DB 저장
        ExerciseSession session = ExerciseSession.builder()
                .userId(user.getId())
                .exerciseGoal(request.getGoal())
                .routeDistanceKm(request.getRouteDistance())
                .totalTimeSec(totalTimeSec)
                .build();

        ExerciseSession saved = exerciseSessionRepository.save(session);

        log.info("[Session] 세션 시작: sessionId={} userId={} goal={} totalTimeSec={}",
                saved.getId(), user.getId(), request.getGoal(), totalTimeSec);

        // 4. FE에 sessionId + totalTime 반환
        return new SessionStartResponse(saved.getId(), totalTimeSec, "started");
    }

    // [ 세션 종료 ]
    @Transactional
    public void endSession(SessionEndRequest request) {

        ExerciseSession session = exerciseSessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "세션을 찾을 수 없습니다. sessionId=" + request.getSessionId()));

        session.end();

        log.info("[Session] 세션 종료: sessionId={} totalElapsed={}s",
                request.getSessionId(), request.getTotalElapsed());
    }
}
