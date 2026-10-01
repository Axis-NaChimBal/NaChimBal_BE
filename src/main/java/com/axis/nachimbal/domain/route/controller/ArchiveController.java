package com.axis.nachimbal.domain.route.controller;

import com.axis.nachimbal.domain.route.dto.RouteCreateRequest;
import com.axis.nachimbal.domain.route.dto.RouteSummaryResponse;
import com.axis.nachimbal.domain.route.entity.Route;
import com.axis.nachimbal.domain.route.entity.RouteCategory;
import com.axis.nachimbal.domain.route.service.ArchiveService;
import com.axis.nachimbal.domain.route.dto.RouteDetailResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
public class ArchiveController {

    private final ArchiveService routeService;

    @PostMapping
    public ResponseEntity<RouteSummaryResponse> createRoute(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody RouteCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(routeService.createRoute(userId, request));
    }

    @GetMapping
    public ResponseEntity<List<RouteSummaryResponse>> getRoutes(
            @AuthenticationPrincipal Long userId,
            @RequestParam("category") RouteCategory category) {
        return ResponseEntity.ok(routeService.getRoutes(userId, category));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoute(
            @AuthenticationPrincipal Long userId,
            @PathVariable("id") Long id) {
        routeService.deleteRoute(userId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<RouteDetailResponse> getRouteDetail(@PathVariable("id") Long id) {
        return ResponseEntity.ok(routeService.getRouteDetail(id));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<String> handleNotFound(NoSuchElementException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @PatchMapping("/{id}/favorite")
    public ResponseEntity<RouteSummaryResponse> updateFavorite(
            @PathVariable("id") Long id,
            @RequestBody FavoriteRequest request) {
        Route updated = routeService.updateFavorite(id, request.favorite());
        return ResponseEntity.ok(RouteSummaryResponse.from(updated));
    }

    // 요청 body를 받을 DTO
    public record FavoriteRequest(boolean favorite) {}
}