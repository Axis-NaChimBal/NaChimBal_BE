package com.axis.nachimbal.domain.session.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.misc.NotNull;

@Getter
@AllArgsConstructor
public class SessionSaveResponse {
    private Long sessionId;
    private String startedAt;
    private String endedAt;
    private Double distance;
    private Integer durationSec;
    private Double avgSpeed;
}