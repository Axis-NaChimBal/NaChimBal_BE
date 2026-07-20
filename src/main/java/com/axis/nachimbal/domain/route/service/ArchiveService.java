package com.axis.nachimbal.domain.route.service;

import com.axis.nachimbal.domain.route.dto.RouteCreateRequest;
import com.axis.nachimbal.domain.route.dto.RouteSummaryResponse;
import com.axis.nachimbal.domain.route.dto.WaypointRequest;
import com.axis.nachimbal.domain.route.entity.PolylineCodec;
import com.axis.nachimbal.domain.route.entity.Route;
import com.axis.nachimbal.domain.route.entity.RouteCategory;
import com.axis.nachimbal.domain.route.entity.Waypoint;
import com.axis.nachimbal.domain.route.repository.RouteRepository;
import com.axis.nachimbal.domain.route.dto.RouteDetailResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArchiveService {

    private final RouteRepository routeRepository;
    private final GeocodingService geocodingService;

    @Transactional
    public RouteSummaryResponse createRoute(Long userId, RouteCreateRequest request) {
        String polyline = PolylineCodec.encode(request.getFullPath());
        String address = resolveAddress(request);

        Route route = Route.builder()
                .userId(userId)
                .name(resolveName(request.getName()))
                .category(request.getCategory())
                .totalDistanceKm(metersToKm(request.getTotalDistance()))
                .estimatedTimeSec(request.getTotalTime())
                .polyline(polyline)
                .address(address)
                .build();

        int order = 0;
        for (WaypointRequest wp : request.getWaypoints()) {
            route.addWaypoint(Waypoint.builder()
                    .orderIndex(order++)
                    .latitude(wp.getLatitude())
                    .longitude(wp.getLongitude())
                    .label(wp.getLabel())
                    .build());
        }

        Route saved = routeRepository.save(route);
        log.info("경로 저장 완료: routeId={}, userId={}, category={}", saved.getId(), userId, saved.getCategory());
        return RouteSummaryResponse.from(saved);
    }

    public List<RouteSummaryResponse> getRoutes(Long userId, RouteCategory category) {
        List<Route> routes = (category == RouteCategory.MY_ROUTE)
                ? routeRepository.findByUserIdAndCategoryOrderByCreatedAtDesc(userId, category)
                : routeRepository.findByCategoryOrderByCreatedAtDesc(category);

        return routes.stream().map(RouteSummaryResponse::from).toList();
    }

    @Transactional
    public void deleteRoute(Long userId, Long routeId) {
        Route route = routeRepository.findByIdAndUserId(routeId, userId)
                .orElseThrow(() -> new NoSuchElementException(
                        "삭제할 경로를 찾을 수 없거나 삭제 권한이 없습니다. id=" + routeId));
        routeRepository.delete(route);
        log.info("경로 삭제 완료: routeId={}, userId={}", routeId, userId);
    }

    public RouteDetailResponse getRouteDetail(Long routeId) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new NoSuchElementException(
                        "경로를 찾을 수 없습니다. id=" + routeId));
        return RouteDetailResponse.from(route);
    }

    @Transactional
    public Route updateFavorite(Long id, boolean favorite) {
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("경로를 찾을 수 없습니다. id=" + id));
        route.setFavorite(favorite);
        return route; // JPA dirty checking으로 자동 UPDATE
    }

    private String resolveAddress(RouteCreateRequest request) {
        PolylineCodec.LatLng first = PolylineCodec.firstPoint(request.getFullPath());
        Optional<String> address = geocodingService.reverseGeocode(first.lat(), first.lng());
        if (address.isEmpty()) {
            log.info("주소 변환 결과 없음 (lat={}, lng={}) - address는 null로 저장됩니다.", first.lat(), first.lng());
        }
        return address.orElse(null);
    }

    private String resolveName(String name) {
        return (name == null || name.isBlank()) ? "이름 없는 경로" : name;
    }

    private double metersToKm(double meters) {
        return Math.round((meters / 1000.0) * 10) / 10.0;
    }
}