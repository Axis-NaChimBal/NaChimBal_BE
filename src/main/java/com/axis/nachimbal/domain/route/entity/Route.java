package com.axis.nachimbal.domain.route.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "routes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 20)
    private RouteCategory category;

    @Column(name = "total_distance_km", nullable = false)
    private Double totalDistanceKm;

    @Column(name = "estimated_time_sec")
    private Integer estimatedTimeSec;

    @Lob
    @Column(name = "polyline", nullable = false, columnDefinition = "TEXT")
    private String polyline;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "address")
    private String address;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "favorite", nullable = false)
    private boolean favorite = false;

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    private List<Waypoint> waypoints = new ArrayList<>();

    @Builder
    public Route(Long userId, String name, RouteCategory category, Double totalDistanceKm,
                 Integer estimatedTimeSec, String polyline, String imageUrl, String address) {
        this.userId = userId;
        this.name = name;
        this.category = category;
        this.totalDistanceKm = totalDistanceKm;
        this.estimatedTimeSec = estimatedTimeSec;
        this.polyline = polyline;
        this.imageUrl = imageUrl;
        this.address = address;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public void addWaypoint(Waypoint waypoint) {
        this.waypoints.add(waypoint);
        waypoint.assignRoute(this);
    }
}