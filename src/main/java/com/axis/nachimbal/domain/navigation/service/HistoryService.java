package com.axis.nachimbal.domain.navigation.service;

import com.axis.nachimbal.domain.navigation.dto.RecentPlaceDto;
import com.axis.nachimbal.domain.navigation.repository.SearchHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HistoryService {

    private final SearchHistoryRepository searchHistoryRepository;

    // ── 로그인 전 임시 고정 userId ────────────────────────────────
    // 나중에 로그인 개발 시 이 상수를 제거하고,
    // 메서드 파라미터로 userId를 받도록 교체만 하면 됨
    private static final Long TEMP_USER_ID = 1L;

    // 최근 목적지 조회
    @Transactional(readOnly = true)
    public List<RecentPlaceDto> getRecentPlaces(int limit) {
        return searchHistoryRepository
                .findRecentByUserId(TEMP_USER_ID, limit)
                .stream()
                .map(RecentPlaceDto::new)
                .toList();
    }
}
