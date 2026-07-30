//BE → AI, 세션 시작
package com.axis.nachimbal.domain.running.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AiSessionStartRequest {

    @JsonProperty("session_id")
    private String sessionId;

    @JsonProperty("resting_hr")
    private Double restingHr;

    private Integer age;

    @JsonProperty("target_duration")
    private Double targetDuration;
}