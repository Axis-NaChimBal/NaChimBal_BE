package com.axis.nachimbal.domain.challenge.dto;

import com.axis.nachimbal.domain.challenge.enums.PeriodType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.misc.NotNull;

@Getter
@NoArgsConstructor
public class GoalSaveRequest {

    @NotNull
    private PeriodType periodType;

    @NotNull
    private Double goalDistance;
}