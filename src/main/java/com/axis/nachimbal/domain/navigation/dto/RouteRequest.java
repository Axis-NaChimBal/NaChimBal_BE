package com.axis.nachimbal.domain.navigation.dto;

import lombok.Data;

@Data
public class RouteRequest {
    private String startName; // 출발지 이름
    private double startX;   // 출발지 경도
    private double startY;   // 출발지 위도

    private String endName;   // 도착지 이름
    private double endX;     // 도착지 경도
    private double endY;     // 도착지 위도
}