package com.axis.nachimbal.domain.navigation.dto;

import com.axis.nachimbal.domain.navigation.enums.HapticType;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RouteResponse {

    private int totalDistance;
    private int totalTime;
    private double noticeDistance;
    private List<StepInfo> steps;

    @Data
    @Builder
    public static class StepInfo {
        private String description;
        private String turnType;

        // 회전 좌표 + 다음 회전까지의 거리
        private double pointX;
        private double pointY;
        private int distance;

        // 햅틱 알림 좌표
        private double noticePointX;
        private double noticePointY;

        // 햅틱 패턴
        private HapticType hapticType;
    }
}