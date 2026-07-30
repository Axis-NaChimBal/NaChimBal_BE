package com.axis.nachimbal.domain.challenge.dto;

import com.axis.nachimbal.domain.challenge.enums.PeriodType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GoalSaveResponse {
    private Long goalId;
    private PeriodType periodType;
    private Double goalDistance;
}