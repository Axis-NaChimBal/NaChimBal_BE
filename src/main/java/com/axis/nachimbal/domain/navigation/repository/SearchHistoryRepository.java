package com.axis.nachimbal.domain.navigation.repository;

import com.axis.nachimbal.domain.navigation.entity.SearchHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {

    // 특정 유저의 최근 검색 기록을 시간 역순으로 가져옴
    // place가 null인 항목(장소 선택 안 한 것)은 제외
    @Query("""
        SELECT h FROM SearchHistory h
        WHERE h.userId = :userId
          AND h.place IS NOT NULL
        ORDER BY h.searchedAt DESC
        LIMIT :limit
    """)
    List<SearchHistory> findRecentByUserId(
            @Param("userId") Long userId,
            @Param("limit")  int limit
    );
}