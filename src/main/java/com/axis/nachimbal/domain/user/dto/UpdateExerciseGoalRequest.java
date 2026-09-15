package com.axis.nachimbal.domain.user.dto;

import com.axis.nachimbal.domain.running.entity.GoalType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateExerciseGoalRequest {
    @NotNull
    private GoalType exerciseGoal;
}
