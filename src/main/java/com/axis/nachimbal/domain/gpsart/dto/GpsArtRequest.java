package com.axis.nachimbal.domain.gpsart.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

// [ GPS 아트 생성 요청 ] : 프론트(RouteSetupScreen)에서 전달하는 중심 좌표 + 탐색 반경
@Getter
@NoArgsConstructor
public class GpsArtRequest {

    private Double latitude;
    private Double longitude;
    private Integer radius; // 도로 그래프 탐색 반경(m). 프론트 GPS_ART_RADIUS_METERS와 동일 값 사용
}