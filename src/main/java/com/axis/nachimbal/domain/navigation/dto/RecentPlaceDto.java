package com.axis.nachimbal.domain.navigation.dto;

import com.axis.nachimbal.domain.navigation.entity.SearchHistory;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class RecentPlaceDto {

    private final Long placeId;
    private final String name;        // 장소명
    private final String address;     // 주소
    private final BigDecimal lat;     // 위도
    private final BigDecimal lng;     // 경도
    private final LocalDateTime searchedAt; // 검색 시각

    // SearchHistory 엔티티 → DTO 변환
    public RecentPlaceDto(SearchHistory history) {
        this.placeId    = history.getPlace().getPlaceId();
        this.name       = history.getPlace().getName();
        this.address    = history.getPlace().getAddress();
        this.lat        = history.getPlace().getLat();
        this.lng        = history.getPlace().getLng();
        this.searchedAt = history.getSearchedAt();
    }
}