package com.axis.nachimbal.domain.route.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PathPointRequest {

    @NotNull(message = "fullPath의 x(경도) 값은 필수입니다.")
    private Double x;

    @NotNull(message = "fullPath의 y(위도) 값은 필수입니다.")
    private Double y;
}