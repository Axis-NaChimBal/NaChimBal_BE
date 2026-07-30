package com.axis.nachimbal.domain.session.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.misc.NotNull;

@Getter
@NoArgsConstructor
public class SessionSaveRequest {

    @NotNull
    private String startedAt;

    @NotNull
    private String endedAt;

    @NotNull
    private Double distance;

    @NotNull
    private Integer durationSec;

    @NotNull
    private Double avgSpeed;
}