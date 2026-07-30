//BE → AI, /predict 요청
package com.axis.nachimbal.domain.running.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AiPredictRequest {

    @JsonProperty("session_id")
    private String sessionId;

    private List<AiWindowItem> window;
}