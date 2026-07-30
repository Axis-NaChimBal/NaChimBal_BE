package com.axis.nachimbal.domain.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.misc.NotNull;

@Getter
@NoArgsConstructor
public class UpdatePaceControlRequest {
    @NotNull
    private Boolean paceControlEnabled;
}