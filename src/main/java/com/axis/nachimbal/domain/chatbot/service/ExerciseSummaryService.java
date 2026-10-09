package com.axis.nachimbal.domain.chatbot.service;

import com.axis.nachimbal.domain.chatbot.dto.ExerciseSummaryDto;
import com.axis.nachimbal.domain.result.entity.ExerciseResult;
import com.axis.nachimbal.domain.result.repository.ExerciseResultRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

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

    // 가장 최근 종료된 러닝 1건 조회 — 결과가 없으면 신규 유저(기록 0건)로 보고 ChatService에서 "기록 없음"을 프롬프트에 명시
    public Optional<ExerciseResult> getLatestSession(Long userId) {
        return resultRepository.findLatestSessions(userId, PageRequest.of(0, 1))
                .stream()
                .findFirst();
    }
}