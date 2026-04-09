package com.axis.nachimbal.domain.navigation.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "search_history")
@Getter
@NoArgsConstructor
public class SearchHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "search_history_id")
    private Long searchHistoryId;

    // 로그인 없이 개발하는 동안 user_id를 임시 고정값으로 사용
    // 나중에 로그인 개발 시 JWT에서 꺼낸 userId로 교체만 하면 됨
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "keyword")
    private String keyword;           // 검색어 (선택)

    // 검색 후 선택한 장소 (place 테이블과 연결)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id")
    private Place place;

    @Column(name = "searched_at", nullable = false)
    private LocalDateTime searchedAt;
}
