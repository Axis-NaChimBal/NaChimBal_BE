package com.axis.nachimbal.domain.route.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "waypoints")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Waypoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Column(name = "label")
    private String label;

    @Builder
    public Waypoint(Integer orderIndex, Double latitude, Double longitude, String label) {
        this.orderIndex = orderIndex;
        this.latitude = latitude;
        this.longitude = longitude;
        this.label = label;
    }

    void assignRoute(Route route) {
        this.route = route;
    }
}