package com.axis.nachimbal.domain.result.repository;

import com.axis.nachimbal.domain.chatbot.dto.ExerciseSummaryDto;
import com.axis.nachimbal.domain.result.entity.ExerciseResult;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ExerciseResultRepository extends JpaRepository<ExerciseResult, Long> {
    List<ExerciseResult> findAllBySession_UserId(Long userId);

    @Query("""
        SELECT new com.axis.nachimbal.domain.chatbot.dto.ExerciseSummaryDto(
            COUNT(r.id),
            COALESCE(AVG(r.distanceKm), 0),
            COALESCE(AVG(r.avgSpeedKmh), 0),
            COALESCE(SUM(r.durationSec), 0),
            AVG(NULLIF(r.avgHeartRate, 0)),
            COALESCE(SUM(r.caloriesKcal), 0.0)
        )
        FROM ExerciseResult r
        JOIN r.session s
        WHERE s.userId = :userId
          AND s.startedAt BETWEEN :from AND :to
          AND s.sessionStatus = com.axis.nachimbal.domain.running.entity.SessionStatus.ENDED
        """)
    ExerciseSummaryDto findSummary(
            @Param("userId") Long userId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    // "지난 러닝이랑 비교" 질문용 — LIMIT 대신 Pageable로 1건만 제한
    @Query("""
        SELECT r
        FROM ExerciseResult r
        JOIN FETCH r.session s
        WHERE s.userId = :userId
          AND s.sessionStatus = com.axis.nachimbal.domain.running.entity.SessionStatus.ENDED
        ORDER BY s.startedAt DESC
        """)
    List<ExerciseResult> findLatestSessions(@Param("userId") Long userId, Pageable pageable);
}