package com.axis.nachimbal.domain.gpsart.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

// [ GPS 아트 생성 응답 ] : AI 서버 결과를 프론트로 그대로 전달
@Getter
@Builder
public class GpsArtResponse {

    private boolean found;
    private List<Coordinate> fullPath;      // 지도에 그릴 실제 경로 (x=경도, y=위도)
    private List<WaypointDto> waypoints;    // 경유지 목록 (steps 재계산용, 기존 아카이빙 구조와 동일)
    private Double totalDistanceMeters;
    private Double distScore;               // 도형 유사도 점수 (디버깅/로그용, 프론트에서 안 써도 됨)
    private String shape;                   // 생성된 도형 (heart, fish, cross, square)
    private Boolean exactMatch;             // 기준 점수 통과 여부 (false면 "가장 비슷한" 경로)

    @Getter
    @Builder
    public static class Coordinate {
        private double x;
        private double y;
    }

    @Getter
    @Builder
    public static class WaypointDto {
        private double latitude;
        private double longitude;
    }
}