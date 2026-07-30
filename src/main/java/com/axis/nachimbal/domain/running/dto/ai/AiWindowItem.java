//BE → AI, window 배열 원소
package com.axis.nachimbal.domain.running.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AiWindowItem {

    @JsonProperty("heart_rate")
    private Double heartRate;

    private Double speed;
    private Double altitude;

    @JsonProperty("elapsed_time")
    private Double elapsedTime;
}