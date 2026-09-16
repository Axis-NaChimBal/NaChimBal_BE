package com.axis.nachimbal.domain.result.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.misc.NotNull;

@Getter
@NoArgsConstructor
public class ResultSaveRequest {
    @NotNull
    private Double distance;
    @NotNull
    private Integer durationSec;
    @NotNull
    private Double avgSpeed;
    @NotNull
    private Integer avgHeartRate;
    @NotNull
    private Integer maxHeartRate;
    @NotNull
    private Integer minHeartRate;
    @NotNull
    private Double caloriesKcal;
    @NotNull
    private Integer paceAdjustCount;
}