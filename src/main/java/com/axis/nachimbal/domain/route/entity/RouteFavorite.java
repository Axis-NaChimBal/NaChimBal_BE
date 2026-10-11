package com.axis.nachimbal.domain.route.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "route_favorites",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_route_favorite_user_route", columnNames = {"user_id", "route_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RouteFavorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "route_id", nullable = false)
    private Long routeId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public RouteFavorite(Long userId, Long routeId) {
        this.userId = userId;
        this.routeId = routeId;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
