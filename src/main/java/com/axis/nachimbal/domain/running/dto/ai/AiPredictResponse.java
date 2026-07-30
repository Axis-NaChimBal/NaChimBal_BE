//AI → BE 응답
package com.axis.nachimbal.domain.running.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AiPredictResponse {

    @JsonProperty("session_id")
    private String sessionId;

    private String label;
    private Double confidence;

    @JsonProperty("recommended_speed")
    private Double recommendedSpeed;

    @JsonProperty("consecutive_count")
    private Integer consecutiveCount;
}