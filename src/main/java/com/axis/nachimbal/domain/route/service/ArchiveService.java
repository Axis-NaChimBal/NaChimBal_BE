package com.axis.nachimbal.domain.route.service;

import com.axis.nachimbal.domain.route.dto.*;
import com.axis.nachimbal.domain.route.entity.PolylineCodec;
import com.axis.nachimbal.domain.route.entity.Route;
import com.axis.nachimbal.domain.route.entity.RouteCategory;
import com.axis.nachimbal.domain.route.entity.Waypoint;
import com.axis.nachimbal.domain.route.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.axis.nachimbal.domain.route.entity.GpxParser;
import org.springframework.web.multipart.MultipartFile;

import java.util.stream.Collectors;
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

    @Transactional
    public RouteSummaryResponse createGpsArt(String name, String address, MultipartFile gpxFile) {
        // 1. GPX 파싱
        List<GpxParser.GpxPoint> points = GpxParser.parse(gpxFile);

        // 2. 거리 계산
        double distanceKm = GpxParser.calculateDistanceKm(points);

        // 3. fullPath 변환 ({lat,lng} → PathPointRequest 형태로 encode)
        List<PathPointRequest> fullPath = points.stream()
                .map(p -> {
                    PathPointRequest req = new PathPointRequest();
                    return req;
                })
                .collect(Collectors.toList());

        // PathPointRequest에 직접 값을 넣을 수 없으니 PolylineCodec용 LatLng 리스트로 직접 변환
        List<PolylineCodec.LatLng> latLngs = points.stream()
                .map(p -> new PolylineCodec.LatLng(p.lat(), p.lng()))
                .collect(Collectors.toList());

        String polyline;
        try {
            polyline = new com.fasterxml.jackson.databind.ObjectMapper()
                    .writeValueAsString(latLngs);
        } catch (Exception e) {
            throw new IllegalStateException("polyline 직렬화 실패", e);
        }

        // 4. waypoints (GPX 포인트를 그대로 waypoints로 저장)
        Route route = Route.builder()
                .userId(null)
                .name(name)
                .category(RouteCategory.GPS_ART)
                .totalDistanceKm(distanceKm)
                .estimatedTimeSec(null)
                .polyline(polyline)
                .address(address)
                .imageUrl(null)
                .build();

        // GPX 포인트를 waypoints로 저장 (전체 다 넣으면 너무 많으니 일정 간격으로 샘플링)
        int step = Math.max(1, points.size() / 20); // 최대 20개 waypoint
        for (int i = 0; i < points.size(); i += step) {
            GpxParser.GpxPoint p = points.get(i);
            route.addWaypoint(Waypoint.builder()
                    .orderIndex(i / step)
                    .latitude(p.lat())
                    .longitude(p.lng())
                    .label(null)
                    .build());
        }

        Route saved = routeRepository.save(route);
        log.info("GPS 아트 저장 완료: routeId={}, name={}", saved.getId(), name);
        return RouteSummaryResponse.from(saved);
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