package com.axis.nachimbal.domain.route.dto;

import com.axis.nachimbal.domain.route.entity.RouteCategory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class RouteCreateRequest {

    private String name;

    @NotNull(message = "category는 필수입니다.")
    private RouteCategory category;

    @NotEmpty(message = "경유지(waypoints)는 최소 1개 이상이어야 합니다.")
    @Valid
    private List<WaypointRequest> waypoints;

    @NotEmpty(message = "경로 좌표(fullPath)가 비어 있습니다.")
    @Valid
    private List<PathPointRequest> fullPath;

    @NotNull(message = "totalDistance는 필수입니다.")
    private Double totalDistance;

    private Integer totalTime;
}
