package com.axis.nachimbal.domain.chatbot.dto;

public record ExerciseSummaryDto(
        Long sessionCount,
        Double avgDistanceKm,
        Double avgSpeedKmh,
        Long totalDurationSec,
        Double avgHeartRate,       // 심박 측정된 세션만 평균, 하나도 없으면 null
        Double totalCaloriesKcal
) {
    public String toPromptContext() {
        if (sessionCount == null || sessionCount == 0) {
            return "이 기간에는 러닝 기록 없음";
        }
        return """
                러닝 횟수: %d회
                평균 거리: %.2fkm
                평균 속도: %.1fkm/h
                총 운동 시간: %d분
                평균 심박수: %s
                총 소모 칼로리: %.0fkcal""".formatted(
                sessionCount,
                avgDistanceKm,
                avgSpeedKmh,
                totalDurationSec / 60,
                avgHeartRate != null ? Math.round(avgHeartRate) + " bpm" : "측정 기록 없음",
                totalCaloriesKcal
        );
    }
}