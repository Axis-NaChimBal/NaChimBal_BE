package com.axis.nachimbal.domain.chatbot.dto;

public record ExerciseSummaryDto(
        long sessionCount,
        double avgDistanceKm,
        double avgSpeedKmh,
        long totalDurationSec
) {
    public static ExerciseSummaryDto empty() {
        return new ExerciseSummaryDto(0, 0.0, 0.0, 0);
    }

    // 프롬프트에 넣을 자연어 문장으로 변환
    public String toPromptContext() {
        if (sessionCount == 0) {
            return "이 사용자는 최근 러닝 기록이 없습니다.";
        }
        return String.format(
                "최근 %d회 러닝, 평균 거리 %.1fkm, 평균 속도 %.1fkm/h, 총 운동 시간 %d분",
                sessionCount, avgDistanceKm, avgSpeedKmh, totalDurationSec / 60
        );
    }
}