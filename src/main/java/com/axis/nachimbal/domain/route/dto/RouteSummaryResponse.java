package com.axis.nachimbal.domain.route.dto;

import com.axis.nachimbal.domain.route.entity.Route;
import lombok.Builder;
import lombok.Getter;

import java.util.Locale;

@Getter
@Builder
public class RouteSummaryResponse {

    private String id;
    private String name;
    private String distance;
    private String address;
    private String imageUrl;
    private boolean favorite;

    public static RouteSummaryResponse from(Route route) {
        return RouteSummaryResponse.builder()
                .id(String.valueOf(route.getId()))
                .name(route.getName())
                .distance(formatDistance(route.getTotalDistanceKm()))
                .address(route.getAddress())
                .imageUrl(route.getImageUrl())
                .favorite(route.isFavorite())
                .build();
    }

    private static String formatDistance(Double km) {
        if (km == null) return null;
        if (km == Math.floor(km)) {
            return String.format(Locale.KOREA, "%dkm", km.intValue());
        }
        return String.format(Locale.KOREA, "%.1fkm", km);
    }
}
