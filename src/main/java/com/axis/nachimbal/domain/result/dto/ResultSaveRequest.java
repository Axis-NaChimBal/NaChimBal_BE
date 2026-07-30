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
}