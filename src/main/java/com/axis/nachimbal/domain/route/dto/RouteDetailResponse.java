package com.axis.nachimbal.domain.route.dto;

import com.axis.nachimbal.domain.route.entity.PolylineCodec;
import com.axis.nachimbal.domain.route.entity.Route;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class RouteDetailResponse {

    private String id;
    private String name;
    private String category;
    private String address;
    private Double totalDistanceKm;
    private Integer estimatedTimeSec;
    private List<PathPointResponse> fullPath;
    private List<WaypointResponse> waypoints;

    @Getter
    @Builder
    public static class PathPointResponse {
        private Double x; // 경도
        private Double y; // 위도
    }

    @Getter
    @Builder
    public static class WaypointResponse {
        private Double latitude;
        private Double longitude;
        private String label;
    }

    public static RouteDetailResponse from(Route route) {
        List<PathPointResponse> fullPath = PolylineCodec.decode(route.getPolyline()).stream()
                .map(p -> PathPointResponse.builder()
                        .x(p.lng())
                        .y(p.lat())
                        .build())
                .toList();

        return RouteDetailResponse.builder()
                .id(String.valueOf(route.getId()))
                .name(route.getName())
                .category(route.getCategory().name())
                .address(route.getAddress())
                .totalDistanceKm(route.getTotalDistanceKm())
                .estimatedTimeSec(route.getEstimatedTimeSec())
                .fullPath(fullPath)
                .waypoints(route.getWaypoints().stream()
                        .map(w -> WaypointResponse.builder()
                                .latitude(w.getLatitude())
                                .longitude(w.getLongitude())
                                .label(w.getLabel())
                                .build())
                        .toList())
                .build();
    }
}
