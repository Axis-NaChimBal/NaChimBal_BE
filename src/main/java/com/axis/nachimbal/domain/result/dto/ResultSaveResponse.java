package com.axis.nachimbal.domain.result.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResultSaveResponse {
    private Long sessionId;
    private Double distanceKm;
    private Integer durationSec;
    private Double avgSpeedKmh;
    private Integer avgHeartRate;
    private Integer maxHeartRate;
    private Integer minHeartRate;
    private Double caloriesKcal;
    private Integer paceAdjustCount;
}