package com.axis.nachimbal.domain.running.service;

import com.axis.nachimbal.domain.running.client.AiServerClient;
import com.axis.nachimbal.domain.running.dto.SessionDataRequest;
import com.axis.nachimbal.domain.running.dto.SessionDataResponse;
import com.axis.nachimbal.domain.running.dto.SessionEndRequest;
import com.axis.nachimbal.domain.running.dto.SessionStartRequest;
import com.axis.nachimbal.domain.running.dto.SessionStartResponse;
import com.axis.nachimbal.domain.running.dto.ai.AiPredictResponse;
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

    private static final int REQUIRED_WINDOW_SIZE = 24;

    private final UserRepository userRepository;
    private final ExerciseSessionRepository exerciseSessionRepository;
    private final AiServerClient aiServerClient;

    private static double getSpeedMps(GoalType goal) {
        return switch (goal) {
            case INJURY_RECOVERY -> 2.08;
            case BEGINNER        -> 2.38;
            case DIET            -> 2.56;
            case ENDURANCE       -> 2.78;
            case MARATHON_PACE   -> 3.03;
            case SPEED_UP        -> 3.33;
            case INTERVAL        -> 3.70;
            case SPRINT          -> 4.76;
        };
    }

    // [ 세션 시작 ]
    @Transactional
    public SessionStartResponse startSession(SessionStartRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "사용자를 찾을 수 없습니다. userId=" + request.getUserId()));

        double speedMps  = getSpeedMps(request.getGoal());
        int totalTimeSec = (int) Math.round((request.getRouteDistance() * 1000) / speedMps);
        int hrRest = user.getRestingHr() != null ? user.getRestingHr() : 60;

        ExerciseSession session = ExerciseSession.builder()
                .userId(user.getId())
                .exerciseGoal(request.getGoal())
                .routeDistanceKm(request.getRouteDistance())
                .totalTimeSec(totalTimeSec)
                .build();

        ExerciseSession saved = exerciseSessionRepository.save(session);

        log.info("[Session] 세션 시작: sessionId={} userId={} goal={} totalTimeSec={}",
                saved.getId(), user.getId(), request.getGoal(), totalTimeSec);

        // AI 서버에 세션 등록 (target_zone 없음, resting_hr/age/target_duration만 전달)
        aiServerClient.initSession(saved.getId(), hrRest, user.getAge(), totalTimeSec);

        return new SessionStartResponse(saved.getId(), totalTimeSec, "started");
    }

    // [ 세션 종료 ]
    @Transactional
    public void endSession(SessionEndRequest request) {
        ExerciseSession session = exerciseSessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "세션을 찾을 수 없습니다. sessionId=" + request.getSessionId()));

        session.end();
        aiServerClient.endSession(request.getSessionId());

        log.info("[Session] 세션 종료: sessionId={} totalElapsed={}s",
                request.getSessionId(), request.getTotalElapsed());
    }

    // [ 러닝 중 데이터 처리 ]
    @Transactional(readOnly = true)
    public SessionDataResponse processSessionData(SessionDataRequest request) {

        // window가 24개 미만이면 AI 호출 스킵 (초반 데이터 부족 구간)
        if (request.getWindow().size() < REQUIRED_WINDOW_SIZE) {
            log.debug("[Service] window 부족({}개), AI 호출 스킵: sessionId={}",
                    request.getWindow().size(), request.getSessionId());
            return SessionDataResponse.builder()
                    .sessionId(request.getSessionId())
                    .label("NORMAL")
                    .idealPace(null)
                    .confidence(null)
                    .consecutiveCount(0)
                    .build();
        }

        AiPredictResponse aiResponse = aiServerClient.predict(request.getSessionId(), request.getWindow());

        if (aiResponse == null) {
            // AI 서버 호출 실패 시 안전값 반환 (앱이 죽지 않도록)
            return SessionDataResponse.builder()
                    .sessionId(request.getSessionId())
                    .label("NORMAL")
                    .idealPace(null)
                    .confidence(0.0)
                    .consecutiveCount(0)
                    .build();
        }

        // recommended_speed(m/s) → idealPace(min/km) 변환
        Double idealPace = null;
        if (aiResponse.getRecommendedSpeed() != null && aiResponse.getRecommendedSpeed() > 0) {
            idealPace = 1000.0 / (aiResponse.getRecommendedSpeed() * 60);
        }

        return SessionDataResponse.builder()
                .sessionId(request.getSessionId())
                .label(aiResponse.getLabel())
                .idealPace(idealPace)
                .confidence(aiResponse.getConfidence())
                .consecutiveCount(aiResponse.getConsecutiveCount())
                .build();
    }
}