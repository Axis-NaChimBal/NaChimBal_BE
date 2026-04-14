package com.axis.nachimbal.domain.navigation.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "place")
@Getter
@NoArgsConstructor
public class Place {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "place_id")
    private Long placeId;

    @Column(name = "provider_place_id")
    private String providerPlaceId;   // T-map 등 외부 API 장소 ID

    @Column(name = "name", nullable = false)
    private String name;              // 장소명

    @Column(name = "address", nullable = false)
    private String address;           // 주소

    @Column(name = "lat", nullable = false, precision = 10, scale = 7)
    private BigDecimal lat;           // 위도

    @Column(name = "lng", nullable = false, precision = 10, scale = 7)
    private BigDecimal lng;           // 경도
}