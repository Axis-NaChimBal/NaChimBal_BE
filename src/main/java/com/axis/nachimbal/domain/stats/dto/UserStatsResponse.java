package com.axis.nachimbal.domain.stats.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class UserStatsResponse {

    private Integer age;
    private boolean paceControlEnabled;
    private Integer restingHr;
    private double       monthlyGoalPercent;
    private double       avgDistance;
    private int          avgDurationSec;
    private double       avgSpeed;
    private double       maxRecord;
    private double       totalDistance;
    private List<String> streaks;
    private Double       weeklyGoal;
    private Double       monthlyGoal;
}