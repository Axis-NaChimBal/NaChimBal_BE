package com.axis.nachimbal.domain.chatbot.service;

import com.axis.nachimbal.domain.chatbot.dto.ExerciseSummaryDto;
import com.axis.nachimbal.domain.result.repository.ExerciseResultRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ExerciseSummaryService {

    private final ExerciseResultRepository resultRepository;

    public ExerciseSummaryService(ExerciseResultRepository resultRepository) {
        this.resultRepository = resultRepository;
    }

    public ExerciseSummaryDto getWeeklySummary(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekAgo = now.minusDays(7);
        return resultRepository.findSummary(userId, weekAgo, now);
    }

    public ExerciseSummaryDto getMonthlySummary(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime monthAgo = now.minusMonths(1);
        return resultRepository.findSummary(userId, monthAgo, now);
    }

    // 신규 유저 / 세션 0건 처리 — 프롬프트 조립 단계에서 이 케이스를 분기해서 "아직 러닝 기록이 없다"는 걸 시스템 프롬프트에 명시해줘야 함
    public boolean hasNoData(ExerciseSummaryDto summary) {
        return summary.sessionCount() == 0;
    }
}